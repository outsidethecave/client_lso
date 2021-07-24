package com.lso;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.gridlayout.widget.GridLayout;

public class GameActivity extends AppCompatActivity {

    private static final String TAG = GameActivity.class.getSimpleName();

    private final GameController controller = GameController.getInstance();

    private GridLayout grid;
    private Button north_btn;
    private Button south_btn;
    private Button east_btn;
    private Button west_btn;
    private TextView atk_txtview;
    private TextView def_txtview;
    private ProgressDialog progressDialog;



    @Override
    protected void onCreate (Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        initButtons();
        initAtkDefViews();
        initGrid();
        setProgressDialog();

        progressDialog.show();

        new Thread(() -> {

            controller.lookForMatch(this);

            controller.getPlayerData(this);

            controller.play(this);

        }).start();

    }



    private void initButtons () {
        north_btn = findViewById(R.id.north_btn);
        south_btn = findViewById(R.id.south_btn);
        east_btn = findViewById(R.id.east_btn);
        west_btn = findViewById(R.id.west_btn);

        // Un po confusionario ma non potevo non usare la doppia lambda
        north_btn.setOnClickListener(v -> new Thread(() -> controller.makeMove(this, 'N')).start());
        south_btn.setOnClickListener(v -> new Thread(() -> controller.makeMove(this, 'S')).start());
        east_btn.setOnClickListener(v -> new Thread(() -> controller.makeMove(this, 'E')).start());
        west_btn.setOnClickListener(v -> new Thread(() -> controller.makeMove(this, 'O')).start());
    }

    private void initAtkDefViews () {
        atk_txtview = (TextView) findViewById(R.id.atk_txtview);
        def_txtview = (TextView) findViewById(R.id.def_txtview);
    }

    private void initGrid () {
        grid = findViewById(R.id.grid);

        TextView square; GridLayout.LayoutParams params;
        for (int i = 0; i < GameController.GRID_SIZE; i++) {
            for (int j = 0; j < GameController.GRID_SIZE; j++) {

                params = new GridLayout.LayoutParams();
                params.columnSpec = GridLayout.spec(i, 1f);
                params.rowSpec = GridLayout.spec(j, 1f);

                square = new TextView(this);
                square.setGravity(Gravity.CENTER);
                square.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.grid_square, getTheme()));
                square.setText(String.valueOf(0));
                square.setTextSize(18);
                square.setTextColor(Color.BLACK);
                square.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                square.setLayoutParams(params);

                grid.addView(square);

            }
        }

    }

    private void setProgressDialog () {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Ricerca partecipanti in corso...");
    }

    public void enableButtons (boolean enabled) {
        north_btn.setEnabled(enabled);
        south_btn.setEnabled(enabled);
        east_btn.setEnabled(enabled);
        west_btn.setEnabled(enabled);
    }



    public void dismissProgressDialog() {
        progressDialog.dismiss();
    }

    public void setText_atk (char atk) {
        atk_txtview.setText(String.valueOf(atk));
    }

    public void setText_def (char def) {
        def_txtview.setText(String.valueOf(def));
    }

    public GridLayout getGrid() {
        return grid;
    }

}