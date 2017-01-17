package com.talkingdata.myna;

import java.util.Locale;

public class MynaResult {

    private final int errorCode;
    private final String errorMsg;

    public MynaResult(int eCode, String eMsg){
        errorCode = eCode;
        errorMsg = eMsg;
    }

    public int getErrorCode(){
        return errorCode;
    }

    public String getErrorMsg(){
        return errorMsg;
    }

    @Override
    public String toString(){
        return String.format(Locale.getDefault(), "[Error %d] %s", errorCode, errorMsg);
    }
}
