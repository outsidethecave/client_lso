package com.lso;

import android.graphics.Color;

import java.util.Random;

public class Player {

    private final String nickname;
    private final String symbol;
    private int territories = 1;
    private int position;
    private final int color;


    public Player (String nickname, String symbol, int position) {
        this.nickname = nickname;
        this.symbol = symbol;
        this.position = position;

        Random rnd = new Random();
        this.color = Color.rgb(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
    }


    public String getNickname() {
        return nickname;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getPosition() {
        return position;
    }

    public int getColor() {
        return color;
    }


    public void addTerritory () {
        territories++;
    }

    public void removeTerritory () {
        territories--;
    }

    public int getTerritories () {
        return territories;
    }


    public void changePosition(int gridsize, char direction) {
        switch (direction) {
            case 'N':
                position -= 1;
                break;
            case 'S':
                position += 1;
                break;
            case 'O':
                position -= gridsize;
                break;
            case 'E':
                position += gridsize;
                break;
        }
    }

}
