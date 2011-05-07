package org.francis.intel.challenge;

import java.util.Arrays;

import org.francis.intel.challenge.ProblemReader.PuzzleData;

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
         int[] pebbles = Masyu.recordPebbles(puzzle,board);
         int[][] nearestPebbleMatrix = Masyu.pebblesByClosestDistance(pebbles, puzzle.width);
         SMPThreadedMasyuSolverFactory factory = new SMPThreadedMasyuSolverFactory(puzzle.height, puzzle.width, board, pebbles, nearestPebbleMatrix);
         factory.createAndRunSolversLocal(1, 2, null);
     }
}