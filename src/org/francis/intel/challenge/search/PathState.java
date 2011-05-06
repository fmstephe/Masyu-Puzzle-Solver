package org.francis.intel.challenge.search;

import org.francis.intel.challenge.stack.IntStack;
import org.francis.intel.challenge.stack.LevelStack;

public class PathState implements Constants {
    
    private final int[] pathMaskA;
    public final int[] boardA;  // Globally shared data-structure, don't fuck with it
    private final int[] pebbles;  // Globally shared data-structure, don't fuck with it
    public final int totalSqrs;
    public final int width;
    public final int height;
    public final int sPos;
    boolean triviallyUnsolvable;
    
    public PathState(int[] boardA, int[] pebbles, int width, int height) {
        this.boardA = boardA;
        this.width = width;
        this.height = height;
        this.totalSqrs = boardA.length;
        this.pebbles = pebbles;
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
        if (getBoardVal(0) == WHITE) return false; // Top left
        if (getBoardVal(width-1) == WHITE) return false; // Top Right
        if (getBoardVal(width*(height-1)) == WHITE) return false; // Bottom Left
        if (getBoardVal(boardA.length-1) == WHITE) return false; // Bottom Right
        // Check the sides for white pebbles
        LevelStack throwAway = new LevelStack(boardA.length);
        // Top side
        int inARow = 0;
        for (int i = 0; i < width; i++) {
            if (inARow > 2) {
                return false;
            }
            if (getBoardVal(i) == WHITE) {
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
            if (getBoardVal(i) == WHITE) {
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
            if (getBoardVal(i) == WHITE) {
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
            if (getBoardVal(i) == WHITE) {
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
        internalWhiteSequences();
        return true;
    }
    
    private void internalWhiteSequences() {
        int inARow = 0;
        LevelStack throwAway = new LevelStack(boardA.length);
        // Horizontal Check
        for (int row = 1; row < height-1; row++) {
            for (int i = 0; i < width; i++) {
                
                if (getBoardVal((row*width)+i) == WHITE)
                    inARow++;
                else
                    inARow = 0;
                
                if (inARow == 3) {
                    int ppPos = (row*width)+(i-2);
                    int pPos = (row*width)+(i-1);
                    int cPos = (row*width)+i;
                    recordConstrs(ppPos, EMPTY, NOT_LEFT | NOT_RIGHT, throwAway);
                    recordConstrs(pPos, EMPTY, NOT_LEFT | NOT_RIGHT, throwAway);
                    recordConstrs(cPos, EMPTY, NOT_LEFT | NOT_RIGHT, throwAway);
                }
                if (inARow > 3) {
                    int cPos = (row*width)+i;
                    recordConstrs(cPos, EMPTY, NOT_LEFT | NOT_RIGHT, throwAway);
                }
            }
        }
        inARow = 0;
        // Vertical Check
        for (int col = 1; col < width-1; col++) {
            for (int i = 0; i < height; i++) {
                
                if (getBoardVal((i*width)+col) == WHITE)
                    inARow++;
                else
                    inARow = 0;
                
                if (inARow == 3) {
                    int ppPos = (i-2)*width+col;
                    int pPos = (i-1)*width+col;
                    int cPos = (i*width)+col;
                    recordConstrs(ppPos, EMPTY, NOT_UP | NOT_DOWN, throwAway);
                    recordConstrs(pPos, EMPTY, NOT_UP | NOT_DOWN, throwAway);
                    recordConstrs(cPos, EMPTY, NOT_UP | NOT_DOWN, throwAway);
                }
                if (inARow > 3) {
                    int cPos = (i*width)+col;
                    recordConstrs(cPos, EMPTY, NOT_UP | NOT_DOWN, throwAway);
                }
            }
        }
    }
    
    public boolean isForbidden(int pos, int dir) {
        int mask = SearchUtils.forbidDir(dir);
        if ((pathMaskA[pos] & mask) == mask)
            return true;
        int nPos = SearchUtils.nxtPos(pos,dir,width,boardA.length);
        if (nPos >= 0) {
            mask = SearchUtils.forbidDir(SearchUtils.complementDir(dir));
            if ((pathMaskA[nPos] & mask) == mask)
                return true;
        }
        return false;
    }
    
    public boolean legal(int cPos, int cDir, int nPos, int nDir) {
        if (!checkSurroundingConstraints(cPos)) return false;
        if (getBoardVal(nPos) == WHITE) return cDir == nDir; // Must pass straight through a white pebble
        else if (getBoardVal(nPos) == BLACK) return cDir != nDir; // This indicates a nice right angle turn
        else return true;
    }
    
    private boolean checkSurroundingConstraints(int pos) {
        for (int dir = UP; dir < NOTHING_LEFT; dir++) {
            int nPos = SearchUtils.nxtPos(pos, dir, width, totalSqrs);
            if (nPos >= 0) {
                if (isOverConstrained(nPos)) {
                    return false; 
                }
            }
        }
        return true;
    }
    
    private boolean isOverConstrained(int pos) {
        if (getBoardVal(pos) == EMPTY) return false;
        int mask = pathMaskA[pos];
        if ((mask & MASK_PATH) != 0) return false; 
        int conCount = 0;
        for (int dir = UP; dir < NOTHING_LEFT; dir++) {
            int forbidDir = SearchUtils.forbidDir(dir);
            int forbidCDir = SearchUtils.forbidDir(SearchUtils.complementDir(dir));
            if ((mask & forbidDir) == forbidDir || (pathMaskA[SearchUtils.nxtPos(pos, dir, width, totalSqrs)] & forbidCDir) == forbidCDir) {
                conCount++;
            }
        }
            
        return conCount > 2;
    }
    
    public void setConstraints(IntStack pStack, LevelStack dStack, LevelStack cStack, PathState pathMask) {
        int nPos = pStack.peek()&MASK_POS_VAL;
        int nDir = dStack.peekVal();
        if (nPos == sPos) {
            pathMask.recordConstrs(nPos,nDir,EMPTY,cStack);
        }
        else {
            int bMask = SearchUtils.forbidDir(SearchUtils.complementDir(dStack.peekVal(1)));
            int fMask = SearchUtils.forbidDir(nDir);
            int mask = CLOSED ^ bMask ^ fMask;
            pathMask.recordConstrs(nPos,nDir,mask,cStack);
        }
        if (pStack.size() > 2 && getBoardVal(nPos) == WHITE) {
            int pDir = dStack.peekVal(1);
            int ppDir = dStack.peekVal(2);
            // We came straight in - add some constraints for the exit
            if (nDir == pDir && pDir == ppDir) {
                int nnPos = SearchUtils.nxtPos(nPos,nDir,width,boardA.length);
                pathMask.recordConstrs(nnPos,EMPTY,SearchUtils.forbidDir(nDir),cStack);
            }
        }
        else if (pStack.size() == 1) {
            // We left the white pebble, add constraints for entering the white pebble
            if (getBoardVal(nPos) == WHITE) {
                pathMask.recordConstrs(nPos,EMPTY,SearchUtils.forbidPerpendicular(nDir),cStack);
            }
            // We left the black pebble, add constraints for entering the black pebble
            else if (getBoardVal(nPos) == BLACK) {
                pathMask.recordConstrs(nPos,EMPTY,SearchUtils.forbidDir(SearchUtils.complementDir(nDir)),cStack);
            }
        }
        else if (pStack.size() == 2) {
            int pPos = pStack.peek(1)&MASK_POS_VAL;
            int pDir = dStack.peekVal(1);
            // We left the white pebble and went straight, add constraints for turning before entering the white pebble
            if (getBoardVal(pPos) == WHITE && nDir == pDir) {
                int ppDir = SearchUtils.complementDir(nDir);
                int ppPos = SearchUtils.nxtPos(pPos,ppDir,width,boardA.length);
                pathMask.recordConstrs(ppPos,EMPTY,SearchUtils.forbidDir(ppDir),cStack);
                // We started on a white pebble and moved immediately to another one, add constraints for exiting the second white pebble
                if (getBoardVal(nPos) == WHITE) {
                    int nnPos = SearchUtils.nxtPos(nPos,nDir,width,boardA.length);
                    pathMask.recordConstrs(nnPos,EMPTY,SearchUtils.forbidDir(nDir),cStack);
                }
            }
        }
        cStack.finishLevel();
    }
    
    public void recordConstrs(int pos, int dir, int fFlags, LevelStack cStack) {
        int newMask = (pathMaskA[pos] | fFlags | dir);
        assert (pathMaskA[pos]&newMask) == pathMaskA[pos];
        if (pathMaskA[pos] != newMask) {
            cStack.pushVal(pos);
            cStack.pushVal(pathMaskA[pos]);
            pathMaskA[pos] = newMask;
        }
        assert checkBoardState();
    }
    
    private boolean checkBoardState() {
        for (int pos = 0; pos < pathMaskA.length; pos++) {
            int dir = pathMaskA[pos] & MASK_PATH;
            if (dir != 0) { 
                int nPos = SearchUtils.nxtPos(pos,dir,width,totalSqrs);
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

    public void backtrackConstraints(LevelStack cStack) {
        while (!cStack.clearLevelIfEmpty()) {
            int maskState = cStack.popVal();
            int pos = cStack.popVal();
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
    
    public int getBoardVal(int pos) {
        return boardA[pos]&MASK_BOARD_VAL;
    }
    
    public int getPebbleIdx(int pos) {
        assert getBoardVal(pos) != EMPTY;
        return boardA[pos]&MASK_BOARD_IDX;
    }
    
    public String printBoard() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < boardA.length; i++) {
            if (i % width == 0) builder.append("\n");
            switch(getBoardVal(i)) {
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
                switch (getBoardVal((row*width)+i)) {
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
