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
    boolean[][] MarkedSquares = new boolean[GridSize][GridSize];

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
        coverBoard();

        for(int x = 0; x < GridSize; x++){
            for(int y = 0; y < GridSize; y++){
                MarkedSquares[x][y] = false;
            }
        }
    }

    public void click(Boolean isLeftClick, double row, double col) {
        int GridRow = (int) row / (pixelSize / GridSize);
        int GridCol = (int) col / (pixelSize / GridSize);

        if (isLeftClick) {
            showAllAround(GridRow, GridCol);
        } else {
            try {
                Node temp = getNodeByRowColumnIndex(CoverSquareGridpane, GridRow, GridCol);
                FileInputStream inputstream = new FileInputStream("src/main/resources/fxml/images/Square.jpg");

                if(!MarkedSquares[GridRow][GridCol]){
                    if(MainArray[GridRow][GridCol] == null || (MainArray[GridRow][GridCol] != null && MainArray[GridRow][GridCol] != 0)) {
                        inputstream = new FileInputStream("src/main/resources/fxml/images/flag.png");
                        MarkedSquares[GridRow][GridCol] = true;
                    }
                }
                else if (MarkedSquares[GridRow][GridCol]){
                    MarkedSquares[GridRow][GridCol] = false;
                }

                Image image = new Image(inputstream);
                ((ImageView) temp).setImage(image);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    public void showAllAround(int row, int col) {
        Node temp;
        if (MainArray[row][col] != null && MainArray[row][col] == 1) {
            temp = getNodeByRowColumnIndex(CoverSquareGridpane, row, col);
            temp.setVisible(false);
            lose();
        } else {
            showNearby(row, col);
        }
    }
    private void showNearby(int row, int col) {
        if (MainArray[row][col] == null) {
            Node temp = getNodeByRowColumnIndex(CoverSquareGridpane, row, col);
            temp.setVisible(false);
            MainArray[row][col] = 0;

            for (int x = -1; x < 2; x++) {
                for (int y = -1; y < 2; y++) {
                    if ((row + x < GridSize && row + x >= 0) && (col + y >= 0 && col + y < GridSize) &&
                            (x != y) && !(x == -1 && y == 1) && !(x == 1 && y == -1)) {
                        if (MainArray[row + x][col + y] == null) {
                            showAllAround(row + x, col + y);
                        }

                    }
                    if((row + x < GridSize && row + x >= 0) && (col + y >= 0 && col + y < GridSize)){
                        if (MainArray[row+x][col+y] !=  null && MainArray[row + x][col + y] == 2) {
                            Node num = getNodeByRowColumnIndex(CoverSquareGridpane, row+x, col+y);
                            MainArray[row+x][col+y] = 0;

                            num.setVisible(false);
                        }
                    }
                }
            }
        }
        else{
            Node temp = getNodeByRowColumnIndex(CoverSquareGridpane, row, col);
            temp.setVisible(false);
            MainArray[row][col] = 0;
        }
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

    private void lose() {

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

                    //Normally 206
                    if (num < numBombs && Math.random() < .1) {
                        if(MainArray[row][col] == null) {
                            MainGridpane.add(tempImage, row, col);

                            //I hate this so much but this is the only way to make it work
                            MainArray[col][row] = 1;
                            num++;
                        }
                    }
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
                        MainArray[row][col] = 2;
                        MainGridpane.add(tempText, col, row);
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
                    rect.setOpacity(.7);

                    CoverSquareGridpane.add(rect, row, col);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
