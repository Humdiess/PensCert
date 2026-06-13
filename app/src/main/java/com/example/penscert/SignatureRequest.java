package com.example.penscert;

import com.google.gson.annotations.SerializedName;

public class SignatureRequest {
    @SerializedName("id")
    public String id;

    @SerializedName("participant_name")
    public String participantName;

    @SerializedName("participant_role")
    public String participantRole;

    @SerializedName("document_type")
    public String documentType;

    @SerializedName("status")
    public String status; // PENDING, APPROVED, REJECTED

    @SerializedName("created_at")
    public String createdAt;

    public SignatureRequest() {}

    public SignatureRequest(String name, String role, String type) {
        this.participantName = name;
        this.participantRole = role;
        this.documentType = type;
        this.status = "PENDING";
    }
}
