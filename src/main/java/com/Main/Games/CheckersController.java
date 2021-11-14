package com.Main.Games;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.shape.Circle;

public class CheckersController {
    private class Piece{
        private final String color;
        private String type;
        public Piece(String color, String type){
            this.color = color;
            this.type = type;
        }
        public String getColor(){
            return color;
        }
        public String getType(){
            return type;
        }
        public void upgradeToKing(){
            type = "king";
        }
    }

    @FXML
    Circle Red1, Red2, Red3, Red4, Red5, Red6, Red7, Red8, Red9, Red10, Red11, Red12;

    @FXML
    Circle Black1, Black2, Black3, Black4, Black5, Black6, Black7, Black8, Black9, Black10, Black11, Black12;

    @FXML
    Button  Button0, Button1, Button2, Button3, Button4, Button5, Button6, Button7, Button8, Button9, Button10,
            Button11, Button12, Button13, Button14, Button15, Button16, Button17, Button18, Button19, Button20,
            Button21, Button22, Button23, Button24, Button25, Button26, Button27, Button28, Button29, Button30,
            Button31, Button32, Button33, Button34, Button35, Button36, Button37, Button38, Button39, Button40,
            Button41, Button42, Button43, Button44, Button45, Button46, Button47, Button48, Button49, Button50,
            Button51, Button52, Button53, Button54, Button55, Button56, Button57, Button58, Button59, Button60,
            Button61, Button62, Button63;

    int rowInput1 = -1;
    int colInput1 = -1;
    int rowInput2 = -1;
    int colInput2 = -1;
    Piece[][] Board = new Piece[8][8];


    public void initialize(){
        setStarterBoard();
        displayBoard();
    }
    public void callMove(ActionEvent e){
        Object source = e.getSource();
        if(source == Button0)
            move(0,0);
        else if(source == Button1)
            move(0,1);
        else if(source == Button2)
            move(0,2);
        else if(source == Button3)
            move(0,3);
        else if(source == Button4)
            move(0,4);
        else if(source == Button5)
            move(0,5);
        else if(source == Button6)
            move(0,6);
        else if(source == Button7)
            move(0,7);

        else if(source == Button8)
            move(1,0);
        else if(source == Button9)
            move(1,1);
        else if(source == Button10)
            move(1,2);
        else if(source == Button11)
            move(1,3);
        else if(source == Button12)
            move(1,4);
        else if(source == Button13)
            move(1,5);
        else if(source == Button14)
            move(1,6);
        else if(source == Button15)
            move(1,7);

        else if(source == Button16)
            move(2,0);
        else if(source == Button17)
            move(2,1);
        else if(source == Button18)
            move(2,2);
        else if(source == Button19)
            move(2,3);
        else if(source == Button20)
            move(2,4);
        else if(source == Button21)
            move(2,5);
        else if(source == Button22)
            move(2,6);
        else if(source == Button23)
            move(2,7);

        else if(source == Button24)
            move(3,0);
        else if(source == Button25)
            move(3,1);
        else if(source == Button26)
            move(3,2);
        else if(source == Button27)
            move(3,3);
        else if(source == Button28)
            move(3,4);
        else if(source == Button29)
            move(3,5);
        else if(source == Button30)
            move(3,6);
        else if(source == Button31)
            move(3,7);

        else if(source == Button32)
            move(4,0);
        else if(source == Button33)
            move(4,1);
        else if(source == Button34)
            move(4,2);
        else if(source == Button35)
            move(4,3);
        else if(source == Button36)
            move(4,4);
        else if(source == Button37)
            move(4,5);
        else if(source == Button38)
            move(4,6);
        else if(source == Button39)
            move(4,7);

        else if(source == Button40)
            move(5,0);
        else if(source == Button41)
            move(5,1);
        else if(source == Button42)
            move(5,2);
        else if(source == Button43)
            move(5,3);
        else if(source == Button44)
            move(5,4);
        else if(source == Button45)
            move(5,5);
        else if(source == Button46)
            move(5,6);
        else if(source == Button47)
            move(5,7);

        else if(source == Button48)
            move(6,0);
        else if(source == Button49)
            move(6,1);
        else if(source == Button50)
            move(6,2);
        else if(source == Button51)
            move(6,3);
        else if(source == Button52)
            move(6,4);
        else if(source == Button53)
            move(6,5);
        else if(source == Button54)
            move(6,6);
        else if(source == Button55)
            move(6,7);

        else if(source == Button56)
            move(7,0);
        else if(source == Button57)
            move(7,1);
        else if(source == Button58)
            move(7,2);
        else if(source == Button59)
            move(7,3);
        else if(source == Button60)
            move(7,4);
        else if(source == Button61)
            move(7,5);
        else if(source == Button62)
            move(7,6);
        else if(source == Button63)
            move(7,7);
    }
    public void move(int row, int column){
        if(rowInput1 == -1 && colInput1 == -1) {
            rowInput1 = row;
            colInput1 = column;
        }
        else {
            rowInput2 = row;
            colInput2 = column;

            Board[rowInput2][colInput2] = Board[rowInput1][colInput1];
            Board[rowInput1][colInput1] = null;

            displayBoard();
            rowInput1 = -1;
            colInput1 = -1;
            rowInput2 = -1;
            colInput2 = -1;
        }
        /*
        else {
            rowInput2 = row;
            colInput2 = column;

            if(canMove(Board[rowInput1][colInput1], rowInput1, colInput1, rowInput2, colInput2)) {
                Piece temp;
               if(Board[rowInput1][colInput2].getColor().equals("Red")){
                   if(Board[rowInput1][colInput2].getType().equals("regular")) {
                       //Checking to move diagonally forward one space
                       if ((rowInput1 - 1 == rowInput2 && colInput1 + 1 == colInput2) || (rowInput1 - 1 == rowInput2 && colInput1 - 1 == colInput2)) {
                           Board[rowInput2][colInput2] = Board[rowInput1][colInput1];
                           Board[rowInput1][colInput1] = null;
                       }
                       //Jumping Logic
                       else{

                       }
                   }
               }
                else{

                }


                displayBoard();
            }
            rowInput1 = -1;
            colInput1 = -1;
            rowInput2 = -1;
            colInput2 = -1;
        }

         */
    }
    public boolean canMove(Piece piece, int row, int column, int newRow, int newCol){
        if(piece == null)
            return false;
        //checks if it is one diagonal space
        if(piece.getColor().equals("Red")){

        }
        else if (piece.getColor().equals("Black")){

        }
        return true;
    }
    public void displayBoard(){
        int numRedMoved = 1;
        int numBlackMoved = 1;
        for(int row = 0; row < 8; row++){
            for(int col = 0; col < 8; col++){
                //hide("Red");
                if(Board[row][col] != null && Board[row][col].getColor().equals("Red")) {
                    if(numRedMoved == 1) {
                        Red1.setCenterX((col * 75) + 37.5);
                        Red1.setCenterY((row * 75) + 37.5);
                        Red1.setVisible(true);
                    }
                    else if(numRedMoved == 2) {
                        Red2.setCenterX((col * 75) + 37.5);
                        Red2.setCenterY((row * 75) + 37.5);
                        Red2.setVisible(true);
                    }
                    else if(numRedMoved == 3) {
                        Red3.setCenterX((col * 75) + 37.5);
                        Red3.setCenterY((row * 75) + 37.5);
                        Red3.setVisible(true);
                    }
                    else if(numRedMoved == 4) {
                        Red4.setCenterX((col * 75) + 37.5);
                        Red4.setCenterY((row * 75) + 37.5);
                        Red4.setVisible(true);
                    }
                    else if(numRedMoved == 5) {
                        Red5.setCenterX((col * 75) + 37.5);
                        Red5.setCenterY((row * 75) + 37.5);
                        Red5.setVisible(true);
                    }
                    else if(numRedMoved == 6) {
                        Red6.setCenterX((col * 75) + 37.5);
                        Red6.setCenterY((row * 75) + 37.5);
                        Red6.setVisible(true);
                    }
                    else if(numRedMoved == 7) {
                        Red7.setCenterX((col * 75) + 37.5);
                        Red7.setCenterY((row * 75) + 37.5);
                        Red7.setVisible(true);
                    }
                    else if(numRedMoved == 8) {
                        Red8.setCenterX((col * 75) + 37.5);
                        Red8.setCenterY((row * 75) + 37.5);
                        Red8.setVisible(true);
                    }
                    else if(numRedMoved == 9) {
                        Red9.setCenterX((col * 75) + 37.5);
                        Red9.setCenterY((row * 75) + 37.5);
                        Red9.setVisible(true);
                    }
                    else if(numRedMoved == 10) {
                        Red10.setCenterX((col * 75) + 37.5);
                        Red10.setCenterY((row * 75) + 37.5);
                        Red10.setVisible(true);
                    }
                    else if(numRedMoved == 11) {
                        Red11.setCenterX((col * 75) + 37.5);
                        Red11.setCenterY((row * 75) + 37.5);
                        Red11.setVisible(true);
                    }
                    else if(numRedMoved == 12) {
                        Red12.setCenterX((col * 75) + 37.5);
                        Red12.setCenterY((row * 75) + 37.5);
                        Red12.setVisible(true);
                    }
                    numRedMoved++;
                }
                //hide("Black");
                if(Board[row][col] != null && Board[row][col].getColor().equals("Black")) {
                    if(numBlackMoved == 1) {
                        Black1.setCenterX((col * 75) + 37.5);
                        Black1.setCenterY((row * 75) + 37.5);
                        Black1.setVisible(true);
                    }
                    else if(numBlackMoved == 2) {
                        Black2.setCenterX((col * 75) + 37.5);
                        Black2.setCenterY((row * 75) + 37.5);
                        Black2.setVisible(true);
                    }
                    else if(numBlackMoved == 3) {
                        Black3.setCenterX((col * 75) + 37.5);
                        Black3.setCenterY((row * 75) + 37.5);
                        Black3.setVisible(true);
                    }
                    else if(numBlackMoved == 4) {
                        Black4.setCenterX((col * 75) + 37.5);
                        Black4.setCenterY((row * 75) + 37.5);
                        Black4.setVisible(true);
                    }
                    else if(numBlackMoved == 5) {
                        Black5.setCenterX((col * 75) + 37.5);
                        Black5.setCenterY((row * 75) + 37.5);
                        Black5.setVisible(true);
                    }
                    else if(numBlackMoved == 6) {
                        Black6.setCenterX((col * 75) + 37.5);
                        Black6.setCenterY((row * 75) + 37.5);
                        Black6.setVisible(true);
                    }
                    else if(numBlackMoved == 7) {
                        Black7.setCenterX((col * 75) + 37.5);
                        Black7.setCenterY((row * 75) + 37.5);
                        Black7.setVisible(true);
                    }
                    else if(numBlackMoved == 8) {
                        Black8.setCenterX((col * 75) + 37.5);
                        Black8.setCenterY((row * 75) + 37.5);
                        Black8.setVisible(true);
                    }
                    else if(numBlackMoved == 9) {
                        Black9.setCenterX((col * 75) + 37.5);
                        Black9.setCenterY((row * 75) + 37.5);
                        Black9.setVisible(true);
                    }
                    else if(numBlackMoved == 10) {
                        Black10.setCenterX((col * 75) + 37.5);
                        Black10.setCenterY((row * 75) + 37.5);
                        Black10.setVisible(true);
                    }
                    else if(numBlackMoved == 11) {
                        Black11.setCenterX((col * 75) + 37.5);
                        Black11.setCenterY((row * 75) + 37.5);
                        Black11.setVisible(true);
                    }
                    else if(numBlackMoved == 12) {
                        Black12.setCenterX((col * 75) + 37.5);
                        Black12.setCenterY((row * 75) + 37.5);
                        Black12.setVisible(true);
                    }
                    numBlackMoved++;
                }
            }
        }
    }
    public void setStarterBoard(){
        Board[7][0] = new Piece("Red", "normal");
        Board[7][2] = new Piece("Red", "normal");
        Board[7][4] = new Piece("Red", "normal");
        Board[7][6] = new Piece("Red", "normal");
        Board[6][1] = new Piece("Red", "normal");
        Board[6][3] = new Piece("Red", "normal");
        Board[6][5] = new Piece("Red", "normal");
        Board[6][7] = new Piece("Red", "normal");
        Board[5][0] = new Piece("Red", "normal");
        Board[5][2] = new Piece("Red", "normal");
        Board[5][4] = new Piece("Red", "normal");
        Board[5][6] = new Piece("Red", "normal");

        Board[0][1] = new Piece("Black", "normal");
        Board[0][3] = new Piece("Black", "normal");
        Board[0][5] = new Piece("Black", "normal");
        Board[0][7] = new Piece("Black", "normal");
        Board[1][0] = new Piece("Black", "normal");
        Board[1][2] = new Piece("Black", "normal");
        Board[1][4] = new Piece("Black", "normal");
        Board[1][6] = new Piece("Black", "normal");
        Board[2][1] = new Piece("Black", "normal");
        Board[2][3] = new Piece("Black", "normal");
        Board[2][5] = new Piece("Black", "normal");
        Board[2][7] = new Piece("Black", "normal");
    }
    public void hide(String s){
        if(s.equals("Red")) {
            Red1.setVisible(false);
            Red2.setVisible(false);
            Red3.setVisible(false);
            Red4.setVisible(false);
            Red5.setVisible(false);
            Red6.setVisible(false);
            Red7.setVisible(false);
            Red8.setVisible(false);
            Red9.setVisible(false);
            Red10.setVisible(false);
            Red11.setVisible(false);
            Red12.setVisible(false);
        }
        else if(s.equals("Black")) {
            Black1.setVisible(false);
            Black2.setVisible(false);
            Black3.setVisible(false);
            Black4.setVisible(false);
            Black5.setVisible(false);
            Black6.setVisible(false);
            Black7.setVisible(false);
            Black8.setVisible(false);
            Black9.setVisible(false);
            Black10.setVisible(false);
            Black11.setVisible(false);
            Black12.setVisible(false);
        }
    }
}
