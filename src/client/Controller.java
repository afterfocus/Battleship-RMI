package client;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import model.Answer;
import model.Cell;
import model.CellState;
import model.ServerShot;
import server.IRemote;

import java.io.*;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Controller {
    @FXML
    Pane leftPane;
    @FXML
    Pane rightPane;
    @FXML
    Label playerShips;
    @FXML
    Label enemyShips;
    @FXML
    TextArea log;
    @FXML
    Pane gameOverPane;
    @FXML
    Label gameOverLabel;
    @FXML
    Slider sizeSlider;
    @FXML
    Label sizeLabel;
    @FXML
    Label playerScoreLabel;
    @FXML
    Label enemyScoreLabel;

    private int N;
    private CellView[][] playerCells;
    private CellView[][] enemyCells;
    private int playerShipsCount;
    private int enemyShipsCount;
    private int playerScore;
    private int enemyScore;

    private IRemote remote;

    @FXML
    public void initialize() throws NotBoundException {
        try {
            System.setProperty("java.rmi.server.hostname", "127.0.0.1");
            remote = (IRemote) Naming.lookup("rmi://localhost:8080/IRemote");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sliderDragged() {
        sizeLabel.setText((int)sizeSlider.getValue() + "");
    }

    public void startBattle() throws RemoteException {
        gameOverPane.setVisible(false);
        N = (int)sizeSlider.getValue();
        initializePlayerFields(remote.initializeFields(N));

        playerShipsCount = remote.getShipsCount();
        enemyShipsCount = playerShipsCount;
        playerShips.setText(playerShipsCount + "");
        enemyShips.setText(enemyShipsCount + "");

        playerScore = 0;
        enemyScore = 0;
        playerScoreLabel.setText("0");
        enemyScoreLabel.setText("0");

        boolean isPlayerFirst = remote.isPlayerFirst();
        log.setText(isPlayerFirst ? "Вы ходите первым" : "Противник ходит первым");

        if (!isPlayerFirst) receiveServerShoot();
    }

    private void initializePlayerFields(CellState[][] playerCellStates) {
        playerCells = new CellView[N][N];
        enemyCells = new CellView[N][N];
        leftPane.getChildren().clear();
        rightPane.getChildren().clear();
        leftPane.getChildren().add(initializePlayerGrid(playerCellStates));
        rightPane.getChildren().add(initializeEnemyGrid());
    }

    private GridPane initializeGrid(double cellSize) {
        GridPane gridPane = new GridPane();
        for (int i = 1; i <= N; i++) {
            Label number = new Label(i + "");
            number.setMinSize(cellSize, cellSize);
            number.setFont(new Font(65.0 / Math.sqrt(N)));
            number.setAlignment(Pos.CENTER);
            gridPane.add(number, 0, i);
        }
        for (int j = 1; j <= N; j++) {
            Label letter = new Label((char)(1039 + j) + "");
            letter.setMinSize(cellSize, cellSize);
            letter.setFont(new Font(65.0 / Math.sqrt(N)));
            letter.setAlignment(Pos.CENTER);
            gridPane.add(letter, j, 0);
        }
        gridPane.setLayoutX(50);
        gridPane.setLayoutY(54);
        gridPane.setGridLinesVisible(true);
        return gridPane;
    }

    private GridPane initializePlayerGrid(CellState[][] playerCellStates) {
        double cellSize = 440.0 / (N + 1);
        GridPane gridPane = initializeGrid(cellSize);

        for (int x = 0; x < N; x++) {
            for (int y = 0; y < N; y++) {
                playerCells[x][y] = new CellView(x, y, cellSize - 1, playerCellStates[x][y]);
                gridPane.add(playerCells[x][y], x + 1, y + 1);
            }
        }
        return gridPane;
    }

    private GridPane initializeEnemyGrid() {
        double cellSize = 440.0 / (N + 1);
        GridPane gridPane = initializeGrid(cellSize);

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                CellView enemyCell = new CellView(i, j, cellSize - 1, CellState.UNKNOWN);

                enemyCell.setOnMouseClicked(event -> {
                    try {
                        int x = enemyCell.X();
                        int y = enemyCell.Y();
                        Answer answer = remote.sendShot(x, y);
                        log.appendText("\nВы: " + (char) (1040 + x) + (y + 1) + " - ");

                        switch (answer) {
                            case MISSED:
                                enemyCell.setState(CellState.MISSED);
                                log.appendText("Промах");
                                playerScore++;
                                playerScoreLabel.setText(playerScore + "");
                                break;
                            case DAMAGED:
                                enemyCell.setState(CellState.DESTROYED);
                                playerScore++;
                                playerScoreLabel.setText(playerScore + "");
                                log.appendText("Попадание");
                                break;
                            case DESTROYED: {
                                enemyCell.setState(CellState.DESTROYED);
                                log.appendText("Корабль уничтожен");
                                enemyShipsCount--;
                                enemyShips.setText(enemyShipsCount + "");

                                Cell[] emptyCells = remote.destroyShip(x, y, false);

                                for (Cell cells : emptyCells) {
                                    enemyCells[cells.getX()][cells.getY()].setState(CellState.MISSED);
                                    playerScore++;
                                    playerScoreLabel.setText(playerScore + "");
                                }

                                if (enemyShipsCount == 0) {
                                    gameOverLabel.setText("Вы победили");
                                    gameOverPane.setVisible(true);
                                }
                                break;
                            }
                        }
                        enemyCell.setOnMouseClicked(null);

                        if (answer == Answer.MISSED) {
                            receiveServerShoot();
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                });
                enemyCells[i][j] = enemyCell;
                gridPane.add(enemyCell, i + 1, j + 1);
            }
        }
        return gridPane;
    }


    private void receiveServerShoot() throws RemoteException {
        Answer answer;
        do {
            ServerShot shot = remote.receiveShot();
            int x = shot.getX();
            int y = shot.getY();
            answer = shot.getAnswer();

            CellView playerCell = playerCells[x][y];
            log.appendText("\nПротивник: " + (char)(1040 + x) + (y + 1) + " - ");

            switch (answer) {
                case MISSED:
                    playerCell.setState(CellState.MISSED);
                    log.appendText("Промах");
                    enemyScore++;
                    enemyScoreLabel.setText(enemyScore + "");
                    break;
                case DAMAGED:
                    playerCell.setState(CellState.DESTROYED);
                    log.appendText("Попадание");
                    enemyScore++;
                    enemyScoreLabel.setText(enemyScore + "");
                    break;
                case DESTROYED: {
                    playerCell.setState(CellState.DESTROYED);
                    log.appendText("Корабль уничтожен");

                    playerShipsCount--;
                    playerShips.setText(playerShipsCount + "");

                    Cell[] emptyCells = remote.destroyShip(x, y, true);

                    for (Cell cells: emptyCells) {
                        playerCells[cells.getX()][cells.getY()].setState(CellState.MISSED);
                        enemyScore++;
                        enemyScoreLabel.setText(enemyScore + "");
                    }

                    if (playerShipsCount == 0) {
                        gameOverLabel.setText("Вы проиграли");
                        gameOverPane.setVisible(true);
                    }
                    break;
                }
            }
        }
        while (answer != Answer.MISSED && playerShipsCount > 0);
    }
}
