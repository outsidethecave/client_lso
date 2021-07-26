package com.lso;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ConnectionHandler {

    private static final String TAG = ConnectionHandler.class.getSimpleName();

    //private static final String SERVER_IP = "20.203.137.149";
    private static final String SERVER_IP = "192.168.1.75";
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
        out.print(line);
        out.flush();
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
