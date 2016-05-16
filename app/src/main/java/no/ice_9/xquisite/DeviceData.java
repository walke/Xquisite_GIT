package no.ice_9.xquisite;

import android.app.Activity;

/**
 * Created by human on 16.05.16.
 */
public class DeviceData {

    static int BLOCKTYPE_DEVICE_ID  = 0;
    static int BLOCKTYPE_STORY      = 1;
    static int BLOCKTYPE_STORY_PART = 2;

    DataBase mDataBase;
    
    //DEVICE BLOCKS
    DataBase.Block mDeviceId;

    public DeviceData(Activity activity)
    {
        mDataBase=new DataBase(activity);
       // mDataBase.clear();
        mDataBase.load();


    }

    public void setDeviceId(int id)
    {
        byte[] buffer=new byte[12];

        int emptyId=mDataBase.getEmptyId();

        XQUtils.Int2ByteArr(buffer,emptyId,0);
        XQUtils.Int2ByteArr(buffer,BLOCKTYPE_DEVICE_ID,4);
        XQUtils.Int2ByteArr(buffer,id,8);

        mDataBase.addBlock(buffer);
        mDataBase.save();
    }

    public int getDeviceId()
    {
        int id=0;
        DataBase.Block[] tmp=mDataBase.getBlocksByBlockType(BLOCKTYPE_DEVICE_ID);
        if(tmp==null)
        {
            setDeviceId(1);
            id= 1;
        }
        else
        {
            id=(int)XQUtils.ByteArr2Int(tmp[tmp.length-1].mBuffer,8);
        }
        //byte[] idmDeviceId.getData();
        return id;
    }
}
