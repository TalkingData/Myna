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
     * Add a new recognition configuration to be executed later
     */
    void addRecognizer(MynaRecognizerAbstractClass recognizer);

    /**
     * Remove a new recognition configuration to be executed later
     */
    void removeRecognizer(int configId);

    /**
     * Get the status of Myna initialization
     */
    boolean isInitialized();
}
