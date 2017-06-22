package com.talkingdata.myna;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.DetectedActivityResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.talkingdata.myna.tools.Utils;

import java.util.List;

class AwarenessImpl implements MynaInterface, ConnectionCallbacks, OnConnectionFailedListener, ResultCallback<Status> {

    private GoogleApiClient client;
    private boolean isInitialized = false;
    private MynaInitCallback initCallback;
    private MynaResultCallback resultCallback;
    private Handler googleHandler;

    AwarenessImpl(MynaInitCallback initCallback, MynaResultCallback resultCallback){
        this.initCallback = initCallback;
        this.resultCallback = resultCallback;
    }

    @Override
    public void init(Context context) {
        client = new GoogleApiClient.Builder(context)
                .addApi(Awareness.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        client.connect();

        HandlerThread googleHandleThread = new HandlerThread("googleHandleThread");
        googleHandleThread.start();
        googleHandler = new Handler(googleHandleThread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                Awareness.SnapshotApi.getDetectedActivity(client)
                        .setResultCallback(new ResultCallback<DetectedActivityResult>(){
                            @Override
                            public void onResult(@NonNull DetectedActivityResult detectedActivityResult) {
                                handleResult(detectedActivityResult);
                            }
                        });
                Message newMsg = new Message();
                newMsg.what = 0;
                googleHandler.sendMessageDelayed(newMsg, 5000);
            }
        };
    }

    @Override
    public void start() {
        if(!client.isConnected()){
            client.reconnect();
        }
        Message msg = Message.obtain(googleHandler);
        msg.what = 0;
        msg.sendToTarget();
        isInitialized = true;
    }

    @Override
    public boolean isInitialized() {
        return isInitialized;
    }

    @Override
    public void stop() {
        googleHandler.removeMessages(0);
        client.disconnect();
        isInitialized = false;
    }

    /**
     * Add a new recognition configuration to be executed later
     */
    @Override
    public void addRecognizer(MynaRecognizerAbstractClass recognizer){
    }

    /**
     * Remove a new recognition configuration to be executed later
     */
    @Override
    public void removeRecognizer(int configId){
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(Utils.TAG, "!!! Great !!! Connected to GoogleApiClient");
        initCallback.onSucceeded();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(Utils.TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
        Log.i(Utils.TAG, "Connection failed: ConnectionResult.getErrorMessage() = " + result.getErrorMessage());
        MynaResult m_result = new MynaResult(result.getErrorCode(), result.getErrorMessage());
        initCallback.onFailed(m_result);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onResult(@NonNull Status status) {

    }

    private void handleResult(@NonNull DetectedActivityResult detectedActivityResult){
        ActivityRecognitionResult ar = detectedActivityResult.getActivityRecognitionResult();
        RecognizedActivityResult result = new RecognizedActivityResult();
        List<DetectedActivity> acts = ar.getProbableActivities();
        result.activities = new RecognizedActivity[acts.size()];
        for(int i = 0; i < acts.size(); ++i){
            DetectedActivity act = acts.get(i);
            result.activities[i] = new RecognizedActivity(act.getType(), act.getConfidence());
        }
        resultCallback.onResult(result);
    }
}
