package com.mapbox.mapboxandroiddemo.examples.labs.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Destination implements Parcelable{
    private double lat, lng;

    public Destination(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    protected Destination(Parcel in) {
        lat = in.readDouble();
        lng = in.readDouble();

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(lat);
        dest.writeDouble(lng);
    }

    public static final Parcelable.Creator<Destination> CREATOR = new Parcelable.Creator<Destination>() {
        @Override
        public Destination createFromParcel(Parcel in) {
            return new Destination(in);
        }

        @Override
        public Destination[] newArray(int size) {
            return new Destination[size];
        }
    };
}
