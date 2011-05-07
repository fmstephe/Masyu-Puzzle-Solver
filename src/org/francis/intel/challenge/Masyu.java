package org.francis.intel.challenge;

import java.io.File;
import java.util.Iterator;
import java.util.Random;

import org.francis.intel.challenge.ProblemReader.PuzzleData;
import org.francis.intel.challenge.search.MasyuSearcher;
import org.francis.p2p.worksharing.network.message.ResultMessage;
import org.francis.p2p.worksharing.smp.SMPMessageManager;

public class Masyu {
    
    public static void main(String[] args) throws InterruptedException {
        String inFileA = args[0];
        String outFileA = args[1];
        File inFile = new File(inFileA);
        File[] problemFiles = inFile.listFiles();
        for (File problem : problemFiles) {
            System.out.println(problem);
            PuzzleData puzzle = ProblemReader.parseDimacsFile(problem);
            int[] board = makeBoard(puzzle);
            int[] pebbles = recordPebbles(puzzle,board);
            int[][] nearestPebbleMatrix = pebblesByClosestDistance(pebbles, puzzle.width);
            SMPThreadedMasyuSolverFactory factory = new SMPThreadedMasyuSolverFactory(puzzle.height, puzzle.width, board, pebbles, nearestPebbleMatrix);
            long startTime = System.currentTimeMillis();
            SMPMessageManager messageManager = factory.createAndRunSolversLocal(8, 2, null);
            ResultMessage result = messageManager.receiveResultOrShutDown(60000);
            if (result != null) System.out.println(result.result);
            System.out.println("Total Time : " + ((double) System.currentTimeMillis() - startTime) / 1000);
            if (result != null) ProblemReader.writeSolution(new File(outFileA + "/" + problem.getName()), result.result.toString());
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

    public static int[] recordPebbles(PuzzleData puzzle, int[] board) {
        assert puzzle.blackPebbles.size()%2 == 0;
        assert puzzle.whitePebbles.size()%2 == 0;
        int[] pebbles = new int[(puzzle.blackPebbles.size() + puzzle.whitePebbles.size())/2];
        int idx = 0;
        Iterator<Integer> blackItr = puzzle.blackPebbles.iterator();
        Iterator<Integer> whiteItr = puzzle.whitePebbles.iterator();
        Random rnd = new Random();
        while (true) {
            Iterator<Integer> itr = null;
            if (rnd.nextBoolean()) {
                if (blackItr.hasNext()) {
                    itr = blackItr;
                }
                else {
                    itr = whiteItr;
                }
            }
            else {
                if (whiteItr.hasNext()) {
                    itr = whiteItr;
                }
                else {
                    itr = blackItr;
                }
            }
            if (!itr.hasNext())
                break;
            int row = itr.next();
            int col = itr.next();
            int pos = (row * puzzle.width) + col;
            board[pos] |= idx;
            pebbles[idx++] = pos;
        }
        assert idx == pebbles.length;
        return pebbles;
    }
    
    public static void mainTestI(String[] args) throws InterruptedException {
        int[] pebbles = new int[2000];
        Random rnd = new Random();
        for (int i = 0; i < pebbles.length; i++) {
            pebbles[i] = rnd.nextInt(10000);
        }
        long currentTime = System.currentTimeMillis();
        pebblesByClosestDistance(pebbles,100);
        System.out.println(""+(float)(System.currentTimeMillis()-currentTime)/1000);
    }
    
    public static void mainTestII(String[] args) throws InterruptedException {
        int width = 5;
        int[] pebbles = new int[]{11,8,3,23,13,5};
        pebblesByClosestDistance(pebbles, width);
    }

    public static int[][] pebblesByClosestDistance(int[] pebbles, int width) throws InterruptedException {
        int[][] pebblesByClosestDistance = new int[pebbles.length][];
        int[] idxArray = new int[pebbles.length];
        for (int i = 0; i < idxArray.length; i++) {
            idxArray[i] = i;
        }
        Thread[] workers = new Thread[80];
        for (int i = 0; i < workers.length; i++) {
            Worker worker = new Worker(pebblesByClosestDistance, pebbles, idxArray, width, i, workers.length);
            Thread thread = new Thread(worker);
            workers[i] = thread;
        }
        for (int i = 0; i < workers.length; i++) {
            workers[i].start();
        }
        for (int i = 0; i < workers.length; i++) {
            workers[i].join();
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
        if (comp[src[mid - 1]] <= comp[src[mid]]) {
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
    
    private static class Worker implements Runnable {

        final int[][] pebblesByClosestDistance;
        final int[] pebbles;
        final int[] idxArray;
        final int width;
        final int offset;
        final int inc;
        
        public Worker(int[][] pebblesByClosestDistance, int[] pebbles, int[] idxArray, int width, int offset, int inc) {
            super();
            this.pebblesByClosestDistance = pebblesByClosestDistance;
            this.pebbles = pebbles;
            this.idxArray = idxArray;
            this.width = width;
            this.offset = offset;
            this.inc = inc;
        }

        @Override
        public void run() {
            final float[] distanceArray = new float[pebbles.length];
            for (int i = offset; i < pebbles.length; i+=inc) {
                for (int j = 0; j < pebbles.length; j++) {
                    // The distances for i
                    distanceArray[j] = calculateDistance(pebbles[i], pebbles[j], width);
                }
                assert distanceArray[i] == 0;
                int[] idxArrayII = idxArray.clone();
                int[] idxArraySorted = idxArray.clone();
                mergeSort(idxArrayII,idxArraySorted,distanceArray,0,distanceArray.length,0);
                pebblesByClosestDistance[i] = idxArraySorted;
                assert idxArraySorted[0] == i;
            }
        }
    }
}