package no.ice_9.xquisite_SCI;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;
import android.widget.FrameLayout;

import java.io.File;
import java.util.TimerTask;

/**
 * Created by human on 23.03.16.
 *
 * Player subactivity
 * plays last part of the story
 */
public class PlayerClass extends SubAct{

    public static final char NB_UO=(char)128;
    public static final char NB_AE=(char)129;
    public static final char NB_OY=(char)130;
    public static final char NB_uo=(char)131;
    public static final char NB_ae=(char)132;
    public static final char NB_oy=(char)133;

    ASCIIscreen mAscii;
    //Server_OLD mServer;
    DBmanager mDBmanager;
    int mTime=0;
    boolean donemode=false;
    boolean replay=false;

    Session mSession;

    MediaPlayer mVideoView;
    PlayView surfaceView;
    FrameLayout mFrame;

    private Thread mTask;
    private boolean mVideoReady;
    MainActivity tAct;
    public boolean mError;

    private Uri mVideoUri;
    public int mParent;
    private int mCurrentPart;
    private static int mStartPart=13;

    static String[] mQuestion;


    private StoryPart[] mVideoPart;
    int mStoryParts;

    public boolean init()
    {
        //mVideoView = new VideoView(tAct);
        //mFrame=new FrameLayout(tAct);
        /*if(mVideoView!=null)
        {
            mVideoView.release();
        }mVideoView=null;*/




        mStartPart=mStoryParts-5;

        Log.d("PLAYER","parent:"+mParent);
        if(mParent==-2)
        {

        }
        else if(mParent<1 )
        {
            Log.d("ASCII","parent chaeck"+mParent);
            Thread mTask = new Thread(new Runnable() {
                @Override
                public void run() {

                    //Looper.prepare();

                    int res[] = mDBmanager.getLastStoryNdx();
                    int storyindx = res[0];
                    int storyParts= res[1];
                    mParent=storyindx;
                    mStoryParts=storyParts;
                    Log.d("PLAYER","parent:"+mParent+","+mStoryParts);
                    mStartPart=mStoryParts-5;
                    if(mParent==0 || mParent==-1){mStartPart=0;}


                }
            });

            mTask.start();
        }
        Log.d("PLAYER","waiting startpart");
        while(mStartPart<0 && mParent!=-2){Log.d("PLAYER","wait"+mStartPart+" "+mStoryParts+" "+mParent);}
        Log.d("PLAYER","startpart:"+mStartPart);

        mError=false;

        mCurrentPart=mStartPart;
        mVideoPart=new StoryPart[16];
        for(int i=0;i<16;i++){mVideoPart[i]=new StoryPart();}

        mQuestion =new String[7];
        initQuestions();

        mVideoReady=false;

        mAscii.mAsciiStartUpdater(100);
        mAscii.clear();

        mTask = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("PLAYER", "getting list ");
                Looper.prepare();

                //TODO:TMP
                if(mParent!=-2)
                {
                    int res[] = mDBmanager.getLastStoryNdx();
                    int storyindx = res[0];
                    int storyParts= res[1];
                    mParent=storyindx;
                    mStoryParts=storyParts;
                }

                //boolean result=loadVideo();
                boolean result=loadStoryData();
                Log.d("PLAYER","RES SD"+result);
                if(result)
                {
                    Log.d("PLAYER", "prep");
                    preparePlayer();
                    /*tAct.runOnUiThread(new Runnable() {
                        public void run() {

                            Log.d("PLAYER", "got list");


                            //playVideo();

                        }
                    });*/
                    Log.d("PLAYER", "prep2");
                }
                else
                {
                    //mAscii.clear();
                    //mAscii.pushLine("NO VIDEO FOUND, PUSH THE BUTTON TO RECORD NEW");
                    mAscii.mAsciiStopUpdater(100);
                }


            }
        });

        mTask.start();

        return true;
    }

    /*public PlayerClass(Activity activity,ASCIIscreen ascii,DBmanager dBman, int parent, int parentParts)
    {
        tAct=activity;
        //mServer=server;
        mDBmanager=dBman;
        mAscii = ascii;

        //mVideoView = new VideoView(tAct);
        //mFrame=new FrameLayout(tAct);
        Log.d("PLAYER","par:"+parent+" prts:"+parentParts);


        mParent=parent;
        mStoryParts=parentParts;
        mStartPart=mStoryParts-1;

        Log.d("PLAYER","parent:"+mParent);
        if(mParent<1)
        {
            Thread mTask = new Thread(new Runnable() {
                @Override
                public void run() {

                    //Looper.prepare();
                    int res[] = mDBmanager.getLastStoryNdx();
                    int storyindx = res[0];
                    int storyParts= res[1];
                    mParent=storyindx;
                    mStoryParts=storyParts;
                    Log.d("PLAYER","parent:"+mParent+","+mStoryParts);
                    mStartPart=mStoryParts-1;
                    if(mParent==0){mStartPart=0;}

                }
            });

            mTask.start();
        }
        while(mStartPart<0){Log.d("PLAYER","wait"+mStartPart+" "+mStoryParts+" "+mParent);}
        Log.d("PLAYER","startpart:"+mStartPart);

        mError=false;

        mCurrentPart=mStartPart;
        mVideoPart=new StoryPart[16];
        for(int i=0;i<16;i++){mVideoPart[i]=new StoryPart();}

        mQuestion =new String[7];
        initQuestions();

        mVideoReady=false;

        mAscii.mAsciiStartUpdater(100);
        mAscii.clear();

        mTask = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("PLAYER", "getting list ");
                Looper.prepare();

                //TODO:TMP
                int res[] = mDBmanager.getLastStoryNdx();
                int storyindx = res[0];
                int storyParts= res[1];
                mParent=storyindx;
                mStoryParts=storyParts;

                //boolean result=loadVideo();
                boolean result=loadStoryData();
                Log.d("PLAYER","RES SD"+result);
                if(result)
                {

                    tAct.runOnUiThread(new Runnable() {
                        public void run() {

                            Log.d("PLAYER", "got list");
                            preparePlayer();

                            //playVideo();

                        }
                    });
                }
                else
                {
                    //mAscii.clear();
                    //mAscii.pushLine("NO VIDEO FOUND, PUSH THE BUTTON TO RECORD NEW");
                    mAscii.mAsciiStopUpdater(100);
                }


            }
        });

        mTask.start();

    }*/

    @Override
    public int[] action(int act)
    {
        Log.d("PLAYER","actino"+act+" "+mError);
        int[] result=new int[5];
        result[0]=-1;
        if(act==0)return result;
        if(act==14)
        {
            replay=true;
            mTime=-1;

            init();
            mError=false;
            //mCurrentPart--;
            while(mVideoView==null);
            mTime=0;

            //startVideo();
            return result;
        }
        if(mParent==-2)mError=true;

        if(mError)
        {
            //mAscii.clear();
            Log.d("PLAYER","EXIT");
            result[0]=finishVideo();
            result[1]=mParent;
            result[2]=mStoryParts;
            Log.d("PLAYER","prog:"+mSession.currentProgressPart+", "+mSession.totalProgressParts);
            mSession.iterate();
            Log.d("PLAYER","prog:"+mSession.currentProgressPart+", "+mSession.totalProgressParts);
            mAscii.mGLView.mRenderer.setProgress((float)mSession.currentProgressPart/(float)mSession.totalProgressParts,1);
            //return -1;
        }

        else if(mVideoReady)
        {
            //mAscii.minimizeInfo();
            //mAscii.clear();
            startVideo();


        }
       /* else if(mCurrentPart<mStoryParts)
        {
            playNext();
        }*/
        else
        {
            //mAscii.pushLine("You look a bit impatient");
        }
        Log.d("PLAYER","ACTION");
        return result;
    }

    @Override
    public TimerTask getTimerTask()
    {
        return new TimerTask() {
            @Override
            public void run() {

                //Log.d("PLAYER","::"+mVideoView.isActivated()+","+mVideoView);
                if(mAscii.mReady)
                {
                    if(mTime==0)
                    {
                        Log.d("PLAYER","time reset");
                        donemode=false;
                        mAscii.mGLView.mRenderer.progMark=true;
                        mAscii.mGLView.mRenderer.setMode(XQGLRenderer.MODE_PLAY);

                        //mAscii.clear();
                        //mAscii.maximizeInfo();
                        //mAscii.pushLine("loading video..");

                        //mAscii.pushLine("");
                        //mAscii.pushLine("Get ready to play!");
                        //mAscii.pushLine("The year is 2062, and our main character X is 17 years old.");
                        if(mParent==0)
                        {
                            mAscii.modLine("No story found, push the button to start the story",0,0,true);
                        }
                        else
                        {
                            //mAscii.modLine("", 4, 0,true);
                            mAscii.modLine(tAct.getResources().getString(R.string.Play_initMsg), 0, 0,true);
                        }
                        //mAscii.modLine();

                        //mAscii.pushLine("PRESS THE BUTTON TO PLAY");


                        //mAscii.pushLine("");
                        mTime++;



                        Log.d("PLAYER","time reset"+mVideoView);





                    }

                    if(mVideoView!=null && mTime==1)
                    {
                        if(mVideoView.isPlaying() )
                        {
                            Log.d("PLAYER","playing");
                            mAscii.modLine("",0,0,false);
                            mAscii.modLine("",2,0,false);
                            mAscii.modLine("",3,0,false);
                            mAscii.modLine(tAct.getResources().getString(R.string.Play_playMsg),1,0,true);

                            mTime++;
                        }
                    }
                    if(mVideoView!=null && mTime>1)
                    {

                        try
                        {
                            if(mVideoView.isPlaying() )
                            {
                                int pos=mVideoView.getCurrentPosition();
                                int dur=mVideoView.getDuration();
                                //mAscii.mGLView.mRenderer.setProgress((float)pos/(float)dur,1);
                            }
                        }
                        catch (IllegalStateException e) {}
                        if(!donemode)
                        {
                            donemode=true;
                            //mAscii.mGLView.mRenderer.setMode(XQGLRenderer.MODE_PLAY_DONE);
                        }

                    }

                    /*if(mVideoView!=null)
                    {
                        if(mVideoView.isPlaying())
                        {

                        }



                    }*/

                }

            }
        };
    }

    @Override
    public void onBack() {
        super.onBack();

        DialogFragment dg= new uncompleteCurrent();
        dg.show(tAct.getFragmentManager(), "MARK AS LAST?");



    }

    public static class uncompleteCurrent extends DialogFragment {
        /*MainActivity acti

        public InterviewSkip(MainActivity activity){}*/

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("would you like to mark this story as last in its branch?")
                    .setPositiveButton("no", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    })
                    .setNegativeButton("yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog

                            ((MainActivity)getActivity()).currentSubActivity.runFunc();
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

    @Override
    public void runFunc()
    {
        mDBmanager.completeNdx(mParent,2);
    }

    private void initQuestions()
    {

        mQuestion=new String[]
                {
                        "How old is X now?",
                        "Where is X now?",
                        "What is she doing now?",
                        "What is she feeling?",
                        "What is she thinking?",
                        "What is her biggest challenge?"
                };


        //there are as many parts as questions +1 free part;
        //initially they are not done

    }

    //LOAD STORY DATA
    private boolean loadStoryData()
    {
        /*int res[] = mServer.getLastStoryNdx();
        int storyindx = res[0];
        int storyParts= res[1];
        mParent=storyindx;
        mStoryParts=storyParts;*/

        if(mParent==0)
        {
            Log.d("PLAYER", "no video ");
            mAscii.modLine("no video found",0,0,true);
            //mAscii.pushLine("****************************************");
            mAscii.modLine("PUSH THE BUTTON TO RECORD THE FIRST VIDEO",1,0,false);
            Log.d("PLAYER", "no video1 ");

            mError=true;
            return false;
        }
        else if(mParent==-2)
        {
            Log.d("PLAYER", "loading intro file");
            String FilePath=tAct.getExternalFilesDir("").getPath();
            File mediaStorageDir = new File(FilePath, "");
            mVideoPart[0].populate("appintro.mp4","",mediaStorageDir.getPath()+File.separator+"appintro.mp4",StoryPart.PART_TYPE_VIDEO,"",0);
            mCurrentPart=0;
        }
        else
        {
            //mAscii.pushLine("");
            mAscii.modLine("", 3, 0,false);





            new Thread(new Runnable() {
                @Override
                public void run() {

                    loadParts();

                }
            }).start();
            Log.d("PLAYER", "empt" + mCurrentPart);

            while(mVideoPart[mCurrentPart].isEmpty())
            {
                //Log.d("PLAYER","prtpth "+mCurrentPart);
            }
            //Log.d("PLAYER","prtpth "+mVideoPart[1].getFilePath());


            //mAscii.pushLine("seems like it is ready");
            //mAscii.modLine("ready", 3, 0,true);
            //Log.d("PLAYER", "file path:" + mVideoPart[mCurrentPart].getFilePath());



        }

        Log.d("PLAYER", "getting file"+mVideoPart[mCurrentPart].getFilePath());
        //mAscii.pushLine("one more second..");
        mVideoUri= Uri.fromFile(new File(mVideoPart[mCurrentPart].getFilePath()));
        Log.d("PLAYER", "got file");
        Log.d("PLAYER", "URI" + mVideoUri);

        return true;
    }

    private void preparePlayer() {

        Log.d("PLAYER", "got list");

        Log.d("PLAYER", "URI" + mVideoUri+mVideoView);



        tAct.runOnUiThread(new Runnable() {
            public void run() {
                Log.d("PLAYER", "UUI");
                mVideoView=MediaPlayer.create(tAct,mVideoUri);

                if(mVideoView==null)
                {
                    mAscii.pushLine("ERROR OCCURED WHILE LOADING VIDEO");
                    mAscii.pushLine("PUSH THE BUTTON RECORD NEW ONE");
                    mError=true;
                    return;
                }

                //surfaceView=new Surface(mAscii.mGLView);//TODO: GET TEXTURE

                mVideoView.setSurface(new Surface(mAscii.mGLView.mRenderer.mSurface));


                mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                    public void onPrepared(MediaPlayer player) {
                        //mAscii.pushLine("ready");
                        Log.d("PLAYER", "PLAY");
                        //tAct.setContentView(mAscii.mGLView);
                        //mAscii.pushLine("123");
                        mVideoReady=true;
                        playVideo();
                        if(replay){startVideo();}
                    }
                });

                mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer player) {
                        //mAscii.mGLView.mRenderer.setProgress(0.0f,0);
                        if (mCurrentPart < 16) {

                            mVideoReady = false;
                            mCurrentPart++;
                            Log.d("PLAYER", "PARTQ" + mVideoPart[mCurrentPart].getQuestion());
                            playNext();
                        } else {
                            //mVideoView.release();
                            mError = true;
                            //mAscii.pushLine("START RECORDING");
                            //finishVideo();
                        }
                        mStartPart=-1;

                        //mAscii.mGLView.mRenderer.mSurface.release();
                        mVideoView.setDisplay(null);
                        mVideoView.reset();
                        //mVideoView.release();


                        //mVideoView=null;

                    }
                });

                //playVideo();

            }
        });


        /*while(mVideoView==null)
        {
            //Log.d("PLAYER", "URI");
        }*/

        Log.d("PLAYER", "URI" +mVideoView);





    }

    private void startVideo()
    {
        /*mPlayButton.setVisibility(View.GONE);
        mPlayerMessage.setAlpha(0.0f);
        mPlayerMessage.setVisibility(View.GONE);
        mPlayButton.setAlpha(0.0f);*/
        Log.d("PLAYER","starting");
        while(!mVideoReady);


        mVideoView.start();
        Log.d("PLAYER","VIDEO LINE");
        //mAscii.pushLine(mVideoPart[mCurrentPart].getQuestion());
    }




    //LOAD PARTSif(mCurrentPart>=mStartPart)
    private void loadParts()
    {
        Log.d("PLAYER", "LOADING PARTS");
        for(int i=mStartPart;i<mStoryParts;i++)
        {

            //Log.d("PLAYER","part:"+i);
            mVideoPart[i]=mDBmanager.loadPart(mParent,i);
            //Log.d("PLAYER","part:"+i+"->"+mVideoPart[i].getFilePath());
        }
    }

    //PLAY VIDEO
    private void playVideo()
    {




        //mPlayButton.setVisibility(View.VISIBLE);
        //mPlayButton.setAlpha(1.0f);

        //mVideoView.start();
        //int totDuration=mVideoView.getDuration();
        //mVideoView.seekTo((totDuration/3)*2);
        //Log.d("PLAYER", "duration"+);

        //boolean done=false;
        /*while(!done)
        {
            if(mVideoView.isPlaying() && mVideoView.getCurrentPosition()>((totDuration/3)*2))
            {
                done=true;
                //mVideoView.pause();

                //mLoadingFrame.setAlpha(0.0f);
            }
            else{mVideoView.start();Log.d("PLAYER", "not playing");}

        }*/
        //mVideoView.setAlpha(1.0f);

        mVideoReady=true;

        //mAscii.pushLine("So now it looks ready");




        //mAscii.pushLine("#########################");
        //mAscii.pushLine("PUSH BUTTON TO PLAY STORY");
        Log.d("PLAYER","PLAYING IN");
        if(mCurrentPart>mStartPart || mParent==-2)
        {
            Log.d("PLAYER","STARTING PART");


            //mAscii.clear();
            startVideo();
        }


    }

    private void playNext()
    {

        //mAscii.pushLine("loading another part..");
        //mAscii.pushLine("Close your eyes for a moment and think about what you just heard. What were the key elements? ");
        Log.d("PLAYER","next");
        while(mVideoPart[mCurrentPart].isEmpty())
        {
            if(mCurrentPart>mStartPart){break;}
        }
        //mAscii.pushLine("");
        Log.d("PLAYER","loaded");
        if(mCurrentPart>mStartPart)
        //if(mVideoPart[mCurrentPart].isLast())
        {
            mAscii.mGLView.mRenderer.setMode(XQGLRenderer.MODE_PLAY_DONE);
            mAscii.modLine(tAct.getResources().getString(R.string.Play_endMsg),0,0,true);
            //mAscii.pushLine("");
            mAscii.modLine(tAct.getResources().getString(R.string.GlobMsg_continue),2,0,false);
            Log.d("PLAYER","LAST");

            mCurrentPart=16;
            mError=true;
            //finishVideo();
            return;
        }


        mVideoUri=Uri.fromFile(new File(mVideoPart[mCurrentPart].getFilePath()));
        //mVideoReady=true;
        preparePlayer();

    }

    private int finishVideo()
    {
        if(mVideoView!=null)
        {
            //mAscii.mGLView.mRenderer.setProgress(0.0f,1);
            //mAscii.mGLView.mRenderer.setProgress(0.0f,0);
            mTime=-1;
            mVideoView.release();
            mVideoView=null;
        }

        Log.d("PLAYER","parent"+mParent);
        return mParent;
        //tAct.mParent=mParent;
        //Intent intent = new Intent(this, RecorderActivity.class);
        //intent.putExtra(EXTRA_MESSAGE2, String.valueOf(mParent));
        //startActivity(intent);
        //finish();

        //GO DIRECTLY TO RECORDING
        /*mNextButton.setAlpha(1.0f);
        mNextButton.setVisibility(View.VISIBLE);*/
    }

    @Override
    public void destroy()
    {

    }


}



