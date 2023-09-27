package com.psh.scanpay.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@IgnoreExtraProperties
@Entity(tableName = "order_history")
public class Order {
    @Exclude
    @PrimaryKey(autoGenerate = true)
    private int ID;

    @ServerTimestamp
    @Ignore
    private Timestamp timestamp;

    @ColumnInfo(name = "store_id")
    private String storeId;

    @ColumnInfo(name = "store_name")
    @Exclude
    private String storeName;

    @ColumnInfo(name = "order_id")
    private String orderId;

    @Ignore
    private List<ProductItem> itemsList;
    @Ignore
    private String customerId;
    @ColumnInfo(name = "total_quantity")
    private int totalQuantity;
    @ColumnInfo(name = "total_price")
    private double totalPrice;
    @ColumnInfo(name = "transaction_number")
    private String transactionNumber;
    @Ignore
    private boolean verified;
    @Ignore
    private String customerName;
    @Exclude
    @ColumnInfo(name = "date")
    private String date;

    public Order() {
    }

    public Order(String orderId, String storeId, String storeName, String customerId, int totalQuantity,
                 double totalPrice, List<ProductItem> itemsList, String transactionNumber, String customerName) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.storeId = storeId;
        this.storeName = storeName;
        this.customerId = customerId;
        this.totalPrice = totalPrice;
        this.totalQuantity = totalQuantity;
        this.itemsList = itemsList;
        this.transactionNumber = transactionNumber;
        this.customerName = customerName;
        this.verified = false;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm aa");
        this.date = dateFormat.format(new Date());
    }

    @Exclude
    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    @Exclude
    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
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

    @Exclude
    public String getDate() {
        return date;
    }

    @Exclude
    public void setDate(String date) {
        this.date = date;
    }
}
