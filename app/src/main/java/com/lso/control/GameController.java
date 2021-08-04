package com.lso.control;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.content.res.ResourcesCompat;
import androidx.gridlayout.widget.GridLayout;

import com.lso.ConnectionHandler;
import com.lso.Player;
import com.lso.R;
import com.lso.activities.ConnectionActivity;
import com.lso.activities.GameActivity;
import com.lso.activities.MainActivity;
import com.lso.activities.WinnerActivity;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;

public class GameController {

    private static final String TAG = GameController.class.getSimpleName();

    private static final String LOOK_FOR_MATCH = "4";
    private static final String STOP_LOOKING_FOR_MATCH = "5";
    private static final String GAME_ACTION = "6";

    private static final String LEAVE_QUEUE = "1";

    private static final String LEAVE_MATCH = "7";

    private static final char RECEIVE_ACTIVE_PLAYER_AND_TIME = '1';
    private static final char SUCCESSFUL_ATTACK = '2';
    private static final char FAILED_ATTACK = '3';
    private static final char MOVE_ON_OWN_SQUARE = '4';
    private static final char MOVE_ON_FREE_SQUARE = '5';
    private static final char TIME_ENDED = '6';
    private static final char MATCH_LEFT = '0';


    private static final int FIRST_LOGMSG_SIZE = 30;
    private static final int SECOND_LOGMSG_SIZE = 25;
    private static final int EVENT_LOGMSG_SIZE = 20;

    private GridLayout grid;
    public int gridSize;
    private int winCondition;

    private GameActivity activity;

    private final ArrayList<Player> players = new ArrayList<>();

    private CountDownTimer timer;
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



    public void play () {

        activity.showProgressDialog();

        new Thread(() -> {

            lookForMatch();

            if (!hasLeftQueue) {

                getGameData();

                initGrid();

                startGame();

            }

            hasLeftQueue = false;

        }).start();

    }


    private void lookForMatch () {

        String readVal;

        ConnectionHandler.write(LOOK_FOR_MATCH);

        try {
            readVal = ConnectionHandler.read();
            if (LEAVE_QUEUE.equals(readVal)) {
                hasLeftQueue = true;
            }
            else {
                hasLeftQueue = false;
                activity.runOnUiThread(activity::dismissProgressDialog);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            activity.runOnUiThread(() -> {
                activity.dismissProgressDialog();
                Toast.makeText(activity, ConnectionHandler.CONNECTION_ERROR_MESSAGE, Toast.LENGTH_SHORT).show();
            });
            clear();
            activity.startActivity(new Intent(activity, ConnectionActivity.class));
            activity.finishAffinity();
        }
    }
    public void stopLookingForMatch () {

        new Thread(() -> ConnectionHandler.write(STOP_LOOKING_FOR_MATCH)).start();

        hasLeftQueue = false;
        activity.startActivity(new Intent(activity, MainActivity.class));
        activity.finishAffinity();
        Toast.makeText(activity, "Ricerca partita interrotta", Toast.LENGTH_SHORT).show();

    }

    private void getGameData() {

        if (!players.isEmpty()) {
            throw new IllegalArgumentException("Dati già ottenuti");
        }

        String gameData;
        String[] gameData_tokens;
        
        boolean gridAndWinRecieved = false;

        String nickname;
        String symbol;
        int x, y, position;

        Player p;
        String playerMessage;

        while (true) {

            try {

                gameData = ConnectionHandler.read();

                if ("|".equals(gameData)) break;
                if (gameData == null) throw new IOException();

                gameData_tokens = gameData.split("\\|");    // Separatore

                if (!gridAndWinRecieved) {
                    gridSize = Integer.parseInt(gameData_tokens[0]);
                    winCondition = Integer.parseInt(gameData_tokens[1]);
                    gridAndWinRecieved = true;

                    activity.log(0, FIRST_LOGMSG_SIZE, false, "TERRITORI DA CONQUISTARE: " + winCondition, 2);
                    activity.log(0, SECOND_LOGMSG_SIZE, false, "GIOCATORI: ", 1);
                }

                nickname = gameData_tokens[2];
                symbol = gameData_tokens[3];
                x = Integer.parseInt(gameData_tokens[4]);
                y = Integer.parseInt(gameData_tokens[5]);
                position = gridSize * x + y;

                p = new Player(nickname, symbol, position);
                players.add(p);

                playerMessage = p.getSymbol() + " - " + p.getNickname() + "  (Posizione " + p.getPosition() + ")";
                activity.log(p.getColor(), 20, false,  playerMessage, 1);

            } catch (IOException e) {
                e.printStackTrace();

                activity.runOnUiThread(() -> Toast.makeText(activity, ConnectionHandler.CONNECTION_ERROR_MESSAGE, Toast.LENGTH_SHORT).show());

                clear();
                activity.startActivity(new Intent(activity, ConnectionActivity.class));
                activity.finishAffinity();
                break;
            }

        }

        activity.log(0, 20, false, "", 3);

    }

    private void makeGrid() {

        grid = activity.findViewById(R.id.grid);

        TextView square; GridLayout.LayoutParams params;
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {

                params = new GridLayout.LayoutParams();
                params.columnSpec = GridLayout.spec(i, 1f);
                params.rowSpec = GridLayout.spec(j, 1f);

                square = new TextView(activity);

                square.setGravity(Gravity.CENTER);
                square.setForeground(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.grid_square, activity.getTheme()));
                square.setText(String.valueOf(0));
                square.setTextSize(18);
                square.setTextColor(Color.BLACK);
                square.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                square.setLayoutParams(params);

                TextView finalSquare = square;
                activity.runOnUiThread(() -> grid.addView(finalSquare));

            }
        }

    }

    private void initGrid() {

        makeGrid();

        for (Player p : players) {

            activity.runOnUiThread(() -> {
                TextView playerSquare = (TextView) grid.getChildAt(p.getPosition());
                playerSquare.setText(p.getSymbol());
                playerSquare.setBackgroundColor(p.getColor());
            });

        }

    }

    private void startGame() {

        String serverMessage;
        long timerEnd;
        char[] serverMessage_array;

        char event;
        int activePlayer_index;

        while (!winIsReached) {

            try {

                serverMessage = ConnectionHandler.read();

                if (serverMessage == null) throw new IOException();

            } catch (IOException e) {
                e.printStackTrace();

                if (timer != null) timer.cancel();

                activity.runOnUiThread(() -> Toast.makeText(activity, ConnectionHandler.CONNECTION_ERROR_MESSAGE, Toast.LENGTH_SHORT).show());

                clear();
                activity.startActivity(new Intent(activity, ConnectionActivity.class));
                activity.finishAffinity();
                return;
            }

            serverMessage_array = serverMessage.toCharArray();

            event = serverMessage_array[0];

            switch (event) {

                case RECEIVE_ACTIVE_PLAYER_AND_TIME:
                    cancelTimer(timer);
                    activePlayer_index = Integer.parseInt(serverMessage.substring(1, 3));
                    timerEnd = Long.parseLong(serverMessage.substring(3));
                    setActivePlayerAndButtonsAndTime(activePlayer_index, timerEnd);
                break;

                case SUCCESSFUL_ATTACK:
                    cancelTimer(timer);
                    updateGridAfterSuccessfulAttack(serverMessage);
                break;

                case FAILED_ATTACK:
                    cancelTimer(timer);
                    updateGridAfterFailedAttack(serverMessage);
                break;

                case MOVE_ON_OWN_SQUARE:
                    cancelTimer(timer);
                    updateGridAfterMoveToFreeOrOwnSquare(serverMessage, false);
                break;

                case MOVE_ON_FREE_SQUARE:
                    cancelTimer(timer);
                    updateGridAfterMoveToFreeOrOwnSquare(serverMessage, true);
                break;

                case TIME_ENDED:
                    activity.log(activePlayer.getColor(), EVENT_LOGMSG_SIZE, true, " Tempo scaduto per " + activePlayer.getNickname() + ".", 2);
                    if (activePlayer.getNickname().equals(AuthController.getCurrUser())) {
                        activity.runOnUiThread(() -> Toast.makeText(activity, "Tempo scaduto!", Toast.LENGTH_SHORT).show());
                    }
                break;

                case MATCH_LEFT:
                    cancelTimer(timer);
                    clear();
                    goToMainActivity();
                return;

                default:
                    return;

            }

        }

        showWinner();

    }
    private void setActivePlayerAndButtonsAndTime(int index, long timerEnd) {
        activity.runOnUiThread(() -> {

            timer = startTimer(timerEnd);
            activePlayer = players.get(index);

            activity.log(activePlayer.getColor(), EVENT_LOGMSG_SIZE, true, "Tocca a " + activePlayer.getNickname() + " (Posizione: " + activePlayer.getPosition() + " Punteggio: " + activePlayer.getTerritories() + ")", 2);

            if (activePlayer.getNickname().equals(AuthController.getCurrUser())) {
                Toast.makeText(activity, "È il tuo turno", Toast.LENGTH_SHORT).show();
                activity.enableButtons(true);
            } else {
                activity.enableButtons(false);
            }

        });
    }
    private void updateGridAfterSuccessfulAttack (String serverData) {

        char[] serverData_array = serverData.toCharArray();

        String logMessage;

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
        if (activePlayer.getTerritories() == winCondition) {
            winIsReached = true;
            winner = activePlayer.getNickname();
        }
        defendingPlayer.removeTerritory();
        activePlayer.changePosition(gridSize, direction);

        activePlayer_oldSquare = (TextView) grid.getChildAt(oldPosition);
        activePlayer_newSquare = (TextView) grid.getChildAt(activePlayer.getPosition());

        activity.runOnUiThread(() -> {
            if (activePlayer_oldSquare.getText().toString().equals(activePlayer.getSymbol())) {
                activePlayer_oldSquare.setBackgroundColor(Color.WHITE);
            }
            activePlayer_newSquare.setText(activePlayer.getSymbol());
            activePlayer_newSquare.setBackgroundColor(activePlayer.getColor());
        });

        logMessage = activePlayer.getNickname() + " si sposta in posizione " + activePlayer.getPosition() + ", sottraendola a " + defendingPlayer.getNickname() + " [ATK: " + atk + " DEF: " + def + "].";
        activity.log(activePlayer.getColor(), EVENT_LOGMSG_SIZE, true, logMessage, 2);

    }
    private void updateGridAfterFailedAttack (String serverData) {

        char[] serverData_array = serverData.toCharArray();

        String logMessage;

        char atk = serverData_array[2];
        char def = serverData_array[3];
        int defendingPlayer_index = Integer.parseInt(serverData.substring(4));

        Player defendingPlayer = players.get(defendingPlayer_index);

        logMessage = activePlayer.getNickname() + " prova a spostarsi in posizione " + defendingPlayer.getPosition() + ", posseduta da " + defendingPlayer.getNickname() + ", ma fallisce [ATK: " + atk + " DEF: " + def + "].";
        activity.log(activePlayer.getColor(), EVENT_LOGMSG_SIZE, true, logMessage, 2);

        activity.runOnUiThread(() -> {
            activity.setText_atk(atk);
            activity.setText_def(def);
        });

    }
    private void updateGridAfterMoveToFreeOrOwnSquare (String serverData, boolean free) {

        char[] serverData_array = serverData.toCharArray();

        String logMessage;

        int oldPosition = activePlayer.getPosition();

        char direction;

        TextView activePlayer_oldSquare;
        TextView activePlayer_newSquare;

        direction = serverData_array[1];

        activePlayer.changePosition(gridSize, direction);
        if (free) {
            activePlayer.addTerritory();
            if (activePlayer.getTerritories() == winCondition) {
                winIsReached = true;
                winner = activePlayer.getNickname();
            }
        }

        activePlayer_oldSquare = (TextView) grid.getChildAt(oldPosition);
        activePlayer_newSquare = (TextView) grid.getChildAt(activePlayer.getPosition());

        activity.runOnUiThread(() -> {
            if (activePlayer_oldSquare.getText().toString().equals(activePlayer.getSymbol())) {
                activePlayer_oldSquare.setBackgroundColor(Color.WHITE);
            }
            if (free) {
                activePlayer_newSquare.setText(activePlayer.getSymbol());
            }
            activePlayer_newSquare.setBackgroundColor(activePlayer.getColor());
        });

        if (free) {
            logMessage = activePlayer.getNickname() + " si sposta in posizione " + activePlayer.getPosition() + ", conquistandola.";
        }
        else {
            logMessage = activePlayer.getNickname() + " si sposta in posizione " + activePlayer.getPosition() + ", già posseduta";
        }

        activity.log(activePlayer.getColor(), EVENT_LOGMSG_SIZE, true, logMessage, 2);

    }
    private void showWinner() {
        activity.runOnUiThread(() -> {
            goToWinnerActivity();
            clear();
        });
    }
    private void clear() {
        players.clear();
        activePlayer = null;
        winner = null;
        winIsReached = false;
    }

    private CountDownTimer startTimer (long timerEnd) {
        long runTime = timerEnd * 1000 - System.currentTimeMillis();

        return new CountDownTimer(runTime, 1000) {

            @Override
            public void onTick (long millisUntilFinished) {
                activity.setText_time(millisUntilFinished/1000);
            }

            @Override
            public void onFinish () {
                // Se la vede il server
            }

        }.start();
    }
    private void cancelTimer (CountDownTimer timer) {
        if (timer != null) {
            timer.cancel();
        }
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
                if (activePlayer.getPosition() % gridSize == 0) {
                    Log.d(TAG, "makeMove: tentativo di uscire dalla mappa a Nord");
                    activity.runOnUiThread(() -> Toast.makeText(activity, "Non puoi uscire dalla mappa!", Toast.LENGTH_SHORT).show());
                    return false;
                }
                break;
            case 'S':
                if (activePlayer.getPosition() % gridSize == gridSize - 1) {
                    Log.d(TAG, "makeMove: tentativo di uscire dalla mappa a Sud");
                    activity.runOnUiThread(() -> Toast.makeText(activity, "Non puoi uscire dalla mappa!", Toast.LENGTH_SHORT).show());
                    return false;
                }
                break;
            case 'O':
                if (activePlayer.getPosition() - gridSize < 0) {
                    Log.d(TAG, "makeMove: tentativo di uscire dalla mappa a Ovest");
                    activity.runOnUiThread(() -> Toast.makeText(activity, "Non puoi uscire dalla mappa!", Toast.LENGTH_SHORT).show());
                    return false;
                }
                break;
            case 'E':
                if (activePlayer.getPosition() + gridSize >= gridSize * gridSize) {
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

}
