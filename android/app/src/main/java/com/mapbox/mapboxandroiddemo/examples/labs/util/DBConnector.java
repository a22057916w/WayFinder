package com.mapbox.mapboxandroiddemo.examples.labs.util;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class DBConnector {
    /**
     * This method is deviced for NON_POSTER navigation, which is highly unpractical  and implement
     * because it is hard to identify a suitable hallway to place the anchor
     */
    public static String path_test_v2Query(String floor, String id, String room) {
        String result = "";
        try {
            URL url = new URL("http://140.136.150.100/test/path_test_v2.php");

            //連線到 url網址
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost method = new HttpPost(String.valueOf(url));

            //傳值給PHP
            List<NameValuePair> vars = new ArrayList<NameValuePair>();
            vars.add(new BasicNameValuePair("floor", floor));
            vars.add(new BasicNameValuePair("number", id));
            vars.add(new BasicNameValuePair("room2", room));
            method.setEntity(new UrlEncodedFormEntity(vars, HTTP.UTF_8));

            //接收PHP回傳的資料
            HttpResponse response = httpclient.execute(method);
            result = EntityUtils.toString(response.getEntity(), "UTF-8");

        } catch (Exception e) {
            Log.e("DBConnector_pathSearch", e.toString());
            result = e.toString();
        }
        return result;
    }


    static String posterQuery(String floor, String id) {
        String result = "";
        try {
            URL url = new URL("http://140.136.150.100/test/poster.php");

            //連線到 url網址
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost method = new HttpPost(String.valueOf(url));

            //傳值給PHP
            List<NameValuePair> vars = new ArrayList<NameValuePair>();
            vars.add(new BasicNameValuePair("floor", floor));
            vars.add(new BasicNameValuePair("number", id));
            method.setEntity(new UrlEncodedFormEntity(vars, HTTP.UTF_8));

            //接收PHP回傳的資料
            HttpResponse response = httpclient.execute(method);
            result = EntityUtils.toString(response.getEntity(), "UTF-8");

        } catch (Exception e) {
            Log.e("DBConnector_posterSearch", e.toString());
            result = e.toString();
        }
        return result;
    }

    static String pathQuery(String floor, String id, String room) {
        String result = "";
        try {
            URL url = new URL("http://140.136.150.100/test/path_v5.php");

            //連線到 url網址
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost method = new HttpPost(String.valueOf(url));

            //傳值給PHP
            List<NameValuePair> vars = new ArrayList<NameValuePair>();
            vars.add(new BasicNameValuePair("floor", floor));
            vars.add(new BasicNameValuePair("number", id));
            vars.add(new BasicNameValuePair("room2", room));
            method.setEntity(new UrlEncodedFormEntity(vars, HTTP.UTF_8));

            //接收PHP回傳的資料
            HttpResponse response = httpclient.execute(method);
            result = EntityUtils.toString(response.getEntity(), "UTF-8");

        } catch (Exception e) {
            Log.e("DBConnector_posterSearch", e.toString());
            result = e.toString();
        }
        return result;
    }

    static String destQuery(String room) {
        String result = "";
        try {
            URL url = new URL("http://140.136.150.100/test/dest.php");

            //連線到 url網址
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost method = new HttpPost(String.valueOf(url));

            //傳值給PHP
            List<NameValuePair> vars = new ArrayList<NameValuePair>();
            vars.add(new BasicNameValuePair("room2", room));
            method.setEntity(new UrlEncodedFormEntity(vars, HTTP.UTF_8));

            //接收PHP回傳的資料
            HttpResponse response = httpclient.execute(method);
            result = EntityUtils.toString(response.getEntity(), "UTF-8");

        } catch (Exception e) {
            Log.e("DBConnector_posterSearch", e.toString());
            result = e.toString();
        }
        return result;
    }

    static String destQuery_test(String floor, String id, String room) {
        String result = "";
        try {
            URL url = new URL("http://140.136.150.100/test/dest_test.php");

            //連線到 url網址
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost method = new HttpPost(String.valueOf(url));

            //傳值給PHP
            List<NameValuePair> vars = new ArrayList<NameValuePair>();
            vars.add(new BasicNameValuePair("floor", floor));
            vars.add(new BasicNameValuePair("number", id));
            vars.add(new BasicNameValuePair("room2", room));
            method.setEntity(new UrlEncodedFormEntity(vars, HTTP.UTF_8));

            //接收PHP回傳的資料
            HttpResponse response = httpclient.execute(method);
            result = EntityUtils.toString(response.getEntity(), "UTF-8");

        } catch (Exception e) {
            Log.e("DBConnector_posterSearch", e.toString());
            result = e.toString();
        }
        return result;
    }
}
