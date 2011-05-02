package org.francis.intel.challenge;

import java.util.Random;

public class Test {

    public static void main(String[] args) {
        int height = 5;
        int width = 5;
        byte[] board = makeBoard(height,width);
        MasyuSearchState searcher = new MasyuSearchState(height, width, board);
        searcher.search();
    }

    private static byte[] makeBoard(int height, int width) {
        byte[] board = new byte[height*width];
        for (int i = 0; i < board.length; i++) {
            board[0] = 0;
        }
        Random rnd = new Random();
        board[5] = MasyuSearchState.WHITE;
        board[11] = MasyuSearchState.WHITE;
        board[12] = MasyuSearchState.WHITE;
        board[13] = MasyuSearchState.WHITE;
        board[20] = MasyuSearchState.BLACK;
        return board;
    }
}