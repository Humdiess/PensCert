package com.example.penscert;

import com.google.gson.annotations.SerializedName;

public class Notification {
    @SerializedName("id")
    public String id;

    @SerializedName("title")
    public String title;

    @SerializedName("message")
    public String message;

    @SerializedName("type")
    public String type; // STATUS_CHANGE, NEW_REQUEST, APPROVED, REJECTED

    @SerializedName("timestamp")
    public String timestamp;

    @SerializedName("isRead")
    public boolean isRead;

    @SerializedName("certId")
    public String certId;

    public Notification() {}

    public Notification(String id, String title, String message, String type, String certId) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.type = type;
        this.certId = certId;
        this.isRead = false;
        this.timestamp = String.valueOf(System.currentTimeMillis());
    }
}
