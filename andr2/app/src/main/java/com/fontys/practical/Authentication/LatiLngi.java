package com.fontys.practical.Authentication;

public class LatiLngi {
    public String name;
    public double latitude;
    public double longitude;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LatiLngi(){

    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

//    public LatiLngi(String name){
//
//        this.name=name;
//
//
//    }
    public LatiLngi(double latitude, double longitude){


        this.latitude=latitude;
        this.longitude=longitude;

    }
}
