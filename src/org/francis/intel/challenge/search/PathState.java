package org.francis.intel.challenge.search;

import org.francis.intel.challenge.stack.IntStack;
import org.francis.intel.challenge.stack.ResizingIntStack;

public class PathState implements Constants {
    
    private final int[] pathMaskA;
    private final int[] boardA;
    private final int[] pebbles;
    public final int totalSqrs;
    public final int width;
    public final int height;
    public final int sPos;
    boolean triviallyUnsolvable;
    
    public PathState(int[] boardA, int width, int height) {
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
    
    private int[] initPathMask() {
        int[] pathMask = new int[boardA.length];
        for (int i = 0; i < pathMask.length; i++) {
            int forbidden = 0;
            forbidden = i < width ? (forbidden | NOT_UP) : forbidden;
            forbidden = (i % width) == 0 ? (forbidden | NOT_LEFT) : forbidden;
            forbidden = (i % width) == width-1 ? (forbidden | NOT_RIGHT) : forbidden;
            forbidden = i >= width*(height-1) ? (forbidden | NOT_DOWN) : forbidden;
            pathMask[i] = (EMPTY_PATH | forbidden);
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
        int inARow = 0;
        for (int i = 0; i < width; i++) {
            if (inARow > 2) {
                return false;
            }
            if (boardA[i] == WHITE) {
                recordConstrs(i, EMPTY, NOT_DOWN, throwAway);
                inARow++;
            }
            else {
                inARow = 0;
            }
            if (inARow == 2) {
                int leftPos = i-2;
                recordConstrs(leftPos, EMPTY, NOT_LEFT, throwAway);
                int rightPos = i+1;
                recordConstrs(rightPos, EMPTY, NOT_RIGHT, throwAway);
            }
        }
        inARow = 0;
        // Bottom Side
        for (int i = width*(height-1); i < boardA.length; i++) {
            if (inARow > 2) {
                return false;
            }
            if (boardA[i] == WHITE) {
                recordConstrs(i, EMPTY, NOT_UP, throwAway);
                inARow++;
            }
            else {
                inARow = 0;
            }
            if (inARow == 2) {
                int leftPos = i-2;
                recordConstrs(leftPos, EMPTY, NOT_LEFT, throwAway);
                int rightPos = i+1;
                recordConstrs(rightPos, EMPTY, NOT_RIGHT, throwAway);
            }
        }
        inARow = 0;
        // Left Side
        for (int i = 0; i < boardA.length; i+=width) {
            if (inARow > 2) {
                return false;
            }
            if (boardA[i] == WHITE) {
                recordConstrs(i, EMPTY, NOT_RIGHT, throwAway);
                inARow++;
            }
            else {
                inARow = 0;
            }
            if (inARow == 2) {
                int topPos = i-(2*width);
                recordConstrs(topPos, EMPTY, NOT_UP, throwAway);
                int bottomPos = i+width;
                recordConstrs(bottomPos, EMPTY, NOT_DOWN, throwAway);
            }
        }
        inARow = 0;
        // Right Side
        for (int i = width-1; i < boardA.length; i+=width) {
            if (inARow > 2) {
                return false;
            }
            if (boardA[i] == WHITE) {
                recordConstrs(i, EMPTY, NOT_LEFT, throwAway);
                inARow++;
            }
            else {
                inARow = 0;
            }
            if (inARow == 2) {
                int topPos = i-(2*width);
                recordConstrs(topPos, EMPTY, NOT_UP, throwAway);
                int bottomPos = i+width;
                recordConstrs(bottomPos, EMPTY, NOT_DOWN, throwAway);
            }
        }
        return true;
    }
    
    public boolean isForbidden(int pos, int dir) {
        int mask = SearchUtils.forbidDir(dir);
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
    
    public boolean legal(IntStack dStack, int cPos, int cDir, int nPos, int nDir) {
        if (cDir != MAGIC_DIR && !checkSurroundingConstraints(cPos)) return false;
        if (boardA[nPos] == WHITE) return cDir == nDir; // Must pass straight through a white pebble
        else if (boardA[nPos] == BLACK) return cDir != nDir; // This indicates a nice right angle turn
        else return true;
    }
    
    private boolean checkSurroundingConstraints(int pos) {
        for (int dir = UP; dir < NOTHING_LEFT; dir++) {
            int nPos = SearchUtils.nxtPos(pos, dir, -1, width, totalSqrs);
            if (nPos >= 0) {
                if (isOverConstrained(nPos)) {
                    return false; 
                }
            }
        }
        return true;
    }
    
    private boolean isOverConstrained(int pos) {
        if (boardA[pos] == EMPTY) return false;
        int mask = pathMaskA[pos];
        if ((mask & MASK_PATH) != 0) return false; 
        int conCount = 0;
        for (int dir = UP; dir < NOTHING_LEFT; dir++) {
            int forbidDir = SearchUtils.forbidDir(dir);
            int forbidCDir = SearchUtils.forbidDir(SearchUtils.complementDir(dir));
            if ((mask & forbidDir) == forbidDir || (pathMaskA[SearchUtils.nxtPos(pos, dir, -1, width, totalSqrs)] & forbidCDir) == forbidCDir) {
                conCount++;
            }
        }
            
        return conCount > 2;
    }
    
    public void setConstraints(IntStack pStack, IntStack dStack, ResizingIntStack cStack, PathState pathMask) {
        int cCount = 0;
        int nPos = pStack.peek();
        int nDir = dStack.peek();
        if (nPos == sPos) {
            pathMask.recordConstrs(nPos,nDir,EMPTY,cStack);
            cCount++;
        }
        else {
            int bMask = SearchUtils.forbidDir(SearchUtils.complementDir(dStack.peek(1)));
            int fMask = SearchUtils.forbidDir(nDir);
            int mask = CLOSED ^ bMask ^ fMask;
            pathMask.recordConstrs(nPos,nDir,mask,cStack);
            cCount++;
        }
        if (dStack.size() > 3 && boardA[nPos] == WHITE) {
            int pDir = dStack.peek(1);
            int ppDir = dStack.peek(2);
            // We came straight in - add some constraints for the exit
            if (nDir == pDir && pDir == ppDir) {
                int nnPos = SearchUtils.nxtPos(nPos,nDir,sPos,width,boardA.length);
                pathMask.recordConstrs(nnPos,EMPTY,SearchUtils.forbidDir(nDir),cStack);
                cCount++;
            }
        }
        else if (dStack.size() == 2) {
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
        else if (dStack.size() == 3) {
            int pPos = pStack.peek(1);
            int pDir = dStack.peek(1);
            // We left the white pebble and went straight, add constraints for turning before entering the white pebble
            if (boardA[pPos] == WHITE && nDir == pDir) {
                int ppDir = SearchUtils.complementDir(nDir);
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
        cStack.push(cCount);
    }
    
    public void recordConstrs(int pos, int dir, int fFlags, ResizingIntStack cStack) {
        int newMask = (pathMaskA[pos] | fFlags | dir);
        assert (pathMaskA[pos]&newMask) == pathMaskA[pos];
        cStack.push(pos);
        cStack.push(pathMaskA[pos]);
        pathMaskA[pos] = newMask;
        assert checkBoardState();
    }
    
    private boolean checkBoardState() {
        for (int pos = 0; pos < pathMaskA.length; pos++) {
            int dir = pathMaskA[pos] & MASK_PATH;
            if (dir != 0) { 
                int nPos = SearchUtils.nxtPos(pos, dir, -1, width, totalSqrs);
                if (nPos >= 0) {
                    int forbidMask = SearchUtils.forbidDir(SearchUtils.complementDir(dir));
                    if ((pathMaskA[nPos] & forbidMask) == forbidMask) {
                        System.out.println(this);
                        assert false;
                    }
                }
            }
        }
        return true;
    }

    public void backtrackConstraints(ResizingIntStack cStack) {
        int count = cStack.pop();
        for (; count > 0; count--) {
            int maskState = cStack.pop();
            int pos = cStack.pop();
            pathMaskA[pos] = maskState;
        }
    }
    
    public boolean complete() {
        for (int pebble : pebbles) {
            int pebblePath = (pathMaskA[pebble]&MASK_PATH);
            if (pebblePath == 0) {
                return false;
            }
        }
        return true;
    }
    
    public String printBoard() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < boardA.length; i++) {
            if (i % width == 0) builder.append("\n");
            switch(boardA[i]) {
                case BLACK : builder.append("B");
                case WHITE : builder.append("W");
                case EMPTY : builder.append(".");
            }
        }
        return builder.toString();
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        String newLine = System.getProperty("line.separator");
        for (int row = 0; row < height; row++) {
            for (int i = 0; i < width; i++) {
                String sqr = "     ";
                int constrsElem = (pathMaskA[(row*width)+i] & MASK_CONSTRS);
                if ((constrsElem & NOT_UP) == NOT_UP) sqr = "  -  ";
                builder.append(sqr);
            }
            builder.append(newLine);
            for (int i = 0; i < width; i++) {
                String sqr = null;
                int constrsElem = (pathMaskA[(row*width)+i] & MASK_CONSTRS);
                if ((constrsElem & NOT_LEFT) == NOT_LEFT) 
                    sqr = "|";
                else
                    sqr = " ";
                builder.append(sqr);
                int pathElem = (pathMaskA[(row*width)+i] & MASK_PATH);
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
            for (int i = 0; i < width; i++) {
                String sqr = "     ";
                int constrsElem = (pathMaskA[(row*width)+i] & MASK_CONSTRS);
                if ((constrsElem & NOT_DOWN) == NOT_DOWN) sqr = "  -  ";
                builder.append(sqr);
            }
            builder.append(newLine);
        }
        return builder.toString();
    }
}
