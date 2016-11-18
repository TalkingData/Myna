package com.talkingdata.sdk.myna;

import android.hardware.Sensor;
import android.os.Handler;
import android.util.SparseIntArray;

import dice.tree.model.CBRRDTModel;
import dice.tree.structure.Node;

public class HumanActivityRecognizer extends MynaRecognizerInterface {

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
        int[] labels = {2, 0, 3};
        result.activities = new RecognizedActivity[labels.length];
        for(int index = 0; index < labels.length; ++index){
            result.activities[index] = new RecognizedActivity(labels[index], confidences[index]);

        }
        if(resultCallback != null){
            resultCallback.onResult(result);
        }
    }
}
