package server;

import model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

class Game {
    private int N;
    private Field playerField;
    private Field enemyField;
    private Random R = new Random();
    private ArrayList<Cell> shipCellsFound = new ArrayList<>();

    CellState[][] initializeFields(int N) {
        this.N = N;
        playerField = new Field(N);
        enemyField = new Field(N);

        CellState[][] playerCells = new CellState[N][N];
        for (int x = 0; x < N; x++)
            for (int y = 0; y < N; y++)
                playerCells[x][y] = playerField.getState(x, y);
        return playerCells;
    }

    int getShipsCount() {
        return playerField.getShipsCount();
    }

    boolean isPlayerFirst() {
        return R.nextBoolean();
    }

    Answer sendShot(int x, int y) {
        return enemyField.makeShoot(x, y);
    }

    ServerShot receiveShot() {
        Answer answer;
        int x, y;
        if (shipCellsFound.size() == 0) {
            do {
                x = R.nextInt(N);
                y = R.nextInt(N);
                answer = playerField.makeShoot(x, y);
            } while (answer == Answer.INVALID);
        } else {
            Cell first = shipCellsFound.get(0);
            Cell last = shipCellsFound.get(shipCellsFound.size() - 1);

            do {
                if (shipCellsFound.size() > 1) {
                    if (isShipHorizontal()) {
                        if (first.getX() - 1 >= 0 && playerField.getCell(first.getX() - 1, first.getY()).isNotChecked()) {
                            x = first.getX() - 1;
                            y = first.getY();
                        } else {
                            x = last.getX() + 1;
                            y = last.getY();
                        }
                    } else {
                        if (first.getY() - 1 >= 0 && playerField.getCell(first.getX(), first.getY() - 1).isNotChecked()) {
                            x = first.getX();
                            y = first.getY() - 1;
                        } else {
                            x = last.getX();
                            y = last.getY() + 1;
                        }
                    }
                } else {
                    switch (R.nextInt(4)) {
                        case 0: {
                            x = first.getX() - 1;
                            y = first.getY();
                            break;
                        }
                        case 1: {
                            x = first.getX() + 1;
                            y = first.getY();
                            break;
                        }
                        case 2: {
                            x = first.getX();
                            y = first.getY() - 1;
                            break;
                        }
                        default: {
                            x = first.getX();
                            y = first.getY() + 1;
                            break;
                        }
                    }
                }
                answer = playerField.makeShoot(x, y);
            } while (answer == Answer.INVALID);
        }
        playerField.getCell(x, y).setChecked(true);

        if (answer == Answer.DESTROYED) {
            shipCellsFound.clear();
        } else if (answer == Answer.DAMAGED) {
            shipCellsFound.add(playerField.getCell(x, y));
            Collections.sort(shipCellsFound);
        }
        return new ServerShot(x, y, answer);
    }

    private boolean isShipHorizontal() {
        if (shipCellsFound.size() > 1) {
            return shipCellsFound.get(0).getY() == shipCellsFound.get(1).getY();
        } else return false;
    }

    Cell[] destroyShip(int x, int y, boolean isPlayerShip) {
        Cell[] emptyCells = isPlayerShip ? playerField.destroyShip(x, y) : enemyField.destroyShip(x, y);
        for (Cell emptyCell : emptyCells) {
            emptyCell.setState(CellState.MISSED);
            emptyCell.setChecked(true);
        }
        return emptyCells;
    }
}
