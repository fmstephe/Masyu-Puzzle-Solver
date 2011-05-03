package org.francis.intel.challenge.stack;

import java.util.Arrays;

public class ResizingIntStack {

    private int[] stack;
    private int idx = 0;
    
    public ResizingIntStack(int size) {
        stack = new int[size];
    }
    
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

    public int pop() {
        return stack[--idx];
    }
    
    public int peek() {
        return stack[idx-1];
    }
    
    public int peek(int lookback) {
        return stack[idx-(lookback+1)];
    }
    
    public int size() {
        return idx;
    }
    
    @Override
    public String toString() {
        int[] print = new int[idx];
        System.arraycopy(stack, 0, print, 0, idx);
        return Arrays.toString(print);
    }
}