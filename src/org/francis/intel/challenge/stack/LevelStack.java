package org.francis.intel.challenge.stack;

public class LevelStack {
    
    private int levelCount;
    private int depthCounter;
    private final ResizingIntStack intStack;
    
    public LevelStack(int size) {
        levelCount = 0;
        depthCounter = 0;
        intStack = new ResizingIntStack(size);
    }
    
    public LevelStack(LevelStack levelStack) {
        this.levelCount = levelStack.levelCount;
        this.depthCounter = levelStack.depthCounter;
        this.intStack = new ResizingIntStack(levelStack.intStack);
    }

    public int peekVal() {
        assert intStack.peek() != 0;
        return intStack.peek(1);
    }
    
    public int peekVal(int backLook) {
        assert backLook < levelCount;
        int peekIdx = getBackwardLevelIndex(backLook);
        assert intStack.peek(peekIdx) != 0;
        return intStack.peek(peekIdx+1);
    }
    
    private int getBackwardLevelIndex(int backLook) {
        int backCount = 0;
        int peekIdx = 0;
        while (backCount < backLook) {
            int depth = intStack.peek(peekIdx);
            peekIdx += depth+1;
            backCount++;
        }
        return peekIdx;
    }
    
    public void pushVal(int val) {
        intStack.push(val);
        depthCounter++;
    }

    public int popVal() {
        int depth = intStack.pop();
        assert depth > 0;
        int dir = intStack.pop();
        intStack.push(--depth);
        // NB: If the depth of this level became 0 from this pop - you must call clearLevel before calling pop again
        return dir;
    }
    
    public void finishLevel() {
        intStack.push(depthCounter);
        depthCounter = 0;
        levelCount++;
    }
    
    public void clearLevel() {
        assert depthCounter == 0;
        assert levelCount > 0;
        int depth = intStack.pop();
        intStack.setSize(intStack.size()-depth);
        levelCount--;
    }
    
    public boolean clearLevelIfEmpty() {
        if (isEmptyLevel()) {
            clearLevel();
            return true;
        }
        return false;
    }
    
    public void orLevel(int backLook, int mask) {
        assert backLook < levelCount;
        assert depthCounter == 0;
        assert intStack.size() != 0;
        int peekIdx = getBackwardLevelIndex(backLook);
        int levelDepth = intStack.peek(peekIdx);
        int bottomPeekIdx = peekIdx+levelDepth+1;
        assert levelDepth != 0;
        for (int i = 0; i < levelDepth; i++) {
            intStack.set(intStack.size()-(bottomPeekIdx-i),intStack.get(intStack.size()-(bottomPeekIdx-i))|mask);
        }
        assert true; // Handy debug point
    }
    
    public void pushLevelInto(int backLook, LevelStack receivingStack) {
        assert backLook < levelCount;
        assert depthCounter == 0;
        assert intStack.size() != 0;
        int peekIdx = getBackwardLevelIndex(backLook);
        int levelDepth = intStack.peek(peekIdx);
        int bottomPeekIdx = peekIdx+levelDepth+1;
        assert levelDepth != 0;
        for (int i = 0; i < levelDepth; i++) {
            int val = intStack.get(intStack.size()-(bottomPeekIdx-i));
            receivingStack.pushVal(val);
        }
        receivingStack.finishLevel();
    }
    
    public boolean isEmptyLevel() {
        return intStack.peek() == 0;
    }
    
    public int levels() {
        return levelCount;
    }

    public void replicateHere(LevelStack rStack) {
        intStack.replicateHere(rStack.intStack);
        this.depthCounter = rStack.depthCounter;
        this.levelCount = rStack.levelCount;
    }
}
