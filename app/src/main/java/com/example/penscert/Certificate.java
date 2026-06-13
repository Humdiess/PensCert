package com.example.penscert;

import com.google.gson.annotations.SerializedName;

/**
 * Model data untuk Sertifikat/Dokumen sesuai dengan skema tabel Supabase.
 */
public class Certificate {
    @SerializedName("id")
    public String id;

    @SerializedName("certificate_number")
    public String certificateNumber;

    @SerializedName("participant_name")
    public String participantName;

    @SerializedName("participant_role")
    public String participantRole;

    @SerializedName("event_name")
    public String eventName;

    @SerializedName("issued_at")
    public String issuedAt;

    @SerializedName("sha256_hash")
    public String sha256Hash;

    @SerializedName("rsa_signature")
    public String rsaSignature;

    @SerializedName("verification_url")
    public String verificationUrl;

    @SerializedName("status")
    public String status; // VALID, PENDING, REVOKED

    @SerializedName("cert_id")
    public String certId;

    @SerializedName("pdf_url")
    public String pdfUrl;

    @SerializedName("qr_token")
    public String qrToken;

    @SerializedName("pdf_storage_path")
    public String pdfStoragePath;

    @SerializedName("pdf_filename")
    public String pdfFilename;

    @SerializedName("name")
    public String name;

    @SerializedName("role")
    public String role;

    @SerializedName("event")
    public String event;

    @SerializedName("hash")
    public String hash;

    @SerializedName("signature")
    public String signature;

    // Field untuk posisi QR Code
    @SerializedName("qr_x")
    public float qrX;

    @SerializedName("qr_y")
    public float qrY;

    @SerializedName("qr_page")
    public int qrPage;

    // Field untuk target penandatangan (Dosen)
    @SerializedName("target_signer")
    public String targetSigner;

    // Field Baru untuk Surat Absen
    @SerializedName("subject_name")
    public String subjectName;

    @SerializedName("subject_date")
    public String subjectDate;

    // Backward compatibility or internal UI logic
    @SerializedName("verification_status")
    public String verificationStatus;

    public String getDisplayName() {
        if (participantName != null && !participantName.trim().isEmpty()) return participantName;
        if (name != null && !name.trim().isEmpty()) return name;
        return "Nama Tidak Diketahui";
    }

    public String getDisplayId() {
        if (certificateNumber != null && !certificateNumber.trim().isEmpty()) return certificateNumber;
        if (certId != null && !certId.trim().isEmpty()) return certId;
        return "N/A";
    }

    public String getDisplayType() {
        if (eventName != null && !eventName.trim().isEmpty()) return eventName;
        if (event != null && !event.trim().isEmpty()) return event;
        return "Dokumen Resmi";
    }

    public String getDisplayRole() {
        if (participantRole != null && !participantRole.trim().isEmpty()) return participantRole;
        if (role != null && !role.trim().isEmpty()) return role;
        return "Peserta";
    }

    public String getStatusLabel() {
        String s = (status != null) ? status : (verificationStatus != null ? verificationStatus : "PENDING");
        switch (s.toUpperCase()) {
            case "VALID": return "TERVERIFIKASI";
            case "REJECTED":
            case "REVOKED": return "DITOLAK";
            case "PENDING": return "MENUNGGU";
            default: return s;
        }
    }
}
