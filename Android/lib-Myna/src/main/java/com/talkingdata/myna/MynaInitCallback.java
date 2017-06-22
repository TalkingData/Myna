package com.talkingdata.myna;


/**
 * Define resultCallback methods to handle different initialization results.
 */
public interface MynaInitCallback {

    /**
     * Called when Myna is successfully initialized.
     */
    void onSucceeded();

    /**
     * Called when Myna failed to initialize.
     */
    void onFailed(MynaResult error);
}
