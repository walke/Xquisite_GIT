package no.ice_9.xquisite;

import android.app.Activity;

/**
 * Created by human on 27.05.16.
 */
public class StoryRecClass extends RecorderBase {

    public StoryRecClass(Activity activity,ASCIIscreen ascii,Server server,int parent, int reserved, int offset)
    {
        NPARTS=3;

        mAscii=ascii;
        tAct=activity;
        mServer=server;

        lastRecorder=true;
        mEndMessege="PUSH BUTTON FINISH ";


        initQuestions();
        init();
    }

    private void initQuestions()
    {


        //mQuestionTime=QTIME;

        mQuestion=new Question[NPARTS];
        mQuestion[0]=new Question("What are the key words from what you just heard? " ,10);
        mQuestion[1]=new Question("Think of what X might do next. Put yourself in her shoes, and challenge yourself to be dramatic. ",120);
        mQuestion[2]=new Question("You now have one more minute to add to your story, or summarise for the next player. ",60);

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
