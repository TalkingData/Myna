package com.talkingdata.sdk.myna;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import static android.content.Context.BIND_AUTO_CREATE;

public class DataScientistAPI {

    private static boolean isInitialized = false;
    private static MynaService.MyBinder myBinder;
    private static MynaInitCallback s_initCallback;

    /**
     * Initialize Myna
     * @param context Application context
     * @param initCallback Callback to handle the result of initialization
     */
    public static void init(Context context, MynaInitCallback initCallback){
        s_initCallback = initCallback;
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
     * Stop all background tasks
     */
    public static void stop(){
        MynaHelper.stop();
    }

    /**
     * Start to recognize
     */
    public static void start(){
        MynaHelper.start();
    }

    /**
     * Get the status of Myna initialization
     */
    public static boolean isInitialized(){
        return isInitialized;
    }

    /**
     * Add a new recognition configuration to be executed later
     */
    public static void addRecognizer(MynaRecognizerInterface recognizer){
        myBinder.addRecognizer(recognizer);
    }

    /**
     * Clean Myna env
     */
    public static void cleanUp(Context ctx){
        stop();
        myBinder.cleanUp();
        ctx.unbindService(connection);
    }
    /**
     * Remove a new recognition configuration to be executed later
     */
    public static void removeRecognizer(int configId){
        myBinder.removeRecognizer(configId);
    }

    private static ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isInitialized = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myBinder = (MynaService.MyBinder) service;
            s_initCallback.onSucceeded();
            isInitialized = true;
        }
    };
}
