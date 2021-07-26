package com.lso;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class WinnerActivity extends AppCompatActivity {

    private Button menuBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_winner);

        String winner = "VINCE IL GIOCATORE " + getIntent().getStringExtra("winner");

        TextView winner_txtview = (TextView) findViewById(R.id.winner_txtview);
        winner_txtview.setText(winner);

        menuBtn = (Button) findViewById(R.id.torna_al_menu_btn_winner);
        menuBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finishAffinity();
        });

    }

    @Override
    public void onBackPressed() {
        menuBtn.performClick();
    }
}