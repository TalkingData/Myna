package com.talkingdata.myna.demo;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.talkingdata.myna.MynaApi;
import com.talkingdata.myna.MynaInitCallback;
import com.talkingdata.myna.MynaResult;
import com.talkingdata.myna.MynaResultCallback;
import com.talkingdata.myna.RecognizedActivity;
import com.talkingdata.myna.RecognizedActivityResult;

public class DeveloperActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MynaApi.init(this, new MyInitCallback(), new MyCallback(), MynaApi.TALKINGDATA);
    }

    @Override
    protected void onPause() {
        if(MynaApi.isInitialized()){
            MynaApi.stop();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(MynaApi.isInitialized()){
            MynaApi.start();
        }
    }

    class MyInitCallback implements MynaInitCallback {

        @Override
        public void onSucceeded() {
            Toast.makeText(getApplicationContext(), "Myna initialization Succeeded!", Toast.LENGTH_LONG).show();
            MynaApi.start();
        }

        @Override
        public void onFailed(MynaResult error) {
            Toast.makeText(getApplicationContext(), "Myna initialization failed!", Toast.LENGTH_LONG).show();
        }
    }

    class MyCallback implements MynaResultCallback<RecognizedActivityResult> {

        @Override
        public void onResult(@NonNull RecognizedActivityResult detectedResults) {
            StringBuilder sb = new StringBuilder();
            for(RecognizedActivity act : detectedResults.getProbableActivities()){
                sb.append(act.toString());
                sb.append("\n");
            }
            String result = sb.toString();
            Log.i(DemoApplication.TAG, result);
            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
        }
    }
}
