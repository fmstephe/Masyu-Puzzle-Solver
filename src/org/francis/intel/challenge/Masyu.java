package org.francis.intel.challenge;

import java.io.File;

import org.francis.intel.challenge.ProblemReader.PuzzleData;
import org.francis.intel.challenge.search.MasyuSearcher;

public class Masyu {

    public static void main(String[] args) {
        String inFileA = args[0];
        String outFileA = args[1];
        File inFile = new File(inFileA);
        File[] problemFiles = inFile.listFiles();
        for (File problem : problemFiles) {
            PuzzleData puzzle = ProblemReader.parseDimacsFile(problem);
            int[] board = makeBoard(puzzle);
            MasyuSearcher searcher = new MasyuSearcher(puzzle.height, puzzle.width, board);
            long startTime = System.currentTimeMillis();
            String result = searcher.search();
            System.out.println("Total Time : "+((double)System.currentTimeMillis()-startTime)/1000);
            ProblemReader.writeSolution(new File(outFileA+"/"+problem.getName()), result);
        }
    }
    
    private static int[] makeBoard(PuzzleData puzzle) {
        int height = puzzle.height;
        int width = puzzle.width;
        int[] board = new int[height*width];
        for (int i = 0; i < puzzle.blackPebbles.size(); i+=2) {
            int row = puzzle.blackPebbles.get(i);
            int col = puzzle.blackPebbles.get(i+1);
            board[(row*width)+col] = MasyuSearcher.BLACK;
        }
        for (int i = 0; i < puzzle.whitePebbles.size(); i+=2) {
            int row = puzzle.whitePebbles.get(i);
            int col = puzzle.whitePebbles.get(i+1);
            board[(row*width)+col] = MasyuSearcher.WHITE;
        }
        return board;
    }
}
