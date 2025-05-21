package com.example.fitshield;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class FloatingIconService extends Service {
    private WindowManager windowManager;
    private View floatingView;
    private WindowManager.LayoutParams params;

    @Override
    public void onCreate() {
        super.onCreate();

        // ✅ Check overlay permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Overlay permission not granted", Toast.LENGTH_LONG).show();
            stopSelf();
            return;
        }

        // ✅ Foreground service with notification
        String channelId = "fitshield_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "FitShield Floating Icon",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("FitShield is active")
                .setContentText("Emergency service running")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        startForeground(1, notification);

        // ✅ Setup floating view
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_icon, null);

        int windowType = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                windowType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 100;

        try {
            windowManager.addView(floatingView, params);
        } catch (Exception e) {
            Toast.makeText(this, "Failed to add floating icon: " + e.getMessage(), Toast.LENGTH_LONG).show();
            stopSelf();
            return;
        }

        // ✅ Handle movement and click
        floatingView.setOnTouchListener(new View.OnTouchListener() {
            private int lastX, lastY;
            private int initialX, initialY;
            private long touchStartTime;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        touchStartTime = System.currentTimeMillis();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) event.getRawX() - lastX;
                        params.y = initialY + (int) event.getRawY() - lastY;
                        windowManager.updateViewLayout(floatingView, params);
                        return true;

                    case MotionEvent.ACTION_UP:
                        long duration = System.currentTimeMillis() - touchStartTime;
                        if (duration < 200) {
                            sendEmergencySMS();
                        }
                        return true;
                }
                return false;
            }
        });
    }

    private void sendEmergencySMS() {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("FitShieldPrefs", MODE_PRIVATE);
        String c1 = prefs.getString("contact1", "");
        String c2 = prefs.getString("contact2", "");
        String c3 = prefs.getString("contact3", "");

        String message = "🚨 Emergency! I need help. Please reach me as soon as possible.";
        SmsManager smsManager = SmsManager.getDefault();

        try {
            if (!c1.isEmpty()) smsManager.sendTextMessage(c1, null, message, null, null);
            if (!c2.isEmpty()) smsManager.sendTextMessage(c2, null, message, null, null);
            if (!c3.isEmpty()) smsManager.sendTextMessage(c3, null, message, null, null);

            Toast.makeText(this, "Emergency SMS sent!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to send SMS: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingView != null) {
            windowManager.removeView(floatingView);
        }
    }
}
