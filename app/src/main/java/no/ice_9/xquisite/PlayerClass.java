package no.ice_9.xquisite;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;
import java.util.List;
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
    //Server mServer;
    DBmanager mDBmanager;
    int mTime=0;

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





        mStartPart=mStoryParts-1;

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
                    mStartPart=mStoryParts-1;
                    if(mParent==0 || mParent==-1){mStartPart=0;}


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

        if(mParent==-2)mError=true;

        if(mError)
        {
            //mAscii.clear();
            Log.d("PLAYER","EXIT");
            result[0]=finishVideo();
            result[1]=mParent;
            result[2]=mStoryParts;

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
            mAscii.pushLine("You look a bit impatient");
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
                        mAscii.mGLView.mRenderer.setMode(XQGLRenderer.MODE_PLAY);
                        //mAscii.clear();
                        //mAscii.maximizeInfo();
                        //mAscii.pushLine("loading video..");

                        mAscii.pushLine("");
                        //mAscii.pushLine("Get ready to play!");
                        //mAscii.pushLine("The year is 2062, and our main character X is 17 years old.");
                        if(mParent==0)
                        {
                            mAscii.modLine("No story found, push the button to start the story",0,0,true);
                        }
                        else
                        {
                            mAscii.modLine(tAct.getResources().getString(R.string.Play_initMsg), 0, 0,true);
                        }
                        //mAscii.modLine();

                        //mAscii.pushLine("PRESS THE BUTTON TO PLAY");


                        mAscii.pushLine("");
                        mTime++;









                    }

                    if(mVideoView!=null && mTime==1)
                    {
                        if(mVideoView.isPlaying() )
                        {
                            mAscii.modLine("",0,0,false);
                            mAscii.modLine("",2,0,false);
                            mAscii.modLine("",3,0,false);
                            mAscii.modLine(tAct.getResources().getString(R.string.Play_playMsg),1,0,true);

                            mTime++;
                        }
                    }
                    if(mVideoView!=null && mTime>1)
                    {
                        int pos=mVideoView.getCurrentPosition();
                        int dur=mVideoView.getDuration();
                        if(mVideoView.isPlaying() )
                        {

                            mAscii.mGLView.mRenderer.setProgress((float)pos/(float)dur,1);
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
        }
        else
        {
            //mAscii.pushLine("");
            mAscii.modLine("LOADING...", 3, 0,true);





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
            mAscii.modLine("ready", 3, 0,true);
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
        Log.d("PLAYER", "URI" + mVideoUri);
        //surfaceView=new Surface(mAscii.mGLView);//TODO: GET TEXTURE


        mVideoView=MediaPlayer.create(tAct,mVideoUri);
        if(mVideoView==null)
        {
            mAscii.pushLine("ERROR OCCURED WHILE LOADING VIDEO");
            mAscii.pushLine("PUSH THE BUTTON RECORD NEW ONE");
            mError=true;
            return;
        }
        mVideoView.setSurface(new Surface(mAscii.mGLView.mRenderer.mSurface));
        //surfaceView=new PlayView(tAct,mVideoView);
        //mFrame.addView(surfaceView);
        //mAscii.mGLView.onPause();
        //tAct.setContentView(surfaceView);

        //while(!surfaceView.ready){Log.d("PLAYER","WAITING");}
        //tAct.setContentView(mAscii.mGLView);
        //mAscii.mGLView.onResume();

        //mVideoView.setSurface(surfaceView.getHolder().getSurface());
        //mVideoView.setVideoURI(mVideoUri);


        /*mVideoView.setOnInfoListener(new MediaPlayer.OnInfoListener()
        {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                Log.d("PLAYER","INFO");
                return false;
            }
        });

        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener()
        {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.d("PLAYER","ERROR");
                return false;
            }
        });*/

        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            public void onPrepared(MediaPlayer player) {
                //mAscii.pushLine("ready");
                Log.d("PLAYER", "PLAY");
                //tAct.setContentView(mAscii.mGLView);
                //mAscii.pushLine("123");
                playVideo();
            }
        });

        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer player) {
                mAscii.mGLView.mRenderer.setProgress(0.0f,0);
                if (mCurrentPart < 16) {

                    mVideoReady = false;
                    mCurrentPart++;
                    Log.d("PLAYER", "PARTQ" + mVideoPart[mCurrentPart].getQuestion());
                    playNext();
                } else {
                    //mVideoView.release();
                    mError = true;
                    mAscii.pushLine("START RECORDING");
                    //finishVideo();
                }

            }
        });
    }

    private void startVideo()
    {
        /*mPlayButton.setVisibility(View.GONE);
        mPlayerMessage.setAlpha(0.0f);
        mPlayerMessage.setVisibility(View.GONE);
        mPlayButton.setAlpha(0.0f);*/



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

            Log.d("PLAYER","part:"+i);
            mVideoPart[i]=mDBmanager.loadPart(mParent,i);
            Log.d("PLAYER","part:"+i+"->"+mVideoPart[i].getFilePath());
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

        if(mCurrentPart>mStartPart || mParent==-2)
        {

            //mAscii.clear();
            startVideo();
        }


    }

    private void playNext()
    {

        //mAscii.pushLine("loading another part..");
        //mAscii.pushLine("Close your eyes for a moment and think about what you just heard. What were the key elements? ");
        while(mVideoPart[mCurrentPart].isEmpty())
        {
            if(mCurrentPart>mStartPart){break;}
        }
        mAscii.pushLine("");
        Log.d("PLAYER","loaded");
        if(mCurrentPart>mStartPart)
        //if(mVideoPart[mCurrentPart].isLast())
        {
            mAscii.mGLView.mRenderer.setMode(XQGLRenderer.MODE_IDLE);
            mAscii.modLine(tAct.getResources().getString(R.string.Play_endMsg),0,0,true);
            //mAscii.pushLine("");
            mAscii.modLine(tAct.getResources().getString(R.string.GlobMsg_continue),1,0,false);
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
            mAscii.mGLView.mRenderer.setProgress(0.0f,1);
            mAscii.mGLView.mRenderer.setProgress(0.0f,0);
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



