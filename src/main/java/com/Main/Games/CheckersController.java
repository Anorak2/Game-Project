package com.Main.Games;

import com.Main.MenuController;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CheckersController extends MenuController {
    private static class Piece {
        private final boolean isBlack;
        private boolean isKing = false;

        public Piece(Boolean isBlack) {
            this.isBlack = isBlack;
        }


        public boolean isKing() {
            return isKing;
        }

        public void upgradeToKing() {
            isKing = true;
        }
    }

    private class Algorithm implements Runnable {
        private final Piece[][] boardT = new Piece[8][8];
        private final boolean isBlackT;
        private final int num;
        public Algorithm(Piece[][] board, boolean isBlack, int num) {
            int tempLoopStore = board.length;
            for (int x = 0; x < tempLoopStore; x ++)
                System.arraycopy(board[x], 0, boardT[x], 0, board[x].length);
            this.isBlackT = isBlack;
            this.num = num;
        }

        @Override
        public void run() {
            int maxDepth;
            int[] count = countAll(boardT);
            if(count[0] + count[1] >= 15)
                maxDepth = 7;
            else if (count[0] + count[1] >= 10)
                maxDepth = 8;
            else
                maxDepth = 9;

            eval[num] = findBestMove(boardT, isBlackT, 0, maxDepth);
        }

        //The recursive backtracking methods
        //The bane of my existence
        private synchronized double findBestMove(Piece[][] board, boolean isBlack, int depth, int maxDepth){
            Piece[][] tempBoard = new Piece[8][8];
            int index;


            for (int z = 0; z < 8; z++) {
                System.arraycopy(board[z], 0, tempBoard[z], 0, board.length);
            }
            ArrayList<Double> scores;
            if(depth == maxDepth)
                return evaluatePosition(board);


            double check = evaluatePosition(tempBoard);
            if(check == 999999 || check == -999999)
                return check;


            if (isBlack) {
                List<int[]> allMovesBlack = findAllBlackMoves(board);
                scores = new ArrayList<>(allMovesBlack.size());
                for (int[] ints : allMovesBlack) {
                    movePiece(tempBoard, ints[0], ints[1], ints[2], ints[3]);
                    //MultiCapture
                    //Checks if the last move was a capture and if we can capture again
                    if (isCapture(ints[0], ints[1], ints[2], ints[3]) && canCapture(tempBoard, ints[2], ints[3])) {
                        scores.add(findBestMove(tempBoard, true, depth+1, maxDepth));
                        //scores.add(findBestMoveMultiCapture(tempBoard, true, depth));
                    }else{
                        scores.add(findBestMove(tempBoard, false, depth+1, maxDepth));
                    }
                    for (int z = 0; z < 8; z++) {
                        System.arraycopy(board[z], 0, tempBoard[z], 0, 8);
                    }
                }
            }
            else {
                List<int[]> allMovesRed = findAllRedMoves(tempBoard);
                scores = new ArrayList<>(allMovesRed.size());

                for (int[] ints : allMovesRed) {
                    movePiece(tempBoard, ints[0], ints[1], ints[2], ints[3]);
                    //MultiCapture
                    //Checks if the last move was a capture and if we can capture again
                    if (isCapture(ints[0], ints[1], ints[2], ints[3]) && canCapture(tempBoard, ints[2], ints[3])) {
                        //scores.add(findBestMoveMultiCapture(tempBoard, false, depth));
                        scores.add(findBestMove(tempBoard, false, depth+1, maxDepth));
                    }
                    else{
                        scores.add(findBestMove(tempBoard, true, depth + 1, maxDepth));
                    }
                    tempBoard = new Piece[8][8];
                    for (int z = 0; z < 8; z++) {
                        System.arraycopy(board[z], 0, tempBoard[z], 0, board.length);
                    }
                }

                //Returning lowest rated move
            }
            int highest = 0, lowest = 0;
            int loopStorage = scores.size();
            for (int x = 1; x < loopStorage; x++) {
                if (scores.get(x) > scores.get(highest)) {
                    highest = x;
                }
                else if (scores.get(x) < scores.get(lowest)) {
                    lowest = x;
                }
            }
            if(isBlack)
                index = highest;
            else
                index = lowest;
            return scores.get(index);
        }


        //Evaluates if the position is good for the bot or not
        private double evaluatePosition(Piece[][] board) {
            // How the algo works:
            //If pieces are all gone it is either very good or very bad position
            //Score is next important, kings = 5 plebeians = 3
            //Then Distance to king a piece
            //Finally there is a slightly random component

            //Red then black
            int[] scores = getScores(board);
            int[] pieceCount = countAll(board);
            double positionScoreForBlack = 0;


            //if all pieces are gone then it's either a fantastic or horrible position
            if (pieceCount[0] == 0)
                return 999999;
            else if (pieceCount[1] == 0)
                return -999999;
            else if (findAllBlackMoves(board).size() == 0)
                return -999999;
            else if (findAllRedMoves(board).size() == 0)
                return 999999;

            // difference in score x10 to emphasize importance of score
            positionScoreForBlack += ((scores[1] - scores[0]) * 4.5);

            //positionScoreForBlack -= pieceCount[0] * .5;

            //This takes into account the distance to becoming royalty
            /*
            int blackDistance = 0, redDistance = 0;
            for (int x = 0; x < 8; x++) {
                for (int y = 0; y < 8; y++) {
                    if (board[x][y] != null && !board[x][y].isKing) {
                        if (board[x][y].isBlack)
                            blackDistance += x;
                        else
                            redDistance += (7 - x);
                    }
                }
            }

            if (blackDistance > redDistance)
                positionScoreForBlack += 1.5;
            else if (blackDistance < redDistance)
                positionScoreForBlack -= 1.5;
            */

            // Random element
            positionScoreForBlack += (Math.random() * 1) - .5;

            return positionScoreForBlack;
        }
        private double simpleEvaluate(Piece[][] board, int amountOfMoves){
            int[] scores = getScores(board);
            int[] pieceCount = countAll(board);


            //if all pieces are gone then it's either a fantastic or horrible position
            if (pieceCount[0] == 0)
                return 999999;
            else if (pieceCount[1] == 0)
                return -999999;
            else if (amountOfMoves == 0)
                return -999999;

            return ((scores[1] - scores[0]) * 10);
            //return 4;
        }

        //Finding moves, duh
        private List<int[]> findAllBlackMoves(Piece[][] board) {
            List<int[]> moves = new LinkedList<>();
            for (int x = 7; x > -1; x--) {
                for (int y = 7; y > -1; y--) {
                    int[] temp = new int[4];
                    if (board[x][y] != null && board[x][y].isBlack) {
                        //regular pieces
                        if (!board[x][y].isKing()) {
                            if ((x + 1 <= 7) && (y - 1 >= 0) && isMoveLegal(board, x, y, x + 1, y - 1)) {
                                temp[0] = x;
                                temp[1] = y;
                                temp[2] = x + 1;
                                temp[3] = y - 1;
                                moves.add(temp);
                                temp = new int[4];
                            }
                            if ((x + 1 <= 7) && (y + 1 <= 7) && isMoveLegal(board, x, y, x + 1, y + 1)) {
                                temp[0] = x;
                                temp[1] = y;
                                temp[2] = x + 1;
                                temp[3] = y + 1;
                                moves.add(temp);
                                temp = new int[4];
                            }
                        }
                        //kings
                        else {
                            if ((x + 1 <= 7) && (y - 1 >= 0) && isMoveLegal(board, x, y, x + 1, y - 1)) {
                                temp[0] = x;
                                temp[1] = y;
                                temp[2] = x + 1;
                                temp[3] = y - 1;
                                moves.add(temp);
                                temp = new int[4];
                            }
                            if ((x + 1 <= 7) && (y + 1 <= 7) && isMoveLegal(board, x, y, x + 1, y + 1)) {
                                temp[0] = x;
                                temp[1] = y;
                                temp[2] = x + 1;
                                temp[3] = y + 1;
                                moves.add(temp);
                                temp = new int[4];
                            }
                            if ((x - 1 >= 0) && (y - 1 >= 0) && isMoveLegal(board, x, y, x - 1, y - 1)) {
                                temp[0] = x;
                                temp[1] = y;
                                temp[2] = x - 1;
                                temp[3] = y - 1;
                                moves.add(temp);
                                temp = new int[4];
                            }
                            if ((x - 1 >= 0) && (y + 1 <= 7) && isMoveLegal(board, x, y, x - 1, y + 1)) {
                                temp[0] = x;
                                temp[1] = y;
                                temp[2] = x - 1;
                                temp[3] = y + 1;
                                moves.add(temp);
                                temp = new int[4];
                            }
                            //jumping
                            if ((x - 2 >= 0) && (y - 2 >= 0) && isMoveLegal(board, x, y, x - 2, y - 2)) {
                                temp[0] = x;
                                temp[1] = y;
                                temp[2] = x - 2;
                                temp[3] = y - 2;
                                moves.add(temp);
                                temp = new int[4];
                            }
                            if ((x - 2 >= 0) && (y + 2 <= 7) && isMoveLegal(board, x, y, x - 2, y + 2)) {
                                temp[0] = x;
                                temp[1] = y;
                                temp[2] = x - 2;
                                temp[3] = y + 2;
                                moves.add(temp);
                                temp = new int[4];
                            }
                        }

                        //Shared Jumping
                        if ((x + 2 <= 7) && (y - 2 >= 0) && isMoveLegal(board, x, y, x + 2, y - 2)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x + 2;
                            temp[3] = y - 2;
                            moves.add(temp);
                            temp = new int[4];
                        }
                        if ((x + 2 <= 7) && (y + 2 <= 7) && isMoveLegal(board, x, y, x + 2, y + 2)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x + 2;
                            temp[3] = y + 2;
                            moves.add(temp);
                        }
                    }
                }
            }
            return moves;
        }
        private List<int[]> findAllRedMoves(Piece[][] board) {
            List<int[]> moves = new LinkedList<>();
            int[] temp = new int[4];
            for (int x = 7; x > -1; x--) {
                for (int y = 7; y > -1; y--) {
                    if (board[x][y] != null && !board[x][y].isBlack) {
                        if (!board[x][y].isKing()) {
                            if ((x - 1 >= 0) && (y - 1 >= 0) && isMoveLegal(board, x, y, x - 1, y - 1)) {
                                temp[0] = x;
                                temp[1] = y;
                                temp[2] = x - 1;
                                temp[3] = y - 1;
                                moves.add(temp);
                                temp = new int[4];
                            }
                            if ((x - 1 >= 0) && (y + 1 <= 7) && isMoveLegal(board, x, y, x - 1, y + 1)) {
                                temp[0] = x;
                                temp[1] = y;
                                temp[2] = x - 1;
                                temp[3] = y + 1;
                                moves.add(temp);
                                temp = new int[4];
                            }
                        }
                        else {
                            if ((x + 1 <= 7) && (y - 1 >= 0) && isMoveLegal(board, x, y, x + 1, y - 1)) {
                                temp[0] = x;
                                temp[1] = y;
                                temp[2] = x + 1;
                                temp[3] = y - 1;
                                moves.add(temp);
                                temp = new int[4];
                            }
                            if ((x + 1 <= 7) && (y + 1 <= 7) && isMoveLegal(board, x, y, x + 1, y + 1)) {
                                temp[0] = x;
                                temp[1] = y;
                                temp[2] = x + 1;
                                temp[3] = y + 1;
                                moves.add(temp);
                                temp = new int[4];
                            }
                            if ((x - 1 >= 0) && (y - 1 >= 0) && isMoveLegal(board, x, y, x - 1, y - 1)) {
                                temp[0] = x;
                                temp[1] = y;
                                temp[2] = x - 1;
                                temp[3] = y - 1;
                                moves.add(temp);
                                temp = new int[4];
                            }
                            if ((x - 1 >= 0) && (y + 1 <= 7) && isMoveLegal(board, x, y, x - 1, y + 1)) {
                                temp[0] = x;
                                temp[1] = y;
                                temp[2] = x - 1;
                                temp[3] = y + 1;
                                moves.add(temp);
                                temp = new int[4];
                            }
                            //jumping
                            if ((x + 2 <= 7) && (y - 2 >= 0) && isMoveLegal(board, x, y, x + 2, y - 2)) {
                                temp[0] = x;
                                temp[1] = y;
                                temp[2] = x + 2;
                                temp[3] = y - 2;
                                moves.add(temp);
                                temp = new int[4];
                            }
                            if ((x + 2 <= 7) && (y + 2 <= 7) && isMoveLegal(board, x, y, x + 2, y + 2)) {
                                temp[0] = x;
                                temp[1] = y;
                                temp[2] = x + 2;
                                temp[3] = y + 2;
                                moves.add(temp);
                                temp = new int[4];
                            }
                        }
                        //Shared Jumping part
                        if ((x - 2 >= 0) && (y - 2 >= 0) && isMoveLegal(board, x, y, x - 2, y - 2)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x - 2;
                            temp[3] = y - 2;
                            moves.add(temp);
                            temp = new int[4];
                        }
                        if ((x - 2 >= 0) && (y + 2 <= 7) && isMoveLegal(board, x, y, x - 2, y + 2)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x - 2;
                            temp[3] = y + 2;
                            moves.add(temp);
                            temp = new int[4];
                        }
                    }
                }
            }
            return moves;
        }
        private int[] findCapture(Piece[][] tempBoard, boolean isBlack){
            int[] thing = new int[4];
            for(int row = 0; row < 8; row++) {
                for(int col = 0; col < 8; col++) {
                    if(tempBoard[row][col] != null) {
                        if (tempBoard[row][col].isKing()) {
                            //jumping
                            if ((row + 2 <= 7) && (col - 2 >= 0) && isMoveLegal(tempBoard, row, col, row + 2, col - 2)) {
                                thing[0] = row;
                                thing[1] = col;
                                thing[2] = row + 2;
                                thing[3] = col - 2;
                            }
                            if ((row + 2 <= 7) && (col + 2 <= 7) && isMoveLegal(tempBoard, row, col, row + 2, col + 2)) {
                                thing[0] = row;
                                thing[1] = col;
                                thing[2] = row + 2;
                                thing[3] = col + 2;
                            }
                            if ((row - 2 >= 0) && (col - 2 >= 0) && isMoveLegal(tempBoard, row, col, row - 2, col - 2)) {
                                thing[0] = row;
                                thing[1] = col;
                                thing[2] = row - 2;
                                thing[3] = col - 2;
                            }
                            if ((row - 2 >= 0) && (col + 2 <= 7) && isMoveLegal(tempBoard, row, col, row - 2, col + 2)) {
                                thing[0] = row;
                                thing[1] = col;
                                thing[2] = row - 2;
                                thing[3] = col + 2;
                            }
                        }
                        else if (isBlack) {
                            //Jumping
                            if ((row + 2 <= 7) && (col - 2 >= 0) && isMoveLegal(tempBoard, row, col, row + 2, col - 2)) {
                                thing[0] = row;
                                thing[1] = col;
                                thing[2] = row + 2;
                                thing[3] = col - 2;
                            }
                            if ((row + 2 <= 7) && (col + 2 <= 7) && isMoveLegal(tempBoard, row, col, row + 2, col + 2)) {
                                thing[0] = row;
                                thing[1] = col;
                                thing[2] = row + 2;
                                thing[3] = col + 2;
                            }
                        }
                        else {
                            //Jumping
                            if ((row - 2 >= 0) && (col - 2 >= 0) && isMoveLegal(tempBoard, row, col, row - 2, col - 2)) {
                                thing[0] = row;
                                thing[1] = col;
                                thing[2] = row - 2;
                                thing[3] = col - 2;
                            }
                            if ((row - 2 >= 0) && (col + 2 <= 7) && isMoveLegal(tempBoard, row, col, row - 2, col + 2)) {
                                thing[0] = row;
                                thing[1] = col;
                                thing[2] = row - 2;
                                thing[3] = col + 2;
                            }
                        }
                    }
                }
            }
            /*
            if(num == 3) {
                for (int a = 0; a < 8; a++) {
                    System.out.print(num);
                    for (int b = 0; b < 8; b++) {
                        if (tempBoard[a][b] != null) {
                            if (tempBoard[a][b].isBlack)
                                System.out.print("B ");
                            else
                                System.out.print("R ");
                        } else
                            System.out.print("_ ");
                    }
                    System.out.println();
                }
                System.out.println("\n \n");
                System.out.println(num + " row: " + row + ", col: " + col);
                System.out.println(isBlack);
            }

             */
            return thing;
        }
        public static boolean isCapture(int row1, int col1, int row2, int col2){
            //As the name implies, only meant for use in conjunction with findAllBlackMoves or findAllRedMoves
            return (row1+2==row2 || row1-2==row2) && (col1+2== col2 || col1-2==col2);
        }

        //some helper methods for evaluatePosition, red then black to avoid code duplication
        //list goes red then black
        private int @NotNull [] countAll(Piece[][] board) {
            int red = 0;
            int black = 0;
            for (int x = 0; x < 8; x++) {
                for (int y = 0; y < 8; y++) {
                    if (board[x][y] != null && !board[x][y].isBlack)
                        red++;
                    if (board[x][y] != null && board[x][y].isBlack)
                        black++;
                }
            }
            return new int[]{red, black};
        }

        private int[] getScores(Piece[][] board) {
            int red = 0;
            int black = 0;
            for (int x = 0; x < 8; x++) {
                for (int y = 0; y < 8; y++) {
                    if(board[x][y] == null)
                        continue;
                    if (!board[x][y].isBlack) {
                        if (board[x][y].isKing() || x == 0)
                            red += 5;
                        else
                            red += 3;
                    } else {
                        if (board[x][y].isKing() || x == 7)
                            black += 5;
                        else
                            black += 3;
                    }

                }
            }
            return new int[]{red, black};
        }
    }

    @FXML
    AnchorPane popUp, PieceAnchorPane;
    @FXML
    Text finalText;

    private double[] eval;

    private int rowInput1, colInput1;
    private Piece[][] MainBoard = new Piece[8][8];
    boolean lockMove = false;
    private final int[] done = new int[4];


    public void initialize() {
        rowInput1 = -1;
        colInput1 = -1;
        MainBoard = new Piece[8][8];
        lockMove = false;
        popUp.setVisible(false);
        setStarterBoard();
        displayBoard();
    }

    private int[] startAI(Piece[][] board) {
        List<int[]> allMovesBlack = findAllBlackMoves(MainBoard);
        Piece[][] tempBoard = new Piece[8][8];
        int[] finished = new int[4];
        int index;
        eval = new double[allMovesBlack.size()];

        try {
            ExecutorService es = Executors.newCachedThreadPool();
            int tempLoopStore = allMovesBlack.size();
            for (int x = 0; x < tempLoopStore; x++) {
                for (int z = 0; z < 8; z++)
                    System.arraycopy(board[z], 0, tempBoard[z], 0, board.length);

                movePiece(tempBoard, allMovesBlack.get(x)[0], allMovesBlack.get(x)[1], allMovesBlack.get(x)[2], allMovesBlack.get(x)[3]);
                Algorithm myThread;

                //myThread = new Algorithm(tempBoard, true, 1, x)

                // Checks if last move was a capture and if I can capture again/ for chain captures
                boolean wasLastMoveCap = Algorithm.isCapture(allMovesBlack.get(x)[0], allMovesBlack.get(x)[1], allMovesBlack.get(x)[2], allMovesBlack.get(x)[3]);
                if (wasLastMoveCap && canCapture(tempBoard, allMovesBlack.get(x)[2], allMovesBlack.get(x)[3]))
                    myThread = new Algorithm(tempBoard, true, x);
                else
                    myThread = new Algorithm(tempBoard, false, x);

                es.execute(myThread);
            }

            es.shutdown();
            boolean thing = es.awaitTermination(40, TimeUnit.MINUTES);
            if (!thing)
                throw new Exception();
        } catch (Exception e) {
            e.printStackTrace();
        }
        int highest = 0, lowest = 0;
        if (allMovesBlack.size() > 1) {
            for (int x = 0; x < eval.length; x++) {
                if (eval[x] >= eval[highest])
                    highest = x;
                else if (eval[x] < eval[lowest])
                    lowest = x;
            }
        }
        //if (highest == lowest)
        //    index = (int) (Math.random() * allMovesBlack.size());
        //else
        index = highest;

        if (allMovesBlack.size() > 0)
            System.arraycopy(allMovesBlack.get(index), 0, finished, 0, allMovesBlack.get(index).length);
        //System.out.println(Arrays.toString(eval));
        //System.out.println(eval[index]);
        return finished;
    }
    public synchronized void crappyAi() {
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
            long startTime = System.nanoTime();

            int[] x = startAI(MainBoard);
            System.arraycopy(x, 0, done, 0, x.length);

            System.out.println((System.nanoTime() - startTime) / 1000000);


            movePiece(MainBoard, done[0], done[1], done[2], done[3]);
            if ((done[1] + 2 == done[3] || done[1] - 2 == done[3]) && canCapture(MainBoard, done[2], done[3])) {
                crappyAi();
            }
            //Promotion
            if (done[2] == 7) {
                MainBoard[done[2]][done[3]].upgradeToKing();
                //Recursion for multi jump
                if ((done[0] + 2 == done[2] && done[1] + 2 == done[3]) ||
                        (done[0] + 2 == done[2] && done[1] - 2 == done[3]) ||
                        (done[0] - 2 == done[2] && done[1] + 2 == done[3]) ||
                        (done[0] - 2 == done[2] && done[1] - 2 == done[3])) {
                    if (canCapture(MainBoard, done[2], done[3])) {
                        crappyAi();
                    }
                }
            }
            displayBoard();
            checkWinner();
        });
        new Thread(sleeper).start();
    }
    private List<int[]> findAllBlackMoves(Piece[][] board) {
        List<int[]> moves = new LinkedList<>();
        for (int x = 7; x > -1; x--) {
            for (int y = 7; y > -1; y--) {
                int[] temp = new int[4];
                if (board[x][y] != null && board[x][y].isBlack) {
                    //regular pieces
                    if (!board[x][y].isKing()) {
                        if ((x + 1 <= 7) && (y - 1 >= 0) && isMoveLegal(board, x, y, x + 1, y - 1)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x + 1;
                            temp[3] = y - 1;
                            moves.add(temp);
                            temp = new int[4];
                        }
                        if ((x + 1 <= 7) && (y + 1 <= 7) && isMoveLegal(board, x, y, x + 1, y + 1)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x + 1;
                            temp[3] = y + 1;
                            moves.add(temp);
                            temp = new int[4];
                        }
                        //Jumping
                        if ((x + 2 <= 7) && (y - 2 >= 0) && isMoveLegal(board, x, y, x + 2, y - 2)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x + 2;
                            temp[3] = y - 2;
                            moves.add(temp);
                            temp = new int[4];
                        }
                        if ((x + 2 <= 7) && (y + 2 <= 7) && isMoveLegal(board, x, y, x + 2, y + 2)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x + 2;
                            temp[3] = y + 2;
                            moves.add(temp);
                        }
                    }
                    //kings
                    else {
                        if ((x + 1 <= 7) && (y - 1 >= 0) && isMoveLegal(board, x, y, x + 1, y - 1)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x + 1;
                            temp[3] = y - 1;
                            moves.add(temp);
                            temp = new int[4];
                        }
                        if ((x + 1 <= 7) && (y + 1 <= 7) && isMoveLegal(board, x, y, x + 1, y + 1)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x + 1;
                            temp[3] = y + 1;
                            moves.add(temp);
                            temp = new int[4];
                        }
                        if ((x - 1 >= 0) && (y - 1 >= 0) && isMoveLegal(board, x, y, x - 1, y - 1)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x - 1;
                            temp[3] = y - 1;
                            moves.add(temp);
                            temp = new int[4];
                        }
                        if ((x - 1 >= 0) && (y + 1 <= 7) && isMoveLegal(board, x, y, x - 1, y + 1)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x - 1;
                            temp[3] = y + 1;
                            moves.add(temp);
                            temp = new int[4];
                        }
                        //jumping
                        if ((x + 2 <= 7) && (y - 2 >= 0) && isMoveLegal(board, x, y, x + 2, y - 2)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x + 2;
                            temp[3] = y - 2;
                            moves.add(temp);
                            temp = new int[4];
                        }
                        if ((x + 2 <= 7) && (y + 2 <= 7) && isMoveLegal(board, x, y, x + 2, y + 2)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x + 2;
                            temp[3] = y + 2;
                            moves.add(temp);
                            temp = new int[4];
                        }
                        if ((x - 2 >= 0) && (y - 2 >= 0) && isMoveLegal(board, x, y, x - 2, y - 2)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x - 2;
                            temp[3] = y - 2;
                            moves.add(temp);
                            temp = new int[4];
                        }
                        if ((x - 2 >= 0) && (y + 2 <= 7) && isMoveLegal(board, x, y, x - 2, y + 2)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x - 2;
                            temp[3] = y + 2;
                            moves.add(temp);
                        }
                    }
                }
            }
        }
        return moves;
    }
    private List<int[]> findAllRedMoves(Piece[][] board) {
        List<int[]> moves = new LinkedList<>();
        for (int x = 7; x > -1; x--) {
            for (int y = 7; y > -1; y--) {
                int[] temp = new int[4];
                if (board[x][y] != null && !board[x][y].isBlack) {
                    if (!board[x][y].isKing()) {
                        if ((x - 1 >= 0) && (y - 1 >= 0) && isMoveLegal(board, x, y, x - 1, y - 1)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x - 1;
                            temp[3] = y - 1;
                            moves.add(temp);
                            temp = new int[4];
                        }
                        if ((x - 1 >= 0) && (y + 1 <= 7) && isMoveLegal(board, x, y, x - 1, y + 1)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x - 1;
                            temp[3] = y + 1;
                            moves.add(temp);
                            temp = new int[4];
                        }
                        //Jumping
                        if ((x - 2 >= 0) && (y - 2 >= 0) && isMoveLegal(board, x, y, x - 2, y - 2)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x - 2;
                            temp[3] = y - 2;
                            moves.add(temp);
                            temp = new int[4];
                        }
                        if ((x - 2 >= 7) && (y + 2 <= 7) && isMoveLegal(board, x, y, x - 2, y + 2)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x - 2;
                            temp[3] = y + 2;
                            moves.add(temp);
                        }
                    }
                    else {
                        if ((x + 1 <= 7) && (y - 1 >= 0) && isMoveLegal(board, x, y, x + 1, y - 1)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x + 1;
                            temp[3] = y - 1;
                            moves.add(temp);
                            temp = new int[4];
                        }
                        if ((x + 1 <= 7) && (y + 1 <= 7) && isMoveLegal(board, x, y, x + 1, y + 1)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x + 1;
                            temp[3] = y + 1;
                            moves.add(temp);
                            temp = new int[4];
                        }
                        if ((x - 1 >= 0) && (y - 1 >= 0) && isMoveLegal(board, x, y, x - 1, y - 1)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x - 1;
                            temp[3] = y - 1;
                            moves.add(temp);
                            temp = new int[4];
                        }
                        if ((x - 1 >= 0) && (y + 1 <= 7) && isMoveLegal(board, x, y, x - 1, y + 1)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x - 1;
                            temp[3] = y + 1;
                            moves.add(temp);
                            temp = new int[4];
                        }
                        //jumping
                        if ((x + 2 <= 7) && (y - 2 >= 0) && isMoveLegal(board, x, y, x + 2, y - 2)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x + 2;
                            temp[3] = y - 2;
                            moves.add(temp);
                            temp = new int[4];
                        }
                        if ((x + 2 <= 7) && (y + 2 <= 7) && isMoveLegal(board, x, y, x + 2, y + 2)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x + 2;
                            temp[3] = y + 2;
                            moves.add(temp);
                            temp = new int[4];
                        }
                        if ((x - 2 >= 0) && (y - 2 >= 0) && isMoveLegal(board, x, y, x - 2, y - 2)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x - 2;
                            temp[3] = y - 2;
                            moves.add(temp);
                            temp = new int[4];
                        }
                        if ((x - 2 >= 0) && (y + 2 <= 7) && isMoveLegal(board, x, y, x - 2, y + 2)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x - 2;
                            temp[3] = y + 2;
                            moves.add(temp);
                        }
                    }
                }
            }
        }
        return moves;
    }

    private int[] getScores(Piece[][] board) {
        int red = 0;
        int black = 0;
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                if (board[x][y] != null && !board[x][y].isBlack) {
                    if (board[x][y].isKing())
                        red += 5;
                    else
                        red += 3;
                }
                if (board[x][y] != null && board[x][y].isBlack) {
                    if (board[x][y].isKing())
                        black += 5;
                    else
                        black += 3;
                }
            }
        }
        return new int[]{red, black};
    }
    private void checkWinner() {
        List<int[]> blackMoves = findAllBlackMoves(MainBoard);
        List<int[]> redMoves = findAllRedMoves(MainBoard);
        int[] scores = getScores(MainBoard);
        if (scores[0] == 0 || (!blackMoves.isEmpty() && redMoves.isEmpty())) {
            winner("B");
        } else if (scores[1] == 0 || (blackMoves.isEmpty() && !redMoves.isEmpty())) {
            winner("R");
        } else if (blackMoves.isEmpty()) {
            winner("T");
        }
    }
    private void winner(String s) {
        popUp.setVisible(true);
        lockMove = true;
        if (s.equals("T")) {
            finalText.setText("Tie!");
        } else if (s.equals("B")) {
            finalText.setText("You Lose!");
        }
    }
    private void move(int rowInput, int columnInput) {
        if (!lockMove) {
            if (rowInput1 == -1 && colInput1 == -1) {
                rowInput1 = rowInput;
                colInput1 = columnInput;
                if (MainBoard[rowInput1][colInput1] == null) {
                    rowInput1 = -1;
                    colInput1 = -1;
                }
            } else {
                boolean moved = false;

                if (rowInput1 != rowInput && colInput1 != columnInput && !MainBoard[rowInput1][colInput1].isBlack) {
                    if (isMoveLegal(MainBoard, rowInput1, colInput1, rowInput, columnInput)) {
                        movePiece(MainBoard, rowInput1, colInput1, rowInput, columnInput);
                        moved = true;
                        //Promoting
                        if (!MainBoard[rowInput][columnInput].isBlack) {
                            if (rowInput == 0)
                                MainBoard[rowInput][columnInput].upgradeToKing();
                        } else {
                            if (rowInput == 7)
                                MainBoard[rowInput][columnInput].upgradeToKing();
                        }
                    }

                    displayBoard();
                    if (((rowInput1 + 2 == rowInput && colInput1 + 2 == columnInput) || (rowInput1 + 2 == rowInput && colInput1 - 2 == columnInput) || (rowInput1 - 2 == rowInput && colInput1 + 2 == columnInput) || (rowInput1 - 2 == rowInput && colInput1 - 2 == columnInput))) {
                        if (!canCapture(MainBoard, rowInput, columnInput) && moved) {
                            crappyAi();
                        }
                    } else if (moved) {
                        crappyAi();
                    }
                    displayBoard();
                }
                rowInput1 = -1;
                colInput1 = -1;
            }
        }
        checkWinner();
    }
    public void mouseClick(MouseEvent e) {
        int row = (int) e.getSceneY() / 75;
        int col = (int) e.getSceneX() / 75;
        move(row, col);
    }

    private boolean isMoveLegal(Piece[][] board, int rowInput1, int colInput1, int rowInput2, int colInput2) {
        //bounds detection
        if ((rowInput1 < 0 || colInput1 > 7) || (rowInput2 < 0 || colInput2 > 7))
            return false;
        //protecting from nulls
        if (board[rowInput1][colInput1] == null)
            return false;
        if (board[rowInput2][colInput2] != null)
            return false;
        //logic for king
        if (board[rowInput1][colInput1].isKing()) {
            if (rowInput1 - 1 == rowInput2 && colInput1 + 1 == colInput2 && (board[rowInput1 - 1][colInput1 + 1] == null)) {
                return true;
            } else if (rowInput1 - 1 == rowInput2 && colInput1 - 1 == colInput2 && (board[rowInput1 - 1][colInput1 - 1] == null)) {
                return true;
            } else if (rowInput1 + 1 == rowInput2 && colInput1 - 1 == colInput2 && (board[rowInput1 + 1][colInput1 - 1] == null)) {
                return true;
            } else if (rowInput1 + 1 == rowInput2 && colInput1 + 1 == colInput2 && (board[rowInput1 + 1][colInput1 + 1] == null)) {
                return true;
            } else {
                //Jumping Logic
                if (rowInput1 - 2 == rowInput2 && colInput1 + 2 == colInput2) {
                    return board[rowInput1 - 1][colInput1 + 1] != null && !board[rowInput1 - 1][colInput1 + 1].isBlack == board[rowInput1][colInput1].isBlack;
                } else if (rowInput1 - 2 == rowInput2 && colInput1 - 2 == colInput2) {
                    return board[rowInput1 - 1][colInput1 - 1] != null && !board[rowInput1 - 1][colInput1 - 1].isBlack == board[rowInput1][colInput1].isBlack;
                } else if (rowInput1 + 2 == rowInput2 && colInput1 + 2 == colInput2) {
                    return board[rowInput1 + 1][colInput1 + 1] != null && !board[rowInput1 + 1][colInput1 + 1].isBlack == board[rowInput1][colInput1].isBlack;
                } else if (rowInput1 + 2 == rowInput2 && colInput1 - 2 == colInput2) {
                    return board[rowInput1 + 1][colInput1 - 1] != null && !board[rowInput1 + 1][colInput1 - 1].isBlack == board[rowInput1][colInput1].isBlack;
                }
            }
        }
        //logic for Red
        else if (!board[rowInput1][colInput1].isBlack) {
            //Checking to move diagonally forward one space
            if (rowInput1 - 1 == rowInput2 && colInput1 - 1 == colInput2 && (board[rowInput1 - 1][colInput1 - 1] == null)) {
                return true;
            } else if (rowInput1 - 1 == rowInput2 && colInput1 + 1 == colInput2 && (board[rowInput1 - 1][colInput1 + 1] == null)) {
                return true;
            }
            //Jumping Logic
            else if (rowInput1 - 2 == rowInput2 && colInput1 + 2 == colInput2 && board[rowInput1 - 1][colInput1 + 1] != null) {
                return board[rowInput1 - 1][colInput1 + 1].isBlack;
            } else if (rowInput1 - 2 == rowInput2 && colInput1 - 2 == colInput2 && board[rowInput1 - 1][colInput1 - 1] != null ) {
                return board[rowInput1 - 1][colInput1 - 1].isBlack;
            }
        }
        //logic for Black
        else {
            //Checking to move diagonally forward one space
            if (rowInput1 + 1 == rowInput2 && colInput1 - 1 == colInput2 && (board[rowInput1 + 1][colInput1 - 1] == null)) {
                return true;
            } else if (rowInput1 + 1 == rowInput2 && colInput1 + 1 == colInput2 && (board[rowInput1 + 1][colInput1 + 1] == null)) {
                return true;
            }
            //Jumping Logic
            else if (rowInput1 + 2 == rowInput2 && colInput1 + 2 == colInput2 && board[rowInput1 + 1][colInput1 + 1] != null) {
                return !board[rowInput1 + 1][colInput1 + 1].isBlack;
            } else if (rowInput1 + 2 == rowInput2 && colInput1 - 2 == colInput2 && board[rowInput1 + 1][colInput1 - 1] != null ) {
                return !board[rowInput1 + 1][colInput1 - 1].isBlack;
            }
        }
        return false;

    }
    private void movePiece(Piece[][] board, int row, int column, int newRow, int newCol) {
        if (row - 2 == newRow && column + 2 == newCol) {
            board[row - 1][column + 1] = null;
        } else if (row - 2 == newRow && column - 2 == newCol) {
            board[row - 1][column - 1] = null;
        } else if (row + 2 == newRow && column + 2 == newCol) {
            board[row + 1][column + 1] = null;
        } else if (row + 2 == newRow && column - 2 == newCol) {
            board[row + 1][column - 1] = null;
        }
        Piece temp = board[row][column];
        board[row][column] = null;
        board[newRow][newCol] = temp;
    }
    public boolean canCapture(Piece[][] board, int row1, int col1) {
        if (row1 + 2 < 8 && col1 + 2 < 8 && isMoveLegal(board, row1, col1, row1 + 2, col1 + 2))
            return true;
        else if (row1 + 2 < 8 && col1 - 2 >= 0 && isMoveLegal(board, row1, col1, row1 + 2, col1 - 2))
            return true;
        else if (row1 - 2 >= 0 && col1 + 2 < 8 && isMoveLegal(board, row1, col1, row1 - 2, col1 + 2))
            return true;
        else
            return row1 - 2 >= 0 && col1 - 2 >= 0 && isMoveLegal(board, row1, col1, row1 - 2, col1 - 2);
    }

    private void displayBoard() {
        PieceAnchorPane.getChildren().clear();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (MainBoard[row][col] != null && !MainBoard[row][col].isBlack) {
                    Circle RedCircle = new Circle(37.5, Color.RED);
                    RedCircle.setCenterX((col * 75) + 37.5);
                    RedCircle.setCenterY((row * 75) + 37.5);
                    RedCircle.setStyle("-fx-stroke: black; -fx-strokeType: inside");
                    PieceAnchorPane.getChildren().add(RedCircle);
                    if (MainBoard[row][col].isKing()) {
                        Text RedKing = new Text("K");
                        RedKing.setX(col * 75);
                        RedKing.setY(row * 75);
                        RedKing.setLayoutX(29);
                        RedKing.setLayoutY(48);
                        RedKing.setFont(new Font(30));
                        PieceAnchorPane.getChildren().add(RedKing);
                    }
                }
                if (MainBoard[row][col] != null && MainBoard[row][col].isBlack) {
                    Circle BlackCircle = new Circle(37.5, Color.BLACK);
                    BlackCircle.setCenterX((col * 75) + 37.5);
                    BlackCircle.setCenterY((row * 75) + 37.5);
                    PieceAnchorPane.getChildren().add(BlackCircle);
                    if (MainBoard[row][col].isKing()) {
                        Text BlackKing = new Text("K");
                        BlackKing.setX(col * 75);
                        BlackKing.setY(row * 75);
                        BlackKing.setLayoutX(29);
                        BlackKing.setLayoutY(48);
                        BlackKing.setStyle("-fx-text-fill: white");
                        PieceAnchorPane.getChildren().add(BlackKing);
                    }
                }
            }
        }
    }
    private void setStarterBoard() {
        MainBoard[7][0] = new Piece(false);
        MainBoard[7][2] = new Piece(false);
        MainBoard[7][4] = new Piece(false);
        MainBoard[7][6] = new Piece(false);
        MainBoard[6][1] = new Piece(false);
        MainBoard[6][3] = new Piece(false);
        MainBoard[6][5] = new Piece(false);
        MainBoard[6][7] = new Piece(false);
        MainBoard[5][0] = new Piece(false);
        MainBoard[5][2] = new Piece(false);
        MainBoard[5][4] = new Piece(false);
        MainBoard[5][6] = new Piece(false);

        MainBoard[0][1] = new Piece(true);
        MainBoard[0][3] = new Piece(true);
        MainBoard[0][5] = new Piece(true);
        MainBoard[0][7] = new Piece(true);
        MainBoard[1][0] = new Piece(true);
        MainBoard[1][2] = new Piece(true);
        MainBoard[1][4] = new Piece(true);
        MainBoard[1][6] = new Piece(true);
        MainBoard[2][1] = new Piece(true);
        MainBoard[2][3] = new Piece(true);
        MainBoard[2][5] = new Piece(true);
        MainBoard[2][7] = new Piece(true);
    }

    @FXML
    public void onKeyPressed(KeyEvent key) {
        try {
            if (key.getCode().equals(KeyCode.ENTER))
                exit();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // ----------- Shitty Tester methods -----------

    /*
    public void unitTests(){
        double numTests = 15;
        long total = 0, startTime;

        for(int x = 0; x < numTests; x++) {
            try {

                clearBoard();
                setStarterBoard();
                synchronized (this) {
                    movePiece(MainBoard, 5, 4, 4, 3);
                    startTime = System.nanoTime();
                    crappyAi();
                    total += System.nanoTime() - startTime;
                }
                System.out.println(x);

                synchronized (this) {
                    movePiece(MainBoard, 5, 6, 4, 5);
                    startTime = System.nanoTime();
                    crappyAi();
                    total += System.nanoTime() - startTime;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }
    */
    public void unitTests(){

    }

    private void clearBoard(){
        for(int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                MainBoard[x][y] = null;
            }
        }
    }

    public void moveOnBoard(int row, int column, int newRow, int newCol){
        if (row - 2 == newRow && column + 2 == newCol) {
            MainBoard[row - 1][column + 1] = null;
        } else if (row - 2 == newRow && column - 2 == newCol) {
            MainBoard[row - 1][column - 1] = null;
        } else if (row + 2 == newRow && column + 2 == newCol) {
            MainBoard[row + 1][column + 1] = null;
        } else if (row + 2 == newRow && column - 2 == newCol) {
            MainBoard[row + 1][column - 1] = null;
        }
        Piece temp = MainBoard[row][column];
        MainBoard[row][column] = null;
        MainBoard[newRow][newCol] = temp;
    }
}