package org.francis.intel.challenge.search;

public class SearchUtils implements Constants {

    public static int complementDir(int dir) {
        return (dir^1);
    }
    
    public static int forbidDir(int dir) {
        return (1 << (dir+LEFT_SHIFT_OFFSET));
    }
    
    public static int allowOnlyDir(int dir) {
        int forbidMask = (int)(1 << (dir+LEFT_SHIFT_OFFSET));
        return (forbidMask ^ MASK_CONSTRS);
    }
    
    public static int forbidPerpendicular(int dir) {
        int forbidMask = (int)(1 << (dir+LEFT_SHIFT_OFFSET));
        int forbidCMask = (int)(1 << (complementDir(dir)+LEFT_SHIFT_OFFSET));
        return (forbidMask ^ forbidCMask ^ MASK_CONSTRS);
    }
    
    public static int nxtPos(int pos, int dir, int width, int totalLength) {
        switch (dir) {
            case UP :
                return pos-width;
            case DOWN : 
                int np = pos+width;
                return np >= totalLength ? -1 : np;
            case LEFT : 
                int col = pos%width;
                return col == 0 ? -1 : pos-1;
            case RIGHT : 
                col = pos%width;
                return col == width-1 ? -1 : pos+1;
            default : throw new IllegalArgumentException();
        }
    }
    
    // Removes the shared bit from a dir
    public static int filterDir(int dir) {
        return dir & MASK_DIR;
    }
    
    
    public static boolean isSharedDir(int dir) {
        return (dir & MASK_SHARED) == MASK_SHARED;
    }
    
    public static int makeSharedDir(int dir) {
        return dir | MASK_SHARED;
    }
}
