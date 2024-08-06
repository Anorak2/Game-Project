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

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
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

        private SplittableRandom rand_generator = new SplittableRandom();

        public Algorithm(Piece[][] board, boolean isBlack, int num, boolean isMulti, int[] lastMove) {
            int tempLoopStore = board.length;
            for (int x = 0; x < tempLoopStore; x++)
                System.arraycopy(board[x], 0, boardT[x], 0, board[x].length);
            this.isBlackT = isBlack;
            this.num = num;
            this.isMulti = isMulti;
            this.lastMove = lastMove;
        }

        @Override
        public void run() {
            // 0 easy, 1 normal, 2 hard
            int [][] difficulty = {{5,6,7}, {6,7,8}, {7, 8, 9}};
            int mode = 2;
            if (numberOfMoves >= 21) {
                maxDepth = difficulty[mode][0];
            } else if (numberOfMoves > 13) {
                maxDepth = difficulty[mode][1];
            } else if (numberOfMoves > 8) {
                maxDepth = difficulty[mode][2];
            }

            ThreadMXBean mxbean = ManagementFactory.getThreadMXBean();
            long t1 = mxbean.getCurrentThreadCpuTime();
            eval[num] = findBestMove(boardT, isBlackT, 0, isMulti, lastMove);
            times[num] = mxbean.getCurrentThreadCpuTime() - t1;
        }

        //The recursive methods
        //The bane of my existence
        private synchronized double findBestMove(Piece[][] board, boolean isBlack, int depth, boolean isMulti, int[] lastMove) {
            Piece[][] tempBoard;
            int index;
            List<int[]> allMoves;
            Double[] scores;

            // The exit clause
            if (depth == maxDepth) {
                return evaluatePosition(board);
            }

            // Exit if the game is won or lost
            double check = simpleEvaluate(board, true);
            if (check != 0) {
                return check;
            }

            //---------- Main Logic ----------
            long startTime = System.nanoTime();
            if (isBlack) {
                allMoves = findAllBlackMoves(board);
            } else {
                allMoves = findAllRedMoves(board);
            }
            long endTime = System.nanoTime();
            findAll += (endTime - startTime);
            scores = new Double[allMoves.size()];
            int iteration = 0;

            tempBoard = new Piece[8][8];
            for (int[] ints : allMoves) {
                //Enforcing multi-capture rules
                if (isMulti && (ints[0] != lastMove[2] || ints[1] != lastMove[3])) {
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
                } else {
                    scores[iteration] = findBestMove(tempBoard, !isBlack, depth + 1, false, ints);
                }
                iteration++;
            }

            //---------- Returning optimal move ----------
            int highest = 0, lowest = 0;
            int loopStorage = scores.length;
            for (int x = 1; x < loopStorage; x++) {
                if (scores[x] != null && scores[x] != 929274.001) {
                    if (scores[x] > scores[highest]) {
                        highest = x;
                    } else if (scores[x] < scores[lowest]) {
                        lowest = x;
                    }
                }
            }
            if (isBlack)
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
            long evalStartTime = System.nanoTime();
            long startTime = System.nanoTime();
            double simpleEval = simpleEvaluate(board, false);
            if (simpleEval != 0)
                return simpleEval;
            long endTime = System.nanoTime();
            simpleEvalTime += endTime - startTime;

            //Red then black
            startTime = System.nanoTime();
            int[] scores = getScores(board);
            double positionScoreForBlack = 0;
            endTime = System.nanoTime();
            getScores += endTime - startTime;

            if(scores[0] == 0){
                return 999999;
            }
            else if (scores[1] == 0){
                return -999999;
            }

            // difference in score x10 to emphasize importance of score
            positionScoreForBlack += (scores[1] - scores[0]) * 4.5;


            // Random element
            startTime = System.nanoTime();
            positionScoreForBlack += (rand_generator.nextDouble() * 1) - .5;
            endTime = System.nanoTime();
            evalRand += endTime - startTime;
            endTime = System.nanoTime();
            evalPosition += endTime - evalStartTime;
            return positionScoreForBlack;
        }


        // These are extremely cut down methods which are used for efficiency
        private double simpleEvaluate(Piece[][] board, boolean check_pieces) {

            long startTime = System.nanoTime();
            //If black has no moves available
            if (!simpleBlackMoves(board)) {
                return -999999;
            }
            //if red has no moves available
            else if (!simpleRedMoves(board)) {
                return 999999;
            }
            long endTime = System.nanoTime();
            simpleMoves += endTime - startTime;

            if(!check_pieces) {
                return 0;
            }
            boolean[] pieceCount = simpleCount(board);

            //If there are no red pieces
            if (!pieceCount[0]) {
                return 999999;
            }
            // If there are no black pieces
            else if (!pieceCount[1]) {
                return -999999;
            }
            return 0;
        }
        private boolean[] simpleCount(Piece[][] board) {
            boolean red = false;
            boolean black = false;
            for (int x = 0; x < 8; x++) {
                for (int y = 0; y < 8; y++) {
                    if (board[x][y] == null) {
                        continue;
                    }
                    if (board[x][y].isBlack) {
                        black = true;
                    }
                    else{
                        red = true;
                    }
                    if(red && black) {
                        return new boolean[]{true, true};
                    }
                }
            }
            return new boolean[]{red, black};
        }
        public static boolean simpleBlackMoves(Piece[][] board) {
            for (int x = 0; x <= 7; x++) {
                for (int y = 0; y <= 7; y++) {
                    if (board[x][y] == null || !board[x][y].isBlack) {
                        continue;
                    }
                    return anyValidMoves(board, x, y);
                }
            }
            return false;
        }
        private static boolean simpleRedMoves(Piece[][] board) {
            for (int x = 7; x > -1; x--) {
                for (int y = 7; y > -1; y--) {
                    if (board[x][y] == null || board[x][y].isBlack) {
                        continue;
                    }
                    return anyValidMoves(board, x, y);
                }
            }
            return false;
        }


        //Finding moves, duh
        public static List<int[]> findAllBlackMoves(Piece[][] board) {
            //List<int[]> moves = new LinkedList<>();
            //Stack<int[]> moves = new Stack<>();
            List<int[]> moves = new ArrayList<>();

            for (int x = 7; x >= 0; x--) {
                for (int y = 7; y >= 0; y--) {
                    if (board[x][y] != null && board[x][y].isBlack) {
                        //regular pieces
                        if (!board[x][y].isKing()) {
                            if (isMoveLegal(board, x, y, x + 1, y - 1)) {
                                moves.add(new int[]{x, y, x+1, y-1});
                            }
                            if (isMoveLegal(board, x, y, x + 1, y + 1)) {
                                moves.add(new int[]{x, y, x+1, y+1});
                            }
                        }
                        //kings
                        else {
                            if (isMoveLegal(board, x, y, x + 1, y - 1)) {
                                moves.add(new int[]{x, y, x+1, y-1});

                            }
                            if (isMoveLegal(board, x, y, x + 1, y + 1)) {
                                moves.add(new int[]{x, y, x+1, y+1});

                            }
                            if (isMoveLegal(board, x, y, x - 1, y - 1)) {
                                moves.add(new int[]{x, y, x-1, y-1});
                            }
                            if (isMoveLegal(board, x, y, x - 1, y + 1)) {
                                moves.add(new int[]{x, y, x-1, y+1});

                            }
                            //jumping
                            if (isMoveLegal(board, x, y, x - 2, y - 2)) {
                                moves.add(new int[]{x, y, x-2, y-2});

                            }
                            if (isMoveLegal(board, x, y, x - 2, y + 2)) {
                                moves.add(new int[]{x, y, x-2, y+2});
                            }
                        }

                        //Shared Jumping
                        if (isMoveLegal(board, x, y, x + 2, y - 2)) {
                            moves.add(new int[]{x, y, x+2, y-2});
                        }
                        if (isMoveLegal(board, x, y, x + 2, y + 2)) {
                            moves.add(new int[]{x, y, x+2, y+2});

                        }
                    }
                }
            }
            return moves;
        }
        public static List<int[]> findAllRedMoves(Piece[][] board) {
            //List<int[]> moves = new LinkedList<>();
            //Stack<int[]> moves = new Stack<>();
            List<int[]> moves = new ArrayList<>();
            for (int x = 7; x > -1; x--) {
                for (int y = 7; y > -1; y--) {
                    if (board[x][y] != null && !board[x][y].isBlack) {
                        if (!board[x][y].isKing()) {
                            if (isMoveLegal(board, x, y, x - 1, y - 1)) {
                                moves.add(new int[]{x, y, x-1, y-1});
                            }
                            if (isMoveLegal(board, x, y, x - 1, y + 1)) {
                                moves.add(new int[]{x, y, x-1, y+1});
                            }
                        }
                        else {
                            if (isMoveLegal(board, x, y, x + 1, y - 1)) {
                                moves.add(new int[]{x, y, x+1, y-1});
                            }
                            if (isMoveLegal(board, x, y, x + 1, y + 1)) {
                                moves.add(new int[]{x, y, x+1, y+1});
                            }
                            if (isMoveLegal(board, x, y, x - 1, y - 1)) {
                                moves.add(new int[]{x, y, x-1, y-1});
                            }
                            if (isMoveLegal(board, x, y, x - 1, y + 1)) {
                                moves.add(new int[]{x, y, x-1, y+1});
                            }
                            //jumping
                            if (isMoveLegal(board, x, y, x + 2, y - 2)) {
                                moves.add(new int[]{x, y, x+2, y-2});
                            }
                            if (isMoveLegal(board, x, y, x + 2, y + 2)) {
                                moves.add(new int[]{x, y, x+2, y+2});
                            }
                        }
                        //Shared Jumping part
                        if (isMoveLegal(board, x, y, x - 2, y - 2)) {
                            moves.add(new int[]{x, y, x-2, y-2});
                        }
                        if (isMoveLegal(board, x, y, x - 2, y + 2)) {
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
    private long[] times;
    private long findAll = 0, simpleMoves = 1, evalPosition = 0, evalRand = 0, getScores = 0, simpleEvalTime = 0;


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
        times = new long[allMovesBlack.size()];


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


            int[] x = startAI(MainBoard);
            System.arraycopy(x, 0, done, 0, x.length);
            Arrays.sort(times);
            System.out.println("\n---------------------");
            System.out.println("Find all moves: " +  findAll / 1000000);
            System.out.println("SimpleMoves family: " +  simpleMoves / 1000000);
            System.out.println("Evaluating position: " +  evalPosition / 1000000);

            System.out.println("\nSimple Evaluate: " +  simpleEvalTime / 1000000);
            System.out.println("Getting Scores: " +  getScores / 1000000);
            System.out.println("Random Element: " +  evalRand / 1000000);

            System.out.println("\nShortest thread: " + times[0] / 1000000);
            System.out.println("Longest thread: " + times[times.length-1] / 1000000);
            System.out.println("---------------------\n");


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
        //System.out.println("row: " + row + "  col:" + col);
        move(row, col);
    }

    private static boolean isMoveLegal(Piece[][] board, int rowInput1, int colInput1, int rowInput2, int colInput2) {
        //bounds detection for first set of inputs
        if (rowInput1 < 0 || rowInput1 > 7 || colInput1 < 0 || colInput1 > 7 ) {
            return false;
        }
        //bounds detection for second set of inputs
        if (rowInput2 < 0 || rowInput2 > 7 || colInput2 < 0 || colInput2 > 7 ) {
            return false;
        }
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

    private static boolean anyValidMoves(Piece[][] board, int rowInput1, int colInput1) {
        //bounds detection for first set of inputs
        if (rowInput1 < 0 || rowInput1 > 7 || colInput1 < 0 || colInput1 > 7 ) {
            return false;
        }
        //protecting from nulls
        if (board[rowInput1][colInput1] == null)
            return false;
        boolean PieceIsBlack = board[rowInput1][colInput1].isBlack;

        //logic for king
        if (board[rowInput1][colInput1].isKing()) {
            if ( rowInput1 - 1 >= 0 && colInput1 + 1 < 8 && board[rowInput1 - 1][colInput1 + 1] == null) {
                return true;
            } else if (rowInput1 - 1 >= 0 && colInput1 - 1 >= 0 && board[rowInput1 - 1][colInput1 - 1] == null) {
                return true;
            } else if (rowInput1 + 1 < 8 && colInput1 - 1 >= 0 && board[rowInput1 + 1][colInput1 - 1] == null) {
                return true;
            } else if (rowInput1 + 1 < 8 && colInput1 + 1 < 8 && board[rowInput1 + 1][colInput1 + 1] == null) {
                return true;
            } else {
                //Jumping Logic
                if (rowInput1 - 2 >= 0 && colInput1 + 2 < 8 && board[rowInput1 - 1][colInput1 + 1] != null && !board[rowInput1 - 1][colInput1 + 1].isBlack == PieceIsBlack) {
                    return true;
                } else if (rowInput1 - 2 >= 0 && colInput1 - 2 >= 0 && board[rowInput1 - 1][colInput1 - 1] != null && !board[rowInput1 - 1][colInput1 - 1].isBlack == PieceIsBlack) {
                    return true;
                } else if (rowInput1 + 2 < 8 && colInput1 + 2 < 8 && board[rowInput1 + 1][colInput1 + 1] != null && !board[rowInput1 + 1][colInput1 + 1].isBlack == PieceIsBlack) {
                    return true;
                } else if (rowInput1 + 2 < 8  && colInput1 - 2 >= 0 && board[rowInput1 + 1][colInput1 - 1] != null && !board[rowInput1 + 1][colInput1 - 1].isBlack == PieceIsBlack) {
                    return true;
                }
            }
        }
        //logic for Black
        if (PieceIsBlack) {
            //Checking to move diagonally forward one space
            if (rowInput1 + 1 < 8 && colInput1 - 1 > -1 && (board[rowInput1 + 1][colInput1 - 1] == null)) {
                return true;
            } else if (rowInput1 + 1 < 8 && colInput1 + 1 < 8 && (board[rowInput1 + 1][colInput1 + 1] == null)) {
                return true;
            }
            //Jumping Logic
            else if (rowInput1 + 2 < 8 && colInput1 + 2 < 8 && board[rowInput1 + 1][colInput1 + 1] != null && !board[rowInput1 + 1][colInput1 + 1].isBlack) {
                return true;
            } else if (rowInput1 + 2 < 8 && colInput1 - 2 > -1 && board[rowInput1 + 1][colInput1 - 1] != null && !board[rowInput1 + 1][colInput1 - 1].isBlack) {
                return true;
            }
        }
        //logic for Red
        else{
            //Checking to move diagonally forward one space
            if (rowInput1 - 1 >= 0 && colInput1 - 1 >= 0 && (board[rowInput1 - 1][colInput1 - 1] == null)) {
                return true;
            } else if (rowInput1 - 1 >= 0 && colInput1 + 1 < 8 && (board[rowInput1 - 1][colInput1 + 1] == null)) {
                return true;
            }
            //Jumping Logic
            else if (rowInput1 - 2 >= 0 && colInput1 + 2 < 8 && board[rowInput1 - 1][colInput1 + 1] != null && board[rowInput1 - 1][colInput1 + 1].isBlack) {
                return true;
            } else if (rowInput1 - 2 >= 0 && colInput1 - 2 >= 0 && board[rowInput1 - 1][colInput1 - 1] != null && board[rowInput1 - 1][colInput1 - 1].isBlack) {
                return true;
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
    public List<Long> testOnce(){
        setStarterBoard();
        displayBoard();

        List<int[]> moveList = new ArrayList<>();
        List<Long> timeList = new ArrayList<>();

        // Adding the preset moves
        moveList.add(new int[]{5,4,4,3});
        moveList.add(new int[]{2,3,3,2});

        moveList.add(new int[]{6,5,5,4});
        moveList.add(new int[]{1,4,2,3});

        moveList.add(new int[]{4,3,3,4});
        moveList.add(new int[]{2,5,4,3});
        moveList.add(new int[]{4,3,6,5});

        moveList.add(new int[]{7,6,5,4});
        moveList.add(new int[]{2,3,3,4});

        int count = 0;
        boolean algoMove = false;
        for(int[] current : moveList){
            movePiece(MainBoard, current[0], current[1], current[2], current[3]);
            if(algoMove) {
                startAI(MainBoard);
                timeList.add(Arrays.stream(times).sum() / 1000000);
            }

            if (count != 4) {
                algoMove = !algoMove;
            }
            count++;
        }
        displayBoard();
        return timeList;
    }

    public void unitTests(){
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("done waiting");

        List<List<Long>> times = new ArrayList<>();

        for(int x = 0; x < 5; x++) {
            times.add(testOnce());
        }

        for(List<Long> currentList : times){
            for(Long current : currentList){
                System.out.println(current);
            }
            long sum = 0;
            for (Long aLong : currentList) {
                sum += aLong;
            }
            System.out.println("\n"+sum+"\n\n");
        }
    }
}
