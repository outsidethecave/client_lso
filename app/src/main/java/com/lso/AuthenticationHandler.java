package com.lso;

import java.io.IOException;

public class AuthenticationHandler {

    public static String currUser;

    public static int addUser (String nick, String pswd) {
        ConnectionHandler.write("1"+ nick + "|" + pswd);
        try {
            String readVal = ConnectionHandler.read();
            if ("1".equals(readVal)) {
                return 0;
            }
            else if ("0".equals(readVal)) {
                return 1;
            }
            else
                return 2;
        } catch (IOException e) {
            e.printStackTrace();
            return 2;
        }
    }

    public static int logIn (String nick, String pswd) {
        ConnectionHandler.write("2"+ nick + "|" + pswd);
        try {
            String readVal = ConnectionHandler.read();
            if ("0".equals(readVal)) {
                return 0;
            }
            else if ("1".equals(readVal)) {
                return 1;
            }
            else if ("2".equals(readVal)) {
                return 2;
            }
            else {
                return 3;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return 3;
        }
    }

    public static void setCurrUser (String nick) {
        currUser = nick;
    }

    public static String getCurrUser() {
        return currUser;
    }
}
