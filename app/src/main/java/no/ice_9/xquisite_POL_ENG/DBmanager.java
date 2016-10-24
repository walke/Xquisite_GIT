package no.ice_9.xquisite_POL_ENG;

/**
 * Created by human on 20.06.16.
 */
public class DBmanager {

    DeviceData mDevData;
    DeviceData mDevData_NO;

    MainActivity tAct;

    DBmanager(MainActivity activity, DeviceData devData,DeviceData devData_NO)
    {
        tAct=activity;
        mDevData=devData;
        mDevData_NO=devData_NO;
    }

    public int[] getLastStoryNdx()
    {
        int result[];

        if(tAct.userLanguage==0)
            result=mDevData.getLastStory();
        else
            result=mDevData_NO.getLastStory();

        return result;
    }

    public int reserveNdx(int parent)
    {
        int id;
        if(tAct.userLanguage==0)
        {
            id = mDevData.getEmptyTypeId(1);
            mDevData.addStory(id, parent, 0);
        }

        else
        {
            id = mDevData_NO.getEmptyTypeId(1);
            mDevData_NO.addStory(id, parent, 0);
        }


        return id;
    }

    public boolean uploadPart(StoryPart part,int storyPart, int ndx, int parent, int user)
    {
        if(tAct.userLanguage==0)
        {
            DataBase.Block dndx=mDevData.getStoryNdx(ndx);
            mDevData.addStoryPart(dndx,part,true);
        }
        else
        {
            DataBase.Block dndx=mDevData_NO.getStoryNdx(ndx);
            mDevData_NO.addStoryPart(dndx,part,true);
        }


        return true;
    }

    public int completeNdx(int ndx,int flag)
    {
        if(tAct.userLanguage==0)
        {
            return mDevData.completeStory(ndx,flag);
        }
        else
        {
            return mDevData_NO.completeStory(ndx,flag);
        }


    }

    public StoryPart loadPart(int ndx, int storyPart)
    {


        StoryPart part;
        if(tAct.userLanguage==0)
        {
            part=mDevData.getStoryPart(ndx,storyPart);
        }
        else
        {
            part=mDevData_NO.getStoryPart(ndx,storyPart);
        }


        return part;
    }
}
