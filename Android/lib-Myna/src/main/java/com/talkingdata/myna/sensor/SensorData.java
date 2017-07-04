package com.talkingdata.myna.sensor;

import org.json.JSONObject;


public class SensorData {
    /**
     * Sensor data
     */
    public float[] accelerate, gyroscope, gravity, magnetic, game_rotation_vector, orientation;


    /**
     * Calculated accelerometer values in real world coordination system.
     */
    public float[] world_accelerometer;

    public float light, pressure, temperature;

    /**
     * Time when recording the data of these sensors
     */
    public long timestamp;

    public SensorData() {
        accelerate = new float[3];
        gyroscope = new float[3];
        gravity = new float[3];
        magnetic = new float[3];
        game_rotation_vector = new float[3];
        orientation = new float[3];
        world_accelerometer = new float[3];
        timestamp = System.currentTimeMillis();
    }

    /**
     * Reset the value of all fields with the given instance
     * @param sd Source instance with the sensor data waiting to be cloned.
     */
    public void clone(SensorData sd) {
        System.arraycopy(sd.accelerate, 0, this.accelerate, 0, 3);
        System.arraycopy(sd.gyroscope, 0, this.gyroscope, 0, 3);
        System.arraycopy(sd.gravity, 0, this.gravity, 0, 3);
        System.arraycopy(sd.magnetic, 0, this.magnetic, 0, 3);
        System.arraycopy(sd.game_rotation_vector, 0, this.game_rotation_vector, 0, 3);
        System.arraycopy(sd.orientation, 0, this.orientation, 0, 3);
        System.arraycopy(sd.world_accelerometer, 0, this.world_accelerometer, 0, 3);

        this.light = sd.light;
        this.temperature = sd.temperature;
        this.pressure = sd.pressure;


        // Update the timestamp
        this.timestamp = sd.timestamp;
    }

    @Override
    public String toString(){
        return toCSVString();
    }

    private String getValues(float[] array) {
        return String.valueOf(array[0]) + ","
                + String.valueOf(array[1]) + ","
                + String.valueOf(array[2]);
    }

    /**
     * Get all sensor data in JSON format
     * @return JSONObject object of all sensor data in JSON format
     */
    JSONObject toJsonObj() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("accelerate", getXYZJsonObj(accelerate[0], accelerate[1], accelerate[2]));
            obj.put("gyroscope", getXYZJsonObj(gyroscope[0], gyroscope[1], gyroscope[2]));
            obj.put("gravity", getXYZJsonObj(gravity[0], gravity[1], gravity[2]));
            obj.put("magnetic", getXYZJsonObj(magnetic[0], magnetic[1], magnetic[2]));
            obj.put("game_rotation_vector", getXYZJsonObj(game_rotation_vector[0], game_rotation_vector[1], game_rotation_vector[2]));
            obj.put("orientation", getXYZJsonObj(orientation[0], orientation[1], orientation[2]));
            obj.put("world_accelerometer", getXYZJsonObj(world_accelerometer[0], world_accelerometer[1], world_accelerometer[2]));
            obj.put("light", light);
            obj.put("pressure", pressure);
            obj.put("temperature", temperature);
            obj.put("timestamp", timestamp);
            return obj;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get all sensor data in CSV format
     * @return String object of all sensor data in CSV format
     */
    private String toCSVString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getValues(accelerate));
        sb.append(",");
        sb.append(getValues(gyroscope));
        sb.append(",");
        sb.append(getValues(gravity));
        sb.append(",");
        sb.append(getValues(magnetic));
        sb.append(",");
        sb.append(getValues(game_rotation_vector));
        sb.append(",");
        sb.append(getValues(orientation));
        sb.append(",");
        sb.append(getValues(world_accelerometer));
        sb.append(",");
        sb.append(String.valueOf(light));
        sb.append(",");
        sb.append(String.valueOf(pressure));
        sb.append(",");
        sb.append(String.valueOf(temperature));
        sb.append(",");
        sb.append(String.valueOf(timestamp));
        return sb.toString();
    }

    private JSONObject getXYZJsonObj(final float x, final float y, final float z) {
        try {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("x", x);
            jsonObj.put("y", y);
            jsonObj.put("z", z);
            return jsonObj;

        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }
}
