package com.anvay.noqueuepaydashboard.models;

import com.google.android.gms.maps.model.LatLng;

public class Store {
    private String storeId;
    private String storeName;
    private String storeUpiId;
    private String address;
    private double latitude;
    private double longitude;
    private String imageUrl;
    private String storeExtraInfo;
    private String mobileNumber;
    private String email;
    private String zipcode;
    public Store() {
    }

    public Store(String storeId, String storeName, String storeAddress, String storeUpiId,
                 String imageUrl, String storeExtraInfo, String mobileNumber, String email,
                 String zipcode, double latitude, double longitude) {
        this.storeId = storeId;
        this.storeName = storeName;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.address = storeAddress;
        this.storeUpiId = storeUpiId;
        this.imageUrl = imageUrl;
        this.storeExtraInfo = storeExtraInfo;
        this.latitude = latitude;
        this.longitude = longitude;
        this.zipcode = zipcode;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getStoreUpiId() {
        return storeUpiId;
    }

    public void setStoreUpiId(String storeUpiId) {
        this.storeUpiId = storeUpiId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getStoreExtraInfo() {
        return storeExtraInfo;
    }

    public void setStoreExtraInfo(String storeExtraInfo) {
        this.storeExtraInfo = storeExtraInfo;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
