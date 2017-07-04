package com.talkingdata.myna;

import java.util.Locale;

public class RecognizedActivity {


    /**
     * In_Vehicle
     */
    public final static int In_Vehicle = 0;

    /**
     * Biking
     */
    public final static int ON_BICYCLE = 1;

    /**
     * On_Foot
     */
    public final static int ON_FOOT = 2;

    /**
     * Still
     */
    public final static int STILL = 3;

    /**
     * Unknown
     */
    public final static int UNKNOWN = 4;

    /**
     * Tilting
     */
    public final static int TILTING = 5;

    /**
     * Walking
     */
    public final static int WALKING = 7;

    /**
     * Running
     */
    public final static int RUNNING = 8;

    /**
     * By bus
     */
    public final static int BUS = 101;

    /**
     * By Subway
     */
    public final static int SUBWAY = 102;

    /**
     * By car
     */
    public final static int CAR = 103;

    public final static int HAND_HOLDING = 201;
    public final static int NOT_HAND_HOLDING = 202;

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
        return String.format(Locale.CHINESE, "%s : %s", RecognizedActivity.getActivityNameByType(activityType), String.valueOf(possibility));
    }

    public static String getActivityName(final int actType){
        return getActivityNameByType(actType);
    }

    private static String getActivityNameByType(final int type){

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
            case 101:
                result = "BUS";
                break;
            case 102:
                result = "SUBWAY";
                break;
            case 103:
                result = "CAR";
                break;
            case 201:
                result = "HAND_HOLDING";
                break;
            case 202:
                result = "NOT_HAND_HOLDING";
                break;
            default:
                break;
        }
        return result;
    }
}
