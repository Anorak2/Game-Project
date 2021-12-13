package com.Main.Games;

import com.Main.MenuController;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.ResourceBundle;

public class MinesweeperController extends MenuController {
    @FXML
    Rectangle mainBox;
    @FXML
    GridPane MainGridpane, CoverSquareGridpane;
    @FXML
    AnchorPane popUp;
    @FXML
    Text finalText;

    private boolean keyHeld = false;

    //Constants for the board
    private final int GridSize = 24;
    private final int pixelSize = 600;
    private final int numBombs = (int) ((GridSize * GridSize) / 4.85);
    private Integer[][] MainArray;
    private boolean[][] MarkedSquares;
    private boolean locked;


    public void initialize() {
        MainGridpane.getChildren().clear();
        CoverSquareGridpane.getChildren().clear();
        popUp.setVisible(false);
        locked = false;
        MainArray = new Integer[GridSize][GridSize];
        MarkedSquares = new boolean[GridSize][GridSize];
        MainGridpane.setGridLinesVisible(true);

        mainBox.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY)) {
                if(keyHeld)
                    click(false, event.getSceneY(), event.getSceneX());
                else
                    click(true, event.getSceneY(), event.getSceneX());
            } else if (event.getButton().equals(MouseButton.SECONDARY)) {
                click(false, event.getSceneY(), event.getSceneX());
            }
        });
        setBoard(0);
        addNumbers();
        coverBoard();

        for(int x = 0; x < GridSize; x++){
            for(int y = 0; y < GridSize; y++){
                MarkedSquares[x][y] = false;
            }
        }
    }

    public void click(Boolean isLeftClick, double row, double col) {
        if(!locked) {
            int GridRow = (int) row / (pixelSize / GridSize);
            int GridCol = (int) col / (pixelSize / GridSize);

            if (isLeftClick && !MarkedSquares[GridRow][GridCol]) {
                showAllAround(GridRow, GridCol);
            } else {
                try {
                    Node temp = getNodeByRowColumnIndex(CoverSquareGridpane, GridRow, GridCol);
                    FileInputStream inputstream = new FileInputStream("src/main/resources/fxml/images/Square.jpg");

                    if (!MarkedSquares[GridRow][GridCol]) {
                        if (MainArray[GridRow][GridCol] == null || (MainArray[GridRow][GridCol] != null && MainArray[GridRow][GridCol] != 0)) {
                            inputstream = new FileInputStream("src/main/resources/fxml/images/flag.png");
                            MarkedSquares[GridRow][GridCol] = true;
                        }
                    } else if (MarkedSquares[GridRow][GridCol]) {
                        MarkedSquares[GridRow][GridCol] = false;
                    }

                    Image image = new Image(inputstream);
                    ((ImageView) temp).setImage(image);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if(clearedAllNonBombs()){
                win();
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
                    int currentRow = row + x;
                    int currentCol = col + y;

                    if ((currentRow < GridSize && currentRow >= 0) && (currentCol >= 0 && currentCol < GridSize) &&
                            (x != y) && !(x == -1 && y == 1) && !(x == 1 && y == -1)) {
                        if (MainArray[currentRow][currentCol] == null) {
                            showAllAround(currentRow, currentCol);
                        }

                    }
                    if((currentRow < GridSize && currentRow >= 0) && (currentCol >= 0 && currentCol < GridSize)){
                        if (MainArray[currentRow][currentCol] !=  null && MainArray[currentRow][currentCol] == 2) {
                            Node num = getNodeByRowColumnIndex(CoverSquareGridpane, currentRow, currentCol);
                            MainArray[currentRow][currentCol] = 0;

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
        locked = true;
        showBombs();
        finalText.setText("You Lose");

        //I just thought this was hilarious
        win();
    }
    private void win() {
        Task<Void> sleeper = new Task<>() {
            @Override
            protected Void call() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        sleeper.setOnSucceeded(event -> {
            popUp.setVisible(true);
        });
        new Thread(sleeper).start();
    }
    private boolean clearedAllNonBombs(){
        int count = 0;
        int numMarked = 0;
        for(int x = 0; x < GridSize; x++){
            for(int y = 0; y < GridSize; y++){
                if(getNodeByRowColumnIndex(CoverSquareGridpane, x, y).isVisible())
                    count++;
            }
        }
        for(int x = 0; x < GridSize; x++){
            for(int y = 0; y < GridSize; y++){
                if(MarkedSquares[x][y])
                    numMarked++;
            }
        }
        count -= numMarked;
        return count == numBombs;
    }
    public void setKeyHeld(boolean x){
        keyHeld = x;
    }
    public void showBombs(){
        for (int x = 0; x < GridSize; x++){
            for(int y = 0; y < GridSize; y++){
                if(MainArray[x][y] != null && MainArray[x][y] == 1 && !MarkedSquares[x][y]){
                    Node temp = getNodeByRowColumnIndex(CoverSquareGridpane, x, y);
                    temp.setVisible(false);
                }
            }
        }
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void addNumbers(){
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
                    rect.setOpacity(.65);

                    CoverSquareGridpane.add(rect, row, col);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

