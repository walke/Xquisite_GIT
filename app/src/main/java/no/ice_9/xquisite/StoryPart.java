package no.ice_9.xquisite;

/**
 * Created by human on 06.11.15.
 *
 * Every story is made out of parts and each part is made out of few data-values
 * this values are organized here to be used throughout whole application
 */
public class StoryPart {

    public static final int PART_TYPE_TEXT =  0;
    public static final int PART_TYPE_CHOOSE= 1;
    public static final int PART_TYPE_VIDEO = 2;

    boolean empty=true;
    boolean last=false;
    String mFname;
    String mQuestion;
    String mPath;
    int mType;

    String mText;
    int mChoose;

    public StoryPart()
    {
        empty=true;
        mFname="";
        mQuestion="";
        mPath="";
        last=false;
    }

    public void populate(String fname, String quest,String path,int type,String text,int choose)
    {
        empty=false;
        mFname=fname;
        mQuestion=quest;
        mPath=path;
        mType=type;
        mText=text;
        mChoose=choose;
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
