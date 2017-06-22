package com.talkingdata.myna;

import com.talkingdata.myna.sensor.SensorData;

interface ClassifierInterface {

    double[] recognize(SensorData[] sensorData, final int sampleFreq, final int sampleCount);
    double[] getCurrentFeatures();
}
