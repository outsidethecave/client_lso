package com.lso.control;

import android.content.Intent;
import android.widget.Toast;

import com.lso.ConnectionHandler;
import com.lso.UserDataAccess;
import com.lso.activities.ActiveUsersActivity;
import com.lso.activities.ConnectionActivity;

import java.util.ArrayList;
import java.util.List;

public class ActiveUsersController {

    private ActiveUsersActivity activity;

    private final List<String> utenti = new ArrayList<>();

    private static final ActiveUsersController instance = new ActiveUsersController();
    public static ActiveUsersController getInstance() {
        return instance;
    }



    private ActiveUsersController() {}



    public void showActiveUsers () {

        new Thread(() -> {
            boolean usersFetched = UserDataAccess.fetchActiveUsers(utenti);
            if (!usersFetched) {
                ConnectionHandler.stopConnection();
                activity.runOnUiThread(() -> {
                    Toast.makeText(activity, "Errore di connessione", Toast.LENGTH_SHORT).show();
                    activity.startActivity(new Intent(activity, ConnectionActivity.class));
                    activity.finishAffinity();
                });
            }
            else {
                activity.runOnUiThread(() -> activity.notifyAdapter());
            }
        }).start();

    }


    public List<String> getUtenti() {
        return utenti;
    }

    public void setActivity (ActiveUsersActivity activity) {
        this.activity = activity;
    }
}
