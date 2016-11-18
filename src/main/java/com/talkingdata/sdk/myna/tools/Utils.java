package com.talkingdata.sdk.myna.tools;

import android.content.Context;
import android.hardware.SensorManager;
import android.util.Base64;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.talkingdata.sdk.myna.sensor.SensorData;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Utils {

    public static final String TAG = "Myna";


    public static void calculateWorldAcce(SensorData sd){
        float[] Rotate = new float[16];
        float[] I = new float[16];
        float[] currOrientation = new float[3];
        SensorManager.getRotationMatrix(Rotate, I, sd.gravity, sd.magnetic);
        SensorManager.getOrientation(Rotate, currOrientation);
        System.arraycopy(currOrientation, 0, sd.orientation, 0, 3);

        float[] relativeAcc = new float[4];
        float[] earthAcc = new float[4];
        float[] inv = new float[16];
        System.arraycopy(sd.accelerate, 0, relativeAcc, 0, 3);
        relativeAcc[3] = 0;
        android.opengl.Matrix.invertM(inv, 0, Rotate, 0);
        android.opengl.Matrix.multiplyMV(earthAcc, 0, inv, 0, relativeAcc, 0);
        System.arraycopy(earthAcc, 0, sd.world_accelerometer, 0, 3);
    }

    public static String loadFeaturesFromAssets(Context ctx, String fileName){
        String content = null;
        if(ctx == null ||  fileName == null || fileName.isEmpty()){
            return content;
        }
        try {
            InputStream file = ctx.getAssets().open(fileName);
            byte[] formArray = new byte[file.available()];
            file.read(formArray);
            file.close();
            content = new String(formArray);
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return content;
    }

    /** Read the object from Base64 string. */
    public static Object fromString( String s ) throws IOException,
            ClassNotFoundException {
        byte [] data = Base64.decode(s, Base64.DEFAULT);
        ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(  data ) );
        Object o  = ois.readObject();
        ois.close();
        return o;
    }

    /** Write the object to a Base64 string. */
    public static String toString( Serializable o ) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject( o );
        oos.close();
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }

    public static double get3AxisDistance(final float x, final float y, final float z){
        return Math.sqrt(x * x + y * y + z * z);
    }


    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    public static boolean isGooglePlayServiceSupported(Context ctx) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(ctx);
        return resultCode == ConnectionResult.SUCCESS;
    }
}