package org.francis.intel.challenge;

import java.io.File;

import org.francis.intel.challenge.ProblemReader.PuzzleData;
import org.francis.intel.challenge.search.MasyuSearcher;
import org.francis.p2p.worksharing.network.message.ResultMessage;
import org.francis.p2p.worksharing.smp.SMPMessageManager;

public class Masyu {
    
    public static void mainII(String[] args) {
        String inFileA = args[0];
        String outFileA = args[1];
        File inFile = new File(inFileA);
        File[] problemFiles = inFile.listFiles();
        for (File problem : problemFiles) {
            PuzzleData puzzle = ProblemReader.parseDimacsFile(problem);
            int[] board = makeBoard(puzzle);
            int[] pebbles = recordPebbles(puzzle);
            int[][] nearestPebbleMatrix = pebblesByClosestDistance(pebbles, puzzle.width);
            SMPThreadedMasyuSolverFactory factory = new SMPThreadedMasyuSolverFactory(puzzle.height, puzzle.width, board, pebbles, nearestPebbleMatrix);
            long startTime = System.currentTimeMillis();
            SMPMessageManager messageManager = factory.createAndRunSolversLocal(1, 2, null);
            ResultMessage result = messageManager.receiveResultOrShutDown(1000000000l);
            System.out.println(result.result);
            System.out.println("Total Time : " + ((double) System.currentTimeMillis() - startTime) / 1000);
            ProblemReader.writeSolution(new File(outFileA + "/" + problem.getName()), result.result.toString());
        }
    }

    public static int[] makeBoard(PuzzleData puzzle) {
        int height = puzzle.height;
        int width = puzzle.width;
        int[] board = new int[height * width];
        for (int i = 0; i < puzzle.blackPebbles.size(); i += 2) {
            int row = puzzle.blackPebbles.get(i);
            int col = puzzle.blackPebbles.get(i + 1);
            board[(row * width) + col] = MasyuSearcher.BLACK;
        }
        for (int i = 0; i < puzzle.whitePebbles.size(); i += 2) {
            int row = puzzle.whitePebbles.get(i);
            int col = puzzle.whitePebbles.get(i + 1);
            board[(row * width) + col] = MasyuSearcher.WHITE;
        }
        return board;
    }

    public static int[] recordPebbles(PuzzleData puzzle) {
        int[] pebbles = new int[puzzle.blackPebbles.size() + puzzle.whitePebbles.size()];
        for (int i = 0; i < puzzle.whitePebbles.size(); i++) {
            int row = puzzle.blackPebbles.get(i * 2);
            int col = puzzle.blackPebbles.get((i * 2) + 1);
            pebbles[i] = (row * puzzle.width) + col;
        }
        for (int i = 0; i < puzzle.whitePebbles.size(); i++) {
            int row = puzzle.whitePebbles.get(i * 2);
            int col = puzzle.whitePebbles.get((i * 2) + 1);
            pebbles[i] = (row * puzzle.width) + col;
        }
        return pebbles;
    }

    public static int[][] pebblesByClosestDistance(int[] pebbles, int width) {
        int[][] pebblesByClosestDistance = new int[pebbles.length][];
        final float[] distanceArray = new float[pebbles.length];
        int[] idxArray = new int[pebbles.length];
        for (int i = 0; i < idxArray.length; i++) {
            idxArray[i] = i;
        }
        for (int i = 0; i < pebbles.length; i++) {
            for (int j = 0; j < pebbles.length; j++) {
                // The distances for i
                distanceArray[j] = calculateDistance(pebbles[i], pebbles[j], width);
                int[] idxArrayII = idxArray.clone();
                int[] idxArraySorted = idxArray.clone();
                mergeSort(idxArrayII,idxArraySorted,distanceArray,0,distanceArray.length,0);
                pebblesByClosestDistance[i] = idxArraySorted;
            }
        }
        return pebblesByClosestDistance;
    }

    private static float calculateDistance(int i, int j, int width) {
        float xi = i % width;
        float yi = i / width;
        float xj = j % width;
        float yj = j / width;
        return (float) Math.sqrt(Math.pow(xi - xj, 2) + Math.pow(yi - yj, 2));
    }

    private static void mergeSort(int[] src, int[] dest, float[] comp, int low, int high, int off) {
        int length = high - low;
        // Insertion sort on smallest arrays
        if (length < 7) {
            for (int i = low; i < high; i++)
                for (int j = i; j > low && comp[dest[j - 1]] > comp[dest[j]]; j--)
                    swap(dest, j, j - 1);
            return;
        }
        // Recursively sort halves of dest into src
        int destLow = low;
        int destHigh = high;
        low += off;
        high += off;
        int mid = (low + high) >>> 1;
        mergeSort(dest, src, comp, low, mid, -off);
        mergeSort(dest, src, comp, mid, high, -off);

        // If list is already sorted, just copy from src to dest. This is an
        // optimization that results in faster sorts for nearly ordered lists.
        if (src[mid - 1] <= src[mid]) {
            System.arraycopy(src, low, dest, destLow, length);
            return;
        }

        // Merge sorted halves (now in src) into dest
        for (int i = destLow, p = low, q = mid; i < destHigh; i++) {
            if (q >= high || p < mid && comp[src[p]] <= comp[src[q]])
                dest[i] = src[p++];
            else
                dest[i] = src[q++];
        }
    }

    private static void swap(int[] x, int a, int b) {
        int t = x[a];
        x[a] = x[b];
        x[b] = t;
    }
}