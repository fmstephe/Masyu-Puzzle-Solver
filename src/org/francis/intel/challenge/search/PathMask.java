package org.francis.intel.challenge.search;

public class PathMask implements Constants {
    
    byte[] pathMask;
    byte[] board;
    int width;
    int height;
    
    public PathMask(byte[] board, int width, int height) {
        this.board = board;
        this.height = height;
        this.width = width;
        this.pathMask = initPathMask();
    }
    
    private byte[] initPathMask() {
        byte[] pathMask = new byte[board.length];
        for (int i = 0; i < pathMask.length; i++) {
            byte forbidden = 0;
            forbidden = i < width ? (byte)(forbidden | NOT_UP) : forbidden;
            forbidden = (i % width) == 0 ? (byte)(forbidden | NOT_LEFT) : forbidden;
            forbidden = (i % width) == width-1 ? (byte)(forbidden | NOT_RIGHT) : forbidden;
            forbidden = i >= width*(height-1) ? (byte)(forbidden | NOT_DOWN) : forbidden;
            pathMask[i] = (byte)(EMPTY_PATH | forbidden);
        }
        return pathMask;
    }
}
