package com.example.penscert;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;

public class AdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        MaterialCardView cardRequests = findViewById(R.id.cardRequests);
        MaterialCardView cardCreate = findViewById(R.id.cardCreate);
        MaterialCardView cardRevocation = findViewById(R.id.cardRevocation);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        cardRequests.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminRequestsActivity.class));
        });

        cardCreate.setOnClickListener(v -> {
            startActivity(new Intent(this, CreateCertificateActivity.class));
        });

        cardRevocation.setOnClickListener(v -> {
            startActivity(new Intent(this, RevocationActivity.class));
        });

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
            prefs.edit()
                    .putBoolean("is_logged_in", false)
                    .remove("user_id")
                    .remove("user_role")
                    .remove("user_name")
                    .apply();
            // Clear notifications on logout
            new NotificationHelper(this).clearAll();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
