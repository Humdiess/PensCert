package com.example.penscert;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

public class CreateCertificateActivity extends AppCompatActivity {

    private ViewFlipper viewFlipper;
    private LinearProgressIndicator stepProgress;
    private TextView tvStepTitle, tvMainTitle, tvCsvStatus, tvTemplateStatus;
    private TextView tvSumMode, tvSumName, tvSumEventValue;
    private Button btnPrev, btnNext;
    
    private TextInputEditText etName, etRole, etEvent;
    private RadioGroup rgMode;
    private LinearLayout containerSingle, containerBulk;
    private View btnUploadCsv, btnSelectTemplate;
    private ProgressBar progressBar;
    
    private SupabaseHelper supabaseHelper;
    private Uri csvUri, templateUri;
    private String selectedTemplateName = "template.pdf";
    private boolean isBulkMode = false;
    private int currentStep = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_certificate);

        initViews();
        setupLogic();
        updateStepUI();
    }

    private void initViews() {
        viewFlipper = findViewById(R.id.viewFlipper);
        stepProgress = findViewById(R.id.stepProgress);
        tvStepTitle = findViewById(R.id.tvStepTitle);
        tvMainTitle = findViewById(R.id.tvMainTitle);
        
        etName = findViewById(R.id.etName);
        etRole = findViewById(R.id.etRole);
        etEvent = findViewById(R.id.etEvent);
        rgMode = findViewById(R.id.rgMode);
        
        containerSingle = findViewById(R.id.containerSingle);
        containerBulk = findViewById(R.id.containerBulk);
        btnUploadCsv = findViewById(R.id.btnUploadCsv);
        btnSelectTemplate = findViewById(R.id.btnSelectTemplate);
        
        tvCsvStatus = findViewById(R.id.tvCsvStatus);
        tvTemplateStatus = findViewById(R.id.tvTemplateStatus);
        tvSumMode = findViewById(R.id.tvSumMode);
        tvSumName = findViewById(R.id.tvSumName);
        tvSumEventValue = findViewById(R.id.tvSumEventValue);
        
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupLogic() {
        supabaseHelper = new SupabaseHelper();

        rgMode.setOnCheckedChangeListener((group, checkedId) -> {
            isBulkMode = (checkedId == R.id.rbBulk);
            containerSingle.setVisibility(isBulkMode ? View.GONE : View.VISIBLE);
            containerBulk.setVisibility(isBulkMode ? View.VISIBLE : View.GONE);
        });

        btnUploadCsv.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("text/*");
            csvPickerLauncher.launch(intent);
        });

        btnSelectTemplate.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/pdf");
            templatePickerLauncher.launch(intent);
        });

        btnNext.setOnClickListener(v -> handleNext());
        btnPrev.setOnClickListener(v -> handleBack());
    }

    private void handleNext() {
        if (currentStep == 0) {
            if (validateStep1()) goToStep(1);
        } else if (currentStep == 1) {
            if (validateStep2()) goToStep(2);
        } else if (currentStep == 2) {
            if (validateStep3()) processFinal();
        }
    }

    private void handleBack() {
        if (currentStep > 0) goToStep(currentStep - 1);
        else finish();
    }

    private void goToStep(int step) {
        if (step > currentStep) {
            viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_right));
            viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_left));
        } else {
            viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_left));
            viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_right));
        }
        currentStep = step;
        viewFlipper.setDisplayedChild(step);
        updateStepUI();
        if (step == 2) updateSummary();
    }

    private void updateStepUI() {
        switch (currentStep) {
            case 0:
                tvStepTitle.setText("Langkah 1 dari 3");
                tvMainTitle.setText("Mode Penerbitan");
                stepProgress.setProgress(33);
                btnNext.setText("Lanjut");
                break;
            case 1:
                tvStepTitle.setText("Langkah 2 dari 3");
                tvMainTitle.setText("Detail Kegiatan");
                stepProgress.setProgress(66);
                btnNext.setText("Lanjut");
                break;
            case 2:
                tvStepTitle.setText("Langkah 3 dari 3");
                tvMainTitle.setText("Finalisasi");
                stepProgress.setProgress(100);
                btnNext.setText("Terbitkan");
                break;
        }
    }

    private boolean validateStep1() {
        if (isBulkMode) {
            if (csvUri == null) { Toast.makeText(this, "Unggah file CSV dahulu", Toast.LENGTH_SHORT).show(); return false; }
        } else {
            if (etName.getText().toString().isEmpty()) { etName.setError("Nama wajib diisi"); return false; }
        }
        return true;
    }

    private boolean validateStep2() {
        if (etEvent.getText().toString().isEmpty()) { etEvent.setError("Nama kegiatan wajib diisi"); return false; }
        return true;
    }

    private boolean validateStep3() {
        if (templateUri == null) { Toast.makeText(this, "Pilih template PDF dahulu", Toast.LENGTH_SHORT).show(); return false; }
        return true;
    }

    private void updateSummary() {
        tvSumMode.setText(isBulkMode ? "Banyak Peserta (Bulk)" : "Satu Peserta");
        tvSumName.setText(isBulkMode ? "Dari file: List_Peserta.csv" : etName.getText().toString());
        tvSumEventValue.setText(etEvent.getText().toString());
    }

    private final ActivityResultLauncher<Intent> csvPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    csvUri = result.getData().getData();
                    tvCsvStatus.setText("File CSV Siap");
                }
            }
    );

    private final ActivityResultLauncher<Intent> templatePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    templateUri = result.getData().getData();
                    selectedTemplateName = getFileName(templateUri);
                    tvTemplateStatus.setText("Template PDF Siap");
                }
            }
    );

    private void processFinal() {
        if (isBulkMode) {
            Toast.makeText(this, "Memproses data massal...", Toast.LENGTH_SHORT).show();
            finish(); // Mock bulk process
        } else {
            generateSingle();
        }
    }

    private void generateSingle() {
        btnNext.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        String uuid = UUID.randomUUID().toString();
        String remotePath = "certificates/" + uuid + ".pdf";

        try {
            InputStream is = getContentResolver().openInputStream(templateUri);
            byte[] bytes = getBytes(is);
            String fileHash = calculateSHA256(bytes);

            // 1. Upload file PDF ke storage dahulu
            supabaseHelper.uploadFile("certificates", remotePath, bytes, "application/pdf", new SupabaseHelper.Callback<String>() {
                @Override
                public void onSuccess(String publicUrl) {
                    // 2. Jika upload berhasil, baru simpan data ke database
                    saveToDatabase(uuid, publicUrl, fileHash);
                }

                @Override
                public void onError(Exception e) {
                    btnNext.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(CreateCertificateActivity.this, "Gagal upload PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            btnNext.setEnabled(true);
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Gagal membaca file PDF", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveToDatabase(String uuid, String pdfUrl, String fileHash) {
        String certId = "CERT-" + uuid.substring(0, 8).toUpperCase();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS+00", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String now = sdf.format(new Date());

        Certificate cert = new Certificate();
        cert.id = uuid;
        cert.certificateNumber = certId;
        cert.participantName = etName.getText().toString().trim();
        cert.eventName = etEvent.getText().toString().trim();
        cert.participantRole = etRole.getText().toString().trim();
        cert.issuedAt = now;
        
        // Agar muncul di halaman Approval (jika halaman tsb memfilter PENDING)
        // Jika dosen ingin langsung VALID, tetap pakai VALID tapi dia tidak akan muncul di "Daftar Tunggu"
        cert.status = "VALID"; 
        cert.verificationStatus = "VALID";
        
        cert.qrToken = uuid;
        cert.verificationUrl = "https://proj-pens-cert.vercel.app/verify?id=" + uuid;
        cert.pdfUrl = pdfUrl;
        cert.pdfFilename = selectedTemplateName;
        cert.pdfStoragePath = "certificates/" + uuid + ".pdf";
        cert.certId = certId;

        cert.name = cert.participantName; cert.role = cert.participantRole; cert.event = cert.eventName;
        cert.sha256Hash = fileHash;
        cert.hash = fileHash;
        cert.rsaSignature = "ISSUED-BY-LECTURER";
        cert.signature = "ISSUED-BY-LECTURER";

        supabaseHelper.createCertificate(cert, new SupabaseHelper.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(CreateCertificateActivity.this, "Sertifikat Berhasil Diterbitkan!", Toast.LENGTH_LONG).show();
                finish();
            }
            @Override
            public void onError(Exception e) {
                btnNext.setEnabled(true);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(CreateCertificateActivity.this, "Gagal simpan data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) result = result.substring(cut + 1);
            }
        }
        return result != null ? result : "certificate.pdf";
    }

    private byte[] getBytes(InputStream inputStream) throws Exception {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024]; int len;
        while ((len = inputStream.read(buffer)) != -1) byteBuffer.write(buffer, 0, len);
        return byteBuffer.toByteArray();
    }

    private String calculateSHA256(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(bytes);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return "error_hash";
        }
    }
}
