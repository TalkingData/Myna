package com.talkingdata.myna;

import android.util.Log;

import com.talkingdata.myna.sensor.SensorData;
import com.talkingdata.myna.tools.Statistics;

import java.util.Locale;

public class HandHoldingClassifier implements ClassifierInterface {

    public static final String TYPE = "hand_holding";

    /**
     * Recognize current human activity based on pre-defined rules.
     * @param sensorData Raw sensor data points.
     */
    @Override
    public double[] recognize(SensorData[] sensorData, final int sampleFreq, final int sampleCount) {
        float[] accelerometer = new float[sensorData.length];
        float[] pitch = new float[sensorData.length];
        float[] roll = new float[sensorData.length];
        for(int i = 0; i < sensorData.length; ++i){
            accelerometer[i] = (float)Math.sqrt(Math.pow(sensorData[i].accelerate[0], 2)
                    + Math.pow(sensorData[i].accelerate[1], 2)
                    + Math.pow(sensorData[i].accelerate[2], 2));
            pitch[i] = sensorData[i].orientation[1];
            roll[i] = sensorData[i].orientation[2];
        }
        float accelerometer_std = Statistics.getStdDev(accelerometer);
        float pitch_degree = (float)Math.toDegrees(Statistics.getMean(pitch));
        float roll_degree = (float)Math.toDegrees(Statistics.getMean(roll));
        boolean portrait = pitch_degree < -15 && pitch_degree > -60 && roll_degree >= -15 && roll_degree <= 15;
        boolean landscape = (roll_degree < -15 && roll_degree > -60 || roll_degree > 15 && roll_degree < 60) && pitch_degree >= -15 && pitch_degree <= 15;
        double[] doubleResult = new double[2];
        Log.i("Debug", String.format(Locale.getDefault(), "pitch = %.2f, roll = %.2f, p = %s, l = %s, accelerometer = %.2f",
                pitch_degree,
                roll_degree,
                String.valueOf(portrait),
                String.valueOf(landscape),
                accelerometer_std));
        if((portrait || landscape) && accelerometer_std > 0.2){
            doubleResult[0] = 1.0f;
            doubleResult[1] = 0.0f;
        }else{
            doubleResult[0] = 0.0f;
            doubleResult[1] = 1.0f;
        }
        return doubleResult;
    }

    @Override
    public double[] getCurrentFeatures(){
        return null;
    }
}
