package com.talkingdata.myna.demo;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.talkingdata.sdk.myna.DataScientistAPI;
import com.talkingdata.sdk.myna.HumanActivityRecognizer;
import com.talkingdata.sdk.myna.MynaApi;
import com.talkingdata.sdk.myna.MynaInitCallback;
import com.talkingdata.sdk.myna.MynaResult;
import com.talkingdata.sdk.myna.MynaResultCallback;
import com.talkingdata.sdk.myna.RandomForestClassifier;
import com.talkingdata.sdk.myna.RecognizedActivity;
import com.talkingdata.sdk.myna.RecognizedActivityResult;


public class DemoApplication extends Application{

    final static String TAG = "DemoLog";
    @Override
    public void onCreate() {
        super.onCreate();
    }
}
