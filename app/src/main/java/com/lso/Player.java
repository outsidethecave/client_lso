package com.lso;

import android.graphics.Color;

import java.util.Random;

public class Player {

    private String nickname;
    private String symbol;
    private int territories = 1;
    private int position;
    private int color;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void addTerritory () {
        territories++;
    }

    public void removeTerritory () {
        territories--;
    }

    public int getPosition() {
        return position;
    }

    public void move (char direction) {
        switch (direction) {
            case 'N':
                position -= 1;
                break;
            case 'S':
                position += 1;
                break;
            case 'O':
                position -= 10;
                break;
            case 'E':
                position += 10;
                break;
        }
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public Player (String nickname, String symbol, int position) {
        this.nickname = nickname;
        this.symbol = symbol;
        this.position = position;

        Random rnd = new Random();
        this.color = Color.rgb(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
    }

}
