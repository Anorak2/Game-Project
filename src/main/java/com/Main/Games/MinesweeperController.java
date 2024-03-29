package com.Main.Games;

import com.Main.MenuController;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.util.ArrayList;
import java.util.Objects;

public class MinesweeperController extends MenuController {
    @FXML
    Rectangle mainBox;
    @FXML
    GridPane MainGridPane, CoverSquareGridPane;
    @FXML
    AnchorPane popUp, bigBoi, HelpMenu, text;
    @FXML
    Text finalText;
    @FXML
    BorderPane Menu;

    private boolean keyHeld = false;

    //Constants for the board
    private int GridSize = 24;
    private int numBombs;
    private boolean[][] MarkedSquares;
    private boolean locked;
    private long tileSize;
    private boolean safety = false;
    private boolean isFirstClick;
    boolean godMode = false;
    private final Image squareImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/fxml/images/Square.jpg")));
    private final Image flagImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/fxml/images/flag.png")));

    //-1 == nothing, clicked on square == 0, Bomb == 1, Number == 2
    private Integer[][] MainArray;


    public void initialize() {
        try {
            double pixelSize = 600.0;

            //Menu stuff
                        final Menu menu1 = new Menu("size");
            MenuItem six = new MenuItem("6x6");
            MenuItem twelve = new MenuItem("12x12");
            MenuItem eighteen = new MenuItem("18x18");
            MenuItem twentyFour = new MenuItem("24x24");
            six.setOnAction(event -> changeGridSize(6));
            twelve.setOnAction(event -> changeGridSize(12));
            eighteen.setOnAction(event -> changeGridSize(18));
            twentyFour.setOnAction(event -> changeGridSize(24));
            menu1.getItems().addAll(six, twelve, eighteen, twentyFour);


            final Menu menu2 = new Menu("Other");
            CheckMenuItem gMode = new CheckMenuItem("God Mode");
            CheckMenuItem safe = new CheckMenuItem("Safety Off");
            MenuItem help = new MenuItem("Help");

            gMode.setOnAction(event -> godMode());
            safe.setOnAction(event -> toggleSafety());
            help.setOnAction(event -> help());
            menu2.getItems().addAll(gMode, safe, help);

            MenuBar menuBar = new MenuBar();
            menuBar.getMenus().addAll(menu1, menu2);
            menuBar.useSystemMenuBarProperty().set(true);
            Menu.setTop(menuBar);

            numBombs = (int) ((GridSize * GridSize) / 4.85);
            tileSize = (long) (pixelSize / GridSize);
            clearMainGridPane();
            CoverSquareGridPane.getChildren().clear();
            CoverSquareGridPane.setGridLinesVisible(true);
            MainGridPane.setGridLinesVisible(true);
            popUp.setVisible(false);
            locked = false;
            MainArray = new Integer[GridSize][GridSize];
            MarkedSquares = new boolean[GridSize][GridSize];
            finalText.setText("You Win!");
            isFirstClick = true;


            MainGridPane.getRowConstraints().clear();
            MainGridPane.getColumnConstraints().clear();
            CoverSquareGridPane.getRowConstraints().clear();
            CoverSquareGridPane.getColumnConstraints().clear();

            mainBox.setOnMouseClicked(event -> {
                if (event.getButton().equals(MouseButton.PRIMARY)) {
                    click(!keyHeld, event.getSceneY(), event.getSceneX());
                } else if (event.getButton().equals(MouseButton.SECONDARY)) {
                    click(false, event.getSceneY(), event.getSceneX());
                }
            });
            for (int x = 0; x < GridSize; x++) {
                for (int y = 0; y < GridSize; y++) {
                    MarkedSquares[x][y] = false;
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
            }
            setBoard(0);
            coverBoard();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void click(Boolean isLeftClick, double row, double col) {
        long startTime = System.nanoTime();
        int GridRow = (int) (row / tileSize);
        int GridCol = (int) (col / tileSize);
        if(isFirstClick && safety){
            if(MainArray[GridRow][GridCol] == 1)
                moveBomb(GridRow, GridCol);
            addNumbers();
        }
        else if(isFirstClick){
            addNumbers();
        }
        if(!locked) {
            if(GridRow >= GridSize)
                GridRow = GridSize-1;
            if(GridCol >= GridSize)
                GridCol = GridSize-1;

            if (isLeftClick && !MarkedSquares[GridRow][GridCol])
                showAllAround(GridRow, GridCol);
            else if(!isLeftClick){
                Node temp = getNodeByRowCol(CoverSquareGridPane, GridRow, GridCol);

                if (!MarkedSquares[GridRow][GridCol]) {
                    if (MainArray[GridRow][GridCol] == -1 || (MainArray[GridRow][GridCol] != -1 && MainArray[GridRow][GridCol] != 0)) {
                        MarkedSquares[GridRow][GridCol] = true;
                        ((ImageView) temp).setImage(flagImage);
                    }
                } else if (MarkedSquares[GridRow][GridCol]) {
                    MarkedSquares[GridRow][GridCol] = false;
                    ((ImageView) temp).setImage(squareImage);
                }
            }
            if(clearedAllNonBombs()){
                win(true);
            }
        }
        isFirstClick = false;
        long endTime = System.nanoTime();
        System.out.println((endTime - startTime)/ 1000000);
    }
    public void showAllAround(int row, int col) {
        Node temp;
        if (MainArray[row][col] == 1) {
            temp = getNodeByRowCol(CoverSquareGridPane, row, col);
            temp.setVisible(false);
            lose();
        } else {
            showNearby(row, col);
        }
    }
    private void showNearby(int row, int col) {
        if (MainArray[row][col] == -1) {
            Node temp = getNodeByRowCol(CoverSquareGridPane, row, col);
            temp.setVisible(false);
            MainArray[row][col] = 0;
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
                            Node num = getNodeByRowCol(CoverSquareGridPane, currentRow, currentCol);
                            MainArray[currentRow][currentCol] = 0;
                            num.setVisible(false);
                        }
                    }
                }
            }
        }
        else{
            Node temp = getNodeByRowCol(CoverSquareGridPane, row, col);
            temp.setVisible(false);
            MainArray[row][col] = 0;
        }
    }
    @SuppressWarnings("ConstantConditions")
    private int numBombsNearby(int row, int col){
        int count = 0, newRow, newCol;

        if(MainArray[row][col] == 1)
            return 0;

        if(row == 0 || row == GridSize-1 || col == 0 || col == GridSize-1) {
            for (int x = -1; x < 2; x++) {
                for (int y = -1; y < 2; y++) {
                    newRow = row + x;
                    newCol = col + y;
                    if (newRow < GridSize && newRow >= 0 && newCol < GridSize && newCol >= 0 &&
                            MainArray[newRow][newCol] == 1)
                        count++;
                }
            }
        }
        else {
            for (int x = -1; x < 2; x++) {
                for (int y = -1; y < 2; y++) {
                    if (MainArray[row + x][col + y] == 1)
                        count++;
                }
            }
        }
        return count;
    }

    private void lose() {
        locked = true;
        showBombs();
        finalText.setText("You Lose");

        Media sound = new Media(Objects.requireNonNull(getClass().getResource("/fxml/sounds/explosion.mp3")).toExternalForm());
        MediaPlayer mediaPlayer = new MediaPlayer(sound);
        mediaPlayer.play();

        //I just thought this was hilarious
        //Edit: not as funny anymore now that the variable is there
        win(false);
    }
    private void win(boolean didWin) {
        locked = true;
        if(didWin){
            Media sound = new Media(Objects.requireNonNull(getClass().getResource("/fxml/sounds/victory.mp3")).toExternalForm());
            MediaPlayer mediaPlayer = new MediaPlayer(sound);
            mediaPlayer.play();
        }
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
                if (MainArray[x][y] != 0)
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
                    Node temp = getNodeByRowCol(CoverSquareGridPane, x, y);
                    temp.setVisible(false);
                }
            }
        }
    }

    private Node getNodeByRowCol(GridPane pane, final int row, final int column) {
        Node result = null;
        ObservableList<Node> children = pane.getChildren();

        for (Node node : children) {
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
            Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/fxml/images/mine-icon.png")));

            for (int row = 0; row < GridSize; row++) {
                for (int col = 0; col < GridSize; col++) {
                    //Normally 206
                    if (num < numBombs && Math.random() < .1 && MainArray[col][row] == -1) {

                        ImageView tempImage = new ImageView();
                        tempImage.setImage(image);
                        tempImage.setFitHeight(tileSize);
                        tempImage.setFitWidth(tileSize);

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
        int nearbyBombs;
        for (int row = 0; row < GridSize; row++) {
            for (int col = 0; col < GridSize; col++) {
                nearbyBombs = numBombsNearby(row, col);
                if(nearbyBombs != 0) {
                    Text tempText = new Text(row*tileSize, col*tileSize, "" + nearbyBombs);
                    //Text tempText = new Text("" + nearbyBombs);
                    tempText.setStyle("-fx-font-size : 20px; -fx-font-size: "+ (tileSize/2+2) +"px");
                    GridPane.setHalignment(tempText, HPos.CENTER);

                    MainArray[row][col] = 2;
                    MainGridPane.add(tempText, col, row);
                    //text.getChildren().add(tempText);
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
                    if(godMode)
                        rect.setOpacity(.6);

                    CoverSquareGridPane.add(rect, row, col);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void moveBomb(int row, int col) {
        int newRow = (int)(Math.random()*GridSize);
        int newCol = (int)(Math.random()*GridSize);
        if(MainArray[newRow][newCol] != 1 && !(newRow == row && newCol == col)) {
            MainArray[row][col] = -1;

            MainArray[newRow][newCol] = 1;
            GridPane.setConstraints(getNodeByRowCol(MainGridPane, row, col), newCol, newRow);
        }
        else {
            moveBomb(row,col);
        }
    }

    private void changeGridSize(int x){
        GridSize = x;
        initialize();
    }
    private void clearMainGridPane(){
        ObservableList<Node> children = MainGridPane.getChildren();
        ArrayList<Node> badChildren = new ArrayList<>();

        for (Node node : children) {
            if (GridPane.getRowIndex(node) != null && GridPane.getColumnIndex(node) != null && (node instanceof ImageView || node instanceof Text)) {
                badChildren.add(node);
            }
        }

        for (Node badChild : badChildren) {
            MainGridPane.getChildren().remove(badChild);
        }
    }
    private void toggleSafety(){
        safety = !safety;
    }
    public void help(){
        HelpMenu.setVisible(!HelpMenu.isVisible());
    }
    public void godMode(){
        ObservableList<Node> children = CoverSquareGridPane.getChildren();
        if(godMode){
            for(Node node : children){
                if(node instanceof ImageView) {
                    int temp = MainArray[GridPane.getRowIndex(node)][GridPane.getColumnIndex(node)];
                    if (temp != 0)
                        node.setOpacity(1);
                }
            }
        }
        else{
            for(Node node : children){
                node.setOpacity(.6);
            }
        }
        godMode = !godMode;
    }
}