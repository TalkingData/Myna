package com.talkingdata.myna.demo;

import android.hardware.Sensor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.talkingdata.myna.DataScientistAPI;
import com.talkingdata.myna.HandHoldingClassifier;
import com.talkingdata.myna.HandHoldingRecognizer;
import com.talkingdata.myna.HumanActivityRecognizer;
import com.talkingdata.myna.MynaInitCallback;
import com.talkingdata.myna.MynaResult;
import com.talkingdata.myna.MynaResultCallback;
import com.talkingdata.myna.RandomForestClassifier;
import com.talkingdata.myna.RecognizedActivity;
import com.talkingdata.myna.RecognizedActivityResult;
import com.talkingdata.myna.XGBoostClassifier;
import com.talkingdata.myna.tools.Utils;


public class DataScientistActivity extends AppCompatActivity {

    private boolean isInitialized = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DataScientistAPI.init(this, new MyInitCallback(), new MyCallback());
    }

    @Override
    protected void onPause() {
        if(isInitialized){
            DataScientistAPI.stop();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isInitialized){
            DataScientistAPI.start();
        }
    }

    private class MyInitCallback implements MynaInitCallback {

        @Override
        public void onSucceeded() {
            Toast.makeText(getApplicationContext(), "Myna initialization Succeeded!", Toast.LENGTH_LONG).show();
            isInitialized = true;
            doAsDataScientist();
        }

        @Override
        public void onFailed(MynaResult error) {
            Toast.makeText(getApplicationContext(), "Myna initialization failed!", Toast.LENGTH_LONG).show();
            isInitialized = false;
        }
    }

    private class MyCallback implements MynaResultCallback<RecognizedActivityResult> {

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

    private void doAsDataScientist(){
        xgBoost();
    }

    private void randomForest(){
        String trainedTrees = Utils.loadFeaturesFromAssets(getApplicationContext(), "classificator.json");
        RandomForestClassifier randomForestClassifier = new RandomForestClassifier(trainedTrees);
        HumanActivityRecognizer humanActivityRecognizer = new HumanActivityRecognizer(randomForestClassifier, new MyCallback());
        humanActivityRecognizer.setSamplingPointCount(128);
        humanActivityRecognizer.setSamplingInterval(50);
        DataScientistAPI.addRecognizer(humanActivityRecognizer);
        DataScientistAPI.start();
    }

    private void xgBoost(){
        XGBoostClassifier xgBoostClassifier = new XGBoostClassifier(this.getApplicationContext());
        HumanActivityRecognizer humanActivityRecognizer = new HumanActivityRecognizer(xgBoostClassifier, new MyCallback());
        humanActivityRecognizer.setSamplingPointCount(128);
        humanActivityRecognizer.setSamplingInterval(50);
        DataScientistAPI.addRecognizer(humanActivityRecognizer);
        DataScientistAPI.start();
    }

    private void handHolding(){
        HandHoldingClassifier xgBoostClassifier = new HandHoldingClassifier();
        HandHoldingRecognizer handHoldingRecognizer = new HandHoldingRecognizer(xgBoostClassifier, new MyCallback());
        handHoldingRecognizer.setSamplingInterval(20);
        handHoldingRecognizer.addSensorType(Sensor.TYPE_GAME_ROTATION_VECTOR);
        DataScientistAPI.addRecognizer(handHoldingRecognizer);
        DataScientistAPI.start();
    }
}
