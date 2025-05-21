package com.example.fitshield;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class HomePageActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create a simple text view to show the home page is working
        TextView textView = new TextView(this);
        textView.setText("Welcome to Home Page");
        textView.setTextSize(20f);
        textView.setPadding(50, 50, 50, 50);

        setContentView(textView);
    }
}