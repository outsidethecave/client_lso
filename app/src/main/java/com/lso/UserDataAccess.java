package com.lso;

import com.lso.control.AuthController;

import java.io.IOException;
import java.util.List;

public class UserDataAccess {

    private static final String SEPARATOR = "|";


    public static int addUser (String nick, String pswd) {

        String outcome;
        int outcome_int;

        ConnectionHandler.write(AuthController.SIGNUP + nick + SEPARATOR + pswd);
        try {

            outcome = ConnectionHandler.read();
            if (outcome == null) throw new IOException();
            outcome_int = Integer.parseInt(outcome);

            if (outcome_int == AuthController.SIGNUP_SUCCESS) {
                return AuthController.SIGNUP_SUCCESS;
            }
            else if (outcome_int == AuthController.USER_ALREADY_EXISTS) {
                return AuthController.USER_ALREADY_EXISTS;
            }

        } catch (IOException e) {
            ConnectionHandler.stopConnection();
            return AuthController.GENERIC_SIGNUP_FAILURE;
        }

        return AuthController.GENERIC_SIGNUP_FAILURE;

    }

    public static int logIn (String nick, String pswd) {

        String outcome;
        int outcome_int;

        ConnectionHandler.write(AuthController.LOGIN + nick + SEPARATOR + pswd);
        try {

            outcome = ConnectionHandler.read();
            if (outcome == null) throw new IOException();
            outcome_int = Integer.parseInt(outcome);

            if (outcome_int == AuthController.LOGIN_SUCCESS) {
                return AuthController.LOGIN_SUCCESS;
            }
            else if (outcome_int == AuthController.USER_DOESNT_EXIST) {
                return AuthController.USER_DOESNT_EXIST;
            }
            else if (outcome_int == AuthController.WRONG_PASSWORD) {
                return AuthController.WRONG_PASSWORD;
            }

        } catch (IOException e) {
            ConnectionHandler.stopConnection();
            return AuthController.GENERIC_LOGIN_FAILURE;
        }

        return AuthController.GENERIC_LOGIN_FAILURE;

    }

    public static boolean fetchActiveUsers (List<String> activeUsers) {

       activeUsers.clear();

       ConnectionHandler.write("3");
       String readVal;

       try {

           while (true) {
               readVal = ConnectionHandler.read();

               if (readVal == null) throw new IOException();
               if ("|".equals(readVal)) break;

               activeUsers.add(readVal);
           }

       }
       catch (IOException e) {
           e.printStackTrace();
           return false;
       }
       return true;
   }

}
