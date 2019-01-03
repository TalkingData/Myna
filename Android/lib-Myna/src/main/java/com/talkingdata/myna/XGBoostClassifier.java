package com.talkingdata.myna;

import android.content.Context;

import com.talkingdata.myna.sensor.Feature;
import com.talkingdata.myna.sensor.SensorData;
import com.talkingdata.myna.sensor.SensorFeature;
import com.talkingdata.myna.tools.Statistics;

import java.io.InputStream;

import biz.k11i.xgboost.Predictor;
import biz.k11i.xgboost.util.FVec;

public class XGBoostClassifier implements ClassifierInterface {

    private Predictor onFootPredictor;
    private Predictor inVehiclePredictor;
    private double[] features;
    private final int ON_FOOT = 1;
    private final int IN_VEHICLE = 2;
    private int currentPredictorIndex;

    public static final String TYPE = "xgboost";

    public XGBoostClassifier(Context ctx) {
        try {
            InputStream is = ctx.getAssets().open("on_foot.model");
            onFootPredictor = new Predictor(is);
            is = ctx.getAssets().open("in_vehicle.model");
            inVehiclePredictor = new Predictor(is);
            is.close();

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * Extract and select features from the raw sensor data points.
     * These data points are collected with certain sampling frequency and windows.
     * @param sensorData Raw sensor data points.
     * @return Extracted features.
     */
    private double[] prepareFeatures(SensorData[] sensorData, final int sampleFreq, final int sampleCount) {
        currentPredictorIndex = getPredictorIndex(sensorData);
        double[] matrix = new double[SensorFeature.FEATURE_COUNT];
        Feature aFeature = new Feature();
        aFeature.extractFeatures(sensorData, sampleFreq, sampleCount);
        System.arraycopy(aFeature.getFeaturesAsArray(), 0, matrix, 0, SensorFeature.FEATURE_COUNT);
        return matrix;
    }

    private int getPredictorIndex(SensorData[] sensorData){
        int batchSize = sensorData.length;
        float[] input = new float[batchSize];
        for (int i = 0; i < batchSize; i++){
            input[i] = (float)Math.sqrt(
                    sensorData[i].accelerate[0] * sensorData[i].accelerate[0]
                    + sensorData[i].accelerate[1] * sensorData[i].accelerate[1]
                    + sensorData[i].accelerate[2] * sensorData[i].accelerate[2]
            );
        }
        float mean = Statistics.getMean(input);
        float std = Statistics.getStdDev(input);
        if(mean > 10 || std < 0.5){
            return ON_FOOT;
        }else{
            return IN_VEHICLE;
        }
    }

    /**
     * Recognize current human activity based on pre-defined rules.
     * @param sensorData Raw sensor data points.
     */
    @Override
    public double[] recognize(SensorData[] sensorData, final int sampleFreq, final int sampleCount) {
        features = prepareFeatures(sensorData, sampleFreq, sampleCount);
        double[] result =  predict();
        double[] finalResult = new double[6];
        if(currentPredictorIndex == ON_FOOT){
            System.arraycopy(result, 0, finalResult, 0, result.length);
            finalResult[3] = 0.0;
            finalResult[4] = 0.0;
            finalResult[5] = 0.0;
        }else{
            finalResult[0] = 0.0;
            finalResult[1] = 0.0;
            finalResult[2] = 0.0;
            System.arraycopy(result, 0, finalResult, 0, result.length);
        }
        return finalResult;
    }

    @Override
    public double[] getCurrentFeatures(){
        return features;
    }

    private double[] predict() {
        FVec vector = FVec.Transformer.fromArray(features, true);
        if(currentPredictorIndex == ON_FOOT){
            return onFootPredictor.predict(vector);
        }else{
            return inVehiclePredictor.predict(vector);
        }
    }
}
