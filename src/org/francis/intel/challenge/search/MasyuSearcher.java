package org.francis.intel.challenge.search;

import org.francis.intel.challenge.stack.ByteStack;
import org.francis.intel.challenge.stack.IntStack;
import org.francis.intel.challenge.stack.ResizingIntStack;

public class MasyuSearcher implements Constants {
    
    PathState pathState;
    IntStack pStack;
    ByteStack dStack;
    ResizingIntStack cStack;
    public int solutionCount = 0;
    
    public MasyuSearcher(int height, int width, int[] board) {
        assert height*width == board.length;
        pathState = new PathState(board,width,height);
        pStack = new IntStack(pathState.totalSqrs+2);
        dStack = new ByteStack(pathState.totalSqrs+2);
        cStack = new ResizingIntStack(4*(pathState.totalSqrs+2));
    }
    
    public String search() {
        StringBuilder result = new StringBuilder();
        if (pathState.triviallyUnsolvable) {
            System.out.println("Could not find a solution");
            return "No Solution Found";
        }
        System.out.println(pathState);
        pshInit();
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
                if (pathState.complete()) {
                    solutionCount++;
                    System.out.println();
                    System.out.println("Solution Found!");
                    System.out.println();System.out.println();
                    System.out.print(pathState);
                    System.out.println(printSolution());
                    result.append(printSolution());
                    result.append(System.getProperty("line.separator"));
                    System.out.println("Solution Found!");
                }
                pStack.pop();
                dStack.pop();
                pathState.backtrackConstraints(cStack);
                backtrack();
                continue;
            }
            if (!pshMove()) backtrack();
        }
    }
    
    private String printSolution() {
        StringBuilder builder = new StringBuilder();
        String newLine = System.getProperty("line.separator");
        builder.append((getRow(pathState.sPos)+1)+" "+(getCol(pathState.sPos)+1));
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

    private void backtrack() {
        int nDir = NOTHING_LEFT;
        do {
            pStack.pop();
            nDir = dStack.pop();
            pathState.backtrackConstraints(cStack);
        } while (pStack.size() > 0 && !pshMove(++nDir));
    }
    
    private void pshInit() {
        pStack.push(EMPTY);
        dStack.push(MAGIC_DIR);
        cStack.push(0); // Here we add the number of constraints added, not many
    }

    private boolean pshMove() {
        return pshMove(UP);
    }
    
    private boolean pshMove(int initDir) {
        assert pStack.size() == dStack.size();
        assert dStack.size() == cStack.size();
        int cPos = pStack.peek();
        int cDir = dStack.peek();
        int nPos = SearchUtils.nxtPos(cPos,cDir,pathState.sPos,pathState.width,pathState.totalSqrs);
        if (nPos == pathState.sPos && cDir != MAGIC_DIR) {
            pStack.push(nPos);
            dStack.push(EMPTY);
            pathState.setConstraints(pStack,dStack,cStack,pathState);
            return true;
        }
        for (int nDir = initDir; nDir < NOTHING_LEFT; nDir++) {
            if (nDir == (cDir^1)) continue;
            if (pathState.isForbidden(nPos,nDir)) continue;
            if(pathState.legal(cDir,nPos,nDir) || cDir == MAGIC_DIR) { // The magic dir skirts around legality
                pStack.push(nPos);
                dStack.push(nDir);
                pathState.setConstraints(pStack,dStack,cStack,pathState);
                return true;
            }
        }
        return false;
    }
    
    private boolean returned(int cPos) {
        return cPos == pathState.sPos;
    }

    private int getRow(int pos) {
        return pos/pathState.width;
    }
    
    private int getCol(int pos) {
        return pos%pathState.width;
    }
    
    @Override
    public String toString() {
        return pathState.printBoard();
    }
}