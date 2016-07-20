package no.ice_9.xquisite;

import android.app.Activity;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

/**
 * Created by human on 16.05.16.
 */
public class DeviceData {

    //BLOCK TYPES
    static public int BLOCKTYPE_DEVICE          = 0;
    static public int BLOCKTYPE_STORY           = 1;
    static public int BLOCKTYPE_STORY_PART      = 2;
    static public int BLOCKTYPE_STRING          = 3;
    static public int BLOCKTYPE_LANGUAGE_LIST   = 4;
    static public int BLOCKTYPE_LANGUAGE        = 5;

    //BLOCK SIZES
    static public int BLOCKSIZE_DEVICE      =28;
    static public int BLOCKSIZE_STORY       =52;
    static public int BLOCKSIZE_PART        =60;

    //OFFSETS
    //global
    static public int BLOCKOFFSET_GLOBAL_block_id=0;
    static public int BLOCKOFFSET_GLOBAL_block_created=4;
    static public int BLOCKOFFSET_GLOBAL_block_modified=8;
    static public int BLOCKOFFSET_GLOBAL_block_server_status=12;
    static public int BLOCKOFFSET_GLOBAL_type=16;
    static public int BLOCKOFFSET_GLOBAL_id=20;
    static public int BLOCKOFFSET_DEVICE_id = 20;

    //story
    static public int BLOCKOFFSET_STORY_id=20;
    static public int BLOCKOFFSET_STORY_parent=24;
    static public int BLOCKOFFSET_STORY_on_server=28;
    static public int BLOCKOFFSET_STORY_complete=32;
    static public int BLOCKOFFSET_STORY_parts=36;
    static public int BLOCKOFFSET_STORY_first_part=40;
    static public int BLOCKOFFSET_STORY_last_part=44;
    static public int BLOCKOFFSET_STORY_date_added=48;

    //part
    static public int BLOCKOFFSET_PART_id=20;
    static public int BLOCKOFFSET_PART_story=24;
    static public int BLOCKOFFSET_PART_on_server=28;
    static public int BLOCKOFFSET_PART_question=32;
    static public int BLOCKOFFSET_PART_fname=36;
    static public int BLOCKOFFSET_PART_next_part=40;
    static public int BLOCKOFFSET_PART_previous_part=44;
    static public int BLOCKOFFSET_PART_file_size=48;
    static public int BLOCKOFFSET_PART_text=52;
    static public int BLOCKOFFSET_PART_type=56;

    //string
    static public int BLOCKOFFSET_STRING_id=20;

    //language list
    static public int BLOCKOFFSET_LANGLIST_id=20;
    static public int BLOCKOFFSET_LANGLIST_first=24;
    static public int BLOCKOFFSET_LANGLIST_last=28;

    //language
    static public int BLOCKOFFSET_LANGUAGE_id=20;
    static public int BLOCKOFFSET_LANGUAGE_next=24;
    static public int BLOCKOFFSET_LANGUAGE_previous=28;
    static public int BLOCKOFFSET_LANGUAGE_title=32;
    static public int BLOCKOFFSET_LANGUAGE_last_story=36;



    DataBase mDataBase;
    String videoDir;

    //DEVICE BLOCKS
    DataBase.Block mDeviceId;

    /**
     * Device data constructor
     * holding:
     *  device id for syncing with server
     *  story data and its parts
     * @param activity Activity is passed to get access to filesystem
     */
    public DeviceData(Activity activity)
    {
        videoDir=activity.getExternalFilesDir("VID").toString();
        mDataBase=new DataBase(activity);
       // mDataBase.clear();
        mDataBase.load();


    }

    /**
     * clear data function
     * clears all data related to the app
     *
     */
    public void clear()
    {
        mDataBase.clear();


        Runtime runtime=Runtime.getRuntime();
        try {
            runtime.exec("rm -r " + videoDir);
        } catch (IOException e) { }
    }

    /**
     * set device id (1 is set by default in case no sychronization occured yet)
     * @param id device id
     */
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

    /**
     * as it sounds get current device id
     * @return device id
     */
    public int getDeviceId()
    {
        int id;
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

    /**
     * find not occupied id to store block
     * TODO: maybe make reservation to avoid writing two blocks with same id
     * @param type
     * @return
     */
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


    /**
     * add new story and put its data in the data file
     * @param id story id
     * @param parent parent story id
     * @param complete is complete story (TODO:in case story is added from the server)
     */
    public void addStory(int id, int parent, int complete)
    {
        int time=Calendar.getInstance().getTime().hashCode();//hash for now
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
        XQUtils.Int2ByteArr(buffer,time,BLOCKOFFSET_STORY_date_added);

        mDataBase.addBlock(buffer);
        mDataBase.save();
    }

    /**
     * gets block id of the story
     * @param id story id
     * @return block id
     */
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

    /**
     * adds one part of the story
     * @param story story block
     * @param part part data (filename and question)
     * @param up upload to server on creation TODO:?
     */
    public void addStoryPart(DataBase.Block story,StoryPart part,boolean up)
    {
        byte[] buffer=new byte[BLOCKSIZE_PART];

        DataBase.Block questionBlock=addStringBlock(part.getQuestion());
        DataBase.Block fnameBlock=addStringBlock(part.getFilePath());
        DataBase.Block textBlock=addStringBlock(part.mText);//TODO: NULL STRING

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
        XQUtils.Int2ByteArr(buffer,textBlock.mId,BLOCKOFFSET_PART_text);//TEXT INPUT
        XQUtils.Int2ByteArr(buffer,part.mType,BLOCKOFFSET_PART_type);//TYPE OF INPUT

        //assign pointer to this part from previous
        DataBase.Block thisBlock=mDataBase.addBlock(buffer);
        DataBase.Block lastInStory=story;
        DataBase.Block firstInStory=story;
        int firstid=(int)XQUtils.ByteArr2Int(firstInStory.mBuffer,BLOCKOFFSET_STORY_first_part);
        int lastid=(int)XQUtils.ByteArr2Int(lastInStory.mBuffer,BLOCKOFFSET_STORY_last_part);
        if(firstid==0)
        {
            XQUtils.Int2ByteArr(firstInStory.mBuffer,thisBlock.mId,BLOCKOFFSET_STORY_first_part);
            XQUtils.Int2ByteArr(lastInStory.mBuffer,thisBlock.mId,BLOCKOFFSET_STORY_last_part);
        }
        else
        {
            XQUtils.Int2ByteArr(lastInStory.mBuffer,thisBlock.mId,BLOCKOFFSET_STORY_last_part);

            lastInStory=mDataBase.getBlocksByBlockId(lastid);
            XQUtils.Int2ByteArr(lastInStory.mBuffer,thisBlock.mId,BLOCKOFFSET_PART_next_part);

            XQUtils.Int2ByteArr(thisBlock.mBuffer,lastid,BLOCKOFFSET_PART_previous_part);
        }
        /*while(lastid!=0)
        {
            lastInStory=mDataBase.getBlocksByBlockId(lastid);
            lastid=(int)XQUtils.ByteArr2Int(lastInStory.mBuffer,BLOCKOFFSET_PART_next_part);
        }*/

        //XQUtils.Int2ByteArr(lastInStory.mBuffer,thisBlock.mId,BLOCKOFFSET_PART_next_part);
        int curnoofstories=(int)XQUtils.ByteArr2Int(story.mBuffer,BLOCKOFFSET_STORY_parts);
        XQUtils.Int2ByteArr(story.mBuffer,curnoofstories+1,BLOCKOFFSET_STORY_parts);
        mDataBase.save();
    }

    /**
     * adding string block
     * @param str string of certaion length
     * @return returns pointer to string block
     */
    private DataBase.Block addStringBlock(String str)
    {
        if(str==null)str="x";
        byte[] buffer=new byte[24+str.length()];

        int emptyId=mDataBase.getEmptyId();
        int emptystirngId=getEmptyTypeId(BLOCKTYPE_STRING);


        XQUtils.Int2ByteArr(buffer,emptyId,BLOCKOFFSET_GLOBAL_block_id);//BLOCK ID
        XQUtils.Int2ByteArr(buffer,BLOCKTYPE_STRING,BLOCKOFFSET_GLOBAL_type);//BLOCK TYPE
        XQUtils.Int2ByteArr(buffer,emptystirngId,BLOCKOFFSET_STRING_id);//PART ID
        for(int i=0;i<str.length();i++)
        {
            buffer[24+i]=(byte)str.charAt(i);
        }

        DataBase.Block block=mDataBase.addBlock(buffer);
        mDataBase.save();

        return block;
    }

    /**
     * mark story as complete
     * @param id story id
     * @param flag upload to server on completion TODO:?
     * @return returns block id
     */
    public int completeStory(int id,int flag)
    {
        DataBase.Block storyBlock=getStoryNdx(id);

        XQUtils.Int2ByteArr(storyBlock.mBuffer,flag,BLOCKOFFSET_STORY_complete);

        mDataBase.save();

        return storyBlock.mId;
    }

    /**
     * get story part data
     * @param story story id
     * @param part part id
     * @return story data
     */
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
        question=new String(questionBlock.mBuffer,24,questionBlock.mBuffer.length-24);
        DataBase.Block fnameBlock=mDataBase.getBlocksByBlockId((int)XQUtils.ByteArr2Int(partBlock.mBuffer,BLOCKOFFSET_PART_fname));

        String fname="";
        //for (int i=12;i<fnameBlock.mBuffer.length;i++){fname+=fnameBlock.mBuffer[i];}
        fname=new String(fnameBlock.mBuffer,24,fnameBlock.mBuffer.length-24);

        DataBase.Block textBlock=mDataBase.getBlocksByBlockId((int)XQUtils.ByteArr2Int(partBlock.mBuffer,BLOCKOFFSET_PART_text));
        String text="";

        text=new String(textBlock.mBuffer,24,textBlock.mBuffer.length-24);

        storyPart.populate("", question, fname,StoryPart.PART_TYPE_VIDEO,text,0);

        return storyPart;
    }

    /**
     * get last recorded story
     * TODO: search by date/time
     * @return {story id, number of parts}
     */
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

    /**
     * get all data to print
     * CONTROLLING FUNCTION, not necessary for app
     * @return string of all data read from data file
     */
    public dataleaf getAllData()
    {
        dataleaf result=new dataleaf("DATA");
        /*result.dataleafs=new dataleaf[3];
        for(int i=0;i<3;i++)
        {
            result.dataleafs[i]=new dataleaf("leaf"+i);
        }
        result.dataleafs[1].dataleafs=new dataleaf[2];
        result.dataleafs[1].dataleafs[0]=new dataleaf("INLEAF1");
        result.dataleafs[1].dataleafs[1]=new dataleaf("INLEAF2");*/

        String ret = "";


        int size=mDataBase.getDataSize();
        dataleaf datasize=new dataleaf("Size of data");
        datasize.dataleafs=new dataleaf[]{new dataleaf(""+size)};
        //ret+="\n datafile size:"+size+"\n";



        DataBase.Block[] blocks;

        blocks=mDataBase.getBlocksByBlockType(BLOCKTYPE_DEVICE);
        //ret+="\n device id history:\n";
        dataleaf deviceIdHistory = new dataleaf("device id history");
        deviceIdHistory.dataleafs=new dataleaf[blocks.length];
        for(int i=0;i<blocks.length;i++)
        {
            int id=(int)XQUtils.ByteArr2Int(blocks[i].mBuffer,BLOCKOFFSET_DEVICE_id);
            deviceIdHistory.dataleafs[i]=new dataleaf(" "+id);
        }

        /*for(int i=0;i<blocks.length;i++)
        {
            int id=(int)XQUtils.ByteArr2Int(blocks[i].mBuffer,BLOCKOFFSET_DEVICE_id);
            ret+=i+": "+id+"\n";
        }
        ret+="-----\n";*/


        dataleaf stories=new dataleaf("STORIES");
        blocks=mDataBase.getBlocksByBlockType(BLOCKTYPE_STORY);
        stories.dataleafs=new dataleaf[blocks.length+1];

        stories.dataleafs[0]=new dataleaf("number of stories:"+blocks.length);












        //ret+="stories:"+blocks.length+"\n";
        for(int i=0;i<blocks.length;i++)
        {
            stories.dataleafs[i+1]=new dataleaf("story:"+i);
            stories.dataleafs[i+1].dataleafs=new dataleaf[5];


            ret+="story:"+i+"\n";
            int id=(int)XQUtils.ByteArr2Int(blocks[i].mBuffer,BLOCKOFFSET_STORY_id);
            int parent=(int)XQUtils.ByteArr2Int(blocks[i].mBuffer,BLOCKOFFSET_STORY_parent);
            int complete=(int)XQUtils.ByteArr2Int(blocks[i].mBuffer,BLOCKOFFSET_STORY_complete);
            int parts=(int)XQUtils.ByteArr2Int(blocks[i].mBuffer,BLOCKOFFSET_STORY_parts);
            stories.dataleafs[i+1].dataleafs[0]=new dataleaf("id "+id);
            stories.dataleafs[i+1].dataleafs[1]=new dataleaf("parent "+parent);
            if(complete==0)stories.dataleafs[i+1].dataleafs[2]=new dataleaf("incomplete");
            else stories.dataleafs[i+1].dataleafs[2]=new dataleaf("complete");

            stories.dataleafs[i+1].dataleafs[3]=new dataleaf("number of parts "+parts);


            /*ret+="\t"+"id: "+"\t"+id+"\n";
            ret+="\t"+"parent: "+"\t"+parent+"\n";
            ret+="\t"+"complete: "+"\t"+complete+"\n";
            ret+="\t"+"number of parts: "+"\t"+parts+"\n";*/

            int partblockid=(int)XQUtils.ByteArr2Int(blocks[i].mBuffer,BLOCKOFFSET_STORY_first_part);
            DataBase.Block partBlock=mDataBase.getBlocksByBlockId(partblockid);

            stories.dataleafs[i+1].dataleafs[4]=new dataleaf("PARTS");


            int c=0;
            while(partblockid!=0)
            {
                dataleaf part=new dataleaf("part"+c);
                id=(int)XQUtils.ByteArr2Int(partBlock.mBuffer,BLOCKOFFSET_PART_id);
                part.addLeaf(new dataleaf("id:"+id));

                int storyblockid=(int)XQUtils.ByteArr2Int(partBlock.mBuffer,BLOCKOFFSET_PART_story);
                DataBase.Block storyblock=mDataBase.getBlocksByBlockId(storyblockid);
                int storyid=(int)XQUtils.ByteArr2Int(storyblock.mBuffer,BLOCKOFFSET_STORY_id);
                int questionblockid=(int)XQUtils.ByteArr2Int(partBlock.mBuffer,BLOCKOFFSET_PART_question);
                DataBase.Block questionblock=mDataBase.getBlocksByBlockId(questionblockid);
                String question=new String(questionblock.mBuffer,24,questionblock.mBuffer.length-24);
                int fnameblockid=(int)XQUtils.ByteArr2Int(partBlock.mBuffer,BLOCKOFFSET_PART_fname);
                DataBase.Block fnameblock=mDataBase.getBlocksByBlockId(fnameblockid);
                String fname=new String(fnameblock.mBuffer,24,fnameblock.mBuffer.length-24);
                int textblockid=(int)XQUtils.ByteArr2Int(partBlock.mBuffer,BLOCKOFFSET_PART_text);
                DataBase.Block textblock=mDataBase.getBlocksByBlockId(textblockid);
                String text=new String(textblock.mBuffer,24,textblock.mBuffer.length-24);

                part.addLeaf(new dataleaf("story id: "+storyid));
                part.addLeaf(new dataleaf("question: "+question));
                part.addLeaf(new dataleaf("filename: "+fname));
                part.addLeaf(new dataleaf("text:     "+text));

                /*ret+="\t"+"\t"+"part\n";

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
                ret+="\t"+"\t"+"file: "+"\t"+fname+"\n";*/

                partblockid=(int)XQUtils.ByteArr2Int(partBlock.mBuffer,BLOCKOFFSET_PART_next_part);
                partBlock=mDataBase.getBlocksByBlockId(partblockid);

                stories.dataleafs[i+1].dataleafs[4].addLeaf(part);
                c++;
            }

            //ret+="\n";
        }
        //ret+="-----\n";

        result.dataleafs=new dataleaf[]{datasize,deviceIdHistory,stories};

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
        //return ret;
        return result;
    }


}
