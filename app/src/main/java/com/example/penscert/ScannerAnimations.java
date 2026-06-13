package com.example.penscert;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

/**
 * Premium animation utilities for the QR scanner and app-wide transitions.
 */
public class ScannerAnimations {

    /**
     * Scanning line that moves up and down within the scanner frame.
     */
    public static void startScanLine(View scanLine, float containerHeight) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(
                scanLine,
                "translationY",
                0f,
                containerHeight
        );
        animator.setDuration(2200);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
    }

    /**
     * Pulsing dot animation for status indicators.
     */
    public static void startDotPulse(View dot) {
        ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(dot, "alpha", 1f, 0.3f);
        alphaAnim.setDuration(800);
        alphaAnim.setRepeatMode(ValueAnimator.REVERSE);
        alphaAnim.setRepeatCount(ValueAnimator.INFINITE);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(dot, "scaleX", 1f, 1.3f);
        scaleX.setDuration(800);
        scaleX.setRepeatMode(ValueAnimator.REVERSE);
        scaleX.setRepeatCount(ValueAnimator.INFINITE);

        ObjectAnimator scaleY = ObjectAnimator.ofFloat(dot, "scaleY", 1f, 1.3f);
        scaleY.setDuration(800);
        scaleY.setRepeatMode(ValueAnimator.REVERSE);
        scaleY.setRepeatCount(ValueAnimator.INFINITE);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(alphaAnim, scaleX, scaleY);
        set.start();
    }

    /**
     * Fade-in + slide-up entrance for overlay UI elements.
     */
    public static void fadeInSlideUp(View view, int delayMs) {
        view.setAlpha(0f);
        view.setTranslationY(40f);
        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setStartDelay(delayMs)
                .setInterpolator(new DecelerateInterpolator(1.5f))
                .start();
    }

    /**
     * Scale-in entrance with overshoot for emphasis elements.
     */
    public static void scaleIn(View view, int delayMs) {
        view.setScaleX(0.6f);
        view.setScaleY(0.6f);
        view.setAlpha(0f);
        view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(450)
                .setStartDelay(delayMs)
                .setInterpolator(new OvershootInterpolator(1.2f))
                .start();
    }

    /**
     * Subtle breathing glow animation for the scanner frame.
     */
    public static void startBreathingGlow(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 0.08f, 0.18f);
        animator.setDuration(1500);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
    }

    /**
     * Staggered entrance animation for bottom toolbar buttons.
     */
    public static void staggerToolbarEntrance(View... buttons) {
        for (int i = 0; i < buttons.length; i++) {
            fadeInSlideUp(buttons[i], 400 + (i * 100));
        }
    }
}
