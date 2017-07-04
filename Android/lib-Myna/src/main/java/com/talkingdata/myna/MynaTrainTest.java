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
import dice.tree.structure.Node;

class MynaTrainTest {
    private MynaTrainTestCallback ttcallback;
    private Context context;
    private int attrSize = 30;
    private int labelNum = 6;
    private int maxS = 10;
    private int treeNum = 3;
    private int maxTreeDepth = 15;
    private Node[] trees = new Node[treeNum];

    private SparseArray<Feature> rfFeatures = new SparseArray<>();
    private SparseIntArray rfLabels = new SparseIntArray();
    private SensorData[] periodVal;
    private int FEATURE_DATA_BATCH_SIZE = 128;
    private final String ARF_FILE_NAME = "train.arff";

    private final int SAMPLE_FREQUENCY = 20;

    private ClassifierInterface classifier;

    private int[] labels = {
            RecognizedActivity.WALKING,
            RecognizedActivity.RUNNING,
            RecognizedActivity.BUS,
            RecognizedActivity.SUBWAY,
            RecognizedActivity.CAR,
            RecognizedActivity.STILL};


    MynaTrainTest(MynaTrainTestCallback callback, Context ctx) {
        context = ctx;
        ttcallback = callback;
        periodVal = new SensorData[FEATURE_DATA_BATCH_SIZE];
    }

    void startTrainingWithExistedData() {
        Utils.deleteFile("train.arff");
        Utils.deleteFile("classificator.json");

        ttcallback.onTrainingProgress("Preparing data...");
        prepareDataForTraining();

        ttcallback.onTrainingProgress("Creating new train.arff file...");
        createTrainingDataset(rfFeatures, rfLabels);

        ttcallback.onTrainingProgress("Starting training...");
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
        trees = treeBuilder.buildTrees(treeNum);
        treeBuilder.clear();
        String newModel = getModelAsString(trainInstances.getAttributes());
        if(newModel != null && !newModel.isEmpty()){
            String savedPath = Utils.save(context, newModel, "classificator.json", 0);
            Log.i(Utils.TAG, "Created new trees!");
            return savedPath;
        }else{
            return null;
        }
    }

    private String getModelAsString(int[] attributes) {
        try {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("attrSize", String.valueOf(this.attrSize));
            jsonObj.put("labelNum", String.valueOf(this.labelNum));
            jsonObj.put("maxS", String.valueOf(this.maxS));
            jsonObj.put("treeNum", String.valueOf(this.treeNum));
            jsonObj.put("maxTreeDepth", String.valueOf(this.maxTreeDepth));
            jsonObj.put("attributes", Arrays.toString(attributes).replace("[", "").replace("]", ""));
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
            for (Node tree : trees) {
                jo = new JSONObject();
                jo.put("tree", Utils.toString(tree));
                jsonArray.put(jo);
            }
            return jsonArray;

        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    private void prepareDataForTraining() {
        SparseArray<File> files = Utils.getDataFiles("/rHAR/data/train/");
        if (files == null){
            ttcallback.onTrainingFinished("No training data found!");
            return;
        }

        for (int i = 0; i < files.size(); ++i) {
            File file = files.valueAt(i);
            int label = Integer.parseInt(file.getName().replace(".csv", ""));
            try {
                Log.i("rHAR", String.format("Training file: %s.", file.getName()));
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                int count = 0;
                while ((line = br.readLine()) != null) {
                    line = line.replace("\n", "").trim();
                    if(!line.isEmpty()){
                        String[] data = line.split(",");
                        if(data.length > 0){
                            SensorData sd = new SensorData();
                            sd.world_accelerometer[0] = Float.parseFloat(data[0]);
                            sd.world_accelerometer[1] = Float.parseFloat(data[1]);
                            sd.world_accelerometer[2] = Float.parseFloat(data[2]);
                            if (periodVal[count] == null){
                                periodVal[count] = new SensorData();
                            }
                            periodVal[count].clone(sd);
                        }
                    }
                    ++count;
                    if(count == FEATURE_DATA_BATCH_SIZE){
                        getFeature(periodVal, label);
                    }
                    count = count % FEATURE_DATA_BATCH_SIZE;
                }
                br.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void getFeature(SensorData[] sensorData, int label) {
        Feature feature = new Feature();
        feature.extractFeatures(sensorData, SAMPLE_FREQUENCY, FEATURE_DATA_BATCH_SIZE);

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
            case RecognizedActivity.WALKING:
                choClass = "1,0,0,0,0,0";
                break;
            case RecognizedActivity.RUNNING:
                choClass = "0,1,0,0,0,0";
                break;
            case RecognizedActivity.BUS:
                choClass = "0,0,1,0,0,0";
                break;
            case RecognizedActivity.SUBWAY:
                choClass = "0,0,0,1,0,0";
                break;
            case RecognizedActivity.CAR:
                choClass = "0,0,0,0,1,0";
                break;
            case RecognizedActivity.STILL:
                choClass = "0,0,0,0,0,1";
                break;
            default:
                break;
        }
        return choClass;
    }

    private String get1AxisData(final SensorFeature sf) {
        return sf.minX + "," +
                sf.maxX + "," +
                sf.meanX + "," +
                sf.stdDevX + "," +
                sf.meanApproximationX + "," +
                sf.stdDevApproximationX + "," +
                sf.magnitudeX + "," +
                sf.freqX + "," +

                sf.minY + "," +
                sf.maxY + "," +
                sf.meanY + "," +
                sf.stdDevY + "," +
                sf.meanApproximationY + "," +
                sf.stdDevApproximationY + "," +
                sf.magnitudeY + "," +
                sf.freqY + "," +

                sf.minZ + "," +
                sf.maxZ + "," +
                sf.meanZ + "," +
                sf.stdDevZ + "," +
                sf.meanApproximationZ + "," +
                sf.stdDevApproximationZ + "," +
                sf.magnitudeZ + "," +
                sf.freqZ;
    }

    private void addFile() {
        String strBuilder = "@relation MultiLabelData\n\n" +
                "@attribute Accelerometer_minX numeric\n" +
                "@attribute Accelerometer_maxX numeric\n" +
                "@attribute Accelerometer_meanX numeric\n" +
                "@attribute Accelerometer_stdDevX numeric\n" +
                "@attribute Accelerometer_meanApproximationX numeric\n" +
                "@attribute Accelerometer_stdDevApproximationX numeric\n" +
                "@attribute Accelerometer_magnitudeX numeric\n" +
                "@attribute Accelerometer_freqX numeric\n" +

                "@attribute Accelerometer_minY numeric\n" +
                "@attribute Accelerometer_maxY numeric\n" +
                "@attribute Accelerometer_meanY numeric\n" +
                "@attribute Accelerometer_stdDevY numeric\n" +
                "@attribute Accelerometer_meanApproximationY numeric\n" +
                "@attribute Accelerometer_stdDevApproximationY numeric\n" +
                "@attribute Accelerometer_magnitudeY numeric\n" +
                "@attribute Accelerometer_freqY numeric\n" +

                "@attribute Accelerometer_minZ numeric\n" +
                "@attribute Accelerometer_maxZ numeric\n" +
                "@attribute Accelerometer_meanZ numeric\n" +
                "@attribute Accelerometer_stdDevZ numeric\n" +
                "@attribute Accelerometer_meanApproximationZ numeric\n" +
                "@attribute Accelerometer_stdDevApproximationZ numeric\n" +
                "@attribute Accelerometer_magnitudeZ numeric\n" +
                "@attribute Accelerometer_freqZ numeric\n" +

                "@attribute WALKING {0,1}\n" +
                "@attribute RUNNING {0,1}\n" +
                "@attribute BUG {0,1}\n" +
                "@attribute SUBWAY {0,1}\n" +
                "@attribute CAR {0,1}\n" +
                "@attribute STILL {0,1}\n" +
                "@data\n";

        Utils.saveAsAppend(context.getApplicationContext(), strBuilder, ARF_FILE_NAME);
    }

    void startTestingWithExistedData(final String classifierType){
        SparseArray<File> files = Utils.getDataFiles("/rHAR/data/test/");
        if (files == null){
            ttcallback.onFinalReport("No testing data found!", 0);
            return;
        }
        String featureFileName = classifierType + "features.csv";
        String finalDescription = null;
        switch (classifierType) {
            case RandomForestClassifier.TYPE:
                generateRFClassifier();
                finalDescription = String.format(Locale.getDefault(), "RDTSettings - attrSize: %d, labelNum: %d, maxS: %d, treeNum: %d, maxTreeDepth: %d",
                        attrSize, labelNum, maxS, treeNum, maxTreeDepth);
                break;
            case XGBoostClassifier.TYPE:
                finalDescription = "XGBoost";
                generateXGBoostClassifier();
                break;
            case LSTMClassifier.TYPE:
                finalDescription = "LSTM";
                generateLSTMClassifier();
                break;
            default:
                ttcallback.onFinalReport("Unsupported classifier type!", 0);
                return;
        }
        Utils.deleteFile(featureFileName);

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < files.size(); ++i) {
            File file = files.valueAt(i);
            int label = Integer.parseInt(file.getName().replace(".csv", ""));
            try {
                Log.i("rHAR", String.format("Testing file: %s.", file.getName()));
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                int count = 0;
                while ((line = br.readLine()) != null) {
                    line = line.replace("\n", "").trim();
                    if(!line.isEmpty()){
                        String[] data = line.split(",");
                        if(data.length > 0){
                            SensorData sd = new SensorData();
                            sd.world_accelerometer[0] = Float.parseFloat(data[0]);
                            sd.world_accelerometer[1] = Float.parseFloat(data[1]);
                            sd.world_accelerometer[2] = Float.parseFloat(data[2]);
                            if (periodVal[count] == null){
                                periodVal[count] = new SensorData();
                            }
                            periodVal[count].clone(sd);
                        }
                    }
                    ++count;
                    if(count == FEATURE_DATA_BATCH_SIZE){
                        double[] probabilities = classifier.recognize(periodVal, SAMPLE_FREQUENCY, FEATURE_DATA_BATCH_SIZE);
                        handleTestingProgress(probabilities, label, featureFileName);
                    }
                    count = count % FEATURE_DATA_BATCH_SIZE;
                }
                br.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        long durationRDT = System.currentTimeMillis() - startTime;
        ttcallback.onFinalReport(finalDescription, durationRDT);
    }

    private void generateRFClassifier() {
        if(!Utils.isFileExists(context, "classificator.json")){
            ttcallback.onFinalReport("No random forest model found!", 0);
            return;
        }
        String trainedTrees = Utils.load(context, "classificator.json");
        classifier = new RandomForestClassifier(trainedTrees);
    }

    private void generateXGBoostClassifier() {
        classifier = new XGBoostClassifier(context);
    }

    private void generateLSTMClassifier() {
        classifier = new LSTMClassifier(context);
    }

    private void handleTestingProgress(double[] probabilities, int label, String featureName){
        RecognizedActivityResult result = new RecognizedActivityResult();
        result.activities = new RecognizedActivity[labels.length];
        for(int index = 0; index < labels.length; ++index){
            result.activities[index] = new RecognizedActivity(labels[index], probabilities[index]);

        }
        double[] features = classifier.getCurrentFeatures();
        if(features != null){
            StringBuilder sb = new StringBuilder();
            for(double f: features){
                sb.append((float)f);
                sb.append(",");
            }
            sb.append(",");
            sb.append(String.valueOf(label));
            sb.append("\n");
            Utils.saveAsAppend(context, sb.toString(), featureName);
        }
        ttcallback.onTestingResult(result, label);
    }
}
