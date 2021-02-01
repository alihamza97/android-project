package com.fontys.practical;

public class UsersLocations {
    public String id;
    public boolean loggedIn;
    public double latitude;
    public double longitude;
    public String token;

    public UsersLocations(){
    }

    public UsersLocations(String id, double latitude, double longitude, String token){
        this.id = id;
        this.loggedIn = true;
        this.latitude = latitude;
        this.longitude = longitude;
        this.token=token;

    }

    public boolean getLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
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
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }


}
