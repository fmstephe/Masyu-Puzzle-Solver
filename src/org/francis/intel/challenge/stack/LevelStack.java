package org.francis.intel.challenge.stack;

public class LevelStack {
    
    private int levelCount;
    private int depthCounter;
    private final ResizingIntStack dStack;
    
    public LevelStack(int size) {
        levelCount = 0;
        depthCounter = 0;
        dStack = new ResizingIntStack(size);
    }
    
    public int peekVal() {
        assert dStack.peek() != 0;
        return dStack.peek(1);
    }
    
    public int peekVal(int backLook) {
        int backCount = 0;
        int peekIdx = 0;
        while (backCount < backLook) {
            int depth = dStack.peek(peekIdx);
            assert depth != 0;
            peekIdx += depth+1;
            backLook++;
        }
        return dStack.peek(peekIdx+1);
    }
    
    public void pushVal(int dir) {
        dStack.push(dir);
        depthCounter++;
    }

    public int popVal() {
        int depth = dStack.pop();
        assert depth > 0;
        int dir = dStack.pop();
        dStack.push(--depth);
        // NB: If the depth of this level became 0 from this pop - you must call clearLevel before calling pop again
        return dir;
    }
    
    public void sealLevel() {
        dStack.push(depthCounter);
        depthCounter = 0;
        levelCount++;
    }
    
    public void clearLevel() {
        // TODO we need some assertions which distinguish between depth counters and dir values
        int depth = dStack.pop();
        dStack.setSize(dStack.size()-depth);
        levelCount--;
    }
    
    public boolean isEmptyLevel() {
        return dStack.peek() == 0;
    }
    
    public boolean clearLevelIfEmpty() {
        if (isEmptyLevel()) {
            clearLevel();
            return true;
        }
        return false;
    }
    
    public int levels() {
        return levelCount;
    }
}
