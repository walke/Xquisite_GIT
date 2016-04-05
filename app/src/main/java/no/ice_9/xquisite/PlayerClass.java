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
 */
public class PlayerClass extends SubAct{

    ASCIIscreen mAscii;
    Server mServer;
    int mTime=0;

    MediaPlayer mVideoView;
    PlayView surfaceView;
    FrameLayout mFrame;

    private Thread mTask;
    private boolean mVideoReady;
    Activity tAct;
    private boolean mError;

    private Uri mVideoUri;
    private int mParent;
    private int mCurrentPart;
    private static int mStartPart=12;

    static String[] mQuestion;


    private StoryPart[] mVideoPart;
    private int mStoryParts;



    public PlayerClass(Activity activity,ASCIIscreen ascii,Server server, int parent, int parentParts)
    {
        tAct=activity;
        mServer=server;
        mAscii = ascii;

        //mVideoView = new VideoView(tAct);
        //mFrame=new FrameLayout(tAct);
        Log.d("PLAYER","par:"+parent+" prts:"+parentParts);


        mParent=parent;
        mStoryParts=parentParts;




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
                int res[] = mServer.getLastStoryNdx();
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
                    mAscii.mAsciiStopUpdater(100);
                }


            }
        });

        mTask.start();

    }

    @Override
    public int[] action()
    {
        int[] result=new int[1];
        result[0]=-1;
        if(mError)
        {
            Log.d("PLAYER","EXIT");
            result[0]=finishVideo();
            //return -1;
        }
        else if(mVideoReady)
        {
            mAscii.minimizeInfo();
            mAscii.clear();
            startVideo();
        }
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
                        mAscii.clear();
                        mAscii.maximizeInfo();
                        //mAscii.pushLine("loading video..");

                        mAscii.pushLine("");
                        mAscii.pushLine("Thanks. Get ready to play!");
                        mAscii.pushLine("The year is 2062, and our main character X is 17 years old.");
                        mAscii.pushLine("Listen to where we are in her story…");

                        mAscii.pushLine("PRESS THE BUTTON TO PLAY");


                        mAscii.pushLine("");
                        mTime++;









                    }

                    if(mVideoView!=null && mTime==1)
                    {
                        if(mVideoView.isPlaying() )
                        {
                            mAscii.modLine("",0,0);
                            mAscii.modLine("",2,0);
                            mAscii.modLine("",3,0);
                            mAscii.modLine("PLAYING..",1,0);

                            mTime++;
                        }
                    }
                    if(mVideoView!=null && mTime>1)
                    {
                        if(mVideoView.isPlaying() )
                        {
                            int pos=mVideoView.getCurrentPosition();
                            int dur=mVideoView.getDuration();
                            mAscii.mGLView.mRenderer.setProgress((float)pos/(float)dur);
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
            mAscii.pushLine("no video found");
            mAscii.pushLine("****************************************");
            mAscii.pushLine("PUSH THE BUTTON TO RECORD THE FIRST VIDEO");
            Log.d("PLAYER", "no video1 ");

            mError=true;
            return false;
        }
        else
        {
            mAscii.pushLine("we found some cluster of a story for you");
            mAscii.pushLine("we will now try to get it ready for you");





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


            mAscii.pushLine("seems like it is ready");
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
                Log.d("PLAYER", "PLAY");
                //tAct.setContentView(mAscii.mGLView);
                //mAscii.pushLine("123");
                playVideo();
            }
        });

        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer player) {

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
        for(int i=0;i<mStoryParts;i++)
        {

            Log.d("PLAYER","part:"+i);
            mVideoPart[i]=mServer.loadPart(mParent,i);
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




        mAscii.pushLine("#########################");
        mAscii.pushLine("PUSH THE BUTTON TO PLAY IT");

        if(mCurrentPart>mStartPart)
        {
            mAscii.clear();
            startVideo();
        }


    }

    private void playNext()
    {

        //mAscii.pushLine("loading another part..");
        while(mVideoPart[mCurrentPart].isEmpty())
        {
            if(mCurrentPart>=mStartPart){break;}
        }
        Log.d("PLAYER","loaded");
        if(mCurrentPart>=mStartPart)
        //if(mVideoPart[mCurrentPart].isLast())
        {
            mAscii.pushLine("PRESS THE BUTTON TO START RECORDING..");
            Log.d("PLAYER","LAST");
            mCurrentPart=16;
            mError=true;
            //finishVideo();
            return;
        }


        mVideoUri=Uri.fromFile(new File(mVideoPart[mCurrentPart].getFilePath()));

        preparePlayer();

    }

    private int finishVideo()
    {
        if(mVideoView!=null)
        {
            mAscii.mGLView.mRenderer.setProgress(0.0f);
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



