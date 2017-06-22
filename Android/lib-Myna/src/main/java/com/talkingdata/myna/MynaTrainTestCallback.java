package com.talkingdata.myna;

public interface MynaTrainTestCallback {
    void onTrainingProgress(final String msg);
    void onTrainingFinished(final String msg);
    void onTestingResult(RecognizedActivityResult result, final int label);
    void onFinalReport(final String result, final long duration);
}
