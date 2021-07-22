package com.lso;

import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class GameController {

    private static final String TAG = GameController.class.getSimpleName();

    private static final GameController instance = new GameController();

    public static final int GRID_SIZE = 10;
    public static final int WIN = 10;   // just in case
    public static final int NUMBER_OF_PLAYERS = 2;   // just in case

    public static GameController getInstance() {
        return instance;
    }


    public void lookForMatch (GameActivity activity) {

        ConnectionHandler.write("4");

        try {
            String readVal = ConnectionHandler.read();
            if ("1".equals(readVal)) {
                activity.runOnUiThread(() -> activity.getProgressDialog().dismiss());
                // Inizia partita
            } else {
                activity.runOnUiThread(() -> {
                    activity.getProgressDialog().dismiss();
                    Toast.makeText(activity, "Errore di connessione.", Toast.LENGTH_SHORT).show();
                });
            }
        }
        catch (IOException e) {
            activity.runOnUiThread(() -> {
                activity.getProgressDialog().dismiss();
                Toast.makeText(activity, "Errore di connessione.", Toast.LENGTH_SHORT).show();
            });
        }

    }

    public void getPlayerData (GameActivity activity, ArrayList<Player> players) {

        if (!players.isEmpty()) {
            return;
        }

        String player_data = null;
        String[] player_data_tokens;

        String nickname;
        String symbol;
        int x;
        int y;

        while (true) {

            try {
                player_data = ConnectionHandler.read();
                if ("|".equals(player_data) || player_data == null) break;
                player_data_tokens = player_data.split("\\|");
                nickname = player_data_tokens[0];
                symbol = player_data_tokens[1];
                x = Integer.parseInt(player_data_tokens[2]);
                y = Integer.parseInt(player_data_tokens[3]);

                players.add(new Player(nickname, symbol, x, y));
            } catch (IOException e) {
                activity.runOnUiThread(() -> Toast.makeText(activity, "Errore di connessione.", Toast.LENGTH_SHORT).show());
                e.printStackTrace();
            }

        }

    }

    public void play () {



    }

}
