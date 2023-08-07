package com.anvay.noqueuepaydashboard.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

public class Complaint {
    @ServerTimestamp
    private Timestamp timestamp;

    private String category;
    private String description;
    private String storeId;
    private String ticketNumber;

    public Complaint() {
    }

    public Complaint(String category, String description, String storeId, String ticketNumber) {
        this.category = category;
        this.description = description;
        this.storeId = storeId;
        this.ticketNumber = ticketNumber;
    }

    public String getCategory() {

        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }
}
