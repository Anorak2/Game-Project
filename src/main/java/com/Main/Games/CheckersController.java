package com.Main.Games;

import javafx.fxml.FXML;
import javafx.scene.shape.Circle;

public class CheckersController {


    @FXML
    Circle Red1;
    @FXML
    Circle Red2;
    @FXML
    Circle Red3;
    @FXML
    Circle Red4;
    @FXML
    Circle Red5;
    @FXML
    Circle Red6;
    @FXML
    Circle Red7;
    @FXML
    Circle Red8;
    @FXML
    Circle Red9;
    @FXML
    Circle Red10;
    @FXML
    Circle Red11;
    @FXML
    Circle Red12;

    @FXML
    Circle Black1;
    @FXML
    Circle Black2;
    @FXML
    Circle Black3;
    @FXML
    Circle Black4;
    @FXML
    Circle Black5;
    @FXML
    Circle Black6;
    @FXML
    Circle Black7;
    @FXML
    Circle Black8;
    @FXML
    Circle Black9;
    @FXML
    Circle Black10;
    @FXML
    Circle Black11;
    @FXML
    Circle Black12;

    int rowInput1 = -1;
    int colInput1 = -1;
    int rowInput2 = -1;
    int colInput2 = -1;
    String[][] Board = new String[8][8];


    public void initialize(){
        setStarterBoard();
        displayBoard();
    }
    public void move(int row, int column){
        if(rowInput1 == -1 && colInput1 == -1) {
            rowInput1 = row;
            colInput1 = column;
        }
        else {
            rowInput2 = row;
            colInput2 = column;
        }

        if(rowInput2 != -1){

        }

        displayBoard();
    }
    public void canMove(int row, int column, int newRow, int newCol){

    }
    public void displayBoard(){
        int numRedMoved = 1;
        int numBlackMoved = 1;
        for(int row = 0; row < 8; row++){
            for(int col = 0; col < 8; col++){
                if(Board[row][col] != null && Board[row][col].equals("Red")) {
                    if(numRedMoved == 1) {
                        Red1.setCenterX((col * 75) + 37.5);
                        Red1.setCenterY((row * 75) + 37.5);
                    }
                    else if(numRedMoved == 2) {
                        Red2.setCenterX((col * 75) + 37.5);
                        Red2.setCenterY((row * 75) + 37.5);
                    }
                    else if(numRedMoved == 3) {
                        Red3.setCenterX((col * 75) + 37.5);
                        Red3.setCenterY((row * 75) + 37.5);
                    }
                    else if(numRedMoved == 4) {
                        Red4.setCenterX((col * 75) + 37.5);
                        Red4.setCenterY((row * 75) + 37.5);
                    }
                    else if(numRedMoved == 5) {
                        Red5.setCenterX((col * 75) + 37.5);
                        Red5.setCenterY((row * 75) + 37.5);
                    }
                    else if(numRedMoved == 6) {
                        Red6.setCenterX((col * 75) + 37.5);
                        Red6.setCenterY((row * 75) + 37.5);
                    }
                    else if(numRedMoved == 7) {
                        Red7.setCenterX((col * 75) + 37.5);
                        Red7.setCenterY((row * 75) + 37.5);
                    }
                    else if(numRedMoved == 8) {
                        Red8.setCenterX((col * 75) + 37.5);
                        Red8.setCenterY((row * 75) + 37.5);
                    }
                    else if(numRedMoved == 9) {
                        Red9.setCenterX((col * 75) + 37.5);
                        Red9.setCenterY((row * 75) + 37.5);
                    }
                    else if(numRedMoved == 10) {
                        Red10.setCenterX((col * 75) + 37.5);
                        Red10.setCenterY((row * 75) + 37.5);
                    }
                    else if(numRedMoved == 11) {
                        Red11.setCenterX((col * 75) + 37.5);
                        Red11.setCenterY((row * 75) + 37.5);
                    }
                    else if(numRedMoved == 12) {
                        Red12.setCenterX((col * 75) + 37.5);
                        Red12.setCenterY((row * 75) + 37.5);
                    }
                    numRedMoved++;
                }
                if(Board[row][col] != null && Board[row][col].equals("Black")) {
                    if(numBlackMoved == 1) {
                        Black1.setCenterX((col * 75) + 37.5);
                        Black1.setCenterY((row * 75) + 37.5);
                    }
                    else if(numBlackMoved == 2) {
                        Black2.setCenterX((col * 75) + 37.5);
                        Black2.setCenterY((row * 75) + 37.5);
                    }
                    else if(numBlackMoved == 3) {
                        Black3.setCenterX((col * 75) + 37.5);
                        Black3.setCenterY((row * 75) + 37.5);
                    }
                    else if(numBlackMoved == 4) {
                        Black4.setCenterX((col * 75) + 37.5);
                        Black4.setCenterY((row * 75) + 37.5);
                    }
                    else if(numBlackMoved == 5) {
                        Black5.setCenterX((col * 75) + 37.5);
                        Black5.setCenterY((row * 75) + 37.5);
                    }
                    else if(numBlackMoved == 6) {
                        Black6.setCenterX((col * 75) + 37.5);
                        Black6.setCenterY((row * 75) + 37.5);
                    }
                    else if(numBlackMoved == 7) {
                        Black7.setCenterX((col * 75) + 37.5);
                        Black7.setCenterY((row * 75) + 37.5);
                    }
                    else if(numBlackMoved == 8) {
                        Black8.setCenterX((col * 75) + 37.5);
                        Black8.setCenterY((row * 75) + 37.5);
                    }
                    else if(numBlackMoved == 9) {
                        Black9.setCenterX((col * 75) + 37.5);
                        Black9.setCenterY((row * 75) + 37.5);
                    }
                    else if(numBlackMoved == 10) {
                        Black10.setCenterX((col * 75) + 37.5);
                        Black10.setCenterY((row * 75) + 37.5);
                    }
                    else if(numBlackMoved == 11) {
                        Black11.setCenterX((col * 75) + 37.5);
                        Black11.setCenterY((row * 75) + 37.5);
                    }
                    else if(numBlackMoved == 12) {
                        Black12.setCenterX((col * 75) + 37.5);
                        Black12.setCenterY((row * 75) + 37.5);
                    }
                    numBlackMoved++;
                }
            }
        }
    }
    public void setStarterBoard(){
        Board[7][0] = "Red";
        Board[7][2] = "Red";
        Board[7][4] = "Red";
        Board[7][6] = "Red";
        Board[6][1] = "Red";
        Board[6][3] = "Red";
        Board[6][5] = "Red";
        Board[6][7] = "Red";
        Board[5][0] = "Red";
        Board[5][2] = "Red";
        Board[5][4] = "Red";
        Board[5][6] = "Red";

        Board[0][1] = "Black";
        Board[0][3] = "Black";
        Board[0][5] = "Black";
        Board[0][7] = "Black";
        Board[1][0] = "Black";
        Board[1][2] = "Black";
        Board[1][4] = "Black";
        Board[1][6] = "Black";
        Board[2][1] = "Black";
        Board[2][3] = "Black";
        Board[2][5] = "Black";
        Board[2][7] = "Black";
    }
}
