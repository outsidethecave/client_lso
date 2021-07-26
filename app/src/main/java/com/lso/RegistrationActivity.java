package com.lso;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

public class RegistrationActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private TextInputLayout nickInput;
    private TextInputLayout pswd1Input;
    private TextInputLayout pswd2Input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        setProgressDialog();
        setBackToMenuButton();
        setInputLayouts();
        setRegisterButton();
    }



    private void setProgressDialog () {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Effettuando la registrazione...");
    }

    private void setBackToMenuButton() {
        Button torna_al_login_btn = findViewById(R.id.torna_al_menu_btn_reg);
        torna_al_login_btn.setOnClickListener(v -> {
            startActivity(new Intent(RegistrationActivity.this, ConnectionActivity.class));
            finishAffinity();
        });
    }

    private void setInputLayouts() {
        nickInput = findViewById(R.id.nick_input_reg);
        pswd1Input = findViewById(R.id.pswd_input_reg);
        pswd2Input = findViewById(R.id.pswd2_input_reg);

        nickInput.getEditText().setOnClickListener(v -> nickInput.setError(null));
        pswd1Input.getEditText().setOnClickListener(v -> pswd1Input.setError(null));
        pswd2Input.getEditText().setOnClickListener(v -> pswd2Input.setError(null));
    }

    private void setRegisterButton () {
        Button registerButton = findViewById(R.id.registrati_btn_reg);
        registerButton.setOnClickListener(v -> {

            progressDialog.show();

            nickInput.setError(null);
            pswd1Input.setError(null);
            pswd2Input.setError(null);

            String nick = nickInput.getEditText().getText().toString().trim();
            String pswd1 = pswd1Input.getEditText().getText().toString();
            String pswd2 = pswd2Input.getEditText().getText().toString();

            boolean exit = false;
            if (!InputErrorHandler.isNickname(nick, nickInput)) {
                exit = true;
            }
            if (!InputErrorHandler.isPassword(pswd1, pswd1Input)) {
                exit = true;
            }
            if (!InputErrorHandler.passwordsMatch(pswd1, pswd2, pswd2Input)) {
                exit = true;
            }
            if (!exit) {
                new Thread(() -> {
                    while (true) {
                        int addUserOutcome = AuthHandler.addUser(nick, pswd1);
                        if (addUserOutcome == AuthHandler.SIGNUP_SUCCESS) {
                            runOnUiThread(() -> Toast.makeText(this, "Utente registrato", Toast.LENGTH_SHORT).show());
                        }
                        else if (addUserOutcome == AuthHandler.USER_ALREADY_EXISTS) {
                            runOnUiThread(() -> nickInput.setError("Questo nickname è già esistente"));
                        }
                        else {
                            ConnectionHandler.stopConnection();
                            if (ConnectionHandler.startConnection()) {
                                continue;
                            }
                            runOnUiThread(() -> {
                                progressDialog.dismiss();
                                Toast.makeText(this, "Errore di connessione", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, ConnectionActivity.class));
                                finishAffinity();
                            });
                        }
                        runOnUiThread(() -> progressDialog.dismiss());
                        break;
                    }
                }).start();
            }
            else {
                progressDialog.dismiss();
            }
        });
    }

}