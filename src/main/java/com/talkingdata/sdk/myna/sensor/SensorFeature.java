
package com.talkingdata.sdk.myna.sensor;

import android.util.Log;

import org.json.JSONObject;

public class SensorFeature {
    public float minx = 1000, miny = 1000, minz = 1000;
    public float maxx = -1000, maxy = -1000, maxz = -1000;
    public float avgx = 0, avgy = 0, avgz = 0;
    public float varx = 0, vary = 0, varz = 0;
    public float rangex = 0, rangey = 0, rangez = 0;
    public float jumpx = 0, jumpy = 0, jumpz = 0;
    public float fallx = 0, fally = 0, fallz = 0;
    public float amp1x = 0, amp1y = 0, amp1z = 0;
    public float amp2x = 0, amp2y = 0, amp2z = 0;
    public float amp3x = 0, amp3y = 0, amp3z = 0;
    public int freq1x = 0, freq1y = 0, freq1z = 0;
    public int freq2x = 0, freq2y = 0, freq2z = 0;
    public int freq3x = 0, freq3y = 0, freq3z = 0;

    public SensorFeature() {
    }

    JSONObject getJsonObj() {
        try {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("min", getXYZJsonObj(minx, miny, minz));
            jsonObj.put("max", getXYZJsonObj(maxx, maxy, maxz));
            jsonObj.put("avg", getXYZJsonObj(avgx, avgy, avgz));
            jsonObj.put("var", getXYZJsonObj(varx, vary, varz));
            jsonObj.put("range", getXYZJsonObj(rangex, rangey, rangez));
            jsonObj.put("jump", getXYZJsonObj(jumpx, jumpy, jumpz));
            jsonObj.put("fall", getXYZJsonObj(fallx, fally, fallz));
            jsonObj.put("amp1", getXYZJsonObj(amp1x, amp1y, amp1z));
            jsonObj.put("amp2", getXYZJsonObj(amp2x, amp2y, amp2z));
            jsonObj.put("amp3", getXYZJsonObj(amp3x, amp3y, amp3z));
            jsonObj.put("freq1", getXYZJsonObj(freq1x, freq1y, freq1z));
            jsonObj.put("freq2", getXYZJsonObj(freq2x, freq2y, freq2z));
            jsonObj.put("freq3", getXYZJsonObj(freq3x, freq3y, freq3z));
            return jsonObj;

        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    private JSONObject getXYZJsonObj(final float x, final float y, final float z) {
        try {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("x", (double) x);
            jsonObj.put("y", (double) y);
            jsonObj.put("z", (double) z);
            return jsonObj;

        } catch (Throwable e) {
            Log.i("rHAR", String.valueOf(x) + "\t" + String.valueOf(y) + "\t" + String.valueOf(z));
            e.printStackTrace();
            return null;
        }
    }

    void parseJsonObj(final JSONObject jo) {
        try {
            JSONObject jsonObj = jo.optJSONObject("min");
            parseXYZJsonObj(jsonObj, 1);
            jsonObj = jo.optJSONObject("max");
            parseXYZJsonObj(jsonObj, 2);
            jsonObj = jo.optJSONObject("avg");
            parseXYZJsonObj(jsonObj, 3);
            jsonObj = jo.optJSONObject("var");
            parseXYZJsonObj(jsonObj, 4);
            jsonObj = jo.optJSONObject("range");
            parseXYZJsonObj(jsonObj, 5);
            jsonObj = jo.optJSONObject("jump");
            parseXYZJsonObj(jsonObj, 6);
            jsonObj = jo.optJSONObject("fall");
            parseXYZJsonObj(jsonObj, 7);
            jsonObj = jo.optJSONObject("amp1");
            parseXYZJsonObj(jsonObj, 8);
            jsonObj = jo.optJSONObject("amp2");
            parseXYZJsonObj(jsonObj, 9);
            jsonObj = jo.optJSONObject("amp3");
            parseXYZJsonObj(jsonObj, 10);
            jsonObj = jo.optJSONObject("freq1");
            parseXYZJsonObj(jsonObj, 11);
            jsonObj = jo.optJSONObject("freq2");
            parseXYZJsonObj(jsonObj, 12);
            jsonObj = jo.optJSONObject("freq3");
            parseXYZJsonObj(jsonObj, 13);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void parseXYZJsonObj(final JSONObject jo, final int type) {
        if (jo == null)
            return;
        switch (type) {
            case 1:
                minx = (float) jo.optDouble("x");
                miny = (float) jo.optDouble("y");
                minz = (float) jo.optDouble("z");
                break;
            case 2:
                maxx = (float) jo.optDouble("x");
                maxy = (float) jo.optDouble("y");
                maxz = (float) jo.optDouble("z");
                break;
            case 3:
                avgx = (float) jo.optDouble("x");
                avgy = (float) jo.optDouble("y");
                avgz = (float) jo.optDouble("z");
                break;
            case 4:
                varx = (float) jo.optDouble("x");
                vary = (float) jo.optDouble("y");
                varz = (float) jo.optDouble("z");
                break;
            case 5:
                rangex = (float) jo.optDouble("x");
                rangey = (float) jo.optDouble("y");
                rangez = (float) jo.optDouble("z");
            case 6:
            	jumpx = (float) jo.optDouble("x");
            	jumpy = (float) jo.optDouble("y");
            	jumpz = (float) jo.optDouble("z");
            case 7:
            	fallx = (float) jo.optDouble("x");
            	fally = (float) jo.optDouble("y");
            	fallz = (float) jo.optDouble("z");
            case 8:
                amp1x = (float) jo.optDouble("x");
                amp1y = (float) jo.optDouble("y");
                amp1z = (float) jo.optDouble("z");
                break;
            case 9:
                amp2x = (float) jo.optDouble("x");
                amp2y = (float) jo.optDouble("y");
                amp2z = (float) jo.optDouble("z");
                break;
            case 10:
                amp3x = (float) jo.optDouble("x");
                amp3y = (float) jo.optDouble("y");
                amp3z = (float) jo.optDouble("z");
            case 11:
                freq1x = jo.optInt("x");
                freq1y = jo.optInt("y");
                freq1z = jo.optInt("z");
            case 12:
                freq2x = jo.optInt("x");
                freq2y = jo.optInt("y");
                freq2z = jo.optInt("z");
            case 13:
                freq3x = jo.optInt("x");
                freq3y = jo.optInt("y");
                freq3z = jo.optInt("z");
            default:
                break;
        }
    }

}
