package org.francis.intel.challenge;

import org.francis.intel.challenge.search.MasyuSearcher;

public class Test {

    public static void main(String[] args) {
        int height = 3;
        int width = 3;
        int[] board = makeBoard(height,width);
        SMPThreadedMasyuSolverFactory factory = new SMPThreadedMasyuSolverFactory(height, width, board);
        factory.createAndRunSolversLocal(1, 2, null);
    }

    private static int[] makeBoard(int height, int width) {
        int[] board = new int[height*width];
        for (int i = 0; i < board.length; i++) {
            board[0] = 0;
        }
        board[4] = MasyuSearcher.BLACK;
//        board[14] = MasyuSearcher.WHITE;
//        board[7] = MasyuSearcher.WHITE;
//        board[13] = MasyuSearcher.WHITE;
        return board;
    }
}