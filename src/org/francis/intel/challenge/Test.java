package org.francis.intel.challenge;

import java.util.Random;

import org.francis.intel.challenge.search.MasyuSearcher;

public class Test {

    public static void main(String[] args) {
        int height = 5;
        int width = 5;
        byte[] board = makeBoard(height,width);
        MasyuSearcher searcher = new MasyuSearcher(height, width, board);
        searcher.search();
    }

    private static byte[] makeBoard(int height, int width) {
        byte[] board = new byte[height*width];
        for (int i = 0; i < board.length; i++) {
            board[0] = 0;
        }
        Random rnd = new Random();
        board[5] = MasyuSearcher.WHITE;
        board[11] = MasyuSearcher.WHITE;
        board[12] = MasyuSearcher.WHITE;
        board[13] = MasyuSearcher.WHITE;
        board[20] = MasyuSearcher.BLACK;
        return board;
    }
}