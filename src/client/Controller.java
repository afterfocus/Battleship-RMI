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
import model.CellState;

import java.io.*;
import java.net.Socket;

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

    private static ObjectInputStream in;
    private static ObjectOutputStream out;

    @FXML
    public void initialize() {
        try {
            Socket socket = new Socket("localhost", 8080);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sliderDragged() {
        sizeLabel.setText((int)sizeSlider.getValue() + "");
    }

    public void startBattle() throws IOException, ClassNotFoundException {
        gameOverPane.setVisible(false);
        N = (int)sizeSlider.getValue();

        System.out.print("Отправка N... ");
        out.writeInt(N);
        out.flush();
        System.out.println("Отправлено " + N);

        initializePlayerFields();

        System.out.print("Определение первого хода... ");
        boolean isPlayerFirst = in.readBoolean();
        System.out.println(isPlayerFirst ? "Игрок ходит первым" : "Противник ходит первым");
        log.setText(isPlayerFirst ? "Вы ходите первым" : "Противник ходит первым");

        if (!isPlayerFirst) receiveBotShoot(Answer.MISSED);
    }

    private void initializePlayerFields() throws IOException, ClassNotFoundException {
        playerCells = new CellView[N][N];
        enemyCells = new CellView[N][N];
        leftPane.getChildren().clear();
        rightPane.getChildren().clear();
        leftPane.getChildren().add(initializePlayerGrid());
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

    private GridPane initializePlayerGrid() throws IOException, ClassNotFoundException {
        double cellSize = 440.0 / (N + 1);
        GridPane gridPane = initializeGrid(cellSize);

        for (int x = 0; x < N; x++)
            for (int y = 0; y < N; y++) {
                System.out.print("Чтение CellState... ");
                playerCells[x][y] = new CellView(x, y, cellSize - 1, (CellState) in.readObject());
                gridPane.add(playerCells[x][y], x + 1, y + 1);
                System.out.println("Получено");
            }
        System.out.print("Чтение playerShipsCount... ");
        playerShipsCount = in.readInt();
        System.out.println("Получено " + playerShipsCount);

        enemyShipsCount = playerShipsCount;
        playerShips.setText(playerShipsCount + "");
        enemyShips.setText(enemyShipsCount + "");

        playerScore = 0;
        enemyScore = 0;
        playerScoreLabel.setText("0");
        enemyScoreLabel.setText("0");
        return gridPane;
    }

    private GridPane initializeEnemyGrid() {
        double cellSize = 440.0 / (N + 1);
        GridPane gridPane = initializeGrid(cellSize);

        for (int x = 0; x < N; x++) {
            for (int y = 0; y < N; y++) {
                CellView enemyCell = new CellView(x, y, cellSize - 1, CellState.UNKNOWN);

                enemyCell.setOnMouseClicked(event -> {
                        try {
                            System.out.print("Отправка X... ");
                            out.writeInt(enemyCell.X());
                            System.out.println("Отправлено " + enemyCell.X());
                            System.out.print("Отправка Y... ");
                            out.writeInt(enemyCell.Y());
                            System.out.println("Отправлено " + enemyCell.Y());
                            out.flush();

                            log.appendText("\nВы: " + (char)(1040 + enemyCell.X()) + (enemyCell.Y() + 1) + " - ");
                            System.out.print("Чтение Answer... ");
                            Answer answer = (Answer) in.readObject();
                            System.out.println("Получено " + answer);

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

                                    System.out.print("Чтение n... ");
                                    int n = in.readInt();
                                    System.out.println("Получено " + n);
                                    for (int i = 0; i < n; i++) {
                                        System.out.print("Чтение r... ");
                                        int r = in.readInt();
                                        System.out.println("Получено " + r);

                                        System.out.print("Чтение c... ");
                                        int c = in.readInt();
                                        System.out.println("Получено " + c);

                                        enemyCells[r][c].setState(CellState.MISSED);
                                        enemyCells[r][c].setOnMouseClicked(null);

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
                            receiveBotShoot(answer);

                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                });
                enemyCells[x][y] = enemyCell;
                gridPane.add(enemyCell, x + 1, y + 1);
            }
        }
        return gridPane;
    }


    private void receiveBotShoot(Answer answer) throws IOException, ClassNotFoundException {
        if (answer == Answer.MISSED) {
            do {
                System.out.print("Чтение x... ");
                int x = in.readInt();
                System.out.println("Получено " + x);

                System.out.print("Чтение y... ");
                int y = in.readInt();
                System.out.println("Получено " + y);

                CellView playerCell = playerCells[x][y];
                log.appendText("\nПротивник: " + (char)(1040 + x) + (y + 1) + " - ");

                System.out.print("Чтение Answer... ");
                answer = (Answer) in.readObject();
                System.out.println("Получено " + answer);

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

                        System.out.print("Чтение n... ");
                        int n = in.readInt();
                        System.out.println("Получено " + n);

                        for (int i = 0; i < n; i++) {
                            System.out.print("Чтение x... ");
                            x = in.readInt();
                            System.out.println("Получено " + x);

                            System.out.print("Чтение y... ");
                            y = in.readInt();
                            System.out.println("Получено " + y);
                            playerCells[x][y].setState(CellState.MISSED);

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
}
