package org.francis.intel.challenge.stack;

public interface IntStack {

    public abstract void push(int pInt);

    public abstract int pop();

    public abstract int peek();

    public abstract int peek(int lookback);

    public abstract int size();

    public abstract void setSize(int size);
    
    public abstract int get(int i);
    
    public abstract void set(int i, int val);
}