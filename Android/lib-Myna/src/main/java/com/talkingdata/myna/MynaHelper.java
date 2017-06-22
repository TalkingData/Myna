package com.talkingdata.myna;

import android.content.Context;

import com.talkingdata.myna.tools.Utils;

class MynaHelper {

    private static MynaInterface mynaImpl = null;
    private synchronized static MynaInterface prepareImpl(Context context,
                                                          MynaInitCallback initCallback,
                                                          MynaResultCallback resultCallback,
                                                          int mode,
                                                          boolean keepDefaultRecognizer){
        if(mynaImpl == null){
            synchronized (MynaInterface.class){
                if(mynaImpl == null){
                    if(mode == MynaApi.GOOGLE && Utils.isGooglePlayServiceSupported(context)){
                        mynaImpl = new AwarenessImpl(initCallback, resultCallback);
                    }else{
                        mynaImpl = new MynaImpl(initCallback, resultCallback, keepDefaultRecognizer);

                    }
                }
            }
        }
        mynaImpl.init(context);
        return mynaImpl;
    }

    /**
     * Initialize Myna
     * @param context Application context
     * @param initCallback Callback to handle the result of initialization
     * @param mode Indicator of user choice of implementation.
     *             Available values:
     *             <code>TALKINGDATA</code>,
     *             <code>GOOGLE</code>
     */
    static void init(Context context, MynaInitCallback initCallback, MynaResultCallback resultCallback, int mode){
        if(context != null){
            init(context, initCallback, resultCallback, mode, true);
        }
    }

    static void init(Context context, MynaInitCallback initCallback, MynaResultCallback resultCallback, int mode, boolean keepDefaultRecognizer){
        if(context != null){
            prepareImpl(context, initCallback, resultCallback, mode, keepDefaultRecognizer);
        }
    }

    /**
     * Start to recognize
     */
    static void start(){
        mynaImpl.start();
    }

    /**
     * Stop all background tasks
     */
    static void stop(){
        mynaImpl.stop();
    }

    /**
     * Add a new recognition configuration to be executed later
     */
    static void addRecognizer(MynaRecognizerAbstractClass recognizer){
        mynaImpl.addRecognizer(recognizer);
    }

    /**
     * Remove a new recognition configuration to be executed later
     */
    static void removeRecognizer(int configId){
        mynaImpl.removeRecognizer(configId);
    }


    /**
     * Get the status of Myna initialization
     */
    static boolean isInitialized(){
        return mynaImpl.isInitialized();
    }

    static void train(MynaTrainTestCallback ttCallback, Context ctx){
        MynaTrainTest tt = new MynaTrainTest(ttCallback, ctx);
        tt.startTrainingWithExistedData();
    }

    static void test(MynaTrainTestCallback ttCallback, Context ctx, String classifierType){
        MynaTrainTest tt = new MynaTrainTest(ttCallback, ctx);
        tt.startTestingWithExistedData(classifierType);
    }
}
