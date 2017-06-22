
package com.talkingdata.myna.sensor;


import com.talkingdata.myna.tools.Statistics;

public class Feature {
    private int batchSize;
    private SensorFeature feature;

    public Feature() {
        feature = new SensorFeature();
        batchSize = 0;
    }

    /**
     * Get selected feature
     * @return selected feature
     */
    public SensorFeature getSelectedFeatures(){
        return feature;
    }

    /**
     * Extract feature from raw sensor data
     */
    public void extractFeatures(SensorData[] sensorData, final int sampleFreq, final int sampleCount) {
        batchSize = sensorData.length;
        float[][] input = new float[3][batchSize];
        for (int i = 0; i < batchSize; i++){
            input[0][i] = sensorData[i].world_accelerometer[0];
            input[1][i] = sensorData[i].world_accelerometer[1];
            input[2][i] = sensorData[i].world_accelerometer[2];
        }
//        Statistics.normalize(input[0]);
//        Statistics.normalize(input[1]);
//        Statistics.normalize(input[2]);
        getFeaturesInternally(input, sampleFreq, sampleCount);
    }

    /**
     * Internal implementation of extracting feature from raw sensor data
     */
    private void getFeaturesInternally(float[][] dataset, final int sampleFreq, final int sampleCount) {
        feature.maxX = Statistics.getMax(dataset[0]);
        feature.maxY = Statistics.getMax(dataset[1]);
        feature.maxZ = Statistics.getMax(dataset[2]);

        feature.minX = Statistics.getMin(dataset[0]);
        feature.minY = Statistics.getMin(dataset[1]);
        feature.minZ = Statistics.getMin(dataset[2]);

        feature.meanX = Statistics.getMean(dataset[0]);
        feature.meanY = Statistics.getMean(dataset[1]);
        feature.meanZ = Statistics.getMean(dataset[2]);

        feature.stdDevX = Statistics.getStdDev(dataset[0]);
        feature.stdDevY = Statistics.getStdDev(dataset[1]);
        feature.stdDevZ = Statistics.getStdDev(dataset[2]);

        calculateWaveletApproximation(dataset, 0);
        calculateWaveletApproximation(dataset, 1);
        calculateWaveletApproximation(dataset, 2);

        calculateFreqAndMagnitude(dataset, sampleFreq, sampleCount, 0);
        calculateFreqAndMagnitude(dataset, sampleFreq, sampleCount, 1);
        calculateFreqAndMagnitude(dataset, sampleFreq, sampleCount, 2);
    }

    private void calculateFreqAndMagnitude(float[][] dataset, final int sampleFreq, final int sampleCount, int axis){
        float[] freqAndMag = Statistics.getFreqAndMagnitudeViaFFT(dataset[axis], sampleFreq, sampleCount);
        if(axis == 0){
            feature.freqX = freqAndMag[0];
            feature.magnitudeX = freqAndMag[1];
        }else if(axis == 1){
            feature.freqY = freqAndMag[0];
            feature.magnitudeY = freqAndMag[1];
        }else if(axis == 2){
            feature.freqZ = freqAndMag[0];
            feature.magnitudeZ = freqAndMag[1];
        }
    }

    private void calculateWaveletApproximation(float[][] dataset, int axis){
        float[] approximation = Statistics.getWaveletApproximation(dataset[axis]);
        if(axis == 0){
            feature.meanApproximationX = Statistics.getMean(approximation);
            feature.stdDevApproximationX = Statistics.getStdDev(approximation);
        }else if(axis == 1){
            feature.meanApproximationY = Statistics.getMean(approximation);
            feature.stdDevApproximationY = Statistics.getStdDev(approximation);
        }else if(axis == 2){
            feature.meanApproximationZ = Statistics.getMean(approximation);
            feature.stdDevApproximationZ = Statistics.getStdDev(approximation);
        }
    }

    public double[] getFeaturesAsArray(){
        double[] array = new double[SensorFeature.FEATURE_COUNT];
        int index = 0;
        array[index++] = feature.minX;
        array[index++] = feature.maxX;
        array[index++] = feature.meanX;
        array[index++] = feature.stdDevX;
        array[index++] = feature.meanApproximationX;
        array[index++] = feature.stdDevApproximationX;
        array[index++] = feature.magnitudeX;
        array[index++] = feature.freqX;

        array[index++] = feature.minY;
        array[index++] = feature.maxY;
        array[index++] = feature.meanY;
        array[index++] = feature.stdDevY;
        array[index++] = feature.meanApproximationY;
        array[index++] = feature.stdDevApproximationY;
        array[index++] = feature.magnitudeY;
        array[index++] = feature.freqY;

        array[index++] = feature.minZ;
        array[index++] = feature.maxZ;
        array[index++] = feature.meanZ;
        array[index++] = feature.stdDevZ;
        array[index++] = feature.meanApproximationZ;
        array[index++] = feature.stdDevApproximationZ;
        array[index++] = feature.magnitudeZ;
        array[index] = feature.freqZ;
        return array;
    }
}
