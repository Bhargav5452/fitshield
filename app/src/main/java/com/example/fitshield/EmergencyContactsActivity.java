package com.example.fitshield;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class EmergencyContactsActivity extends AppCompatActivity {

    private EditText contact1, contact2, contact3;
    private SharedPreferences sharedPreferences;

    private final ActivityResultLauncher<Intent> overlayPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (Settings.canDrawOverlays(this)) {
                    Log.d("EmergencyContactsActivity", "Overlay permission granted via launcher");
                    startFloatingIconService();
                    moveToHomePage();
                } else {
                    Log.d("EmergencyContactsActivity", "Overlay permission denied");
                    Toast.makeText(this, "Overlay permission is required for emergency icon.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contacts);

        contact1 = findViewById(R.id.contact1);
        contact2 = findViewById(R.id.contact2);
        contact3 = findViewById(R.id.contact3);
        Button saveButton = findViewById(R.id.saveContactsBtn);

        sharedPreferences = getSharedPreferences("FitShieldPrefs", Context.MODE_PRIVATE);

        if (sharedPreferences.getBoolean("contacts_saved", false)) {
            startActivity(new Intent(this, HomePageActivity.class));
            finish();
            return;
        }

        saveButton.setOnClickListener(v -> saveContacts());
    }

    private void saveContacts() {
        String num1 = contact1.getText().toString().trim();
        String num2 = contact2.getText().toString().trim();
        String num3 = contact3.getText().toString().trim();

        if (isValidNumber(num1) && isValidNumber(num2) && isValidNumber(num3)) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("contact1", num1);
            editor.putString("contact2", num2);
            editor.putString("contact3", num3);
            editor.putBoolean("contacts_saved", true);
            editor.apply();

            Toast.makeText(this, "Contacts Saved!", Toast.LENGTH_SHORT).show();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Please allow overlay permission first", Toast.LENGTH_LONG).show();
                startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName())));
                return;
            }
            else {
                // Permission already granted
                startFloatingIconService();
                moveToHomePage();
            }

        } else {
            Toast.makeText(this, "Enter valid 10-digit numbers!", Toast.LENGTH_SHORT).show();
        }
    }

    private void startFloatingIconService() {
        try {
            Intent serviceIntent = new Intent(this, FloatingIconService.class);
            startService(serviceIntent);
        } catch (Exception e) {
            Toast.makeText(this, "Error starting service: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("EmergencyContactsActivity", "Service start error", e);
        }
    }

    private void moveToHomePage() {
        Intent intent = new Intent(this, HomePageActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean isValidNumber(String num) {
        return num.length() == 10 && num.matches("\\d+");
    }
}
