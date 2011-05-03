package org.francis.intel.challenge.search;

public interface Constants {
 // Playing board values
    public static final int BLACK = 1;
    public static final int WHITE = 2;
    public static final int EMPTY = 0;
    // Path-mask values
    public static final int EMPTY_PATH = 0;
    public static final int UP = 2;
    public static final int DOWN = 3;
    public static final int LEFT = 4;
    public static final int RIGHT = 5;
    public static final int NOTHING_LEFT = 6;
    public static final int MAGIC_DIR = 8; // This direction always magically points to the starting position
    public static final int NOT_UP = 16;
    public static final int NOT_DOWN = 32;
    public static final int NOT_LEFT = 64;
    public static final int NOT_RIGHT = 128;
    public static final int CLOSED = NOT_UP | NOT_DOWN | NOT_LEFT | NOT_RIGHT;
    // Mask for isolating path
    public static final int MASK_PATH = Integer.parseInt("00000111",2);
    // Mask for isolating the forbidden flags
    public static final int MASK_CONSTRS = Integer.parseInt("11110000",2);
    // XOR mask for changing orientation UP -> LEFT, DOWN -> RIGHT
    public static final int FLIP_ORIENTATION = Byte.parseByte("00000110",2);
    // Left-shift offset for turn a direction into a NOT_* flag
    // Used as: int flag = 1 << (dir+LEFT_SHIFT_OFFSET);
    public static final int LEFT_SHIFT_OFFSET = 2;
}
