package com.Main.Games;

import com.Main.MenuController;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.Objects;

public class CheckersController extends MenuController {
    private static class Piece{
        private final String color;
        private boolean isKing = false;
        public Piece(String color){
            this.color = color;
        }
        public String getColor(){
            return color;
        }
        public boolean isKing(){
            return isKing;
        }
        public void upgradeToKing(){
            isKing = true;
        }
    }


    @FXML
    Circle Red1, Red2, Red3, Red4, Red5, Red6, Red7, Red8, Red9, Red10, Red11, Red12;

    @FXML
    Circle Black1, Black2, Black3, Black4, Black5, Black6, Black7, Black8, Black9, Black10, Black11, Black12;
    @FXML
    Text king1, king2, king3, king4, king5, king6, king7, king8, king9, king10, king11, king12, king13,
            king14, king15, king16, king17, king18, king19, king20, king21, king22, king23, king24;
    @FXML
    AnchorPane popUp;
    @FXML
    Text finalText;

    private int rowInput1 = -1;
    private int colInput1 = -1;
    private int rowInput2 = -1;
    private int colInput2 = -1;
    private Piece[][] MainBoard = new Piece[8][8];
    boolean lockmove = false;


    public void initialize(){
        MainBoard = new Piece[8][8];
        lockmove = false;
        popUp.setVisible(false);
        setStarterBoard();
        displayBoard();
    }

    private void crappyAi(){
        Task<Void> sleeper = new Task<>() {
            @Override
            protected Void call() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        sleeper.setOnSucceeded(event -> {
            Piece[][] tempBoard = new Piece[8][8];
            ArrayList<int[]> allMovesBlack = findAllBlackMoves(MainBoard);
            //ArrayList<int[]> allMovesRed = findAllRedMoves(MainBoard);
            ArrayList<Double> scores = new ArrayList<>();
            int index = 0;
            for (int x = 0; x < allMovesBlack.size(); x++) {
                allMovesBlack = findAllBlackMoves(MainBoard);
                //allMovesRed = findAllRedMoves(MainBoard);
                copyMain(tempBoard);
                movePiece(tempBoard, allMovesBlack.get(x)[0], allMovesBlack.get(x)[1], allMovesBlack.get(x)[2], allMovesBlack.get(x)[3]);
                scores.add(x, evaluatePosition(tempBoard));
            }
            if (!allMovesBlack.isEmpty()) {
                Double highest = scores.get(0);
                Double lowest = scores.get(0);
                if(allMovesBlack.size() > 1) {
                    for (int x = 1; x < scores.size(); x++) {
                        if (scores.get(x) >= highest) {
                            highest = scores.get(x);
                            index = x;
                        }
                        else if(scores.get(x) < lowest){
                            lowest = scores.get(x);
                        }
                    }
                }
                if(Objects.equals(highest, lowest)){
                    index = (int) (Math.random() * allMovesBlack.size());
                }
                movePiece(MainBoard, allMovesBlack.get(index)[0], allMovesBlack.get(index)[1], allMovesBlack.get(index)[2], allMovesBlack.get(index)[3]);

                //Promotion
                if(allMovesBlack.get(index)[2] == 7)
                    MainBoard[allMovesBlack.get(index)[2]][allMovesBlack.get(index)[3]].upgradeToKing();

                //Recursion for multi jump
                if     ((allMovesBlack.get(index)[0] + 2 == allMovesBlack.get(index)[2] && allMovesBlack.get(index)[1] + 2 == allMovesBlack.get(index)[3]) ||
                        (allMovesBlack.get(index)[0] + 2 == allMovesBlack.get(index)[2] && allMovesBlack.get(index)[1] - 2 == allMovesBlack.get(index)[3]) ||
                        (allMovesBlack.get(index)[0] - 2 == allMovesBlack.get(index)[2] && allMovesBlack.get(index)[1] + 2 == allMovesBlack.get(index)[3]) ||
                        (allMovesBlack.get(index)[0] - 2 == allMovesBlack.get(index)[2] && allMovesBlack.get(index)[1] - 2 == allMovesBlack.get(index)[3])) {
                    if (canCapture(MainBoard, allMovesBlack.get(index)[2], allMovesBlack.get(index)[3])) {
                        crappyAi();
                    }
                }
            }
            checkWinner();
        });
        new Thread(sleeper).start();
    }
    private double evaluatePosition(Piece[][] board){
        int[] scores = getScores(board);
        int[] pieceCount = countAll(board);
        double positionScoreForBlack = 0;

        if (pieceCount[0] < countAll(MainBoard)[0])
            return 9999;

        //if all pieces are gone then it's either a fantastic or horrible position
        if(pieceCount[0] == 0)
            return 9999;
        else if(pieceCount[1] == 0)
            return -9999;
        //if the score is higher than better position
        if(scores[0] > scores[1])
            positionScoreForBlack--;
        else if(scores[0] < scores[1])
            positionScoreForBlack++;

        return positionScoreForBlack;
    }

    //Used to calculate all possible moves for the bot
    private ArrayList<int[]> findAllBlackMoves(Piece[][] board){
        ArrayList<int[]> moves = new ArrayList<>();
        for(int x = 7 ; x > -1; x--){
            for(int y = 7; y > -1; y--){
                int[] temp = new int[4];
                if(board[x][y] != null && board[x][y].getColor().equals("Black")){
                    //regular pieces
                    if(!board[x][y].isKing()){
                        if((x+1 <= 7) && (y-1 >= 0) && canMove(board, x,y,x+1, y-1)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x+1;
                            temp[3] = y-1;
                            moves.add(temp);
                            temp = new int[4];
                        }
                        if((x+1 <= 7) && (y+1 <= 7) && canMove(board, x,y,x+1, y+1)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x+1;
                            temp[3] = y+1;
                            moves.add(temp);
                            temp = new int[4];
                        }
                        //Jumping
                        if((x+2 <= 7) && (y-2 >= 0) && canMove(board, x,y,x+2, y-2)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x+2;
                            temp[3] = y-2;
                            moves.add(temp);
                            temp = new int[4];
                        }
                        if((x+2 <= 7) && (y+2 <= 7) && canMove(board, x,y,x+2, y+2)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x+2;
                            temp[3] = y+2;
                            moves.add(temp);
                            temp = new int[4];
                        }
                    }
                    //kings
                    else {
                        if((x+1 <= 7) && (y-1 >= 0) && canMove(board, x,y,x+1, y-1)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x+1;
                            temp[3] = y-1;
                            moves.add(temp);
                            temp = new int[4];
                        }
                        if((x+1 <= 7) && (y+1 <= 7) && canMove(board, x,y,x+1, y+1)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x+1;
                            temp[3] = y+1;
                            moves.add(temp);
                            temp = new int[4];
                        }
                        if((x-1 >= 0) && (y-1 >= 0) && canMove(board, x,y,x-1, y-1)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x-1;
                            temp[3] = y-1;
                            moves.add(temp);
                            temp = new int[4];
                        }
                        if((x-1 >= 0) && (y+1 <= 7) && canMove(board, x,y,x-1, y+1)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x-1;
                            temp[3] = y+1;
                            moves.add(temp);
                            temp = new int[4];
                        }
                        //jumping
                        if((x+2 <= 7) && (y-2 >= 0) && canMove(board, x,y,x+2, y-2)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x+2;
                            temp[3] = y-2;
                            moves.add(temp);
                            temp = new int[4];
                        }
                        if((x+2 <= 7) && (y+2 <= 7) && canMove(board, x,y,x+2, y+2)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x+2;
                            temp[3] = y+2;
                            moves.add(temp);
                            temp = new int[4];
                        }
                        if((x-2 >= 0) && (y-2 >= 0) && canMove(board, x,y,x-2, y-2)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x-2;
                            temp[3] = y-2;
                            moves.add(temp);
                            temp = new int[4];
                        }
                        if((x-2 >= 0) && (y+2 <= 7) && canMove(board, x,y,x-2, y+2)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x-2;
                            temp[3] = y+2;
                            moves.add(temp);
                        }
                    }
                }
            }
        }
        return moves;
    }
    private ArrayList<int[]> findAllRedMoves(Piece[][] board){
        ArrayList<int[]> moves = new ArrayList<>();
        int[] temp = new int[4];
        for(int x = 7 ; x > -1; x--){
            for(int y = 7; y > -1; y--){
                if(board[x][y] != null && board[x][y].getColor().equals("Red")){
                    if(!board[x][y].isKing()){
                        if((x-1 >= 0) && (y-1 >= 0) && canMove(board, x,y,x-1, y-1)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x-1;
                            temp[3] = y-1;
                            moves.add(temp);
                        }
                        if((x-1 >= 0) && (y+1 <= 7) && canMove(board, x,y,x-1, y+1)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x-1;
                            temp[3] = y+1;
                            moves.add(temp);
                        }
                        //Jumping
                        if((x-2 >= 0) && (y-2 >= 0) && canMove(board, x,y,x-2, y-2)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x-2;
                            temp[3] = y-2;
                            moves.add(temp);
                        }
                        if((x-2 >= 7) && (y+2 <= 7) && canMove(board, x,y,x-2, y+2)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x-2;
                            temp[3] = y+2;
                            moves.add(temp);
                        }
                    }
                    else {
                        if((x+1 <= 7) && (y-1 >= 0) && canMove(board, x,y,x+1, y-1)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x+1;
                            temp[3] = y-1;
                            moves.add(temp);
                        }
                        if((x+1 <= 7) && (y+1 <= 7) && canMove(board, x,y,x+1, y+1)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x+1;
                            temp[3] = y+1;
                            moves.add(temp);
                        }
                        if((x-1 >= 0) && (y-1 >= 0) && canMove(board, x,y,x-1, y-1)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x-1;
                            temp[3] = y-1;
                            moves.add(temp);
                        }
                        if((x-1 >= 0) && (y+1 <= 7) && canMove(board, x,y,x-1, y+1)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x-1;
                            temp[3] = y+1;
                            moves.add(temp);
                        }
                        //jumping
                        if((x+2 <= 7) && (y-2 >= 0) && canMove(board, x,y,x+2, y-2)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x+2;
                            temp[3] = y-2;
                            moves.add(temp);
                        }
                        if((x+2 <= 7) && (y+2 <= 7) && canMove(board, x,y,x+2, y+2)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x+2;
                            temp[3] = y+2;
                            moves.add(temp);
                        }
                        if((x-2 >= 0) && (y-2 >= 0) && canMove(board, x,y,x-2, y-2)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x-2;
                            temp[3] = y-2;
                            moves.add(temp);
                        }
                        if((x-2 >= 0) && (y+2 <= 7) && canMove(board, x,y,x-2, y+2)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x-2;
                            temp[3] = y+2;
                            moves.add(temp);
                        }
                    }
                }
            }
        }
        return moves;
    }

    //some helper methods for evaluatePosition, red then black to avoid code duplication
    private int[] countAll(Piece[][] board){
        int red = 0;
        int black = 0;
        for(int x = 0; x < 8; x++){
            for(int y = 0; y < 8; y++){
                if(board[x][y] != null && board[x][y].getColor().equals("Red"))
                    red++;
                if(board[x][y] != null && board[x][y].getColor().equals("Black"))
                    black++;
            }
        }
        return new int[]{red, black};
    }
    private int[] getScores(Piece[][] board){
        int red = 0;
        int black = 0;
        for(int x = 0; x < 8; x++){
            for(int y = 0; y < 8; y++){
                if(board[x][y] != null && board[x][y].getColor().equals("Red")) {
                    if(board[x][y].isKing())
                        red += 5;
                    else
                        red += 3;
                }
                if(board[x][y] != null && board[x][y].getColor().equals("Black")) {
                    if (board[x][y].isKing())
                        black += 5;
                    else
                        black += 3;
                }
            }
        }
        return new int[] {red, black};
    }
    private void copyMain(Piece[][] tempBoard){
        for(int x = 0; x < 8; x++)
            System.arraycopy(MainBoard[x], 0, tempBoard[x], 0, 8);
    }

    private void checkWinner(){
        ArrayList<int[]> blackMoves = findAllBlackMoves(MainBoard);
        ArrayList<int[]> redMoves = findAllRedMoves(MainBoard);
        int[] scores = getScores(MainBoard);
        if(scores[0] == 0 || !blackMoves.isEmpty() && redMoves.isEmpty()){
            winner("B");
        }
        else if (scores[1] == 0 || blackMoves.isEmpty() && !redMoves.isEmpty()){
            winner("R");
        }
        else if(blackMoves.isEmpty() && redMoves.isEmpty()){
            winner("T");
        }
    }
    private void winner(String s){
        popUp.setVisible(true);
        lockmove = true;
        if(s.equals("T")){
            finalText.setText("Tie!");
        }
        else if (s.equals("B")){
            finalText.setText("You Lose!");
        }
    }
    private void move(int row, int column){
        if(!lockmove) {
            if (rowInput1 == -1 && colInput1 == -1) {
                rowInput1 = row;
                colInput1 = column;
                if (MainBoard[rowInput1][colInput1] == null) {
                    rowInput1 = -1;
                    colInput1 = -1;
                }
            } else {
                rowInput2 = row;
                colInput2 = column;
                boolean moved = false;

                if (rowInput1 != rowInput2 && colInput1 != colInput2 && MainBoard[rowInput1][colInput1].getColor().equals("Red")) {
                    if (canMove(MainBoard, rowInput1, colInput1, rowInput2, colInput2)) {
                        movePiece(MainBoard, rowInput1, colInput1, rowInput2, colInput2);
                        moved=true;
                        //Promoting
                        if (MainBoard[rowInput2][colInput2].getColor().equals("Red")) {
                            if (rowInput2 == 0)
                                MainBoard[rowInput2][colInput2].upgradeToKing();
                        } else {
                            if (rowInput2 == 7)
                                MainBoard[rowInput2][colInput2].upgradeToKing();
                        }
                    }

                    displayBoard();
                    if (((rowInput1 + 2 == rowInput2 && colInput1 + 2 == colInput2) || (rowInput1 + 2 == rowInput2 && colInput1 - 2 == colInput2) || (rowInput1 - 2 == rowInput2 && colInput1 + 2 == colInput2) || (rowInput1 - 2 == rowInput2 && colInput1 - 2 == colInput2))) {
                        if (!canCapture(MainBoard, rowInput2, colInput2) && moved) {
                            crappyAi();
                        }
                    } else if (moved){
                        crappyAi();
                    }
                    rowInput1 = -1;
                    colInput1 = -1;
                    rowInput2 = -1;
                    colInput2 = -1;
                } else {
                    rowInput1 = -1;
                    colInput1 = -1;
                    rowInput2 = -1;
                    colInput2 = -1;
                }
            }
        }
        checkWinner();
    }
    public void mouseClick(MouseEvent e){
        int row = (int) e.getSceneY()/75;
        int col = (int) e.getSceneX()/75;
        move(row, col);
    }
    private boolean canMove(Piece[][] board, int rowInput1, int colInput1, int rowInput2, int colInput2){
        //bounds detection
        if((rowInput1 < 0 || colInput1 > 7) || (rowInput2 < 0 ||colInput2 > 7))
            return false;
        //protecting from nulls
        else if(board[rowInput1][colInput1] == null)
            return false;
        else if(board[rowInput2][colInput2] != null)
            return false;
        //logic for king
        else if(board[rowInput1][colInput1].isKing()){
            if (rowInput1 - 1 == rowInput2 && colInput1 + 1 == colInput2 && (board[rowInput1-1][colInput1+1] == null)) {
                return true;
            }
            else if(rowInput1 - 1 == rowInput2 && colInput1 - 1 == colInput2 && (board[rowInput1-1][colInput1-1] == null)){
                return true;
            }
            else if(rowInput1 + 1 == rowInput2 && colInput1 - 1 == colInput2 && (board[rowInput1+1][colInput1-1] == null)) {
                return true;
            }
            else if(rowInput1 + 1 == rowInput2 && colInput1 + 1 == colInput2 && (board[rowInput1+1][colInput1+1] == null)){
                return true;
            }
            else{
                //Jumping Logic
                if(rowInput1 - 2 == rowInput2 && colInput1 + 2 == colInput2){
                    //Checks it isn't null and checks they aren't the same color
                    return board[rowInput1 - 1][colInput1 + 1] != null && !board[rowInput1 - 1][colInput1 + 1].getColor().equals(board[rowInput1][colInput1].getColor());
                }
                else if(rowInput1 - 2 == rowInput2 && colInput1 - 2 == colInput2) {
                    return board[rowInput1 - 1][colInput1 - 1] != null && !board[rowInput1 - 1][colInput1 - 1].getColor().equals(board[rowInput1][colInput1].getColor());
                }
                else if(rowInput1 + 2 == rowInput2 && colInput1 + 2 == colInput2){
                    return board[rowInput1 + 1][colInput1 + 1] != null && !board[rowInput1 + 1][colInput1 + 1].getColor().equals(board[rowInput1][colInput1].getColor());
                }
                else if(rowInput1 + 2 == rowInput2 && colInput1 - 2 == colInput2) {
                    return board[rowInput1 + 1][colInput1 - 1] != null && !board[rowInput1 + 1][colInput1 - 1].getColor().equals(board[rowInput1][colInput1].getColor());
                }
            }
        }
        //logic for Red
        else if(board[rowInput1][colInput1].getColor().equals("Red")){
            //Checking to move diagonally forward one space
            if (rowInput1 - 1 == rowInput2 && colInput1 - 1 == colInput2 && (board[rowInput1-1][colInput1-1] == null)) {
                return true;
            }
            else if(rowInput1 - 1 == rowInput2 && colInput1 + 1 == colInput2 && (board[rowInput1-1][colInput1+1] == null)){
                return true;
            }
            //Jumping Logic
            else if(rowInput1 - 2 == rowInput2 && colInput1 + 2 == colInput2){
                return board[rowInput1 - 1][colInput1 + 1] != null && board[rowInput1 - 1][colInput1 + 1].getColor().equals("Black");
            }
            else if(rowInput1 - 2 == rowInput2 && colInput1 - 2 == colInput2) {
                return board[rowInput1 - 1][colInput1 - 1] != null && board[rowInput1 - 1][colInput1 - 1].getColor().equals("Black");
            }
        }
        //logic for Black
        else{
            //Checking to move diagonally forward one space
            if (rowInput1 + 1 == rowInput2 && colInput1 - 1 == colInput2 && (board[rowInput1+1][colInput1-1] == null)) {
                return true;
            }
            else if(rowInput1 + 1 == rowInput2 && colInput1 + 1 == colInput2 && (board[rowInput1+1][colInput1+1] == null)){
                return true;
            }
            //Jumping Logic
            else if(rowInput1 + 2 == rowInput2 && colInput1 + 2 == colInput2){
                return board[rowInput1 + 1][colInput1 + 1] != null && board[rowInput1 + 1][colInput1 + 1].getColor().equals("Red");
            }
            else if(rowInput1 + 2 == rowInput2 && colInput1 - 2 == colInput2) {
                return board[rowInput1 + 1][colInput1 - 1] != null && board[rowInput1 + 1][colInput1 - 1].getColor().equals("Red");
            }
        }
        return false;

    }
    private void movePiece(Piece[][] board, int row, int column, int newRow, int newCol){
        if(row - 2 == newRow && column + 2 == newCol){
           board[row - 1][column + 1] = null;
        }
        else if(row - 2 == newRow && column - 2 == newCol) {
            board[row - 1][column - 1] = null;
        }
        else if(row + 2 == newRow && column + 2 == newCol){
            board[row + 1][column + 1] = null;
        }
        else if(row + 2 == newRow && column - 2 == newCol) {
            board[row + 1][column - 1] = null;
        }
        Piece temp = board[row][column];
        board[row][column] = null;
        board[newRow][newCol] = temp;
        displayBoard();
    }
    public boolean canCapture(Piece[][] board, int row1, int col1){
        if(row1+2 < 8 && col1+2 < 8 && canMove(board, row1, col1, row1+2, col1+2))
            return true;
        else if(row1+2 < 8 && col1-2 >= 0 && canMove(board, row1, col1, row1+2, col1-2))
            return true;
        else if(row1-2 >= 0 && col1+2 < 8 && canMove(board, row1, col1, row1-2, col1+2))
            return true;
        else if(row1-2 >= 0 && col1-2 >=0 && canMove(board, row1, col1, row1-2, col1-2))
            return true;
        return false;
    }

    private void displayBoard(){
        int numRedMoved = 1;
        int numBlackMoved = 1;
        hide("Red");
        hide("Black");
        for(int row = 0; row < 8; row++){
            for(int col = 0; col < 8; col++){
                if(MainBoard[row][col] != null && MainBoard[row][col].getColor().equals("Red")) {
                    if(numRedMoved == 1) {
                        Red1.setCenterX((col * 75) + 37.5);
                        Red1.setCenterY((row * 75) + 37.5);
                        Red1.setVisible(true);
                        if(MainBoard[row][col].isKing()){
                            king1.setX(col*75) ;
                            king1.setY(row*75);
                            king1.setVisible(true);
                        }
                    }
                    else if(numRedMoved == 2) {
                        Red2.setCenterX((col * 75) + 37.5);
                        Red2.setCenterY((row * 75) + 37.5);
                        Red2.setVisible(true);
                        if(MainBoard[row][col].isKing()){
                            king2.setX(col*75) ;
                            king2.setY(row*75);
                            king2.setVisible(true);
                        }
                    }
                    else if(numRedMoved == 3) {
                        Red3.setCenterX((col * 75) + 37.5);
                        Red3.setCenterY((row * 75) + 37.5);
                        Red3.setVisible(true);
                        if(MainBoard[row][col].isKing()){
                            king3.setX(col*75) ;
                            king3.setY(row*75);
                            king3.setVisible(true);
                        }
                    }
                    else if(numRedMoved == 4) {
                        Red4.setCenterX((col * 75) + 37.5);
                        Red4.setCenterY((row * 75) + 37.5);
                        Red4.setVisible(true);
                        if(MainBoard[row][col].isKing()){
                            king4.setX(col*75) ;
                            king4.setY(row*75);
                            king4.setVisible(true);
                        }
                    }
                    else if(numRedMoved == 5) {
                        Red5.setCenterX((col * 75) + 37.5);
                        Red5.setCenterY((row * 75) + 37.5);
                        Red5.setVisible(true);
                        if(MainBoard[row][col].isKing()){
                            king5.setX(col*75) ;
                            king5.setY(row*75);
                            king5.setVisible(true);
                        }
                    }
                    else if(numRedMoved == 6) {
                        Red6.setCenterX((col * 75) + 37.5);
                        Red6.setCenterY((row * 75) + 37.5);
                        Red6.setVisible(true);
                        if(MainBoard[row][col].isKing()){
                            king6.setX(col*75) ;
                            king6.setY(row*75);
                            king6.setVisible(true);
                        }
                    }
                    else if(numRedMoved == 7) {
                        Red7.setCenterX((col * 75) + 37.5);
                        Red7.setCenterY((row * 75) + 37.5);
                        Red7.setVisible(true);
                        if(MainBoard[row][col].isKing()){
                            king7.setX(col*75) ;
                            king7.setY(row*75);
                            king7.setVisible(true);
                        }
                    }
                    else if(numRedMoved == 8) {
                        Red8.setCenterX((col * 75) + 37.5);
                        Red8.setCenterY((row * 75) + 37.5);
                        Red8.setVisible(true);
                        if(MainBoard[row][col].isKing()){
                            king8.setX(col*75) ;
                            king8.setY(row*75);
                            king8.setVisible(true);
                        }
                    }
                    else if(numRedMoved == 9) {
                        Red9.setCenterX((col * 75) + 37.5);
                        Red9.setCenterY((row * 75) + 37.5);
                        Red9.setVisible(true);
                        if(MainBoard[row][col].isKing()){
                            king9.setX(col*75) ;
                            king9.setY(row*75);
                            king9.setVisible(true);
                        }
                    }
                    else if(numRedMoved == 10) {
                        Red10.setCenterX((col * 75) + 37.5);
                        Red10.setCenterY((row * 75) + 37.5);
                        Red10.setVisible(true);
                        if(MainBoard[row][col].isKing()){
                            king10.setX(col*75) ;
                            king10.setY(row*75);
                            king10.setVisible(true);
                        }
                    }
                    else if(numRedMoved == 11) {
                        Red11.setCenterX((col * 75) + 37.5);
                        Red11.setCenterY((row * 75) + 37.5);
                        Red11.setVisible(true);
                        if(MainBoard[row][col].isKing()){
                            king11.setX(col*75) ;
                            king11.setY(row*75);
                            king11.setVisible(true);
                        }
                    }
                    else if(numRedMoved == 12) {
                        Red12.setCenterX((col * 75) + 37.5);
                        Red12.setCenterY((row * 75) + 37.5);
                        Red12.setVisible(true);
                        if(MainBoard[row][col].isKing()){
                            king12.setX(col*75) ;
                            king12.setY(row*75);
                            king12.setVisible(true);
                        }
                    }
                    numRedMoved++;
                }
                if(MainBoard[row][col] != null && MainBoard[row][col].getColor().equals("Black")) {
                    if(numBlackMoved == 1) {
                        Black1.setCenterX((col * 75) + 37.5);
                        Black1.setCenterY((row * 75) + 37.5);
                        Black1.setVisible(true);
                        if(MainBoard[row][col].isKing()){
                            king13.setX(col*75) ;
                            king13.setY(row*75);
                            king13.setVisible(true);
                        }
                    }
                    else if(numBlackMoved == 2) {
                        Black2.setCenterX((col * 75) + 37.5);
                        Black2.setCenterY((row * 75) + 37.5);
                        Black2.setVisible(true);
                        if(MainBoard[row][col].isKing()){
                            king14.setX(col*75) ;
                            king14.setY(row*75);
                            king14.setVisible(true);
                        }
                    }
                    else if(numBlackMoved == 3) {
                        Black3.setCenterX((col * 75) + 37.5);
                        Black3.setCenterY((row * 75) + 37.5);
                        Black3.setVisible(true);
                        if(MainBoard[row][col].isKing()){
                            king15.setX(col*75) ;
                            king15.setY(row*75);
                            king15.setVisible(true);
                        }
                    }
                    else if(numBlackMoved == 4) {
                        Black4.setCenterX((col * 75) + 37.5);
                        Black4.setCenterY((row * 75) + 37.5);
                        Black4.setVisible(true);
                        if(MainBoard[row][col].isKing()){
                            king16.setX(col*75) ;
                            king16.setY(row*75);
                            king16.setVisible(true);
                        }
                    }
                    else if(numBlackMoved == 5) {
                        Black5.setCenterX((col * 75) + 37.5);
                        Black5.setCenterY((row * 75) + 37.5);
                        Black5.setVisible(true);
                        if(MainBoard[row][col].isKing()){
                            king17.setX(col*75) ;
                            king17.setY(row*75);
                            king17.setVisible(true);
                        }
                    }
                    else if(numBlackMoved == 6) {
                        Black6.setCenterX((col * 75) + 37.5);
                        Black6.setCenterY((row * 75) + 37.5);
                        Black6.setVisible(true);
                        if(MainBoard[row][col].isKing()){
                            king18.setX(col*75);
                            king18.setY(row*75);
                            king18.setVisible(true);
                        }
                    }
                    else if(numBlackMoved == 7) {
                        Black7.setCenterX((col * 75) + 37.5);
                        Black7.setCenterY((row * 75) + 37.5);
                        Black7.setVisible(true);
                        if(MainBoard[row][col].isKing()){
                            king19.setX(col*75) ;
                            king19.setY(row*75);
                            king19.setVisible(true);
                        }
                    }
                    else if(numBlackMoved == 8) {
                        Black8.setCenterX((col * 75) + 37.5);
                        Black8.setCenterY((row * 75) + 37.5);
                        Black8.setVisible(true);
                        if(MainBoard[row][col].isKing()){
                            king20.setX(col*75) ;
                            king20.setY(row*75);
                            king20.setVisible(true);
                        }
                    }
                    else if(numBlackMoved == 9) {
                        Black9.setCenterX((col * 75) + 37.5);
                        Black9.setCenterY((row * 75) + 37.5);
                        Black9.setVisible(true);
                        if(MainBoard[row][col].isKing()){
                            king21.setX(col*75) ;
                            king21.setY(row*75);
                            king21.setVisible(true);
                        }
                    }
                    else if(numBlackMoved == 10) {
                        Black10.setCenterX((col * 75) + 37.5);
                        Black10.setCenterY((row * 75) + 37.5);
                        Black10.setVisible(true);
                        if(MainBoard[row][col].isKing()){
                            king22.setX(col*75) ;
                            king22.setY(row*75);
                            king22.setVisible(true);
                        }
                    }
                    else if(numBlackMoved == 11) {
                        Black11.setCenterX((col * 75) + 37.5);
                        Black11.setCenterY((row * 75) + 37.5);
                        Black11.setVisible(true);
                        if(MainBoard[row][col].isKing()){
                            king23.setX(col*75) ;
                            king23.setY(row*75);
                            king23.setVisible(true);
                        }
                    }
                    else if(numBlackMoved == 12) {
                        Black12.setCenterX((col * 75) + 37.5);
                        Black12.setCenterY((row * 75) + 37.5);
                        Black12.setVisible(true);
                        if(MainBoard[row][col].isKing()){
                            king24.setX(col*75) ;
                            king24.setY(row*75);
                            king24.setVisible(true);
                        }
                    }
                    numBlackMoved++;
                }
            }
        }
    }
    private void setStarterBoard() {
        MainBoard[7][0] = new Piece("Red");
        MainBoard[7][2] = new Piece("Red");
        MainBoard[7][4] = new Piece("Red");
        MainBoard[7][6] = new Piece("Red");
        MainBoard[6][1] = new Piece("Red");
        MainBoard[6][3] = new Piece("Red");
        MainBoard[6][5] = new Piece("Red");
        MainBoard[6][7] = new Piece("Red");
        MainBoard[5][0] = new Piece("Red");
        MainBoard[5][2] = new Piece("Red");
        MainBoard[5][4] = new Piece("Red");
        MainBoard[5][6] = new Piece("Red");

        MainBoard[0][1] = new Piece("Black");
        MainBoard[0][3] = new Piece("Black");
        MainBoard[0][5] = new Piece("Black");
        MainBoard[0][7] = new Piece("Black");
        MainBoard[1][0] = new Piece("Black");
        MainBoard[1][2] = new Piece("Black");
        MainBoard[1][4] = new Piece("Black");
        MainBoard[1][6] = new Piece("Black");
        MainBoard[2][1] = new Piece("Black");
        MainBoard[2][3] = new Piece("Black");
        MainBoard[2][5] = new Piece("Black");
        MainBoard[2][7] = new Piece("Black");
    }
    private void hide(String s){
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

            king1.setVisible(false);
            king2.setVisible(false);
            king3.setVisible(false);
            king4.setVisible(false);
            king5.setVisible(false);
            king6.setVisible(false);
            king7.setVisible(false);
            king8.setVisible(false);
            king9.setVisible(false);
            king10.setVisible(false);
            king11.setVisible(false);
            king12.setVisible(false);
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

            king13.setVisible(false);
            king14.setVisible(false);
            king15.setVisible(false);
            king16.setVisible(false);
            king17.setVisible(false);
            king18.setVisible(false);
            king19.setVisible(false);
            king20.setVisible(false);
            king21.setVisible(false);
            king22.setVisible(false);
            king23.setVisible(false);
            king24.setVisible(false);
        }
    }
}