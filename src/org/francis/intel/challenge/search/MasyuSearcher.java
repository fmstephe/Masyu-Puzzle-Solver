package org.francis.intel.challenge.search;

import org.francis.intel.challenge.stack.IntStack;
import org.francis.intel.challenge.stack.LevelStack;
import org.francis.intel.challenge.stack.UnsafeIntStack;
import org.francis.p2p.worksharing.network.NetworkManager;
import org.francis.p2p.worksharing.network.WorkSharer;

public class MasyuSearcher implements Constants, WorkSharer, Runnable {
    
    final PathState pathState;
    final IntStack pStack;
    final IntStack pebbleIdxStack;
    final LevelStack dStack;
    final LevelStack cStack;
    final int[] markablePebbles; // Local mutable array - must be copied from the one passed in
    final int[][] nearestPebbleMatrix; // Globally shared data-structure, don't fuck with it
    final NetworkManager networkManager;
    int nextPebblePos;
    int sharableWork;
    boolean primeWorker;
    
    public MasyuSearcher(int height, int width, int[] board, int[] pebbles, int[][] nearestPebbleMatrix, NetworkManager networkManager, boolean primeWorker) {
        assert height*width == board.length;
        this.sharableWork = 0;
        this.pathState = new PathState(board,pebbles,width,height);
        this.pStack = new UnsafeIntStack(pathState.totalSqrs+2);
        this.dStack = new LevelStack(5*(pathState.totalSqrs+2));
        this.cStack = new LevelStack(4*(pathState.totalSqrs+2));
        this.pebbleIdxStack = new UnsafeIntStack(pebbles.length+2);
        this.markablePebbles = pebbles.clone();
        this.nearestPebbleMatrix = nearestPebbleMatrix;
        this.networkManager = networkManager;
        this.primeWorker = primeWorker;
    }
    
    @Override
    public void run() {
        try {
            search();
        }
        catch (Exception e) {
            e.printStackTrace();
            System.err.println(pathState);
        }
    }
    
    public void search() {
        int solutionCount = 0;
        if (primeWorker) {
//            System.out.println(pathState);
            if (!pshInit() || pathState.triviallyUnsolvable) {
                networkManager.triviallyUnsolvable(this);
                return;
            }
        }
        else {
            if (!networkManager.manageNetwork(this))
                return;
        }
        int commCountdown = primeWorker ? 5 : 0;
        while (true) {
            if (pStack.size() == 0 || commCountdown <= 0) {
                if (!networkManager.manageNetwork(this))
                    return;
                commCountdown = 5;
            }
//            if (pStack.size() == 0) {
//                System.out.println("Number of Solutions = "+solutionCount);
//                networkManager.manageNetwork(this);
//                return;
//            }
//            System.out.println(pathState.toString());
            if (pStack.size() > 2 && returned()) {
                if (pathState.complete()) {
//                    System.out.println("Solution Found by " + Thread.currentThread());
//                    solutionCount++;
//                    System.out.println();
//                    System.out.println("Solution Found!");
//                    System.out.println();System.out.println();
//                    System.out.print(pathState);
                    if(!networkManager.manageNetwork(this))
                        return;
                }
                popPos();
                int rawDir = dStack.peekVal();
                if (!SearchUtils.isSharedDir(rawDir))
                    sharableWork--;
                dStack.clearLevel();
                pathState.backtrackConstraints(cStack);
                backtrack();
                continue;
            }
            if (!pshMove()) backtrack();
            commCountdown--;
        }
    }
    
    private String printSolution() {
        StringBuilder builder = new StringBuilder();
        String newLine = System.getProperty("line.separator");
        builder.append((getRow(pathState.sPos)+1)+" "+(getCol(pathState.sPos)+1));
        builder.append(newLine);
        int count = 0;
        for (int i = dStack.levels()-1; i >= 0; i--) {
            switch (SearchUtils.getDirVal(dStack.peekVal(i))) {
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
            assert verifyWorkSize();
            assert dStack.levels() == pStack.size();
            assert dStack.levels() == cStack.levels();
            if (brnchMove()) {
                assert verifyWorkSize();
                return;
            }
            if (pStack.size() == 0) return;
        }
    }
    
    private boolean pshInit() {
        sharableWork++; // We are quite sloppy with some pushing and incrementing here - because if this step doesn't work out we don't backtrack
        pushPos(pathState.sPos);
        pushDirs(pathState.sPos);
        while (!dStack.isEmptyLevel()) {
            int sDir = SearchUtils.getDirVal(dStack.peekVal());
            if (!pathState.isForbidden(pathState.sPos,sDir)) {
                pathState.setConstraints(pStack,dStack,cStack,pathState);
                break;
            }
            dStack.popVal();
        }
        if (dStack.isEmptyLevel()) {
            return false;
        }
        assert pStack.size() == dStack.levels();
        assert pStack.size() == cStack.levels();
        return true;
    }
    
    private void pushPebble(int pIdx) {
        assert (markablePebbles[pIdx]&MASK_PEBBLE_MARK) != MASK_PEBBLE_MARK;
        assert pebbleIdxStack.size() == 0 || pathState.getPebbleIdx(nextPebblePos) != pebbleIdxStack.peek();
        pebbleIdxStack.push(pIdx);
        markablePebbles[pIdx] |= MASK_PEBBLE_MARK;
        int[] nearestPebbles = nearestPebbleMatrix[pIdx];
        // This first element in nearestPebbles always points to itself
        assert nearestPebbles[0] == pIdx;
        for (int i = 1; i < nearestPebbles.length; i++) {
            int nearPebbleIdx = nearestPebbles[i];
            if ((markablePebbles[nearPebbleIdx]&MASK_PEBBLE_MARK) != MASK_PEBBLE_MARK) {
                assert nearPebbleIdx != pIdx;
                nextPebblePos = markablePebbles[nearPebbleIdx];
                assert pathState.getPebbleIdx(nextPebblePos) != pebbleIdxStack.peek();
                return;
            }
        }
        // We have run out of pebbles to target so head back to the start again
        nextPebblePos = pathState.sPos;
        assert pathState.getPebbleIdx(nextPebblePos) != pebbleIdxStack.peek();
    }
    
    private void popPebble() {
        assert pebbleIdxStack.size() > 0;
        assert (markablePebbles[pebbleIdxStack.peek()]&MASK_PEBBLE_MARK) == MASK_PEBBLE_MARK;
        assert pathState.getPebbleIdx(nextPebblePos) != pebbleIdxStack.peek();
        int pIdx = pebbleIdxStack.pop();
        markablePebbles[pIdx] &= MASK_PEBBLE_VAL;
        nextPebblePos = markablePebbles[pIdx];
        assert (markablePebbles[pIdx]&MASK_PEBBLE_MARK) != MASK_PEBBLE_MARK;
        assert pebbleIdxStack.size() == 0 || pathState.getPebbleIdx(nextPebblePos) != pebbleIdxStack.peek();
    }
    
    private void pushPos(int pos) {
        int pushPos = pos;
        if (pos == nextPebblePos || pathState.getBoardVal(pos) != EMPTY) {
            pushPos = pos | MASK_POS_PEBBLE;
            pushPebble(pathState.getPebbleIdx(pos));
        }
        pStack.push(pushPos);
    }
    
    private void popPos() {
        int rawPos = pStack.pop();
        if ((rawPos&MASK_POS_PEBBLE) == MASK_POS_PEBBLE) {
            popPebble();
        }
    }

    private boolean brnchMove() {
        assert pStack.size() > 0;
        assert dStack.levels() > 0;
        assert verifyWorkSize();
        assert dStack.levels() == pStack.size();
        assert dStack.levels() == cStack.levels();
        int cPos = SearchUtils.getPosVal(pStack.peek());
        boolean isShared = SearchUtils.isSharedDir(dStack.popVal());
        pathState.backtrackConstraints(cStack);
        if (isShared) {
            popPos();
            dStack.clearLevel();
            assert verifyWorkSize();
            assert dStack.levels() == pStack.size();
            assert dStack.levels() == cStack.levels();
            return false;
        }
        assert (cPos != pathState.sPos || pStack.size() == 1);
        while (!dStack.clearLevelIfEmpty()) {
            int cDir = SearchUtils.getDirVal(dStack.peekVal());
            int pDir = pStack.size() == 1 ? -1 : SearchUtils.getDirVal(dStack.peekVal(1));
            int pPos = pStack.size() == 1 ? -1 : SearchUtils.getPosVal(pStack.peek(1));
            if (cDir != (pDir^1) && !pathState.isForbidden(cPos,cDir)) {
                if(pStack.size() == 1 || pathState.legal(pPos,pDir,cPos,cDir)) { // The initial position push doesn't obey the law
                    pathState.setConstraints(pStack,dStack,cStack,pathState);
                    assert verifyWorkSize();
                    assert dStack.levels() == pStack.size();
                    assert dStack.levels() == cStack.levels();
                    return true;
                }
            }
            dStack.popVal();
        }
        popPos();
        sharableWork--;
        assert verifyWorkSize();
        assert dStack.levels() == pStack.size();
        assert dStack.levels() == cStack.levels();
        return false;
    }
    
    private boolean pshMove() {
        assert pathState.checkBoardState();
        assert verifyWorkSize();
        assert dStack.levels() == pStack.size();
        assert dStack.levels() == cStack.levels();
        assert pStack.size() != 0;
        int cPos = SearchUtils.getPosVal(pStack.peek());
        int cDir = SearchUtils.getDirVal(dStack.peekVal());
        int nPos = SearchUtils.nxtPos(cPos, cDir, pathState.width, pathState.totalSqrs);
        sharableWork++;
        pushDirs(nPos);
        while (!dStack.clearLevelIfEmpty()) {
            int nDir = SearchUtils.getDirVal(dStack.peekVal());
            if (!(nDir == (cDir^1)) && !pathState.isForbidden(nPos,nDir)) {
                if(pathState.legal(cPos,cDir,nPos,nDir)) { // The initial position push doesn't obey the law
                    pushPos(nPos);
                    pathState.setConstraints(pStack,dStack,cStack,pathState);
                    assert verifyWorkSize();
                    assert dStack.levels() == pStack.size();
                    assert dStack.levels() == cStack.levels();
                    return true;
                }
            }
            dStack.popVal();
        } 
        sharableWork--;
        assert verifyWorkSize();
        assert dStack.levels() == pStack.size();
        assert dStack.levels() == cStack.levels();
        return false;
    }
    
    private void pshMoveReceivedWork(int nDir, int cDir) {
        if (pStack.size() == 0) {
            pushPos(pathState.sPos);
            pathState.setConstraints(pStack,dStack,cStack,pathState);
            assert pStack.size() == dStack.levels();
            assert pStack.size() == cStack.levels();
        }
        else {
            int cPos = SearchUtils.getPosVal(pStack.peek());
            int nPos = SearchUtils.nxtPos(cPos,cDir,pathState.width,pathState.totalSqrs);
            assert nDir != (cDir^1);
            assert !pathState.isForbidden(nPos,nDir);
            assert pathState.legal(cPos,cDir,nPos,nDir);
            pushPos(nPos);
            pathState.setConstraints(pStack,dStack,cStack,pathState);
            assert pStack.size() == dStack.levels();
            assert pStack.size() == cStack.levels();
        }
    }
    
    private void pushDirs(int nPos) {
        int cX = getCol(nPos);
        int cY = getRow(nPos);
        int nX = getCol(nextPebblePos);
        int nY = getRow(nextPebblePos);
        int leftOrRight = (cX-nX) < 0 ? RIGHT : LEFT;
        int upOrDown = (cY-nY) < 0 ? DOWN : UP;
        int hPos = SearchUtils.nxtPos(nPos, leftOrRight, pathState.width, pathState.totalSqrs);
        int vPos = SearchUtils.nxtPos(nPos, upOrDown, pathState.width, pathState.totalSqrs);
        int hX = getCol(hPos);
        int hY = getRow(hPos);
        int vX = getCol(vPos);
        int vY = getRow(vPos);
        int hDistance = Math.abs(hX-nX) + Math.abs(hY-nY);
        int vDistance = Math.abs(vX-nX) + Math.abs(vY-vY);
        if (vDistance < hDistance) { // Move up and down first
            dStack.pushVal(SearchUtils.complementDir(leftOrRight));
            dStack.pushVal(SearchUtils.complementDir(upOrDown));
            dStack.pushVal(leftOrRight);
            dStack.pushVal(upOrDown);
            dStack.finishLevel();
        }
        else { // Move side to side first
            dStack.pushVal(SearchUtils.complementDir(leftOrRight));
            dStack.pushVal(SearchUtils.complementDir(upOrDown));
            dStack.pushVal(upOrDown);
            dStack.pushVal(leftOrRight);
            dStack.finishLevel();
        }
    }
    
    private boolean returned() {
        int cPos = SearchUtils.getPosVal(pStack.peek());
        int cDir = SearchUtils.getDirVal(dStack.peekVal());
        int nxtPos = SearchUtils.nxtPos(cPos,cDir,pathState.width,pathState.totalSqrs);
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

    public Object giveWork() {
        return giveWorkBreadth();
    }
    
    public Object giveWorkBreadth() {
        assert verifyWorkSize();
        LevelStack sharedStack = new LevelStack(dStack);
        int retainTarget = (sharableWork/2)+(sharableWork%2);
        int retainCount = 0;
        for (int i = 0; i < dStack.levels(); i++) {
            if (!SearchUtils.isSharedDir(dStack.peekVal(i))) {
                if (retainCount < retainTarget) {
                    sharedStack.orLevel(i, MASK_DIR_SHARED);
                    retainCount++;
                }
                else {
                    dStack.orLevel(i, MASK_DIR_SHARED);
                    sharableWork--;
                }
            }
        }
        assert verifyWorkSize();
        while (SearchUtils.isSharedDir(sharedStack.peekVal()))
            sharedStack.clearLevel();
        return sharedStack;
    }
    
    public Object giveWorkDepth() {
        assert verifyWorkSize();
        LevelStack sharedStack = new LevelStack(dStack);
        boolean share = false;
        for (int i = 0; i < dStack.levels(); i++) {
            if (!SearchUtils.isSharedDir(dStack.peekVal(i))) {
                if (share) {
                    dStack.orLevel(i, MASK_DIR_SHARED);
                    sharableWork--;
                }
                share = !share;
            }
        }
        share = true;
        for (int i = 0; i < sharedStack.levels(); i++) {
            if (!SearchUtils.isSharedDir(sharedStack.peekVal(i))) {
                if (share) {
                    sharedStack.orLevel(i, MASK_DIR_SHARED);
                }
                share = !share;
            }
        }
        assert verifyWorkSize();
        if (SearchUtils.isSharedDir(sharedStack.peekVal()))
            sharedStack.clearLevel();
        return sharedStack;
    }
    
    private boolean verifyWorkSize() {
        int workSizeCount = 0;
        for (int i = 0; i < dStack.levels(); i++) {
            if (!SearchUtils.isSharedDir(dStack.peekVal(i))) {
                workSizeCount++;
            }
        }
        if (workSizeCount == sharableWork)
            return true;
        else
            return false;
    }
    
    @Override
    public void receiveWork(Object rObj) {
        assert dStack.levels() == 0;
        assert pStack.size() == 0;
        assert cStack.levels() == 0;
        LevelStack rStack = (LevelStack)rObj;
        for (int i = rStack.levels()-1; i >= 0; i--) {
            if (!SearchUtils.isSharedDir(rStack.peekVal(i)))
                sharableWork++;
            int cDir = -1;
            if (dStack.levels() > 0)
                cDir = SearchUtils.getDirVal(dStack.peekVal());
            rStack.pushLevelInto(i, dStack);
            pshMoveReceivedWork(SearchUtils.getDirVal(rStack.peekVal(i)),cDir);
        }
        backtrack();
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