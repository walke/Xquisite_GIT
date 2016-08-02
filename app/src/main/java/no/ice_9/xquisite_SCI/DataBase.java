package no.ice_9.xquisite_SCI;

import android.app.Activity;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Created by human on 16.05.16.
 *
 * Local database
 *
 * made of blocks
 * all blocks are written to single file TODO: think of better system! now file is being rewritten each time some block is being deleted. unsafe?
 * this class is separated from DeviceData class to provide more flexible structure of database
 *
 */
public class DataBase {

    File mDataFile;//data file
    byte[] mDataBuffer; //memory for reading data file to memory TODO: read parts?
    Block[] mDataBlocks; //memory for translating buffer to blocks

    /**
     * Structure of data block
     */
    class Block
    {
        //each block gets its part from total buffer TODO: 2x high memory consumtion!!
        byte[] mBuffer;

        //Block id
        int mId;
        //Block type id
        int mTypeId;
        /**
         *Block of data contains different data related to the application
         * every block has a unique id and type id
         * @param buffer buffer to build a block from
         */
        public Block(byte[] buffer)
        {
            mBuffer=buffer;
            mId=(int)XQUtils.ByteArr2Int(buffer,0);
            mTypeId=(int)XQUtils.ByteArr2Int(buffer,16);

        }
    }

    /**
     * Constructor of the database
     * @param activity requires to use activities methods
     */
    public DataBase(Activity activity)
    {
        mDataFile=new File(activity.getExternalFilesDir("DAT").getPath(),"xq.dat");
        try{
        Log.d("DATA", "" + mDataFile.getCanonicalPath());}
        catch (Exception io){}

    }

    /**
     * clears all local data
     */
    public void clear()
    {
        mDataFile.delete();
        load();
    }

    /**
     * Loads local data
     */
    public void load()
    {
        mDataBuffer=null;
        mDataBlocks=null;
        if(!mDataFile.exists())
        {
            try
            {
                mDataFile.createNewFile();

            }
            catch (Exception io){Log.e("MAIN", "could not create data file");}
        }

        int size=getDataSize();
        mDataBuffer=new byte[size];
        if(size>0)
        {
            loadDataToBuffer();
        }
        else{mDataBuffer=new byte[0];}
        Log.d("DATA","buf"+mDataBuffer);
        getDataBlocks(mDataBuffer);
    }

    /**
     * saves data to local file
     */
    public void save()
    {
        buildBuffer();

        if(!mDataFile.exists())
        {
            try
            {
                mDataFile.createNewFile();
            }
            catch (Exception io){Log.e("DATA", "could not create data file");return;}
        }

        saveBufferToFile();
    }

    /**
     * reads data file and returns its size
     * @return size of the data file
     */
    public int getDataSize()
    {
        int fs=0;
        try
        {

            FileInputStream fi=new FileInputStream(mDataFile);
            fs=fi.available();
            fi.close();
        }catch (Exception io){Log.e("DATA", "could not get file size");}

        return fs;
    }

    /**
     * reads data file and stores it in a byte buffer
     */
    private void loadDataToBuffer()
    {
        try
        {
            FileInputStream fi=new FileInputStream(mDataFile);
            int fs=fi.available();

            mDataBuffer=new byte[fs];

            fi.read(mDataBuffer,0,fs);

            fi.close();
        }catch (Exception io){Log.e("DATA", "could not load file to buffer");}
    }

    /**
     * translates buffer into blocks of data
     * @param buffer to get blocks from
     */
    private void getDataBlocks(byte[] buffer)
    {

        if(buffer.length<4)return;
        int redBytes=0;

        while(redBytes<buffer.length)
        {
            Log.d("DATA","buf4:"+buffer[0]+"."+buffer[1]+"."+buffer[2]+"."+buffer[3]);
            int blocksize=(int)XQUtils.ByteArr2Int(buffer,redBytes);redBytes+=4;
            if((blocksize+redBytes)>buffer.length)return;

            byte[] blockbuffer=new byte[blocksize];
            for(int i=0;i<blocksize;i++)
            {
                blockbuffer[i]=buffer[redBytes];redBytes++;
            }
            //Log.d("DATA","buflen:"+blockbuffer.length+","+buffer.length);
            addBlock(blockbuffer);

        }




    }

    /**
     * adds new block of data to memory based on data read from local data file
     * @param blockbuffer raw buffer cut out of the data file
     */
    public Block addBlock(byte[] blockbuffer)
    {
        Block[] tmp;
        if(mDataBlocks==null)
        {
            tmp=new Block[0];
        }
        else
        {
            tmp=mDataBlocks;
        }

        mDataBlocks=new Block[tmp.length+1];

        for(int i=0;i<tmp.length;i++)
        {
            mDataBlocks[i]=tmp[i];
        }

        mDataBlocks[tmp.length]=new Block(blockbuffer);

        return mDataBlocks[tmp.length];
    }

    /**
     * builds a buffer out of blocks
     */
    private void buildBuffer()
    {
        int totalSize=0;
        for(int i=0;i<mDataBlocks.length;i++)
        {
            totalSize+=4;
            totalSize+=mDataBlocks[i].mBuffer.length;
        }

        mDataBuffer=new byte[totalSize];
        int pointer=0;
        for(int i=0;i<mDataBlocks.length;i++)
        {
            XQUtils.Int2ByteArr(mDataBuffer,mDataBlocks[i].mBuffer.length,pointer);pointer+=4;

            for(int j=0;j<mDataBlocks[i].mBuffer.length;j++)
            {
                mDataBuffer[pointer]=mDataBlocks[i].mBuffer[j];pointer++;
            }

        }
    }

    /**
     * saves buffer into local data file
     */
    private void saveBufferToFile()
    {
        try
        {

            FileOutputStream fi=new FileOutputStream(mDataFile);
            Log.d("DATA","buf4:"+mDataBuffer[0]+"."+mDataBuffer[1]+"."+mDataBuffer[2]+"."+mDataBuffer[3]);
            fi.write(mDataBuffer,0,mDataBuffer.length);

            fi.close();
        }catch (Exception io){Log.e("DATA", "could not save buffer");}
    }

    public Block[] getAllBlocks()
    {
        Block[] blocks =new Block[mDataBlocks.length];

        for(int i=0; i<mDataBlocks.length;i++)
        {

                blocks[i]=mDataBlocks[i];

        }
        return blocks;
    }

    /**
     * loops through all blocks and returns only matching type
     * @param type type of the block
     * @return all blocks of matching type
     */
    public Block[] getBlocksByBlockType(int type)
    {
        Block[] blocks;
        int count=0;
        if(mDataBlocks==null){return null;}
        for(int i=0; i<mDataBlocks.length;i++)
        {
            if(mDataBlocks[i].mTypeId==type)
            {
                count++;
            }
        }
        blocks=new Block[count];

        int pointer=0;
        for(int i=0; i<mDataBlocks.length;i++)
        {
            if(mDataBlocks[i].mTypeId==type)
            {
                blocks[pointer]=mDataBlocks[i];pointer++;
            }
        }
        return blocks;
    }

    public Block getBlocksByBlockId(int id)
    {

        int count=0;
        if(mDataBlocks==null){return null;}
        for(int i=0; i<mDataBlocks.length;i++)
        {
            if(mDataBlocks[i].mId==id)
            {
                return mDataBlocks[i];
            }
        }

        return null;
    }

    /**
     * loops all block and gets empty block id
     *
     * @return
     */
    public int getEmptyId()
    {
        int id=0;
        if(mDataBlocks==null)return id;
        for(int i=0;i<mDataBlocks.length;i++)
        {
            if(id==mDataBlocks[i].mId)
            {
                id++;
                i=0;
            }
        }
        return id;
    }



}
