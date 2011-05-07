package org.francis.intel.challenge.stack;

import java.util.Arrays;

public class ResizingIntStack implements IntStack {

    private int[] stackA;
    private int idx = 0;
    
    public ResizingIntStack(int size) {
        stackA = new int[size];
    }
    
    public ResizingIntStack(ResizingIntStack intStack) {
        this.stackA = intStack.stackA.clone();
        this.idx = intStack.idx;
    }

    /* (non-Javadoc)
     * @see org.francis.intel.challenge.stack.IIntStack#push(int)
     */
    @Override
    public void push(int pInt) {
        if (idx == stackA.length)
            resize();
        stackA[idx++] = pInt;
    }
    
    private void resize() {
        int[] newStack = new int[stackA.length*2];
        System.arraycopy(stackA, 0, newStack, 0, stackA.length);
        stackA = newStack;
    }

    /* (non-Javadoc)
     * @see org.francis.intel.challenge.stack.IIntStack#pop()
     */
    @Override
    public int pop() {
        return stackA[--idx];
    }
    
    /* (non-Javadoc)
     * @see org.francis.intel.challenge.stack.IIntStack#peek()
     */
    @Override
    public int peek() {
        return stackA[idx-1];
    }
    
    /* (non-Javadoc)
     * @see org.francis.intel.challenge.stack.IIntStack#peek(int)
     */
    @Override
    public int peek(int lookback) {
        return stackA[idx-(lookback+1)];
    }
    
    /* (non-Javadoc)
     * @see org.francis.intel.challenge.stack.IIntStack#size()
     */
    @Override
    public int size() {
        return idx;
    }
    
    /* (non-Javadoc)
     * @see org.francis.intel.challenge.stack.IIntStack#setSize(int)
     */
    @Override
    public void setSize(int size) {
        this.idx = size;
    }
    
    @Override
    public void set(int i, int val) {
        assert i < idx;
        stackA[i] = val;
    }
    
    @Override
    public int get(int i) {
        assert i < idx;
        return stackA[i];
    }
    
    @Override
    public String toString() {
        int[] print = new int[idx];
        System.arraycopy(stackA, 0, print, 0, idx);
        return Arrays.toString(print);
    }

    public void replicateHere(ResizingIntStack replayStack) {
        if (stackA.length >= replayStack.idx) {
            System.arraycopy(replayStack.stackA, 0, stackA, 0, replayStack.idx);
        }
        else {
            stackA = replayStack.stackA.clone();
        }
        idx = replayStack.idx;
    }
}