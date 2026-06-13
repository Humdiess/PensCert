package com.example.penscert;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class IntroActivity extends AppCompatActivity {

    private ViewFlipper introFlipper;
    private Button btnAction;
    private int currentSlide = 0;
    private View dot1, dot2, dot3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Modern edge-to-edge with light status bar icons for dark hero background
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(false);

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        if (prefs.getBoolean("intro_shown", false)) {
            startMainActivity();
            return;
        }

        setContentView(R.layout.activity_intro);

        introFlipper = findViewById(R.id.introFlipper);
        btnAction = findViewById(R.id.btnIntroAction);
        TextView btnSkip = findViewById(R.id.btnSkipIntro);
        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);
        dot3 = findViewById(R.id.dot3);

        updateDots();

        btnAction.setOnClickListener(v -> {
            if (currentSlide < 2) {
                currentSlide++;
                showNextSlide();
            } else {
                prefs.edit().putBoolean("intro_shown", true).apply();
                startMainActivity();
            }
        });

        btnSkip.setOnClickListener(v -> {
            prefs.edit().putBoolean("intro_shown", true).apply();
            startMainActivity();
        });
    }

    private void updateDots() {
        View[] dots = {dot1, dot2, dot3};
        for (int i = 0; i < dots.length; i++) {
            ViewGroup.LayoutParams params = dots[i].getLayoutParams();
            if (i == currentSlide) {
                params.width = (int) (24 * getResources().getDisplayMetrics().density);
                params.height = (int) (8 * getResources().getDisplayMetrics().density);
                dots[i].setBackgroundResource(R.drawable.dot_active);
            } else {
                params.width = (int) (8 * getResources().getDisplayMetrics().density);
                params.height = (int) (8 * getResources().getDisplayMetrics().density);
                dots[i].setBackgroundResource(R.drawable.dot_inactive);
            }
            dots[i].setLayoutParams(params);
        }
    }

    private void showNextSlide() {
        introFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_right));
        introFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_left));
        introFlipper.showNext();

        updateDots();

        if (currentSlide == 2) {
            btnAction.setText("Mulai Sekarang");
        } else {
            btnAction.setText("Lanjut");
        }
    }

    private void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
