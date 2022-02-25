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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CheckersController extends MenuController {
    private static class Piece {
        private final String color;
        private boolean isKing = false;

        public Piece(String color) {
            this.color = color;
        }

        public String getColor() {
            return color;
        }

        public boolean isKing() {
            return isKing;
        }

        public void upgradeToKing() {
            isKing = true;
        }
    }
    private class Algorithm implements Runnable{
        private final Piece[][] boardT;
        private final boolean isBlackT;
        private final int depthT;
        private final int num;

        public Algorithm(Piece[][] board, boolean isBlack, int depth, int num){
            this.boardT = board;
            this.isBlackT = isBlack;
            this.depthT = depth;
            this.num = num;
        }

        @Override
        public void run() {
            long startTime = System.nanoTime();
            eval[num] = findBestMove(boardT, isBlackT, depthT);
            System.out.println("Thread: " + (System.nanoTime() - startTime) / 1000000);
        }

        private double findBestMove(Piece[][] board, boolean isBlack, int depth) {
            //System.out.println(depth);
            Piece[][] tempBoard = new Piece[8][8];
            int maxDepth = 2;
            int index = 0;

            for (int z = 0; z < 8; z++)
                System.arraycopy(board[z], 0, tempBoard[z], 0, board.length);
            ArrayList<Double> scores = new ArrayList<>();
            ArrayList<int[]> allMovesBlack = findAllBlackMoves(board);


            if (isBlack) {
                for (int x = 0; x < allMovesBlack.size(); x++) {
                    allMovesBlack = findAllBlackMoves(tempBoard);
                    movePiece(tempBoard, allMovesBlack.get(x)[0], allMovesBlack.get(x)[1], allMovesBlack.get(x)[2], allMovesBlack.get(x)[3]);
                    if (canCapture(tempBoard, allMovesBlack.get(x)[2], allMovesBlack.get(x)[3]))
                        scores.add(x, findBestMove(tempBoard, true, depth));
                    else
                        scores.add(x, findBestMove(tempBoard, false, depth));
                    //evaluatePosition(tempBoard, allMovesBlack.size())
                    for (int z = 0; z < 8; z++)
                        System.arraycopy(board[z], 0, tempBoard[z], 0, board.length);
                }
                //Returning highest rated move
                if (!allMovesBlack.isEmpty()) {
                    int highest = 0, lowest = 0;
                    if (allMovesBlack.size() > 1) {
                        for (int x = 1; x < scores.size(); x++) {
                            if (scores.get(x) >= scores.get(highest))
                                highest = x;
                            else if (scores.get(x) < scores.get(lowest))
                                lowest = x;
                        }
                    }
                    if (highest == lowest)
                        index = (int) (Math.random() * allMovesBlack.size());
                    else
                        index = highest;
                }
            } else {
                ArrayList<int[]> allMovesRed = findAllRedMoves(tempBoard);
                if (depth == maxDepth) {
                    for (int x = 0; x < allMovesRed.size(); x++) {
                        allMovesRed = findAllRedMoves(tempBoard);
                        movePiece(tempBoard, allMovesRed.get(x)[0], allMovesRed.get(x)[1], allMovesRed.get(x)[2], allMovesRed.get(x)[3]);
                        //MultiCapture
                        if (canCapture(tempBoard, allMovesRed.get(x)[2], allMovesRed.get(x)[3]))
                            scores.add(x, findBestMove(tempBoard, false, depth));
                        else
                            scores.add(x, evaluatePosition(board, allMovesRed.size()));
                        for (int z = 0; z < 8; z++)
                            System.arraycopy(board[z], 0, tempBoard[z], 0, board.length);
                    }
                    //Returning highest rated move
                    if (!allMovesRed.isEmpty()) {
                        int highest = 0;
                        int lowest = 0;
                        if (allMovesRed.size() > 1) {
                            for (int x = 1; x < scores.size(); x++) {
                                if (scores.get(x) >= scores.get(highest))
                                    highest = x;
                                else if (scores.get(x) < scores.get(lowest))
                                    lowest = x;
                            }
                        }
                        if (highest == lowest)
                            index = (int) (Math.random() * allMovesRed.size());
                        else
                            index = lowest;
                        return scores.get(index);
                    }

                } else {
                    for (int x = 0; x < allMovesRed.size(); x++) {
                        movePiece(tempBoard, allMovesRed.get(x)[0], allMovesRed.get(x)[1], allMovesRed.get(x)[2], allMovesRed.get(x)[3]);
                        //MultiCapture
                        if (canCapture(tempBoard, allMovesRed.get(x)[2], allMovesRed.get(x)[3]))
                            scores.add(x, findBestMove(tempBoard, false, depth));
                        else
                            scores.add(x, findBestMove(tempBoard, true, depth + 1));
                        for (int z = 0; z < 8; z++)
                            System.arraycopy(board[z], 0, tempBoard[z], 0, board.length);
                    }
                }
            }
            return scores.get(index);
        }
        private double evaluatePosition(Piece[][] board, int amountOfMoves) {
            // How the algo works:
            //If pieces are all gone it is either good or bad position
            //Score is next important, kings = 5 plebeians = 3
            //Then Distance to king a piece
            //Finally there is a slightly random component
            int[] scores = getScores(board);
            int[] pieceCount = countAll(board);
            double positionScoreForBlack = 0;


            //if all pieces are gone then it's either a fantastic or horrible position
            if (pieceCount[0] == 0)
                return 999999;
            else if (pieceCount[1] == 0)
                return -999999;
            else if(amountOfMoves == 0)
                return  -999999;
            //if the score is higher than better position
            /*
            if (scores[0] > scores[1])
                positionScoreForBlack -= 20;
            else if (scores[0] < scores[1])
                positionScoreForBlack += 20;
             */
            positionScoreForBlack += (scores[1] - scores[0]) * 7.5;

            //This takes into account the distance to becoming royalty
            int blackDistance = 0 , redDistance = 0;
            for(int x = 0; x < 8; x++){
                for(int y = 0; y < 8; y++){
                    if(board[x][y] != null && !board[x][y].isKing) {
                        if (board[x][y].color.equals("Black"))
                            blackDistance += (6 - x);
                        else
                            redDistance  += (x-1);
                    }
                }
            }
            if(blackDistance > redDistance)
                positionScoreForBlack += 3;
            else if (blackDistance < redDistance)
                positionScoreForBlack -= 3;


            positionScoreForBlack += (Math.random()*5)-2.5;

            return positionScoreForBlack;
        }

        //Used to calculate all possible moves for the bot
        private ArrayList<int[]> findAllBlackMoves(Piece[][] board) {
            ArrayList<int[]> moves = new ArrayList<>();
            for (int x = 7; x > -1; x--) {
                for (int y = 7; y > -1; y--) {
                    int[] temp = new int[4];
                    if (board[x][y] != null && board[x][y].getColor().equals("Black")) {
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
        private ArrayList<int[]> findAllRedMoves(Piece[][] board) {
            ArrayList<int[]> moves = new ArrayList<>();
            int[] temp = new int[4];
            for (int x = 7; x > -1; x--) {
                for (int y = 7; y > -1; y--) {
                    if (board[x][y] != null && board[x][y].getColor().equals("Red")) {
                        if (!board[x][y].isKing()) {
                            if ((x - 1 >= 0) && (y - 1 >= 0) && isMoveLegal(board, x, y, x - 1, y - 1)) {
                                temp[0] = x;
                                temp[1] = y;
                                temp[2] = x - 1;
                                temp[3] = y - 1;
                                moves.add(temp);
                            }
                            if ((x - 1 >= 0) && (y + 1 <= 7) && isMoveLegal(board, x, y, x - 1, y + 1)) {
                                temp[0] = x;
                                temp[1] = y;
                                temp[2] = x - 1;
                                temp[3] = y + 1;
                                moves.add(temp);
                            }
                            //Jumping
                            if ((x - 2 >= 0) && (y - 2 >= 0) && isMoveLegal(board, x, y, x - 2, y - 2)) {
                                temp[0] = x;
                                temp[1] = y;
                                temp[2] = x - 2;
                                temp[3] = y - 2;
                                moves.add(temp);
                            }
                            if ((x - 2 >= 7) && (y + 2 <= 7) && isMoveLegal(board, x, y, x - 2, y + 2)) {
                                temp[0] = x;
                                temp[1] = y;
                                temp[2] = x - 2;
                                temp[3] = y + 2;
                                moves.add(temp);
                            }
                        } else {
                            if ((x + 1 <= 7) && (y - 1 >= 0) && isMoveLegal(board, x, y, x + 1, y - 1)) {
                                temp[0] = x;
                                temp[1] = y;
                                temp[2] = x + 1;
                                temp[3] = y - 1;
                                moves.add(temp);
                            }
                            if ((x + 1 <= 7) && (y + 1 <= 7) && isMoveLegal(board, x, y, x + 1, y + 1)) {
                                temp[0] = x;
                                temp[1] = y;
                                temp[2] = x + 1;
                                temp[3] = y + 1;
                                moves.add(temp);
                            }
                            if ((x - 1 >= 0) && (y - 1 >= 0) && isMoveLegal(board, x, y, x - 1, y - 1)) {
                                temp[0] = x;
                                temp[1] = y;
                                temp[2] = x - 1;
                                temp[3] = y - 1;
                                moves.add(temp);
                            }
                            if ((x - 1 >= 0) && (y + 1 <= 7) && isMoveLegal(board, x, y, x - 1, y + 1)) {
                                temp[0] = x;
                                temp[1] = y;
                                temp[2] = x - 1;
                                temp[3] = y + 1;
                                moves.add(temp);
                            }
                            //jumping
                            if ((x + 2 <= 7) && (y - 2 >= 0) && isMoveLegal(board, x, y, x + 2, y - 2)) {
                                temp[0] = x;
                                temp[1] = y;
                                temp[2] = x + 2;
                                temp[3] = y - 2;
                                moves.add(temp);
                            }
                            if ((x + 2 <= 7) && (y + 2 <= 7) && isMoveLegal(board, x, y, x + 2, y + 2)) {
                                temp[0] = x;
                                temp[1] = y;
                                temp[2] = x + 2;
                                temp[3] = y + 2;
                                moves.add(temp);
                            }
                            if ((x - 2 >= 0) && (y - 2 >= 0) && isMoveLegal(board, x, y, x - 2, y - 2)) {
                                temp[0] = x;
                                temp[1] = y;
                                temp[2] = x - 2;
                                temp[3] = y - 2;
                                moves.add(temp);
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
    }





    @FXML
    AnchorPane popUp, PieceAnchorPane;
    @FXML
    Text finalText;

    private double[] eval;

    private int rowInput1 = -1;
    private int colInput1 = -1;
    private Piece[][] MainBoard = new Piece[8][8];
    boolean lockMove = false;
    private final int[] done = new int[4];


    public void initialize() {
        MainBoard = new Piece[8][8];
        lockMove = false;
        popUp.setVisible(false);
        setStarterBoard();
        displayBoard();
        //popUp.setVisible(true);
    }


    private int[] startAI(Piece[][] board){
        ArrayList<int[]> allMovesBlack = findAllBlackMoves(MainBoard);
        Piece[][] tempBoard = new Piece[8][8];
        int[] finished = new int[4];
        int index = 0;
        eval = new double[allMovesBlack.size()];

        try {
            ExecutorService es = Executors.newCachedThreadPool();
            for (int x = 0; x < allMovesBlack.size(); x++) {
                movePiece(tempBoard, allMovesBlack.get(x)[0], allMovesBlack.get(x)[1], allMovesBlack.get(x)[2], allMovesBlack.get(x)[3]);
                Algorithm myThread;
                if (canCapture(tempBoard, allMovesBlack.get(x)[2], allMovesBlack.get(x)[3]))
                    myThread = new Algorithm(tempBoard, true, 1, x);
                else
                    myThread = new Algorithm(tempBoard, false, 1, x);
                es.execute(myThread);

                for (int z = 0; z < 8; z++)
                    System.arraycopy(board[z], 0, tempBoard[z], 0, board.length);
            }

            es.shutdown();
            boolean thing = es.awaitTermination(4, TimeUnit.MINUTES);

            if(!thing)
                throw new Exception();
        }catch (Exception e){
            e.printStackTrace();
        }
        if (!allMovesBlack.isEmpty()) {
            int highest = 0, lowest = 0;
            if (allMovesBlack.size() > 1) {
                for (int x = 1; x < eval.length; x++) {
                    if (eval[x] >= eval[highest])
                        highest = x;
                    else if (eval[x] < eval[lowest])
                        lowest = x;
                }
            }
            if (highest == lowest)
                index = (int) (Math.random() * allMovesBlack.size());
            else
                index = highest;
        }

        if(allMovesBlack.size() > 0)
            System.arraycopy(allMovesBlack.get(index), 0, finished, 0, allMovesBlack.get(index).length);
        System.out.println(Arrays.toString(eval));
        System.out.println(eval[index]);
        return finished;
    }
    private void crappyAi() {
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

            //findBestMove(MainBoard, true, 1, false);
            int[] x = startAI(MainBoard);
            System.arraycopy(x, 0, done, 0, x.length);

            System.out.println((System.nanoTime() - startTime) / 1000000);


            movePiece(MainBoard, done[0], done[1], done[2], done[3]);
            if((done[1]+2 == done[3] || done[1]-2 == done[3]) && canCapture(MainBoard, done[2], done[3])){
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
    /*
    private double findBestMove(Piece[][] board, boolean isBlack, int depth, boolean isMultiCapture) {
        long startTime = System.nanoTime();
        //System.out.println(depth);
        Piece[][] tempBoard = new Piece[8][8];
        int maxDepth = 2;
        int index = 0;

        //if(isMultiCapture)
        //    depth -= 1;

        for (int z = 0; z < 8; z++)
            System.arraycopy(board[z], 0, tempBoard[z], 0, board.length);
        ArrayList<Double> scores = new ArrayList<>();
        ArrayList<int[]> allMovesBlack = findAllBlackMoves(board);


        if (isBlack) {
            for (int x = 0; x < allMovesBlack.size(); x++) {
                allMovesBlack = findAllBlackMoves(tempBoard);
                movePiece(tempBoard, allMovesBlack.get(x)[0], allMovesBlack.get(x)[1], allMovesBlack.get(x)[2], allMovesBlack.get(x)[3]);
                if (canCapture(tempBoard, allMovesBlack.get(x)[2], allMovesBlack.get(x)[3]))
                    scores.add(x, findBestMove(tempBoard, true, depth, true));
                else
                    scores.add(x, findBestMove(tempBoard, false, depth, false));
                //evaluatePosition(tempBoard, allMovesBlack.size())
                for (int z = 0; z < 8; z++)
                    System.arraycopy(board[z], 0, tempBoard[z], 0, board.length);
            }
            //Returning highest rated move
            if (!allMovesBlack.isEmpty()) {
                int highest = 0, lowest = 0;
                if (allMovesBlack.size() > 1) {
                    for (int x = 1; x < scores.size(); x++) {
                        if (scores.get(x) >= scores.get(highest))
                            highest = x;
                        else if (scores.get(x) < scores.get(lowest))
                            lowest = x;
                    }
                }
                if (highest == lowest)
                    index = (int) (Math.random() * allMovesBlack.size());
                else
                    index = highest;
            }
        }
        else {
            ArrayList<int[]> allMovesRed = findAllRedMoves(tempBoard);
            if (depth == maxDepth) {
                for (int x = 0; x < allMovesRed.size(); x++) {
                    allMovesRed = findAllRedMoves(tempBoard);
                    movePiece(tempBoard, allMovesRed.get(x)[0], allMovesRed.get(x)[1], allMovesRed.get(x)[2], allMovesRed.get(x)[3]);
                    //MultiCapture
                    if (canCapture(tempBoard, allMovesRed.get(x)[2], allMovesRed.get(x)[3]))
                        scores.add(x, findBestMove(tempBoard, false, depth, true));
                    else
                        scores.add(x, evaluatePosition(board, allMovesRed.size()));
                    for (int z = 0; z < 8; z++)
                        System.arraycopy(board[z], 0, tempBoard[z], 0, board.length);
                }
                //Returning highest rated move
                if (!allMovesRed.isEmpty()) {
                    int highest = 0;
                    int lowest = 0;
                    if (allMovesRed.size() > 1) {
                        for (int x = 1; x < scores.size(); x++) {
                            if (scores.get(x) >= scores.get(highest))
                                highest = x;
                            else if (scores.get(x) < scores.get(lowest))
                                lowest = x;
                        }
                    }
                    if (highest == lowest)
                        index = (int) (Math.random() * allMovesRed.size());
                    else
                        index = lowest;
                    return scores.get(index);
                }

            } else {
                for (int x = 0; x < allMovesRed.size(); x++) {
                    movePiece(tempBoard, allMovesRed.get(x)[0], allMovesRed.get(x)[1], allMovesRed.get(x)[2], allMovesRed.get(x)[3]);
                    //MultiCapture
                    if (canCapture(tempBoard, allMovesRed.get(x)[2], allMovesRed.get(x)[3]))
                        scores.add(x, findBestMove(tempBoard, false, depth, true));
                    else
                        scores.add(x, findBestMove(tempBoard, true, depth + 1, false));
                    for (int z = 0; z < 8; z++)
                        System.arraycopy(board[z], 0, tempBoard[z], 0, board.length);
                }
            }
        }
        if(allMovesBlack.size() > 0)
            System.arraycopy(allMovesBlack.get(index), 0, done, 0, allMovesBlack.get(index).length);
        System.out.println("Normie: " + (System.nanoTime() - startTime) / 1000000);
        return -1;
    }
    private double evaluatePosition(Piece[][] board, int amountOfMoves) {
        // How the algo works:
        //If pieces are all gone it is either good or bad position
        //Score is next important, kings = 5 plebeians = 3
        //Then Distance to king a piece
        //Finally there is a slightly random component
        int[] scores = getScores(board);
        int[] pieceCount = countAll(board);
        double positionScoreForBlack = 0;


        //if all pieces are gone then it's either a fantastic or horrible position
        if (pieceCount[0] == 0)
            return 999999;
        else if (pieceCount[1] == 0)
            return -999999;
        else if(amountOfMoves == 0)
            return  -999999;
        //if the score is higher than better position
        if (scores[0] > scores[1])
            positionScoreForBlack -= 20;
        else if (scores[0] < scores[1])
            positionScoreForBlack += 20;

        //This takes into account the distance to becoming royalty
                int blackDistance = 0 , redDistance = 0;
        for(int x = 0; x < 8; x++){
            for(int y = 0; y < 8; y++){
                if(board[x][y] != null && !board[x][y].isKing) {
                    if (board[x][y].color.equals("Black"))
                        blackDistance += (6 - x);
                    else
                        redDistance  += (x-1);
                }
            }
        }
        if(blackDistance > redDistance)
            positionScoreForBlack += 3;
        else if (blackDistance < redDistance)
            positionScoreForBlack -= 3;


        positionScoreForBlack += (Math.random()*5)-2.5;

        return positionScoreForBlack;
    }

    //Used to calculate all possible moves for the bot
     */

    private ArrayList<int[]> findAllBlackMoves(Piece[][] board) {
        ArrayList<int[]> moves = new ArrayList<>();
        for (int x = 7; x > -1; x--) {
            for (int y = 7; y > -1; y--) {
                int[] temp = new int[4];
                if (board[x][y] != null && board[x][y].getColor().equals("Black")) {
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
    private ArrayList<int[]> findAllRedMoves(Piece[][] board) {
        ArrayList<int[]> moves = new ArrayList<>();
        int[] temp = new int[4];
        for (int x = 7; x > -1; x--) {
            for (int y = 7; y > -1; y--) {
                if (board[x][y] != null && board[x][y].getColor().equals("Red")) {
                    if (!board[x][y].isKing()) {
                        if ((x - 1 >= 0) && (y - 1 >= 0) && isMoveLegal(board, x, y, x - 1, y - 1)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x - 1;
                            temp[3] = y - 1;
                            moves.add(temp);
                        }
                        if ((x - 1 >= 0) && (y + 1 <= 7) && isMoveLegal(board, x, y, x - 1, y + 1)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x - 1;
                            temp[3] = y + 1;
                            moves.add(temp);
                        }
                        //Jumping
                        if ((x - 2 >= 0) && (y - 2 >= 0) && isMoveLegal(board, x, y, x - 2, y - 2)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x - 2;
                            temp[3] = y - 2;
                            moves.add(temp);
                        }
                        if ((x - 2 >= 7) && (y + 2 <= 7) && isMoveLegal(board, x, y, x - 2, y + 2)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x - 2;
                            temp[3] = y + 2;
                            moves.add(temp);
                        }
                    } else {
                        if ((x + 1 <= 7) && (y - 1 >= 0) && isMoveLegal(board, x, y, x + 1, y - 1)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x + 1;
                            temp[3] = y - 1;
                            moves.add(temp);
                        }
                        if ((x + 1 <= 7) && (y + 1 <= 7) && isMoveLegal(board, x, y, x + 1, y + 1)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x + 1;
                            temp[3] = y + 1;
                            moves.add(temp);
                        }
                        if ((x - 1 >= 0) && (y - 1 >= 0) && isMoveLegal(board, x, y, x - 1, y - 1)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x - 1;
                            temp[3] = y - 1;
                            moves.add(temp);
                        }
                        if ((x - 1 >= 0) && (y + 1 <= 7) && isMoveLegal(board, x, y, x - 1, y + 1)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x - 1;
                            temp[3] = y + 1;
                            moves.add(temp);
                        }
                        //jumping
                        if ((x + 2 <= 7) && (y - 2 >= 0) && isMoveLegal(board, x, y, x + 2, y - 2)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x + 2;
                            temp[3] = y - 2;
                            moves.add(temp);
                        }
                        if ((x + 2 <= 7) && (y + 2 <= 7) && isMoveLegal(board, x, y, x + 2, y + 2)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x + 2;
                            temp[3] = y + 2;
                            moves.add(temp);
                        }
                        if ((x - 2 >= 0) && (y - 2 >= 0) && isMoveLegal(board, x, y, x - 2, y - 2)) {
                            temp[0] = x;
                            temp[1] = y;
                            temp[2] = x - 2;
                            temp[3] = y - 2;
                            moves.add(temp);
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

    //some helper methods for evaluatePosition, red then black to avoid code duplication
    private int[] countAll(Piece[][] board) {
        int red = 0;
        int black = 0;
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                if (board[x][y] != null && board[x][y].getColor().equals("Red"))
                    red++;
                if (board[x][y] != null && board[x][y].getColor().equals("Black"))
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
                if (board[x][y] != null && board[x][y].getColor().equals("Red")) {
                    if (board[x][y].isKing())
                        red += 5;
                    else
                        red += 3;
                }
                if (board[x][y] != null && board[x][y].getColor().equals("Black")) {
                    if (board[x][y].isKing())
                        black += 5;
                    else
                        black += 3;
                }
            }
        }
        return new int[] {red, black};
    }
    private void checkWinner() {
        ArrayList<int[]> blackMoves = findAllBlackMoves(MainBoard);
        ArrayList<int[]> redMoves = findAllRedMoves(MainBoard);
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

                if (rowInput1 != rowInput && colInput1 != columnInput && MainBoard[rowInput1][colInput1].getColor().equals("Red")) {
                    if (isMoveLegal(MainBoard, rowInput1, colInput1, rowInput, columnInput)) {
                        movePiece(MainBoard, rowInput1, colInput1, rowInput, columnInput);
                        moved = true;
                        //Promoting
                        if (MainBoard[rowInput][columnInput].getColor().equals("Red")) {
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
        else if (board[rowInput1][colInput1] == null)
            return false;
        else if (board[rowInput2][colInput2] != null)
            return false;
            //logic for king
        else if (board[rowInput1][colInput1].isKing()) {
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
                    //Checks it isn't null and checks they aren't the same color
                    return board[rowInput1 - 1][colInput1 + 1] != null && !board[rowInput1 - 1][colInput1 + 1].getColor().equals(board[rowInput1][colInput1].getColor());
                } else if (rowInput1 - 2 == rowInput2 && colInput1 - 2 == colInput2) {
                    return board[rowInput1 - 1][colInput1 - 1] != null && !board[rowInput1 - 1][colInput1 - 1].getColor().equals(board[rowInput1][colInput1].getColor());
                } else if (rowInput1 + 2 == rowInput2 && colInput1 + 2 == colInput2) {
                    return board[rowInput1 + 1][colInput1 + 1] != null && !board[rowInput1 + 1][colInput1 + 1].getColor().equals(board[rowInput1][colInput1].getColor());
                } else if (rowInput1 + 2 == rowInput2 && colInput1 - 2 == colInput2) {
                    return board[rowInput1 + 1][colInput1 - 1] != null && !board[rowInput1 + 1][colInput1 - 1].getColor().equals(board[rowInput1][colInput1].getColor());
                }
            }
        }
        //logic for Red
        else if (board[rowInput1][colInput1].getColor().equals("Red")) {
            //Checking to move diagonally forward one space
            if (rowInput1 - 1 == rowInput2 && colInput1 - 1 == colInput2 && (board[rowInput1 - 1][colInput1 - 1] == null)) {
                return true;
            } else if (rowInput1 - 1 == rowInput2 && colInput1 + 1 == colInput2 && (board[rowInput1 - 1][colInput1 + 1] == null)) {
                return true;
            }
            //Jumping Logic
            else if (rowInput1 - 2 == rowInput2 && colInput1 + 2 == colInput2) {
                return board[rowInput1 - 1][colInput1 + 1] != null && board[rowInput1 - 1][colInput1 + 1].getColor().equals("Black");
            } else if (rowInput1 - 2 == rowInput2 && colInput1 - 2 == colInput2) {
                return board[rowInput1 - 1][colInput1 - 1] != null && board[rowInput1 - 1][colInput1 - 1].getColor().equals("Black");
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
            else if (rowInput1 + 2 == rowInput2 && colInput1 + 2 == colInput2) {
                return board[rowInput1 + 1][colInput1 + 1] != null && board[rowInput1 + 1][colInput1 + 1].getColor().equals("Red");
            } else if (rowInput1 + 2 == rowInput2 && colInput1 - 2 == colInput2) {
                return board[rowInput1 + 1][colInput1 - 1] != null && board[rowInput1 + 1][colInput1 - 1].getColor().equals("Red");
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
                if (MainBoard[row][col] != null && MainBoard[row][col].getColor().equals("Red")) {
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
                if (MainBoard[row][col] != null && MainBoard[row][col].getColor().equals("Black")) {
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

    @FXML
    public void onKeyPressed(KeyEvent key){
        try {
            if (key.getCode().equals(KeyCode.ENTER))
                exit();

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}