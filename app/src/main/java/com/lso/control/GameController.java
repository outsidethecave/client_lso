package com.lso.control;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@SuppressLint("SetTextI18n")
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

    private static final String ARROW = "\u2023";

    private GridLayout grid;

    private int numberOfPlayers = 0;
    private int gridSize;
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

                game();

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

        numberOfPlayers = players.size();
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
                square.setTextSize(42 - 2 * gridSize);
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
                playerSquare.setText(ARROW + p.getSymbol());
                playerSquare.setBackgroundColor(p.getColor());
            });

        }

    }

    private void game() {

        String serverMessage;
        long timerEnd;
        char[] serverMessage_array;

        char event;
        int oldActivePlayer_index;
        int activePlayer_index = -1;

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
                    oldActivePlayer_index = activePlayer_index;
                    activePlayer_index = Integer.parseInt(serverMessage.substring(1, 3));
                    timerEnd = Long.parseLong(serverMessage.substring(3));
                    clearGridOfDeserters(oldActivePlayer_index, activePlayer_index);
                    setActivePlayerAndButtonsAndTime(activePlayer_index, timerEnd);
                break;

                case SUCCESSFUL_ATTACK:
                    cancelTimer(timer);
                    handleSuccessfulAttack(serverMessage);
                break;

                case FAILED_ATTACK:
                    cancelTimer(timer);
                    handleFailedAttack(serverMessage);
                break;

                case MOVE_ON_OWN_SQUARE:
                    cancelTimer(timer);
                    handleMoveToFreeOrOwnSquare(serverMessage, false);
                break;

                case MOVE_ON_FREE_SQUARE:
                    cancelTimer(timer);
                    handleMoveToFreeOrOwnSquare(serverMessage, true);
                break;

                case TIME_ENDED:
                    if (activePlayer.getNickname().equals(AuthController.getCurrUser())) {
                        activity.runOnUiThread(() -> Toast.makeText(activity, "Tempo scaduto!", Toast.LENGTH_SHORT).show());
                    }
                    activity.log(activePlayer.getColor(), EVENT_LOGMSG_SIZE, true, " Tempo scaduto per " + activePlayer.getNickname() + ".", 2);
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

            activePlayer = players.get(index);
            activity.setColor_time(activePlayer.getColor());
            timer = startTimer(timerEnd);

            if (activePlayer.getNickname().equals(AuthController.getCurrUser())) {
                Toast.makeText(activity, "È il tuo turno", Toast.LENGTH_SHORT).show();
                activity.enableButtons(true);
            } else {
                activity.enableButtons(false);
            }

            activity.log(activePlayer.getColor(), EVENT_LOGMSG_SIZE, true, "Tocca a " + activePlayer.getNickname() + " (Posizione: " + activePlayer.getPosition() + " Punteggio: " + activePlayer.getTerritories() + ")", 2);

        });
    }
    private void handleSuccessfulAttack (String serverData) {

        char[] serverData_array = serverData.toCharArray();

        String logMessage;

        int oldPosition = activePlayer.getPosition();

        char direction;
        char atk, def;
        int defendingPlayer_index;

        Player defendingPlayer = null;

        direction = serverData_array[1];
        atk = serverData_array[2];
        def = serverData_array[3];
        defendingPlayer_index = Integer.parseInt(serverData.substring(4));

        if (defendingPlayer_index != -1) {
            defendingPlayer = players.get(defendingPlayer_index);
        }

        Player finalDefendingPlayer = defendingPlayer;
        activity.runOnUiThread(() -> {
            activity.setText_atk(atk, activePlayer.getColor());
            if (finalDefendingPlayer != null) {
                activity.setText_def(def, finalDefendingPlayer.getColor());
            }
            else {
                activity.setText_def(def, Color.GRAY);
            }
        });

        activePlayer.addTerritory();
        if (activePlayer.getTerritories() == winCondition) {
            winIsReached = true;
            winner = activePlayer.getNickname();
        }
        if (defendingPlayer != null) {
            defendingPlayer.removeTerritory();
        }
        activePlayer.changePosition(gridSize, direction);

        updateGrid(activePlayer, oldPosition);

        if (activePlayer.getNickname().equals(AuthController.getCurrUser())) {
            activity.runOnUiThread(() -> Toast.makeText(activity, "Territorio conquistato!", Toast.LENGTH_SHORT).show());
        }

        logMessage = activePlayer.getNickname() + " si sposta in posizione " + activePlayer.getPosition() + ", sottraendola a " + (defendingPlayer != null ? defendingPlayer.getNickname() : "un disertore") + " [ATK: " + atk + " DEF: " + def + "].";
        activity.log(activePlayer.getColor(), EVENT_LOGMSG_SIZE, true, logMessage, 2);

    }
    private void handleFailedAttack (String serverData) {

        char[] serverData_array = serverData.toCharArray();

        String logMessage;

        char atk = serverData_array[2];
        char def = serverData_array[3];
        int defendingPlayer_index = Integer.parseInt(serverData.substring(4));

        Player defendingPlayer = null;

        if (defendingPlayer_index != -1) {
            defendingPlayer = players.get(defendingPlayer_index);
        }

        Player finalDefendingPlayer = defendingPlayer;
        activity.runOnUiThread(() -> {
            activity.setText_atk(atk, activePlayer.getColor());
            if (finalDefendingPlayer != null) {
                activity.setText_def(def, finalDefendingPlayer.getColor());
            }
            else {
                activity.setText_def(def, Color.GRAY);
            }
        });

        if (activePlayer.getNickname().equals(AuthController.getCurrUser())) {
            activity.runOnUiThread(() -> Toast.makeText(activity, "Attacco fallito!", Toast.LENGTH_SHORT).show());
        }

        if (defendingPlayer != null) {
            logMessage = activePlayer.getNickname() + " prova a spostarsi in posizione " + defendingPlayer.getPosition() + ", posseduta da " + defendingPlayer.getNickname() + ", ma fallisce [ATK: " + atk + " DEF: " + def + "].";
        }
        else {
            logMessage = activePlayer.getNickname() + " prova a spostarsi in una posizione posseduta da un disertore, ma le sue difese reggono ancora [ATK: " + atk + " DEF: " + def + "].";
        }
        activity.log(activePlayer.getColor(), EVENT_LOGMSG_SIZE, true, logMessage, 2);

    }
    private void handleMoveToFreeOrOwnSquare (String serverData, boolean free) {

        char[] serverData_array = serverData.toCharArray();

        String logMessage;

        int oldPosition = activePlayer.getPosition();

        char direction;

        direction = serverData_array[1];

        activePlayer.changePosition(gridSize, direction);
        if (free) {
            activePlayer.addTerritory();
            if (activePlayer.getTerritories() == winCondition) {
                winIsReached = true;
                winner = activePlayer.getNickname();
            }
        }

        updateGrid(activePlayer, oldPosition);

        if (free) {
            logMessage = activePlayer.getNickname() + " si sposta in posizione " + activePlayer.getPosition() + ", conquistandola.";
        }
        else {
            logMessage = activePlayer.getNickname() + " si sposta in posizione " + activePlayer.getPosition() + ", già posseduta";
        }

        activity.log(activePlayer.getColor(), EVENT_LOGMSG_SIZE, true, logMessage, 2);

    }
    private void updateGrid (Player activePlayer, int oldPosition) {

        TextView activePlayer_oldSquare = (TextView) grid.getChildAt(oldPosition);
        TextView activePlayer_newSquare = (TextView) grid.getChildAt(activePlayer.getPosition());

        String oldSquareText = activePlayer_oldSquare.getText().toString();
        String newSquareText = activePlayer_newSquare.getText().toString();

        activity.runOnUiThread(() -> {
            if (oldSquareText.equals(ARROW + activePlayer.getSymbol())) {
                activePlayer_oldSquare.setText(activePlayer.getSymbol());
            }
            else {
                activePlayer_oldSquare.setText(oldSquareText.replace(ARROW + activePlayer.getSymbol(), ""));
            }
            if (newSquareText.contains(ARROW)) {
                activePlayer_newSquare.append(ARROW + activePlayer.getSymbol());
            }
            else {
                activePlayer_newSquare.setText(ARROW + activePlayer.getSymbol());
            }
            activePlayer_newSquare.setBackgroundColor(activePlayer.getColor());
        });

    }
    private void clearGridOfDeserters (int oldActivePlayer_index, int newActivePlayer_index) {

        int deserter_index;
        Set<String> deserterSymbols = new HashSet<>();

        if (newActivePlayer_index == oldActivePlayer_index + 1 || oldActivePlayer_index == numberOfPlayers - 1 && newActivePlayer_index == 0) {
            return;
        }

        deserter_index = (oldActivePlayer_index == numberOfPlayers - 1 ? 0 : oldActivePlayer_index + 1);

        while (deserter_index != newActivePlayer_index) {
            activity.log(players.get(deserter_index).getColor(), EVENT_LOGMSG_SIZE, true, players.get(deserter_index).getNickname() + " ha abbandonato la partita", 2);
            deserterSymbols.add(players.get(deserter_index).getSymbol());

            deserter_index++;
            if (deserter_index == numberOfPlayers) {
                deserter_index = 0;
            }
        }

        activity.runOnUiThread(() -> {

            TextView cell;
            String cellText;
            for (int i = 0; i < gridSize * gridSize; i++) {
                cell = ((TextView)grid.getChildAt(i));
                cellText = cell.getText().toString();
                for (String symbol : deserterSymbols) {
                    if (cellText.contains(symbol)) {
                        cell.setText(cellText.replace(ARROW + symbol, "").replace(symbol, ""));
                    }
                }
            }

        });

    }
    private void showWinner () {
        activity.runOnUiThread(() -> {
            goToWinnerActivity();
            clear();
        });
    }
    private void clear () {
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
                    activity.runOnUiThread(() -> Toast.makeText(activity, "Non puoi uscire dalla mappa!", Toast.LENGTH_SHORT).show());
                    return false;
                }
                break;
            case 'S':
                if (activePlayer.getPosition() % gridSize == gridSize - 1) {
                    activity.runOnUiThread(() -> Toast.makeText(activity, "Non puoi uscire dalla mappa!", Toast.LENGTH_SHORT).show());
                    return false;
                }
                break;
            case 'O':
                if (activePlayer.getPosition() - gridSize < 0) {
                    activity.runOnUiThread(() -> Toast.makeText(activity, "Non puoi uscire dalla mappa!", Toast.LENGTH_SHORT).show());
                    return false;
                }
                break;
            case 'E':
                if (activePlayer.getPosition() + gridSize >= gridSize * gridSize) {
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
