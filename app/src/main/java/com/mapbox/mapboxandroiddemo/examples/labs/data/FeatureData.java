package com.mapbox.mapboxandroiddemo.examples.labs.data;


import com.mapbox.geojson.Feature;

import java.io.Serializable;


public class FeatureData implements Serializable {
    private Feature feature;
    private String Type, ID, name;

    public FeatureData(Feature feature) {
        this.feature = feature;
        setFeatureData(feature);
    }

    private void setFeatureData(Feature feature) {

        // Setting properties
        Type = feature.getStringProperty("type");
        ID = feature.getStringProperty("id");
        name = feature.getStringProperty("name");
    }

    public String getType() {
        return this.Type;
    }

    public String getID() {
        return this.ID;
    }

    public String getName() {
        return this.name;
    }

    public Feature getFeature() {
        return this.feature;
    }
}
