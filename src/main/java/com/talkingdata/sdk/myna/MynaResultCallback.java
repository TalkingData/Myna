package com.talkingdata.sdk.myna;

import android.support.annotation.NonNull;

public interface MynaResultCallback<R extends MynaResultInterface> {
    void onResult(@NonNull R var1);
}
