package model;

import java.util.ArrayList;
import java.util.Random;

public class Field {
    private int N;
    private Cell[][] cells;
    private Random R = new Random();

    public Field(int N) {
        this.N = N;
        cells = new Cell[N][N];

        for (int i = 0; i < N; i++)
            for (int j = 0; j < N; j++)
                 cells[i][j] = new Cell(i, j, CellState.UNKNOWN);

        findPlaceForShip(N * N / 201, 6);
        findPlaceForShip(N * N / 134, 5);
        findPlaceForShip(N * N / 75, 4);
        findPlaceForShip(N * N / 36, 3);
        findPlaceForShip((int)((double) N / 3.01), 2);
        findPlaceForShip((int)((double) N / 2.5), 1);

        for (int i = 0; i < N; i++)
            for (int j = 0; j < N; j++)
                if (cells[i][j].getState() == CellState.UNKNOWN) cells[i][j].setState(CellState.EMPTY);
    }

    public Cell getCell(int row, int column) {
        return cells[row][column];
    }

    public int getShipsCount() {
        return N * N / 201 + N * N / 134 + N * N / 75 + N * N / 36 + (int)((double) N / 3.01) + (int)((double) N / 2.5);
    }

    public CellState getState(int row, int column) {
        return cells[row][column].getState();
    }

    public Answer makeShoot(int row, int column) {
        if (row < 0 || column < 0 || row >= N || column >= N) return Answer.INVALID;
        else {
            Cell cell = cells[row][column];
            switch (cell.getState()) {
                case EMPTY: {
                    cell.setState(CellState.MISSED);
                    return Answer.MISSED;
                }
                case OCCUPIED: {
                    cell.setState(CellState.DESTROYED);
                    if (cell.getShip().dealDamage()) {
                        return Answer.DESTROYED;
                    } else {
                        return Answer.DAMAGED;
                    }
                }
                case MISSED:
                case DESTROYED:
                    return Answer.INVALID;
                default:
                    return null;
            }
        }
    }

    private void findPlaceForShip(int count, int length) {
        for (int i = 0; i < count; i++) {
            boolean isHorizontal = R.nextBoolean();
            int row = R.nextInt(N);
            int column = R.nextInt(N);

            if (checkPlaceAvailable(row, column, isHorizontal, length))
                placeShip(row, column, new Ship(isHorizontal, length));
            else i--;
        }
    }

    private boolean checkPlaceAvailable(int row, int column, boolean isHorizontal, int length) {
        if (isHorizontal) {
            for (int l = 0; l < length; l++)
                if (column + l > N - 1 || cells[row][column + l].getState() == CellState.OCCUPIED || cells[row][column + l].getState() == CellState.EMPTY) return false;
            return true;
        } else {
            for (int l = 0; l < length; l++)
                if (row + l > N - 1 || cells[row + l][column].getState() == CellState.OCCUPIED  || cells[row + l][column].getState() == CellState.EMPTY) return false;
            return true;
        }
    }

    private void placeShip(int row, int column, Ship ship) {
        int length = ship.getLength();
        if (ship.isHorizontal()) {
            if (column - 1 >= 0) {
                cells[row][column - 1].setState(CellState.EMPTY);
                if (row - 1 >= 0) cells[row - 1][column - 1].setState(CellState.EMPTY);
                if (row + 1 < N) cells[row + 1][column - 1].setState(CellState.EMPTY);
            }
            if (column + length < N) {
                cells[row][column + length].setState(CellState.EMPTY);
                if (row - 1 >= 0) cells[row - 1][column + length].setState(CellState.EMPTY);
                if (row + 1 < N) cells[row + 1][column + length].setState(CellState.EMPTY);
            }

            for (int l = 0; l < length; l++) {
                cells[row][column + l].setShip(ship);
                ship.setCell(l, cells[row][column + l]);
                if (row - 1 >= 0) cells[row - 1][column + l].setState(CellState.EMPTY);
                if (row + 1 < N) cells[row + 1][column + l].setState(CellState.EMPTY);
            }
        } else {
            if (row - 1 >= 0) {
                cells[row - 1][column].setState(CellState.EMPTY);
                if (column - 1 >= 0) cells[row - 1][column - 1].setState(CellState.EMPTY);
                if (column + 1 < N) cells[row - 1][column + 1].setState(CellState.EMPTY);
            }
            if (row + length < N) {
                cells[row + length][column].setState(CellState.EMPTY);
                if (column - 1 >= 0) cells[row + length][column - 1].setState(CellState.EMPTY);
                if (column + 1 < N) cells[row + length][column + 1].setState(CellState.EMPTY);
            }

            for (int l = 0; l < length; l++) {
                cells[row + l][column].setShip(ship);
                ship.setCell(l, cells[row + l][column]);
                if (column - 1 >= 0) cells[row + l][column - 1].setState(CellState.EMPTY);
                if (column + 1 < N) cells[row + l][column + 1].setState(CellState.EMPTY);
            }
        }
    }

    public Cell[] destroyShip(int r, int c) {
        Ship ship = cells[r][c].getShip();
        int row = ship.getCells()[0].getX();
        int column = ship.getCells()[0].getY();
        int length = ship.getLength();
        ArrayList<Cell> emptyCells = new ArrayList<>();

        if (ship.isHorizontal()) {
            if (column - 1 >= 0) {
                emptyCells.add(cells[row][column - 1]);
                if (row - 1 >= 0) emptyCells.add(cells[row - 1][column - 1]);
                if (row + 1 < N) emptyCells.add(cells[row + 1][column - 1]);
            }
            if (column + length < N) {
                emptyCells.add(cells[row][column + length]);
                if (row - 1 >= 0) emptyCells.add(cells[row - 1][column + length]);
                if (row + 1 < N) emptyCells.add(cells[row + 1][column + length]);
            }
            for (int l = 0; l < length; l++) {
                if (row - 1 >= 0) emptyCells.add(cells[row - 1][column + l]);
                if (row + 1 < N) emptyCells.add(cells[row + 1][column + l]);
            }
        } else {
            if (row - 1 >= 0) {
                emptyCells.add(cells[row - 1][column]);
                if (column - 1 >= 0) emptyCells.add(cells[row - 1][column - 1]);
                if (column + 1 < N) emptyCells.add(cells[row - 1][column + 1]);
            }
            if (row + length < N) {
                emptyCells.add(cells[row + length][column]);
                if (column - 1 >= 0) emptyCells.add(cells[row + length][column - 1]);
                if (column + 1 < N) emptyCells.add(cells[row + length][column + 1]);
            }
            for (int l = 0; l < length; l++) {
                if (column - 1 >= 0) emptyCells.add(cells[row + l][column - 1]);
                if (column + 1 < N) emptyCells.add(cells[row + l][column + 1]);
            }
        }
        return emptyCells.toArray(new Cell[0]);
    }
}
