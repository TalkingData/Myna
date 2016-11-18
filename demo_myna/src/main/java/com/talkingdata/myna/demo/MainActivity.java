package com.talkingdata.myna.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.talkingdata.sdk.myna.MynaApi;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        Log.i("Myna", MynaApi.isInitialized() ? "Myna has been initialized successfully!" : "My has not been initialized yet.");
        if(MynaApi.isInitialized()){
            MynaApi.start();
        }
    }
}
