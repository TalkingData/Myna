package com.talkingdata.sdk.myna;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import static android.content.Context.BIND_AUTO_CREATE;

class MynaImpl implements MynaInterface {

    private boolean isInitialized = false;

    private MynaService.MyBinder myBinder;
    private MynaInitCallback initCallback;
    private MynaResultCallback resultCallback;
    private Context ctx;

    MynaImpl(MynaInitCallback callback, MynaResultCallback resultCallback){
        this.initCallback = callback;
        this.resultCallback = resultCallback;
    }

    /**
     * Initialize Myna
     * @param context Application context
     * @param initCallback Callback to handle the result of initialization
     * @param resultCallback Callback to handle the result of initialization
     */
    @Override
    public void init(Context context, MynaInitCallback initCallback, MynaResultCallback resultCallback) {
        this.ctx = context;
        try{
            Intent bindIntent = new Intent(context, MynaService.class);
            context.bindService(bindIntent, connection, BIND_AUTO_CREATE);
        }catch (Throwable t){
            isInitialized = false;
            MynaResult m_result = new MynaResult(-1, t.getLocalizedMessage());
            initCallback.onFailed(m_result);
        }
    }

    /**
     * Start to recognize
     */
    @Override
    public void start() {
        myBinder.startRecognizing();
    }

    /**
     * Stop all background tasks
     */
    @Override
    public void stop() {
        myBinder.stopRecognizing();
    }


    /**
     * Get the status of Myna initialization
     */
    @Override
    public boolean isInitialized(){
        return isInitialized;
    }

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isInitialized = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myBinder = (MynaService.MyBinder) service;
            RandomForestClassifier randomForestClassifier = new RandomForestClassifier(ctx);
            HumanActivityRecognizer humanActivityRecognizer = new HumanActivityRecognizer(randomForestClassifier, resultCallback);
            humanActivityRecognizer.setSamplingPointCount(512);
            humanActivityRecognizer.setSamplingDuration(20);
            myBinder.addRecognizer(humanActivityRecognizer);
            initCallback.onSucceeded();
            isInitialized = true;
        }
    };
}
