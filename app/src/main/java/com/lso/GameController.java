package com.lso;

import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.gridlayout.widget.GridLayout;

import java.io.IOException;
import java.util.ArrayList;

public class GameController {

    private static final String TAG = GameController.class.getSimpleName();

    private static final String LOOK_FOR_MATCH = "4";
    private static final String STOP_LOOKING_FOR_MATCH = "5";
    private static final String GAME_ACTION = "6";

    private static final String LEAVE_QUEUE = "1";

    private static final String LEAVE_MATCH = "8";

    private static final char RECEIVE_ACTIVE_PLAYER = '1';
    private static final char SUCCESSFUL_ATTACK = '2';
    private static final char FAILED_ATTACK = '3';
    private static final char MOVE_ON_OWN_SQUARE = '4';
    private static final char MOVE_ON_FREE_SQUARE = '5';
    private static final char MATCH_LEFT = '0';


    public static final int GRID_SIZE = 7;
    private static final int WIN = 10;

    private GameActivity activity;

    private final ArrayList<Player> players = new ArrayList<>();
    private Player activePlayer;
    private String winner;

    private boolean winIsReached = false;
    private boolean hasLeftQueue = false;

    private static final GameController instance = new GameController();
    public static GameController getInstance() {
        return instance;
    }



    private GameController() {}

    public void setActivity(GameActivity activity) {
        this.activity = activity;
    }



    public void lookForMatch () {

        String readVal;

        ConnectionHandler.write(LOOK_FOR_MATCH);

        try {
            readVal = ConnectionHandler.read();
            Log.d(TAG, "lookForMatch: " + readVal);
            if (LEAVE_QUEUE.equals(readVal)) {
                hasLeftQueue = true;
            }
            else {
                hasLeftQueue = false;
                activity.runOnUiThread(activity::dismissProgressDialog);
            }
        }
        catch (IOException e) {
            activity.runOnUiThread(() -> {
                activity.dismissProgressDialog();
                Toast.makeText(activity, "Errore di connessione.", Toast.LENGTH_SHORT).show();
            });
        }

    }


    public void stopLookingForMatch () {

        new Thread(() -> ConnectionHandler.write(STOP_LOOKING_FOR_MATCH)).start();

        hasLeftQueue = false;
        activity.startActivity(new Intent(activity, MainActivity.class));
        activity.finishAffinity();
        Toast.makeText(activity, "Ricerca partita interrotta", Toast.LENGTH_SHORT).show();

    }


    public void getPlayerData () {

        if (!players.isEmpty()) {
            throw new IllegalArgumentException("Dati già ottenuti");
        }

        String playerData;
        String[] playerData_tokens;

        String nickname;
        String symbol;
        int x, y, position;

        while (true) {

            try {

                playerData = ConnectionHandler.read();
                Log.d(TAG, "PLAYER DATA: " + playerData);
                //ConnectionHandler.write(ACK);

                if ("|".equals(playerData) || playerData == null) break;

                playerData_tokens = playerData.split("\\|");    // Separatore

                nickname = playerData_tokens[0];
                symbol = playerData_tokens[1];
                x = Integer.parseInt(playerData_tokens[2]);
                y = Integer.parseInt(playerData_tokens[3]);
                position = GRID_SIZE * x + y;

                players.add(new Player(nickname, symbol, position));

            } catch (IOException e) {
                activity.runOnUiThread(() -> Toast.makeText(activity, "Errore di connessione.", Toast.LENGTH_SHORT).show());
                e.printStackTrace();
            }

        }

    }


    public void setGridToInitialPositions () {

        GridLayout grid = activity.getGrid();

        TextView playerSquare;

        for (Player p : players) {

            playerSquare = (TextView) grid.getChildAt(p.getPosition());

            final TextView playerSquare_final = playerSquare;
            activity.runOnUiThread(() -> {
                playerSquare_final.setText(p.getSymbol());
                playerSquare_final.setTextColor(p.getColor());
            });

        }

    }


    public void play () {

        String serverMessage = "";
        char[] serverMessage_array;

        char action;
        int activePlayer_index;

        while (!winIsReached) {

            try {
                serverMessage = ConnectionHandler.read();
                Log.d(TAG, "PLAY - SERVER MESSAGE: " + serverMessage);
            } catch (IOException e) {
                activity.runOnUiThread(() -> Toast.makeText(activity, "Errore di connessione.", Toast.LENGTH_SHORT).show());
                e.printStackTrace();
            }

            serverMessage_array = serverMessage.toCharArray();

            action = serverMessage_array[0];
            Log.d(TAG, "ACTION: " + action);

            switch (action) {

                case RECEIVE_ACTIVE_PLAYER:
                    activePlayer_index = Integer.parseInt(serverMessage.substring(1));
                    setActivePlayerAndButtons(activePlayer_index);
                break;

                case SUCCESSFUL_ATTACK:
                    updateGridAfterSuccessfulAttack(serverMessage);
                break;

                case FAILED_ATTACK:
                    updateGridAfterFailedAttack(serverMessage);
                break;

                case MOVE_ON_OWN_SQUARE:
                    updateGridAfterMoveToFreeOrOwnSquare(serverMessage, false);
                break;

                case MOVE_ON_FREE_SQUARE:
                    updateGridAfterMoveToFreeOrOwnSquare(serverMessage, true);
                break;

                case MATCH_LEFT:
                    clear();
                    goToMainActivity();
                    return;

            }

        }

        showWinner();

    }
    private void setActivePlayerAndButtons(int index) {
        activity.runOnUiThread(() -> {
            activePlayer = players.get(index);
            Log.d(TAG, "UNO - ACTIVE PLAYER SET: " + index);
            if (activePlayer.getNickname().equals(AuthHandler.getCurrUser())) {
                Toast.makeText(activity, "È il tuo turno", Toast.LENGTH_SHORT).show();
                activity.enableButtons(true);
            } else {
                activity.enableButtons(false);
            }
        });
    }
    private void updateGridAfterSuccessfulAttack (String serverData) {

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
        if (activePlayer.getTerritories() == WIN) {
            winIsReached = true;
            winner = activePlayer.getNickname();
        }
        defendingPlayer.removeTerritory();
        activePlayer.changePosition(GRID_SIZE, direction);

        activePlayer_oldSquare = (TextView) activity.getGrid().getChildAt(oldPosition);
        activePlayer_newSquare = (TextView) activity.getGrid().getChildAt(activePlayer.getPosition());

        activity.runOnUiThread(() -> {
            if (activePlayer_oldSquare.getText().toString().equals(activePlayer.getSymbol())) {
                activePlayer_oldSquare.setTextColor(Color.BLACK);
            }
            activePlayer_newSquare.setText(activePlayer.getSymbol());
            activePlayer_newSquare.setTextColor(activePlayer.getColor());
            Log.d(TAG, "DUE - GRID UPDATED AFTER SUCCESSFUL ATTACK. OLDPOS: " + oldPosition + " NEWPOS: " + activePlayer.getPosition());
        });

    }
    private void updateGridAfterFailedAttack (String serverData) {

        char[] serverData_array = serverData.toCharArray();

        char atk = serverData_array[2];
        char def = serverData_array[3];

        activity.runOnUiThread(() -> {
            activity.setText_atk(atk);
            activity.setText_def(def);
        });

    }
    private void updateGridAfterMoveToFreeOrOwnSquare (String serverData, boolean free) {

        char[] serverData_array = serverData.toCharArray();

        int oldPosition = activePlayer.getPosition();

        char direction;

        TextView activePlayer_oldSquare;
        TextView activePlayer_newSquare;

        direction = serverData_array[1];

        activePlayer.changePosition(GRID_SIZE, direction);
        if (free) {
            activePlayer.addTerritory();
            if (activePlayer.getTerritories() == WIN) {
                winIsReached = true;
                winner = activePlayer.getNickname();
            }
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
            Log.d(TAG, "DUE - GRID UPDATED AFTER CONQUERING " + (free ? "FREE" : "OWN") + " SQUARE. OLDPOS: " + oldPosition + " NEWPOS: " + activePlayer.getPosition());
        });

    }
    private void showWinner() {
        activity.runOnUiThread(() -> {
            goToWinnerActivity();
            clear();
            Log.d(TAG, "ENDING MATCH");
        });
    }
    private void clear() {
        players.clear();
        activePlayer = null;
        winner = null;
        winIsReached = false;
    }

    public void makeMove (char direction) {

        if (direction != 'N' &&
            direction != 'S' &&
            direction != 'E' &&
            direction != 'O') {
            throw new IllegalArgumentException("Direzione non valida");
        }

        if (isValidMove(direction)) {
            ConnectionHandler.write(GAME_ACTION + direction);
        }

    }
    private boolean isValidMove (char direction) {

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


    public void leaveMatch() {

        new Thread(() -> ConnectionHandler.write(LEAVE_MATCH)).start();

    }


    private void goToMainActivity() {
        activity.runOnUiThread(() -> {
            activity.startActivity(new Intent(activity, MainActivity.class));
            activity.finishAffinity();
            Toast.makeText(activity, "Partita abbandonata", Toast.LENGTH_SHORT).show();
        });

    }
    private void goToWinnerActivity() {
        activity.runOnUiThread(() -> {
            Intent intent = new Intent(activity, WinnerActivity.class);
            intent.putExtra("winner", winner);
            activity.startActivity(intent);
            activity.finishAffinity();
        });
    }


    boolean hasLeftQueue() {
        return hasLeftQueue;
    }

}
