package com.example.penscert;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

public class VerificationResultActivity extends AppCompatActivity {

    private ImageView ivStatusIcon;
    private TextView tvStatusBadge, tvDocTitle, tvIssuedDate, tvName, tvCertId, tvRole, tvHash, tvSignature;
    private TextView tvVerifyMatkul, tvVerifyTanggal;
    private LinearLayout layoutVerifyAbsen;
    private MaterialCardView statusBadge;
    private Button btnViewPdf, btnShare, btnBack;
    private SupabaseHelper supabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification_result);

        ivStatusIcon = findViewById(R.id.ivStatusIcon);
        tvStatusBadge = findViewById(R.id.tvStatusBadge);
        tvDocTitle = findViewById(R.id.tvDocTitle);
        tvIssuedDate = findViewById(R.id.tvIssuedDate);
        tvName = findViewById(R.id.tvName);
        tvCertId = findViewById(R.id.tvCertId);
        tvRole = findViewById(R.id.tvRole);
        tvHash = findViewById(R.id.tvHash);
        tvSignature = findViewById(R.id.tvSignature);
        
        // Detail Absen
        layoutVerifyAbsen = findViewById(R.id.layoutVerifyAbsen);
        tvVerifyMatkul = findViewById(R.id.tvVerifyMatkul);
        tvVerifyTanggal = findViewById(R.id.tvVerifyTanggal);

        statusBadge = findViewById(R.id.statusBadge);
        btnViewPdf = findViewById(R.id.btnViewPdf);
        btnShare = findViewById(R.id.btnShare);
        btnBack = findViewById(R.id.btnBack);

        btnViewPdf.setVisibility(View.GONE);
        btnShare.setVisibility(View.GONE);

        supabaseHelper = new SupabaseHelper();

        // Handle both QR scan result and direct CERT_ID navigation
        String scanResult = getIntent().getStringExtra("SCAN_RESULT");
        String certId = getIntent().getStringExtra("CERT_ID");
        if (scanResult != null && !scanResult.isEmpty()) {
            verifyDocument(scanResult);
        } else if (certId != null && !certId.isEmpty()) {
            verifyDocument(certId);
        } else {
            showError("No document ID provided");
        }

        btnBack.setOnClickListener(v -> finish());
    }

    private void verifyDocument(String input) {
        String docId = input;
        if (input.contains("id=")) {
            docId = input.substring(input.indexOf("id=") + 3);
            if (docId.contains("&")) {
                docId = docId.substring(0, docId.indexOf("&"));
            }
        } else if (input.contains("/")) {
            docId = input.substring(input.lastIndexOf("/") + 1);
        }

        supabaseHelper.fetchCertificate(docId, new SupabaseHelper.Callback<Certificate>() {
            @Override
            public void onSuccess(Certificate cert) {
                if (cert != null) {
                    displayResult(cert);
                } else {
                    showError("Dokumen tidak ditemukan di database.");
                }
            }

            @Override
            public void onError(Exception e) {
                showError(e.getMessage());
            }
        });
    }

    private void showError(String message) {
        if (isFinishing()) return;
        tvDocTitle.setText("Verifikasi Gagal");
        tvStatusBadge.setText("TIDAK TERDAFTAR");
        statusBadge.setCardBackgroundColor(Color.parseColor("#40EF4444"));
        tvStatusBadge.setTextColor(Color.parseColor("#FECACA"));
        ivStatusIcon.setImageResource(android.R.drawable.ic_delete);
        ivStatusIcon.setColorFilter(Color.parseColor("#FECACA"));
        Toast.makeText(this, "Error: " + message, Toast.LENGTH_LONG).show();
    }

    private void displayResult(Certificate cert) {
        if (isFinishing()) return;

        tvName.setText(cert.getDisplayName());
        tvCertId.setText(cert.getDisplayId());
        tvRole.setText(cert.getDisplayRole());
        tvDocTitle.setText(cert.getDisplayType());
        tvIssuedDate.setText("Diterbitkan pada " + (cert.issuedAt != null ? cert.issuedAt : "-"));

        tvHash.setText(cert.sha256Hash != null ? cert.sha256Hash : (cert.hash != null ? cert.hash : "N/A"));
        tvSignature.setText(cert.rsaSignature != null ? cert.rsaSignature : (cert.signature != null ? cert.signature : "N/A"));

        // Tampilkan detail absen jika tersedia
        if (cert.subjectName != null && !cert.subjectName.isEmpty()) {
            layoutVerifyAbsen.setVisibility(View.VISIBLE);
            tvVerifyMatkul.setText(cert.subjectName);
            tvVerifyTanggal.setText(cert.subjectDate != null ? cert.subjectDate : "-");
        } else {
            layoutVerifyAbsen.setVisibility(View.GONE);
        }

        String currentStatus = cert.status != null ? cert.status : (cert.verificationStatus != null ? cert.verificationStatus : "PENDING");

        if ("VALID".equalsIgnoreCase(currentStatus)) {
            tvStatusBadge.setText("TERVERIFIKASI RESMI");
            statusBadge.setCardBackgroundColor(Color.parseColor("#4010B981"));
            tvStatusBadge.setTextColor(Color.parseColor("#A7F3D0"));
            ivStatusIcon.setImageResource(R.drawable.ic_verified);
            ivStatusIcon.setColorFilter(Color.parseColor("#A7F3D0"));
        } else if ("PENDING".equalsIgnoreCase(currentStatus)) {
            tvStatusBadge.setText("MENUNGGU TTD");
            statusBadge.setCardBackgroundColor(Color.parseColor("#40F59E0B"));
            tvStatusBadge.setTextColor(Color.parseColor("#FDE68A"));
            ivStatusIcon.setImageResource(android.R.drawable.ic_menu_recent_history);
            ivStatusIcon.setColorFilter(Color.parseColor("#FDE68A"));
        } else {
            tvStatusBadge.setText("TIDAK VALID / DICABUT");
            statusBadge.setCardBackgroundColor(Color.parseColor("#40EF4444"));
            tvStatusBadge.setTextColor(Color.parseColor("#FECACA"));
            ivStatusIcon.setImageResource(android.R.drawable.stat_notify_error);
            ivStatusIcon.setColorFilter(Color.parseColor("#FECACA"));
        }

        if (cert.pdfUrl != null && !cert.pdfUrl.isEmpty()) {
            btnViewPdf.setVisibility(View.VISIBLE);
            btnViewPdf.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(cert.pdfUrl));
                startActivity(intent);
            });

            btnShare.setVisibility(View.VISIBLE);
            btnShare.setOnClickListener(v -> {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Lihat Dokumen: " + cert.pdfUrl);
                startActivity(Intent.createChooser(shareIntent, "Bagikan Dokumen"));
            });
        }
    }
}
