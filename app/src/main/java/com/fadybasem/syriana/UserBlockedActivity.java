package com.fadybasem.syriana;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class UserBlockedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_blocked);

        findViewById(R.id.exitButton).setOnClickListener(view -> finishAffinity());
    }
}