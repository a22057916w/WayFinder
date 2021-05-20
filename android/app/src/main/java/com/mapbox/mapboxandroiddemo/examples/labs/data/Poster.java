package com.mapbox.mapboxandroiddemo.examples.labs.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Pair;

public class Poster implements Parcelable {
    private double lat, lng;
    private Pair<Integer, Integer> rotation;
    private String direction;

    public Poster(double lat, double lng, Pair<Integer, Integer> rotation) {
        this.lat = lat;
        this.lng = lng;
        this.rotation = rotation;
        this.direction = setDirection(rotation);
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public Pair<Integer, Integer> getRotation() {
        return rotation;
    }

    public String getDirection() { return this.direction;}

    private String setDirection(Pair<Integer, Integer> rotation) {
        if(rotation.first != 0) {
            if(rotation.first > 0)
                return "E";
            else
                return "W";
        }
        else {
            if (rotation.second > 0)
                return "N";
            else
                return "S";
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    protected Poster(Parcel in) {
        lat = in.readDouble();
        lng = in.readDouble();

        // restore the pair<int, int>
        rotation = new Pair<>(in.readInt(), in.readInt());

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(lat);
        dest.writeDouble(lng);
        dest.writeInt(rotation.first);
        dest.writeInt(rotation.second);
    }

    public static final Creator<Poster> CREATOR = new Creator<Poster>() {
        @Override
        public Poster createFromParcel(Parcel in) {
            return new Poster(in);
        }

        @Override
        public Poster[] newArray(int size) {
            return new Poster[size];
        }
    };
}
