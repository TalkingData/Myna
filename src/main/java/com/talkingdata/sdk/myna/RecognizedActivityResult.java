package com.talkingdata.sdk.myna;


public class RecognizedActivityResult implements MynaResultInterface{

    RecognizedActivity[] activities;

    private MynaResult result = null;

    /**
     * Get the most possible activity
     */
    public RecognizedActivity getMostProbableActivity(){
        if(activities == null || activities.length == 0){
            return null;
        }

        RecognizedActivity act = activities[0];
        for (RecognizedActivity activity:
             activities) {
            if(act.getActivityPossibility() < activity.getActivityPossibility()){
                act = activity;
            }
        }
        return act;
    }

    /**
     * Get all the possible activities
     */
    public RecognizedActivity[] getProbableActivities(){
        return activities;
    }

    @Override
    public MynaResult getError() {
        return result;
    }
}
