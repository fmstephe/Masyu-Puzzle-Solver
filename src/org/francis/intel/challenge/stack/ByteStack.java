package org.francis.intel.challenge.stack;

import java.util.Arrays;

public class ByteStack {
    private final byte[] stack;
    private int idx = 0;
    
    public ByteStack(int size) {
        stack = new byte[size];
    }
    
    public void push(byte pInt) {
        stack[idx++] = pInt;
    }
    
    public byte pop() {
        return stack[--idx];
    }
    
    public byte peek() {
        return stack[idx-1];
    }
    
    public byte peek(int lookback) {
        return stack[idx-(lookback+1)];
    }
    
    public int size() {
        return idx;
    }
    
    @Override
    public String toString() {
        byte[] print = new byte[idx];
        System.arraycopy(stack, 0, print, 0, idx);
        return Arrays.toString(print);
    }
}
