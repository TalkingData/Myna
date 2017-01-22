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

import com.talkingdata.myna.MynaApi;
import com.talkingdata.myna.MynaTrainTestCallback;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class TrainActivity extends AppCompatActivity {

    private StringBuilder strBuilder = new StringBuilder();
    private HashMap<String, Integer> totalActNum = new HashMap<>();
    private HashMap<String, Integer> succeededActNumRDT = new HashMap<>();
    private String detailedLogStr = null;
    Handler handler;
    private final int TRAINING = 0;
    private final int TESTING =1;

    private Button bt_start_training;
    private Button bt_start_testing;
    private TextView trainingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.training);
        bt_start_training = (Button) findViewById(R.id.bt_start_training);
        bt_start_testing = (Button) findViewById(R.id.bt_start_testing);
        trainingView = (TextView)findViewById(R.id.tv_result);
        HandlerThread ht = new HandlerThread("ht");
        ht.start();
        handler = new Handler(ht.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case TRAINING:
                        MynaApi.train(new MyTrainTestImpl(), getApplicationContext());
                        break;
                    case TESTING:
                        resetTestingResults();
                        MynaApi.test(new MyTrainTestImpl(), getApplicationContext());
                        break;
                    default:
                        break;
                }
            }
        };
        setListener();
    }
    private void setListener() {
        bt_start_training.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableButtons();
                handler.sendEmptyMessage(TRAINING);
            }
        });

        bt_start_testing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableButtons();
                handler.sendEmptyMessage(TESTING);
            }
        });
    }

    private void disableButtons(){
        Handler mainHandler = new Handler(getMainLooper());
        mainHandler.post(new Runnable() {

            @Override
            public void run() {
                bt_start_testing.setEnabled(false);
                bt_start_training.setEnabled(false);
            }
        });
    }

    private void enableButtons(){
        Handler mainHandler = new Handler(getMainLooper());
        mainHandler.post(new Runnable() {

            @Override
            public void run() {
                bt_start_testing.setEnabled(true);
                bt_start_training.setEnabled(true);
            }
        });
    }

    private void resetTestingResults(){
        resetTestingResultsInDetail(totalActNum);
        resetTestingResultsInDetail(succeededActNumRDT);
    }

    private void resetTestingResultsInDetail(HashMap<String, Integer> hMap){
        hMap.clear();
        hMap.put(getActivityName(1), 0);
        hMap.put(getActivityName(4), 0);
        hMap.put(getActivityName(7), 0);
    }

    private String getActivityName(final int label){
        StringBuilder singleActivityStrBdr = new StringBuilder();

        if (label == 1) {
            singleActivityStrBdr.append("步行");
        } else if (label == 4) {
            singleActivityStrBdr.append("乘车");
        } else if (label == 7) {
            singleActivityStrBdr.append("静止");
        }

        String result = singleActivityStrBdr.toString();
        return result.substring(result.indexOf("-") + 1);
    }

    class MyTrainTestImpl implements MynaTrainTestCallback {
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
        public void onResult(final int[] detectedActivity, final double[] probability) {
            strBuilder = new StringBuilder();
            Date date = new Date();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault());
            strBuilder.append(df.format(date));
            strBuilder.append(": ");
            StringBuilder detectStrBdr = new StringBuilder();
            String mostProbActivity = "";
            double maxProb = 0F;
            for (int i = 0; i < detectedActivity.length; ++i) {
                int act = detectedActivity[i];
                double prob = Double.isNaN(probability[i]) ? 0 : probability[i];
                String currentActivity = getActivityName(act);
                detectStrBdr.append(String.format(Locale.getDefault(), "\t%-15s \t %10.2f%%\n",
                        currentActivity,
                        prob * 100));
                if (Double.compare(maxProb, prob) < 0) {
                    maxProb = prob;
                    mostProbActivity = currentActivity;
                }
            }
            if(mostProbActivity.trim().isEmpty()){
                strBuilder.append("Unknown\n");
            }else{
                strBuilder.append(mostProbActivity);
                strBuilder.append("\n");
                strBuilder.append(detectStrBdr);
            }
            String output = strBuilder.toString();
            Log.i("rHAR", output);
            strBuilder.append(trainingView.getText());
            updateUI(strBuilder.toString());
        }

        @Override
        public
        void onTestingResult(final int[] detectedActivity, final double[] probRDT, final long durationRDT,
                             final String fileName, final int label) {
            strBuilder = new StringBuilder();
            String mostProbActivityRDT = "";
            double maxProbRDT = 0F;
            for (int i = 0; i < detectedActivity.length; ++i) {
                int act = detectedActivity[i];
                String currentActivity = getActivityName(act);
                double prob = Double.isNaN(probRDT[i]) ? 0 : probRDT[i];
                if (Double.compare(maxProbRDT, prob) < 0) {
                    maxProbRDT = prob;
                    mostProbActivityRDT = currentActivity;
                }
            }

            String realActivity = getActivityName(label);
            totalActNum.put(realActivity, totalActNum.get(realActivity) + 1);
            if(mostProbActivityRDT.equals(realActivity)){
                succeededActNumRDT.put(realActivity, succeededActNumRDT.get(realActivity) + 1);
            }
            strBuilder.append(new File(fileName).getName());
            strBuilder.append("\n");
            if(mostProbActivityRDT.trim().isEmpty()){
                strBuilder.append(String.format(Locale.getDefault(),
                        "\tActual: %s, Predicted: %s\n", realActivity, mostProbActivityRDT));
            }else{
                strBuilder.append(String.format(Locale.getDefault(),
                        "\tActual: %s, Predicted: %s\n", realActivity, mostProbActivityRDT));
            }
            strBuilder.append(String.format(Locale.CHINESE, "durationRDT: %dms\n", durationRDT));
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
        void onFinalReport(final String RDTSettings){
            if(RDTSettings == null || RDTSettings.isEmpty()){
                updateUI("测试模型不存在，请先训练。");
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Total Report:\n");
            sb.append(RDTSettings);
            sb.append("\n");
            sb.append(String.format("\t%-20s\t%-10s\n", "Activity", "RDT"));
            for(String keyStr : totalActNum.keySet()){
                int succeededCountRDT = succeededActNumRDT.get(keyStr);
                int successfulRateRDT = 0;
                int totalCount = totalActNum.get(keyStr);
                if(totalCount != 0){
                    successfulRateRDT = succeededCountRDT * 100 / totalCount;
                }
                sb.append(String.format(Locale.CHINESE, "\t%-12s\t%3d/%-3d(%3d%%)\n", keyStr,
                        succeededCountRDT, totalCount, successfulRateRDT));
            }
            final String reportStr = sb.toString();
            Log.i("rHAR", reportStr);
            final String finalReportStr = reportStr + detailedLogStr;
            updateUI(finalReportStr);
        }
    }
}
