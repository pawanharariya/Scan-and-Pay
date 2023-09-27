package com.psh.scanpay.models;

public class User {
    private String name, mobileNumber, city, zipcode, email;

    public User() {
    }

    public User(String name, String mobileNumber, String city, String zipcode, String email) {
        this.name = name;
        this.mobileNumber = mobileNumber;
        this.city = city;
        this.email = email;
        this.zipcode = zipcode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
