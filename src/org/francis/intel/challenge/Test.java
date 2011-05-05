package org.francis.intel.challenge;

import java.util.Arrays;

import org.francis.intel.challenge.ProblemReader.PuzzleData;
import org.francis.intel.challenge.search.MasyuSearcher;
import org.francis.p2p.worksharing.smp.SMPMessageManager;

public class Test {

     public static void main(String[] args) throws InterruptedException {
         PuzzleData puzzle = new PuzzleData();
         Integer[] whitePebbles = new Integer[]{1,3};
         Integer[] blackPebbles = new Integer[]{3,2};
         puzzle.height = 4;
         puzzle.width = 4;
         puzzle.blackPebbles = Arrays.asList(blackPebbles);
         puzzle.whitePebbles = Arrays.asList(whitePebbles);
         int[] board = Masyu.makeBoard(puzzle);
         int[] pebbles = Masyu.recordPebbles(puzzle);
         int[][] nearestPebbleMatrix = Masyu.pebblesByClosestDistance(pebbles, puzzle.width);
         SMPThreadedMasyuSolverFactory factory = new SMPThreadedMasyuSolverFactory(puzzle.height, puzzle.width, board, pebbles, nearestPebbleMatrix);
         long startTime = System.currentTimeMillis();
         SMPMessageManager messageManager = factory.createAndRunSolversLocal(1, 2, null);
     }

    private static int[] makeBoard(int height, int width) {
        int[] board = new int[height * width];
        for (int i = 0; i < board.length; i++) {
            board[0] = 0;
        }
        board[4] = MasyuSearcher.BLACK;
        board[14] = MasyuSearcher.WHITE;
        board[7] = MasyuSearcher.WHITE;
        board[13] = MasyuSearcher.WHITE;
        return board;
    }
}