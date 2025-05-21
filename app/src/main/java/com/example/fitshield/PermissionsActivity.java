package com.example.fitshield;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.fitshield.EmergencyContactsActivity;
import com.example.fitshield.HomePageActivity;

public class PermissionsActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS = 101;
    private String[] requiredPermissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.RECORD_AUDIO
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (allPermissionsGranted()) {
            checkEmergencyContacts();
        } else {
            ActivityCompat.requestPermissions(this, requiredPermissions, REQUEST_PERMISSIONS);
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void checkEmergencyContacts() {
        SharedPreferences sharedPreferences = getSharedPreferences("FitShieldPrefs", Context.MODE_PRIVATE);
        boolean contactsSaved = sharedPreferences.getBoolean("contacts_saved", false);

        if (contactsSaved) {
            startActivity(new Intent(this, HomePageActivity.class));
        } else {
            startActivity(new Intent(this, EmergencyContactsActivity.class));
        }
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSIONS) {
            boolean allGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                checkEmergencyContacts();
            } else {
                Toast.makeText(this, "Permissions required!", Toast.LENGTH_LONG).show();
                finish(); // Close app if permissions are denied
            }
        }
    }
}
