package com.example.penscert;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class VerifyInputActivity extends AppCompatActivity {

    private EditText etDocId;
    private View loadingLayout;

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(
            new ScanContract(),
            result -> {
                if (result.getContents() != null) {
                    etDocId.setText(result.getContents());
                    doVerify(result.getContents());
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_verify_input);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightStatusBars(false);

        etDocId = findViewById(R.id.etDocId);
        loadingLayout = findViewById(R.id.loadingLayout);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        Button btnCek = findViewById(R.id.btnCek);
        btnCek.setOnClickListener(v -> {
            String id = etDocId.getText().toString().trim();
            if (id.isEmpty()) {
                etDocId.setError("ID dokumen tidak boleh kosong");
                return;
            }
            doVerify(id);
        });

        Button btnScanQr = findViewById(R.id.btnScanQr);
        btnScanQr.setOnClickListener(v -> {
            ScanOptions options = new ScanOptions();
            options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
            options.setPrompt("Arahkan ke QR Code");
            options.setCameraId(0);
            options.setBeepEnabled(false);
            options.setOrientationLocked(false);
            options.setCaptureActivity(CaptureActivityPortrait.class);
            barcodeLauncher.launch(options);
        });
    }

    private void doVerify(String docId) {
        loadingLayout.setVisibility(View.VISIBLE);

        SupabaseHelper supabaseHelper = new SupabaseHelper();
        supabaseHelper.fetchCertificate(docId, new SupabaseHelper.Callback<Certificate>() {
            @Override
            public void onSuccess(Certificate cert) {
                loadingLayout.setVisibility(View.GONE);
                Intent intent = new Intent(VerifyInputActivity.this, VerificationResultActivity.class);
                intent.putExtra("CERT_ID", cert.id);
                intent.putExtra("STATUS", cert.status);
                intent.putExtra("HASH", cert.hash != null ? cert.hash : cert.sha256Hash);
                intent.putExtra("STORAGE_URL", cert.pdfUrl != null ? cert.pdfUrl : "");
                intent.putExtra("DISPLAY_NAME", cert.getDisplayName());
                intent.putExtra("DISPLAY_ID", cert.getDisplayId());
                intent.putExtra("DISPLAY_TYPE", cert.getDisplayType());
                intent.putExtra("TARGET_SIGNER", cert.targetSigner);
                intent.putExtra("SUBJECT_NAME", cert.subjectName);
                startActivity(intent);
            }

            @Override
            public void onError(Exception e) {
                loadingLayout.setVisibility(View.GONE);
                Toast.makeText(VerifyInputActivity.this,
                        "Dokumen tidak ditemukan: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
