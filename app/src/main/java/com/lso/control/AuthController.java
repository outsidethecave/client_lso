package com.lso.control;

import android.content.Intent;
import android.widget.Toast;

import com.lso.ConnectionHandler;
import com.lso.data.UserDataAccess;
import com.lso.activities.AuthActivity;
import com.lso.activities.ConnectionActivity;
import com.lso.activities.LogInActivity;
import com.lso.activities.MainActivity;
import com.lso.activities.RegistrationActivity;

public class AuthController {

    public static final int SIGNUP = 1;
    public static final int SIGNUP_SUCCESS = 0;
    public static final int USER_ALREADY_EXISTS = 1;
    public static final int GENERIC_SIGNUP_FAILURE = 2;

    public static final int LOGIN = 2;
    public static final int LOGIN_SUCCESS = 0;
    public static final int USER_DOES_NOT_EXIST = 1;
    public static final int WRONG_PASSWORD = 2;
    public static final int USER_ALREADY_CONNECTED = 3;
    public static final int GENERIC_LOGIN_FAILURE = 4;

    public static final int LOGOUT = 8;


    private RegistrationActivity registrationActivity;
    private LogInActivity loginActivity;


    private static String currUser;

    private static final AuthController instance = new AuthController();
    public static AuthController getInstance() {
        return instance;
    }



    private AuthController() {}



    public void register (String nick, String pswd) {

        registrationActivity.showProgressDialog();

        new Thread(() -> {
            while (true) {
                int addUserOutcome = UserDataAccess.addUser(nick, pswd);
                if (addUserOutcome == AuthController.SIGNUP_SUCCESS) {
                    registrationActivity.runOnUiThread(() -> {
                        registrationActivity.clearInputs();
                        Toast.makeText(registrationActivity, "Utente registrato", Toast.LENGTH_SHORT).show();
                    });
                }
                else if (addUserOutcome == AuthController.USER_ALREADY_EXISTS) {
                    registrationActivity.runOnUiThread(() -> registrationActivity.getNickInput().setError("Questo utente esiste già"));
                }
                else {
                    ConnectionHandler.stopConnection();
                    if (ConnectionHandler.startConnection()) {
                        continue;
                    }
                    registrationActivity.runOnUiThread(() -> {
                        registrationActivity.dismissProgressDialog();
                        Toast.makeText(registrationActivity, ConnectionHandler.CONNECTION_ERROR_MESSAGE, Toast.LENGTH_SHORT).show();
                        registrationActivity.startActivity(new Intent(registrationActivity, ConnectionActivity.class));
                        registrationActivity.finishAffinity();
                    });
                }
                registrationActivity.runOnUiThread(() -> registrationActivity.dismissProgressDialog());
                break;
            }
        }).start();

    }

    public void logIn (String nick, String pswd) {

        loginActivity.showProgressDialog();

        new Thread(() -> {
            while (true) {
                int loginOutcome = UserDataAccess.logIn(nick, pswd);
                if (loginOutcome == AuthController.LOGIN_SUCCESS) {
                    AuthController.setCurrUser(nick);
                    loginActivity.goToMainActivity();
                    loginActivity.runOnUiThread(() -> Toast.makeText(loginActivity, nick + " ha effettuato il login", Toast.LENGTH_SHORT).show());
                }
                else if (loginOutcome == AuthController.USER_DOES_NOT_EXIST) {
                    loginActivity.runOnUiThread(() -> loginActivity.getNickInput().setError("Non esiste un account con questo nome"));
                }
                else if (loginOutcome == AuthController.WRONG_PASSWORD) {
                    loginActivity.runOnUiThread(() -> loginActivity.getPswdInput().setError("Password errata"));
                }
                else if (loginOutcome == AuthController.USER_ALREADY_CONNECTED) {
                    loginActivity.runOnUiThread(() -> loginActivity.getNickInput().setError("Questo utente risulta già connesso"));
                }
                else {
                    ConnectionHandler.stopConnection();
                    if (ConnectionHandler.startConnection()) {
                        continue;
                    }
                    loginActivity.runOnUiThread(() -> {
                        loginActivity.dismissProgressDialog();
                        Toast.makeText(loginActivity, ConnectionHandler.CONNECTION_ERROR_MESSAGE, Toast.LENGTH_SHORT).show();
                        loginActivity.startActivity(new Intent(loginActivity, ConnectionActivity.class));
                        loginActivity.finishAffinity();
                    });
                }
                loginActivity.runOnUiThread(() -> loginActivity.dismissProgressDialog());
                break;
            }
        }).start();

    }

    public void logOut (MainActivity mainActivity) {
        currUser = null;

        mainActivity.startActivity(new Intent(mainActivity, AuthActivity.class));
        mainActivity.finishAffinity();

        new Thread(() -> ConnectionHandler.write(String.valueOf(LOGOUT))).start();
    }



    public void setRegistrationActivity (RegistrationActivity activity) {
        this.registrationActivity = activity;
    }

    public void setLoginActivity (LogInActivity activity) {
        this.loginActivity = activity;
    }


    public static void setCurrUser (String nick) {
        currUser = nick;
    }

    public static String getCurrUser() {
        return currUser;
    }

}
