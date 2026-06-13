package com.example.penscert;

import android.view.View;
import android.view.animation.AccelerateInterpolator;

import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

/**
 * Custom QR scanner activity with a modern overlay UI.
 * Handles close button, entrance animations, and scanner frame effects.
 */
public class ScanQrActivity extends CaptureActivity {

    @Override
    protected DecoratedBarcodeView initializeContent() {
        setContentView(R.layout.activity_scan_qr);

        // Scanner frame breathing glow
        View scannerContainer = findViewById(R.id.scanner_container);
        if (scannerContainer != null) {
            ScannerAnimations.startBreathingGlow(scannerContainer);
        }

        // Scan line animation (260dp frame height)
        View scanLine = findViewById(R.id.scan_line);
        if (scanLine != null) {
            float heightPx = 260 * getResources().getDisplayMetrics().density;
            ScannerAnimations.startScanLine(scanLine, heightPx);
        }

        // Status dot pulse
        View statusDot = findViewById(R.id.status_dot);
        if (statusDot != null) {
            ScannerAnimations.startDotPulse(statusDot);
        }

        // Close button — the primary fix
        View btnClose = findViewById(R.id.btnClose);
        if (btnClose != null) {
            btnClose.setClickable(true);
            btnClose.setFocusable(true);
            btnClose.setOnClickListener(v -> {
                // Animate out then finish
                v.animate()
                        .scaleX(0.8f)
                        .scaleY(0.8f)
                        .alpha(0f)
                        .setDuration(150)
                        .setInterpolator(new AccelerateInterpolator())
                        .withEndAction(() -> finish())
                        .start();
            });
        }

        // Flash toggle button
        View btnFlash = findViewById(R.id.btn_flash);
        if (btnFlash != null) {
            final boolean[] flashOn = {false};
            btnFlash.setOnClickListener(v -> {
                flashOn[0] = !flashOn[0];
                DecoratedBarcodeView barcodeView = findViewById(R.id.zxing_barcode_scanner);
                if (barcodeView != null) {
                    barcodeView.getBarcodeView().setTorch(flashOn[0]);
                }
                v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(80)
                        .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(80).start())
                        .start();
            });
        }

        // Entrance animations for overlay elements
        animateEntrance();

        return findViewById(R.id.zxing_barcode_scanner);
    }

    private void animateEntrance() {
        // Header text group (top-left)
        View headerGroup = findViewById(R.id.header_group);
        if (headerGroup != null) {
            ScannerAnimations.fadeInSlideUp(headerGroup, 100);
        }

        // Close button
        View btnClose = findViewById(R.id.btnClose);
        if (btnClose != null) {
            ScannerAnimations.scaleIn(btnClose, 200);
        }

        // Scanner frame scale-in
        View scannerContainer = findViewById(R.id.scanner_container);
        if (scannerContainer != null) {
            ScannerAnimations.scaleIn(scannerContainer, 150);
        }

        // Status pill
        View statusPill = findViewById(R.id.status_pill);
        if (statusPill != null) {
            ScannerAnimations.fadeInSlideUp(statusPill, 350);
        }

        // Bottom toolbar buttons — staggered
        View btnFlash = findViewById(R.id.btn_flash);
        View btnGallery = findViewById(R.id.btn_gallery);
        View btnFocus = findViewById(R.id.btn_focus);
        if (btnFlash != null && btnGallery != null && btnFocus != null) {
            ScannerAnimations.staggerToolbarEntrance(btnFlash, btnGallery, btnFocus);
        }
    }
}
