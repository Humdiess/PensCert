package com.example.penscert;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private TextView tvUserName, tvStatTotal, tvStatValid, tvStatPending;
    private RecyclerView rvRecentActivity;
    private RecentActivityAdapter activityAdapter;
    private SupabaseHelper supabaseHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvUserName = view.findViewById(R.id.tvUserName);
        tvStatTotal = view.findViewById(R.id.tvStatTotal);
        tvStatValid = view.findViewById(R.id.tvStatValid);
        tvStatPending = view.findViewById(R.id.tvStatPending);
        rvRecentActivity = view.findViewById(R.id.rvRecentActivity);

        supabaseHelper = new SupabaseHelper();

        // Setup user greeting
        SharedPreferences prefs = requireContext().getSharedPreferences("AppPrefs", getContext().MODE_PRIVATE);
        String userName = prefs.getString("user_name", "Pengguna");
        String role = prefs.getString("user_role", "Mahasiswa");
        tvUserName.setText(userName);

        // Setup recent activity
        rvRecentActivity.setLayoutManager(new LinearLayoutManager(requireContext()));
        activityAdapter = new RecentActivityAdapter(new ArrayList<>(), cert -> {
            Intent intent = new Intent(requireContext(), DocumentDetailActivity.class);
            intent.putExtra("CERT_ID", cert.id);
            startActivity(intent);
        });
        rvRecentActivity.setAdapter(activityAdapter);

        // Quick actions
        view.findViewById(R.id.btnScan).setOnClickListener(v -> launchQrScanner());
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
            // Switch to Documents tab
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).switchToDocuments();
            }
        });

        loadStats();
        loadRecentActivity();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (supabaseHelper != null) {
            loadStats();
            loadRecentActivity();
        }
    }

    private void loadStats() {
        supabaseHelper.getCertificateCount(null, new SupabaseHelper.Callback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                if (isAdded()) tvStatTotal.setText(String.valueOf(result));
            }
            @Override
            public void onError(Exception e) {
                if (isAdded()) tvStatTotal.setText("0");
            }
        });

        supabaseHelper.getCertificateCount("VALID", new SupabaseHelper.Callback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                if (isAdded()) tvStatValid.setText(String.valueOf(result));
            }
            @Override
            public void onError(Exception e) {
                if (isAdded()) tvStatValid.setText("0");
            }
        });

        supabaseHelper.getCertificateCount("PENDING", new SupabaseHelper.Callback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                if (isAdded()) tvStatPending.setText(String.valueOf(result));
            }
            @Override
            public void onError(Exception e) {
                if (isAdded()) tvStatPending.setText("0");
            }
        });
    }

    private void loadRecentActivity() {
        supabaseHelper.fetchRecentCertificates(5, new SupabaseHelper.Callback<List<Certificate>>() {
            @Override
            public void onSuccess(List<Certificate> result) {
                if (isAdded() && result != null) {
                    activityAdapter.updateData(result);
                }
            }
            @Override
            public void onError(Exception e) {
                // Silently fail for recent activity
            }
        });
    }

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(
            new ScanContract(), result -> {
                if (result.getContents() != null) {
                    Intent intent = new Intent(requireContext(), VerificationResultActivity.class);
                    intent.putExtra("SCAN_RESULT", result.getContents());
                    startActivity(intent);
                }
            });

    private void launchQrScanner() {
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setPrompt("Arahkan kamera ke QR Code");
        options.setCameraId(0);
        options.setBeepEnabled(false);
        options.setOrientationLocked(true);
        options.setCaptureActivity(ScanQrActivity.class);
        barcodeLauncher.launch(options);
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
