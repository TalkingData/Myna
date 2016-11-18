package com.talkingdata.sdk.myna;

import com.talkingdata.sdk.myna.sensor.SensorData;

interface ClassifierInterface {

    double[] recognize(SensorData[] sensorData);
}
