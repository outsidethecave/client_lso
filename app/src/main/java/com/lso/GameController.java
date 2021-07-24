package com.lso;

import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.gridlayout.widget.GridLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class GameController {

    private static final String TAG = GameController.class.getSimpleName();

    private static final String LOOK_FOR_MATCH = "4";
    private static final String MATCH_FOUND = "1";
    private static final String ACK = "5";

    private static final char RECIEVE_ACTIVE_PLAYER = '1';
    private static final char SUCCESSFUL_ATTACK = '2';
    private static final char FAILED_ATTACK = '3';
    private static final char MOVE_ON_OWN_SQUARE = '4';
    private static final char MOVE_ON_FREE_SQUARE = '5';
    private static final char PLAYER_WIN = '6';

    private static final GameController instance = new GameController();

    public static final int GRID_SIZE = 10;

    private final ArrayList<Player> players = new ArrayList<>();
    private Player activePlayer;

    public static GameController getInstance() {
        return instance;
    }



    public void lookForMatch (GameActivity activity) {

        ConnectionHandler.write(LOOK_FOR_MATCH);

        try {
            String readVal = ConnectionHandler.read();
            if (MATCH_FOUND.equals(readVal)) {
                ConnectionHandler.write(ACK);
                activity.runOnUiThread(activity::dismissProgressDialog);
            } else {
                activity.runOnUiThread(() -> {
                    activity.dismissProgressDialog();
                    Toast.makeText(activity, "Errore di connessione.", Toast.LENGTH_SHORT).show();
                });
            }
        }
        catch (IOException e) {
            activity.runOnUiThread(() -> {
                activity.dismissProgressDialog();
                Toast.makeText(activity, "Errore di connessione.", Toast.LENGTH_SHORT).show();
            });
        }

    }

    public void getPlayerData (GameActivity activity) {

        if (!players.isEmpty()) {
            throw new IllegalArgumentException("Dati già ottenuti");
        }

        String playerData;
        String[] playerData_tokens;

        String nickname;
        String symbol;
        int x;
        int y;
        int position;

        while (true) {

            try {

                playerData = ConnectionHandler.read();
                ConnectionHandler.write(ACK);

                if ("|".equals(playerData) || playerData == null) break;

                playerData_tokens = playerData.split("\\|");    // Separatore

                nickname = playerData_tokens[0];
                symbol = playerData_tokens[1];
                x = Integer.parseInt(playerData_tokens[2]);
                y = Integer.parseInt(playerData_tokens[3]);
                position = 10*x + y;

                players.add(new Player(nickname, symbol, position));

            } catch (IOException e) {
                activity.runOnUiThread(() -> Toast.makeText(activity, "Errore di connessione.", Toast.LENGTH_SHORT).show());
                e.printStackTrace();
            }

        }

        activity.runOnUiThread(() -> setGridToInitialPositions(activity));

    }
    private void setGridToInitialPositions (GameActivity activity) {

        GridLayout grid = activity.getGrid();

        TextView playerSquare;

        for (Player p : players) {

            playerSquare = (TextView) grid.getChildAt(p.getPosition());

            playerSquare.setText(p.getSymbol());

            playerSquare.setTextColor(p.getColor());

        }

    }

    public void play (GameActivity activity) {

        String serverMessage = "";
        char[] serverMessage_array;

        char action;
        int activePlayer_index;

        while (true) {

            try {
                serverMessage = ConnectionHandler.read();
                ConnectionHandler.write(ACK);
            } catch (IOException e) {
                activity.runOnUiThread(() -> Toast.makeText(activity, "Errore di connessione.", Toast.LENGTH_SHORT).show());
                e.printStackTrace();
            }

            serverMessage_array = serverMessage.toCharArray();

            action = serverMessage_array[0];

            switch (action) {

                case RECIEVE_ACTIVE_PLAYER:
                    activePlayer_index = Integer.parseInt(serverMessage.substring(1));
                    setActivePlayerAndButtons(activity, activePlayer_index);
                break;

                case SUCCESSFUL_ATTACK:
                    updateGridAfterSuccessfulAttack(activity, serverMessage);
                break;

                case FAILED_ATTACK:
                    updateGridAfterFailedAttack(activity, serverMessage);
                break;

                case MOVE_ON_OWN_SQUARE:
                    updateGridAfterMoveToFreeOrOwnSquare(activity, serverMessage, false);
                break;

                case MOVE_ON_FREE_SQUARE:
                    updateGridAfterMoveToFreeOrOwnSquare(activity, serverMessage, true);
                break;

                case PLAYER_WIN:

                break;
            }
        }

    }
    private void setActivePlayerAndButtons(GameActivity activity, int index) {
        activity.runOnUiThread(() -> {
            activePlayer = players.get(index);
            if (activePlayer.getNickname().equals(AuthenticationHandler.getCurrUser())) {
                Toast.makeText(activity, "È il tuo turno", Toast.LENGTH_SHORT).show();
                activity.enableButtons(true);
            } else {
                activity.enableButtons(false);
            }
        });

    }
    private void updateGridAfterSuccessfulAttack (GameActivity activity, String serverData) {

        char[] serverData_array = serverData.toCharArray();

        int oldPosition = activePlayer.getPosition();

        char direction;
        char atk, def;
        int defendingPlayer_index;

        Player defendingPlayer;

        TextView activePlayer_oldSquare;
        TextView activePlayer_newSquare;

        direction = serverData_array[1];
        atk = serverData_array[2];
        def = serverData_array[3];
        defendingPlayer_index = Integer.parseInt(serverData.substring(4));
        defendingPlayer = players.get(defendingPlayer_index);

        activity.runOnUiThread(() -> {
            activity.setText_atk(atk);
            activity.setText_def(def);
        });

        activePlayer.addTerritory();
        defendingPlayer.removeTerritory();
        activePlayer.move(direction);

        activePlayer_oldSquare = (TextView) activity.getGrid().getChildAt(oldPosition);
        activePlayer_newSquare = (TextView) activity.getGrid().getChildAt(activePlayer.getPosition());

        activity.runOnUiThread(() -> {
            if (activePlayer_oldSquare.getText().toString().equals(activePlayer.getSymbol())) {
                activePlayer_oldSquare.setTextColor(Color.BLACK);
            }
            activePlayer_newSquare.setText(activePlayer.getSymbol());
            activePlayer_newSquare.setTextColor(activePlayer.getColor());
        });

    }
    private void updateGridAfterFailedAttack (GameActivity activity, String serverData) {

        char[] serverData_array = serverData.toCharArray();

        char atk = serverData_array[2];
        char def = serverData_array[3];

        activity.runOnUiThread(() -> {
            activity.setText_atk(atk);
            activity.setText_def(def);
        });

    }
    private void updateGridAfterMoveToFreeOrOwnSquare (GameActivity activity, String serverData, boolean free) {

        char[] serverData_array = serverData.toCharArray();

        int oldPosition = activePlayer.getPosition();

        char direction;

        TextView activePlayer_oldSquare;
        TextView activePlayer_newSquare;

        direction = serverData_array[1];

        activePlayer.move(direction);
        if (free) {
            activePlayer.addTerritory();
        }

        activePlayer_oldSquare = (TextView) activity.getGrid().getChildAt(oldPosition);
        activePlayer_newSquare = (TextView) activity.getGrid().getChildAt(activePlayer.getPosition());

        activity.runOnUiThread(() -> {
            if (activePlayer_oldSquare.getText().toString().equals(activePlayer.getSymbol())) {
                activePlayer_oldSquare.setTextColor(Color.BLACK);
            }
            if (free) {
                activePlayer_newSquare.setText(activePlayer.getSymbol());
            }
            activePlayer_newSquare.setTextColor(activePlayer.getColor());
        });

    }
    private void endMatch (GameActivity activity) {

    }

    public void makeMove (GameActivity activity, char direction) {

        if (direction != 'N' &&
            direction != 'S' &&
            direction != 'E' &&
            direction != 'O') {
            throw new IllegalArgumentException("Direzione non valida");
        }

        if (isValidMove(activity, direction)) {
            ConnectionHandler.write(String.valueOf(direction));
        }

    }
    private boolean isValidMove (GameActivity activity, char direction) {

        switch (direction) {

            case 'N':
                if (activePlayer.getPosition() % GRID_SIZE == 0) {
                    Log.d(TAG, "makeMove: tentativo di uscire dalla mappa a Nord");
                    activity.runOnUiThread(() -> Toast.makeText(activity, "Non puoi uscire dalla mappa!", Toast.LENGTH_SHORT).show());
                    return false;
                }
                break;
            case 'S':
                if (activePlayer.getPosition() % GRID_SIZE == GRID_SIZE - 1) {
                    Log.d(TAG, "makeMove: tentativo di uscire dalla mappa a Sud");
                    activity.runOnUiThread(() -> Toast.makeText(activity, "Non puoi uscire dalla mappa!", Toast.LENGTH_SHORT).show());
                    return false;
                }
                break;
            case 'O':
                if (activePlayer.getPosition() - GRID_SIZE < 0) {
                    Log.d(TAG, "makeMove: tentativo di uscire dalla mappa a Ovest");
                    activity.runOnUiThread(() -> Toast.makeText(activity, "Non puoi uscire dalla mappa!", Toast.LENGTH_SHORT).show());
                    return false;
                }
                break;
            case 'E':
                if (activePlayer.getPosition() + GRID_SIZE >= GRID_SIZE * GRID_SIZE) {
                    Log.d(TAG, "makeMove: tentativo di uscire dalla mappa a Est");
                    activity.runOnUiThread(() -> Toast.makeText(activity, "Non puoi uscire dalla mappa!", Toast.LENGTH_SHORT).show());
                    return false;
                }
                break;
        }
        return true;

    }

}
