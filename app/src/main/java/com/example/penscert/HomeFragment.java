package com.example.penscert;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HomeFragment extends Fragment {

    private TextView tvGreeting, tvUserName, tvStatTotal, tvStatValid, tvStatPending;
    private TextView tvVerifiedPercent, tvLastActivity, tvSecurityTip;
    private TextView tvAchievementTitle, tvAchievementDesc, tvAchievementIcon;
    private TextView tvPendingDesc, tvPendingBadge;
    private ProgressBar progressAchievement;
    private EditText etQuickSearch;
    private RecyclerView rvRecentActivity;
    private RecentActivityAdapter activityAdapter;
    private SupabaseHelper supabaseHelper;

    private final String[] securityTips = {
            "Selalu periksa QR Code pada dokumen resmi untuk memastikan keasliannya.",
            "Jangan bagikan dokumen digital Anda kepada pihak yang tidak berwenang.",
            "Verifikasi dokumen secara berkala untuk memastikan statusnya masih valid.",
            "Simpan salinan digital dokumen Anda di tempat yang aman dan terenkripsi.",
            "Perhatikan tanggal kadaluarsa pada setiap dokumen digital yang Anda miliki.",
            "Laporkan segera jika menemukan dokumen yang mencurigakan atau dipalsukan."
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvGreeting = view.findViewById(R.id.tvGreeting);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvStatTotal = view.findViewById(R.id.tvStatTotal);
        tvStatValid = view.findViewById(R.id.tvStatValid);
        tvStatPending = view.findViewById(R.id.tvStatPending);
        tvVerifiedPercent = view.findViewById(R.id.tvVerifiedPercent);
        tvLastActivity = view.findViewById(R.id.tvLastActivity);
        tvSecurityTip = view.findViewById(R.id.tvSecurityTip);
        tvAchievementTitle = view.findViewById(R.id.tvAchievementTitle);
        tvAchievementDesc = view.findViewById(R.id.tvAchievementDesc);
        tvAchievementIcon = view.findViewById(R.id.tvAchievementIcon);
        tvPendingDesc = view.findViewById(R.id.tvPendingDesc);
        tvPendingBadge = view.findViewById(R.id.tvPendingBadge);
        progressAchievement = view.findViewById(R.id.progressAchievement);
        etQuickSearch = view.findViewById(R.id.etQuickSearch);
        rvRecentActivity = view.findViewById(R.id.rvRecentActivity);

        supabaseHelper = new SupabaseHelper();

        // Time-based greeting
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour < 10) greeting = "Selamat Pagi,";
        else if (hour < 15) greeting = "Selamat Siang,";
        else if (hour < 18) greeting = "Selamat Sore,";
        else greeting = "Selamat Malam,";
        tvGreeting.setText(greeting);

        // Setup user greeting
        SharedPreferences prefs = requireContext().getSharedPreferences("AppPrefs", getContext().MODE_PRIVATE);
        String userName = prefs.getString("user_name", "Pengguna");
        tvUserName.setText(userName);

        // Random security tip
        int tipIndex = (int) (Math.random() * securityTips.length);
        tvSecurityTip.setText(securityTips[tipIndex]);

        // Setup recent activity
        rvRecentActivity.setLayoutManager(new LinearLayoutManager(requireContext()));
        activityAdapter = new RecentActivityAdapter(new ArrayList<>(), cert -> {
            Intent intent = new Intent(requireContext(), DocumentDetailActivity.class);
            intent.putExtra("CERT_ID", cert.id);
            startActivity(intent);
        });
        rvRecentActivity.setAdapter(activityAdapter);

        // Quick actions
        view.findViewById(R.id.btnSearch).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), VerifyInputActivity.class));
        });
        view.findViewById(R.id.btnRequest).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), RequestSignatureActivity.class));
        });
        view.findViewById(R.id.btnAdmin).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), AdminActivity.class));
        });
        view.findViewById(R.id.btnVerify).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), VerifyInputActivity.class));
        });
        view.findViewById(R.id.btnSeeAll).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).switchToDocuments();
            }
        });

        // Quick search bar
        view.findViewById(R.id.btnQuickSearch).setOnClickListener(v -> performQuickSearch());
        etQuickSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performQuickSearch();
                return true;
            }
            return false;
        });

        // Panduan (Guide) button
        view.findViewById(R.id.btnGuide).setOnClickListener(v -> showGuideDialog());

        // Pending shortcut card - click to go to Documents with Pending filter
        view.findViewById(R.id.cardPending).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).switchToDocuments();
            }
        });

        loadStats();
        loadRecentActivity();
    }

    private void performQuickSearch() {
        String query = etQuickSearch.getText().toString().trim();
        if (query.isEmpty()) {
            etQuickSearch.setError("Masukkan ID dokumen");
            return;
        }
        Intent intent = new Intent(requireContext(), VerificationResultActivity.class);
        intent.putExtra("CERT_ID", query);
        startActivity(intent);
    }

    private void showGuideDialog() {
        String guide = "📋 Cara Menggunakan PENS Cert:\n\n" +
                "1. Upload Dokumen\n" +
                "   Ajukan dokumen digital melalui menu 'Ajukan TTD'.\n\n" +
                "2. Verifikasi QR Code\n" +
                "   Gunakan tombol scan QR di navbar untuk memverifikasi keaslian dokumen.\n\n" +
                "3. Cek via ID\n" +
                "   Masukkan ID dokumen di menu 'Cek Dokumen' untuk verifikasi manual.\n\n" +
                "4. Kelola Dokumen\n" +
                "   Dosen dapat mengelola dokumen melalui 'Portal Dosen'.\n\n" +
                "5. Pantau Status\n" +
                "   Lihat status dokumen Anda di tab 'Dokumen' atau 'Notifikasi'.";

        new AlertDialog.Builder(requireContext())
                .setTitle("Panduan Penggunaan")
                .setMessage(guide)
                .setPositiveButton("Mengerti", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (supabaseHelper != null) {
            loadStats();
            loadRecentActivity();
        }
    }

    private int lastTotal = 0, lastValid = 0;

    private void loadStats() {
        supabaseHelper.getCertificateCount(null, new SupabaseHelper.Callback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                if (!isAdded()) return;
                lastTotal = result;
                tvStatTotal.setText(String.valueOf(result));
                updateVerifiedPercent();
            }
            @Override
            public void onError(Exception e) {
                if (isAdded()) tvStatTotal.setText("0");
            }
        });

        supabaseHelper.getCertificateCount("VALID", new SupabaseHelper.Callback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                if (!isAdded()) return;
                lastValid = result;
                tvStatValid.setText(String.valueOf(result));
                updateVerifiedPercent();
            }
            @Override
            public void onError(Exception e) {
                if (isAdded()) tvStatValid.setText("0");
            }
        });

        supabaseHelper.getCertificateCount("PENDING", new SupabaseHelper.Callback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                if (!isAdded()) return;
                tvStatPending.setText(String.valueOf(result));
                updatePendingCard(result);
            }
            @Override
            public void onError(Exception e) {
                if (isAdded()) tvStatPending.setText("0");
            }
        });
    }

    private void updateVerifiedPercent() {
        if (lastTotal > 0) {
            int percent = (lastValid * 100) / lastTotal;
            tvVerifiedPercent.setText(percent + "% Terverifikasi");
        } else {
            tvVerifiedPercent.setText("0% Terverifikasi");
        }
        updateAchievement(lastValid);
    }

    private void updateAchievement(int validCount) {
        // Gamification: achievement levels based on verified documents
        String icon, title, desc;
        int progress;
        if (validCount >= 50) {
            icon = "👑"; title = "Master Dokumen"; desc = "50+ dokumen terverifikasi! Luar biasa!";
            progress = 100;
        } else if (validCount >= 20) {
            icon = "💎"; title = "Ahli Verifikasi"; desc = "20+ dokumen terverifikasi! Hebat!";
            progress = 100;
        } else if (validCount >= 10) {
            icon = "⭐"; title = "Verifier Handal"; desc = "10+ dokumen terverifikasi!";
            progress = (validCount * 100) / 20;
        } else if (validCount >= 5) {
            icon = "🏅"; title = "Aktif Verifikasi"; desc = "5+ dokumen terverifikasi! Terus semangat!";
            progress = (validCount * 100) / 10;
        } else if (validCount >= 1) {
            icon = "🏆"; title = "Pemula Digital"; desc = validCount + " dokumen terverifikasi. Ayo tingkatkan!";
            progress = (validCount * 100) / 5;
        } else {
            icon = "🎯"; title = "Mulai Perjalananmu"; desc = "Verifikasi dokumen pertamamu!";
            progress = 0;
        }
        tvAchievementIcon.setText(icon);
        tvAchievementTitle.setText(title);
        tvAchievementDesc.setText(desc);
        progressAchievement.setProgress(progress);
    }

    private void updatePendingCard(int pendingCount) {
        tvPendingBadge.setText(String.valueOf(pendingCount));
        if (pendingCount > 0) {
            tvPendingDesc.setText(pendingCount + " dokumen menunggu approval");
        } else {
            tvPendingDesc.setText("Semua dokumen sudah diproses");
        }
    }

    private void loadRecentActivity() {
        supabaseHelper.fetchRecentCertificates(5, new SupabaseHelper.Callback<List<Certificate>>() {
            @Override
            public void onSuccess(List<Certificate> result) {
                if (isAdded() && result != null) {
                    activityAdapter.updateData(result);
                    // Update last activity info
                    if (!result.isEmpty() && result.get(0).issuedAt != null) {
                        String time = result.get(0).issuedAt;
                        if (time.length() > 16) time = time.substring(0, 16);
                        tvLastActivity.setText("Terakhir: " + time);
                    }
                }
            }
            @Override
            public void onError(Exception e) {
                // Silently fail for recent activity
            }
        });
    }

    // --- Recent Activity Adapter ---
    private static class RecentActivityAdapter extends RecyclerView.Adapter<RecentActivityAdapter.ViewHolder> {
        private List<Certificate> list;
        private final OnItemClickListener listener;

        interface OnItemClickListener {
            void onItemClick(Certificate cert);
        }

        RecentActivityAdapter(List<Certificate> list, OnItemClickListener listener) {
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
                    .inflate(R.layout.item_recent_activity, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Certificate cert = list.get(position);
            holder.tvTitle.setText(cert.getDisplayType());
            holder.tvSubtitle.setText(cert.getDisplayName() + " - " + cert.getStatusLabel());

            String timeText = cert.issuedAt != null ? cert.issuedAt : "Baru";
            if (timeText.length() > 16) timeText = timeText.substring(0, 16);
            holder.tvTime.setText(timeText);

            // Set icon color based on status
            String status = cert.status != null ? cert.status.toUpperCase() : "PENDING";
            int bgRes, iconColor;
            if ("VALID".equals(status)) {
                bgRes = R.drawable.menu_icon_bg_success;
                iconColor = holder.itemView.getContext().getResources().getColor(R.color.success);
            } else if ("REJECTED".equals(status) || "REVOKED".equals(status)) {
                bgRes = R.drawable.menu_icon_bg_destructive;
                iconColor = holder.itemView.getContext().getResources().getColor(R.color.destructive);
            } else {
                bgRes = R.drawable.menu_icon_bg_warning;
                iconColor = holder.itemView.getContext().getResources().getColor(R.color.warning);
            }
            holder.iconContainer.setBackgroundResource(bgRes);
            holder.ivIcon.setColorFilter(iconColor);

            holder.itemView.setOnClickListener(v -> listener.onItemClick(cert));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvSubtitle, tvTime;
            View iconContainer;
            ImageView ivIcon;

            ViewHolder(View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvSubtitle = itemView.findViewById(R.id.tvSubtitle);
                tvTime = itemView.findViewById(R.id.tvTime);
                iconContainer = itemView.findViewById(R.id.iconContainer);
                ivIcon = itemView.findViewById(R.id.ivIcon);
            }
        }
    }
}
