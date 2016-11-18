package com.talkingdata.sdk.myna;

import android.content.Context;

interface MynaInterface {

    /**
     * Initialize Myna
     * @param context Application context
     * @param initCallback Callback to handle the result of initialization
     * @param resultCallback Callback to handle the result of initialization
     */
    void init(Context context, MynaInitCallback initCallback, MynaResultCallback resultCallback);

    /**
     * Start to recognize
     */
    void start();

    /**
     * Stop all background tasks
     */
    void stop();

    /**
     * Get the status of Myna initialization
     */
    boolean isInitialized();
}
