package com.talkingdata.myna.demo;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.talkingdata.myna.DataScientistAPI;
import com.talkingdata.myna.HumanActivityRecognizer;
import com.talkingdata.myna.MynaApi;
import com.talkingdata.myna.MynaInitCallback;
import com.talkingdata.myna.MynaResult;
import com.talkingdata.myna.MynaResultCallback;
import com.talkingdata.myna.RandomForestClassifier;
import com.talkingdata.myna.RecognizedActivity;
import com.talkingdata.myna.RecognizedActivityResult;


public class DemoApplication extends Application{

    final static String TAG = "DemoLog";
    @Override
    public void onCreate() {
        super.onCreate();
    }
}
