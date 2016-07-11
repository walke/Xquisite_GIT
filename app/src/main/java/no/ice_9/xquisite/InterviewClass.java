package no.ice_9.xquisite;

import android.app.ActionBar;
import android.app.Activity;
import android.view.WindowManager;

/**
 * Created by human on 27.05.16.
 *
 * This is an extention of RecorderBase class to
 * purpose of it to make Record an interview of the user in same way as story is being recorded
 */
public class InterviewClass extends RecorderBase {

    public static final char NB_UO=(char)128;
    public static final char NB_AE=(char)129;
    public static final char NB_OY=(char)130;
    public static final char NB_uo=(char)131;
    public static final char NB_ae=(char)132;
    public static final char NB_oy=(char)133;


    public InterviewClass(MainActivity activity,ASCIIscreen ascii,DBmanager dBman)
    {
        NPARTS=11;
        activity.inputField.clearFocus();
        activity.mAscii.mGLView.requestFocus();
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mAscii=ascii;
        tAct=activity;
        //mServer=server;
        mDBmanager=dBman;



        lastRecorder=false;
        mEndMessege=tAct.getResources().getString(R.string.RecInterview_EndMsg);

        initQuestions();
        init();

    }





    private void initQuestions()
    {


        //mQuestionTime=QTIME;

        mQuestion=new Question[NPARTS];
        mQuestion[0]=new Question(tAct.getResources().getString(R.string.RecInterview_Question1) ,60,PART_TYPE_TEXT);//OLD 8
        mQuestion[1]=new Question(tAct.getResources().getString(R.string.RecInterview_Question2) ,60,PART_TYPE_TEXT);//OLD 8
        mQuestion[2]=new Question(tAct.getResources().getString(R.string.RecInterview_Question3) ,60,PART_TYPE_CHOOSE);//OLD 8
        mQuestion[3]=new Question(tAct.getResources().getString(R.string.RecInterview_Question4) ,60,PART_TYPE_CHOOSE);//OLD 8
        mQuestion[4]=new Question(tAct.getResources().getString(R.string.RecInterview_Question5) ,60,PART_TYPE_VIDEO);//OLD 8
        mQuestion[5]=new Question(tAct.getResources().getString(R.string.RecInterview_Question6) ,60,PART_TYPE_VIDEO);//OLD 8
        mQuestion[6]=new Question(tAct.getResources().getString(R.string.RecInterview_Question7) ,60,PART_TYPE_VIDEO);//OLD 8
        mQuestion[7]=new Question(tAct.getResources().getString(R.string.RecInterview_Question8) ,60,PART_TYPE_VIDEO);//OLD 8
        mQuestion[8]=new Question(tAct.getResources().getString(R.string.RecInterview_Question9) ,60,PART_TYPE_VIDEO);//OLD 8
        mQuestion[9]=new Question(tAct.getResources().getString(R.string.RecInterview_Question10) ,60,PART_TYPE_VIDEO);//OLD 8
        mQuestion[10]=new Question(tAct.getResources().getString(R.string.RecInterview_Question11) ,60,PART_TYPE_VIDEO);//OLD 8

        /*mQuestion[0]=new Question("What is your name?" ,5,PART_TYPE_TEXT);//OLD 8
        mQuestion[1]=new Question("What is your email? (spell if needed)",60,PART_TYPE_TEXT);//OLD 8
        mQuestion[2]=new Question("what's your best mistake?",60,PART_TYPE_VIDEO);//OLD 8
        mQuestion[3]=new Question("what's the most significant change you think will happen in the next 50 years?",60,PART_TYPE_VIDEO);//OLD 8
        mQuestion[4]=new Question("if you could time travel, what year would you go to? ",60,PART_TYPE_VIDEO);//OLD 8
        mQuestion[5]=new Question("what did (or what do) you want to be when you grow up?",60,PART_TYPE_VIDEO);//OLD 8*/
        /*mQuestion[2]=new Question("Are you happy to be credited for your contribution? (answer yes or no)",60,PART_TYPE_VIDEO);//OLD 8
        mQuestion[3]=new Question("Are you willing to engage in online or live dialogue with young people and artists? (answer yes or no)",60,PART_TYPE_VIDEO);//OLD 8
        mQuestion[4]=new Question("Describe briefly your earliest memory",60,PART_TYPE_VIDEO);//OLD 8
        mQuestion[5]=new Question("Would your close friends call you an optimist or pessimist about the future?",60,PART_TYPE_VIDEO);//OLD 8
        mQuestion[6]=new Question("What is your favorite science fiction story?",60,PART_TYPE_TEXT);//OLD 8
        mQuestion[7]=new Question("Briefly describe your work and your research themes.",60,PART_TYPE_VIDEO);//OLD 8
        mQuestion[8]=new Question("What is the most exciting element of your research? ",60,PART_TYPE_VIDEO);//OLD 8
        mQuestion[9]=new Question("What is the most challenging element of your work?",60,PART_TYPE_VIDEO);//OLD 8
        mQuestion[10]=new Question("What are the most important environmental changes likely to happen in your field in the next 100 years?",60,PART_TYPE_VIDEO);//OLD 8
        mQuestion[11]=new Question("How might this change affect human lives?",60,PART_TYPE_VIDEO);//OLD 8*/

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
