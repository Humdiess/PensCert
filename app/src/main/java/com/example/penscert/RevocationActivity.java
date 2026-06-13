package com.example.penscert;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RevocationActivity extends AppCompatActivity {

    private EditText etSearch;
    private RecyclerView rvDocuments;
    private View emptyState, loadingLayout;
    private RevocationAdapter adapter;
    private SupabaseHelper supabaseHelper;
    private List<Certificate> allCerts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_revocation);

        etSearch = findViewById(R.id.etSearch);
        rvDocuments = findViewById(R.id.rvDocuments);
        emptyState = findViewById(R.id.emptyState);
        loadingLayout = findViewById(R.id.loadingLayout);

        supabaseHelper = new SupabaseHelper();

        rvDocuments.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RevocationAdapter(new ArrayList<>(), this::showRevokeConfirmation);
        rvDocuments.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterDocuments(s.toString().toLowerCase(Locale.getDefault()));
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        loadDocuments();
    }

    private void loadDocuments() {
        if (loadingLayout != null) loadingLayout.setVisibility(View.VISIBLE);

        supabaseHelper.fetchCertificatesByStatus("VALID", new SupabaseHelper.Callback<List<Certificate>>() {
            @Override
            public void onSuccess(List<Certificate> result) {
                if (loadingLayout != null) loadingLayout.setVisibility(View.GONE);
                allCerts = result != null ? result : new ArrayList<>();
                filterDocuments("");
            }

            @Override
            public void onError(Exception e) {
                if (loadingLayout != null) loadingLayout.setVisibility(View.GONE);
                Toast.makeText(RevocationActivity.this, "Gagal memuat: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterDocuments(String query) {
        List<Certificate> filtered = new ArrayList<>();
        for (Certificate cert : allCerts) {
            if (query.isEmpty() ||
                    cert.getDisplayName().toLowerCase().contains(query) ||
                    cert.getDisplayType().toLowerCase().contains(query) ||
                    cert.getDisplayId().toLowerCase().contains(query)) {
                filtered.add(cert);
            }
        }
        if (filtered.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            rvDocuments.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvDocuments.setVisibility(View.VISIBLE);
            adapter.updateData(filtered);
        }
    }

    private void showRevokeConfirmation(Certificate cert) {
        new AlertDialog.Builder(this)
                .setTitle("Cabut Dokumen")
                .setMessage("Apakah Anda yakin ingin mencabut dokumen \"" + cert.getDisplayType() +
                        "\" milik " + cert.getDisplayName() + "?\n\nTindakan ini tidak dapat dibatalkan.")
                .setPositiveButton("Ya, Cabut", (dialog, which) -> revokeCertificate(cert))
                .setNegativeButton("Batal", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void revokeCertificate(Certificate cert) {
        if (loadingLayout != null) loadingLayout.setVisibility(View.VISIBLE);

        supabaseHelper.revokeCertificate(cert.id, new SupabaseHelper.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                if (loadingLayout != null) loadingLayout.setVisibility(View.GONE);
                Toast.makeText(RevocationActivity.this, "Dokumen berhasil dicabut", Toast.LENGTH_SHORT).show();

                // Add notification
                NotificationHelper notifHelper = new NotificationHelper(RevocationActivity.this);
                notifHelper.addNotification(
                        "Dokumen Dicabut",
                        cert.getDisplayType() + " milik " + cert.getDisplayName() + " telah dicabut",
                        "REJECTED",
                        cert.id
                );

                loadDocuments();
            }

            @Override
            public void onError(Exception e) {
                if (loadingLayout != null) loadingLayout.setVisibility(View.GONE);
                Toast.makeText(RevocationActivity.this, "Gagal mencabut: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- Adapter ---
    private static class RevocationAdapter extends RecyclerView.Adapter<RevocationAdapter.ViewHolder> {
        private List<Certificate> list;
        private final OnRevokeListener listener;

        interface OnRevokeListener {
            void onRevoke(Certificate cert);
        }

        RevocationAdapter(List<Certificate> list, OnRevokeListener listener) {
            this.list = list;
            this.listener = listener;
        }

        void updateData(List<Certificate> newList) {
            this.list = newList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_document, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Certificate cert = list.get(position);

            holder.tvDocType.setText(cert.getDisplayType());
            holder.tvDocName.setText(cert.getDisplayName());
            holder.tvStatus.setText("CABUT");
            holder.tvStatus.setTextColor(Color.parseColor("#EF4444"));
            holder.statusChip.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor("#FEE2E2")));

            holder.iconContainer.setBackgroundResource(R.drawable.menu_icon_bg_success);
            holder.ivDocIcon.setColorFilter(Color.parseColor("#059669"));

            String dateText = cert.issuedAt != null ? cert.issuedAt : "-";
            if (dateText.length() > 16) dateText = dateText.substring(0, 16);
            holder.tvDate.setText(dateText);

            if (cert.subjectName != null && !cert.subjectName.isEmpty()) {
                holder.layoutExtra.setVisibility(View.VISIBLE);
                holder.tvExtra1.setText("Matkul: " + cert.subjectName);
                holder.tvExtra2.setText(cert.subjectDate != null ? "Tanggal: " + cert.subjectDate : "");
            } else {
                holder.layoutExtra.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(v -> listener.onRevoke(cert));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvDocType, tvDocName, tvStatus, tvDate, tvExtra1, tvExtra2;
            MaterialCardView statusChip;
            FrameLayout iconContainer;
            ImageView ivDocIcon;
            LinearLayout layoutExtra;

            ViewHolder(View itemView) {
                super(itemView);
                tvDocType = itemView.findViewById(R.id.tvDocType);
                tvDocName = itemView.findViewById(R.id.tvDocName);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                tvDate = itemView.findViewById(R.id.tvDate);
                statusChip = itemView.findViewById(R.id.statusChip);
                iconContainer = itemView.findViewById(R.id.iconContainer);
                ivDocIcon = itemView.findViewById(R.id.ivDocIcon);
                layoutExtra = itemView.findViewById(R.id.layoutExtra);
                tvExtra1 = itemView.findViewById(R.id.tvExtra1);
                tvExtra2 = itemView.findViewById(R.id.tvExtra2);
            }
        }
    }
}
