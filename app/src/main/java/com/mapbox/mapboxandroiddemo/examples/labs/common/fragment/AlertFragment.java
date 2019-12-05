package com.mapbox.mapboxandroiddemo.examples.labs.common.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import java.util.Objects;


public class AlertFragment extends DialogFragment {
    public static final String TAG = "ALERT_FRAGMENT";
    public static final String ALERT_MESSAGE = "ALERT_MESSAGE";
    public static final String DISMISS_BEHAVIOR = "DISMISS_BEHAVIOR";

    public static final String ACTIVITY_RESTART = "ACTIVITY_RESTART";
    public static final String ACTIVITY_RESUME = "ACTIVITY_RESUME";
    public static final String ACTIVITY_FINISH = "ACTIVITY_FINISH";



    public static AlertFragment newInstance(String message, String dismissBehavior) {
        AlertFragment frag = new AlertFragment();
        Bundle args = new Bundle();
        args.putString(ALERT_MESSAGE, message);
        args.putString(DISMISS_BEHAVIOR, dismissBehavior);
        frag.setArguments(args);
        return frag;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String message = Objects.requireNonNull(getArguments()).getString(ALERT_MESSAGE);
        String dismissBehavior = getArguments().getString(DISMISS_BEHAVIOR);

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message);
        builder.setPositiveButton("Close", (dialog, id) -> {
            if (Objects.requireNonNull(dismissBehavior).equals(ACTIVITY_RESUME))
                dismiss();
            if (Objects.equals(dismissBehavior, ACTIVITY_FINISH))
                Objects.requireNonNull(getActivity()).finish();
            if (Objects.equals(dismissBehavior, ACTIVITY_RESTART))
                Objects.requireNonNull(getActivity()).recreate();
        });
        // Create the AlertDialog object and return it
        return builder.create();
    }


}
