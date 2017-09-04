package com.renegade.trap;

/**
 * Created by brent on 8/12/17.
 */

public class Customer {
    String name;
    String address;
    String city;
    String State;
    String zip;
    double latitude;
    double longitude;

    public Customer() {
    }

    public Customer(String name, String address, String city, String state, String zip, double latitude, double longitude) {
        this.name = name;
        this.address = address;
        this.city = city;
        State = state;
        this.zip = zip;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Customer(String name, String address, String city, String state, String zip) {
        this.name = name;
        this.address = address;
        this.city = city;
        this.zip = zip;
        State = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return State;
    }

    public void setState(String state) {
        State = state;
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

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    @Override
    public String toString() {
        return "\"" + name + "\"," +
               "\"" + address + "\"," +
                "\"" + city + "\"," +
                "\"" + State + "\"," +
                "\"" + zip + "\"," +
                "\"" + latitude + "\"," +
                "\"" + longitude + "\"";
    }
}
