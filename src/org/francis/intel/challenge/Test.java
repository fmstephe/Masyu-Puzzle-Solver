package org.francis.intel.challenge;

import org.francis.intel.challenge.search.MasyuSearcher;

public class Test {

    public static void main(String[] args) {
        int height = 8;
        int width = 8;
        int[] board = makeBoard(height,width);
        MasyuSearcher searcher = new MasyuSearcher(height, width, board);
        searcher.search();
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