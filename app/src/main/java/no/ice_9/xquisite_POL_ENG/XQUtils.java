package no.ice_9.xquisite_POL_ENG;

/**
 * Created by human on 12.04.16.
 */
public class XQUtils {
    public static long ByteArr2Int(byte[] b, int offset)
    {
        return ((0xFF & b[offset]) << 24) | ((0xFF & b[offset+1]) << 16) |
                ((0xFF & b[offset+2]) << 8) | (0xFF & b[offset+3]);
    }

    public static byte[] Int2ByteArr(byte[] b,long i,int offset)
    {
        //byte[] b=new byte[4];

        b[offset+0] = (byte)((i>>24) & 0xFF);
        b[offset+1] = (byte)((i>>16) & 0xFF);
        b[offset+2] = (byte)((i>>8) & 0xFF);
        b[offset+3] = (byte)(i & 0xFF);

        return b;
    }
}
