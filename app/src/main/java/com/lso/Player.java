package com.lso;

public class Player {

    public String nickname;
    public String symbol;
    public int territories = 1;
    public int x;
    public int y;
    public boolean isActive;

    public Player (String nickname, String symbol, int x, int y) {
        this.nickname = nickname;
        this.symbol = symbol;
        this.x = x;
        this.y = y;
    }

}
