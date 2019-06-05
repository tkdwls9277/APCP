package com.example.apcp;

public class DataFromServer {

    private double temp;
    private double humi;
    private double dust;
    private double co;
    private double latitude;
    private double longitude;

    public DataFromServer () {

    }

    public DataFromServer (double temp, double humi, double dust, double co, double latitude, double longitude) {

        this.temp = temp;
        this.humi = humi;
        this.dust = dust;
        this.co = co;
        this.latitude = latitude;
        this.longitude = longitude;

    }
    public void setTemp(double temp) {
        this.temp = temp;
    }

    public double getTemp() {
        return temp;
    }

    public void setHumi(double humi) {
        this.humi = humi;
    }

    public double getHumi() {
        return humi;
    }

    public void setDust(double dust) {
        this.dust = dust;
    }

    public double getDust() {
        return dust;
    }

    public void setCo(double co) {
        this.co = co;
    }

    public double getCo () {
        return co;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
