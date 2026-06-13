package com.example.penscert;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class MyRequestsActivity extends AppCompatActivity {

    private RecyclerView rvMyRequests;
    private View emptyStateLayout;
    private RequestAdapter adapter;
    private SupabaseHelper supabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_requests);

        rvMyRequests = findViewById(R.id.rvMyRequests);
        emptyStateLayout = findViewById(R.id.emptyState);
        
        rvMyRequests.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RequestAdapter(new ArrayList<>(), this::onItemClicked);
        rvMyRequests.setAdapter(adapter);

        supabaseHelper = new SupabaseHelper();
        
        findViewById(R.id.btnBackMyReq).setOnClickListener(v -> finish());

        loadRequests();
    }

    private void loadRequests() {
        supabaseHelper.fetchCertificatesByStatus(null, new SupabaseHelper.Callback<List<Certificate>>() {
            @Override
            public void onSuccess(List<Certificate> result) {
                if (result == null || result.isEmpty()) {
                    showEmptyState(true);
                } else {
                    showEmptyState(false);
                    adapter.updateData(result);
                }
            }

            @Override
            public void onError(Exception e) {
                showEmptyState(true);
                Toast.makeText(MyRequestsActivity.this, "Gagal memuat data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onItemClicked(Certificate cert) {
        if (cert.pdfUrl != null && !cert.pdfUrl.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(cert.pdfUrl));
            startActivity(intent);
        } else {
            Toast.makeText(this, "File PDF belum tersedia", Toast.LENGTH_SHORT).show();
        }
    }

    private void showEmptyState(boolean isEmpty) {
        if (emptyStateLayout != null) {
            emptyStateLayout.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }
        rvMyRequests.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private static class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {
        private List<Certificate> list;
        private final OnItemClickListener listener;

        interface OnItemClickListener { void onItemClick(Certificate cert); }

        RequestAdapter(List<Certificate> list, OnItemClickListener listener) {
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_signature_request, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Certificate cert = list.get(position);
            
            holder.tvDocType.setText(cert.getDisplayType());
            holder.tvName.setText("Subjek: " + cert.getDisplayName());
            holder.tvStatus.setText(cert.getStatusLabel());

            // Tampilkan detail absen jika tersedia
            if (cert.subjectName != null && !cert.subjectName.isEmpty()) {
                holder.layoutAbsenDetail.setVisibility(View.VISIBLE);
                holder.tvMatkul.setText("Matkul: " + cert.subjectName);
                holder.tvTanggal.setText("Tanggal: " + (cert.subjectDate != null ? cert.subjectDate : "-"));
            } else {
                holder.layoutAbsenDetail.setVisibility(View.GONE);
            }

            String status = cert.status != null ? cert.status.toUpperCase() : "PENDING";
            
            if ("VALID".equals(status)) {
                holder.statusChip.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor("#D1FAE5")));
                holder.tvStatus.setTextColor(Color.parseColor("#10B981"));
            } else if ("REJECTED".equals(status) || "REVOKED".equals(status)) {
                holder.statusChip.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor("#FEE2E2")));
                holder.tvStatus.setTextColor(Color.parseColor("#EF4444"));
            } else {
                holder.statusChip.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor("#F1F5F9")));
                holder.tvStatus.setTextColor(Color.parseColor("#64748B"));
            }

            holder.itemView.setOnClickListener(v -> listener.onItemClick(cert));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvDocType, tvName, tvStatus, tvMatkul, tvTanggal;
            MaterialCardView statusChip;
            LinearLayout layoutAbsenDetail;

            ViewHolder(View itemView) {
                super(itemView);
                tvDocType = itemView.findViewById(R.id.tvItemDocType);
                tvName = itemView.findViewById(R.id.tvItemName);
                tvStatus = itemView.findViewById(R.id.tvItemStatus);
                statusChip = itemView.findViewById(R.id.statusChip);
                layoutAbsenDetail = itemView.findViewById(R.id.layoutItemAbsenDetail);
                tvMatkul = itemView.findViewById(R.id.tvItemMatkul);
                tvTanggal = itemView.findViewById(R.id.tvItemTanggal);
            }
        }
    }
}
