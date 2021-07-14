package com.lso;

import com.google.android.material.textfield.TextInputLayout;

public class InputErrorHandler {

    public static boolean isNickname (String nick, TextInputLayout til) {

        char[] nick_array = nick.toCharArray();

        if (nick_array.length < 3) {
            til.setError("Il nickname deve essere lungo almeno 3 caratteri");
            return false;
        }
        if (nick_array.length > 20) {
            til.setError("Il nickname non può essere più lungo di 20 caratteri");
            return false;
        }

        int num_specials = 0;

        for (char c : nick_array) {
            if (!Character.isAlphabetic(c) && !Character.isDigit(c)) {
                if (c != '_' && c != '.') {
                    til.setError("Il nickname può contenere solo lettere, numeri, . o _");
                    return false;
                } else {
                    if (++num_specials > 2) {
                        til.setError("Il nickname può contenere al massimo due caratteri speciali");
                        return false;
                    }
                }
            }
        }

        return true;

    }

    public static boolean isPassword (String pswd, TextInputLayout til) {
        if (pswd.isEmpty()) {
            til.setError("Inserisci la password");
            return false;
        }
        if (pswd.length() < 6) {
            til.setError("La password deve essere lunga almeno 6 caratteri");
            return false;
        }
        if (pswd.length() > 20) {
            til.setError("La password non può essere più lunga di 20 caratteri");
        }
        if (pswd.startsWith(" ") || pswd.endsWith(" ")) {
            til.setError("La password non può iniziare o finire con uno spazio");
            return false;
        }
        if (pswd.contains("|")) {
            til.setError("La password non può contenere il carattere speciale |");

        }
        return true;
    }

    public static boolean passwordsMatch (String pswd1, String pswd2, TextInputLayout til) {
        if (!pswd1.equals(pswd2)) {
            til.setError("Le password non corrispondono");
            return false;
        }
        return true;
    }

}
