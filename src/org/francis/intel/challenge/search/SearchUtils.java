package org.francis.intel.challenge.search;

public class SearchUtils implements Constants {

    public static byte complementDir(int dir) {
        return (byte)(dir^1);
    }
    
    public static byte forbidDir(byte dir) {
        return (byte)(1 << (dir+LEFT_SHIFT_OFFSET));
    }
    
    public static byte allowOnlyDir(byte dir) {
        byte forbidMask = (byte)(1 << (dir+LEFT_SHIFT_OFFSET));
        return (byte)(forbidMask ^ MASK_CONSTRS);
    }
    
    public static int nxtPos(int pos, byte dir, int sPos, int width, int totalLength) {
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
