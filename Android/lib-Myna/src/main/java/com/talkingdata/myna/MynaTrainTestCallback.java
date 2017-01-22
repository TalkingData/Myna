package com.talkingdata.myna;

public interface MynaTrainTestCallback {
    void onTrainingProgress(final String msg);
    void onTrainingFinished(final String msg);
    void onResult(final int[] detectedActivity, final double[] probability);
    void onTestingResult(final int[] detectedActivity, final double[] probRDT, final long durationRDT,
                         final String fileName, final int label);
    void onFinalReport(final String RDTSettings);
}
