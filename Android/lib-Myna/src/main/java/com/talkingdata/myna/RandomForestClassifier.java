package com.talkingdata.myna;

import com.talkingdata.myna.sensor.Feature;
import com.talkingdata.myna.sensor.SensorData;
import com.talkingdata.myna.sensor.SensorFeature;
import com.talkingdata.myna.tools.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;

import dice.data.Instance;
import dice.data.Instances;
import dice.data.SimpleInstances;
import dice.tree.model.CBRRDTModel;
import dice.tree.structure.Node;

public class RandomForestClassifier implements ClassifierInterface {

    private int attrSize = 0;
    private int labelNum = 0;
    private int maxS = 0;
    private Node[] trees = null;
    private CBRRDTModel model;
    private int[] attributes = null;
    private double[] features;

    public static final String TYPE = "randomForest";

    public RandomForestClassifier(String trainedTrees) {
        parseJsonObj(trainedTrees);
        model = new CBRRDTModel();
        model.init(trees, attributes, maxS);
    }

    /**
     * Extract and select features from the raw sensor data points.
     * These data points are collected with certain sampling frequency and windows.
     * @param sensorData Raw sensor data points.
     * @return Extracted features.
     */
    private double[][] prepareFeatures(SensorData[] sensorData, final int sampleFreq, final int sampleCount) {

        double[][] matrix = new double[1][attrSize];
        matrix[0] = new double[attrSize];
        Feature aFeature = new Feature();
        aFeature.extractFeatures(sensorData, sampleFreq, sampleCount);
        System.arraycopy(aFeature.getFeaturesAsArray(), 0, matrix[0], 0, SensorFeature.FEATURE_COUNT);
        int pos = SensorFeature.FEATURE_COUNT;
        if(pos == attrSize - labelNum)
        {
            addLabelData(matrix, pos);
            return matrix;
        }else{
            return null;
        }
    }

    @Override
    public double[] getCurrentFeatures(){
        return features;
    }

    /**
     * Recognize current human activity based on pre-defined rules.
     * @param sensorData Raw sensor data points.
     */
    @Override
    public double[] recognize(SensorData[] sensorData, final int sampleFreq, final int sampleCount) {
        double[][] matrix = prepareFeatures(sensorData, sampleFreq, sampleCount);
        if(matrix != null && matrix.length >= 1){
            features = matrix[0];
        }
        Instances testInstances =  new SimpleInstances(attributes, matrix, null, "humanActivityRecognition");
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
            int treeNum = jsonObj.getInt("treeNum");
            this.trees = new Node[treeNum];
            JSONArray treesArray = jsonObj.getJSONArray("trees");
            parseTreesJsonArray(treesArray);
            String attrStr = jsonObj.getString("attributes");
            parseAttributes(attrStr);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void parseAttributes(final String attributes){
        if(attributes == null || attributes.isEmpty()){
            return;
        }
        String[] attrs = attributes.split(",");
        int attrLen = attrs.length;
        this.attributes = new int[attrLen];
        for(int index = 0; index < attrLen; ++index){
            this.attributes[index] = Integer.parseInt(attrs[index].trim());
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

    private void addLabelData(double[][] matrix, int currentPos){
        for(int index = currentPos; index < attrSize; ++index){
            matrix[0][index] = 0;
        }
    }
}
