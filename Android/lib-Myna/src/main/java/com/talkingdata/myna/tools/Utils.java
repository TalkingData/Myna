package com.talkingdata.myna.tools;

import android.content.Context;
import android.hardware.SensorManager;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.talkingdata.myna.sensor.SensorData;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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
        if((int)(sd.game_rotation_vector[0]) == 0
                && (int)(sd.game_rotation_vector[1]) == 0
                && (int)(sd.game_rotation_vector[2]) == 0){
            SensorManager.getRotationMatrix(Rotate, I, sd.accelerate, sd.magnetic);
        }else{
            SensorManager.getRotationMatrixFromVector(Rotate, sd.game_rotation_vector);
        }
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

    public static void deleteFile(final String fileName){
        File file = new File(Environment.getExternalStorageDirectory(), "/rHAR/" + fileName);
        if(file.exists())
        {
            Log.i(TAG, String.valueOf(file.delete()));
        }
    }

    public static SparseArray<File> getDataFiles(final String folderSubPath){
        if(folderSubPath == null || folderSubPath.trim().isEmpty()){
            return null;
        }
        SparseArray<File> files = new SparseArray<>();
        File file = new File(Environment.getExternalStorageDirectory() + folderSubPath);
        if(!file.exists() || !file.isDirectory())
            return null;

        File[] tempFiles = file.listFiles();
        if(tempFiles == null)
            return null;
        int count = 0;
        for(File tempFile : tempFiles){
            if(!tempFile.isDirectory()){
                files.append(count++, tempFile);
            }
        }
        return files;
    }

    public static void saveAsAppend(final Context ctx, final String content, final String fileName) {
        if(ctx == null || content.isEmpty() || fileName == null || fileName.isEmpty()){
            return;
        }
        File dir = new File(Environment.getExternalStorageDirectory() + "/rHAR/");
        if(!dir.exists()){
            dir.mkdir();
        }
        File file = new File(dir, fileName);
        Log.i(TAG, "Append saving, saved path: " + file.getAbsolutePath());
        try {
            FileWriter fWriter = new FileWriter(file, true);
            BufferedWriter writer = new BufferedWriter(fWriter);
            writer.write(content);
            writer.close();
            fWriter.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static String save(final Context ctx, final String content, final String fileName, final int label) {
        if(ctx == null || content.isEmpty() || fileName == null || fileName.isEmpty()){
            return null;
        }

        File file = new File(ctx.getFilesDir(), fileName);
        String sdState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(sdState)){
            File dir = new File(Environment.getExternalStorageDirectory() + "/rHAR/");
            if(label > 0){
                dir = new File(Environment.getExternalStorageDirectory() + "/rHAR/data/" + String.valueOf(label));
            }
            if(!dir.exists()){
                dir.mkdir();
            }
            file = new File(dir, fileName);
        }
        String savedPath = file.getAbsolutePath();
        Log.i(TAG, "Save path: " + savedPath);
        if (file.exists())
            Log.i(TAG, "Existing file deleting result: "+ String.valueOf(file.delete()));
        try {
            FileWriter fWriter = new FileWriter(file);
            BufferedWriter writer = new BufferedWriter(fWriter);
            writer.write(content);
            writer.close();
            fWriter.close();
            return savedPath;
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isFileExists(Context ctx, String fileName){
        if(ctx == null ||  fileName == null || fileName.isEmpty()){
            return false;
        }
        File file = new File(Environment.getExternalStorageDirectory() + "/rHAR/" + fileName);
        return file.exists();
    }

    public static String load(Context ctx, String fileName) {
        if(ctx == null ||  fileName == null || fileName.isEmpty()){
            return null;
        }
        File file = new File(ctx.getFilesDir(), fileName);
        String sdState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(sdState)){
            File dir = new File(Environment.getExternalStorageDirectory() + "/rHAR/");
            if(!dir.exists()){
                dir.mkdir();
            }
            file = new File(dir, fileName);
        }
        if (file.exists()) {
            try {
                StringBuilder text = new StringBuilder();
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                while ((line = br.readLine()) != null) {
                    text.append(line);
                    text.append('\n');
                }
                br.close();
                return text.toString();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}