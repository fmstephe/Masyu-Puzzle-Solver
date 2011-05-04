package org.francis.intel.challenge.search;

import org.francis.intel.challenge.stack.IntStack;
import org.francis.intel.challenge.stack.ResizingIntStack;
import org.francis.p2p.worksharing.network.NetworkManager;
import org.francis.p2p.worksharing.network.WorkSharer;

public class MasyuSearcher implements Constants, WorkSharer, Runnable {
    
    final PathState pathState;
    final IntStack pStack;
    final IntStack dStack;
    final ResizingIntStack cStack;
    final NetworkManager networkManager;
    int sharableWork;
    int solutionCount = 0;
    boolean primeWorker;
    
    public MasyuSearcher(int height, int width, int[] board, NetworkManager networkManager, boolean primeWorker) {
        assert height*width == board.length;
        this.sharableWork = 0;
        this.pathState = new PathState(board,width,height);
        this.pStack = new IntStack(pathState.totalSqrs+2);
        this.dStack = new IntStack(pathState.totalSqrs+2);
        this.cStack = new ResizingIntStack(4*(pathState.totalSqrs+2));
        this.networkManager = networkManager;
        this.primeWorker = primeWorker;
    }
    
    @Override
    public void run() {
        search();
    }
    
    public void search() {
        StringBuilder result = new StringBuilder();
        System.out.println(pathState);
        if (primeWorker) {
            pshInit();
            if (pathState.triviallyUnsolvable)
                networkManager.triviallyUnsolvable(this);
        }
        int commCountdown = primeWorker ? 5 : 0;
        while (true) {
            if (pStack.size() == 0 || commCountdown <= 0) {
                if (!networkManager.manageNetwork(this))
                    return;
                commCountdown = 5;
            }
            System.out.println(pathState.toString());
            if (pStack.size() > 2 && returned()) {
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
                int dir = dStack.pop();
                if (!SearchUtils.isSharedDir(dir)) sharableWork--; 
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
        for (int i = dStack.size()-1; i >= 0; i--) {
            switch (SearchUtils.filterDir(dStack.peek(i))) {
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
        while (true) {
            pStack.pop();
            int dir = dStack.pop();
            pathState.backtrackConstraints(cStack);
            if (pStack.size() == 0) return;
            if (!SearchUtils.isSharedDir(dir)) {
                sharableWork--;
                if (!pshMove(++nDir)) return;
            }
                
        }
    }
    
    private void pshInit() {
        pStack.push(MAGIC_POS);
        dStack.push(MAGIC_DIR);
        cStack.push(0); // Here we add the number of constraints added, not many
    }

    private boolean pshMove() {
        return pshMove(UP);
    }
    
    private boolean pshMove(int initDir) {
        assert pStack.size() == dStack.size();
        int cPos = pStack.peek();
        int cDir = SearchUtils.filterDir(dStack.peek());
        int nPos = SearchUtils.nxtPos(cPos,cDir,pathState.sPos,pathState.width,pathState.totalSqrs);
        if (nPos == pathState.sPos && cDir != FILTERED_MAGIC_DIR) {
            pStack.push(nPos);
            dStack.push(EMPTY);
            sharableWork++;
            assert verifyWorkSize();
            pathState.setConstraints(pStack,dStack,cStack,pathState);
            return true;
        }
        for (int nDir = initDir; nDir < NOTHING_LEFT; nDir++) {
            if (nDir == (cDir^1)) continue;
            if (pathState.isForbidden(nPos,nDir)) continue;
            if(pathState.legal(dStack,cPos,cDir,nPos,nDir) || cDir == FILTERED_MAGIC_DIR) { // The magic dir skirts around legality
                pStack.push(nPos);
                dStack.push(nDir);
                sharableWork++;
                assert verifyWorkSize();
                pathState.setConstraints(pStack,dStack,cStack,pathState);
                return true;
            }
        }
        return false;
    }
    
    private void pshMoveReceivedWork(int dir) {
        assert pStack.size() == dStack.size();
        if (dir == FILTERED_MAGIC_DIR) {
            pStack.push(MAGIC_POS);
            dStack.push(MAGIC_DIR);
            cStack.push(0);
            return;
        }
        int cPos = pStack.peek();
        int cDir = SearchUtils.filterDir(dStack.peek());
        int nPos = SearchUtils.nxtPos(cPos,cDir,pathState.sPos,pathState.width,pathState.totalSqrs);
        assert dir != (cDir^1);
        assert !pathState.isForbidden(nPos,dir);
        assert pathState.legal(dStack,cPos,cDir,nPos,dir) || cDir == MAGIC_DIR; // The magic dir skirts around legality
        pStack.push(nPos);
        dStack.push(dir);
        if ((dir & MASK_SHARED) != MASK_SHARED) {
            sharableWork++;
            assert verifyWorkSize();
        }
        pathState.setConstraints(pStack,dStack,cStack,pathState);
    }
    
    private boolean returned() {
        return pStack.peek() == pathState.sPos;
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
        assert verifyWorkSize();
        IntStack sharedStack = new IntStack(dStack.size());
        boolean share = sharableWork%2 == 0;
        int shareTarget = sharableWork-1;
        int shareCount = 0;
        int i = 0;
        for (;i < dStack.size(); i++) {
            int dir = dStack.get(i);
            if (!SearchUtils.isSharedDir(dir)) {
                if (shareCount == shareTarget) {
                    assert !share;
                    break; // This is done to prevent the last unshared branchable element from being shared
                }
                if (share) {
                    sharedStack.set(i,dir);
                    dStack.set(i,SearchUtils.makeSharedDir(dir));
                    sharableWork--;
                }
                else {
                    sharedStack.set(i,SearchUtils.makeSharedDir(dir));
                }
                share = !share;
                shareCount++;
            }
            else {
                sharedStack.set(i,dir);
            }
        }
        sharedStack.setSize(i);
        assert verifyWorkSize();
        return sharedStack;
    }
    
    private boolean verifyWorkSize() {
        int workSizeCount = 0;
        for (int i = 0; i < dStack.size(); i++) {
            int dir = dStack.peek(i);
            if (!SearchUtils.isSharedDir(dir)) {
                workSizeCount++;
            }
        }
        return workSizeCount == sharableWork;
    }

    @Override
    public void receiveWork(Object rStack) {
        assert dStack.size() == 0;
        assert pStack.size() == 0;
        assert cStack.size() == 0;
        IntStack stack = (IntStack)rStack;
        for (int i = 0; i < stack.size(); i++) {
            int dir = stack.get(i);
            pshMoveReceivedWork(SearchUtils.filterDir(dir));
        }
    }

    @Override
    public int sharableWork() {
        assert verifyWorkSize();
        return sharableWork;
    }

    @Override
    public boolean needsWork() {
        assert pStack.size() == dStack.size();
        return dStack.size() == 0;
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