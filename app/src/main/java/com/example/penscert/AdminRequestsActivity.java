package com.example.penscert;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AdminRequestsActivity extends AppCompatActivity {

    private RecyclerView rvRequests;
    private AdminRequestAdapter adapter;
    private SupabaseHelper supabaseHelper;
    private View emptyState;
    private View loadingOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_requests);
        
        rvRequests = findViewById(R.id.rvAdminRequests);
        emptyState = findViewById(R.id.emptyStateAdmin);
        loadingOverlay = findViewById(R.id.loadingLayout); 
        
        rvRequests.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminRequestAdapter(new ArrayList<>(), this::approveRequest, this::rejectRequest);
        rvRequests.setAdapter(adapter);

        supabaseHelper = new SupabaseHelper();
        findViewById(R.id.btnBackAdminReq).setOnClickListener(v -> finish());

        loadPendingRequests();
    }

    private void loadPendingRequests() {
        supabaseHelper.fetchCertificatesByStatus("PENDING", new SupabaseHelper.Callback<List<Certificate>>() {
            @Override
            public void onSuccess(List<Certificate> result) {
                if (result == null || result.isEmpty()) {
                    rvRequests.setVisibility(View.GONE);
                    if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
                } else {
                    rvRequests.setVisibility(View.VISIBLE);
                    if (emptyState != null) emptyState.setVisibility(View.GONE);
                    adapter.updateData(result);
                }
            }
            @Override
            public void onError(Exception e) {
                Toast.makeText(AdminRequestsActivity.this, "Gagal memuat: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void approveRequest(Certificate cert) {
        if (cert.pdfUrl == null || cert.pdfUrl.isEmpty()) {
            Toast.makeText(this, "Error: URL PDF tidak ditemukan", Toast.LENGTH_SHORT).show();
            return;
        }

        if (loadingOverlay != null) loadingOverlay.setVisibility(View.VISIBLE);

        new Thread(() -> {
            try {
                String uuid = cert.id;
                String certId = "CERT-" + uuid.substring(0, 8).toUpperCase();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS+00", Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                String now = sdf.format(new Date());

                String verifyUrl = "https://proj-pens-cert.vercel.app/verify?id=" + uuid;
                String rawData = certId + "|" + cert.getDisplayName() + "|" + now;
                String hash = sha256(rawData);
                String signature = "RSA-SIG-" + hash;

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(cert.pdfUrl).build();
                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) throw new IOException("Gagal download PDF");
                InputStream pdfStream = response.body().byteStream();

                Bitmap qrBitmap = generateQRCode(verifyUrl);

                PdfReader reader = new PdfReader(pdfStream);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                PdfStamper stamper = new PdfStamper(reader, out);
                
                int pageNum = cert.qrPage > 0 ? cert.qrPage : 1;
                PdfContentByte overContent = stamper.getOverContent(pageNum);
                
                float pageWidth = reader.getPageSize(pageNum).getWidth();
                float pageHeight = reader.getPageSize(pageNum).getHeight();
                
                float xPos = cert.qrX * pageWidth;
                float yPos = (1 - cert.qrY) * pageHeight - 80;

                ByteArrayOutputStream qrStream = new ByteArrayOutputStream();
                qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, qrStream);
                Image itextImage = Image.getInstance(qrStream.toByteArray());
                
                itextImage.setAbsolutePosition(xPos, yPos);
                itextImage.scaleToFit(80, 80);
                
                overContent.addImage(itextImage);
                
                stamper.close();
                reader.close();

                byte[] finalPdfBytes = out.toByteArray();

                String newPath = "certificates/" + uuid + ".pdf";
                supabaseHelper.uploadFile("certificates", newPath, finalPdfBytes, "application/pdf", new SupabaseHelper.Callback<String>() {
                    @Override
                    public void onSuccess(String publicUrl) {
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("certificate_number", certId);
                        updates.put("status", "VALID");
                        updates.put("verification_status", "VALID");
                        updates.put("issued_at", now);
                        updates.put("sha256_hash", hash);
                        updates.put("rsa_signature", signature);
                        updates.put("verification_url", verifyUrl);
                        updates.put("pdf_url", publicUrl);
                        updates.put("pdf_storage_path", newPath);
                        updates.put("qr_token", uuid);
                        
                        updates.put("name", cert.getDisplayName());
                        updates.put("role", cert.getDisplayRole());
                        updates.put("event", cert.getDisplayType());
                        updates.put("hash", hash);
                        updates.put("signature", signature);

                        supabaseHelper.updateCertificate(cert.id, updates, new SupabaseHelper.Callback<String>() {
                            @Override
                            public void onSuccess(String res) {
                                runOnUiThread(() -> {
                                    if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
                                    Toast.makeText(AdminRequestsActivity.this, "Berhasil ditandatangani!", Toast.LENGTH_SHORT).show();

                                    // Add notification
                                    NotificationHelper notifHelper = new NotificationHelper(AdminRequestsActivity.this);
                                    notifHelper.addNotification(
                                            "Dokumen Disetujui",
                                            cert.getDisplayType() + " milik " + cert.getDisplayName() + " telah diverifikasi",
                                            "APPROVED",
                                            cert.id
                                    );

                                    loadPendingRequests();
                                });
                            }
                            @Override
                            public void onError(Exception e) { handleError(e); }
                        });
                    }
                    @Override
                    public void onError(Exception e) { handleError(e); }
                });

            } catch (Exception e) { handleError(e); }
        }).start();
    }

    private void handleError(Exception e) {
        runOnUiThread(() -> {
            if (loadingOverlay != null) loadingOverlay.setVisibility(View.GONE);
            Log.e("AdminRequests", "Error: ", e);
            Toast.makeText(AdminRequestsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private Bitmap generateQRCode(String text) throws WriterException {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 512, 512);
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }
        return bmp;
    }

    private void rejectRequest(Certificate cert) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "REJECTED");
        updates.put("verification_status", "REJECTED");
        supabaseHelper.updateCertificate(cert.id, updates, new SupabaseHelper.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                Toast.makeText(AdminRequestsActivity.this, "Pengajuan ditolak", Toast.LENGTH_SHORT).show();

                // Add notification
                NotificationHelper notifHelper = new NotificationHelper(AdminRequestsActivity.this);
                notifHelper.addNotification(
                        "Dokumen Ditolak",
                        cert.getDisplayType() + " milik " + cert.getDisplayName() + " telah ditolak",
                        "REJECTED",
                        cert.id
                );

                loadPendingRequests();
            }
            @Override
            public void onError(Exception e) {
                Toast.makeText(AdminRequestsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String sha256(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(base.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) { return "error"; }
    }

    private static class AdminRequestAdapter extends RecyclerView.Adapter<AdminRequestAdapter.ViewHolder> {
        private List<Certificate> list;
        private final OnActionListener approveListener;
        private final OnActionListener rejectListener;

        interface OnActionListener { void onAction(Certificate cert); }

        AdminRequestAdapter(List<Certificate> list, OnActionListener approve, OnActionListener reject) {
            this.list = list;
            this.approveListener = approve;
            this.rejectListener = reject;
        }

        void updateData(List<Certificate> newList) {
            this.list = newList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_request, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Certificate cert = list.get(position);
            holder.tvDocType.setText(cert.getDisplayType());
            holder.tvName.setText(cert.getDisplayName());
            
            // Tampilkan detail absen jika ada
            if (cert.subjectName != null && !cert.subjectName.isEmpty()) {
                holder.layoutAbsenDetail.setVisibility(View.VISIBLE);
                holder.tvMatkul.setText("Matkul: " + cert.subjectName);
                holder.tvTanggal.setText("Tanggal: " + (cert.subjectDate != null ? cert.subjectDate : "-"));
            } else {
                holder.layoutAbsenDetail.setVisibility(View.GONE);
            }

            if (cert.targetSigner != null && !cert.targetSigner.isEmpty()) {
                holder.tvTargetSigner.setText("Tujuan: " + cert.targetSigner);
                holder.tvTargetSigner.setVisibility(View.VISIBLE);
            } else {
                holder.tvTargetSigner.setVisibility(View.GONE);
            }

            holder.btnApprove.setOnClickListener(v -> approveListener.onAction(cert));
            holder.btnReject.setOnClickListener(v -> rejectListener.onAction(cert));
        }

        @Override
        public int getItemCount() { return list.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvDocType, tvName, tvTargetSigner, tvMatkul, tvTanggal;
            LinearLayout layoutAbsenDetail;
            Button btnApprove, btnReject;
            ViewHolder(View itemView) {
                super(itemView);
                tvDocType = itemView.findViewById(R.id.tvAdminDocType);
                tvName = itemView.findViewById(R.id.tvAdminName);
                tvTargetSigner = itemView.findViewById(R.id.tvTargetSigner);
                layoutAbsenDetail = itemView.findViewById(R.id.layoutAbsenDetail);
                tvMatkul = itemView.findViewById(R.id.tvAdminMatkul);
                tvTanggal = itemView.findViewById(R.id.tvAdminTanggal);
                btnApprove = itemView.findViewById(R.id.btnApprove);
                btnReject = itemView.findViewById(R.id.btnReject);
            }
        }
    }
}
