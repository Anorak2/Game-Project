package com.Main.Games;

import com.Main.MenuController;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.io.FileInputStream;
import java.net.URL;
import java.util.ResourceBundle;

public class MinesweeperController extends MenuController implements Initializable {
    @FXML
    Rectangle mainBox;
    @FXML
    GridPane MainGridpane, CoverSquareGridpane;

    //Constants for the board
    private final int GridSize = 24;
    private final int pixelSize = 600;
    private final int numBombs = (int) ((GridSize * GridSize) / 4.85);
    Integer[][] MainArray = new Integer[GridSize][GridSize];

    public void initialize(URL url, ResourceBundle resourceBundle) {
        mainBox.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY)) {
                click(true, event.getSceneY(), event.getSceneX());
                System.out.println("left clicked");
            } else if (event.getButton().equals(MouseButton.SECONDARY)) {
                click(false, event.getSceneY(), event.getSceneX());
                System.out.println("right clicked");
            }
        });
        setBoard(0);
        //coverBoard();
    }

    public void click(Boolean isLeftClick, double row, double col) {
        int GridRow = (int) row / (pixelSize / GridSize);
        int GridCol = (int) col / (pixelSize / GridSize);

        if (isLeftClick) {
            //logic
            //Node temp = getNodeByRowColumnIndex(MainGridpane, GridRow, GridCol);
            showAllAround(GridRow, GridCol);
        } else {
            //Node temp = getNodeByRowColumnIndex(MainGridpane, GridRow, GridCol);
        }
    }

    public void showAllAround(int row, int col) {
        Node temp = getNodeByRowColumnIndex(MainGridpane, row, col);
        if (temp != null) {
            temp = getNodeByRowColumnIndex(CoverSquareGridpane, row, col);
            temp.setVisible(false);
            lose();
        } else {
            //System.out.println(yeet);
            showNearby(row, col);
        }
    }

    private void showNearby(int row, int col) {
        if(MainArray[row][col] == null){
            Node temp = getNodeByRowColumnIndex(CoverSquareGridpane, row, col);
            temp.setVisible(false);
            MainArray[row][col] = 0;
            for (int x = -1; x < 2; x++) {
                for (int y = -1; y < 2; y++) {
                    if ((row < GridSize && row >= 0) && (col >= 0 && col < GridSize) &&
                            (x != y) && !(x == -1 && y == 1) && !(x == 1 && y == -1)) {
                        if (MainArray[row + x][col + y] == null) {
                            if (MainArray[row + x][col + y] == null) {
                                MainArray[row + x][col + y] = 0;
                                Node square = getNodeByRowColumnIndex(CoverSquareGridpane, row + x, col + y);
                                square.setVisible(false);
                                showAllAround(row + x, col + y);
                            }
                        }
                    }
                }
            }
        }
    }

    private void lose() {

    }

    private int numBombsNearby(int row, int col){
        int count = 0;
        if(MainArray[row][col] == null) {
            for (int x = -1; x < 2; x++) {
                for (int y = -1; y < 2; y++) {
                    if (!(x == 0 && y == 0) && row + x < GridSize && row + x >= 0 && col + y < GridSize && col + y >= 0) {
                        if (MainArray[row + x][col + y] != null && MainArray[row + x][col + y] == 1) {
                            count++;
                        }
                    }
                }
            }
        }
        return count;
    }

    private Node getNodeByRowColumnIndex(GridPane pane, final int row, final int column) {
        Node result = null;
        ObservableList<Node> childrens = pane.getChildren();

        for (Node node : childrens) {
            if (GridPane.getRowIndex(node) != null && GridPane.getColumnIndex(node) != null) {
                if (GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == column) {
                    result = node;
                    break;
                }
            }
        }

        return result;
    }

    private void setBoard(int numCurrentBombs) {
        int num = numCurrentBombs;
        try {
            //Set Bombs
            for (int row = 0; row < GridSize; row++) {
                for (int col = 0; col < GridSize; col++) {
                    FileInputStream inputstream = new FileInputStream("src/main/resources/fxml/images/mine-icon.png");
                    Image image = new Image(inputstream);

                    ImageView tempImage = new ImageView();
                    tempImage.setImage(image);
                    tempImage.setFitHeight(pixelSize / GridSize);
                    tempImage.setFitWidth(pixelSize / GridSize);

                    if (num < numBombs && Math.random() < .206) {
                        MainGridpane.add(tempImage, row, col);
                        num++;
                        MainArray[row][col] = 1;
                    }
                    //GridPane.setConstraints((Node) image, 2, 0);
                }
            }
            if (num < numBombs) {
                setBoard(num);
            }
            for (int row = 0; row < GridSize; row++) {
                for (int col = 0; col < GridSize; col++) {
                    int nearbyBomb = numBombsNearby(row, col);
                    if(nearbyBomb != 0) {
                        Text tempText = new Text();
                        tempText.setVisible(true);
                        tempText.setStyle("-fx-font-size : 20px");
                        tempText.setText("" + nearbyBomb);
                        MainGridpane.add(tempText, row, col);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void coverBoard() {
        try {
            for (int row = 0; row < GridSize; row++) {
                for (int col = 0; col < GridSize; col++) {
                    FileInputStream inputstream = new FileInputStream("src/main/resources/fxml/images/Square.jpg");
                    Image image = new Image(inputstream);

                    ImageView rect = new ImageView();
                    rect.setImage(image);

                    rect.setFitHeight(pixelSize / GridSize);
                    rect.setFitWidth(pixelSize / GridSize);

                    CoverSquareGridpane.add(rect, row, col);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
