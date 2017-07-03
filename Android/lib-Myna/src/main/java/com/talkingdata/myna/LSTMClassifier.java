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
    private static final int OUTPUT_SIZE = 6;

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
        for(int i = 0; i < sampleCount; ++i){
            input[3 * i] = sData[i].world_accelerometer[0];
            input[3 * i + 1] = sData[i].world_accelerometer[1];
            input[3 * i + 2] = sData[i].world_accelerometer[2];
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
        float[] result = new float[OUTPUT_SIZE];

        inferenceInterface.feed(INPUT_NODE,input,INPUT_SIZE);
        inferenceInterface.run(OUTPUT_NODES);
        inferenceInterface.fetch(OUTPUT_NODE,result);
        double[] doubleResult = new double[OUTPUT_SIZE];
        for(int i = 0; i < OUTPUT_SIZE; ++i){
            doubleResult[i] = (double)result[i];
        }
        return doubleResult;
    }

    @Override
    public double[] getCurrentFeatures(){
        return null;
    }
}
