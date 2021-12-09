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
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

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
        //String yeet = "" + temp.getClass();
        if(temp != null){
            //System.out.println(yeet);
            temp = getNodeByRowColumnIndex(CoverSquareGridpane, row, col);
            temp.setVisible(false);
            lose();
        }
        else{
            //System.out.println(yeet);
            temp = getNodeByRowColumnIndex(CoverSquareGridpane, row, col);
            temp.setVisible(false);

            showNearby(row, col);
        }
    }
    private void showNearby(int row, int col){
        Node temp;
        for(int x = -1; x < 2; x++){
            for(int y = -1; y < 2; y++){
                if((row < GridSize && row >= 0) && (col >= 0 && col < GridSize) && !(x == 0 && y == 0)){
                    temp = getNodeByRowColumnIndex(MainGridpane, row + x, col + y);
                    if(temp == null) {
                        temp = getNodeByRowColumnIndex(CoverSquareGridpane, row + x, col + y);
                        if(temp != null) {
                            temp.setVisible(false);
                        }
                    }
                }
            }
        }
    }

    private void lose(){

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
            for (int row = 0; row < GridSize; row++) {
                for (int col = 0; col < GridSize; col++) {
                    FileInputStream inputstream = new FileInputStream("/Users/3064683/Desktop/mine-icon.png");
                    Image image = new Image(inputstream);

                    ImageView tempImage = new ImageView();
                    tempImage.setImage(image);
                    tempImage.setFitHeight(pixelSize / GridSize);
                    tempImage.setFitWidth(pixelSize / GridSize);

                    if (num < numBombs && Math.random() < .206) {
                        MainGridpane.add(tempImage, row, col);
                        num++;
                    }
                    //GridPane.setConstraints((Node) image, 2, 0);
                }
            }
            if (num < numBombs) {
                setBoard(num);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void coverBoard() {
        try {
            for (int row = 0; row < GridSize; row++) {
                for (int col = 0; col < GridSize; col++) {
                    FileInputStream inputstream = new FileInputStream("/Users/3064683/Desktop/Square.jpg");
                    Image image = new Image(inputstream);

                    ImageView rect = new ImageView();
                    rect.setImage(image);

                    rect.setFitHeight(pixelSize / GridSize);
                    rect.setFitWidth(pixelSize / GridSize);

                    CoverSquareGridpane.add(rect, row, col);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
