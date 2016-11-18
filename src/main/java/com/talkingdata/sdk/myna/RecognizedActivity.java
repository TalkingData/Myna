package com.talkingdata.sdk.myna;

import java.util.Locale;

public class RecognizedActivity {


    /**
     * In_Vehicle
     */
    private final static int In_Vehicle = 0;

    /**
     * Biking
     */
    private final static int ON_BICYCLE = 1;

    /**
     * On_Foot
     */
    private final static int ON_FOOT = 2;

    /**
     * Still
     */
    private final static int STILL = 3;

    /**
     * Unknown
     */
    private final static int UNKNOWN = 4;

    /**
     * Tilting
     */
    private final static int TILTING = 5;

    /**
     * Walking
     */
    private final static int WALKING = 7;

    /**
     * Running
     */
    private final static int RUNNING = 8;

    /**
     * Real time human activity type
     */
    private int activityType;

    /**
     * Possibility of the activity.
     */
    private double possibility;

    public RecognizedActivity(){
        this(UNKNOWN, 4);
    }

    public RecognizedActivity(final int activityType, final double activityPossibility){
        this.activityType = activityType;
        this.possibility = activityPossibility;
    }

    public int getActivityType(){
        return this.activityType;
    }

    public double getActivityPossibility(){
        return this.possibility;
    }

    @Override
    public String toString(){
        return String.format(Locale.CHINESE, "%s : %s", getActivityNameByType(activityType), String.valueOf(possibility));
    }

    private String getActivityNameByType(int type){

        String result = "UNKNOWN";
        switch (type){
            case 0:
                result = "IN_VEHICLE";
                break;
            case 1:
                result = "ON_BICYCLE";
                break;
            case 2:
                result = "ON_FOOT";
                break;
            case 3:
                result = "STILL";
                break;
            case 5:
                result = "TILTING";
                break;
            case 7:
                result = "WALKING";
                break;
            case 8:
                result = "RUNNING";
                break;
            default:
                break;
        }
        return result;
    }
}
