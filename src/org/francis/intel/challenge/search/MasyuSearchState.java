package org.francis.intel.challenge.search;

import org.francis.intel.challenge.stack.ByteStack;
import org.francis.intel.challenge.stack.IntStack;
import org.francis.intel.challenge.stack.ResizingIntStack;

public class MasyuSearchState implements Constants {
    
    PathMask pathMask;
    public int solutionCount = 0;
    
    public MasyuSearchState(int height, int width, byte[] board) {
        assert height*width == board.length;
        pathMask = new PathMask(board,width,height);
    }
    
    public String search() {
        IntStack pStack = new IntStack(pathMask.totalSqrs+2);
        ByteStack dStack = new ByteStack(pathMask.totalSqrs+2);
        ResizingIntStack cStack = new ResizingIntStack(4*(pathMask.totalSqrs+2));
        StringBuilder result = new StringBuilder();
        if (pathMask.triviallyUnsolvable) {
            System.out.println("Could not find a solution");
            return "No Solution Found";
        }
        System.out.println(pathMask);
        pshInit(pStack,dStack,cStack);
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
                if (pathMask.complete()) {
                    solutionCount++;
                    System.out.println();
                    System.out.println("Solution Found!");
                    System.out.println();System.out.println();
                    System.out.print(pathMask);
                    System.out.println(printSolution(dStack));
                    result.append(printSolution(dStack));
                    result.append(System.getProperty("line.separator"));
                    System.out.println("Solution Found!");
                }
                pStack.pop();
                dStack.pop();
                pathMask.backtrackConstraints(cStack);
                backtrack(pStack,dStack,cStack);
                continue;
            }
            if (!pshMove(pStack,dStack,cStack)) backtrack(pStack,dStack,cStack);
        }
    }
    
    private String printSolution(ByteStack dStack) {
        StringBuilder builder = new StringBuilder();
        String newLine = System.getProperty("line.separator");
        builder.append((getRow(pathMask.sPos)+1)+" "+(getCol(pathMask.sPos)+1));
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

    private void backtrack(IntStack pStack, ByteStack dStack, ResizingIntStack cStack) {
        byte nDir = NOTHING_LEFT;
        do {
            pStack.pop();
            nDir = dStack.pop();
            pathMask.backtrackConstraints(cStack);
        } while (pStack.size() > 0 && !pshMove(pStack,dStack,cStack,++nDir));
    }
    
    private void pshInit(IntStack pStack, ByteStack dStack, ResizingIntStack cStack) {
        pStack.push(EMPTY);
        dStack.push(MAGIC_DIR);
        cStack.push(0); // Here we add the number of constraints added, not many
    }

    private boolean pshMove(IntStack pStack, ByteStack dStack, ResizingIntStack cStack) {
        return pshMove(pStack, dStack, cStack, UP);
    }
    
    private boolean pshMove(IntStack pStack, ByteStack dStack, ResizingIntStack cStack, byte initDir) {
        assert pStack.size() == dStack.size();
        assert dStack.size() == cStack.size();
        int cPos = pStack.peek();
        byte cDir = dStack.peek();
        int nPos = SearchUtils.nxtPos(cPos,cDir,pathMask.sPos,pathMask.width,pathMask.totalSqrs);
        if (nPos == pathMask.sPos && cDir != MAGIC_DIR) {
            pStack.push(nPos);
            dStack.push(EMPTY);
            pathMask.setConstraints(pStack,dStack,cStack,pathMask);
            return true;
        }
        for (byte nDir = initDir; nDir < NOTHING_LEFT; nDir++) {
            if (nDir == (cDir^1)) continue;
            if (pathMask.isForbidden(nPos,nDir)) continue;
            if(pathMask.legal(cDir,nPos,nDir) || cDir == MAGIC_DIR) { // The magic dir skirts around legality
                pStack.push(nPos);
                dStack.push(nDir);
                pathMask.setConstraints(pStack,dStack,cStack,pathMask);
                return true;
            }
        }
        return false;
    }
    
    private boolean returned(int cPos) {
        return cPos == pathMask.sPos;
    }

    private int getPos(int row, int col) {
        return row*pathMask.width+col;
    }

    private int getRow(int pos) {
        return pos/pathMask.width;
    }
    
    private int getCol(int pos) {
        return pos%pathMask.width;
    }
    
//    public boolean checkPathMask(byte[] pathMask) {
//        for (int pos = 0; pos < pathMask.length; pos++) {
//            byte cMask = pathMask[pos];
//            
//            int uPos = nxtPos(pos,UP);
//            if (uPos > 0) {
//                byte uMask = pathMask[uPos];
//                boolean cUp = (cMask&NOT_UP) == NOT_UP;
//                boolean uDown = (uMask&NOT_DOWN) == NOT_DOWN;
//                assert cUp == uDown;
//            }
//            int dPos = nxtPos(pos,DOWN);
//            if (dPos > 0) {
//                byte dMask = pathMask[dPos];
//                boolean cDown = (cMask&NOT_DOWN) == NOT_DOWN;
//                boolean dUp = (dMask&NOT_UP) == NOT_UP;
//                assert cDown == dUp;
//            }
//            int lPos = nxtPos(pos,LEFT);
//            if (lPos > 0) {
//                byte lMask = pathMask[lPos];
//                boolean cLeft = (cMask&NOT_LEFT) == NOT_LEFT;
//                boolean lRight = (lMask&NOT_RIGHT) == NOT_RIGHT;
//                assert cLeft == lRight;
//            }
//            int rPos = nxtPos(pos,RIGHT);
//            if (rPos > 0) {
//                byte dMask = pathMask[rPos];
//                boolean cRight = (cMask&NOT_RIGHT) == NOT_RIGHT;
//                boolean rLeft = (dMask&NOT_LEFT) == NOT_LEFT;
//                assert cRight == rLeft;
//            }
//        }
//        return true; // Assertion hacking - poor form but very effective
//    }
    
    @Override
    public String toString() {
        return pathMask.toString();
    }
}