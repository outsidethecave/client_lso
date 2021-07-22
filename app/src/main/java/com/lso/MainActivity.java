package com.lso;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setActiveUsersButton();
        setFindGameButton();
    }

    @Override
    public void onBackPressed () {
        moveTaskToBack(true);
    }

    private void setActiveUsersButton () {
        Button activeUsersBtn = findViewById(R.id.visualizza_utenti_btn);
        activeUsersBtn.setOnClickListener(v -> goToActiveUsersActivity());
    }

    private void setFindGameButton () {
        Button activeUsersBtn = findViewById(R.id.unisciti_btn);
        activeUsersBtn.setOnClickListener(v -> goToGameActivity());
    }

    private void goToActiveUsersActivity () {
        startActivity(new Intent(this, ActiveUsersActivity.class));
    }

    private void goToGameActivity () {
        startActivity(new Intent(this, GameActivity.class));
    }

}