package com.mapbox.mapboxandroiddemo.examples.labs.common.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

import com.mapbox.mapboxandroiddemo.examples.labs.ar.ArImageAndNavigation;
import com.mapbox.mapboxandroiddemo.examples.labs.data.FeatureData;

import java.util.Objects;

public class NavigationFragment extends DialogFragment {
    public static final String TAG = "NAVIGATION_FRAGMENT";
    private FeatureData featureData;

    @Override
    public void setArguments(@Nullable Bundle args) {
        if (args != null) {
            featureData = (FeatureData) args.getSerializable("featureData");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(featureData.getName() + "-" + featureData.getID())
                .setPositiveButton("Navigation", (dialog, id) -> {
                    Intent intent = new Intent(Objects.requireNonNull(getActivity()).getApplication(), ArImageAndNavigation.class);
                    intent.putExtra("id", TAG);
                    intent.putExtra("query", featureData.getID());
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, id) -> {
                    // User cancelled the dialog
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}