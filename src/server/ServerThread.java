package server;

import model.Answer;
import model.Cell;
import model.CellState;
import model.Field;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class ServerThread extends Thread {
    private int N;
    private Field playerField;
    private Field enemyField;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Random R = new Random();
    private int playerShipsCount;
    private int enemyShipsCount;

    private ArrayList<Cell> shipCellsFound = new ArrayList<>();

    ServerThread(Socket socket) throws IOException {
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        start();
    }

    @Override
    public void run() {
        try {
            for(;;) {
                System.out.print("Чтение N... ");
                N = in.readInt();
                System.out.println("Получено " + N);

                playerField = new Field(N);
                enemyField = new Field(N);

                for (int x = 0; x < N; x++)
                    for (int y = 0; y < N; y++) {
                        System.out.print("Отправка State... ");
                        out.writeObject(playerField.getState(x, y));
                        System.out.println("Отправлено " + playerField.getState(x, y));
                    }
                System.out.print("Отправка shipsCount... ");
                out.writeInt(playerField.getShipsCount());
                out.flush();
                System.out.println("Отправлено " + playerField.getShipsCount());

                playerShipsCount = playerField.getShipsCount();
                enemyShipsCount = playerShipsCount;

                //printField(playerField);

                System.out.print("Определение первого хода... ");
                boolean isPlayerFirst = R.nextBoolean();
                out.writeBoolean(isPlayerFirst);
                out.flush();
                System.out.println(isPlayerFirst ? "Игрок ходит первым" : "Противник ходит первым");

                if (!isPlayerFirst) {
                    Answer answer;
                    do answer = sendShot();
                    while (playerShipsCount > 0 && answer != Answer.MISSED);
                }

                while (playerShipsCount > 0 && enemyShipsCount > 0) {
                    Answer answer = recieveShot();

                    if (answer == Answer.MISSED) {
                        do answer = sendShot();
                        while (playerShipsCount > 0 && answer != Answer.MISSED);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Answer recieveShot() throws IOException {
        System.out.print("Чтение x... ");
        int x = in.readInt();
        System.out.println("Получено " + x);

        System.out.print("Чтение y... ");
        int y = in.readInt();
        System.out.println("Получено " + y);

        Answer answer = enemyField.makeShoot(x, y);
        System.out.print("Отправка Answer... ");
        out.writeObject(answer);
        System.out.println("Отправлено " + answer);

        if (answer == Answer.DESTROYED) {
            destroyShip(x, y, enemyField);
            enemyShipsCount--;
        }
        out.flush();
        return answer;
    }

    private Answer sendShot() throws IOException {
        int x, y;
        Answer answer;
        if (shipCellsFound.size() == 0) {
            do {
                x = R.nextInt(N);
                y = R.nextInt(N);
                answer = playerField.makeShoot(x, y);
                System.out.println("Выстрел: " + x + "/" + y + " - " + answer);
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
                System.out.println("Выстрел: " + y + "/" + x + " - " + answer);
            } while (answer == Answer.INVALID);
        }
        playerField.getCell(x, y).setChecked(true);

        System.out.print("Отправка x... ");
        out.writeInt(x);
        System.out.println("Отправлено " + x);

        System.out.print("Отправка y... ");
        out.writeInt(y);
        System.out.println("Отправлено " + y);

        System.out.print("Отправка answer... ");
        out.writeObject(answer);
        System.out.println("Отправлено " + answer);

        if (answer == Answer.DESTROYED) {
            destroyShip(x, y, playerField);
            playerShipsCount--;
            shipCellsFound.clear();
        } else if (answer == Answer.DAMAGED) {
            shipCellsFound.add(playerField.getCell(x, y));
            Collections.sort(shipCellsFound);
        }
        out.flush();
        //printField(playerField);
        return answer;
    }

    private boolean isShipHorizontal() {
        if (shipCellsFound.size() > 1) {
            return shipCellsFound.get(0).getY() == shipCellsFound.get(1).getY();
        } else return false;
    }


    /*
    private void printField(Field field) {
        for (int x = 0; x < N; x++) {
            for (int y = 0; y < N; y++) {
                switch (field.getCell(y, x).getState()) {
                    case MISSED: System.out.print(".(" + (field.getCell(y, x).isChecked() ? "+)  " : "-)  ")); break;
                    case DESTROYED: System.out.print("X(" + (field.getCell(y, x).isChecked() ? "+)  " : "-)  ")); break;
                    case OCCUPIED: System.out.print("O(" + (field.getCell(y, x).isChecked() ? "+)  " : "-)  ")); break;
                    case EMPTY: System.out.print(" (" + (field.getCell(y, x).isChecked() ? "+)  " : "-)  ")); break;
                }
            }
            System.out.println();
        }
    }*/

    private void destroyShip(int x, int y, Field field) throws IOException {
        Cell[] emptyCells = field.destroyShip(x, y);

        System.out.print("Отправка emptyCellsCount... ");
        out.writeInt(emptyCells.length);
        System.out.println("Отправлено " + emptyCells.length);

        for (Cell emptyCell : emptyCells) {
            System.out.print("Отправка emptyCellX... ");
            out.writeInt(emptyCell.getX());
            System.out.println("Отправлено " + emptyCell.getX());

            System.out.print("Отправка emptyCellY... ");
            out.writeInt(emptyCell.getY());
            System.out.println("Отправлено " + emptyCell.getY());

            emptyCell.setState(CellState.MISSED);
            field.getCell(emptyCell.getX(), emptyCell.getY()).setChecked(true);
        }
    }
}
