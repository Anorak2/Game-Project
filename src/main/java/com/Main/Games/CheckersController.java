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
        private int maxDepth;
        private final boolean isMulti;
        private final int[] lastMove;

        public Algorithm(Piece[][] board, boolean isBlack, int num, boolean isMulti, int[] lastMove) {
            int tempLoopStore = board.length;
            for (int x = 0; x < tempLoopStore; x ++)
                System.arraycopy(board[x], 0, boardT[x], 0, board[x].length);
            this.isBlackT = isBlack;
            this.num = num;
            this.isMulti = isMulti;
            this.lastMove = lastMove;
        }

        @Override
        public void run() {
            if(numberOfMoves >= 21) {
                maxDepth = 6;
            }
            else if(numberOfMoves > 13) {
                maxDepth = 7;
            }
            else if (numberOfMoves > 8) {
                maxDepth = 9;
            }


            eval[num] = findBestMove(boardT, isBlackT, 0, isMulti, lastMove);
        }

        //The recursive methods
        //The bane of my existence
        private synchronized double findBestMove(Piece[][] board, boolean isBlack, int depth, boolean isMulti, int[] lastMove){
            Piece[][] tempBoard;
            int index;
            List<int[]> allMoves;
            Double[] scores;

            // The exit clause
            if(depth == maxDepth)
                return evaluatePosition(board);


            // Exit if the game is won or lost
            double check = simpleEvaluate(board);
            if(check != 0)
                return check;

            //---------- Main Logic ----------
            if (isBlack) {
                allMoves = findAllBlackMoves(board);
            }
            else {
                allMoves = findAllRedMoves(board);
            }
            scores = new Double[allMoves.size()];
            int iteration = 0;

            tempBoard = new Piece[8][8];
            for (int[] ints : allMoves) {
                //Enforcing multi-capture rules
                if(isMulti && (ints[0] != lastMove[2] || ints[1] != lastMove[3])){
                    scores[iteration] = 929274.001;
                    iteration++;
                    continue;
                }
                //---------- Recursion ----------
                //This method creates the new recursive methods
                for (int z = 0; z < 8; z++) {
                    System.arraycopy(board[z], 0, tempBoard[z], 0, 8);
                }

                movePiece(tempBoard, ints[0], ints[1], ints[2], ints[3]);
                //MultiCapture
                //Checks if the last move was a capture and if we can capture again

                if (isCapture(ints) && canCapture(tempBoard, ints[2], ints[3])) {
                    scores[iteration] = findBestMove(tempBoard, isBlack, depth + 1, true, ints);
                }
                else{
                    scores[iteration] = findBestMove(tempBoard, !isBlack, depth + 1, false, ints);
                }
                iteration++;
            }

            //---------- Returning optimal move ----------
            int highest = 0, lowest = 0;
            int loopStorage = scores.length;
            for (int x = 1; x < loopStorage; x++) {
                if(scores[x] != null && scores[x] != 929274.001) {
                     if (scores[x] > scores[highest]) {
                        highest = x;
                    } else if (scores[x] < scores[lowest]) {
                        lowest = x;
                    }
                }
            }
            if(isBlack)
                index = highest;
            else
                index = lowest;
            return scores[index];
        }


        //Evaluates if the position is good for the bot or not
        private double evaluatePosition(Piece[][] board) {
            // How the algo works:
            //If pieces are all gone it is either very good or very bad position
            //Score is next important, kings = 5 plebeians = 3
            //Then Distance to king a piece
            //Finally there is a slightly random component

            double simpleEval = simpleEvaluate(board);
            if(simpleEval != 0)
                return simpleEval;

            //Red then black
            int[] scores = getScores(board);
            double positionScoreForBlack = 0;

            // difference in score x10 to emphasize importance of score
            positionScoreForBlack += (scores[1] - scores[0]) * 4.5;


            //This takes into account the distance to becoming royalty
            int blackDistance = 0, redDistance = 0;
            for (int x = 0; x < 8; x++) {
                if (board[1][x] != null && !board[1][x].isKing) {
                    if (!board[1][x].isBlack)
                        redDistance += x;
                }
                if (board[6][x] != null && !board[6][x].isKing) {
                    if (board[6][x].isBlack)
                        blackDistance += x;
                }
            }
            if (blackDistance > redDistance)
                positionScoreForBlack += 1;
            else if (blackDistance < redDistance)
                positionScoreForBlack -= 1;


            // Random element
            positionScoreForBlack += (Math.random() * 1) - .5;

            return positionScoreForBlack;
        }


        // These are extremely cut down methods which are used for efficiency
        private double simpleEvaluate(Piece[][] board){
            boolean[] pieceCount = simpleCount(board);

            //If there are no red pieces
            if (!pieceCount[0])
                return 999999;
            // If there are no black pieces
            else if (!pieceCount[1])
                return -999999;
            //If black has no moves available
            else if (!simpleBlackMoves(board))
                return -999999;
            //if red has no moves available
            else if (!simpleRedMoves(board))
                return 999999;

            return 0;
        }
        private boolean[] simpleCount(Piece[][] board) {
            boolean red = false;
            boolean black = false;
            for (int x = 0; x < 8; x++) {
                for (int y = 0; y < 8; y++) {
                    if (board[x][y] != null) {
                        if (!black && board[x][y].isBlack) {
                            black = true;
                            if(red) {
                                return new boolean[]{true, true};
                            }
                        }
                        else if(!red){
                            red = true;
                            if(black) {
                                return new boolean[]{true, true};
                            }
                        }
                    }
                }
            }
            return new boolean[]{red, black};
        }
        public static boolean simpleBlackMoves(Piece[][] board) {
            for (int x = 0; x <= 7; x++) {
                for (int y = 0; y <= 7; y++) {
                    if (board[x][y] != null && board[x][y].isBlack) {
                        //regular pieces
                        if (!board[x][y].isKing()) {
                            if ((x + 1 <= 7) && (y - 1 >= 0) && isMoveLegal(board, x, y, x + 1, y - 1)) {
                                return true;
                            }
                            if ((x + 1 <= 7) && (y + 1 <= 7) && isMoveLegal(board, x, y, x + 1, y + 1)) {
                                return true;                            }
                        }
                        //kings
                        else {
                            if ((x + 1 <= 7) && (y - 1 >= 0) && isMoveLegal(board, x, y, x + 1, y - 1)) {
                                return true;
                            }
                            if ((x + 1 <= 7) && (y + 1 <= 7) && isMoveLegal(board, x, y, x + 1, y + 1)) {
                                return true;
                            }
                            if ((x - 1 >= 0) && (y - 1 >= 0) && isMoveLegal(board, x, y, x - 1, y - 1)) {
                                return true;                            }
                            if ((x - 1 >= 0) && (y + 1 <= 7) && isMoveLegal(board, x, y, x - 1, y + 1)) {
                                return true;
                            }
                            //jumping
                            if ((x - 2 >= 0) && (y - 2 >= 0) && isMoveLegal(board, x, y, x - 2, y - 2)) {
                                return true;
                            }
                            if ((x - 2 >= 0) && (y + 2 <= 7) && isMoveLegal(board, x, y, x - 2, y + 2)) {
                                return true;                            }
                        }

                        //Shared Jumping
                        if ((x + 2 <= 7) && (y - 2 >= 0) && isMoveLegal(board, x, y, x + 2, y - 2)) {
                            return true;                        }
                        if ((x + 2 <= 7) && (y + 2 <= 7) && isMoveLegal(board, x, y, x + 2, y + 2)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
        private static boolean simpleRedMoves(Piece[][] board) {
            for (int x = 7; x > -1; x--) {
                for (int y = 7; y > -1; y--) {
                    if (board[x][y] != null && !board[x][y].isBlack) {
                        if (!board[x][y].isKing()) {
                            if ((x - 1 >= 0) && (y - 1 >= 0) && isMoveLegal(board, x, y, x - 1, y - 1)) {
                                return true;
                            }
                            if ((x - 1 >= 0) && (y + 1 <= 7) && isMoveLegal(board, x, y, x - 1, y + 1)) {
                                return true;
                            }
                        }
                        else {
                            if ((x + 1 <= 7) && (y - 1 >= 0) && isMoveLegal(board, x, y, x + 1, y - 1)) {
                                return true;
                            }
                            if ((x + 1 <= 7) && (y + 1 <= 7) && isMoveLegal(board, x, y, x + 1, y + 1)) {
                                return true;
                            }
                            if ((x - 1 >= 0) && (y - 1 >= 0) && isMoveLegal(board, x, y, x - 1, y - 1)) {
                                return true;
                            }
                            if ((x - 1 >= 0) && (y + 1 <= 7) && isMoveLegal(board, x, y, x - 1, y + 1)) {
                                return true;
                            }
                            //jumping
                            if ((x + 2 <= 7) && (y - 2 >= 0) && isMoveLegal(board, x, y, x + 2, y - 2)) {
                                return true;
                            }
                            if ((x + 2 <= 7) && (y + 2 <= 7) && isMoveLegal(board, x, y, x + 2, y + 2)) {
                                return true;
                            }
                        }
                        //Shared Jumping part
                        if ((x - 2 >= 0) && (y - 2 >= 0) && isMoveLegal(board, x, y, x - 2, y - 2)) {
                            return true;
                        }
                        if ((x - 2 >= 0) && (y + 2 <= 7) && isMoveLegal(board, x, y, x - 2, y + 2)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        //Finding moves, duh
        public static List<int[]> findAllBlackMoves(Piece[][] board) {
            List<int[]> moves = new LinkedList<>();
            for (int x = 7; x >= 0; x--) {
                for (int y = 7; y >= 0; y--) {
                    if (board[x][y] != null && board[x][y].isBlack) {
                        //regular pieces
                        if (!board[x][y].isKing()) {
                            if ((x + 1 <= 7) && (y - 1 >= 0) && isMoveLegal(board, x, y, x + 1, y - 1)) {
                                moves.add(new int[]{x, y, x+1, y-1});
                            }
                            if ((x + 1 <= 7) && (y + 1 <= 7) && isMoveLegal(board, x, y, x + 1, y + 1)) {
                                moves.add(new int[]{x, y, x+1, y+1});
                            }
                        }
                        //kings
                        else {
                            if ((x + 1 <= 7) && (y - 1 >= 0) && isMoveLegal(board, x, y, x + 1, y - 1)) {
                                moves.add(new int[]{x, y, x+1, y-1});

                            }
                            if ((x + 1 <= 7) && (y + 1 <= 7) && isMoveLegal(board, x, y, x + 1, y + 1)) {
                                moves.add(new int[]{x, y, x+1, y+1});

                            }
                            if ((x - 1 >= 0) && (y - 1 >= 0) && isMoveLegal(board, x, y, x - 1, y - 1)) {
                                moves.add(new int[]{x, y, x-1, y-1});
                            }
                            if ((x - 1 >= 0) && (y + 1 <= 7) && isMoveLegal(board, x, y, x - 1, y + 1)) {
                                moves.add(new int[]{x, y, x-1, y+1});

                            }
                            //jumping
                            if ((x - 2 >= 0) && (y - 2 >= 0) && isMoveLegal(board, x, y, x - 2, y - 2)) {
                                moves.add(new int[]{x, y, x-2, y-2});

                            }
                            if ((x - 2 >= 0) && (y + 2 <= 7) && isMoveLegal(board, x, y, x - 2, y + 2)) {
                                moves.add(new int[]{x, y, x-2, y+2});
                            }
                        }

                        //Shared Jumping
                        if ((x + 2 <= 7) && (y - 2 >= 0) && isMoveLegal(board, x, y, x + 2, y - 2)) {
                            moves.add(new int[]{x, y, x+2, y-2});
                        }
                        if ((x + 2 <= 7) && (y + 2 <= 7) && isMoveLegal(board, x, y, x + 2, y + 2)) {
                            moves.add(new int[]{x, y, x+2, y+2});

                        }
                    }
                }
            }
            return moves;
        }
        public static List<int[]> findAllRedMoves(Piece[][] board) {
            List<int[]> moves = new LinkedList<>();
            for (int x = 7; x > -1; x--) {
                for (int y = 7; y > -1; y--) {
                    if (board[x][y] != null && !board[x][y].isBlack) {
                        if (!board[x][y].isKing()) {
                            if ((x - 1 >= 0) && (y - 1 >= 0) && isMoveLegal(board, x, y, x - 1, y - 1)) {
                                moves.add(new int[]{x, y, x-1, y-1});
                            }
                            if ((x - 1 >= 0) && (y + 1 <= 7) && isMoveLegal(board, x, y, x - 1, y + 1)) {
                                moves.add(new int[]{x, y, x-1, y+1});
                            }
                        }
                        else {
                            if ((x + 1 <= 7) && (y - 1 >= 0) && isMoveLegal(board, x, y, x + 1, y - 1)) {
                                moves.add(new int[]{x, y, x+1, y-1});
                            }
                            if ((x + 1 <= 7) && (y + 1 <= 7) && isMoveLegal(board, x, y, x + 1, y + 1)) {
                                moves.add(new int[]{x, y, x+1, y+1});
                            }
                            if ((x - 1 >= 0) && (y - 1 >= 0) && isMoveLegal(board, x, y, x - 1, y - 1)) {
                                moves.add(new int[]{x, y, x-1, y-1});
                            }
                            if ((x - 1 >= 0) && (y + 1 <= 7) && isMoveLegal(board, x, y, x - 1, y + 1)) {
                                moves.add(new int[]{x, y, x-1, y+1});
                            }
                            //jumping
                            if ((x + 2 <= 7) && (y - 2 >= 0) && isMoveLegal(board, x, y, x + 2, y - 2)) {
                                moves.add(new int[]{x, y, x+2, y-2});
                            }
                            if ((x + 2 <= 7) && (y + 2 <= 7) && isMoveLegal(board, x, y, x + 2, y + 2)) {
                                moves.add(new int[]{x, y, x+2, y+2});
                            }
                        }
                        //Shared Jumping part
                        if ((x - 2 >= 0) && (y - 2 >= 0) && isMoveLegal(board, x, y, x - 2, y - 2)) {
                            moves.add(new int[]{x, y, x-2, y-2});
                        }
                        if ((x - 2 >= 0) && (y + 2 <= 7) && isMoveLegal(board, x, y, x - 2, y + 2)) {
                            moves.add(new int[]{x, y, x-2, y+2});
                        }
                    }
                }
            }
            return moves;
        }
        public static boolean isCapture(int[] moves){
            //the order is row1, col1, row2, col2 in terms of index
            return (moves[0]+2==moves[2] || moves[0]-2==moves[2] ) && (moves[1]+2==moves[3]  || moves[1]-2==moves[3]);
        }

        //some helper methods for evaluatePosition, red then black to avoid code duplication
        //list goes red then black
        private int @NotNull [] countAll(Piece[][] board) {
            int red = 0;
            int black = 0;
            for (int x = 0; x < 8; x++) {
                for (int y = 0; y < 8; y++) {
                    if (board[x][y] != null) {
                        if (board[x][y].isBlack) {
                            black++;
                        }
                        else {
                            red++;
                        }
                    }
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
                    if (board[x][y].isBlack) {
                        if (board[x][y].isKing() || (!board[x][y].isKing() && x == 7))
                            black += 5;
                        else
                            black += 3;
                    } else {
                        if (board[x][y].isKing() || (!board[x][y].isKing() && x == 0))
                            red += 5;
                        else
                            red += 3;
                    }

                }
            }
            return new int[]{red, black};
        }
    }

    @FXML
    AnchorPane popUp, PieceAnchorPane, pleaseWork;
    @FXML
    Text finalText;

    private double[] eval;

    private int rowInput1, colInput1;
    private Piece[][] MainBoard = new Piece[8][8];
    boolean lockMove = false;
    private final int[] done = new int[4];
    private int numberOfMoves = 0;
    private final int[] botLastMove = new int[4];
    private boolean  botMulti = false;


    public void initialize() {
        rowInput1 = -1;
        colInput1 = -1;
        MainBoard = new Piece[8][8];
        lockMove = false;
        popUp.setVisible(false);
        setStarterBoard();
        displayBoard();
    }

    //This method is the outer shell of the algorithm and is responsible
    //for finding the best move, which is then relayed to startAI
    private int[] startAI(Piece[][] board) {
        List<int[]> allMovesBlack = Algorithm.findAllBlackMoves(MainBoard);
        Piece[][] tempBoard = new Piece[8][8];
        int[] finished = new int[4];
        int index;
        eval = new double[allMovesBlack.size()];

        try {
            ExecutorService es = Executors.newCachedThreadPool();
            int tempLoopStore = allMovesBlack.size();
            numberOfMoves = allMovesBlack.size() + Algorithm.findAllRedMoves(MainBoard).size();

            for (int x = 0; x < tempLoopStore; x++) {

                for (int z = 0; z < 8; z++)
                    System.arraycopy(board[z], 0, tempBoard[z], 0, board.length);
                //Copying the array for last move and qol
                int[] temp = new int[4];
                System.arraycopy(allMovesBlack.get(x), 0, temp, 0, 4);

                //If last move was a capture, and if I could capture after last move with
                //the same piece, but my next move isn't a capture, skip it.
                //Basically forced capture for multiple jumps
                if(botMulti && Algorithm.isCapture(botLastMove) && (temp[0] != botLastMove[2] || temp[1] != botLastMove[3])){
                    continue;
                }

                movePiece(tempBoard, temp[0], temp[1], temp[2], temp[3]);
                Algorithm myThread;

                //Forcing multi-capture rules

                // Checks if last move was a capture and if I can capture again/ for chain captures
                boolean wasLastMoveCap = Algorithm.isCapture(temp);
                if (wasLastMoveCap && canCapture(tempBoard, temp[2], temp[3]))
                    myThread = new Algorithm(tempBoard, true, x, true, temp);
                else
                    myThread = new Algorithm(tempBoard, false, x, false, temp);

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
        index = highest;


        if (allMovesBlack.size() > 0) {
            System.arraycopy(allMovesBlack.get(index), 0, finished, 0, 4);
            System.arraycopy(allMovesBlack.get(index), 0, botLastMove, 0, 4);
        }
        return finished;
    }
    //This is the method that runs the basics/outer layer of the algorithm
    public synchronized void algoShell() {
        Task<Void> sleeper = new Task<>() {
            @Override
            protected Void call() {
                try {
                    Thread.sleep(250);
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

            System.out.println(((System.nanoTime() - startTime) / 1000000) + " - " + numberOfMoves);


            movePiece(MainBoard, done[0], done[1], done[2], done[3]);

            //Necessary to force multi-capture
            botMulti = canCapture(MainBoard, done[2], done[3]);
            if ((done[1] + 2 == done[3] || done[1] - 2 == done[3]) && canCapture(MainBoard, done[2], done[3])) {
                algoShell();
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
                        algoShell();
                    }
                }
            }
            displayBoard();
            checkWinner();
        });
        new Thread(sleeper).start();
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
        List<int[]> blackMoves = Algorithm.findAllBlackMoves(MainBoard);
        List<int[]> redMoves = Algorithm.findAllRedMoves(MainBoard);
        int[] scores = getScores(MainBoard);
        if (scores[0] == 0 || (!blackMoves.isEmpty() && redMoves.isEmpty())) {
            winner("B");
        } else if (scores[1] == 0 || (blackMoves.isEmpty() && !redMoves.isEmpty())) {
            winner("R");
        }
    }
    private void winner(String s) {
        popUp.setVisible(true);
        lockMove = true;
        if (s.equals("R")) {
            finalText.setText("You Win!");
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
                            algoShell();
                        }
                    } else if (moved) {
                        algoShell();
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

    private static boolean isMoveLegal(Piece[][] board, int rowInput1, int colInput1, int rowInput2, int colInput2) {
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
        //logic for Black
        if (board[rowInput1][colInput1].isBlack) {
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
        //logic for Red
        else{
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
                if (MainBoard[row][col] == null) {
                    continue;
                }
                Circle circle;
                boolean isBlack = MainBoard[row][col].isBlack;
                if(!isBlack) {
                    circle = new Circle(31.875, Color.RED);
                    circle.setStyle("-fx-stroke: black; -fx-strokeType: inside");
                } else {
                    circle = new Circle(31.875, Color.BLACK);
                }
                circle.setCenterX((col * 75) + 38.5);
                circle.setCenterY((row * 75) + 37.5);
                PieceAnchorPane.getChildren().add(circle);
                if (MainBoard[row][col].isKing()) {
                    Text king = new Text(col * 75, row * 75, "K");
                    king.setLayoutX(29);
                    king.setLayoutY(48);
                    king.setFont(new Font(30));
                    if(isBlack) {
                        king.setFill(Color.WHITE);
                    }
                    PieceAnchorPane.getChildren().add(king);
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

    //To be honest I don't know what this does, but it sure is here
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