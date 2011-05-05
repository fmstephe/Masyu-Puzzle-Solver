package org.francis.intel.challenge;

import org.francis.intel.challenge.search.MasyuSearcher;

public class Test {

    // public static void main(String[] args) {
    // int height = 5;
    // int width = 5;
    // int[] board = makeBoard(height,width);
    // SMPThreadedMasyuSolverFactory factory = new
    // SMPThreadedMasyuSolverFactory(height, width, board);
    // factory.createAndRunSolversLocal(1, 2, null);
    // }

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

    // 3-way string quicksort a[lo..hi] starting at dth character
    private static void sort(String[] a, int lo, int hi, int d) {

        int lt = lo, gt = hi;
        int v = charAt(a[lo], d);
        int i = lo + 1;
        while (i <= gt) {
            int t = charAt(a[i], d);
            if (t < v)
                exch(a, lt++, i++);
            else if (t > v)
                exch(a, i, gt--);
            else
                i++;
        }

        // a[lo..lt-1] < v = a[lt..gt] < a[gt+1..hi].
        sort(a, lo, lt - 1, d);
        if (v >= 0)
            sort(a, lt, gt, d + 1);
        sort(a, gt + 1, hi, d);
    }

    // sort from a[lo] to a[hi], starting at the dth character
    private static void insertion(String[] a, int lo, int hi, int d) {
        for (int i = lo; i <= hi; i++)
            for (int j = i; j > lo && less(a[j], a[j - 1], d); j--)
                exch(a, j, j - 1);
    }

    // exchange a[i] and a[j]
    private static void exch(String[] a, int i, int j) {
        String temp = a[i];
        a[i] = a[j];
        a[j] = temp;
    }

    // is v less than w, starting at character d
    private static boolean less(String v, String w, int d) {
        assert v.substring(0, d).equals(w.substring(0, d));
        return v.substring(d).compareTo(w.substring(d)) < 0;
    }

    // is the array sorted
    private static boolean isSorted(String[] a) {
        for (int i = 1; i < a.length; i++)
            if (a[i].compareTo(a[i - 1]) < 0)
                return false;
        return true;
    }

    public static void main(String[] args) {

        // read in the strings from standard input
        String[] a = args;
        int N = a.length;

        // sort the strings
        sort(a);

        // print the results
        for (int i = 0; i < N; i++)
            System.out.println(a[i]);
    }
}