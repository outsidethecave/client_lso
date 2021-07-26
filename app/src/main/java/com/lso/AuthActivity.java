package com.lso;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class AuthActivity extends AppCompatActivity {

    private static final String TAG = AuthActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        setRegisterBtn();
        setLoginBtn();
    }

    private void setRegisterBtn () {
        Button registrati_btn = findViewById(R.id.registrati_btn_auth);
        registrati_btn.setOnClickListener(v -> startActivity(new Intent(AuthActivity.this, RegistrationActivity.class)));
    }
    private void setLoginBtn () {
        Button login_btn = findViewById(R.id.login_btn_auth);
        login_btn.setOnClickListener(v -> startActivity(new Intent(AuthActivity.this, LogInActivity.class)));
    }

}