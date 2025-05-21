package com.example.fitshield;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;

public class EmergencyAlertActivity extends AppCompatActivity {

    private MediaRecorder mediaRecorder;
    private boolean isRecording = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_alert);

        TextView alertMessage = findViewById(R.id.alertMessage);
        Button stopAlertButton = findViewById(R.id.stopAlertButton);

        vibratePhone();
        startVoiceRecording();

        stopAlertButton.setOnClickListener(v -> {
            stopVoiceRecording();
            Toast.makeText(this, "Emergency Alert Stopped", Toast.LENGTH_SHORT).show();
            finish();
        });

        sendEmergencyAlert();
    }

    private void vibratePhone() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.vibrate(1000);
        }
    }

    private void startVoiceRecording() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 102);
            return;
        }

        File fileDir = getExternalFilesDir(null);
        String audioFilePath;
        if (fileDir != null) {
            audioFilePath = fileDir.getAbsolutePath() + "/emergency_audio.3gp";
        } else {
            audioFilePath = getFilesDir().getAbsolutePath() + "/emergency_audio.3gp";
        }

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(audioFilePath);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("EmergencyAlertActivity", "Error starting recording: " + e.getMessage());
        }
    }

    private void stopVoiceRecording() {
        if (isRecording) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            isRecording = false;
            Toast.makeText(this, "Recording saved", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendEmergencyAlert() {
        Toast.makeText(this, "Emergency alert sent!", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopVoiceRecording();
    }
}
