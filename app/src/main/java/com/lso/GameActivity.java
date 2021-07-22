package com.lso;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.gridlayout.widget.GridLayout;

import java.io.IOException;
import java.util.ArrayList;

public class GameActivity extends AppCompatActivity {

    private static final String TAG = GameActivity.class.getSimpleName();

    private final GameController controller = GameController.getInstance();

    private ProgressDialog progressDialog;



    @Override
    protected void onCreate (Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        initGrid();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Ricerca partecipanti in corso...");
        progressDialog.show();

        new Thread(() -> {

            controller.lookForMatch(this);

            ArrayList<Player> players = new ArrayList<>();
            controller.getPlayerData(players);

        }).start();

    }



    private void initGrid () {
        GridLayout grid = findViewById(R.id.grid);

        TextView square; GridLayout.LayoutParams params;
        for (int i = 0; i < GameController.GRID_SIZE; i++) {
            for (int j = 0; j < GameController.GRID_SIZE; j++) {
                params = new GridLayout.LayoutParams();
                params.columnSpec = GridLayout.spec(i, 1f);
                params.rowSpec = GridLayout.spec(j, 1f);
                square = new TextView(this);
                square.setGravity(Gravity.CENTER);
                square.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                square.setTextSize(18);
                square.setTextColor(getResources().getColor(R.color.green));
                square.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.grid_square, getTheme()));
                square.setText(String.valueOf(0));
                square.setLayoutParams(params);
                grid.addView(square);
            }
        }
    }



    public ProgressDialog getProgressDialog() {
        return progressDialog;
    }
}