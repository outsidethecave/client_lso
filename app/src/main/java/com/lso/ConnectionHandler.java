package com.lso;

import android.widget.Toast;

import com.lso.activities.ConnectionActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ConnectionHandler {

    private static ConnectionActivity connectionActivity;

    //private static final String SERVER_IP = "20.203.137.149";
    private static final String SERVER_IP = "192.168.1.75";
    private static final int SERVER_PORT = 50000;

    public static final String CONNECTION_ERROR_MESSAGE = "Errore di connessione";

    private static Socket clientSocket;
    private static PrintWriter out;
    private static BufferedReader in;


    public static boolean startConnection () {

        clientSocket = new Socket();
        try {
            clientSocket.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT), 5000);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }

    public static void makeFirstConnection () {

        connectionActivity.showProgressDialog();

        new Thread(() -> {
            if (ConnectionHandler.startConnection()) {
                connectionActivity.runOnUiThread(() -> connectionActivity.dismissProgressDialog());
                connectionActivity.goToAuthActivity();
            } else {
                connectionActivity.runOnUiThread(() -> {
                    connectionActivity.dismissProgressDialog();
                    Toast.makeText(connectionActivity, CONNECTION_ERROR_MESSAGE, Toast.LENGTH_SHORT).show();
                });
            }
        }).start();

    }


    public static String read () throws IOException {
        if (in != null) {
            return in.readLine();
        }
        else {
            return null;
        }
    }

    public static void write (String line) {
        if (out != null) {
            out.print(line);
            out.flush();
        }
    }


    public static void stopConnection () {
        try {
            if (in != null)
                in.close();
            if (out != null)
                out.close();
            if (clientSocket != null)
                clientSocket.close();
            in = null;
            out = null;
            clientSocket = null;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void setConnectionActivity(ConnectionActivity activity) {
        connectionActivity = activity;
    }
}
