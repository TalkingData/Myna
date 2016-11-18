package com.talkingdata.sdk.myna;

import java.util.List;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.util.SparseArray;
import android.util.SparseIntArray;

import com.talkingdata.sdk.myna.sensor.SensorData;
import com.talkingdata.sdk.myna.tools.Utils;

public class MynaService extends Service implements SensorEventListener {

    private SensorManager mSensorManager = null;

    /**
     * Current sampling result for all sensor types.
     */
    private SensorData latestSampledData;

    private MyBinder mBinder = new MyBinder();

    /**
     * Handler to execute the recognition task.
     */
    private Handler getDetectedActivityHandler = null;

    /**
     * Whether or not now is in recognition process?
     */
    private boolean isRecognizing = false;

    /**
     * Start activity recognition
     */
    private final int START_RECOGNIZING = 1;

    /**
     * Stop activity recognition
     */
    private final int STOP_RECOGNIZING = 2;

    /**
     * Added Recognizer
     */
    private SparseArray<MynaRecognizerInterface> addedConfigs;

    @Override
    public void onCreate() {
        super.onCreate();
        initHandler();
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        latestSampledData = new SensorData();
        addedConfigs = new SparseArray<>();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    class MyBinder extends Binder {

        /**
         * Start recognizing for all added configs
         */
        void startRecognizing() {
            if(!isRecognizing){
                registerListener();
                executeTask(START_RECOGNIZING);
                isRecognizing = true;
            }
        }

        /**
         * Stop recognizing for all added configs
         */
        void stopRecognizing() {
            if(isRecognizing){
                executeTask(STOP_RECOGNIZING);
            }
        }

        /**
         * Add a new recognizer config
         */
        int addRecognizer(MynaRecognizerInterface config){
            int configId = addedConfigs.size();
            addedConfigs.append(addedConfigs.size(), config);
            return configId;
        }

        /**
         * Stop recognizing for a specific added config by id
         */
        boolean removeRecognizer(int configId){
            try{
                MynaRecognizerInterface config = addedConfigs.valueAt(configId);
                config.dataFusionHandler.removeCallbacksAndMessages(null);
                addedConfigs.delete(configId);
                if(addedConfigs.size() == 0){
                    unregisterListener();
                    isRecognizing = false;
                }
                return true;
            }catch(Throwable t){
                t.printStackTrace();
            }
            return false;
        }

        /**
         * Cleanup the added configs
         */
        void cleanUp(){
            latestSampledData = new SensorData();
            addedConfigs = new SparseArray<>();
        }
    }

    private void initHandler() {
        HandlerThread getDetectedActivityThread = new HandlerThread(
                "getDetectedActivityWorkThread");
        getDetectedActivityThread.start();

        /**
         * Handler to handle events getDetectedActivity
         */
        getDetectedActivityHandler = new Handler(
                getDetectedActivityThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                handleRequest(msg);
            }
        };
    }

    private void handleRequest(Message msg){
        switch (msg.what) {
            case START_RECOGNIZING:
                startActivityRecognition();
                break;
            case STOP_RECOGNIZING:
                stopActivityRecognition();
                break;
            default:
                break;
        }
    }

    @Override
    public void onDestroy() {
        unregisterListener();
    }

    private void executeTask(int taskType){
        Message msg = Message.obtain(getDetectedActivityHandler, taskType);
        msg.sendToTarget();
    }

    private void startActivityRecognition() {
        for(int configIndex = 0; configIndex < addedConfigs.size(); ++configIndex){
            final MynaRecognizerInterface classifier = addedConfigs.valueAt(configIndex);
            classifier.dataFusionHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (classifier.dataSet[classifier.dataSetIndex] == null) {
                        classifier.dataSet[classifier.dataSetIndex] = new SensorData();
                        classifier.dataSet[classifier.dataSetIndex].clone(latestSampledData);
                        classifier.dataSet[classifier.dataSetIndex].timestamp = System.currentTimeMillis();
                        Utils.calculateWorldAcce(classifier.dataSet[classifier.dataSetIndex]);
                        classifier.dataSetIndex++;
                        if (classifier.dataSetIndex == classifier.getSamplingPointCount() / 2) {
                            classifier.dataSetIndex = 0;
                        }
                    }else{
                        if (classifier.dataSet[classifier.getSamplingPointCount() / 2 + classifier.dataSetIndex] != null) {
                            classifier.dataSet[classifier.dataSetIndex].clone(
                                    classifier.dataSet[classifier.getSamplingPointCount() / 2 + classifier.dataSetIndex]);
                        }
                        classifier.dataSet[classifier.getSamplingPointCount() / 2 + classifier.dataSetIndex] = new SensorData();
                        classifier.dataSet[classifier.getSamplingPointCount() / 2 + classifier.dataSetIndex].clone(latestSampledData);
                        classifier.dataSet[classifier.dataSetIndex].timestamp = System.currentTimeMillis();
                        Utils.calculateWorldAcce(classifier.dataSet[classifier.dataSetIndex]);
                        if (classifier.dataSetIndex == classifier.getSamplingPointCount() / 2 - 1) {
                            try {
                                classifier.recognitionHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        classifier.onResult(classifier.classifier.recognize(classifier.dataSet));
                                    }
                                });
                            } catch (Throwable t) {
                                t.printStackTrace();
                            }
                        }
                        classifier.dataSetIndex = (classifier.dataSetIndex + 1) % (classifier.getSamplingPointCount() / 2);
                    }
                    classifier.dataFusionHandler.postDelayed(this, classifier.getSamplingDuration());
                }
            });
        }
    }

    private void stopActivityRecognition() {
        for(int configIndex = 0; configIndex < addedConfigs.size(); ++configIndex){
            MynaRecognizerInterface config = addedConfigs.valueAt(configIndex);
            config.dataFusionHandler.removeCallbacksAndMessages(null);
        }
        unregisterListener();
        isRecognizing = false;
    }

    /*
     * Register this as a sensor event listener.
     */
    private void registerListener() {
        MynaRecognizerInterface config;
        SparseIntArray configuredSensors;
        for(int configIndex = 0; configIndex < addedConfigs.size(); ++configIndex){
            config = addedConfigs.valueAt(configIndex);
            configuredSensors = config.getAllSensors();
            for(int sensorIndex = 0; sensorIndex < configuredSensors.size(); ++sensorIndex){
                checkAndRegisterSensor(configuredSensors.keyAt(sensorIndex));
            }
        }
    }

    private void checkAndRegisterSensor(int sensorType){
        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        Sensor sensor;
        for (int index = 0; index < sensors.size(); ++index) {
            sensor = sensors.get(index);
            if (sensor.getType() == sensorType) {
                mSensorManager.registerListener(this, sensor,
                        SensorManager.SENSOR_DELAY_FASTEST);
            }
        }
    }

    private void unregisterListener() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            updateSensorData(latestSampledData.accelerate, event);
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            updateSensorData(latestSampledData.gyroscope, event);
        } else if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
            updateSensorData(latestSampledData.gravity, event);
        } else if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            latestSampledData.light = event.values[0];
        } else if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
            latestSampledData.pressure = event.values[0];
        } else if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
            latestSampledData.temperature = event.values[0];
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            updateSensorData(latestSampledData.magnetic, event);
        } else if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            updateSensorData(latestSampledData.magnetic, event);
        }
    }

    private void updateSensorData(float[] D, SensorEvent event) {
        if (D == null) {
            D = new float[3];
        }
        System.arraycopy(event.values, 0, D, 0, 3);
    }
}
