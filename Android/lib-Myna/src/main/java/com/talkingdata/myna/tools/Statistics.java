package com.talkingdata.myna.tools;

import java.util.Arrays;

public class Statistics {

    public static float getMean(float[] data) {
        float sum = 0.0f;
        for (float f : data)
            sum += f;
        return sum / data.length;
    }

    public static float getStdDev(float[] data) {
        return (float)Math.sqrt(getVariance(data));
    }

    public static float getMin(float[] data){
        float min = 10000;
        for (float f : data) {
            min = Math.min(min, f);
        }
        return min;
    }

    public static float getMax(float[] data){
        float max = -10000;
        for (float f : data) {
            max = Math.max(max, f);
        }
        return max;
    }

    public static float[] getFreqAndMagnitudeViaFFT(float[] data, int sampleFreq, int sampleCount){
        int batchSize = data.length;
        float[] freqAndMag = new float[2];
        Complex[] x = new Complex[batchSize];
        for (int j = 0; j < batchSize; ++j) {
            x[j] = new Complex(data[j], 0);
        }
        Complex[] y = FFT.fft(x);
        double max_magnitude = y[1].abs();
        int max_index = 1;
        for (int j = 2; j < batchSize / 2; ++j) {
            double curr_magnitude = y[j].abs();
            if(curr_magnitude > max_magnitude){
                max_magnitude = curr_magnitude;
                max_index = j;
            }
        }
        freqAndMag[0] = sampleFreq * 1.0f / sampleCount * max_index;
        freqAndMag[1] = (float)max_magnitude / 100.0f;
        return freqAndMag;
    }

    public static float[] getWaveletApproximation(float[] data){
        double[] raw = new double[data.length];
        for(int i = 0; i < data.length; ++i){
            raw[i] = data[i];
        }
        Haar1 h = new Haar1();
        double[] coefficients = h.forward(raw, raw.length);
        float[] approximation = new float[coefficients.length / 2];
        for(int i = 0; i < coefficients.length / 2; ++i){
            approximation[i] = (float)coefficients[i];
        }
        return approximation;
    }

    public static void normalize(float[] data){
        float mean = getMean(data);
        float stdDev = getStdDev(data);
        for(int i = 0; i < data.length; ++i){
            data[i] = (data[i] - mean) / stdDev;
        }
    }


    private static float getVariance(float[] data){
        return getVariance(data, 0);
    }

    private static float getVariance(float[] data, int ddof) {
        float mean = getMean(data);
        float temp = 0;
        for (float a : data)
            temp += (a - mean) * (a - mean);

        if(ddof >= data.length){
            return temp / data.length;
        }else{
            return temp / (data.length - ddof);
        }
    }

    public static float getMedian(float[] data){
        Arrays.sort(data);
        float median;
        if (data.length % 2 == 0)
            median = (data[data.length/2] + data[data.length/2 - 1])/2;
        else
            median = data[data.length/2];
        return median;

    }
}
