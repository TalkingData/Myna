package com.talkingdata.myna;

import android.content.Context;
import android.content.res.AssetManager;
import com.talkingdata.myna.sensor.SensorData;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

public class LSTMClassifier implements ClassifierInterface {
    static {
        System.loadLibrary("tensorflow_inference");
    }

    private TensorFlowInferenceInterface inferenceInterface;
    private static final String MODEL_FILE = "file:///android_asset/optimized_frozen_lstm.pb";
    private static final String INPUT_NODE = "input";
    private static final String[] OUTPUT_NODES = {"output"};
    private static final String OUTPUT_NODE = "output";
    private static final int INPUT_SIZE = 3 * 128;
    private static final int OUTPUT_SIZE = 5;

    public LSTMClassifier(final Context ctx) {
        AssetManager assetManager = ctx.getAssets();
        inferenceInterface = new TensorFlowInferenceInterface(assetManager, MODEL_FILE);
    }

    public static final String TYPE = "lstm";

    /**
     * Extract and select features from the raw sensor data points.
     * These data points are collected with certain sampling frequency and windows.
     * @param sData Raw sensor data points.
     * @return Extracted features.
     */
    private float[] prepareFeatures(SensorData[] sData, final int sampleCount) {
        float[] input = new float[INPUT_SIZE];
        if(sData.length == sampleCount){
            for(int i = 0; i < sData.length; ++i){
                input[i] = sData[i].world_accelerometer[0];
                input[sampleCount + i] = sData[i].world_accelerometer[1];
                input[2 * sampleCount + i] = sData[i].world_accelerometer[2];
            }
        }
        return input;
    }

    /**
     * Recognize current human activity based on pre-defined rules.
     * @param sensorData Raw sensor data points.
     */
    @Override
    public double[] recognize(SensorData[] sensorData, final int sampleFreq, final int sampleCount) {
        float[] input = prepareFeatures(sensorData, sampleCount);
        double[] result = new double[OUTPUT_SIZE];

        inferenceInterface.feed(INPUT_NODE,input,INPUT_SIZE);
        inferenceInterface.run(OUTPUT_NODES);
        inferenceInterface.fetch(OUTPUT_NODE,result);
        return result;
    }

    @Override
    public double[] getCurrentFeatures(){
        return null;
    }
}
