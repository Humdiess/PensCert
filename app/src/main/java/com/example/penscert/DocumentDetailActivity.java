package com.example.penscert;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

public class DocumentDetailActivity extends AppCompatActivity {

    private ImageView ivStatusIcon;
    private TextView tvStatusBadge, tvDocTitle, tvIssuedDate;
    private TextView tvName, tvRole, tvCertId;
    private TextView tvHash, tvSignature;
    private TextView tvAbsenMatkul, tvAbsenTanggal, tvSigner;
    private MaterialCardView statusBadge, layoutAbsenCard, layoutSignerCard;
    private Button btnViewPdf, btnShare;
    private View loadingLayout;
    private SupabaseHelper supabaseHelper;
    private Certificate currentCert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_detail);

        ivStatusIcon = findViewById(R.id.ivStatusIcon);
        tvStatusBadge = findViewById(R.id.tvStatusBadge);
        tvDocTitle = findViewById(R.id.tvDocTitle);
        tvIssuedDate = findViewById(R.id.tvIssuedDate);
        tvName = findViewById(R.id.tvName);
        tvRole = findViewById(R.id.tvRole);
        tvCertId = findViewById(R.id.tvCertId);
        tvHash = findViewById(R.id.tvHash);
        tvSignature = findViewById(R.id.tvSignature);
        tvAbsenMatkul = findViewById(R.id.tvAbsenMatkul);
        tvAbsenTanggal = findViewById(R.id.tvAbsenTanggal);
        tvSigner = findViewById(R.id.tvSigner);
        statusBadge = findViewById(R.id.statusBadge);
        layoutAbsenCard = findViewById(R.id.layoutAbsenCard);
        layoutSignerCard = findViewById(R.id.layoutSignerCard);
        btnViewPdf = findViewById(R.id.btnViewPdf);
        btnShare = findViewById(R.id.btnShare);
        loadingLayout = findViewById(R.id.loadingLayout);

        supabaseHelper = new SupabaseHelper();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        String certId = getIntent().getStringExtra("CERT_ID");
        if (certId != null && !certId.isEmpty()) {
            loadDocument(certId);
        } else {
            Toast.makeText(this, "ID dokumen tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadDocument(String certId) {
        if (loadingLayout != null) loadingLayout.setVisibility(View.VISIBLE);

        supabaseHelper.fetchCertificate(certId, new SupabaseHelper.Callback<Certificate>() {
            @Override
            public void onSuccess(Certificate cert) {
                if (isFinishing()) return;
                if (loadingLayout != null) loadingLayout.setVisibility(View.GONE);
                currentCert = cert;
                displayResult(cert);
            }

            @Override
            public void onError(Exception e) {
                if (isFinishing()) return;
                if (loadingLayout != null) loadingLayout.setVisibility(View.GONE);
                Toast.makeText(DocumentDetailActivity.this, "Gagal memuat: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayResult(Certificate cert) {
        tvDocTitle.setText(cert.getDisplayType());
        tvIssuedDate.setText("Diterbitkan pada " + (cert.issuedAt != null ? cert.issuedAt : "-"));
        tvName.setText(cert.getDisplayName());
        tvRole.setText(cert.getDisplayRole());
        tvCertId.setText(cert.getDisplayId());
        tvHash.setText(cert.sha256Hash != null ? cert.sha256Hash : (cert.hash != null ? cert.hash : "N/A"));
        tvSignature.setText(cert.rsaSignature != null ? cert.rsaSignature : (cert.signature != null ? cert.signature : "N/A"));

        // Absen detail
        if (cert.subjectName != null && !cert.subjectName.isEmpty()) {
            layoutAbsenCard.setVisibility(View.VISIBLE);
            tvAbsenMatkul.setText("Matkul: " + cert.subjectName);
            tvAbsenTanggal.setText("Tanggal: " + (cert.subjectDate != null ? cert.subjectDate : "-"));
        }

        // Signer info
        if (cert.targetSigner != null && !cert.targetSigner.isEmpty()) {
            layoutSignerCard.setVisibility(View.VISIBLE);
            tvSigner.setText(cert.targetSigner);
        }

        // Status badge
        String currentStatus = cert.status != null ? cert.status : (cert.verificationStatus != null ? cert.verificationStatus : "PENDING");
        if ("VALID".equalsIgnoreCase(currentStatus)) {
            tvStatusBadge.setText("TERVERIFIKASI RESMI");
            statusBadge.setCardBackgroundColor(Color.parseColor("#4010B981"));
            tvStatusBadge.setTextColor(Color.parseColor("#A7F3D0"));
            ivStatusIcon.setImageResource(R.drawable.ic_shield);
            ivStatusIcon.setColorFilter(Color.parseColor("#A7F3D0"));
        } else if ("PENDING".equalsIgnoreCase(currentStatus)) {
            tvStatusBadge.setText("MENUNGGU TTD");
            statusBadge.setCardBackgroundColor(Color.parseColor("#40F59E0B"));
            tvStatusBadge.setTextColor(Color.parseColor("#FDE68A"));
            ivStatusIcon.setImageResource(R.drawable.ic_clock);
            ivStatusIcon.setColorFilter(Color.parseColor("#FDE68A"));
        } else if ("REJECTED".equalsIgnoreCase(currentStatus)) {
            tvStatusBadge.setText("DITOLAK");
            statusBadge.setCardBackgroundColor(Color.parseColor("#40EF4444"));
            tvStatusBadge.setTextColor(Color.parseColor("#FECACA"));
            ivStatusIcon.setImageResource(R.drawable.ic_lock);
            ivStatusIcon.setColorFilter(Color.parseColor("#FECACA"));
        } else if ("REVOKED".equalsIgnoreCase(currentStatus)) {
            tvStatusBadge.setText("DICABUT");
            statusBadge.setCardBackgroundColor(Color.parseColor("#40EF4444"));
            tvStatusBadge.setTextColor(Color.parseColor("#FECACA"));
            ivStatusIcon.setImageResource(R.drawable.ic_lock);
            ivStatusIcon.setColorFilter(Color.parseColor("#FECACA"));
        }

        // Action buttons
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
                String shareText = "Dokumen: " + cert.getDisplayType() + "\n" +
                        "Pemilik: " + cert.getDisplayName() + "\n" +
                        "Status: " + cert.getStatusLabel() + "\n" +
                        "Lihat PDF: " + cert.pdfUrl;
                if (cert.verificationUrl != null && !cert.verificationUrl.isEmpty()) {
                    shareText += "\nVerifikasi: " + cert.verificationUrl;
                }
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                startActivity(Intent.createChooser(shareIntent, "Bagikan Dokumen"));
            });
        }
    }
}
