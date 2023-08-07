package com.anvay.noqueuepaydashboard.models;

import android.text.format.DateFormat;

import androidx.annotation.Nullable;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Calendar;
import java.util.List;

@IgnoreExtraProperties
public class Order {

    @ServerTimestamp
    private Timestamp timestamp;
    private String orderId;
    private List<ProductItem> itemsList;
    private String customerId;
    private int totalQuantity;
    private double totalPrice;
    private String transactionNumber;
    private boolean verified;
    private String customerName;
    private String storeId;
    @Exclude
    private String docId;

    public Order() {
    }

    public Order(String orderId, String customerId, int totalQuantity, double totalPrice,
                 List<ProductItem> itemsList, String transactionNumber, String storeId) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.totalPrice = totalPrice;
        this.totalQuantity = totalQuantity;
        this.itemsList = itemsList;
        this.transactionNumber = transactionNumber;
        this.storeId = storeId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public List<ProductItem> getItemsList() {
        return itemsList;
    }

    public void setItemsList(List<ProductItem> itemsList) {
        this.itemsList = itemsList;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    @Override
    public boolean equals(@Nullable @org.jetbrains.annotations.Nullable Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof Order))
            return false;
        return this.orderId.equals(((Order) obj).getOrderId());
    }

    @Exclude
    public String getDocId() {
        return docId;
    }

    @Exclude
    public void setDocId(String docId) {
        this.docId = docId;
    }

    @Exclude
    public String getDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(this.timestamp.getSeconds() * 1000);
        return DateFormat.format("dd-MMM-yy hh:mm aa", calendar).toString();
//        return this.timestamp.toDate().toString();
    }
}
