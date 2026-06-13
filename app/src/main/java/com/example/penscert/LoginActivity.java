package com.example.penscert;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etNimNip, etPassword;
    private RadioGroup rgRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Modern edge-to-edge with dark status bar icons on light background
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(false);

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        if (prefs.getBoolean("is_logged_in", false)) {
            startMainActivity();
            return;
        }

        setContentView(R.layout.activity_login);

        etNimNip = findViewById(R.id.etNimNip);
        etPassword = findViewById(R.id.etPassword);
        rgRole = findViewById(R.id.rgRole);
        Button btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            String id = etNimNip.getText() != null ? etNimNip.getText().toString().trim() : "";
            String pass = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

            if (id.isEmpty()) {
                etNimNip.setError("NRP/NIP wajib diisi");
                return;
            }
            if (pass.isEmpty()) {
                etPassword.setError("Kata sandi wajib diisi");
                return;
            }

            String role = rgRole.getCheckedRadioButtonId() == R.id.rbDosen ? "Dosen" : "Mahasiswa";

            prefs.edit()
                    .putBoolean("is_logged_in", true)
                    .putString("user_id", id)
                    .putString("user_role", role)
                    .putString("user_name", id)
                    .apply();

            Toast.makeText(this, "Selamat datang, " + role + "!", Toast.LENGTH_SHORT).show();
            startMainActivity();
        });
    }

    private void startMainActivity() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        if (!prefs.getBoolean("intro_shown", false)) {
            startActivity(new Intent(this, IntroActivity.class));
        } else {
            startActivity(new Intent(this, MainActivity.class));
        }
        finish();
    }
}
