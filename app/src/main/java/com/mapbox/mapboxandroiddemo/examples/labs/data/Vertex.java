package com.mapbox.mapboxandroiddemo.examples.labs.data;


import android.os.Parcel;
import android.os.Parcelable;
import android.util.Pair;

public class Vertex implements Parcelable {
    private double lat, lng;
    private Pair<Integer, Integer> rotation;

    public Vertex(double lat, double lng, Pair<Integer, Integer> rotation) {
        this.lat = lat;
        this.lng = lng;
        this.rotation = rotation;
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

    @Override
    public int describeContents() {
        return 0;
    }

    protected Vertex(Parcel in) {
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

    public static final Creator<Vertex> CREATOR = new Creator<Vertex>() {
        @Override
        public Vertex createFromParcel(Parcel in) {
            return new Vertex(in);
        }

        @Override
        public Vertex[] newArray(int size) {
            return new Vertex[size];
        }
    };

}
