package client;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.CellState;

class CellView extends ImageView {
    private int x;
    private int y;

    CellView(int x, int y, double size, CellState state) {
        super();
        this.x = x;
        this.y = y;
        setState(state);

        setFitHeight(size);
        setFitWidth(size);
        setX(3.0);
        setY(3.0);
    }

    int X() {
        return x;
    }

    int Y() {
        return y;
    }

    void setState(CellState state) {
        switch (state) {
            case UNKNOWN:
            case EMPTY:
                setImage(new Image("clean.png")); break;
            case OCCUPIED: setImage(new Image("occupied.png")); break;
            case DESTROYED: setImage(new Image("destroyed.png")); break;
            case MISSED: setImage(new Image("missed.png")); break;
        }
    }
}
