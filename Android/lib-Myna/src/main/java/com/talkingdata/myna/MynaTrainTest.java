package com.talkingdata.myna;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;

import com.talkingdata.myna.sensor.Feature;
import com.talkingdata.myna.sensor.SensorData;
import com.talkingdata.myna.sensor.SensorFeature;
import com.talkingdata.myna.tools.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Locale;

import dice.data.Instances;
import dice.data.io.ArffReader;
import dice.tree.builder.TreeBuilder;
import dice.tree.model.CBRRDTModel;
import dice.tree.structure.Node;

class MynaTrainTest {
    private MynaTrainTestCallback ttcallback;
    private Context context;
    private CBRRDTModel model;
    private int attrSize = 42;
    private int labelNum = 3;
    private int maxS = 10;
    private int treeNum = 3;
    private int maxTreeDepth = 21;
    private Node[] trees = new Node[treeNum];

    private SparseArray<Feature> rfFeatures = new SparseArray<>();
    private SparseIntArray rfLabels = new SparseIntArray();
    private SensorData[] periodVal;
    private final int FEATURE_DATA_BATCH_SIZE = 512;
    private final String ARF_FILE_NAME = "train.arff";

    MynaTrainTest(MynaTrainTestCallback callback, Context ctx) {
        context = ctx;
        ttcallback = callback;
        periodVal = new SensorData[FEATURE_DATA_BATCH_SIZE];
    }

    void startTrainingWithExistedData() {
        Utils.deleteFile("train.arff");
        Utils.deleteFile("classificator.json");

        ttcallback.onTrainingProgress("Training Walking...");
        Log.i(Utils.TAG, "Training Walking...");
        startTrainingWithExistedDataFromCSV(1);
        ttcallback.onTrainingProgress("Training In_vehicle...");
        Log.i(Utils.TAG, "Training In_vehicle...");
        startTrainingWithExistedDataFromCSV(4);
        ttcallback.onTrainingProgress("Training Still...");
        Log.i(Utils.TAG, "Training Still...");
        startTrainingWithExistedDataFromCSV(7);

        ttcallback.onTrainingProgress("Creating new train.arff file...");
        createTrainingDataset(rfFeatures, rfLabels);
        String modelPath = doTraining();

        if(modelPath != null && !modelPath.isEmpty()){
            ttcallback.onTrainingProgress("New model generated in: " + modelPath);
            ttcallback.onTrainingFinished("Finished training with existed data.");
        }else{
            ttcallback.onTrainingFinished("Exception happened during training.");
        }
    }

    private String doTraining(){
        File trainFile = new File(Environment.getExternalStorageDirectory(), "/rHAR/train.arff");
        String trainFilePath = trainFile.getAbsolutePath();
        ArffReader reader = new ArffReader();
        reader.setFilePath(trainFilePath);
        reader.setAttrSize(attrSize);
        Instances trainInstances = reader.getInstances();

        TreeBuilder treeBuilder = new TreeBuilder(0, TreeBuilder.CBR_RDT);
        treeBuilder.setInstances(trainInstances);
        treeBuilder.setMaxDeep(maxTreeDepth);
        treeBuilder.setMaxS(maxS);
        treeBuilder.setClsSize(labelNum);
        treeBuilder.init();
        trees = treeBuilder.buildTrees(labelNum);
        treeBuilder.clear();
        String newModel = getModelAsString(trainInstances.getAttributes());
        if(newModel != null && !newModel.isEmpty()){
            String savedPath = Utils.save(context, newModel, "classificator.json", 0);
            Log.i(Utils.TAG, "Created new trees!");
            return savedPath;
        }else{
            return null;
        }

//        model = new CBRRDTModel();
//        model.init(trees, trainInstances.getAttributes(), maxS);
    }

    private String getModelAsString(int[] attributes) {
        try {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("attrSize", String.valueOf(this.attrSize));
            jsonObj.put("labelNum", String.valueOf(this.labelNum));
            jsonObj.put("maxS", String.valueOf(this.maxS));
            jsonObj.put("treeNum", String.valueOf(this.treeNum));
            jsonObj.put("maxTreeDepth", String.valueOf(this.maxTreeDepth));
            jsonObj.put("attributes", Arrays.toString(attributes).replace("[", "").replace("】", ""));
            jsonObj.put("trees", getTreeJsonArray());
            return jsonObj.toString();

        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    private JSONArray getTreeJsonArray() {
        try {
            JSONArray jsonArray = new JSONArray();
            JSONObject jo;
            for (int i = 0; i < trees.length; i++) {
                jo = new JSONObject();
                jo.put("tree", Utils.toString(trees[i]));
                jsonArray.put(jo);
            }
            return jsonArray;

        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    private void startTrainingWithExistedDataFromCSV(final int label) {
        SparseArray<File> files = Utils.getDataFiles("/rHAR/data/" + String.valueOf(label));
        if (files == null)
            return;
        Log.i(Utils.TAG, String.format("Files of label %d are: %d.", label, files.size()));
        for (int i = 0; i < files.size(); ++i) {
            try {
                File file = files.valueAt(i);
                Log.i(Utils.TAG, String.format("Training file: %s.", file.getName()));
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                int count = 0;
                while ((line = br.readLine()) != null && count < FEATURE_DATA_BATCH_SIZE) {
                    line = line.replace("\n", "").trim();
                    if (!line.isEmpty()) {
                        String[] data = line.split(",");
                        if (data.length > 0) {
                            SensorData sd = new SensorData();
                            sd.accelerate[0] = Float.parseFloat(data[0]);
                            sd.accelerate[1] = Float.parseFloat(data[1]);
                            sd.accelerate[2] = Float.parseFloat(data[2]);
                            if (periodVal[count] == null) {
                                periodVal[count] = new SensorData();
                            }
                            periodVal[count].clone(sd);
                        }
                    }
                    ++count;
                }
                br.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            getFeature(periodVal, label);
        }
    }

    private void getFeature(SensorData[] sensorData, int label) {
        Feature feature = new Feature();
        feature.extractFeatures(sensorData);

        rfFeatures.put(rfFeatures.size(), feature);
        rfLabels.put(rfLabels.size(), label);
    }


    private void createTrainingDataset(SparseArray<Feature> rfFeatures, SparseIntArray rfLabels) {
        addFile();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rfFeatures.size(); ++i) {
            Feature ft = rfFeatures.get(i);
            sb.append(get1AxisData(ft.getSelectedFeatures()));
            sb.append(",");
            sb.append(switchClass(rfLabels.get(i)));
            sb.append("\n");
        }
        Utils.saveAsAppend(context.getApplicationContext(), sb.toString(), ARF_FILE_NAME);
    }

    private String switchClass(final int status) {
        String choClass = "";
        switch (status) {
            case 1:
                choClass = "1,0,0";
                break;
            case 4:
                choClass = "0,1,0";
                break;
            case 7:
                choClass = "0,0,1";
                break;
            default:
                break;
        }
        return choClass;
    }

    private String get1AxisData(final SensorFeature sf) {
        return sf.minx + "," +
                sf.miny + "," +
                sf.minz + "," +
                sf.maxx + "," +
                sf.maxy + "," +
                sf.maxz + "," +
                sf.avgx + "," +
                sf.avgy + "," +
                sf.avgz + "," +
                sf.varx + "," +
                sf.vary + "," +
                sf.varz + "," +
                sf.rangex + "," +
                sf.rangey + "," +
                sf.rangez + "," +
                sf.jumpx + "," +
                sf.jumpy + "," +
                sf.jumpz + "," +
                sf.fallx + "," +
                sf.fally + "," +
                sf.fallz + "," +
                sf.amp1x + "," +
                sf.amp1y + "," +
                sf.amp1z + "," +
                sf.amp2x + "," +
                sf.amp2y + "," +
                sf.amp2z + "," +
                sf.amp3x + "," +
                sf.amp3y + "," +
                sf.amp3z + "," +
                sf.freq1x + "," +
                sf.freq1y + "," +
                sf.freq1z + "," +
                sf.freq2x + "," +
                sf.freq2y + "," +
                sf.freq2z + "," +
                sf.freq3x + "," +
                sf.freq3y + "," +
                sf.freq3z;
    }

    private void addFile() {
        String strBuilder = "@relation MultiLabelData\n\n" +
                "@attribute Accelerometer_min_x numeric\n" +
                "@attribute Accelerometer_min_y numeric\n" +
                "@attribute Accelerometer_min_z numeric\n" +
                "@attribute Accelerometer_max_x numeric\n" +
                "@attribute Accelerometer_max_y numeric\n" +
                "@attribute Accelerometer_max_z numeric\n" +
                "@attribute Accelerometer_avg_x numeric\n" +
                "@attribute Accelerometer_avg_y numeric\n" +
                "@attribute Accelerometer_avg_z numeric\n" +
                "@attribute Accelerometer_var_x numeric\n" +
                "@attribute Accelerometer_var_y numeric\n" +
                "@attribute Accelerometer_var_z numeric\n" +
                "@attribute Accelerometer_range_x numeric\n" +
                "@attribute Accelerometer_range_y numeric\n" +
                "@attribute Accelerometer_range_z numeric\n" +
                "@attribute Accelerometer_jump_x numeric\n" +
                "@attribute Accelerometer_jump_y numeric\n" +
                "@attribute Accelerometer_jump_z numeric\n" +
                "@attribute Accelerometer_fall_x numeric\n" +
                "@attribute Accelerometer_fall_y numeric\n" +
                "@attribute Accelerometer_fall_z numeric\n" +
                "@attribute Accelerometer_amp1_x numeric\n" +
                "@attribute Accelerometer_amp1_y numeric\n" +
                "@attribute Accelerometer_amp1_z numeric\n" +
                "@attribute Accelerometer_amp2_x numeric\n" +
                "@attribute Accelerometer_amp2_y numeric\n" +
                "@attribute Accelerometer_amp2_z numeric\n" +
                "@attribute Accelerometer_amp3_x numeric\n" +
                "@attribute Accelerometer_amp3_y numeric\n" +
                "@attribute Accelerometer_amp3_z numeric\n" +
                "@attribute Accelerometer_freq1_x numeric\n" +
                "@attribute Accelerometer_freq1_y numeric\n" +
                "@attribute Accelerometer_freq1_z numeric\n" +
                "@attribute Accelerometer_freq2_x numeric\n" +
                "@attribute Accelerometer_freq2_y numeric\n" +
                "@attribute Accelerometer_freq2_z numeric\n" +
                "@attribute Accelerometer_freq3_x numeric\n" +
                "@attribute Accelerometer_freq3_y numeric\n" +
                "@attribute Accelerometer_freq3_z numeric\n" +
                "@attribute On_foot {0,1}\n" +
                "@attribute In_vehicle {0,1}\n" +
                "@attribute Still {0,1}\n" +
                "@data\n";

        Utils.saveAsAppend(context.getApplicationContext(), strBuilder, ARF_FILE_NAME);
    }

    private SparseIntArray y_true = new SparseIntArray();
    private SparseArray<Float> score_1 = new SparseArray<>();
    private SparseArray<Float> score_4 = new SparseArray<>();
    private SparseArray<Float> score_7 = new SparseArray<>();

    void startTestingWithExistedData() {
        if(!Utils.isFileExists(context, "classificator.json")){
            ttcallback.onFinalReport(null);
            return;
        }

        startTestingWithExistedData(1);
        startTestingWithExistedData(4);
        startTestingWithExistedData(7);

        for(int i = 0; i < y_true.size(); ++i){
            Utils.saveAsAppend(context,
                    String.format(Locale.getDefault(), "%d,%.6f,%.6f,%.6f\n",
                            y_true.valueAt(i), score_1.valueAt(i), score_4.valueAt(i), score_7.valueAt(i)),
                    "roc.log");
        }

        String RDTSettings = String.format(Locale.getDefault(), "RDTSettings - attrSize: %d, labelNum: %d, maxS: %d, treeNum: %d, maxTreeDepth: %d",
                attrSize, labelNum, maxS, treeNum, maxTreeDepth);
        ttcallback.onFinalReport(RDTSettings + "\n");
    }

    private void startTestingWithExistedData(final int label) {
        SparseArray<File> files = Utils.getDataFiles("/rHAR/data/test/" + String.valueOf(label));
        if (files == null)
            return;

        String trainedTrees = Utils.load(context, "classificator.json");
        RandomForestClassifier randomForestClassifier = new RandomForestClassifier(trainedTrees);
        for (int i = 0; i < files.size(); ++i) {
            File file = files.valueAt(i);
            try {
                Log.i("rHAR", String.format("Training file: %s.", file.getName()));
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                int count = 0;
                while ((line = br.readLine()) != null && count < FEATURE_DATA_BATCH_SIZE) {
                    line = line.replace("\n", "").trim();
                    if(!line.isEmpty()){
                        String[] data = line.split(",");
                        if(data.length > 0){
                            SensorData sd = new SensorData();
                            sd.accelerate[0] = Float.parseFloat(data[0]);
                            sd.accelerate[1] = Float.parseFloat(data[1]);
                            sd.accelerate[2] = Float.parseFloat(data[2]);
                            if (periodVal[count] == null){
                                periodVal[count] = new SensorData();
                            }
                            periodVal[count].clone(sd);
                        }
                    }
                    ++count;
                }
                br.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            double[] probRDT = randomForestClassifier.recognize(periodVal);
            long startTime = System.currentTimeMillis();
            long durationRDT = System.currentTimeMillis() - startTime;
            int[] act = {1, 4, 7};
            y_true.put(y_true.size(), label);
            score_1.put(score_1.size(), (float)probRDT[0]);
            score_4.put(score_4.size(), (float)probRDT[1]);
            score_7.put(score_7.size(), (float)probRDT[2]);
            ttcallback.onTestingResult(act, probRDT, durationRDT, file.getAbsolutePath(), label);
        }
    }
}
