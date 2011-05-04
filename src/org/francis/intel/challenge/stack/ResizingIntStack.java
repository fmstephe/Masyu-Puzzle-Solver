package org.francis.intel.challenge.stack;

import java.util.Arrays;

public class ResizingIntStack implements IntStack {

    private int[] stack;
    private int idx = 0;
    
    public ResizingIntStack(int size) {
        stack = new int[size];
    }
    
    /* (non-Javadoc)
     * @see org.francis.intel.challenge.stack.IIntStack#push(int)
     */
    @Override
    public void push(int pInt) {
        if (idx == stack.length)
            resize();
        stack[idx++] = pInt;
    }
    
    private void resize() {
        int[] newStack = new int[stack.length*2];
        System.arraycopy(stack, 0, newStack, 0, stack.length);
        stack = newStack;
    }

    /* (non-Javadoc)
     * @see org.francis.intel.challenge.stack.IIntStack#pop()
     */
    @Override
    public int pop() {
        return stack[--idx];
    }
    
    /* (non-Javadoc)
     * @see org.francis.intel.challenge.stack.IIntStack#peek()
     */
    @Override
    public int peek() {
        return stack[idx-1];
    }
    
    /* (non-Javadoc)
     * @see org.francis.intel.challenge.stack.IIntStack#peek(int)
     */
    @Override
    public int peek(int lookback) {
        return stack[idx-(lookback+1)];
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
    public String toString() {
        int[] print = new int[idx];
        System.arraycopy(stack, 0, print, 0, idx);
        return Arrays.toString(print);
    }
}