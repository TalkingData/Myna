package com.talkingdata.myna;

import android.hardware.Sensor;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.SparseIntArray;

import com.talkingdata.myna.sensor.SensorData;

abstract class MynaRecognizerAbstractClass {

    abstract void onResult(double[] confidences);


    /**
     * Sensors whose data will be collected.
     * key = sensorType, value = sensorString
     */
    private SparseIntArray chosenSensors;

    /**
     * Duration between each sampling, indicates sampling frequency.
     */
    private int samplingInterval;

    /**
     * Total count of sampling points you expect to collected,
     * indicates sampling window.
     */
    private int samplingPointCount;

    /**
     * Algorithm which is used to recognize current activity.
     */
    ClassifierInterface classifier;

    /**
     * Callback to return the results
     */
    MynaResultCallback resultCallback;

    int dataSetIndex;

    Handler dataFusionHandler;

    Handler recognitionHandler;

    SensorData[] dataSet;

    /**
     * Recognition algorithm must be provided.
     * @param classifier Recognition algorithm
     * @param resultCallback Callback to handle the recognition result
     */
    MynaRecognizerAbstractClass(ClassifierInterface classifier, MynaResultCallback resultCallback){
        chosenSensors = new SparseIntArray();
        samplingInterval = 50;
        samplingPointCount = 90;
        dataSetIndex = 0;
        this.classifier = classifier;
        this.resultCallback = resultCallback;

        HandlerThread dataFusionHandlerThread = new HandlerThread("dataFusionHandlerThread");
        dataFusionHandlerThread.start();
        dataFusionHandler = new Handler(dataFusionHandlerThread.getLooper());
        HandlerThread recognitionHandlerThread = new HandlerThread("recognitionHandlerThread");
        recognitionHandlerThread.start();
        recognitionHandler = new Handler(recognitionHandlerThread.getLooper());

        dataSet = new SensorData[samplingPointCount];

        setDefaultSensors();
    }

    void resetRecognizer(){
        dataSetIndex = 0;
        dataSet = new SensorData[samplingPointCount];
        dataFusionHandler.removeCallbacksAndMessages(null);
        recognitionHandler.removeCallbacksAndMessages(null);
    }

    /**
     * Add a sensor into the chosen sensor list.
     * @param sensorType sensorType
     */
    public synchronized void addSensorType(int sensorType){
        chosenSensors.put(sensorType, sensorType);
    }

    /**
     * Remove a sensor from the chosen sensor list.
     * @param sensorType The type of the sensor to be removed.
     */
    public synchronized void removeSensorType(int sensorType){
        chosenSensors.delete(sensorType);
    }

    /**
     * Get the chosen sensor list.
     * @return The chosen sensor list, all sensor in this list will be used for data fusion.
     */
    public synchronized SparseIntArray getAllSensors(){
        return chosenSensors;
    }

    /**
     * Set sampling duration.
     * @param duration Sampling duration
     */
    public void setSamplingInterval(int duration){
        samplingInterval = duration;
    }

    /**
     * Get sampling duration
     * @return Sampling duration
     */
    public int getSamplingInterval(){
        return samplingInterval;
    }

    /**
     *```` Set total count of the data points for each recognition.
     * @param pointCount Total count of the data points.
     */
    public void setSamplingPointCount(int pointCount){
        samplingPointCount = pointCount;
        dataSet = new SensorData[samplingPointCount];
    }

    /**
     * Get total count of the data points for each recognition.
     * @return Total count of the data points.
     */
    public int getSamplingPointCount(){
        return samplingPointCount;
    }

    private void setDefaultSensors(){
        addSensorType(Sensor.TYPE_ACCELEROMETER);
        addSensorType(Sensor.TYPE_MAGNETIC_FIELD);
    }
}
