package org.francis.intel.challenge.stack;

import java.util.Arrays;

public class ByteStack {
    private final int[] stack;
    private int idx = 0;
    
    public ByteStack(int size) {
        stack = new int[size];
    }
    
    public void push(int pInt) {
        stack[idx++] = pInt;
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
