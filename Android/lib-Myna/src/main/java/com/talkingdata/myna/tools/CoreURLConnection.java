
package com.talkingdata.myna.tools;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

class CoreURLConnection {

    private final static int ERROR_CODE = 600;
    private final static int DEFAULT_CONNECT_TIMEOUT = 10 * 1000;
    private final static int DEFAULT_READ_TIMEOUT = 10 * 1000;

    static ResponseData doPost(final String url, final String jsonStr) {
        if(jsonStr == null){
            return new ResponseData(ERROR_CODE, "");
        }
        try {
            byte[] payload = Utils.zlib(jsonStr);
            HttpURLConnection conn = getURLConn(new URL(url), payload.length);
            if(conn == null){
                return new ResponseData(ERROR_CODE, "");
            }
            return urlConnectionPost(conn, payload);
        } catch (Throwable e) {
            if (Utils.DEBUG) {
                e.printStackTrace();
            }
        }
        return new ResponseData(ERROR_CODE, "");
    }

    /**
     *
     * @param url url address
     * @param length Length of the payload
     * @return URLConnection object
     */
    private static HttpURLConnection getURLConn(final URL url, final int length){
        HttpURLConnection conn;
        try{
            conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/octet-stream");
            conn.setRequestProperty("Content-Length", String.valueOf(length));

            conn.setDoOutput(true);
            conn.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT);
            conn.setReadTimeout(DEFAULT_READ_TIMEOUT);
        }catch(Throwable e){
            if(Utils.DEBUG){
                e.printStackTrace();
            }
            return null;
        }
        return conn;
    }

    private static ResponseData urlConnectionPost(HttpURLConnection urlConn, byte[] payload) {
        if(payload == null || payload.length == 0 || urlConn == null){
            return new ResponseData(ERROR_CODE, "");
        }
        OutputStream os = null;
        BufferedReader rd = null;
        StringBuilder response = new StringBuilder();
        int statusCode = ERROR_CODE;
        try {
            os = urlConn.getOutputStream();
            os.write(payload);
            os.close();

            // Get Response
            statusCode = urlConn.getResponseCode();
            InputStream is;
            if(statusCode >= HttpURLConnection.HTTP_BAD_REQUEST){
                is = urlConn.getErrorStream();
            }else{
                is = urlConn.getInputStream();
            }
            rd = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\n');
            }
        } catch (Throwable e) {
            if (Utils.DEBUG) {
                e.printStackTrace();
            }
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (Throwable e) {
                if (Utils.DEBUG) {
                    e.printStackTrace();
                }
            }
            try {
                if (rd != null) {
                    rd.close();
                }
            } catch (Throwable e) {
                if (Utils.DEBUG) {
                    e.printStackTrace();
                }
            }
        }
        return new ResponseData(statusCode, response.toString());
    }

    static class ResponseData {
        int statusCode;
        String responseMsg;

        ResponseData(int sCode, String respMsg) {
            statusCode = sCode;
            responseMsg = respMsg;
        }

        int getStatusCode() {
            return statusCode;
        }
    }
}
