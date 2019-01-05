package com.android.toseefkhan.pandog.models;

import com.google.android.gms.maps.model.LatLng;

public class LatLong {

    private Double latitude;
    private Double longitude;

    public LatLong() {
    }

    public LatLong(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

}
