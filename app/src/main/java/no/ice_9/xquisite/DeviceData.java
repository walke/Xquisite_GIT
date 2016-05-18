package no.ice_9.xquisite;

import android.app.Activity;
import android.util.Log;

/**
 * Created by human on 16.05.16.
 */
public class DeviceData {

    //BLOCK TYPES
    static public int BLOCKTYPE_DEVICE  = 0;
    static public int BLOCKTYPE_STORY      = 1;
    static public int BLOCKTYPE_STORY_PART = 2;
    static public int BLOCKTYPE_STRING = 3;

    //BLOCK SIZES
    static public int BLOCKSIZE_DEVICE=16;
    static public int BLOCKSIZE_STORY=36;
    static public int BLOCKSIZE_PART=40;

    //OFFSETS
    static public int BLOCKOFFSET_GLOBAL_block_id=0;
    static public int BLOCKOFFSET_GLOBAL_type=4;
    static public int BLOCKOFFSET_GLOBAL_id=8;
    static public int BLOCKOFFSET_DEVICE_id = 8;

    static public int BLOCKOFFSET_STORY_id=8;
    static public int BLOCKOFFSET_STORY_parent=12;
    static public int BLOCKOFFSET_STORY_on_server=16;
    static public int BLOCKOFFSET_STORY_complete=20;
    static public int BLOCKOFFSET_STORY_parts=24;
    static public int BLOCKOFFSET_STORY_first_part=28;
    static public int BLOCKOFFSET_STORY_last_part=32;

    static public int BLOCKOFFSET_PART_id=8;
    static public int BLOCKOFFSET_PART_story=12;
    static public int BLOCKOFFSET_PART_on_server=16;
    static public int BLOCKOFFSET_PART_question=20;
    static public int BLOCKOFFSET_PART_fname=24;
    static public int BLOCKOFFSET_PART_next_part=28;
    static public int BLOCKOFFSET_PART_previous_part=32;
    static public int BLOCKOFFSET_PART_file_size=36;

    static public int BLOCKOFFSET_STRING_id=8;


    DataBase mDataBase;
    
    //DEVICE BLOCKS
    DataBase.Block mDeviceId;

    public DeviceData(Activity activity)
    {
        mDataBase=new DataBase(activity);
       // mDataBase.clear();
        mDataBase.load();


    }

    public void clear()
    {
        mDataBase.clear();
    }

    public void setDeviceId(int id)
    {
        byte[] buffer=new byte[BLOCKSIZE_DEVICE];

        int emptyId=mDataBase.getEmptyId();

        XQUtils.Int2ByteArr(buffer,emptyId,0);
        XQUtils.Int2ByteArr(buffer,BLOCKTYPE_DEVICE,BLOCKOFFSET_GLOBAL_type);
        XQUtils.Int2ByteArr(buffer,id,BLOCKOFFSET_DEVICE_id);

        mDataBase.addBlock(buffer);
        mDataBase.save();
    }

    public int getDeviceId()
    {
        int id=0;
        DataBase.Block[] tmp=mDataBase.getBlocksByBlockType(BLOCKTYPE_DEVICE);
        if(tmp==null || tmp.length==0)
        {
            setDeviceId(1);
            id= 1;
        }
        else
        {
            id=(int)XQUtils.ByteArr2Int(tmp[tmp.length-1].mBuffer,BLOCKOFFSET_DEVICE_id);
        }
        //byte[] idmDeviceId.getData();
        return id;
    }

    public int getEmptyTypeId(int type)
    {
        int id=1;
        DataBase.Block[] tmp=mDataBase.getBlocksByBlockType(type);

        if(tmp==null)
        {

            return id;
        }
        else
        {
            for(int i=0;i<tmp.length;i++)
            {
                int cid=(int)XQUtils.ByteArr2Int(tmp[i].mBuffer,BLOCKOFFSET_GLOBAL_id);
                if(id==cid){id++;i=0;}

            }

        }
        //byte[] idmDeviceId.getData();
        return id;
    }



    public void addStory(int id, int parent, int complete)
    {
        byte[] buffer=new byte[BLOCKSIZE_STORY];

        int emptyId=mDataBase.getEmptyId();

        XQUtils.Int2ByteArr(buffer,emptyId,BLOCKOFFSET_GLOBAL_block_id);
        XQUtils.Int2ByteArr(buffer,BLOCKTYPE_STORY,BLOCKOFFSET_GLOBAL_type);
        XQUtils.Int2ByteArr(buffer,id,BLOCKOFFSET_STORY_id);
        XQUtils.Int2ByteArr(buffer,parent,BLOCKOFFSET_STORY_parent);
        XQUtils.Int2ByteArr(buffer,0,BLOCKOFFSET_STORY_on_server);
        XQUtils.Int2ByteArr(buffer,complete,BLOCKOFFSET_STORY_complete);
        XQUtils.Int2ByteArr(buffer,0,BLOCKOFFSET_STORY_parts);
        XQUtils.Int2ByteArr(buffer,0,BLOCKOFFSET_STORY_first_part);
        XQUtils.Int2ByteArr(buffer,0,BLOCKOFFSET_STORY_last_part);

        mDataBase.addBlock(buffer);
        mDataBase.save();
    }

    public DataBase.Block getStoryNdx(int id)
    {
        DataBase.Block[] stories=mDataBase.getBlocksByBlockType(BLOCKTYPE_STORY);
        for(int i=0;i<stories.length;i++)
        {
            int cid=(int)XQUtils.ByteArr2Int(stories[i].mBuffer,BLOCKOFFSET_STORY_id);
            if (cid==id)return stories[i];
        }
        return null;
    }

    public void addStoryPart(DataBase.Block story,StoryPart part,boolean up)
    {
        byte[] buffer=new byte[BLOCKSIZE_PART];

        DataBase.Block questionBlock=addStringBlock(part.getQuestion());
        DataBase.Block fnameBlock=addStringBlock(part.getFilePath());

        int emptyId=mDataBase.getEmptyId();
        int emptyPartId=getEmptyTypeId(BLOCKTYPE_STORY_PART);


        XQUtils.Int2ByteArr(buffer,emptyId,BLOCKOFFSET_GLOBAL_block_id);//BLOCK ID
        XQUtils.Int2ByteArr(buffer,BLOCKTYPE_STORY_PART,BLOCKOFFSET_GLOBAL_type);//BLOCK TYPE
        XQUtils.Int2ByteArr(buffer,emptyPartId,BLOCKOFFSET_PART_id);//PART ID
        XQUtils.Int2ByteArr(buffer,story.mId,BLOCKOFFSET_PART_story);//STORY BLOCK ID
        XQUtils.Int2ByteArr(buffer,0,BLOCKOFFSET_PART_on_server);//PART ON SERVER STATUS
        XQUtils.Int2ByteArr(buffer,questionBlock.mId,BLOCKOFFSET_PART_question);//QUESTION STRING BLOCK ID
        XQUtils.Int2ByteArr(buffer, fnameBlock.mId, BLOCKOFFSET_PART_fname);//FILENAME STRING BLOCK ID
        XQUtils.Int2ByteArr(buffer,0,BLOCKOFFSET_PART_next_part);//NEXT PART BLOCK ID
        XQUtils.Int2ByteArr(buffer,0,BLOCKOFFSET_PART_previous_part);//PREVIOUS PART BLOCK ID

        DataBase.Block thisBlock=mDataBase.addBlock(buffer);
        DataBase.Block lastInStory=story;
        int lastid=(int)XQUtils.ByteArr2Int(lastInStory.mBuffer,BLOCKOFFSET_STORY_first_part);
        while(lastid!=0)
        {
            lastInStory=mDataBase.getBlocksByBlockId(lastid);
            lastid=(int)XQUtils.ByteArr2Int(lastInStory.mBuffer,BLOCKOFFSET_PART_next_part);
        }

        XQUtils.Int2ByteArr(lastInStory.mBuffer,thisBlock.mId,BLOCKOFFSET_PART_next_part);
        int curnoofstories=(int)XQUtils.ByteArr2Int(story.mBuffer,BLOCKOFFSET_STORY_parts);
        XQUtils.Int2ByteArr(story.mBuffer,curnoofstories+1,BLOCKOFFSET_STORY_parts);
        mDataBase.save();
    }

    private DataBase.Block addStringBlock(String str)
    {
        byte[] buffer=new byte[12+str.length()];

        int emptyId=mDataBase.getEmptyId();
        int emptystirngId=getEmptyTypeId(BLOCKTYPE_STRING);


        XQUtils.Int2ByteArr(buffer,emptyId,BLOCKOFFSET_GLOBAL_block_id);//BLOCK ID
        XQUtils.Int2ByteArr(buffer,BLOCKTYPE_STRING,BLOCKOFFSET_GLOBAL_type);//BLOCK TYPE
        XQUtils.Int2ByteArr(buffer,emptystirngId,BLOCKOFFSET_STRING_id);//PART ID
        for(int i=0;i<str.length();i++)
        {
            buffer[12+i]=(byte)str.charAt(i);
        }

        DataBase.Block block=mDataBase.addBlock(buffer);
        mDataBase.save();

        return block;
    }

    public int completeStory(int id,boolean up)
    {
        DataBase.Block storyBlock=getStoryNdx(id);

        XQUtils.Int2ByteArr(storyBlock.mBuffer,1,BLOCKOFFSET_STORY_complete);

        mDataBase.save();

        return storyBlock.mId;
    }

    public StoryPart getStoryPart(int story,int part)
    {
        StoryPart storyPart=new StoryPart();
        DataBase.Block storyBlock=getStoryNdx(story);
        int partBlockId=(int)XQUtils.ByteArr2Int(storyBlock.mBuffer,BLOCKOFFSET_STORY_first_part);
        DataBase.Block partBlock=mDataBase.getBlocksByBlockId(partBlockId);

        for(int i=0;i<part;i++)
        {
            partBlockId=(int)XQUtils.ByteArr2Int(partBlock.mBuffer,BLOCKOFFSET_PART_next_part);
            partBlock=mDataBase.getBlocksByBlockId(partBlockId);
        }

        DataBase.Block questionBlock=mDataBase.getBlocksByBlockId((int)XQUtils.ByteArr2Int(partBlock.mBuffer,BLOCKOFFSET_PART_question));
        String question="";
        //for (int i=12;i<questionBlock.mBuffer.length;i++){question+=questionBlock.mBuffer[i];}
        question=new String(questionBlock.mBuffer,12,questionBlock.mBuffer.length-12);
        DataBase.Block fnameBlock=mDataBase.getBlocksByBlockId((int)XQUtils.ByteArr2Int(partBlock.mBuffer,BLOCKOFFSET_PART_fname));
        String fname="";
        //for (int i=12;i<fnameBlock.mBuffer.length;i++){fname+=fnameBlock.mBuffer[i];}
        fname=new String(fnameBlock.mBuffer,12,fnameBlock.mBuffer.length-12);

        storyPart.populate("", question, fname);

        return storyPart;
    }

    public int[] getLastStory()
    {
        DataBase.Block[] blocks = mDataBase.getBlocksByBlockType(BLOCKTYPE_STORY);
        if(blocks==null || blocks.length==0)return new int[]{0,0};
        int pointer=blocks.length-1;
        DataBase.Block last=blocks[pointer];
        boolean done=false;
        while(!done)
        {
            int complete=(int)XQUtils.ByteArr2Int(last.mBuffer,BLOCKOFFSET_STORY_complete);

            if(complete==1)done=true;
            else
            {
                pointer--;
                if(pointer<0)return new int[]{0,0};
                else last=blocks[pointer];
            }
        }
        int[] result=new int[2];
        result[0]=(int)XQUtils.ByteArr2Int(last.mBuffer, BLOCKOFFSET_STORY_id);
        Log.d("DEVDATA","complete"+result[0]);
        result[1]=(int)XQUtils.ByteArr2Int(last.mBuffer, BLOCKOFFSET_STORY_parts);
        return result;
    }

    public String getAllData()
    {
        String ret = "";

        int size=mDataBase.getDataSize();
        ret+="\n datafile size:"+size+"\n";

        DataBase.Block[] blocks;

        blocks=mDataBase.getBlocksByBlockType(BLOCKTYPE_DEVICE);
        ret+="\n device id history:\n";
        for(int i=0;i<blocks.length;i++)
        {
            int id=(int)XQUtils.ByteArr2Int(blocks[i].mBuffer,BLOCKOFFSET_DEVICE_id);
            ret+=i+": "+id+"\n";
        }
        ret+="-----\n";

        blocks=mDataBase.getBlocksByBlockType(BLOCKTYPE_STORY);
        ret+="stories:"+blocks.length+"\n";
        for(int i=0;i<blocks.length;i++)
        {
            ret+="story:"+i+"\n";
            int id=(int)XQUtils.ByteArr2Int(blocks[i].mBuffer,BLOCKOFFSET_STORY_id);
            int parent=(int)XQUtils.ByteArr2Int(blocks[i].mBuffer,BLOCKOFFSET_STORY_parent);
            int complete=(int)XQUtils.ByteArr2Int(blocks[i].mBuffer,BLOCKOFFSET_STORY_complete);
            int parts=(int)XQUtils.ByteArr2Int(blocks[i].mBuffer,BLOCKOFFSET_STORY_parts);
            ret+="\t"+"id: "+"\t"+id+"\n";
            ret+="\t"+"parent: "+"\t"+parent+"\n";
            ret+="\t"+"complete: "+"\t"+complete+"\n";
            ret+="\t"+"number of parts: "+"\t"+parts+"\n";

            int partblockid=(int)XQUtils.ByteArr2Int(blocks[i].mBuffer,BLOCKOFFSET_STORY_first_part);
            DataBase.Block partBlock=mDataBase.getBlocksByBlockId(partblockid);
            while(partblockid!=0)
            {
                ret+="\t"+"\t"+"part\n";
                id=(int)XQUtils.ByteArr2Int(partBlock.mBuffer,BLOCKOFFSET_PART_id);
                int storyblockid=(int)XQUtils.ByteArr2Int(partBlock.mBuffer,BLOCKOFFSET_PART_story);
                DataBase.Block storyblock=mDataBase.getBlocksByBlockId(storyblockid);
                int storyid=(int)XQUtils.ByteArr2Int(storyblock.mBuffer,BLOCKOFFSET_STORY_id);
                int questionblockid=(int)XQUtils.ByteArr2Int(partBlock.mBuffer,BLOCKOFFSET_PART_question);
                DataBase.Block questionblock=mDataBase.getBlocksByBlockId(questionblockid);
                String question=new String(questionblock.mBuffer,12,questionblock.mBuffer.length-12);
                int fnameblockid=(int)XQUtils.ByteArr2Int(partBlock.mBuffer,BLOCKOFFSET_PART_fname);
                DataBase.Block fnameblock=mDataBase.getBlocksByBlockId(fnameblockid);
                String fname=new String(fnameblock.mBuffer,12,fnameblock.mBuffer.length-12);
                ret+="\t"+"\t"+"id: "+"\t"+id+"\n";
                ret+="\t"+"\t"+"story id: "+"\t"+storyid+"\n";
                ret+="\t"+"\t"+"question: "+"\t"+question+"\n";
                ret+="\t"+"\t"+"file: "+"\t"+fname+"\n";

                partblockid=(int)XQUtils.ByteArr2Int(partBlock.mBuffer,BLOCKOFFSET_PART_next_part);
                partBlock=mDataBase.getBlocksByBlockId(partblockid);
            }

            ret+="\n";
        }
        ret+="-----\n";

        blocks=mDataBase.getAllBlocks();



        ret+="number of data blocks:"+blocks.length+"\n";
        for(int i=0;i<blocks.length;i++)
        {
            ret+="block:"+i+"\n";
            ret+="   id:\t"+blocks[i].mId+"\n";
            ret+="   type:\t"+blocks[i].mTypeId+"\n";
            ret+="   size:\t"+blocks[i].mBuffer.length+"\n";
            ret+="\n";
        }
        return ret;
    }

//TODO: count parts for story
}
