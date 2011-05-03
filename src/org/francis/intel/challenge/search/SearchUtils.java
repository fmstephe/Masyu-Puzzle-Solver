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
    
    public static int nxtPos(int pos, int dir, int sPos, int width, int totalLength) {
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
            case MAGIC_DIR :
                return sPos;
            default : throw new IllegalArgumentException();
        }
    }
}
