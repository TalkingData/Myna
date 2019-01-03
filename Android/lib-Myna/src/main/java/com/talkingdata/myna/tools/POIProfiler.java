package com.talkingdata.myna.tools;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.app.ActivityCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Xiaohui Wang(xiaohui96@gmail.com) on 2018/7/5.
 */
public class POIProfiler {
    private Context ctx;
    private LocationListener locationListener;
    private LocationManager locationManager;
    private Handler wifiHandler;
    private final int HOME = 1;
    private final int OFFICE = 2;

    public POIProfiler(final Context context) {
        this.ctx = context;
        HandlerThread wifiHT = new HandlerThread("wifiHT");
        wifiHT.start();
        wifiHandler = new Handler(wifiHT.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                handleWiFiData();
                wifiHandler.sendEmptyMessageDelayed(0, 30 * 60 * 1000);
            }
        };
        wifiHandler.sendEmptyMessage(0);
        initializeLocationManager();
    }

    private void initializeLocationManager() {
        Utils.logI("initializeLocationManager");
        this.locationListener = new LocationListener();
        if (locationManager == null) {
            locationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
        }
        requestLocationUpdate(LocationManager.GPS_PROVIDER);
        requestLocationUpdate(LocationManager.NETWORK_PROVIDER);
        Utils.logI("Registered location provides.");
    }

    public void stopAll() {
        wifiHandler.removeCallbacksAndMessages(null);
        locationManager.removeUpdates(locationListener);
    }

    private void requestLocationUpdate(final String provide) {
        final int LOCATION_INTERVAL = 10 * 60 * 1000;
        final float LOCATION_DISTANCE = 0;

        try {
            if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(
                        provide, LOCATION_INTERVAL, LOCATION_DISTANCE, locationListener);
            }
        } catch (Throwable t) {
            Utils.logI(t.getMessage());
        }
    }


    private class LocationListener implements android.location.LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            Utils.logI("onLocationChanged: " + location);
            try {
                final String currentLocationStr = String.format(Locale.getDefault(), "%s: %.4f, %.4f, %s",
                        location.getProvider(), location.getLatitude(), location.getLongitude(), location.toString());
                Utils.logI(currentLocationStr);
                String connectedSSID = Utils.getCurrentWifiSsid(ctx);
                String connectedMacAddress = Utils.getConnectedWifiMacAddress(ctx);
                String content = String.valueOf(System.currentTimeMillis())
                        + "," + String.valueOf(location.getLatitude())
                        + "," + String.valueOf(location.getLongitude())
                        + "," + String.valueOf(location.getAltitude())
                        + "," + (connectedSSID == null ? "" : connectedSSID)
                        + "," + (connectedMacAddress == null ? "" : connectedMacAddress);
                Utils.saveAsAppend(ctx, content + "\n", "locations");
            } catch (Throwable t) {
                Utils.logI(t.getMessage());
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Utils.logI("onStatusChanged: " + provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Utils.logI("onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Utils.logI("onProviderEnabled: " + provider);
        }
    }

    private void handleWiFiData(){
        if(Utils.isWifiConnected(ctx)){
            String connectedSSID = Utils.getCurrentWifiSsid(ctx);
            String connectedMacAddress = Utils.getConnectedWifiMacAddress(ctx);
            Utils.logI(connectedSSID + "; " + connectedMacAddress);
            saveWifiData(connectedSSID, connectedMacAddress);
            inferencePOI();
        }
    }

    private void saveWifiData(final String ssid, final String bssid){
        try{
            long now = System.currentTimeMillis();
            String wifiData = Utils.load(ctx, "wifiData");
            JSONArray wifiArray;
            if(wifiData == null || wifiData.isEmpty()){
                wifiArray = new JSONArray();
                wifiArray.put(getNewSSIDObject(now, ssid, bssid));
            }else{
                JSONObject jo = new JSONObject(wifiData);
                wifiArray = jo.getJSONArray("wifiArray");
                for(int i = 0; i < wifiArray.length(); ++i){
                    JSONObject ssidObject = wifiArray.getJSONObject(i);
                    if(ssid.equals(ssidObject.get("ssid"))){
                        JSONArray bssidArray = ssidObject.getJSONArray("bssids");
                        boolean exist = false;
                        for(int j = 0; j < bssidArray.length(); ++j){
                            JSONObject bssidObject = bssidArray.getJSONObject(j);
                            if(bssid.equals(bssidObject.get("bssid"))){
                                bssidObject.put("count", bssidObject.getInt("count") + 1);
                                String newTimes = bssidObject.getString("times") + "," + String.valueOf(now);
                                bssidObject.put("times", newTimes);
                                exist = true;
                                break;
                            }
                        }
                        if(!exist){
                            bssidArray.put(getNewBSSIDObject(now, bssid));
                        }
                    }else{
                        wifiArray.put(getNewSSIDObject(now, ssid, bssid));
                    }
                }

            }
            JSONObject deviceObject = getDeviceInfo(ctx);
            deviceObject.put("wifiArray", wifiArray);
            String data = deviceObject.toString();
            Utils.logI(data);
            if(sendData2Server(data) == 200){
                Utils.logI("Successfully submitted data.");
            }else{
                Utils.logI("Failed to submit to data.");
            }
            Utils.save(ctx, data, "wifiData", -1);
        }catch (Throwable t){
            t.printStackTrace();
        }
    }

    private JSONObject getNewSSIDObject(final long now, final String ssid, final String bssid){
        try{
            JSONArray bssidArray = new JSONArray();
            bssidArray.put(getNewBSSIDObject(now, bssid));
            JSONObject ssidObject = new JSONObject();
            ssidObject.put("ssid", ssid);
            ssidObject.put("bssids", bssidArray);
            return ssidObject;
        }catch (Throwable t){
            t.printStackTrace();
            return null;
        }
    }

    private JSONObject getNewBSSIDObject(final long now, final String bssid){
        try{
            JSONObject bssidObject = new JSONObject();
            bssidObject.put("times", String.valueOf(now));
            bssidObject.put("bssid", bssid);
            bssidObject.put("count", 1);
            return bssidObject;
        }catch (Throwable t){
            t.printStackTrace();
            return null;
        }
    }

    private int sendData2Server(final String content) {
        final String URL = "http://54.223.226.16/poidata/";

        try {
            if (Utils.isWifiConnected(ctx)) {
                CoreURLConnection.ResponseData responseData = CoreURLConnection
                        .doPost(URL, content);
                return responseData.getStatusCode();
            }
            return 200;
        } catch (Throwable t) {
            t.printStackTrace();
            return 600;
        }
    }

    private void inferencePOI(){
        boolean isInferencing = isTimeToInferencePOI();
        Utils.logI("Is it time to inference POI: " + String.valueOf(isInferencing));
        if(isInferencing){
            String wifiData = Utils.load(ctx, "wifiData");
            if(wifiData != null && !wifiData.isEmpty()){
                try{
                    JSONObject allInfo = new JSONObject(wifiData);
                    JSONArray wifiArrayData = allInfo.getJSONArray("wifiArray");
                    JSONObject poiObject = new JSONObject();
                    poiObject.put("home", inferenceHome(wifiArrayData));
                    poiObject.put("office", inferenceOffice(wifiArrayData));
                    Utils.logI(poiObject.toString());
                    Utils.save(ctx, poiObject.toString(), "poiData", -1);

                    Utils.deleteFile("wifiData");
                    Utils.deleteFile("locations");
                }catch (Throwable t){
                    t.printStackTrace();
                }
            }
            Utils.save(ctx, String.valueOf(System.currentTimeMillis()), "poicheckpoint", -1);
        }
    }

    private boolean isTimeToInferencePOI(){
        if(Utils.isFileExists(ctx, "poicheckpoint")){
            String lastCheckoutTime = Utils.load(ctx, "poicheckpoint");
            if(lastCheckoutTime == null || lastCheckoutTime.isEmpty()){
                return false;
            }else{
                long timeAsLong = Long.parseLong(lastCheckoutTime.trim());
                return System.currentTimeMillis() - timeAsLong >= 1000 * 60 * 60 * 24 * 7;
            }
        }else{
            Utils.save(ctx, String.valueOf(System.currentTimeMillis()), "poicheckpoint", -1);
            return false;
        }
    }

    private boolean isSleepingTime(final long time){
        boolean isSleeping = false;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if(hour >= 0 && hour <=5){
            isSleeping = true;
        }
        return isSleeping;
    }

    private boolean isWorkingTime(final long time){
        boolean isWorking = false;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        if(day >= 2 && day <= 6){
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            if((hour >=9 && hour <= 12) || (hour >=14 && hour <18)){
                isWorking = true;
            }
        }
        return isWorking;
    }

    private JSONObject inferenceHome(final JSONArray wifiArray){
        try{
            String ssid = "";
            String bssid = "";
            int maxCountForBSSID = 0;
            for(int i = 0; i < wifiArray.length(); ++i) {
                JSONObject ssidObject = wifiArray.getJSONObject(i);
                JSONArray bssidArray = ssidObject.getJSONArray("bssids");
                for(int j = 0; j < bssidArray.length(); ++j){
                    JSONObject bssidObject = bssidArray.getJSONObject(j);
                    int count = getCount(bssidObject.getString("times"), HOME);
                    if(count > maxCountForBSSID){
                        bssid = bssidObject.getString("bssid");
                        maxCountForBSSID = count;
                        ssid = ssidObject.getString("ssid");
                    }
                }
            }
            JSONObject homeObject = new JSONObject();
            if(!ssid.isEmpty() && !bssid.isEmpty()){
                homeObject.put("ssid", ssid);
                homeObject.put("bssid", bssid);
                homeObject.put("homeCount", maxCountForBSSID);
                homeObject.put("location", getLocation(ssid));
                return homeObject;
            }
        }catch (Throwable t){
            t.printStackTrace();
        }
        return null;
    }


    private JSONObject inferenceOffice(final JSONArray wifiArray){
        try{
            String ssid = "";
            int maxCountForSSID = 0;
            for(int i = 0; i < wifiArray.length(); ++i) {
                JSONObject ssidObject = wifiArray.getJSONObject(i);
                JSONArray bssidArray = ssidObject.getJSONArray("bssids");
                int currentCountForSSID = 0;
                for(int j = 0; j < bssidArray.length(); ++j){
                    JSONObject bssidObject = bssidArray.getJSONObject(j);
                    int count = getCount(bssidObject.getString("times"), OFFICE);
                    currentCountForSSID += count;
                }
                if(currentCountForSSID > maxCountForSSID){
                    maxCountForSSID = currentCountForSSID;
                    ssid = ssidObject.getString("ssid");
                }
            }
            JSONObject officeObject = new JSONObject();
            if(!ssid.isEmpty()){
                officeObject.put("ssid", ssid);
                officeObject.put("officeCount", maxCountForSSID);
                officeObject.put("location", getLocation(ssid));
                return officeObject;
            }
        }catch (Throwable t){
            t.printStackTrace();
        }
        return null;
    }

    private int getCount(final String times, final int mode){
        int count = 0;
        String[] timeStrs = times.split(",");
        for (String timeStr : timeStrs) {
            if ((mode == HOME && isSleepingTime(Long.parseLong(timeStr.trim())))
                    || (mode == OFFICE && isWorkingTime(Long.parseLong(timeStr.trim())))) {
                count += 1;
            }
        }
        return count;
    }

    private String getLocation(final String ssid){
        String location = "";
        String locations = Utils.load(ctx, "locations");
        if(locations != null && !locations.isEmpty()){
            String[] locationLines = locations.split("\n");
            ArrayList<Float> latitudes = new ArrayList<>();
            ArrayList<Float> longitudes = new ArrayList<>();
            for(String line : locationLines){
                String[] lineDetails = line.split(",");
                if(lineDetails.length == 6 && lineDetails[4].equals(ssid)){
                    latitudes.add(Float.parseFloat(lineDetails[1]));
                    longitudes.add(Float.parseFloat(lineDetails[2]));
                }
            }
            float latitude = getAverageFloat(latitudes);
            float longitude = getAverageFloat(longitudes);
            location = String.valueOf(latitude) + "," + String.valueOf(longitude);
        }
        return location;
    }

    private float getAverageFloat(ArrayList<Float> values){
        float result = 0.0f;
        for(Float v : values){
            result += v;
        }
        return result / values.size();
    }


    private static JSONObject getDeviceInfo(final Context ctx) {
        JSONObject jo = new JSONObject();
        try {
            jo.put("version", String.valueOf(Build.VERSION.RELEASE).trim());
            jo.put("MANUFACTURER", String.valueOf(Build.MANUFACTURER).trim());
            jo.put("BRAND", String.valueOf(Build.BRAND).trim());
            jo.put("MODEL", String.valueOf(Build.MODEL).trim());
            jo.put("IMEI", String.valueOf(Utils.getIMEI(ctx)).trim());
            jo.put("MacAddress", String.valueOf(Utils.getMacAddress()).trim());
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return jo;
    }
}
