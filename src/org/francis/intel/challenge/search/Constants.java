package org.francis.intel.challenge.search;

public interface Constants {
 // Playing board values
    public static final byte BLACK = 1;
    public static final byte WHITE = 2;
    public static final byte EMPTY = 0;
    // Path-mask values
    public static final byte EMPTY_PATH = 0;//Byte.parseByte("1000000",2);
    public static final byte UP = 2;
    public static final byte DOWN = 3;
    public static final byte LEFT = 4;
    public static final byte RIGHT = 5;
    public static final byte NOTHING_LEFT = 6;
    public static final byte MAGIC_DIR = 8; // This direction always magically points to the starting position
    public static final byte NOT_UP = 16;
    public static final byte NOT_DOWN = 32;
    public static final byte NOT_LEFT = 64;
    public static final byte NOT_RIGHT = Byte.MIN_VALUE;
    public static final byte CLOSED = NOT_UP | NOT_DOWN | NOT_LEFT | NOT_RIGHT;
 // Mask for isolating path
    public static final byte MASK_PATH = Byte.parseByte("00000111",2);
    // Mask for isolating the forbidden flags
    public static final byte MASK_CONSTRS = (byte)-16;
    // XOR mask for changing orientation UP -> LEFT, DOWN -> RIGHT
    public static final byte FLIP_ORIENTATION = Byte.parseByte("00000110",2);
    // Left-shift offset for turn a direction into a NOT_* flag
    // Used as: byte flag = 1 << (dir+LEFT_SHIFT_OFFSET);
    public static final byte LEFT_SHIFT_OFFSET = 2;
}
