package com.hw.diaosiclock.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by hw on 2016/2/16.
 * 暂时用心知天气api
 */
public class HttpUtil {
    private static String result = null;
    private static StringBuffer response = null;
    public synchronized static void sendHttpRequest(final String address, final HttpCallbackListener listener) {
        if(null != address) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    HttpURLConnection connection = null;
                    try {
                        Log.e("sendHttpRequest","in");
                        URL url = new URL(address);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        connection.setReadTimeout(15000);
                        connection.setConnectTimeout(15000);

                        InputStream in = connection.getInputStream();
                        Log.e("sendHttpRequest","InputStream after");
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                        Log.e("sendHttpRequest","reader after");

                        if(null == response) {
                            response = new StringBuffer();
                        }else {
                            response.delete(0, response.length());
                        }

                        String line = null;

                        while((line = reader.readLine()) != null) {
                            response.append(line);
                        }

                        if(null != listener) {
                            listener.onFinish(response.toString());
                        }

                    }catch (Exception e) {
                        Log.e("HttpUtil", "error = "+ response.toString());
                        e.printStackTrace();
                    }finally {
                        if(null != connection) {
                            connection.disconnect();
                        }
                    }
                }
            }).start();
        }
    }

}
