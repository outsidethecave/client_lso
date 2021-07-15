package com.lso;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ConnectionActivity extends AppCompatActivity {

    private static final String TAG = ConnectionActivity.class.getSimpleName();
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);

        setConnettitiButton();
    }


    private void setConnettitiButton() {

        Button connettitiButton = findViewById(R.id.connettiti_btn);
        connettitiButton.setOnClickListener(v -> {
            progressDialog = new ProgressDialog(ConnectionActivity.this);
            progressDialog.setMessage("Connessione al server in corso...");
            progressDialog.show();

            new Thread(() -> {
                if (ConnectionHandler.startConnection()) {
                    runOnUiThread(() -> progressDialog.dismiss());
                    goToMainActivity();
                } else {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(ConnectionActivity.this, "Errore di connessione.", Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();
        });
        connettitiButton.performClick();

    }

    private void goToMainActivity() {
        startActivity(new Intent(this, AuthenticationActivity.class));
    }

}