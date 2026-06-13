package com.example.penscert;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DocumentsFragment extends Fragment {

    private EditText etSearch;
    private TabLayout tabLayout;
    private RecyclerView rvDocuments;
    private View emptyState;
    private TextView tvDocCount, tvSortLabel;
    private DocumentAdapter adapter;
    private SupabaseHelper supabaseHelper;

    private List<Certificate> allCertificates = new ArrayList<>();
    private String currentStatusFilter = null; // null = all
    private String currentSearchQuery = "";
    private boolean sortAscending = false; // false = newest first (default)

    private final String[] tabTitles = {"Semua", "Menunggu", "Tervalidasi", "Ditolak"};
    private final String[] statusFilters = {null, "PENDING", "VALID", "REJECTED"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_documents, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etSearch = view.findViewById(R.id.etSearch);
        tabLayout = view.findViewById(R.id.tabLayout);
        rvDocuments = view.findViewById(R.id.rvDocuments);
        emptyState = view.findViewById(R.id.emptyState);
        tvDocCount = view.findViewById(R.id.tvDocCount);
        tvSortLabel = view.findViewById(R.id.tvSortLabel);

        supabaseHelper = new SupabaseHelper();

        // Sort button toggle
        view.findViewById(R.id.btnSort).setOnClickListener(v -> {
            sortAscending = !sortAscending;
            tvSortLabel.setText(sortAscending ? "Terlama" : "Terbaru");
            filterAndDisplay();
        });

        // Setup tabs
        for (String title : tabTitles) {
            tabLayout.addTab(tabLayout.newTab().setText(title));
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentStatusFilter = statusFilters[tab.getPosition()];
                filterAndDisplay();
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Setup search
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().toLowerCase(Locale.getDefault());
                filterAndDisplay();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Setup RecyclerView
        rvDocuments.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new DocumentAdapter(new ArrayList<>(), cert -> {
            Intent intent = new Intent(requireContext(), DocumentDetailActivity.class);
            intent.putExtra("CERT_ID", cert.id);
            startActivity(intent);
        });
        rvDocuments.setAdapter(adapter);

        loadDocuments();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (supabaseHelper != null) {
            loadDocuments();
        }
    }

    private void loadDocuments() {
        supabaseHelper.fetchCertificatesByStatus(null, new SupabaseHelper.Callback<List<Certificate>>() {
            @Override
            public void onSuccess(List<Certificate> result) {
                if (isAdded()) {
                    allCertificates = result != null ? result : new ArrayList<>();
                    filterAndDisplay();
                }
            }
            @Override
            public void onError(Exception e) {
                if (isAdded()) {
                    showEmptyState(true);
                    Toast.makeText(requireContext(), "Gagal memuat data", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void filterAndDisplay() {
        List<Certificate> filtered = new ArrayList<>();
        for (Certificate cert : allCertificates) {
            // Status filter
            if (currentStatusFilter != null) {
                String certStatus = cert.status != null ? cert.status.toUpperCase() : "PENDING";
                if ("REJECTED".equals(currentStatusFilter)) {
                    if (!"REJECTED".equals(certStatus) && !"REVOKED".equals(certStatus)) continue;
                } else if (!currentStatusFilter.equals(certStatus)) {
                    continue;
                }
            }

            // Search filter
            if (!currentSearchQuery.isEmpty()) {
                boolean matches = false;
                if (cert.getDisplayName().toLowerCase().contains(currentSearchQuery)) matches = true;
                if (cert.getDisplayType().toLowerCase().contains(currentSearchQuery)) matches = true;
                if (cert.getDisplayId().toLowerCase().contains(currentSearchQuery)) matches = true;
                if (cert.targetSigner != null && cert.targetSigner.toLowerCase().contains(currentSearchQuery)) matches = true;
                if (!matches) continue;
            }

            filtered.add(cert);
        }

        // Sort by date
        if (sortAscending) {
            java.util.Collections.sort(filtered, (a, b) -> {
                String dateA = a.issuedAt != null ? a.issuedAt : "";
                String dateB = b.issuedAt != null ? b.issuedAt : "";
                return dateA.compareTo(dateB);
            });
        } else {
            java.util.Collections.sort(filtered, (a, b) -> {
                String dateA = a.issuedAt != null ? a.issuedAt : "";
                String dateB = b.issuedAt != null ? b.issuedAt : "";
                return dateB.compareTo(dateA);
            });
        }

        // Update count
        tvDocCount.setText("Menampilkan " + filtered.size() + " dokumen");

        showEmptyState(filtered.isEmpty());
        adapter.updateData(filtered);
    }

    private void showEmptyState(boolean isEmpty) {
        if (emptyState != null) emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvDocuments.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    // --- Document Adapter ---
    private static class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.ViewHolder> {
        private List<Certificate> list;
        private final OnItemClickListener listener;

        interface OnItemClickListener {
            void onItemClick(Certificate cert);
        }

        DocumentAdapter(List<Certificate> list, OnItemClickListener listener) {
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
            holder.tvStatus.setText(cert.getStatusLabel());

            // Date
            String dateText = cert.issuedAt != null ? cert.issuedAt : "Tanggal tidak diketahui";
            if (dateText.length() > 16) dateText = dateText.substring(0, 16);
            holder.tvDate.setText(dateText);

            // Status chip coloring
            String status = cert.status != null ? cert.status.toUpperCase() : "PENDING";
            if ("VALID".equals(status)) {
                holder.statusChip.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor("#D1FAE5")));
                holder.tvStatus.setTextColor(Color.parseColor("#059669"));
                holder.iconContainer.setBackgroundResource(R.drawable.menu_icon_bg_success);
                holder.ivDocIcon.setColorFilter(Color.parseColor("#059669"));
            } else if ("REJECTED".equals(status) || "REVOKED".equals(status)) {
                holder.statusChip.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor("#FEE2E2")));
                holder.tvStatus.setTextColor(Color.parseColor("#EF4444"));
                holder.iconContainer.setBackgroundResource(R.drawable.menu_icon_bg_destructive);
                holder.ivDocIcon.setColorFilter(Color.parseColor("#EF4444"));
            } else {
                holder.statusChip.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor("#FEF3C7")));
                holder.tvStatus.setTextColor(Color.parseColor("#D97706"));
                holder.iconContainer.setBackgroundResource(R.drawable.menu_icon_bg_warning);
                holder.ivDocIcon.setColorFilter(Color.parseColor("#D97706"));
            }

            // Extra detail section
            if (cert.targetSigner != null && !cert.targetSigner.isEmpty()) {
                holder.layoutExtra.setVisibility(View.VISIBLE);
                holder.tvExtra1.setText("TTD: " + cert.targetSigner);
                holder.tvExtra2.setText(cert.subjectName != null ? "Matkul: " + cert.subjectName : "");
            } else if (cert.subjectName != null && !cert.subjectName.isEmpty()) {
                holder.layoutExtra.setVisibility(View.VISIBLE);
                holder.tvExtra1.setText("Matkul: " + cert.subjectName);
                holder.tvExtra2.setText(cert.subjectDate != null ? "Tanggal: " + cert.subjectDate : "");
            } else {
                holder.layoutExtra.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(v -> listener.onItemClick(cert));
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
