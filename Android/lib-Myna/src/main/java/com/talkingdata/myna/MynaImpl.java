package com.talkingdata.myna;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.talkingdata.myna.tools.Utils;

import static android.content.Context.BIND_AUTO_CREATE;

class MynaImpl implements MynaInterface {

    private boolean isInitialized = false;

    private MynaService.MyBinder myBinder;
    private MynaInitCallback initCallback;
    private MynaResultCallback resultCallback;
    private Context ctx;
    private boolean keepDefaultRecognizer = true;

    MynaImpl(MynaInitCallback callback, MynaResultCallback resultCallback, boolean isKeep){
        this.initCallback = callback;
        this.resultCallback = resultCallback;
        this.keepDefaultRecognizer = isKeep;
    }

    /**
     * Initialize Myna
     * @param context Application context
     */
    @Override
    public void init(Context context) {
        ctx = context;
        try{
            Intent bindIntent = new Intent(ctx, MynaService.class);
            ctx.bindService(bindIntent, connection, BIND_AUTO_CREATE);
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
        try{
            Intent bindIntent = new Intent(ctx, MynaService.class);
            ctx.bindService(bindIntent, connection, BIND_AUTO_CREATE);
        }catch (Throwable t){
            isInitialized = false;
            MynaResult m_result = new MynaResult(-1, t.getLocalizedMessage());
            initCallback.onFailed(m_result);
        }
        myBinder.startRecognizing();
    }

    /**
     * Stop all background tasks
     */
    @Override
    public void stop() {
        myBinder.stopRecognizing();
        ctx.unbindService(connection);
    }

    /**
     * Add a new recognition configuration to be executed later
     */
    @Override
    public void addRecognizer(MynaRecognizerAbstractClass recognizer){
        myBinder.addRecognizer(recognizer);
    }

    /**
     * Remove a new recognition configuration to be executed later
     */
    @Override
    public void removeRecognizer(int configId){
        myBinder.removeRecognizer(configId);
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
            if(keepDefaultRecognizer){
                String trainedTrees = Utils.loadFeaturesFromAssets(ctx, "classificator.json");
                RandomForestClassifier randomForestClassifier = new RandomForestClassifier(trainedTrees);
                HumanActivityRecognizer humanActivityRecognizer = new HumanActivityRecognizer(randomForestClassifier, resultCallback);
                humanActivityRecognizer.setSamplingPointCount(512);
                humanActivityRecognizer.setSamplingInterval(20);
                myBinder.addRecognizer(humanActivityRecognizer);
            }
            initCallback.onSucceeded();
            isInitialized = true;
        }
    };
}
