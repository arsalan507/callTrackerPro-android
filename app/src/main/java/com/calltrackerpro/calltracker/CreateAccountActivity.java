package com.calltrackerpro.calltracker;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class CreateAccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Redirect to the new multi-step signup flow
        Intent intent = new Intent(CreateAccountActivity.this, SignupStep1Activity.class);
        startActivity(intent);
        finish();
    }
}