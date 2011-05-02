package org.francis.intel.challenge;

import gnu.trove.list.array.TIntArrayList;

public class MasyuSearchState {
    
    // Playing board values
    public static final byte BLACK = 1;
    public static final byte WHITE = 2;
    public static final byte EMPTY = 0;
    // Path-mask values
    public static final byte EMPTY_PATH = 0;//Byte.parseByte("1000000",2);
    public static final byte UP = 2;
    public static final byte DOWN = 3;
    public static final byte LEFT = 4;
    public static final byte RIGHT = 5;
    public static final byte NOTHING_LEFT = 6;
    public static final byte MAGIC_DIR = 8; // This direction always magically points to the starting position
    public static final byte NOT_UP = 16;
    public static final byte NOT_DOWN = 32;
    public static final byte NOT_LEFT = 64;
    public static final byte NOT_RIGHT = Byte.MIN_VALUE;
    public static final byte CLOSED = NOT_UP | NOT_DOWN | NOT_LEFT | NOT_RIGHT;
 // Mask for isolating path
    public static final byte MASK_PATH = Byte.parseByte("00000111",2);
    // Mask for isolating the forbidden flags
    public static final byte MASK_CONSTRS = (byte)-16;
    // XOR mask for changing orientation UP -> LEFT, DOWN -> RIGHT
    public static final byte FLIP_ORIENTATION = Byte.parseByte("00000110",2);
    // Left-shift offset for turn a direction into a NOT_* flag
    // Used as: byte flag = 1 << (dir+LEFT_SHIFT_OFFSET);
    public static final byte LEFT_SHIFT_OFFSET = 2;
    // public static final byte CLEAR_CONSTR = Byte.parseByte("10000111",2);
    
    public final int height;
    public final int width;
    public int sPos = -1;
    public final byte[] board;
    public final int[] pebbles;
    public int solutionCount = 0;
    public TIntArrayList reusableBacktrack = new TIntArrayList();
    
    public MasyuSearchState(int height, int width, byte[] board) {
        assert height*width == board.length;
        this.height = height;
        this.width = width;
        this.board = board;
        int pebbleCount = 0;
        for (int i = 0; i < board.length; i++)
            if (board[i] == BLACK || board[i] == WHITE) pebbleCount++;
        pebbles = new int[pebbleCount];
        pebbleCount = 0;
        for (int i = 0; i < board.length; i++)
            if (board[i] == BLACK || board[i] == WHITE) pebbles[pebbleCount++] = i;
        sPos = pebbles[0];
    }
    
    public String search() {
        IntStack pStack = new IntStack(board.length+2);
        ByteStack dStack = new ByteStack(board.length+2);
        Stack<int[]> cStack = new Stack<int[]>(board.length+2);
        StringBuilder result = new StringBuilder();
        byte[] pathMask = initPathMask();
        if (!initConstraints(pathMask)) {
            System.out.println("Could not find a solution");
            return "No Solution Found";
        }
        printState(pathMask);
        pshInit(pStack,dStack,cStack,pathMask,UP);
        while (true) {
            if (pStack.size() == 0) {
                if (solutionCount == 0)
                    result.append("No Solution Found");
                else
                    result.append("Total Solutions: "+solutionCount);
                return result.toString();
            }
//            printState(pathMask);
            if (pStack.size() > 2 && returned(pStack.peek())) {
                if (complete(pStack.peek(),pathMask)) {
                    solutionCount++;
                    System.out.println();
                    System.out.println("Solution Found!");
                    printState(pathMask);
                    System.out.println(printSolution(dStack));
                    result.append(printSolution(dStack));
                    result.append(System.getProperty("line.separator"));
                    System.out.println("Solution Found!");
                }
                pStack.pop();
                dStack.pop();
                resetPathMask(cStack.pop(),pathMask);
                backtrack(pStack,dStack,cStack,pathMask);
                continue;
            }
            if (!pshMove(pStack,dStack,cStack,pathMask)) backtrack(pStack,dStack,cStack,pathMask);
        }
    }
    
    private String printSolution(ByteStack dStack) {
        StringBuilder builder = new StringBuilder();
        String newLine = System.getProperty("line.separator");
        builder.append((getRow(sPos)+1)+" "+(getCol(sPos)+1));
        builder.append(newLine);
        int count = 0;
        for (int i = dStack.size()-1; i >= 0; i--) {
            switch (dStack.peek(i)) {
                case UP : builder.append("U"); break;
                case DOWN : builder.append("D"); break;
                case LEFT : builder.append("L"); break;
                case RIGHT : builder.append("R"); break;
            }
            if (count == 40) {
                count = 0;
                builder.append(newLine);
            }
        }
        return builder.toString();
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
        assert checkPathMask(pathMask);
        return pathMask;
    }

    private boolean initConstraints(byte[] pathMask) {
        // Check the corners for white pebbles
        if (board[0] == WHITE) return false; // Top left
        if (board[width-1] == WHITE) return false; // Top Right
        if (board[width*(height-1)] == WHITE) return false; // Bottom Left
        if (board[board.length-1] == WHITE) return false; // Bottom Right
        // Check the sides for white pebbles
        TIntArrayList thowAway = new TIntArrayList();
        // Top side
        for (int i = 0; i < width; i++)
            if (board[i] == WHITE)
                recordConstrs(i, EMPTY, NOT_DOWN, pathMask, thowAway);
        // Bottom Side
        for (int i = width*(height-1); i < board.length; i++)
            if (board[i] == WHITE)
                recordConstrs(i, EMPTY, NOT_UP, pathMask, thowAway);
        // Left Side
        for (int i = 0; i < board.length; i+=width)
            if (board[i] == WHITE)
                recordConstrs(i, EMPTY, NOT_RIGHT, pathMask, thowAway);
        // Right Side
        for (int i = width-1; i < board.length; i+=width)
            if (board[i] == WHITE)
                recordConstrs(i, EMPTY, NOT_LEFT, pathMask, thowAway);
        return true;
    }

    private void backtrack(IntStack pStack, ByteStack dStack, Stack<int[]> cStack, byte[] pathMask) {
        byte nDir = NOTHING_LEFT;
        do {
            pStack.pop();
            nDir = dStack.pop();
            resetPathMask(cStack.pop(),pathMask);
        } while (pStack.size() > 0 && !pshMove(pStack,dStack,cStack,pathMask,++nDir));
    }
    
    private void resetPathMask(int[] constrs, byte[] pathMask) {
        assert checkPathMask(pathMask);
        if (constrs == null) return;
        assert constrs.length%2 == 0;
        // Here we must write back the saved constraints in reverse order so the earliest saved is written last
        for (int i = constrs.length-1; i >= 0; ) {
            byte maskState = (byte)constrs[i--];
            int pos = constrs[i--];
            pathMask[pos] = maskState;
        }
        assert checkPathMask(pathMask);
    }

    private void pshInit(IntStack pStack, ByteStack dStack, Stack<int[]> cStack, byte[] pathMask, byte initDir) {
        pStack.push(EMPTY);
        dStack.push(MAGIC_DIR);
        cStack.push(new int[0]);
    }

    private boolean pshMove(IntStack pStack, ByteStack dStack, Stack<int[]> cStack, byte[] pathMask) {
        return pshMove(pStack, dStack, cStack, pathMask, UP);
    }
    
    private boolean pshMove(IntStack pStack, ByteStack dStack, Stack<int[]> cStack, byte[] pathMask, byte initDir) {
        assert pStack.size() == dStack.size();
        assert dStack.size() == cStack.size();
        int cPos = pStack.peek();
        byte cDir = dStack.peek();
        int nPos = nxtPos(cPos,cDir);
        if (nPos == sPos && cDir != MAGIC_DIR) {
            pStack.push(nPos);
            dStack.push(EMPTY);
            setConstraints(pStack,dStack,cStack,pathMask);
            return true;
        }
        for (byte nDir = initDir; nDir < NOTHING_LEFT; nDir++) {
            if (nDir == (cDir^1)) continue;
            if (isForbidden(nPos,nDir,pathMask)) continue;
            if(legal(cDir,nPos,nDir,pathMask) || cDir == MAGIC_DIR) { // The magic dir skirts around legality
                pStack.push(nPos);
                dStack.push(nDir);
                setConstraints(pStack,dStack,cStack,pathMask);
                return true;
            }
        }
        return false;
    }
    
    private void setConstraints(IntStack pStack, ByteStack dStack, Stack<int[]> cStack, byte[] pathMask) {
        TIntArrayList backtrack = reusableBacktrack;
        backtrack.clear();
        int nPos = pStack.peek();
        byte nDir = dStack.peek();
        if (nPos == sPos) {
            recordConstrs(nPos,nDir,EMPTY,pathMask,backtrack);
        }
        else {
            recordConstrs(nPos,nDir,CLOSED,pathMask,backtrack);
        }
        if (dStack.size() == 1) {
            // We left the white pebble, add constraints for entering the white pebble
            if (board[nPos] == WHITE) {
                recordConstrs(nPos,EMPTY,allowOnlyDir(complementDir(nDir)),pathMask,backtrack);
            }
            // We left the black pebble, add constraints for entering the black pebble
            else if (board[nPos] == BLACK) {
                recordConstrs(nPos,EMPTY,forbidDir(complementDir(nDir)),pathMask,backtrack);
            }
        }
        else if (dStack.size() == 2) {
            int pPos = pStack.peek(1);
            byte pDir = dStack.peek(1);
            // We left the white pebble and went straight, add constraints for turning before entering the white pebble
            if (board[pPos] == WHITE && nDir == pDir) {
                byte ppDir = complementDir(nDir);
                int ppPos = nxtPos(pPos,ppDir);
                recordConstrs(ppPos,EMPTY,forbidDir(ppDir),pathMask,backtrack);
                // We started on a white pebble and moved immediately to another one, add constraints for exiting the second white pebble
                if (board[nPos] == WHITE) {
                    int nnPos = nxtPos(nPos,nDir);
                    recordConstrs(nnPos,EMPTY,forbidDir(nDir),pathMask,backtrack);
                }
            }
        }
        else if (board[nPos] == WHITE) { // dStack.size() > 2
            int pDir = dStack.peek(1);
            int ppDir = dStack.peek(2);
            // We came straight in - add some constraints for the exit
            if (nDir == pDir && pDir == ppDir) {
                int nnPos = nxtPos(nPos,nDir);
                recordConstrs(nnPos,EMPTY,forbidDir(nDir),pathMask,backtrack);
            }
        }
        int[] constrs = null;
        if (!backtrack.isEmpty()) {
            constrs = new int[backtrack.size()];
            for (int i = 0; i < backtrack.size(); i++) {
                constrs[i] = backtrack.get(i);
            }
        }
        cStack.push(constrs);
    }

    private void recordConstrs(int pos, int dir, byte fFlags, byte[] pathMask, TIntArrayList backtrack) {
        assert checkPathMask(pathMask);
//        byte cMask = pathMask[pos];
//        if ((fFlags & NOT_UP) == NOT_UP && (cMask & NOT_UP) != NOT_UP) {
//            int upPos = nxtPos(pos,UP);
//            rememberAndSetMaskState(upPos,(byte)(pathMask[upPos]|NOT_DOWN),pathMask,backtrack);
//        }
//        if ((fFlags & NOT_DOWN) == NOT_DOWN && (cMask & NOT_DOWN) != NOT_DOWN) {
//            int downPos = nxtPos(pos,DOWN);
//            rememberAndSetMaskState(downPos,(byte)(pathMask[downPos]|NOT_UP),pathMask,backtrack);
//        }
//        if ((fFlags & NOT_LEFT) == NOT_LEFT && (cMask & NOT_LEFT) != NOT_LEFT) {
//            int leftPos = nxtPos(pos,LEFT);
//            rememberAndSetMaskState(leftPos,(byte)(pathMask[leftPos]|NOT_RIGHT),pathMask,backtrack);
//        }
//        if ((fFlags & NOT_RIGHT) == NOT_RIGHT && (cMask & NOT_RIGHT) != NOT_RIGHT) {
//            int rightPos = nxtPos(pos,RIGHT);
//            rememberAndSetMaskState(rightPos,(byte)(pathMask[rightPos]|NOT_LEFT),pathMask,backtrack);
//        }
        byte newMask = (byte)(pathMask[pos] | fFlags | dir);
        rememberAndSetMaskState(pos,newMask,pathMask,backtrack);
        assert checkPathMask(pathMask);
    }

    private void rememberAndSetMaskState(int pos, byte newMask, byte[] pathMask, TIntArrayList backtrack) {
        assert (pathMask[pos]&newMask) == pathMask[pos];
        if (newMask != pathMask[pos]) {
            backtrack.add(pos);
            backtrack.add((int)pathMask[pos]);
            pathMask[pos] = newMask;
        }
    }
    
    private byte complementDir(int dir) {
        return (byte)(dir^1);
    }
    
    private byte forbidDir(byte dir) {
        return (byte)(1 << (dir+LEFT_SHIFT_OFFSET));
    }
    
    private byte allowOnlyDir(byte dir) {
        byte forbidMask = (byte)(1 << (dir+LEFT_SHIFT_OFFSET));
        return (byte)(forbidMask ^ MASK_CONSTRS);
    }
    
    private boolean isForbidden(int pos, byte dir, byte[] pathMask) {
        byte mask = forbidDir(dir);
        if ((pathMask[pos] & mask) == mask)
            return true;
        int nPos = nxtPos(pos,dir);
        if (nPos >= 0) {
            mask = forbidDir(complementDir(dir));
            if ((pathMask[nPos] & mask) == mask)
                return true;
        }
        return false;
    }

    private boolean legal(byte cDir, int nPos, byte nDir, byte[] pathMask) {
        if (board[nPos] == WHITE) return cDir == nDir;
        else if (board[nPos] == BLACK) return cDir != nDir; // This indicates a nice right angle turn
        else return true;
    }

    private void printState(byte[] pathMask) {
        System.out.println();System.out.println();
        for (int row = 0; row < height; row++) {
            for (int i = 0; i < width; i++) {
                String sqr = "    ";
                byte constrsElem = (byte)(pathMask[(row*width)+i] & MASK_CONSTRS);
                if ((constrsElem & NOT_UP) == NOT_UP) sqr = "  - ";
                System.out.print(sqr);
            }
            System.out.println();
            for (int i = 0; i < width; i++) {
                String sqr = null;
                byte constrsElem = (byte)(pathMask[(row*width)+i] & MASK_CONSTRS);
                if (i == 0) {
                    if ((constrsElem & NOT_LEFT) == NOT_LEFT) 
                        sqr = "|";
                    else
                        sqr = " ";
                    System.out.print(sqr);
                }
                byte pathElem = (byte)(pathMask[(row*width)+i] & MASK_PATH);
                switch (pathElem) {
                    case UP : sqr = (row*width)+i == sPos ? " u " : " A "; break;
                    case DOWN : sqr = (row*width)+i == sPos ? " d " : " V "; break;
                    case LEFT : sqr = (row*width)+i == sPos ? " l " : " < "; break;
                    case RIGHT : sqr = (row*width)+i == sPos ? " r " : " > "; break;
                    default : sqr = " . ";
                }
                System.out.print(sqr);
                if ((constrsElem & NOT_RIGHT) == NOT_RIGHT)
                    sqr = "|";
                else
                    sqr = " ";
                System.out.print(sqr);
            }
            System.out.print("                  ");
            for (int i = 0; i < width; i++) {
                switch (board[(row*width)+i] & MASK_PATH) {
                    case WHITE : System.out.print("  W  "); break;
                    case BLACK : System.out.print("  B  "); break;
                    default : System.out.print("  .  ");
                }
            }
            System.out.println();
            if (row == height-1) {
                for (int i = 0; i < width; i++) {
                    String sqr = "    ";
                    byte constrsElem = (byte)(pathMask[(row*width)+i] & MASK_CONSTRS);
                    if ((constrsElem & NOT_DOWN) == NOT_DOWN) sqr = "  - ";
                    System.out.print(sqr);
                }
                System.out.println();
            }
        }
    }

    private int nxtPos(int pos, byte dir) {
        switch (dir) {
            case UP :
                return pos-width;
            case DOWN : 
                int np = pos+width;
                return np >= board.length ? -1 : np;
            case LEFT : 
                int col = getCol(pos);
                return col == 0 ? -1 : pos-1;
            case RIGHT : 
                col = getCol(pos);
                return col == width-1 ? -1 : pos+1;
            case MAGIC_DIR :
                return sPos;
            default : throw new IllegalArgumentException();
        }
    }
    
    private boolean returned(int cPos) {
        return cPos == sPos;
    }
    private boolean complete(int cPos, byte[] pathMask) {
        if (cPos != sPos) return false;
        for (int pebble : pebbles) {
            byte pebblePath = (byte)(pathMask[pebble]&MASK_PATH);
            if (pebblePath == 0) {
                return false;
            }
        }
        return true;
    }

    private int getPos(int row, int col) {
        return row*width+col;
    }

    private int getRow(int pos) {
        return pos/width;
    }
    
    private int getCol(int pos) {
        return pos%width;
    }
    
    public boolean checkPathMask(byte[] pathMask) {
        for (int pos = 0; pos < pathMask.length; pos++) {
            byte cMask = pathMask[pos];
            
            int uPos = nxtPos(pos,UP);
            if (uPos > 0) {
                byte uMask = pathMask[uPos];
                boolean cUp = (cMask&NOT_UP) == NOT_UP;
                boolean uDown = (uMask&NOT_DOWN) == NOT_DOWN;
                assert cUp == uDown;
            }
            int dPos = nxtPos(pos,DOWN);
            if (dPos > 0) {
                byte dMask = pathMask[dPos];
                boolean cDown = (cMask&NOT_DOWN) == NOT_DOWN;
                boolean dUp = (dMask&NOT_UP) == NOT_UP;
                assert cDown == dUp;
            }
            int lPos = nxtPos(pos,LEFT);
            if (lPos > 0) {
                byte lMask = pathMask[lPos];
                boolean cLeft = (cMask&NOT_LEFT) == NOT_LEFT;
                boolean lRight = (lMask&NOT_RIGHT) == NOT_RIGHT;
                assert cLeft == lRight;
            }
            int rPos = nxtPos(pos,RIGHT);
            if (rPos > 0) {
                byte dMask = pathMask[rPos];
                boolean cRight = (cMask&NOT_RIGHT) == NOT_RIGHT;
                boolean rLeft = (dMask&NOT_LEFT) == NOT_LEFT;
                assert cRight == rLeft;
            }
        }
        return true; // Assertion hacking - poor form but very effective
    }
    
    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < board.length; i++) {
            if (i % width == 0) out.append("\n");
            switch (board[i] & MASK_PATH) {
                case WHITE : out.append(" W "); break;
                case BLACK : out.append(" B "); break;
                default : out.append(" . ");
            }
        }
        return out.toString();
    }
}