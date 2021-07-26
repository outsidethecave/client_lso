package com.lso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setActiveUsersButton();
        setFindGameButton();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.logout_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected (@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            AuthHandler.logOut(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
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