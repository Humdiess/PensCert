package com.example.penscert;

import android.view.View;
import android.view.animation.AccelerateInterpolator;

import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

/**
 * Helper class to force ZXing scanner to portrait mode and provide a modern overlay UI.
 * Mirrors the same logic as ScanQrActivity for compatibility.
 */
public class CaptureActivityPortrait extends CaptureActivity {

    @Override
    protected DecoratedBarcodeView initializeContent() {
        setContentView(R.layout.activity_scan_qr);

        // Scanner frame breathing glow
        View scannerContainer = findViewById(R.id.scanner_container);
        if (scannerContainer != null) {
            ScannerAnimations.startBreathingGlow(scannerContainer);
        }

        // Scan line animation
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

        // Close button
        View btnClose = findViewById(R.id.btnClose);
        if (btnClose != null) {
            btnClose.setClickable(true);
            btnClose.setFocusable(true);
            btnClose.setOnClickListener(v -> {
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

        // Flash toggle
        View btnFlash = findViewById(R.id.btn_flash);
        if (btnFlash != null) {
            final boolean[] flashOn = {false};
            btnFlash.setOnClickListener(v -> {
                flashOn[0] = !flashOn[0];
                DecoratedBarcodeView barcodeView = findViewById(R.id.zxing_barcode_scanner);
                if (barcodeView != null) {
                    barcodeView.getBarcodeView().setTorch(flashOn[0]);
                }
            });
        }

        // Entrance animations
        View headerGroup = findViewById(R.id.header_group);
        if (headerGroup != null) ScannerAnimations.fadeInSlideUp(headerGroup, 100);
        if (btnClose != null) ScannerAnimations.scaleIn(btnClose, 200);
        if (scannerContainer != null) ScannerAnimations.scaleIn(scannerContainer, 150);
        View statusPill = findViewById(R.id.status_pill);
        if (statusPill != null) ScannerAnimations.fadeInSlideUp(statusPill, 350);
        View btnGallery = findViewById(R.id.btn_gallery);
        View btnFocus = findViewById(R.id.btn_focus);
        if (btnFlash != null && btnGallery != null && btnFocus != null) {
            ScannerAnimations.staggerToolbarEntrance(btnFlash, btnGallery, btnFocus);
        }

        return findViewById(R.id.zxing_barcode_scanner);
    }
}
