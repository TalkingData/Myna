
package com.talkingdata.myna.sensor;

public class SensorFeature {
    public static final int FEATURE_COUNT = 24;
    public float minX = 10000, minY = 10000, minZ = 10000;
    public float maxX = -10000, maxY = -10000, maxZ = -10000;
    public float meanX = 0, meanY = 0, meanZ = 0;
    public float stdDevX = 0, stdDevY = 0, stdDevZ = 0;
    public float meanApproximationX = 0, meanApproximationY = 0, meanApproximationZ = 0;
    public float stdDevApproximationX = 0, stdDevApproximationY = 0, stdDevApproximationZ = 0;
    public float magnitudeX = 0, magnitudeY = 0, magnitudeZ = 0;
    public float freqX = 0, freqY = 0, freqZ = 0;

}
