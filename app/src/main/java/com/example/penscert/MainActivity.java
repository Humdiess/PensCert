package com.example.penscert;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class MainActivity extends AppCompatActivity {

    private Fragment homeFragment = new HomeFragment();
    private Fragment documentsFragment = new DocumentsFragment();
    private Fragment notificationsFragment = new NotificationsFragment();
    private Fragment profileFragment = new ProfileFragment();
    private Fragment activeFragment;

    // Nav items
    private LinearLayout navHome, navDocs, navNotif, navProfile;
    private ImageView navHomeIcon, navDocsIcon, navNotifIcon, navProfileIcon;
    private TextView navHomeLabel, navDocsLabel, navNotifLabel, navProfileLabel;

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(
            new ScanContract(),
            result -> {
                if (result.getContents() != null) {
                    Intent intent = new Intent(this, VerificationResultActivity.class);
                    intent.putExtra("SCAN_RESULT", result.getContents());
                    startActivity(intent);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(true);
        controller.setAppearanceLightNavigationBars(true);

        initNavViews();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragmentContainer, profileFragment, "profile").hide(profileFragment)
                    .add(R.id.fragmentContainer, notificationsFragment, "notifications").hide(notificationsFragment)
                    .add(R.id.fragmentContainer, documentsFragment, "documents").hide(documentsFragment)
                    .add(R.id.fragmentContainer, homeFragment, "home")
                    .commit();
            activeFragment = homeFragment;
        }

        navHome.setOnClickListener(v -> switchFragment(homeFragment, 0));
        navDocs.setOnClickListener(v -> switchFragment(documentsFragment, 1));
        navNotif.setOnClickListener(v -> switchFragment(notificationsFragment, 2));
        navProfile.setOnClickListener(v -> switchFragment(profileFragment, 3));

        // FAB - QR Scanner
        FloatingActionButton fabScan = findViewById(R.id.fabScan);
        fabScan.setOnClickListener(v -> {
            ScanOptions options = new ScanOptions();
            options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
            options.setPrompt("Arahkan ke QR Code");
            options.setCameraId(0);
            options.setBeepEnabled(false);
            options.setOrientationLocked(false);
            options.setCaptureActivity(CaptureActivityPortrait.class);
            barcodeLauncher.launch(options);
        });

        // Edge-to-edge: apply system bar insets
        View bottomBar = findViewById(R.id.bottomBar);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            int bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;

            // Navbar padding follows system inset
            bottomBar.setPadding(0, 0, 0, bottomInset);

            // FAB shifts up to stay level with navbar
            fabScan.setTranslationY(-bottomInset);

            return insets;
        });
    }

    private void initNavViews() {
        navHome = findViewById(R.id.navHome);
        navDocs = findViewById(R.id.navDocs);
        navNotif = findViewById(R.id.navNotif);
        navProfile = findViewById(R.id.navProfile);

        navHomeIcon = findViewById(R.id.navHomeIcon);
        navDocsIcon = findViewById(R.id.navDocsIcon);
        navNotifIcon = findViewById(R.id.navNotifIcon);
        navProfileIcon = findViewById(R.id.navProfileIcon);

        navHomeLabel = findViewById(R.id.navHomeLabel);
        navDocsLabel = findViewById(R.id.navDocsLabel);
        navNotifLabel = findViewById(R.id.navNotifLabel);
        navProfileLabel = findViewById(R.id.navProfileLabel);
    }

    private void switchFragment(Fragment target, int index) {
        if (target == activeFragment) return;

        getSupportFragmentManager().beginTransaction()
                .hide(activeFragment)
                .show(target)
                .commit();
        activeFragment = target;

        updateNavState(index);
    }

    private void updateNavState(int activeIndex) {
        int activeColor = getColor(R.color.nav_active_icon);
        int inactiveColor = getColor(R.color.nav_inactive);

        // Reset all
        setNavColor(navHomeIcon, navHomeLabel, inactiveColor);
        setNavColor(navDocsIcon, navDocsLabel, inactiveColor);
        setNavColor(navNotifIcon, navNotifLabel, inactiveColor);
        setNavColor(navProfileIcon, navProfileLabel, inactiveColor);

        // Set active
        switch (activeIndex) {
            case 0: setNavColor(navHomeIcon, navHomeLabel, activeColor); break;
            case 1: setNavColor(navDocsIcon, navDocsLabel, activeColor); break;
            case 2: setNavColor(navNotifIcon, navNotifLabel, activeColor); break;
            case 3: setNavColor(navProfileIcon, navProfileLabel, activeColor); break;
        }
    }

    private void setNavColor(ImageView icon, TextView label, int color) {
        icon.setColorFilter(color);
        label.setTextColor(color);
    }

    public void switchToDocuments() {
        switchFragment(documentsFragment, 1);
    }
}
