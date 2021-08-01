package com.lso.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.lso.control.GameController;
import com.lso.R;

public class GameActivity extends AppCompatActivity {

    private static final String TAG = GameActivity.class.getSimpleName();

    private final GameController controller = GameController.getInstance();

    private Button north_btn;
    private Button south_btn;
    private Button east_btn;
    private Button west_btn;

    private TextView atk_txtview;
    private TextView def_txtview;

    private TextView time_txtview;

    private ProgressDialog progressDialog;


    @Override
    protected void onCreate (Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        controller.setActivity(this);

        initButtons();
        initAtkDefTimeViews();
        setProgressDialog();

        controller.play();

    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Abbandona Partita");
        builder.setMessage("Vuoi uscire dalla partita?");
        builder.setPositiveButton("Abbandona", (dialog, id) -> {
            controller.leaveMatch();
            super.onBackPressed();
        });
        builder.setNegativeButton("Resta", null);

        builder.show();
    }


    private void initButtons () {
        north_btn = findViewById(R.id.north_btn);
        south_btn = findViewById(R.id.south_btn);
        east_btn = findViewById(R.id.east_btn);
        west_btn = findViewById(R.id.west_btn);

        // Un po confusionario ma non potevo non usare la doppia lambda
        north_btn.setOnClickListener(v -> new Thread(() -> controller.makeMove('N')).start());
        south_btn.setOnClickListener(v -> new Thread(() -> controller.makeMove('S')).start());
        east_btn.setOnClickListener(v -> new Thread(() -> controller.makeMove('E')).start());
        west_btn.setOnClickListener(v -> new Thread(() -> controller.makeMove('O')).start());
    }

    private void initAtkDefTimeViews () {
        atk_txtview = findViewById(R.id.atk_txtview);
        def_txtview = findViewById(R.id.def_txtview);
        time_txtview = findViewById(R.id.time_txtview);
    }

    private void setProgressDialog () {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Ricerca partecipanti in corso...");
        progressDialog.setOnCancelListener(dialog -> controller.stopLookingForMatch());
    }

    public void enableButtons (boolean enabled) {
        north_btn.setEnabled(enabled);
        south_btn.setEnabled(enabled);
        east_btn.setEnabled(enabled);
        west_btn.setEnabled(enabled);
    }


    public void showProgressDialog () {progressDialog.show();}
    public void dismissProgressDialog () {
        progressDialog.dismiss();
    }

    public void setText_atk (char atk) {
        atk_txtview.setText(String.valueOf(atk));
    }
    public void setText_def (char def) {
        def_txtview.setText(String.valueOf(def));
    }
    public void setText_time (long secs) {
        String timeleft = "00 : " + (secs >= 10 ? secs : "0" + secs);
        time_txtview.setText(timeleft);
    }

}