package com.lso.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.lso.control.AuthController;
import com.lso.R;

@SuppressWarnings("ConstantConditions")
public class LogInActivity extends AppCompatActivity {

    private final AuthController authController = AuthController.getInstance();

    private ProgressDialog progressDialog;
    private TextInputLayout nickInput;
    private TextInputLayout pswdInput;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        authController.setLoginActivity(this);

        setProgressDialog();
        setBackToMenuButton();
        setInputLayouts();
        setLoginBtn();
    }



    private void setBackToMenuButton() {
        Button torna_al_login_btn = findViewById(R.id.torna_al_menu_btn_login);
        torna_al_login_btn.setOnClickListener(v -> {
            startActivity(new Intent(LogInActivity.this, AuthActivity.class));
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

            nickInput.setError(null);
            pswdInput.setError(null);

            String nick = nickInput.getEditText().getText().toString().trim();
            String pswd = pswdInput.getEditText().getText().toString();

            if (nick.isEmpty()) {
                nickInput.setError("Inserisci il nickname");
                progressDialog.dismiss();
            }
            else if (pswd.isEmpty()) {
                pswdInput.setError("Inserisci la password");
                progressDialog.dismiss();
            }
            else {
                authController.logIn(nick, pswd);
            }
        });
    }

    public void goToMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finishAffinity();
    }


    public void showProgressDialog () {
        progressDialog.show();
    }
    public void dismissProgressDialog () {
        progressDialog.dismiss();
    }

    public TextInputLayout getNickInput() {
        return nickInput;
    }

    public TextInputLayout getPswdInput() {
        return pswdInput;
    }
}