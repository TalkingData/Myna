package com.talkingdata.myna.tools;

class Statistics {
    private double[] data;
    private int size;

    Statistics(double[] data) {
        this.data = data;
        size = data.length;
    }

    double getMean() {
        float sum = 0.0f;
        for (double a : data)
            sum += a;
        return sum / size;
    }

    double getStdDev() {
        return Math.sqrt(getVariance());
    }

    double getVariance() {
        double mean = getMean();
        double temp = 0;
        for (double a : data)
            temp += (a - mean) * (a - mean);
        return temp / (size - 1);
    }
}
