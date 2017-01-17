
package com.talkingdata.myna.sensor;


import com.talkingdata.myna.tools.Complex;
import com.talkingdata.myna.tools.FFT;

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
    public void extractFeatures(SensorData[] sensorData) {
        batchSize = sensorData.length;
        float[][] input = new float[batchSize][3];
        getFeaturesInternally(input);
        for (int i = 0; i < batchSize; i++)
            System.arraycopy(sensorData[i].accelerate, 0, input[i], 0, 3);
        getFeaturesInternally(input);
    }

    /**
     * Internal implementation of extracting feature from raw sensor data
     */
    private void getFeaturesInternally(float[][] DataSet) {
        calculateMinAndMax(DataSet);
        calculateAverage(DataSet);
        calculateVariance(DataSet);
        calculateJumpFall(DataSet);
        calculateAmpFreq(DataSet);
    }

    private void calculateMinAndMax(float[][] DataSet) {
        float minx = 100000, miny = 100000, minz = 100000;
        float maxx = -100000, maxy = -100000, maxz = -100000;
        for (int i = 0; i < batchSize; i++) {
            maxx = Math.max(maxx, DataSet[i][0]);
            maxy = Math.max(maxy, DataSet[i][1]);
            maxz = Math.max(maxz, DataSet[i][2]);
            minx = Math.min(minx, DataSet[i][0]);
            miny = Math.min(miny, DataSet[i][1]);
            minz = Math.min(minz, DataSet[i][2]);
        }
        feature.maxx = maxx;
        feature.maxy = maxy;
        feature.maxz = maxz;
        feature.minx = minx;
        feature.miny = miny;
        feature.minz = minz;
        feature.rangex = maxx - minx;
        feature.rangey = maxy - miny;
        feature.rangez = maxz - minz;
    }

    private void calculateAverage(float[][] DataSet) {
        float avgx = 0, avgy = 0, avgz = 0;
        for (int i = 0; i < batchSize; i++) {
            avgx += DataSet[i][0];
            avgy += DataSet[i][1];
            avgz += DataSet[i][2];
        }
        avgx /= batchSize;
        avgy /= batchSize;
        avgz /= batchSize;
        feature.avgx = avgx;
        feature.avgy = avgy;
        feature.avgz = avgz;
    }

    private void calculateVariance(float[][] DataSet) {
        float varx = 0, vary = 0, varz = 0;
        float avgx = feature.avgx;
        float avgy = feature.avgy;
        float avgz = feature.avgz;
        for (int i = 0; i < batchSize; i++) {
            varx += (avgx - DataSet[i][0]) * (avgx - DataSet[i][0]);
            vary += (avgy - DataSet[i][1]) * (avgy - DataSet[i][1]);
            varz += (avgz - DataSet[i][2]) * (avgz - DataSet[i][2]);
        }
        varx /= batchSize - 1;
        vary /= batchSize - 1;
        varz /= batchSize - 1;
        feature.varx = varx;
        feature.vary = vary;
        feature.varz = varz;
    }

    private void calculateJumpFall(float[][] DataSet) {
    	for (int i = 0; i < 3; ++i) {
    		float jump = 0, fall = 0;
    		float jumpt = 0, fallt = 0;
    		for (int j = 0; j < batchSize -1; ++j) {
    			if (DataSet[j][i] < DataSet[j+1][i]) {
    				jumpt += DataSet[j+1][i] - DataSet[j][i];
    				if (fallt > fall) {
    					fall = fallt;
    				}
    				fallt = 0;
    			}
    			if (DataSet[j][i] > DataSet[j+1][i]) {
    				fallt += DataSet[j][i] - DataSet[j+1][i];
    				if (jumpt > jump) {
    					jump = jumpt;
    				}
    				jumpt = 0;
    			}
    		}
    		if (jumpt > jump) {
    			jump = jumpt;
    		}
    		if (fallt > fall) {
    			fall = fallt;
    		}
    		if (i == 0) {
    			feature.jumpx = jump;
    			feature.fallx = fall;
    		}
    		if (i == 1) {
    			feature.jumpy = jump;
    			feature.fally = fall;
    		}
    		if (i == 2) {
    			feature.jumpz = jump;
    			feature.fallz = fall;
    		}
    	}
    }

    private void calculateAmpFreq(float[][] DataSet) {
        for (int i = 0; i < 3; ++i) {
            Complex[] x = new Complex[batchSize];
            for (int j = 0; j < batchSize; ++j) {
                x[j] = new Complex(DataSet[j][i], 0);
            }
            
            Complex[] y = FFT.fft(x);
            
            float[] max3 = new float[3];
            int[] max3_int = new int[3];
            for (int j = 0; j < batchSize / 2; ++j) {
                double amp = y[j].abs();
                int freq = j;
                for (int k = 0; k < 3; ++k) {
                    if (amp > max3[k]) {
                        double tmpd = amp;
                        int tmpi = freq;
                        amp = max3[k];
                        freq = max3_int[k];
                        max3[k] = (float) tmpd;
                        max3_int[k] = tmpi;
                    }
                }
            }
            if (max3_int[0] < max3_int[1]) {
                int tmp = max3_int[0];
                max3_int[0] = max3_int[1];
                max3_int[1] = tmp;
            }
            if (max3_int[1] < max3_int[2]) {
                int tmp = max3_int[1];
                max3_int[1] = max3_int[2];
                max3_int[2] = tmp;
            }
            if (max3_int[0] < max3_int[1]) {
                int tmp = max3_int[0];
                max3_int[0] = max3_int[1];
                max3_int[1] = tmp;
            }
            if (i == 0) {
                feature.amp1x = max3[0];
                feature.amp2x = max3[1];
                feature.amp3x = max3[2];
                feature.freq1x = max3_int[0];
                feature.freq2x = max3_int[1];
                feature.freq3x = max3_int[2];
            }
            if (i == 1) {
                feature.amp1y = max3[0];
                feature.amp2y = max3[1];
                feature.amp3y = max3[2];
                feature.freq1y = max3_int[0];
                feature.freq2y = max3_int[1];
                feature.freq3y = max3_int[2];
            }
            if (i == 2) {
                feature.amp1z = max3[0];
                feature.amp2z = max3[1];
                feature.amp3z = max3[2];
                feature.freq1z = max3_int[0];
                feature.freq2z = max3_int[1];
                feature.freq3z = max3_int[2];
            }
        }
    }
}
