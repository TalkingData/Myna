package com.talkingdata.myna;

public class HumanActivityRecognizer extends MynaRecognizerAbstractClass {

    /**
     * Recognition algorithm must be provided.
     *
     * @param classifier     Recognition algorithm
     * @param resultCallback Callback to handle the recognition result
     */
    public HumanActivityRecognizer(ClassifierInterface classifier, MynaResultCallback resultCallback) {
        super(classifier, resultCallback);
    }

    @Override
    void onResult(double[] confidences){
        RecognizedActivityResult result = new RecognizedActivityResult();
        int[] labels = {
                RecognizedActivity.WALKING,
                RecognizedActivity.RUNNING,
                RecognizedActivity.BUS,
                RecognizedActivity.SUBWAY,
                RecognizedActivity.CAR};
        result.activities = new RecognizedActivity[labels.length];
        for(int index = 0; index < labels.length; ++index){
            result.activities[index] = new RecognizedActivity(labels[index], confidences[index]);

        }
        if(resultCallback != null){
            //noinspection unchecked
            resultCallback.onResult(result);
        }
    }
}
