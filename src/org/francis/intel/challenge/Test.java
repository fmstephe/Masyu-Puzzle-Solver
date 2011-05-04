package org.francis.intel.challenge;

import org.francis.intel.challenge.search.MasyuSearcher;

public class Test {

    public static void main(String[] args) {
        int height = 8;
        int width = 8;
        int[] board = makeBoard(height,width);
        SMPThreadedMasyuSolverFactory factory = new SMPThreadedMasyuSolverFactory(height, width, board);
        factory.createAndRunSolversLocal(1, 2, null);
    }

    private static int[] makeBoard(int height, int width) {
        int[] board = new int[height*width];
        for (int i = 0; i < board.length; i++) {
            board[0] = 0;
        }
        board[59] = MasyuSearcher.WHITE;
        board[60] = MasyuSearcher.WHITE;
        board[62] = MasyuSearcher.WHITE;
        return board;
    }
}