package org.francis.intel.challenge;

import java.io.File;

import org.francis.intel.challenge.ProblemReader.PuzzleData;
import org.francis.intel.challenge.search.MasyuSearcher;
import org.francis.p2p.worksharing.network.message.ResultMessage;
import org.francis.p2p.worksharing.smp.SMPMessageManager;

public class Masyu {

    public static void main(String[] args) {
        String inFileA = args[0];
        String outFileA = args[1];
        File inFile = new File(inFileA);
        File[] problemFiles = inFile.listFiles();
        for (File problem : problemFiles) {
            PuzzleData puzzle = ProblemReader.parseDimacsFile(problem);
            int[] board = makeBoard(puzzle);
            SMPThreadedMasyuSolverFactory factory = new SMPThreadedMasyuSolverFactory(puzzle.height, puzzle.width, board);
            long startTime = System.currentTimeMillis();
            SMPMessageManager messageManager = factory.createAndRunSolversLocal(1, 2, null);
            ResultMessage result = messageManager.receiveResultOrShutDown(1000000000l);
            System.out.println(result.result);
            System.out.println("Total Time : "+((double)System.currentTimeMillis()-startTime)/1000);
            ProblemReader.writeSolution(new File(outFileA+"/"+problem.getName()), result.result.toString());
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
