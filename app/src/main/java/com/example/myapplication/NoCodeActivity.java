package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class NoCodeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_code);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(NoCodeActivity.this,HomeActivity.class));
    }
}
