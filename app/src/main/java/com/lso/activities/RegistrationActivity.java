package com.lso.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.android.material.textfield.TextInputLayout;
import com.lso.control.AuthController;
import com.lso.InputErrorHelper;
import com.lso.R;

@SuppressWarnings("ConstantConditions")

public class RegistrationActivity extends AppCompatActivity {

    private final AuthController authController = AuthController.getInstance();

    private ProgressDialog progressDialog;
    private TextInputLayout nickInput;
    private TextInputLayout pswd1Input;
    private TextInputLayout pswd2Input;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        authController.setRegistrationActivity(this);

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
            startActivity(new Intent(RegistrationActivity.this, AuthActivity.class));
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

            nickInput.setError(null);
            pswd1Input.setError(null);
            pswd2Input.setError(null);

            String nick = nickInput.getEditText().getText().toString().trim();
            String pswd1 = pswd1Input.getEditText().getText().toString();
            String pswd2 = pswd2Input.getEditText().getText().toString();

            boolean exit = false;
            if (!InputErrorHelper.isNickname(nick, nickInput)) {
                exit = true;
            }
            if (!InputErrorHelper.isPassword(pswd1, pswd1Input)) {
                exit = true;
            }
            if (!InputErrorHelper.passwordsMatch(pswd1, pswd2, pswd2Input)) {
                exit = true;
            }
            if (!exit) {
                authController.register(nick, pswd1);
            }
            else {
                progressDialog.dismiss();
            }
        });
    }


    public void showProgressDialog () {
        progressDialog.show();
    }
    public void dismissProgressDialog () {
        progressDialog.dismiss();
    }

    public void clearInputs () {
        nickInput.getEditText().setText("");
        pswd1Input.getEditText().setText("");
        pswd2Input.getEditText().setText("");
    }

    public TextInputLayout getNickInput() {
        return nickInput;
    }
}