package org.francis.intel.challenge.search;

import org.francis.intel.challenge.stack.IntStack;
import org.francis.intel.challenge.stack.LevelStack;
import org.francis.intel.challenge.stack.UnsafeIntStack;
import org.francis.p2p.worksharing.network.NetworkManager;
import org.francis.p2p.worksharing.network.WorkSharer;

public class MasyuSearcher implements Constants, WorkSharer, Runnable {
    
    final PathState pathState;
    final IntStack pStack;
    final IntStack pebbleStack;
    final LevelStack dStack;
    final LevelStack cStack;
    final int[] markablePebbles; // Local mutable array - must be copied from the one passed in
    final int[][] nearestPebbleMatrix; // Globally shared data-structure, don't fuck with it
    final NetworkManager networkManager;
    int nextPebblePos;
    int nextPebbleIdx;
    int sharableWork;
    boolean primeWorker;
    
    public MasyuSearcher(int height, int width, int[] board, int[] pebbles, int[][] nearestPebbleMatrix, NetworkManager networkManager, boolean primeWorker) {
        assert height*width == board.length;
        this.sharableWork = 0;
        this.pathState = new PathState(board,pebbles,width,height);
        this.pStack = new UnsafeIntStack(pathState.totalSqrs+2);
        this.dStack = new LevelStack(5*(pathState.totalSqrs+2));
        this.cStack = new LevelStack(4*(pathState.totalSqrs+2));
        this.pebbleStack = new UnsafeIntStack(pebbles.length);
        this.markablePebbles = pebbles.clone();
        this.nearestPebbleMatrix = nearestPebbleMatrix;
        this.networkManager = networkManager;
        this.primeWorker = primeWorker;
    }
    
    @Override
    public void run() {
        search();
    }
    
    public void search() {
        int solutionCount = 0;
        if (primeWorker) {
            pshInit();
            System.out.println(pathState);
            if (pathState.triviallyUnsolvable)
                networkManager.triviallyUnsolvable(this);
        }
//        int commCountdown = primeWorker ? 5 : 0;
        while (true) {
//            if (pStack.size() == 0 || commCountdown <= 0) {
//                if (!networkManager.manageNetwork(this))
//                    return;
//                commCountdown = 5;
//            }
            if (pStack.size() == 0) {
                System.out.println("Number of Solutions = "+solutionCount);
                networkManager.manageNetwork(this);
                return;
            }
//            System.out.println(pathState.toString());
            if (pStack.size() > 2 && returned()) {
                if (pathState.complete()) {
                    System.out.println("Solution Found by " + Thread.currentThread());
//                    if(!networkManager.manageNetwork(this))
//                        return;
                    solutionCount++;
                    System.out.println();
                    System.out.println("Solution Found!");
                    System.out.println();System.out.println();
                    System.out.print(pathState);
                    System.out.println(printSolution());
                    System.out.println("Solution Found!");
                }
                pStack.pop();
                dStack.clearLevel();
                pathState.backtrackConstraints(cStack);
                backtrack();
                continue;
            }
            if (!pshMove()) backtrack();
//            commCountdown--;
        }
    }
    
    private String printSolution() {
        StringBuilder builder = new StringBuilder();
        String newLine = System.getProperty("line.separator");
        builder.append((getRow(pathState.sPos)+1)+" "+(getCol(pathState.sPos)+1));
        builder.append(newLine);
        int count = 0;
        for (int i = dStack.levels()-1; i >= 0; i--) {
            switch (dStack.peekVal(i)) {
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
        while (true) {
            assert dStack.levels() == pStack.size();
            dStack.popVal();
            pathState.backtrackConstraints(cStack);
            assert dStack.levels() == pStack.size();
            if (brnchMove()) return;
            assert dStack.levels() != pStack.size();
            pStack.pop();
            assert dStack.levels() == pStack.size();
            if (pStack.size() == 0) return;
        }
    }
    
    private void pshInit() {
        pStack.push(pathState.sPos);
        pushDirs();
        cStack.finishLevel(); // Here we seal the first - empty - set of constraints
        pushPebble(0); // The sPos is always the 0th element in the pebble array
        pushPebble(nearestPebbleMatrix[0][1]); // Grab the nearest pebbles to the starting pebble
        pathState.setConstraints(pStack,dStack,cStack,pathState);
    }
    
    private void pushPebble(int pIdx) {
        assert (markablePebbles[pIdx]&MASK_PEBBLE_MARK) != MASK_PEBBLE_MARK;
        pebbleStack.push(pIdx);
        nextPebblePos = markablePebbles[pIdx];
        nextPebbleIdx = pIdx;
        markablePebbles[pIdx] |= MASK_PEBBLE_MARK;
    }
    
    private void popPebble() {
        assert (markablePebbles[pebbleStack.peek()]&MASK_PEBBLE_MARK) == MASK_PEBBLE_MARK;
        int pIdx = pebbleStack.pop();
        markablePebbles[nextPebbleIdx] &= MASK_PEBBLE_VAL;
        if (pebbleStack.size() > 0) {
            nextPebbleIdx = pebbleStack.peek();
            nextPebblePos = markablePebbles[nextPebbleIdx]&MASK_PEBBLE_VAL;
        }
        else {
            nextPebbleIdx = -1;
        }
    }

    private boolean brnchMove() {
        assert pStack.size() > 0;
        assert dStack.levels() > 0;
        int cPos = pStack.peek();
        assert (cPos != pathState.sPos || pStack.size() == 1);
        while (!dStack.clearLevelIfEmpty()) {
            int cDir = dStack.peekVal();
            if ((pStack.size() == 1 || cDir != (dStack.peekVal(1)^1)) && !pathState.isForbidden(cPos,cDir)) {
                if(pStack.size() == 1 || pathState.legal(pStack.peek(1),dStack.peekVal(1),cPos,cDir)) { // The initial position push doesn't obey the law
                    pathState.setConstraints(pStack,dStack,cStack,pathState);
                    assert pStack.size() == dStack.levels();
                    return true;
                }
            }
            dStack.popVal();
        }
        return false;
    }
    
    private boolean pshMove() {
        int cPos = pStack.peek();
        int cDir = dStack.peekVal();
        int nPos = SearchUtils.nxtPos(cPos, cDir, pathState.width, pathState.totalSqrs);
        pushDirs();
        while (!dStack.clearLevelIfEmpty()) {
            int nDir = dStack.peekVal();
            if (!(nDir == (cDir^1)) && !pathState.isForbidden(nPos,nDir)) {
                if(pathState.legal(cPos,cDir,nPos,nDir) || pStack.size() == 1) { // The initial position push doesn't obey the law
                    pStack.push(nPos);
                    pathState.setConstraints(pStack,dStack,cStack,pathState);
                    assert pStack.size() == dStack.levels();
                    return true;
                }
            }
            dStack.popVal();
        } 
        assert pStack.size() == dStack.levels();
        return false;
    }
    
    private void pushDirs() {
        dStack.pushVal(UP);
        dStack.pushVal(DOWN);
        dStack.pushVal(LEFT);
        dStack.pushVal(RIGHT);
        dStack.finishLevel();
    }
    
//    private void pshMoveReceivedWork(int dir) {
//        if (dir == FILTERED_MAGIC_DIR) {
//            pStack.push(MAGIC_POS);
//            dStack.push(MAGIC_DIR);
//            cStack.push(0);
//            return;
//        }
//        int cPos = pStack.peek();
//        int cDir = SearchUtils.filterDir(dStack.peek());
//        int nPos = SearchUtils.nxtPos(cPos,cDir,pathState.sPos,pathState.width,pathState.totalSqrs);
//        assert dir != (cDir^1);
//        assert !pathState.isForbidden(nPos,dir);
//        assert pathState.legal(dStack,cPos,cDir,nPos,dir) || cDir == MAGIC_DIR; // The magic dir skirts around legality
//        pStack.push(nPos);
//        dStack.push(dir);
//        if ((dir & MASK_SHARED) != MASK_SHARED) {
//            sharableWork++;
//            assert verifyWorkSize();
//        }
//        pathState.setConstraints(pStack,dStack,cStack,pathState);
//    }
    
    private boolean returned() {
        int nxtPos = SearchUtils.nxtPos(pStack.peek(),dStack.peekVal(),pathState.width,pathState.totalSqrs);
        return nxtPos == pathState.sPos;
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

    @Override
    public Object giveWork() {
//        assert verifyWorkSize();
//        IntStack sharedStack = new IntStack(dStack.size());
//        boolean share = sharableWork%2 == 0;
//        int shareTarget = sharableWork-1;
//        int shareCount = 0;
//        int i = 0;
//        for (;i < dStack.size(); i++) {
//            int dir = dStack.get(i);
//            if (!SearchUtils.isSharedDir(dir)) {
//                if (shareCount == shareTarget) {
//                    assert !share;
//                    break; // This is done to prevent the last unshared branchable element from being shared
//                }
//                if (share) {
//                    sharedStack.set(i,dir);
//                    dStack.set(i,SearchUtils.makeSharedDir(dir));
//                    sharableWork--;
//                }
//                else {
//                    sharedStack.set(i,SearchUtils.makeSharedDir(dir));
//                }
//                share = !share;
//                shareCount++;
//            }
//            else {
//                sharedStack.set(i,dir);
//            }
//        }
//        sharedStack.setSize(i);
//        assert verifyWorkSize();
//        return sharedStack;
        return null;
    }
    
    private boolean verifyWorkSize() {
        int workSizeCount = 0;
        for (int i = 0; i < dStack.levels(); i++) {
            int dir = dStack.peekVal(i);
            if (!SearchUtils.isSharedDir(dir)) {
                workSizeCount++;
            }
        }
        return workSizeCount == sharableWork;
    }
    
    @Override
    public void receiveWork(Object rStack) {
//        assert dStack.levels() == 0;
//        assert pStack.size() == 0;
//        assert cStack.size() == 0;
//        IntStack stack = (IntStack)rStack;
//        for (int i = 0; i < stack.size(); i++) {
//            int dir = stack.get(i);
//            pshMoveReceivedWork(SearchUtils.filterDir(dir));
//        }
    }

    @Override
    public int sharableWork() {
        assert verifyWorkSize();
        return sharableWork;
    }

    @Override
    public boolean needsWork() {
        return pStack.size() == 0;
    }

    @Override
    public boolean isComplete() {
        return pStack.size() > 2 && returned() && pathState.complete();
    }

    @Override
    public Object getSuccessMessage() {
        return printSolution();
    }

    @Override
    public Object getFailureMessage() {
        return "No Solution Found";
    }
}