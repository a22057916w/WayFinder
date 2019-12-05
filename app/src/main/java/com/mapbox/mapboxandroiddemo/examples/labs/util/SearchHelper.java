package com.mapbox.mapboxandroiddemo.examples.labs.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.google.ar.core.AugmentedImage;
import com.mapbox.mapboxandroiddemo.examples.labs.data.Destination;
import com.mapbox.mapboxandroiddemo.examples.labs.data.Poster;
import com.mapbox.mapboxandroiddemo.examples.labs.data.Vertex;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

public final class SearchHelper {

    public static final String POSTER_QUERY = "POSTER_QUERY";
    public static final String PATH_QUERY = "PATH_QUERY";


    /**
     * This method will be used in the practice
     * for querying the info of poster to set starting point
     */
    public static String posterQuery(AugmentedImage augmentedImage, String targetRoom) {
        // check if the image's name is legal
        if(parseImage(augmentedImage).equals(ErrorManager.IMAGE_NAME_EROR))
            return ErrorManager.IMAGE_NAME_EROR;

        String floor = parseImage(augmentedImage).substring(0, 1);
        String ID = parseImage(augmentedImage).substring(1, 2);

        if(checkActiveInternetConnection()) {
            if (checkTargetName(targetRoom))
                return DBConnector.posterQuery(floor, ID);
            else
                return ErrorManager.ROOM_NAME_ERROR;
        }
        else
            return ErrorManager.INTERNET_CONNECTION_ERROR;
    }

    /**
     * This method is used for developing test
     * for query the info of the poster to set starting point
     */
    public static String posterQuery(String floor, String number, String targetRoom) {
        if(checkActiveInternetConnection()) {
            if (checkTargetName(targetRoom)) {
                return DBConnector.posterQuery(floor, number);
               /* new SearchHelper.SearchTask().execute(POSTER_QUERY, floor, number, targetRoom);
                return ;*/
            }
            else
                return ErrorManager.ROOM_NAME_ERROR;
        }
        else
            return ErrorManager.INTERNET_CONNECTION_ERROR;
    }

    /**
     * This method is deviced for NON_POSTER navigation, which is highly unpractical  and implement
     * because it is hard to identify a suitable hallway to place the anchor
     */
    public static String path_test_v2Query(AugmentedImage augmentedImage, String targetRoom) {
        // check if the image's name is legal
        if(parseImage(augmentedImage).equals(ErrorManager.IMAGE_NAME_EROR))
            return ErrorManager.IMAGE_NAME_EROR;

        String floor = parseImage(augmentedImage).substring(0, 1);
        String ID = parseImage(augmentedImage).substring(1, 2);

        if(checkActiveInternetConnection()) {
            if (checkTargetName(targetRoom))
                return DBConnector.path_test_v2Query(floor, ID, targetRoom);
            else
                return ErrorManager.ROOM_NAME_ERROR;
        }
        else
            return ErrorManager.INTERNET_CONNECTION_ERROR;
    }


    public static String pathQuery(AugmentedImage augmentedImage, String targetRoom) {
        // check if the image's name is legal
        if(parseImage(augmentedImage).equals(ErrorManager.IMAGE_NAME_EROR))
            return ErrorManager.IMAGE_NAME_EROR;

        String floor = parseImage(augmentedImage).substring(0, 1);
        String ID = parseImage(augmentedImage).substring(1, 2);

        if(checkActiveInternetConnection()) {
            if (checkTargetName(targetRoom))
                return DBConnector.pathQuery(floor, ID, targetRoom);
            else
                return ErrorManager.ROOM_NAME_ERROR;
        }
        else
            return ErrorManager.INTERNET_CONNECTION_ERROR;
    }

    /**
     *  This method is used for developing test for querying the route from starring point to destination
     * @return
     */
    public static String pathQuery(String floor, String number, String targetRoom) {
        if(checkActiveInternetConnection()) {
            if (checkTargetName(targetRoom))
                return DBConnector.pathQuery(floor, number, targetRoom);
            else
                return ErrorManager.ROOM_NAME_ERROR;
        }
        else
            return ErrorManager.INTERNET_CONNECTION_ERROR;
    }

    public static String destQuery(String targetRoom) {
        if(checkActiveInternetConnection()) {
            if (checkTargetName(targetRoom))
                return DBConnector.destQuery(targetRoom);
            else
                return ErrorManager.ROOM_NAME_ERROR;
        }
        else
            return ErrorManager.INTERNET_CONNECTION_ERROR;
    }

    public static String destQuery_test(AugmentedImage augmentedImage, String targetRoom) {
        // check if the image's name is legal
        if(parseImage(augmentedImage).equals(ErrorManager.IMAGE_NAME_EROR))
            return ErrorManager.IMAGE_NAME_EROR;

        String floor = parseImage(augmentedImage).substring(0, 1);
        String ID = parseImage(augmentedImage).substring(1, 2);

        if(checkActiveInternetConnection()) {
            if (checkTargetName(targetRoom))
                return DBConnector.destQuery_test(floor, ID, targetRoom);
            else
                return ErrorManager.ROOM_NAME_ERROR;
        }
        else
            return ErrorManager.INTERNET_CONNECTION_ERROR;
    }

    /**
     * This function is used to  store every point's information in a specific route,
     *  and make the function static so that it is callable by other activities
     * @param result
     * @return  ArrayList of  Destination
     */
    public static ArrayList<Vertex> parseCoordinateAndRotation(String result) {
        ArrayList<Vertex> vertices = new ArrayList<>();
        try {
            // parsing string to json format
            JSONArray jsonArray = new JSONArray(result);

            // parsing coordinates info in json to string and get rid of delimiter "[", "]" then spilt the string by ","
            String vertex = jsonArray.getJSONObject(0).getString("coordinate");
            vertex = vertex.replaceAll("\\[", "").replaceAll("\\]", "");
            String[] coordinates = vertex.split(",");

            // parsing rotation info in json to string like "0,1" and stored in param rotations
            String rotation = jsonArray.getJSONObject(0).getString("rotation");
            rotation = rotation.replaceAll("\\[", "").replaceAll("\\]", "");
            String[] rotations = rotation.split(", ");

            for(int i = 0, j = 0; j < coordinates.length - 1; i++, j+=2) {

                // parsing string data to int for rotation
                rotations[i] = rotations[i].replaceAll("\'", "");
                Integer x = Integer.parseInt(rotations[i].split(",")[0]);
                Integer z = Integer.parseInt(rotations[i].split(",")[1]);

                // parsing string data to double for rotation
                double lng = Double.parseDouble(coordinates[j]);
                double lat = Double.parseDouble(coordinates[j+1]);

                // initiate the ArrayList<Destination> target and return to ArrayList<Destination> vertices
                vertices.add(new Vertex(lat, lng, new Pair<>(x, z)));
            }
        } catch (JSONException e) {
            Log.e("parsingError", result);
        }

        return vertices;
    }


    public static Poster parsePoster(String result) {
        Poster poster = null;
        try {
            // parsing string to json format
            JSONArray jsonArray = new JSONArray(result);

            // parsing coordinates info in json to string and get rid of delimiter "[", "]" then spilt the string by ","
            String coordinate = jsonArray.getJSONObject(0).getString("poster_coordinate");
            coordinate = coordinate.replaceAll("\\[", "").replaceAll("\\]", "");
            String[] latlng = coordinate.split(",");

            // parsing rotation info in json to string like "0,1" and stored in pram rotations
            String rotation = jsonArray.getJSONObject(0).getString("poster_rotation");
            rotation = rotation.replaceAll("\\[", "").replaceAll("\\]", "");
            String[] rotations = rotation.replaceAll("\'", "").split(",");

            // parsing string data to int for rotation
            Integer x = Integer.parseInt(rotations[0]);
            Integer z = Integer.parseInt(rotations[1]);

            // parsing string data to double for rotation
            double lng = Double.parseDouble(latlng[0]);
            double lat = Double.parseDouble(latlng[1]);

            poster = new Poster(lat, lng , new Pair<>(x, z));
        } catch (JSONException e) {
            Log.e("parsingError", result);
        }

        return poster;
    }

    public static Destination parseDest(String result) {
        Destination dest = null;
        try {
            // parsing string to json format
            JSONArray jsonArray = new JSONArray(result);

            // parsing coordinates info in json to string and get rid of delimiter "[", "]" then spilt the string by ","
            String coordinate = jsonArray.getJSONObject(0).getString("dest_coordinate");
            coordinate = coordinate.replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("\'", "");
            coordinate = coordinate.replaceAll("\\[", "").replaceAll("\\]", "");
            String[] latlng = coordinate.split(",");
            Log.e("dest", result);
            // parsing string data to double for rotation
            double lng = Double.parseDouble(latlng[0]);
            double lat = Double.parseDouble(latlng[1]);

            dest = new Destination(lat, lng);


        } catch (JSONException e) {
            Log.e("parsingError", result);
            e.printStackTrace();
        }

        return dest;
    }

    public static boolean checkActiveInternetConnection() {
        if (isNetworkAvailable()) {
            try {
                HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.google.com").openConnection());
                urlc.setRequestProperty("User-Agent", "Test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1500);
                urlc.connect();
                return (urlc.getResponseCode() == 200);
            } catch (IOException e) {
                Log.e("INTERNET CONNECTION CHECK", "Error: ", e);
            }
        } else {
            Log.d("INTERNET CONNECTION CHECK", "No network present");
        }
        return false;
    }

    private static String parseImage(AugmentedImage augmentedImage) {
        String name = augmentedImage.getName();
        if(checkImageName(name)) {
            String floor = name.substring(2, 3);
            String ID = name.substring(5, 6);
            return floor + ID;
        }
        else
            return ErrorManager.IMAGE_NAME_EROR;
    }

    private static boolean checkImageName(String name) {
        String pattern = "sf\\df_\\d.jpg";

        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(name);
        return m.find();
    }

    private static boolean isNetworkAvailable() {
        ConnectivityManager manager =
                (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            // Network is present and connected
            isAvailable = true;
        }
        return isAvailable;
    }

    public static boolean checkTargetName(String target) {
        String pattern1 = "sf\\d\\d\\d[a-zA-Z]?";         // sf635A or sf647 ...
        String pattern2 = "[男女]廁";                  // 男廁或女廁

        Pattern r1 = Pattern.compile(pattern1);
        Matcher m1 = r1.matcher(target);

        Pattern r2 = Pattern.compile(pattern2);
        Matcher m2 = r2.matcher(target);

        return m1.find() || m2.find() || target.equals("廁所");
    }
}

