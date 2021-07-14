package com.lso;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ConnectionHandler {

    private static final String TAG = ConnectionHandler.class.getSimpleName();
    private static final String SERVER_IP = "192.168.1.77";
    private static final int SERVER_PORT = 50000;

    private static Socket clientSocket;
    private static PrintWriter out;
    private static BufferedReader in;


    public static boolean startConnection () {

        clientSocket = new Socket();
        try {
            clientSocket.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT), 1000);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            stopConnection();
            return false;
        }

    }

    public static String read () throws IOException {
        return in.readLine();
    }

    public static void write (String line) {
        out.println(line);
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

}
