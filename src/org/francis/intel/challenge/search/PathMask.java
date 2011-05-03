package org.francis.intel.challenge.search;

import org.francis.intel.challenge.stack.ByteStack;
import org.francis.intel.challenge.stack.IntStack;
import org.francis.intel.challenge.stack.ResizingIntStack;

public class PathMask implements Constants {
    
    private final byte[] pathMaskA;
    private final byte[] boardA;
    private final int[] pebbles;
    public final int totalSqrs;
    public final int width;
    public final int height;
    public final int sPos;
    boolean triviallyUnsolvable;
    
    public PathMask(byte[] boardA, int width, int height) {
        this.boardA = boardA;
        this.width = width;
        this.height = height;
        this.totalSqrs = boardA.length;
        int pebbleCount = 0;
        for (int i = 0; i < boardA.length; i++)
            if (boardA[i] == BLACK || boardA[i] == WHITE) pebbleCount++;
        pebbles = new int[pebbleCount];
        pebbleCount = 0;
        for (int i = 0; i < boardA.length; i++)
            if (boardA[i] == BLACK || boardA[i] == WHITE) pebbles[pebbleCount++] = i;
        this.sPos = pebbles[0];
        this.pathMaskA = initPathMask();
        this.triviallyUnsolvable = !initConstraints();
    }
    
    private byte[] initPathMask() {
        byte[] pathMask = new byte[boardA.length];
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
    
    private boolean initConstraints() {
        // Check the corners for white pebbles
        if (boardA[0] == WHITE) return false; // Top left
        if (boardA[width-1] == WHITE) return false; // Top Right
        if (boardA[width*(height-1)] == WHITE) return false; // Bottom Left
        if (boardA[boardA.length-1] == WHITE) return false; // Bottom Right
        // Check the sides for white pebbles
        ResizingIntStack throwAway = new ResizingIntStack(boardA.length);
        // Top side
        for (int i = 0; i < width; i++)
            if (boardA[i] == WHITE)
                recordConstrs(i, EMPTY, NOT_DOWN, throwAway);
        // Bottom Side
        for (int i = width*(height-1); i < boardA.length; i++)
            if (boardA[i] == WHITE)
                recordConstrs(i, EMPTY, NOT_UP, throwAway);
        // Left Side
        for (int i = 0; i < boardA.length; i+=width)
            if (boardA[i] == WHITE)
                recordConstrs(i, EMPTY, NOT_RIGHT, throwAway);
        // Right Side
        for (int i = width-1; i < boardA.length; i+=width)
            if (boardA[i] == WHITE)
                recordConstrs(i, EMPTY, NOT_LEFT, throwAway);
        return true;
    }
    
    public boolean isForbidden(int pos, byte dir) {
        byte mask = SearchUtils.forbidDir(dir);
        if ((pathMaskA[pos] & mask) == mask)
            return true;
        int nPos = SearchUtils.nxtPos(pos,dir,sPos,width,boardA.length);
        if (nPos >= 0) {
            mask = SearchUtils.forbidDir(SearchUtils.complementDir(dir));
            if ((pathMaskA[nPos] & mask) == mask)
                return true;
        }
        return false;
    }
    
    public boolean legal(byte cDir, int nPos, byte nDir) {
        if (boardA[nPos] == WHITE) return cDir == nDir; // Must pass straight through a white pebble
        else if (boardA[nPos] == BLACK) return cDir != nDir; // This indicates a nice right angle turn
        else return true;
    }
    
    public void setConstraints(IntStack pStack, ByteStack dStack, ResizingIntStack cStack, PathMask pathMask) {
        int cCount = 0;
        int nPos = pStack.peek();
        byte nDir = dStack.peek();
        if (nPos == sPos) {
            pathMask.recordConstrs(nPos,nDir,EMPTY,cStack);
            cCount++;
        }
        else {
            pathMask.recordConstrs(nPos,nDir,CLOSED,cStack);
            cCount++;
        }
        if (dStack.size() == 1) {
            // We left the white pebble, add constraints for entering the white pebble
            if (boardA[nPos] == WHITE) {
                pathMask.recordConstrs(nPos,EMPTY,SearchUtils.allowOnlyDir(SearchUtils.complementDir(nDir)),cStack);
                cCount++;
            }
            // We left the black pebble, add constraints for entering the black pebble
            else if (boardA[nPos] == BLACK) {
                pathMask.recordConstrs(nPos,EMPTY,SearchUtils.forbidDir(SearchUtils.complementDir(nDir)),cStack);
                cCount++;
            }
        }
        else if (dStack.size() == 2) {
            int pPos = pStack.peek(1);
            byte pDir = dStack.peek(1);
            // We left the white pebble and went straight, add constraints for turning before entering the white pebble
            if (boardA[pPos] == WHITE && nDir == pDir) {
                byte ppDir = SearchUtils.complementDir(nDir);
                int ppPos = SearchUtils.nxtPos(pPos,ppDir,sPos,width,boardA.length);
                pathMask.recordConstrs(ppPos,EMPTY,SearchUtils.forbidDir(ppDir),cStack);
                cCount++;
                // We started on a white pebble and moved immediately to another one, add constraints for exiting the second white pebble
                if (boardA[nPos] == WHITE) {
                    int nnPos = SearchUtils.nxtPos(nPos,nDir,sPos,width,boardA.length);
                    pathMask.recordConstrs(nnPos,EMPTY,SearchUtils.forbidDir(nDir),cStack);
                    cCount++;
                }
            }
        }
        else if (boardA[nPos] == WHITE) { // dStack.size() > 2
            int pDir = dStack.peek(1);
            int ppDir = dStack.peek(2);
            // We came straight in - add some constraints for the exit
            if (nDir == pDir && pDir == ppDir) {
                int nnPos = SearchUtils.nxtPos(nPos,nDir,sPos,width,boardA.length);
                pathMask.recordConstrs(nnPos,EMPTY,SearchUtils.forbidDir(nDir),cStack);
                cCount++;
            }
        }
        cStack.push(cCount);
    }
    
    public void recordConstrs(int pos, int dir, byte fFlags, ResizingIntStack cStack) {
        byte newMask = (byte)(pathMaskA[pos] | fFlags | dir);
        assert (pathMaskA[pos]&newMask) == pathMaskA[pos];
        cStack.push(pos);
        cStack.push((int)pathMaskA[pos]);
        pathMaskA[pos] = newMask;
    }
    
    public void backtrackConstraints(ResizingIntStack cStack) {
        int count = cStack.pop();
        assert count%2 == 0;
        for (; count > 0; count--) {
            byte maskState = (byte)cStack.pop();
            int pos = cStack.pop();
            pathMaskA[pos] = maskState;
        }
    }
    
    public boolean complete() {
        for (int pebble : pebbles) {
            byte pebblePath = (byte)(pathMaskA[pebble]&MASK_PATH);
            if (pebblePath == 0) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        String newLine = System.getProperty("line.separator");
        for (int row = 0; row < height; row++) {
            for (int i = 0; i < width; i++) {
                String sqr = "    ";
                byte constrsElem = (byte)(pathMaskA[(row*width)+i] & MASK_CONSTRS);
                if ((constrsElem & NOT_UP) == NOT_UP) sqr = "  - ";
                builder.append(sqr);
            }
            builder.append(newLine);
            for (int i = 0; i < width; i++) {
                String sqr = null;
                byte constrsElem = (byte)(pathMaskA[(row*width)+i] & MASK_CONSTRS);
                if (i == 0) {
                    if ((constrsElem & NOT_LEFT) == NOT_LEFT) 
                        sqr = "|";
                    else
                        sqr = " ";
                    builder.append(sqr);
                }
                byte pathElem = (byte)(pathMaskA[(row*width)+i] & MASK_PATH);
                switch (pathElem) {
                    case UP : sqr = (row*width)+i == sPos ? " u " : " A "; break;
                    case DOWN : sqr = (row*width)+i == sPos ? " d " : " V "; break;
                    case LEFT : sqr = (row*width)+i == sPos ? " l " : " < "; break;
                    case RIGHT : sqr = (row*width)+i == sPos ? " r " : " > "; break;
                    default : sqr = " . ";
                }
                builder.append(sqr);
                if ((constrsElem & NOT_RIGHT) == NOT_RIGHT)
                    sqr = "|";
                else
                    sqr = " ";
                builder.append(sqr);
            }
            builder.append("                  ");
            for (int i = 0; i < width; i++) {
                switch (boardA[(row*width)+i] & MASK_PATH) {
                    case WHITE : builder.append("  W  "); break;
                    case BLACK : builder.append("  B  "); break;
                    default : builder.append("  .  ");
                }
            }
            builder.append(newLine);
            if (row == height-1) {
                for (int i = 0; i < width; i++) {
                    String sqr = "    ";
                    byte constrsElem = (byte)(pathMaskA[(row*width)+i] & MASK_CONSTRS);
                    if ((constrsElem & NOT_DOWN) == NOT_DOWN) sqr = "  - ";
                    builder.append(sqr);
                }
                builder.append(newLine);
            }
        }
        return builder.toString();
    }
}
