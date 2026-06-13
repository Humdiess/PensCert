package com.example.penscert;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

public class ProfileFragment extends Fragment {

    private TextView tvAvatarInitial, tvProfileName, tvProfileRole, tvProfileId;
    private TextView tvInfoName, tvInfoRole, tvSecurityStatus;
    private LinearLayout btnAbout, btnHelp, btnShare, btnClearCache;
    private Button btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvAvatarInitial = view.findViewById(R.id.tvAvatarInitial);
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileRole = view.findViewById(R.id.tvProfileRole);
        tvProfileId = view.findViewById(R.id.tvProfileId);
        tvInfoName = view.findViewById(R.id.tvInfoName);
        tvInfoRole = view.findViewById(R.id.tvInfoRole);
        tvSecurityStatus = view.findViewById(R.id.tvSecurityStatus);
        btnAbout = view.findViewById(R.id.btnAbout);
        btnHelp = view.findViewById(R.id.btnHelp);
        btnShare = view.findViewById(R.id.btnShare);
        btnClearCache = view.findViewById(R.id.btnClearCache);
        btnLogout = view.findViewById(R.id.btnLogout);

        loadUserData();

        btnAbout.setOnClickListener(v -> showAboutDialog());
        btnHelp.setOnClickListener(v -> showHelpDialog());
        btnShare.setOnClickListener(v -> shareApp());
        btnClearCache.setOnClickListener(v -> clearCache());
        btnLogout.setOnClickListener(v -> showLogoutConfirmation());
    }

    private void loadUserData() {
        SharedPreferences prefs = requireContext().getSharedPreferences("AppPrefs", getContext().MODE_PRIVATE);
        String userName = prefs.getString("user_name", "Pengguna");
        String role = prefs.getString("user_role", "Mahasiswa");
        String userId = prefs.getString("user_id", "00000000");

        // Set avatar initial
        String initial = userName.length() > 0 ? String.valueOf(userName.charAt(0)).toUpperCase() : "U";
        tvAvatarInitial.setText(initial);

        // Set profile header
        tvProfileName.setText(userName);
        tvProfileRole.setText(role);
        String idLabel = "Dosen".equals(role) ? "NIP: " : "NRP: ";
        tvProfileId.setText(idLabel + userId);

        // Set info card
        tvInfoName.setText(userName);
        tvInfoRole.setText(role);
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Tentang PensCert")
                .setMessage("PensCert adalah aplikasi keamanan dokumen digital untuk lingkungan " +
                        "Politeknik Elektronika Negeri Surabaya (PENS).\n\n" +
                        "Aplikasi ini menyediakan fitur verifikasi dokumen, tanda tangan digital, " +
                        "dan manajemen sertifikat secara aman dan terintegrasi.\n\n" +
                        "Versi 2.0.0\n" +
                        "© 2024 PENS - Workshop Pemrograman 2")
                .setPositiveButton("Tutup", null)
                .show();
    }

    private void showHelpDialog() {
        String help = "📞 Bantuan & Dukungan\n\n" +
                "Jika Anda mengalami masalah dengan aplikasi PENS Cert, " +
                "silakan hubungi:\n\n" +
                "📧 Email: support@pens.ac.id\n" +
                "📱 WhatsApp: +62 812-3456-7890\n" +
                "🌐 Website: pens.ac.id/penscert\n\n" +
                "Jam Operasional:\n" +
                "Senin - Jumat, 08:00 - 16:00 WIB\n\n" +
                "Tim support kami akan merespons dalam 1x24 jam.";
        new AlertDialog.Builder(requireContext())
                .setTitle("Bantuan & Dukungan")
                .setMessage(help)
                .setPositiveButton("Tutup", null)
                .show();
    }

    private void shareApp() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "PENS Cert - Keamanan Dokumen Digital");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                "🔐 Gunakan PENS Cert untuk mengamankan dan memverifikasi dokumen digital Anda!\n\n" +
                "Fitur:\n" +
                "✅ Verifikasi dokumen via QR Code\n" +
                "✅ Tanda tangan digital\n" +
                "✅ Manajemen sertifikat\n\n" +
                "Download di: pens.ac.id/penscert");
        startActivity(Intent.createChooser(shareIntent, "Bagikan melalui"));
    }

    private void clearCache() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Bersihkan Cache")
                .setMessage("Ini akan menghapus data cache aplikasi. Data akun dan dokumen tidak akan terpengaruh.")
                .setPositiveButton("Bersihkan", (dialog, which) -> {
                    try {
                        // Clear cache directory
                        java.io.File cacheDir = requireContext().getCacheDir();
                        deleteDir(cacheDir);
                        Toast.makeText(requireContext(), "Cache berhasil dibersihkan", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), "Gagal membersihkan cache", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private boolean deleteDir(java.io.File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    if (!deleteDir(new java.io.File(dir, child))) return false;
                }
            }
        }
        return dir != null && dir.delete();
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Keluar Akun")
                .setMessage("Apakah Anda yakin ingin keluar dari akun ini?")
                .setPositiveButton("Keluar", (dialog, which) -> performLogout())
                .setNegativeButton("Batal", null)
                .show();
    }

    private void performLogout() {
        SharedPreferences prefs = requireContext().getSharedPreferences("AppPrefs", getContext().MODE_PRIVATE);
        prefs.edit()
                .putBoolean("is_logged_in", false)
                .remove("user_id")
                .remove("user_role")
                .remove("user_name")
                .apply();

        // Clear notifications
        new NotificationHelper(requireContext()).clearAll();

        Toast.makeText(requireContext(), "Berhasil keluar", Toast.LENGTH_SHORT).show();

        // Go back to login
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}
