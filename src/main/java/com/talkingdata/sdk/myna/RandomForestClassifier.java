package com.talkingdata.sdk.myna;

import android.content.Context;

import com.talkingdata.sdk.myna.sensor.Feature;
import com.talkingdata.sdk.myna.sensor.SensorData;
import com.talkingdata.sdk.myna.sensor.SensorFeature;
import com.talkingdata.sdk.myna.tools.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;

import dice.data.Instance;
import dice.data.Instances;
import dice.data.SimpleInstances;
import dice.tree.model.CBRRDTModel;
import dice.tree.structure.Node;

public class RandomForestClassifier implements ClassifierInterface {
    private Context ctx;

    private int attrSize = 42;
    private int labelNum = 3;
    private int maxS = 5;
    private int treeNum = 5;
    private Node[] trees = new Node[treeNum];
    private CBRRDTModel model;
    private int[] attributes = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                                      -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                                      -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                                      -1, -1, -1, -1, -1, -1, -1, -1, -1,  2, 2, 2};

    public RandomForestClassifier(Context context) {
        ctx = context;
    }

    /**
     * Extract and select features from the raw sensor data points.
     * These data points are collected with certain sampling frequency and windows.
     * @param sensorData Raw sensor data points.
     * @return Extracted features.
     */
    private double[][] prepareFeatures(SensorData[] sensorData) {
        String trainedTrees = Utils.loadFeaturesFromAssets(ctx, "classificator.json");
        parseJsonObj(trainedTrees);
        model = new CBRRDTModel();
        model.init(trees, attributes, maxS);

        double[][] matrix = new double[1][attrSize];
        matrix[0] = new double[attrSize];
        Feature aFeature = new Feature();
        aFeature.extractFeatures(sensorData);
        int pos = 0;
        pos = get1AxisData(matrix, aFeature.getSelectedFeatures(), pos);
        if(pos == attrSize - labelNum)
        {
            addLabelData(matrix, pos);
            return matrix;
        }else{
            return null;
        }
    }

    /**
     * Recognize current human activity based on pre-defined rules.
     * @param sensorData Raw sensor data points.
     */
    @Override
    public double[] recognize(SensorData[] sensorData) {
        double[][] features = prepareFeatures(sensorData);
        Instances testInstances =  new SimpleInstances(attributes, features, null, "rhar");
        return predict(testInstances);
    }

    private void parseJsonObj(final String jsonStr) {
        if (jsonStr == null || jsonStr.trim().isEmpty())
            return;
        try {
            JSONObject jsonObj = new JSONObject(jsonStr);
            this.attrSize = jsonObj.getInt("attrSize");
            this.labelNum = jsonObj.getInt("labelNum");
            this.maxS = jsonObj.getInt("maxS");
            this.treeNum = jsonObj.getInt("treeNum");
            JSONArray treesArray = jsonObj.getJSONArray("trees");
            parseTreesJsonArray(treesArray);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void parseTreesJsonArray(final JSONArray ja) {
        if (ja == null)
            return;
        JSONObject jo;
        for (int i = 0; i < ja.length(); i++) {
            try {
                jo = ja.getJSONObject(i);
                trees[i] = (Node) Utils.fromString(jo.getString("tree"));
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    private double[] predict(Instances testInstances) {
        Iterator<Instance> it = testInstances.iterator();
        Instance inst = it.next();
        CBRRDTModel.Prediction pred = model.estimate(inst);
        double confidences[] = new double[labelNum];

        for (int i = 0; i < labelNum; i++) {
            Double t = pred.dist.get(i);
            if (t == null) {
                confidences[i] = 0F;
            } else {
                confidences[i] = t;
            }
        }
        return confidences;
    }

    private int get1AxisData(double[][] matrix, SensorFeature sf, int currentPos){
        int index = currentPos;
        matrix[0][index++] = sf.minx;
        matrix[0][index++] = sf.miny;
        matrix[0][index++] = sf.minz;
        matrix[0][index++] = sf.maxx;
        matrix[0][index++] = sf.maxy;
        matrix[0][index++] = sf.maxz;
        matrix[0][index++] = sf.avgx;
        matrix[0][index++] = sf.avgy;
        matrix[0][index++] = sf.avgz;
        matrix[0][index++] = sf.varx;
        matrix[0][index++] = sf.vary;
        matrix[0][index++] = sf.varz;
        matrix[0][index++] = sf.rangex;
        matrix[0][index++] = sf.rangey;
        matrix[0][index++] = sf.rangez;
        matrix[0][index++] = sf.jumpx;
        matrix[0][index++] = sf.jumpy;
        matrix[0][index++] = sf.jumpz;
        matrix[0][index++] = sf.fallx;
        matrix[0][index++] = sf.fally;
        matrix[0][index++] = sf.fallz;
        matrix[0][index++] = sf.amp1x;
        matrix[0][index++] = sf.amp1y;
        matrix[0][index++] = sf.amp1z;
        matrix[0][index++] = sf.amp2x;
        matrix[0][index++] = sf.amp2y;
        matrix[0][index++] = sf.amp2z;
        matrix[0][index++] = sf.amp3x;
        matrix[0][index++] = sf.amp3y;
        matrix[0][index++] = sf.amp3z;
        matrix[0][index++] = sf.freq1x;
        matrix[0][index++] = sf.freq1y;
        matrix[0][index++] = sf.freq1z;
        matrix[0][index++] = sf.freq2x;
        matrix[0][index++] = sf.freq2y;
        matrix[0][index++] = sf.freq2z;
        matrix[0][index++] = sf.freq3x;
        matrix[0][index++] = sf.freq3y;
        matrix[0][index++] = sf.freq3z;
        return index;
    }

    private void addLabelData(double[][] matrix, int currentPos){
        for(int index = currentPos; index < attrSize; ++index){
            matrix[0][index] = 0;
        }
    }
}
