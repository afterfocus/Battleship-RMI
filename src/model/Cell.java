package model;

import java.io.Serializable;

public class Cell implements Comparable, Serializable {
    private int y;
    private int x;
    private boolean isChecked;
    private CellState state;
    private Ship ship;

    Cell(int x, int y, CellState state) {
        this.x = x;
        this.y = y;
        this.state = state;
    }

    CellState getState() {
        return state;
    }

    public void setState(CellState state) {
        this.state = state;
    }

    Ship getShip() {
        return ship;
    }

    void setShip(Ship ship) {
        this.ship = ship;
        this.state = CellState.OCCUPIED;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isNotChecked() {
        return !isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    @Override
    public int compareTo(Object o) {
        Cell cell = (Cell)o;
        if (x == cell.getX()) {
            return y - cell.getY();
        } else {
            return x - cell.getX();
        }
    }
}
