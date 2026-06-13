package com.example.penscert;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class SetQrPositionActivity extends AppCompatActivity {

    private ImageView ivPdfPreview;
    private View ivQrMockup;
    private View pdfContainer;
    private Button btnSave;
    private ProgressBar loader;
    
    private Uri pdfUri;
    private float lastX, lastY;
    private int containerWidth, containerHeight;
    private int pdfPageWidth, pdfPageHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_qr_position);

        ivPdfPreview = findViewById(R.id.ivPdfPreview);
        ivQrMockup = findViewById(R.id.ivQrMockup);
        pdfContainer = findViewById(R.id.pdfContainer);
        btnSave = findViewById(R.id.btnSavePosition);
        loader = findViewById(R.id.loader);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pdfUri = getIntent().getParcelableExtra("PDF_URI", Uri.class);
        } else {
            pdfUri = getIntent().getParcelableExtra("PDF_URI");
        }

        if (pdfUri != null) {
            renderPdfPreview();
        }

        setupDragListener();

        btnSave.setOnClickListener(v -> {
            float relativeX = ivQrMockup.getX() / pdfContainer.getWidth();
            float relativeY = ivQrMockup.getY() / pdfContainer.getHeight();

            Intent resultIntent = new Intent();
            resultIntent.putExtra("QR_X", relativeX);
            resultIntent.putExtra("QR_Y", relativeY);
            resultIntent.putExtra("QR_PAGE", 1); // For now, focus on page 1
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    private void renderPdfPreview() {
        loader.setVisibility(View.VISIBLE);
        new Thread(() -> {
            try {
                ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(pdfUri, "r");
                if (pfd != null) {
                    PdfRenderer renderer = new PdfRenderer(pfd);
                    PdfRenderer.Page page = renderer.openPage(0);

                    pdfPageWidth = page.getWidth();
                    pdfPageHeight = page.getHeight();

                    Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                    runOnUiThread(() -> {
                        ivPdfPreview.setImageBitmap(bitmap);
                        loader.setVisibility(View.GONE);
                    });

                    page.close();
                    renderer.close();
                }
            } catch (IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Gagal memuat preview PDF", Toast.LENGTH_SHORT).show();
                    loader.setVisibility(View.GONE);
                });
            }
        }).start();
    }

    private void setupDragListener() {
        ivQrMockup.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastX = event.getRawX();
                    lastY = event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float dx = event.getRawX() - lastX;
                    float dy = event.getRawY() - lastY;

                    float newX = v.getX() + dx;
                    float newY = v.getY() + dy;

                    // Boundary checks
                    if (newX >= 0 && newX <= pdfContainer.getWidth() - v.getWidth()) {
                        v.setX(newX);
                    }
                    if (newY >= 0 && newY <= pdfContainer.getHeight() - v.getHeight()) {
                        v.setY(newY);
                    }

                    lastX = event.getRawX();
                    lastY = event.getRawY();
                    break;
            }
            return true;
        });
    }
}
