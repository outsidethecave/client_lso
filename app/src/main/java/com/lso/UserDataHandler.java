package com.lso;

import java.io.IOException;
import java.util.List;

public class UserDataHandler {

   public static boolean fetchActiveUsers (List<String> activeUsers) {

       if (!activeUsers.isEmpty()) {
           return false;
       }

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
