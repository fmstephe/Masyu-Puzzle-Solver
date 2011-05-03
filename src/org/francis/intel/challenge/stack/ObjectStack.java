package org.francis.intel.challenge.stack;

import java.util.Arrays;

public class ObjectStack<E> {

    private final E[] stack;
    private int idx = 0;
    
    public ObjectStack(int size) {
        stack = (E[])new Object[size];
    }
    
    public void push(E elem) {
        stack[idx++] = elem;
    }
    
    public E pop() {
        return stack[--idx];
    }
    
    public E peek() {
        return stack[idx-1];
    }
    
    public E peek(int lookback) {
        return stack[idx-(lookback+1)];
    }
    
    public int size() {
        return idx;
    }
    
    @Override
    public String toString() {
        Object[] print = new Object[idx];
        System.arraycopy(stack, 0, print, 0, idx);
        return Arrays.toString(print);
    }
}