package no.ice_9.xquisite;

import android.app.Activity;

/**
 * Created by human on 27.05.16.
 *
 * this class is an extension of
 */
public class StoryRecClass extends RecorderBase {

    public static final char NB_UO=(char)128;
    public static final char NB_AE=(char)129;
    public static final char NB_OY=(char)130;
    public static final char NB_uo=(char)131;
    public static final char NB_ae=(char)132;
    public static final char NB_oy=(char)133;

    public StoryRecClass(MainActivity activity,ASCIIscreen ascii,DBmanager dBman,int parent, int reserved, int offset, Session session)
    {
        NPARTS=7;

        mAscii=ascii;
        tAct=activity;
        //mServer=server;
        mDBmanager=dBman;

        lastRecorder=true;
        mEndMessege=tAct.getResources().getString(R.string.RecStory_endMsg);


        initQuestions();
        init(session);
    }

    private void initQuestions()
    {


        //mQuestionTime=QTIME;

        mQuestion=new Question[NPARTS];
        /*mQuestion[0]=new Question("What are the key words from what you just heard? " ,10,PART_TYPE_VIDEO);
        mQuestion[1]=new Question("Think of what X might do next. Put yourself in her shoes, and challenge yourself to be dramatic. ",120,PART_TYPE_VIDEO);
        mQuestion[2]=new Question("You now have one more minute to add to your story, or summarise for the next player. ",60,PART_TYPE_VIDEO);
*/
        mQuestion[0]=new Question(tAct.getResources().getString(R.string.RecStory_Question1) ,60,PART_TYPE_VIDEO);//OLD 8
        mQuestion[1]=new Question(tAct.getResources().getString(R.string.RecStory_Question2) ,60,PART_TYPE_VIDEO);//OLD 8
        mQuestion[2]=new Question(tAct.getResources().getString(R.string.RecStory_Question3) ,60,PART_TYPE_VIDEO);//OLD 8

        mQuestion[3]=new Question(tAct.getResources().getString(R.string.RecInterview_Question1) ,60,PART_TYPE_TEXT_NAME);//OLD 8
        mQuestion[4]=new Question(tAct.getResources().getString(R.string.RecInterview_Question2) ,60,PART_TYPE_TEXT_EMAIL);//OLD 8
        mQuestion[5]=new Question(tAct.getResources().getString(R.string.RecInterview_Question3) ,60,PART_TYPE_CHOOSE);//OLD 8
        mQuestion[6]=new Question(tAct.getResources().getString(R.string.RecInterview_Question4) ,60,PART_TYPE_CHOOSE);//OLD 8

        for (int i=0;i<mQuestion.length;i++)
        {
            mTotalTime+=mQuestion[i].time;
        }






        /*mQuestion=new String[]
                {
                        "ENJOY FREEDOM",
                        "SOMEBODY IS WATCHING YOU!"//,
                        // "Where is X now??",
                        // "What is she doing now??",
                        //"What is she feeling??",
                        //"What is she thinking??",
                        // "What is her biggest challenge??"
                };*/


        //there are as many parts as questions +1 free part;
        //initially they are not done
        mPartDone=new boolean[mQuestion.length+1];
        for(int i=0;i<mPartDone.length;i++){mPartDone[i]=false;}
    }
}
