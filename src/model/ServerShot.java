package model;

import java.io.Serializable;

public class ServerShot implements Serializable {
    private int x;
    private int y;
    private Answer answer;

    public ServerShot(int x, int y, Answer answer) {
        this.x = x;
        this.y = y;
        this.answer = answer;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Answer getAnswer() {
        return answer;
    }
}
