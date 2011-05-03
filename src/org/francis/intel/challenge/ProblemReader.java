package org.francis.intel.challenge;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

public class ProblemReader {

    public static PuzzleData parseDimacsFile(File file) {
        LineNumberReader in = null;
        try {
            in = new LineNumberReader(new InputStreamReader(new FileInputStream(file)));
            PuzzleData puzzle = new PuzzleData();
            String dimLine = in.readLine();
            String[] dimA = dimLine.split(" ");
            puzzle.height = Integer.parseInt(dimA[0]);
            puzzle.width = Integer.parseInt(dimA[1]);
            readPebbles(in,puzzle);
            readPebbles(in,puzzle);
            return puzzle;
        } catch (Exception e) {
            String workingDir = System.getProperty("user.dir");
            System.out.println("Failed to parse "+workingDir+"\\"+file);
            e.printStackTrace();
            return null;
        }
        finally {
            if (in != null)
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    private static void readPebbles(LineNumberReader in, PuzzleData puzzle) throws IOException {
        String pebbleIndicator = in.readLine();
        if (pebbleIndicator == null) return;
        boolean isWhite = pebbleIndicator.trim().equals("W");
        List<Integer> fillingList = null;
        if (isWhite)
            fillingList = puzzle.whitePebbles;
        else
            fillingList = puzzle.blackPebbles;
        OUTER_LOOP:
        while (true) {
            String coords = in.readLine();
            String[] coordsA = coords.split(" ");
            for (int i = 0; i < coordsA.length; i+=2) {
                int row = Integer.parseInt(coordsA[i]);
                int col = Integer.parseInt(coordsA[i+1]);
                if (row == 0 && col == 0) break OUTER_LOOP;
                fillingList.add(row-1);
                fillingList.add(col-1);
            }
        }
    }
    
    public static class PuzzleData {
        public int width;
        public int height;
        public List<Integer> blackPebbles;
        public List<Integer> whitePebbles;
        
        public PuzzleData() {
            blackPebbles = new ArrayList<Integer>();
            whitePebbles = new ArrayList<Integer>();
        }
    }
    
    public static void writeSolution(File outFile, String result) {
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(outFile));
            out.write(result);
        } catch (IOException ignore) {
        }
        finally {
            if (out != null)
                try {
                    out.close();
                } catch (IOException ignore) {
                }
        }
    }
}