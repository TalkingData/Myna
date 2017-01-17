package com.talkingdata.myna;

import com.talkingdata.myna.sensor.SensorData;

interface ClassifierInterface {

    double[] recognize(SensorData[] sensorData);
}
