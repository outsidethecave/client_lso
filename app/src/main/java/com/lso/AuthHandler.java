package com.lso;

import android.content.Intent;
import android.util.Log;

import java.io.IOException;

public class AuthHandler {

    private static final String TAG = ConnectionHandler.class.getSimpleName();

    private static final String SEPARATOR = "|";

    public static final int SIGNUP = 1;
    public static final int SIGNUP_SUCCESS = 0;
    public static final int USER_ALREADY_EXISTS = 1;
    public static final int GENERIC_SIGNUP_FAILURE = 2;

    public static final int LOGIN = 2;
    public static final int LOGIN_SUCCESS = 0;
    public static final int USER_DOESNT_EXIST = 1;
    public static final int WRONG_PASSWORD = 2;
    public static final int GENERIC_LOGIN_FAILURE = 3;

    public static final int LOGOUT = 7;


    public static String currUser;


    public static int addUser (String nick, String pswd) {
        ConnectionHandler.write(SIGNUP + nick + SEPARATOR + pswd);
        try {
            int outcome = Integer.parseInt(ConnectionHandler.read());
            if (outcome == SIGNUP_SUCCESS) {
                return SIGNUP_SUCCESS;
            }
            else if (outcome == USER_ALREADY_EXISTS) {
                return USER_ALREADY_EXISTS;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return GENERIC_SIGNUP_FAILURE;
        }
        return GENERIC_SIGNUP_FAILURE;
    }

    public static int logIn (String nick, String pswd) {
        ConnectionHandler.write(LOGIN + nick + SEPARATOR + pswd);
        try {
            int outcome = Integer.parseInt(ConnectionHandler.read());
            Log.d(TAG, "LOGIN OUTCOME: " + outcome);
            if (outcome == LOGIN_SUCCESS) {
                return LOGIN_SUCCESS;
            }
            else if (outcome == USER_DOESNT_EXIST) {
                return USER_DOESNT_EXIST;
            }
            else if (outcome == WRONG_PASSWORD) {
                return WRONG_PASSWORD;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return GENERIC_LOGIN_FAILURE;
        }
        return GENERIC_LOGIN_FAILURE;
    }

    public static void logOut (MainActivity activity) {
        currUser = null;

        activity.startActivity(new Intent(activity, AuthActivity.class));
        activity.finishAffinity();

        new Thread(() -> ConnectionHandler.write(String.valueOf(LOGOUT))).start();
    }


    public static void setCurrUser (String nick) {
        currUser = nick;
    }

    public static String getCurrUser() {
        return currUser;
    }

}
