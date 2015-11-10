package no.ice_9.xquisite;

/**
 * Created by human on 06.11.15.
 */
public class StoryPart {

    boolean empty=true;
    String mFname;
    String mQuestion;
    String mPath;
    public StoryPart()
    {
        empty=true;
        mFname="";
        mQuestion="";
        mPath="";
    }

    public void populate(String fname, String quest,String path)
    {
        empty=false;
        mFname=fname;
        mQuestion=quest;
        mPath=path;
    }

    public String getFilePath() {
        return mPath;
    }

    public String getFname() {
        return mFname;
    }

    public String getQuestion() {
        return mQuestion;
    }

    public boolean isEmpty()
    {
        return empty;
    }
}
