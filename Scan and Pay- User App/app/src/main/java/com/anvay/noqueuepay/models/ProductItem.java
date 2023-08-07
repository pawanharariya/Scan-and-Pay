package com.anvay.noqueuepay.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
@Entity(tableName = "new_cart")
public class ProductItem {
    @PrimaryKey(autoGenerate = true)
    @Exclude
    private int ID;

    @ColumnInfo(name = "product_id")
    private String productId;

    @ColumnInfo(name = "product_price")
    private double productPrice;

    @ColumnInfo(name = "product_quantity")
    private int productQuantity;

    @ColumnInfo(name = "product_name")
    private String productName;

    public boolean createProductFromString(String scanResult) {
        String[] details = scanResult.split("#");
        if (details.length != 3)
            return false;
        this.productId = details[0];
        this.productName = details[1];
        this.productPrice = Double.parseDouble(details[2]);
        this.productQuantity = 1;
        return true;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(double productPrice) {
        this.productPrice = productPrice;
    }

    public int getProductQuantity() {
        return productQuantity;
    }

    public void setProductQuantity(int productQuantity) {
        this.productQuantity = productQuantity;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    @Exclude
    public int getID() {
        return ID;
    }

    @Exclude
    public void setID(int ID) {
        this.ID = ID;
    }

    public void incrementQuantity() {
        productQuantity++;
    }

    public void decrementQuantity() {
        productQuantity--;
    }
}
