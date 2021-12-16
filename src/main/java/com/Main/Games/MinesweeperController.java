package com.Main.Games;

import com.Main.MenuController;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.File;
import java.util.Objects;

public class MinesweeperController extends MenuController {
    @FXML
    Rectangle mainBox;
    @FXML
    GridPane MainGridPane, CoverSquareGridPane, stupidStyleGridPane;
    @FXML
    AnchorPane popUp;
    @FXML
    Text finalText;

    private boolean keyHeld = false;

    //Constants for the board
    private final String musicFile = "src/main/resources/explosion.mp3";
    private final int GridSize = 24;
    private final double pixelSize = 600.0;
    private final int numBombs = (int) ((GridSize * GridSize) / 4.85);
    private boolean[][] MarkedSquares;
    private boolean[][] isShown;
    private boolean locked;
    private final double tileSize = pixelSize / GridSize;

    //-1 == nothing, clicked on square == 0, Bomb == 1, Number == 2
    private Integer[][] MainArray;


    public void initialize() {
        long startTime = System.nanoTime();
        MainGridPane.getChildren().clear();
        CoverSquareGridPane.getChildren().clear();
        stupidStyleGridPane.getChildren().clear();
        popUp.setVisible(false);
        locked = false;
        MainArray = new Integer[GridSize][GridSize];
        MarkedSquares = new boolean[GridSize][GridSize];
        isShown = new boolean[GridSize][GridSize];
        finalText.setText("You Win!");

        MainGridPane.setAlignment(Pos.CENTER);

        mainBox.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY)) {
                click(!keyHeld, event.getSceneY(), event.getSceneX());
            } else if (event.getButton().equals(MouseButton.SECONDARY)) {
                click(false, event.getSceneY(), event.getSceneX());
            }
        });
        for(int x = 0; x < GridSize; x++){
            for(int y = 0; y < GridSize; y++){
                MarkedSquares[x][y] = false;
                isShown[x][y] = false;
                MainArray[x][y] = -1;
             }
            RowConstraints row = new RowConstraints();
            row.setPrefHeight(tileSize);
            ColumnConstraints col = new ColumnConstraints();
            col.setPrefWidth(tileSize);

            MainGridPane.getRowConstraints().add(row);
            MainGridPane.getColumnConstraints().add(col);
            CoverSquareGridPane.getRowConstraints().add(row);
            CoverSquareGridPane.getColumnConstraints().add(col);
            stupidStyleGridPane.getRowConstraints().add(row);
            stupidStyleGridPane.getColumnConstraints().add(col);
        }

        long startTime2 = System.nanoTime();
        setBoard(0);
        addNumbers();
        coverBoard();
        gridLines();
        long endTime2 = System.nanoTime();
        //System.out.println((endTime2 - startTime2)/1000000);

        long endTime = System.nanoTime();
        //System.out.println((endTime - startTime)/1000000);
        //System.out.println();
    }

    public void click(Boolean isLeftClick, double row, double col) {
        if(!locked) {
            int GridRow = (int) (row / tileSize);
            int GridCol = (int) (col / tileSize);

            if(GridRow >= GridSize)
                GridRow = GridSize-1;
            if(GridCol >= GridSize)
                GridCol = GridSize-1;

            if (isLeftClick && !MarkedSquares[GridRow][GridCol]) {
                showAllAround(GridRow, GridCol);
            } else if(!isLeftClick){
                try {
                    Node temp = getImageByRowCol(CoverSquareGridPane, GridRow, GridCol);
                    Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/fxml/images/Square.jpg")));

                    if (!MarkedSquares[GridRow][GridCol]) {
                        if (MainArray[GridRow][GridCol] == -1 || (MainArray[GridRow][GridCol] != -1 && MainArray[GridRow][GridCol] != 0)) {
                            image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/fxml/images/flag.png")));
                            MarkedSquares[GridRow][GridCol] = true;
                        }
                    } else if (MarkedSquares[GridRow][GridCol]) {
                        MarkedSquares[GridRow][GridCol] = false;
                    }

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
        if (MainArray[row][col] == 1) {
            temp = getImageByRowCol(CoverSquareGridPane, row, col);
            temp.setVisible(false);
            lose();
        } else {
            showNearby(row, col);
        }
    }
    private void showNearby(int row, int col) {
        if (MainArray[row][col] == -1) {
            Node temp = getImageByRowCol(CoverSquareGridPane, row, col);
            temp.setVisible(false);
            MainArray[row][col] = 0;
            isShown[row][col] = true;
            for (int x = -1; x < 2; x++) {
                for (int y = -1; y < 2; y++) {
                    int currentRow = row + x;
                    int currentCol = col + y;

                    if ((currentRow < GridSize && currentRow >= 0) && (currentCol >= 0 && currentCol < GridSize) &&
                            (x != y) && !(x == -1 && y == 1) && !(x == 1 && y == -1)) {
                        if (MainArray[currentRow][currentCol] == -1) {
                            showAllAround(currentRow, currentCol);
                        }

                    }
                    if((currentRow < GridSize && currentRow >= 0) && (currentCol >= 0 && currentCol < GridSize)){
                        if (MainArray[currentRow][currentCol] == 2) {
                            Node num = getImageByRowCol(CoverSquareGridPane, currentRow, currentCol);
                            MainArray[currentRow][currentCol] = 0;
                            isShown[currentRow][currentCol] = true;
                            num.setVisible(false);
                        }
                    }
                }
            }
        }
        else{
            Node temp = getImageByRowCol(CoverSquareGridPane, row, col);
            temp.setVisible(false);
            MainArray[row][col] = 0;
            isShown[row][col] = true;
        }
    }
    private int numBombsNearby(int row, int col){
        int count = 0;
        if(MainArray[row][col] != 1) {
            for (int x = -1; x < 2; x++) {
                for (int y = -1; y < 2; y++) {
                    if (row + x < GridSize && row + x >= 0 && col + y < GridSize && col + y >= 0) {
                        if (MainArray[row + x][col + y] == 1) {
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

        Media sound = new Media(new File(musicFile).toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(sound);
        mediaPlayer.play();

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
        sleeper.setOnSucceeded(event -> popUp.setVisible(true));
        new Thread(sleeper).start();
    }
    private boolean clearedAllNonBombs(){
        int count = 0;
        for(int x = 0; x < GridSize; x++) {
            for (int y = 0; y < GridSize; y++) {
                if (!isShown[x][y])
                    count++;
            }
        }
        return count == numBombs;
    }
    public void setKeyHeld(boolean x){
        keyHeld = x;
    }
    public void showBombs(){
        for (int x = 0; x < GridSize; x++){
            for(int y = 0; y < GridSize; y++){
                if(MainArray[x][y] == 1 && !MarkedSquares[x][y]){
                    Node temp = getImageByRowCol(CoverSquareGridPane, x, y);
                    temp.setVisible(false);
                }
            }
        }
    }

    private Node getImageByRowCol(GridPane pane, final int row, final int column) {
        Node result = null;
        ObservableList<Node> children = pane.getChildren();

        for (Node node : children) {
            if (GridPane.getRowIndex(node) != null && GridPane.getColumnIndex(node) != null && node instanceof ImageView) {
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
            Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/fxml/images/mine-icon.png")));

            for (int row = 0; row < GridSize; row++) {
                for (int col = 0; col < GridSize; col++) {
                    //Normally 206
                    if (num < numBombs && Math.random() < .1 && MainArray[row][col] == -1) {

                        ImageView tempImage = new ImageView();
                        tempImage.setImage(image);
                        tempImage.setFitHeight(pixelSize / GridSize);
                        tempImage.setFitWidth(pixelSize / GridSize);

                        MainGridPane.add(tempImage, row, col);

                        //I hate this so much but this is the only way to make it work
                        MainArray[col][row] = 1;
                        num++;
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
                int nearbyBombs = numBombsNearby(row, col);
                if(nearbyBombs != 0) {
                    Text tempText = new Text();
                    tempText.setVisible(true);
                    tempText.setStyle("-fx-font-size : 20px");
                    tempText.setText("" + nearbyBombs);
                    tempText.setTranslateX(tileSize/2-6);
                    MainArray[row][col] = 2;
                    MainGridPane.add(tempText, col, row);
                }
            }
        }
    }
    private void coverBoard() {
        try {
            Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/fxml/images/Square.jpg")));
            for (int row = 0; row < GridSize; row++) {
                for (int col = 0; col < GridSize; col++) {
                    ImageView rect = new ImageView();
                    rect.setImage(image);

                    rect.setFitHeight(tileSize);
                    rect.setFitWidth(tileSize);

                    //rect.setOpacity(.6);

                    CoverSquareGridPane.add(rect, row, col);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void gridLines(){
        for(int x = 0; x < GridSize; x++) {
            for(int y = 0; y < GridSize; y++) {
                Rectangle thing = new Rectangle();
                thing.setWidth(tileSize-1.75);
                thing.setHeight(tileSize-1.75);
                thing.setStyle("-fx-fill: #bdbdbd; -fx-stroke: #bdbdbd; -fx-strokeWidth: 1.75");
                thing.setVisible(true);
                stupidStyleGridPane.getChildren().add(thing);
                GridPane.setConstraints(thing, x,y);
            }
        }
    }
}