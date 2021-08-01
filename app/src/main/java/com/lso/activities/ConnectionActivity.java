package com.lso.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.lso.ConnectionHandler;
import com.lso.R;

public class ConnectionActivity extends AppCompatActivity {

    private static final String TAG = ConnectionActivity.class.getSimpleName();

    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);

        ConnectionHandler.setConnectionActivity(this);

        setProgressDialog();
        setConnettitiButton();
    }


    private void setConnettitiButton() {

        Button connettitiButton = findViewById(R.id.connettiti_btn);

        connettitiButton.setOnClickListener(v -> ConnectionHandler.makeFirstConnection());
        connettitiButton.callOnClick();

    }

    public void goToAuthActivity() {
        startActivity(new Intent(this, AuthActivity.class));
    }


    public void setProgressDialog () {
        progressDialog = new ProgressDialog(ConnectionActivity.this);
        progressDialog.setMessage("Connessione al server in corso...");
    }
    public void showProgressDialog () {
        progressDialog.show();
    }
    public void dismissProgressDialog () {
        progressDialog.dismiss();
    }

}