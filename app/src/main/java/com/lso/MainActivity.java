package com.lso;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRegisterBtn();
        setLoginBtn();
    }

    private void setRegisterBtn () {
        Button registrati_btn = findViewById(R.id.registrati_btn_main);
        registrati_btn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, RegistrationActivity.class)));
    }
    private void setLoginBtn () {
        Button login_btn = findViewById(R.id.login_btn_main);
        login_btn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, LogInActivity.class)));
    }

}