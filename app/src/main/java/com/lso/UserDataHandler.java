package com.lso;

import android.util.Log;

import java.io.IOException;
import java.util.List;

public class UserDataHandler {

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

   public static boolean fetchActiveUsers (List<String> activeUsers) {

       if (!activeUsers.isEmpty()) {
           return false;
       }

       ConnectionHandler.write("3");
       String readVal;
       try {
           while (true) {
               readVal = ConnectionHandler.read();
               if ("|".equals(readVal) || readVal == null) break;
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
