package com.talkingdata.myna.tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.telephony.TelephonyManager;
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
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

public class Utils {

    public static final String TAG = "Myna";
    static final boolean DEBUG = true;


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
            return null;
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

//    public static double get3AxisDistance(final float x, final float y, final float z){
//        return Math.sqrt(x * x + y * y + z * z);
//    }


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
        File file = null;
        if(!dir.exists()){
            if(dir.mkdir()){
                file = new File(dir, fileName);
            }
        }
        if(file == null){
            return;
        }
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
                if(dir.mkdir()){
                    file = new File(dir, fileName);
                }
            }
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

    public static boolean isFileExists(final Context ctx, final String fileName){
        if(ctx == null ||  fileName == null || fileName.isEmpty()){
            return false;
        }
        File file = new File(Environment.getExternalStorageDirectory() + "/rHAR/" + fileName);
        return file.exists();
    }

    public static String load(final Context ctx, final String fileName) {
        if(ctx == null ||  fileName == null || fileName.isEmpty()){
            return null;
        }
        File file = new File(ctx.getFilesDir(), fileName);
        String sdState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(sdState)){
            File dir = new File(Environment.getExternalStorageDirectory() + "/rHAR/");
            if(!dir.exists()){
                if(dir.mkdir()){
                    file = new File(dir, fileName);
                }
            }
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

    public static void logI(final String message){
        if(message != null && !message.isEmpty()){
            Log.i(TAG, message);
        }
    }

    public static String getCurrentWifiSsid(final Context context) {
        String ssid = null;
        try {
            if (Utils.hasPermission(context,
                    android.Manifest.permission.ACCESS_WIFI_STATE)) {
                WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if(wifiManager != null && wifiManager.isWifiEnabled() && isWifiConnected(context)){
                    WifiInfo info = wifiManager.getConnectionInfo();
                    if (info != null && info.getBSSID() != null) {
                        try {
                            ssid = info.getSSID();
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                }
            }
        } catch (Throwable t) {
            Utils.logI(t.getMessage());
        }
        return ssid;
    }

    private static boolean hasPermission(final Context context, final String permission) {
        try {
            return context.checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }catch (Throwable t){
            t.printStackTrace();
        }
        return false;
    }

    static boolean isWifiConnected(final Context context) {
        try {
            if (Utils.hasPermission(context,
                    android.Manifest.permission.ACCESS_NETWORK_STATE) && isNetworkConnected(context)) {
                ConnectivityManager connectivityManager = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                if(connectivityManager != null){
                    NetworkInfo activeNet = connectivityManager.getActiveNetworkInfo();
                    if(activeNet != null){
                        return ConnectivityManager.TYPE_WIFI == activeNet.getType() && activeNet.isConnected();
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return false;
    }

    private static boolean isNetworkConnected(final Context context){
        boolean isConnected = false;
        if (Utils.hasPermission(context, android.Manifest.permission.ACCESS_NETWORK_STATE)) {
            ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if(cm != null){
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            }
        }
        return isConnected;
    }

    public static String getConnectedWifiMacAddress(final Context context) {
        String connectedWifiMacAddress = null;
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        List<ScanResult> wifiList;
        if (wifiManager != null) {
            wifiList = wifiManager.getScanResults();
            WifiInfo info = wifiManager.getConnectionInfo();
            if (wifiList != null && info != null) {
                for (int i = 0; i < wifiList.size(); i++) {
                    ScanResult result = wifiList.get(i);
                    if (info.getBSSID().equals(result.BSSID)) {
                        connectedWifiMacAddress = result.BSSID;
                    }
                }
            }
        }
        return connectedWifiMacAddress;
    }


    public static byte[] zlib(String content){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DeflaterOutputStream out = null;
        Deflater deflater = new Deflater(9,true);
        try {

            out = new DeflaterOutputStream(baos, deflater);
            out.write(content.getBytes("UTF-8"));

        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (out != null)
                try {
                    out.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
        }
        deflater.end();
        return baos.toByteArray();
    }


    @SuppressLint("HardwareIds")
    public static String getIMEI(Context context) {
        try {
            if(Build.VERSION.SDK_INT >= 23){
                if(context.checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE)
                        != PackageManager.PERMISSION_GRANTED){
                    return null;
                }
            }
            TelephonyManager telManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            if (Utils.hasPermission(context,
                    android.Manifest.permission.READ_PHONE_STATE)) {
                if (telManager != null){
                    return telManager.getDeviceId();
                }
            }
        } catch (Throwable t) {
            if (Utils.DEBUG)
                t.printStackTrace();
        }
        return null;
    }


    public static String getMacAddress() {
        String mac = null;
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(Integer.toHexString(b & 0xFF)).append(":");
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                mac = res1.toString();
            }
        } catch (Throwable e) {
            if (Utils.DEBUG)
                e.printStackTrace();
        }
        return mac;
    }
}