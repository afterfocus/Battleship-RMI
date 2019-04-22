package model;

class Ship {
    private boolean isHorizontal;
    private Cell[] cells;
    private int healthPoints;

    Ship(boolean isHorizontal, int length) {
        this.isHorizontal = isHorizontal;
        this.cells = new Cell[length];
        this.healthPoints = length;
    }

    Cell[] getCells() {
        return cells;
    }

    int getLength() {
        return cells.length;
    }

    boolean isHorizontal() {
        return isHorizontal;
    }

    void setCell(int i, Cell cell) {
        cells[i] = cell;
    }

    boolean dealDamage() {
        healthPoints--;
        return healthPoints == 0;
    }
}
