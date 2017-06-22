package com.talkingdata.myna;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import static android.content.Context.BIND_AUTO_CREATE;

public class DataScientistAPI {

    /**
     * Initialize Myna
     * @param context Application context
     * @param initCallback Callback to handle the result of initialization
     */
    public static void init(Context context, MynaInitCallback initCallback, MynaResultCallback resultCallback){
        try{
            MynaHelper.init(context, initCallback, resultCallback, MynaApi.TALKINGDATA, false);
        }catch (Throwable t){
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
     * Add a new recognition configuration to be executed later
     */
    public static void addRecognizer(MynaRecognizerAbstractClass recognizer){
        MynaHelper.addRecognizer(recognizer);
    }

    /**
     * Clean Myna env
     */
    public static void cleanUp(Context ctx){
        stop();
    }

    /**
     * Remove a new recognition configuration to be executed later
     */
    public static void removeRecognizer(int configId){
        MynaHelper.removeRecognizer(configId);
    }
}
