package com.roomscanner.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private View cardStartScan;
    private View cardMultiRoom;
    private View cardExportScans;
    private View cardMitigateExport;
    private View cardMaterialAnalysis;
    private View cardARView;
    private MaterialButton btnSettings;
    private ExtendedFloatingActionButton fabQuickScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupClickListeners();
        checkPermissions();
    }

    private void initializeViews() {
        cardStartScan = findViewById(R.id.cardStartScan);
        cardMultiRoom = findViewById(R.id.cardMultiRoom);
        cardExportScans = findViewById(R.id.cardExportScans);
        cardMitigateExport = findViewById(R.id.cardMitigateExport);
        cardMaterialAnalysis = findViewById(R.id.cardMaterialAnalysis);
        cardARView = findViewById(R.id.cardARView);
        btnSettings = findViewById(R.id.btnSettings);
        fabQuickScan = findViewById(R.id.fabQuickScan);
    }

    private void setupClickListeners() {
        cardStartScan.setOnClickListener(v -> {
            if (checkAllPermissions()) {
                startScan();
            } else {
                requestPermissions();
            }
        });

        cardMultiRoom.setOnClickListener(v -> {
            if (checkAllPermissions()) {
                startMultiRoomScan();
            } else {
                requestPermissions();
            }
        });

        cardExportScans.setOnClickListener(v -> exportScans());

        cardMitigateExport.setOnClickListener(v -> exportToMitigate());

        cardMaterialAnalysis.setOnClickListener(v -> showMaterialAnalysis());

        cardARView.setOnClickListener(v -> {
            if (checkAllPermissions()) {
                openARView();
            } else {
                requestPermissions();
            }
        });

        btnSettings.setOnClickListener(v -> openSettings());

        fabQuickScan.setOnClickListener(v -> {
            if (checkAllPermissions()) {
                startQuickScan();
            } else {
                requestPermissions();
            }
        });
    }

    private boolean checkAllPermissions() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) 
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void checkPermissions() {
        if (!checkAllPermissions()) {
            Snackbar.make(findViewById(android.R.id.content),
                    "Permissions required for full functionality",
                    Snackbar.LENGTH_LONG)
                    .setAction("Grant", v -> requestPermissions())
                    .show();
        }
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            
            if (allGranted) {
                Toast.makeText(this, "All permissions granted! Ready to scan.", Toast.LENGTH_SHORT).show();
            } else {
                Snackbar.make(findViewById(android.R.id.content),
                        "Some permissions denied. App functionality may be limited.",
                        Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private void startScan() {
        Toast.makeText(this, R.string.scan_started, Toast.LENGTH_SHORT).show();
        // TODO: Implement camera scanning with CameraX and ARCore
        // This will be implemented in the camera integration phase
        showComingSoonMessage();
    }

    private void startQuickScan() {
        Toast.makeText(this, "Starting quick scan...", Toast.LENGTH_SHORT).show();
        // TODO: Implement quick scan mode
        showComingSoonMessage();
    }

    private void startMultiRoomScan() {
        Toast.makeText(this, "Starting multi-room scan", Toast.LENGTH_SHORT).show();
        // TODO: Implement multi-room scanning workflow
        showComingSoonMessage();
    }

    private void exportScans() {
        // TODO: Implement export dialog for format selection (OBJ, PLY, DXF, IFC)
        showComingSoonMessage();
    }

    private void exportToMitigate() {
        Toast.makeText(this, "Exporting to Mitigate format", Toast.LENGTH_SHORT).show();
        // TODO: Implement Mitigate export workflow
        // Use MitigateIntegration and WaterDamageAssessment classes
        showComingSoonMessage();
    }

    private void showMaterialAnalysis() {
        // TODO: Implement material analysis view
        showComingSoonMessage();
    }

    private void openARView() {
        // TODO: Implement AR visualization
        showComingSoonMessage();
    }

    private void openSettings() {
        // TODO: Implement settings activity
        showComingSoonMessage();
    }

    private void showComingSoonMessage() {
        Snackbar.make(findViewById(android.R.id.content),
                R.string.feature_coming_soon,
                Snackbar.LENGTH_SHORT)
                .setAction("OK", v -> {})
                .show();
    }
}
