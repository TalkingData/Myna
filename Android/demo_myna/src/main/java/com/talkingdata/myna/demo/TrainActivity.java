package com.talkingdata.myna.demo;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.talkingdata.myna.LSTMClassifier;
import com.talkingdata.myna.MynaApi;
import com.talkingdata.myna.MynaTrainTestCallback;
import com.talkingdata.myna.RandomForestClassifier;
import com.talkingdata.myna.RecognizedActivity;
import com.talkingdata.myna.RecognizedActivityResult;
import com.talkingdata.myna.XGBoostClassifier;

import java.util.HashMap;
import java.util.Locale;

public class TrainActivity extends AppCompatActivity {

    private HashMap<String, Integer> totalActNum = new HashMap<>();
    private HashMap<String, Integer> succeededActNumRDT = new HashMap<>();
    private String detailedLogStr = null;
    Handler handler;
    private final int RF_TRAINING = 0;
    private final int RF_TESTING = 1;
    private final int XGBOOST_TESTING = 2;
    private final int LSTM_TESTING = 3;

    private Button bt_rf_training;
    private Button bt_rf_testing;
    private Button bt_xgboost_testing;
    private Button bt_lstm_testing;
    private TextView trainingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.training);
        bt_rf_training = (Button) findViewById(R.id.bt_rf_training);
        bt_rf_testing = (Button) findViewById(R.id.bt_rf_testing);
        bt_xgboost_testing = (Button) findViewById(R.id.bt_xgboost_testing);
        bt_lstm_testing = (Button) findViewById(R.id.bt_lstm_testing);
        trainingView = (TextView)findViewById(R.id.tv_result);
        HandlerThread ht = new HandlerThread("ht");
        ht.start();
        handler = new Handler(ht.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case RF_TRAINING:
                        MynaApi.train(new MyTrainTestImpl(), getApplicationContext());
                        break;
                    case RF_TESTING:
                        resetTestingResults();
                        MynaApi.test(new MyTrainTestImpl(), getApplicationContext(), RandomForestClassifier.TYPE);
                        break;
                    case XGBOOST_TESTING:
                        resetTestingResults();
                        MynaApi.test(new MyTrainTestImpl(), getApplicationContext(), XGBoostClassifier.TYPE);
                        break;
                    case LSTM_TESTING:
                        resetTestingResults();
                        MynaApi.test(new MyTrainTestImpl(), getApplicationContext(), LSTMClassifier.TYPE);
                        break;
                    default:
                        break;
                }
            }
        };
        setListener();
    }
    private void setListener() {
        bt_rf_training.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableButtons();
                handler.sendEmptyMessage(RF_TRAINING);
            }
        });

        bt_rf_testing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableButtons();
                handler.sendEmptyMessage(RF_TESTING);
            }
        });

        bt_xgboost_testing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableButtons();
                handler.sendEmptyMessage(XGBOOST_TESTING);
            }
        });

        bt_lstm_testing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableButtons();
                handler.sendEmptyMessage(LSTM_TESTING);
            }
        });
    }

    private void disableButtons(){
        Handler mainHandler = new Handler(getMainLooper());
        mainHandler.post(new Runnable() {

            @Override
            public void run() {
                bt_xgboost_testing.setEnabled(false);
                bt_rf_training.setEnabled(false);
                bt_rf_testing.setEnabled(false);
                bt_lstm_testing.setEnabled(false);
            }
        });
    }

    private void enableButtons(){
        Handler mainHandler = new Handler(getMainLooper());
        mainHandler.post(new Runnable() {

            @Override
            public void run() {
                bt_xgboost_testing.setEnabled(true);
                bt_rf_training.setEnabled(true);
                bt_rf_testing.setEnabled(true);
                bt_lstm_testing.setEnabled(true);
            }
        });
    }

    private void resetTestingResults(){
        resetTestingResultsInDetail(totalActNum);
        resetTestingResultsInDetail(succeededActNumRDT);
    }

    private void resetTestingResultsInDetail(HashMap<String, Integer> hMap){
        hMap.clear();
        hMap.put(getActivityName(RecognizedActivity.WALKING), 0);
        hMap.put(getActivityName(RecognizedActivity.RUNNING), 0);
        hMap.put(getActivityName(RecognizedActivity.BUS), 0);
        hMap.put(getActivityName(RecognizedActivity.SUBWAY), 0);
        hMap.put(getActivityName(RecognizedActivity.CAR), 0);
        hMap.put(getActivityName(RecognizedActivity.STILL), 0);
    }

    private String getActivityName(final int label){
        return RecognizedActivity.getActivityName(label);
    }

    private class MyTrainTestImpl implements MynaTrainTestCallback {
        private void updateUI(final String msg){
            Handler mainHandler = new Handler(getMainLooper());
            mainHandler.post(new Runnable() {

                @Override
                public void run() {
                    trainingView.setText(msg);
                }
            });
        }
        @Override
        public void onTrainingFinished(String msg) {
            updateUI(msg);
            enableButtons();
        }

        @Override
        public void onTrainingProgress(final String msg) {
            updateUI(msg);
        }

        @Override
        public
        void onTestingResult(final RecognizedActivityResult result, final int label) {
            StringBuilder strBuilder = new StringBuilder();
            String mostProbActivityRDT = "";
            double maxProbRDT = 0F;
            RecognizedActivity[] probableActivities = result.getProbableActivities();
            for (RecognizedActivity act : probableActivities) {
                String currentActivity = RecognizedActivity.getActivityName(act.getActivityType());
                double prob = Double.isNaN(act.getActivityPossibility()) ? 0 : act.getActivityPossibility();
                if (Double.compare(maxProbRDT, prob) < 0) {
                    maxProbRDT = prob;
                    mostProbActivityRDT = currentActivity;
                }
            }

            String realActivity = RecognizedActivity.getActivityName(label);
            totalActNum.put(realActivity, totalActNum.get(realActivity) + 1);
            if(mostProbActivityRDT.equals(realActivity)){
                succeededActNumRDT.put(realActivity, succeededActNumRDT.get(realActivity) + 1);
            }
            if(mostProbActivityRDT.trim().isEmpty()){
                strBuilder.append(String.format(Locale.getDefault(),
                        "\tActual: %s, Predicted: %s\n", realActivity, mostProbActivityRDT));
            }else{
                strBuilder.append(String.format(Locale.getDefault(),
                        "\tActual: %s, Predicted: %s\n", realActivity, mostProbActivityRDT));
            }
            String output = strBuilder.toString();
            Log.i("rHAR", output);
            detailedLogStr = strBuilder.append(detailedLogStr).toString();
            String tempDisplayLogStr = detailedLogStr;
            if(detailedLogStr.length() > 2048){
                tempDisplayLogStr = tempDisplayLogStr.substring(0, 2048);
            }
            final String displayLogStr = tempDisplayLogStr;
            updateUI(displayLogStr);
        }

        @Override
        public
        void onFinalReport(final String result, final long duration){
            StringBuilder sb = new StringBuilder();
            sb.append("Total Report:\n");
            if(result != null){
                sb.append(result);
                sb.append("\n");
            }
            sb.append(String.format(Locale.getDefault(), "Total duration: %d ms", duration));
            sb.append("\n");
            sb.append(String.format("\t%-20s\t%-10s\n", "Activity", "RDT"));
            for(String keyStr : totalActNum.keySet()){
                int succeededCountRDT = succeededActNumRDT.get(keyStr);
                int successfulRateRDT = 0;
                int totalCount = totalActNum.get(keyStr);
                if(totalCount != 0){
                    successfulRateRDT = succeededCountRDT * 100 / totalCount;
                }
                sb.append(String.format(Locale.getDefault(), "\t%-12s\t%3d/%-3d(%3d%%)\n", keyStr,
                        succeededCountRDT, totalCount, successfulRateRDT));
            }
            final String reportStr = sb.toString();
            Log.i("rHAR", reportStr);
            updateUI(reportStr);
            enableButtons();
        }
    }
}
