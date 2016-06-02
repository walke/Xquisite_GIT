package no.ice_9.xquisite;

/**
 * Created by human on 06.11.15.
 *
 * Every story is made out of parts and each part is made out of few data-values
 * this values are organized here to be used throughout whole application
 */
public class StoryPart {

    boolean empty=true;
    boolean last=false;
    String mFname;
    String mQuestion;
    String mPath;
    public StoryPart()
    {
        empty=true;
        mFname="";
        mQuestion="";
        mPath="";
        last=false;
    }

    public void populate(String fname, String quest,String path)
    {
        empty=false;
        mFname=fname;
        mQuestion=quest;
        mPath=path;
    }

    public void setLast()
    {
        last=true;
        empty=false;
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

    public boolean isLast()
    {
        return last;
    }
}
