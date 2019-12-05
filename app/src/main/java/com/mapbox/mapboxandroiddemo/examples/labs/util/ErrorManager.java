package com.mapbox.mapboxandroiddemo.examples.labs.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

import com.mapbox.mapboxandroiddemo.examples.labs.common.fragment.ErrorFragment;

@SuppressLint("Registered")
public final class ErrorManager {
    private static final ErrorManager THE_INSTANCE = new ErrorManager();


    public static final String INTERNET_CONNECTION_ERROR = "INTERNET_CONNECTION_ERROR";
    public static final String ROOM_NAME_ERROR = "ROOM_NAME_ERROR";
    public static final String IMAGE_NAME_EROR = "IMAGE_NAME_EROR";
    public static final String JSON_EXCEPTION_ERROR = "JSON_EXCEPTION_ERROR";

    public static final String ERROR_MESSAGE = "ERROR_MESSAGE";


    public static ErrorManager getInstance() { return THE_INSTANCE; }


    /*public void show(Activity activity, String errorMsg) {
        Bundle bundle = new Bundle();
        bundle.putString(ERROR_MESSAGE, errorMsg);

        DialogFragment errorFragment = new ErrorFragment();
        errorFragment.setArguments(bundle);
        errorFragment.show(activity.getFragmentManager(), ErrorFragment.TAG);
    }

    public void showWithLog(String errorMsg, String tag, Exception e) {
        Bundle bundle = new Bundle();
        bundle.putString(ERROR_MESSAGE, errorMsg);

        DialogFragment errorFragment = new ErrorFragment();
        errorFragment.setArguments(bundle);
        errorFragment.show(getSupportFragmentManager(), ErrorFragment.TAG);

        Log.e(tag, e.toString());
    }*/

    public void Log(String tag, Exception e) {
        Log.e(tag, e.toString());
    }

}
