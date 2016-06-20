package no.ice_9.xquisite;

/**
 * Created by human on 20.06.16.
 */
public class DBmanager {

    DeviceData mDevData;

    DBmanager(DeviceData devData)
    {
        mDevData=devData;
    }

    public int[] getLastStoryNdx()
    {
        int result[];

        result=mDevData.getLastStory();

        return result;
    }

    public int reserveNdx(int parent)
    {

        int id = mDevData.getEmptyTypeId(1);
        mDevData.addStory(id, parent, 0);
        return id;
    }

    public boolean uploadPart(StoryPart part,int storyPart, int ndx, int parent, int user)
    {


        DataBase.Block dndx=mDevData.getStoryNdx(ndx);
        mDevData.addStoryPart(dndx,part,true);
        return true;
    }

    public int completeNdx(int ndx)
    {

        return mDevData.completeStory(ndx,true);
    }

    public StoryPart loadPart(int ndx, int storyPart)
    {


        StoryPart part=mDevData.getStoryPart(ndx,storyPart);

        return part;
    }
}
