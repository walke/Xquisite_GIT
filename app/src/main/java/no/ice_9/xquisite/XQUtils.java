package no.ice_9.xquisite;

/**
 * Created by human on 12.04.16.
 */
public class XQUtils {
    public static long ByteArr2Int(byte[] b)
    {
        return ((0xFF & b[0]) << 24) | ((0xFF & b[1]) << 16) |
                ((0xFF & b[2]) << 8) | (0xFF & b[3]);
    }

    public static byte[] Int2ByteArr(long i)
    {
        byte[] b=new byte[4];

        b[0] = (byte)((i>>24) & 0xFF);
        b[1] = (byte)((i>>16) & 0xFF);
        b[2] = (byte)((i>>8) & 0xFF);
        b[3] = (byte)(i & 0xFF);

        return b;
    }
}
