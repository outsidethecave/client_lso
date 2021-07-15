package com.lso;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

public class LogInActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private TextInputLayout nickInput;
    private TextInputLayout pswdInput;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        setProgressDialog();
        setBackToMenuButton();
        setInputLayouts();
        setLoginBtn();
    }



    private void setBackToMenuButton() {
        Button torna_al_login_btn = findViewById(R.id.torna_al_menu_btn_login);
        torna_al_login_btn.setOnClickListener(v -> {
            startActivity(new Intent(LogInActivity.this, ConnectionActivity.class));
            finishAffinity();
        });
    }

    private void setProgressDialog () {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Effettuando il login...");
    }

    private void setInputLayouts() {
        nickInput = findViewById(R.id.nick_input_login);
        pswdInput = findViewById(R.id.pswd_input_login);

        nickInput.getEditText().setOnClickListener(v -> nickInput.setError(null));
        pswdInput.getEditText().setOnClickListener(v -> pswdInput.setError(null));
    }

    private void setLoginBtn() {
        Button loginBtn = findViewById(R.id.login_btn_login);
        loginBtn.setOnClickListener(v -> {

            progressDialog.show();

            nickInput.setError(null);
            pswdInput.setError(null);

            String nick = nickInput.getEditText().getText().toString().trim();
            String pswd = pswdInput.getEditText().getText().toString();

            new Thread(() -> {
                while (true) {
                    int loginOutcome = UserDataHandler.logIn(nick, pswd);
                    if (loginOutcome == 0) {
                        // Oltre al toast bisogna passare alla schermata principale
                        // Bisogna creare una variabile currUser in un eventuale Controller
                        runOnUiThread(() -> Toast.makeText(this, nick + " ha effettuato il login", Toast.LENGTH_SHORT).show());
                    }
                    else if (loginOutcome == 1) {
                        runOnUiThread(() -> pswdInput.setError("Password errata"));
                    }
                    else if (loginOutcome == 2) {
                        runOnUiThread(() -> nickInput.setError("Non esiste un account con questo nome"));
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
        });
    }
}