package com.example.penscert;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class RequestSignatureActivity extends AppCompatActivity {

    private ViewFlipper viewFlipper;
    private TextView tvStepTitle, tvMainTitle, tvFileName;
    private LinearProgressIndicator stepProgress;
    private Button btnBack, btnNext;
    
    private TextInputEditText etName, etRole, etMatkul, etTglMatkul;
    private RadioGroup rgDocType;
    private LinearLayout layoutAbsenExtra;
    private AutoCompleteTextView actvSigner;
    private View btnSelectPdf, summaryIdentity, summaryDoc, summarySigner;
    private TextView tvSumName, tvSumDoc, tvSumSigner;
    private LinearLayout loadingLayout;
    
    private SupabaseHelper supabaseHelper;
    private Uri selectedPdfUri;
    private String selectedFileName;
    private float qrX = 0.8f, qrY = 0.8f; 
    private int qrPage = 1;
    private int currentStep = 0; 

    private final String[] lecturers = {
        "Ahmad Zainudin, S.ST., M.T.", "Akuwan Saleh, S.ST., M.T.",
        "Amang Sudarsono, S.T., Ph.D.", "Ir. Anang Budikarso, M.T.",
        "Ari Wijayanti, S.T., M.T.", "Aries Pratiarso, S.T., M.T.",
        "Arifin, S.T., M.T.", "Ir. Budi Aswoyo, M.T.",
        "Djoko Santoso, S.T.", "Hani'ah Mahmudah, S.T., M.T.",
        "Prof. Dr. Ir. Titon Dutono, M.Eng.", "Dra. Rini Satiti, M.Si.",
        "Retno Sukmaningrum, S.T., M.T.", "Ir. Gigih Prabowo, M.T.",
        "Inka Trisna Dewi, S.ST., M.Tr.T.", "Nanang Syahroni, S.Kom., M.T.",
        "Faridatun Nadziroh, S.ST., M.T.", "Nur Adi Siswandari, S.ST., M.T.",
        "Karimatun Nisa, S.ST., M.T.", "Paramita Eka Wahyu Lestari, S.ST., M.T."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_signature);
        initViews();
        setupLogic();
        updateStepUI();
    }

    private void initViews() {
        viewFlipper = findViewById(R.id.viewFlipper);
        tvStepTitle = findViewById(R.id.tvStepTitle);
        tvMainTitle = findViewById(R.id.tvMainTitle);
        stepProgress = findViewById(R.id.stepProgress);
        btnBack = findViewById(R.id.btnBack);
        btnNext = findViewById(R.id.btnNext);
        etName = findViewById(R.id.etReqName);
        etRole = findViewById(R.id.etReqRole);
        rgDocType = findViewById(R.id.rgDocType);
        layoutAbsenExtra = findViewById(R.id.layoutAbsenExtra);
        etMatkul = findViewById(R.id.etMatkul);
        etTglMatkul = findViewById(R.id.etTglMatkul);
        actvSigner = findViewById(R.id.actvSigner);
        btnSelectPdf = findViewById(R.id.btnSelectPdf);
        tvFileName = findViewById(R.id.tvFileName);
        tvSumName = findViewById(R.id.tvSumName);
        tvSumDoc = findViewById(R.id.tvSumDoc);
        tvSumSigner = findViewById(R.id.tvSumSigner);
        summaryIdentity = findViewById(R.id.summaryIdentity);
        summaryDoc = findViewById(R.id.summaryDoc);
        summarySigner = findViewById(R.id.summarySigner);
        loadingLayout = findViewById(R.id.loadingLayout);
    }

    private void setupLogic() {
        supabaseHelper = new SupabaseHelper();
        rgDocType.setOnCheckedChangeListener((group, checkedId) -> {
            layoutAbsenExtra.setVisibility(checkedId == R.id.rbAbsen ? View.VISIBLE : View.GONE);
        });
        etTglMatkul.setOnClickListener(v -> showDatePicker());
        actvSigner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lecturers));
        btnSelectPdf.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/pdf");
            pdfPickerLauncher.launch(intent);
        });

        summaryIdentity.setOnClickListener(v -> goToStep(0));
        summaryDoc.setOnClickListener(v -> goToStep(0));
        summarySigner.setOnClickListener(v -> goToStep(1));

        btnNext.setOnClickListener(v -> handleNext());
        btnBack.setOnClickListener(v -> handleBack());
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
    }

    private void handleNext() {
        if (currentStep == 0 && validateStep1()) goToStep(1);
        else if (currentStep == 1 && validateStep2()) { updateSummary(); goToStep(2); }
        else if (currentStep == 2) submitRequest();
    }

    private void handleBack() {
        if (currentStep > 0) goToStep(currentStep - 1);
        else finish();
    }

    private void updateStepUI() {
        switch (currentStep) {
            case 0:
                tvStepTitle.setText("Langkah 1 dari 3");
                tvMainTitle.setText("Informasi Dasar");
                stepProgress.setProgress(33);
                btnBack.setText("Batal");
                btnNext.setText("Lanjut");
                break;
            case 1:
                tvStepTitle.setText("Langkah 2 dari 3");
                tvMainTitle.setText("Dokumen & TTD");
                stepProgress.setProgress(66);
                btnBack.setText("Kembali");
                btnNext.setText("Lanjut");
                break;
            case 2:
                tvStepTitle.setText("Langkah 3 dari 3");
                tvMainTitle.setText("Ringkasan");
                stepProgress.setProgress(100);
                btnBack.setText("Kembali");
                btnNext.setText("Kirim Sekarang");
                break;
        }
    }

    private boolean validateStep1() {
        if (etName.getText().toString().trim().isEmpty()) { etName.setError("Nama wajib diisi"); return false; }
        if (rgDocType.getCheckedRadioButtonId() == R.id.rbAbsen && etMatkul.getText().toString().trim().isEmpty()) {
            etMatkul.setError("Matkul wajib diisi"); return false;
        }
        return true;
    }

    private boolean validateStep2() {
        if (actvSigner.getText().toString().trim().isEmpty()) { Toast.makeText(this, "Pilih dosen", Toast.LENGTH_SHORT).show(); return false; }
        if (selectedPdfUri == null) { Toast.makeText(this, "Pilih file PDF", Toast.LENGTH_SHORT).show(); return false; }
        return true;
    }

    private void updateSummary() {
        tvSumName.setText(etName.getText().toString() + " (" + etRole.getText().toString() + ")");
        String docInfo = rgDocType.getCheckedRadioButtonId() == R.id.rbUmum ? "Surat Umum" : "Surat Absen";
        if (rgDocType.getCheckedRadioButtonId() == R.id.rbAbsen) {
            docInfo += "\nMatkul: " + etMatkul.getText().toString() + "\nTanggal: " + etTglMatkul.getText().toString();
        }
        tvSumDoc.setText(docInfo);
        tvSumSigner.setText(actvSigner.getText().toString());
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            etTglMatkul.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private final ActivityResultLauncher<Intent> positionSetterLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    qrX = result.getData().getFloatExtra("QR_X", 0.8f);
                    qrY = result.getData().getFloatExtra("QR_Y", 0.8f);
                    qrPage = result.getData().getIntExtra("QR_PAGE", 1);
                }
            }
    );

    private final ActivityResultLauncher<Intent> pdfPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedPdfUri = result.getData().getData();
                    selectedFileName = getFileName(selectedPdfUri);
                    tvFileName.setText(selectedFileName);
                    Intent intent = new Intent(this, SetQrPositionActivity.class);
                    intent.putExtra("PDF_URI", selectedPdfUri);
                    positionSetterLauncher.launch(intent);
                }
            }
    );

    private void submitRequest() {
        setLoading(true);
        String name = etName.getText().toString().trim();
        String role = etRole.getText().toString().trim();
        String type = rgDocType.getCheckedRadioButtonId() == R.id.rbUmum ? "Surat Umum" : "Surat Keterangan Absen";
        String signer = actvSigner.getText().toString().trim();

        try {
            InputStream is = getContentResolver().openInputStream(selectedPdfUri);
            byte[] bytes = getBytes(is);
            String fileHash = calculateSHA256(bytes);
            String remotePath = "requests/" + System.currentTimeMillis() + "_" + selectedFileName;
            supabaseHelper.uploadFile("certificates", remotePath, bytes, "application/pdf", new SupabaseHelper.Callback<String>() {
                @Override
                public void onSuccess(String publicUrl) { saveToDatabase(name, role, type, signer, publicUrl, fileHash); }
                @Override
                public void onError(Exception e) { setLoading(false); Toast.makeText(RequestSignatureActivity.this, "Gagal unggah", Toast.LENGTH_SHORT).show(); }
            });
        } catch (Exception e) { setLoading(false); Toast.makeText(this, "Gagal baca file", Toast.LENGTH_SHORT).show(); }
    }

    private void saveToDatabase(String name, String role, String type, String signer, String pdfUrl, String fileHash) {
        Certificate cert = new Certificate();
        cert.id = UUID.randomUUID().toString();
        // Berikan nomor sementara agar tidak error "violates not-null constraint"
        cert.certificateNumber = "REQ-" + cert.id.substring(0, 8).toUpperCase();
        cert.participantName = name; cert.participantRole = role; cert.eventName = type;
        cert.targetSigner = signer; cert.pdfUrl = pdfUrl; 
        cert.status = "PENDING"; cert.verificationStatus = "PENDING";
        cert.issuedAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        cert.qrX = qrX; cert.qrY = qrY; cert.qrPage = qrPage;
        cert.pdfFilename = selectedFileName;
        cert.name = name; cert.role = role; cert.event = type;
        
        // Fix: Berikan hash awal agar tidak melanggar constraint NOT NULL di database
        cert.sha256Hash = fileHash;
        cert.hash = fileHash;
        
        // Fix: Berikan qrToken awal agar tidak melanggar constraint NOT NULL
        cert.qrToken = UUID.randomUUID().toString();
        
        // Berikan placeholder untuk field lain yang mungkin NOT NULL
        cert.rsaSignature = "PENDING";
        cert.verificationUrl = "PENDING";

        if (rgDocType.getCheckedRadioButtonId() == R.id.rbAbsen) {
            cert.subjectName = etMatkul.getText().toString().trim();
            cert.subjectDate = etTglMatkul.getText().toString().trim();
        }

        supabaseHelper.createCertificate(cert, new SupabaseHelper.Callback<String>() {
            @Override
            public void onSuccess(String result) {
                // Add notification
                NotificationHelper notifHelper = new NotificationHelper(RequestSignatureActivity.this);
                notifHelper.addNotification(
                        "Pengajuan Terkirim",
                        "Pengajuan TTD untuk " + name + " telah dikirim ke " + signer,
                        "NEW_REQUEST",
                        cert.id
                );
                setLoading(false);
                finish();
            }
            @Override
            public void onError(Exception e) { setLoading(false); Toast.makeText(RequestSignatureActivity.this, "Gagal simpan: " + e.getMessage(), Toast.LENGTH_SHORT).show(); }
        });
    }

    private void setLoading(boolean isLoading) {
        btnNext.setEnabled(!isLoading); btnBack.setEnabled(!isLoading);
        loadingLayout.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) result = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
            }
        }
        if (result == null) {
            result = uri.getPath();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) result = result.substring(cut + 1);
            }
        }
        return result != null ? result : "document.pdf";
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
            return "error_calculating_hash";
        }
    }
}
