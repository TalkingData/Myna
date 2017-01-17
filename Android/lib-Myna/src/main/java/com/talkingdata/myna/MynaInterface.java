package com.talkingdata.myna;

import android.content.Context;

interface MynaInterface {

    /**
     * Initialize Myna
     * @param context Application context
     */
    void init(Context context);

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
