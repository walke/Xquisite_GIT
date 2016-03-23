package no.ice_9.xquisite;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.TimerTask;

/**
 * Created by human on 23.03.16.
 */
public class PlayerClass {

    ASCIIscreen mAscii;
    Server mServer;
    int mTime=0;

    MediaPlayer mVideoView;
    PlayView surfaceView;

    private Thread mTask;
    private boolean mVideoReady;
    Activity tAct;
    private boolean mError;

    private Uri mVideoUri;
    private int mParent;
    private int mCurrentPart;
    private static int mStartPart=1;

    static String[] mQuestion;


    private StoryPart[] mVideoPart;


    public PlayerClass(Activity activity,ASCIIscreen ascii,Server server)
    {
        tAct=activity;
        mServer=server;
        mAscii = ascii;

        //mVideoView = new VideoView(tAct);
        surfaceView=new PlayView(tAct,mVideoView);



        mError=false;

        mCurrentPart=mStartPart;
        mVideoPart=new StoryPart[16];
        for(int i=0;i<16;i++){mVideoPart[i]=new StoryPart();}

        mQuestion =new String[7];
        initQuestions();

        mVideoReady=false;

        mAscii.mAsciiStartUpdater(100);


        mTask = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("PLAYER", "getting list ");
                Looper.prepare();

                //boolean result=loadVideo();
                boolean result=loadStoryData();

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

    public int action()
    {

        if(mError)
        {
            finishVideo();
        }
        else if(mVideoReady)
        {
            mAscii.clear();
            startVideo();
        }
        else
        {
            mAscii.pushLine("You look a bit impatient");
        }
        Log.d("PLAYER","ACTION");
        return 0;
    }

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
                        mAscii.pushLine("loading video..");

                        mAscii.pushLine("");
                        mAscii.pushLine("This is a story game about XX chromosome human born in 2045");
                        mAscii.pushLine("Her name is X");
                        mAscii.pushLine("The person before you made up part of her story. You are invited to continue her story");
                        mAscii.pushLine("");
                        mTime++;
                    }

                    if(mVideoView!=null && mVideoView.isPlaying())
                    {


                        //mAscii.canvas=surfaceView.getHolder().getSurface().lockCanvas(new Rect(0,0,10,10));
                        surfaceView.draw(mAscii.canvas);
                        mAscii.putCanvas();
                        //surfaceView.getHolder().getSurface().unlockCanvasAndPost(mAscii.canvas);

                    }
                    //if(mTime>=0 && mTime<20){mAscii.fillTrash();/*mAscii.setRage(true);*/mTime++;}

                    //if(mTime==20){mAscii.setRage(false);mAscii.clear();mTime++;}
                    /*if(mTime==21 && !mAscii.isRage())
                    {
                        mLoadingDialog.dismiss();
                        mAscii.pushLine("########################");
                        mAscii.pushLine("#scienceFuture xquisite#");
                        mAscii.pushLine("########################");
                        mAscii.pushLine("Initializing sequence...");
                        mTime++;
                    }
                    if(mTime==22 && !mAscii.isRage())
                    {
                        mAscii.pushLine("Testing connection to the server...");
                        mInitDone=false;
                        mTime++;
                    }
                    if(mTime==23 && !mAscii.isRage())
                    {

                        if(mServer.checkConnection())
                        {
                            mServerConnection=1;
                        }
                        else{mServerConnection=-1;}
                        Log.d("MAIN", "servResp" + mServerConnection);
                        mTime++;
                    }

                    if(mServerConnection==1  && !mAscii.isRage() && !mInitDone)
                    {
                        mAscii.pushLine("Connection succesed");
                        mAscii.pushLine("");
                        mAscii.pushLine("!TAP THE SCREEN TO CONTINUE!");
                        mInitDone=true;

                        //this.cancel();
                        //mAscii.mAsciiStopUpdater();
                        mTime++;
                    }
                    if(mInitDone)
                    {

                        Log.d("MAIN","WAITING FOR TOUCH");mTime++;
                    }
                    if(mServerConnection==-1  && !mAscii.isRage() && !mInitDone)
                    {
                        mAscii.pushLine("Connection failed");
                        mAscii.pushLine("");
                        mAscii.pushLine("THERE WAS A PROBLEM WITH A CONNECTION TO SERVER");
                        mAscii.pushLine("try to check your internet connection");
                        mAscii.pushLine("if your internet works fine, the problem is on server side");
                        mAscii.pushLine("any way we will try to reconnect in few seconds");
                        //mAscii.mAsciiStopUpdater();
                        //this.cancel();
                        mReconnectTime=mTime;
                        mInitDone=true;
                        mTime++;
                    }
                    if(mTime>(mReconnectTime+50) && mReconnectTime!=-1)
                    {
                        mServerConnection=0;
                        mReconnectTime=-1;
                        mInitDone=false;
                        mAscii.pushLine("");
                        mAscii.pushLine("retrying connecting..");
                        mTime=23;
                    }

                    if(mTime>1000)
                    {
                        mServerConnection=0;
                        mScreenSaver=true;
                        mAscii.fillTrash();
                        mAscii.putImage();
                        mInitDone=false;
                    }
                    Log.d("MAIN","conn"+mServerConnection);
                    Log.d("MAIN", "time" + mTime);*/
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
    public boolean loadStoryData()
    {
        int storyindx = mServer.getLastStoryNdx();
        mParent=storyindx;

        if(storyindx==0)
        {
            Log.d("PLAYER", "no video ");
            mAscii.pushLine("no video found");
            mAscii.pushLine("****************************************");
            mAscii.pushLine("TAP THE SCREEN TO RECORD THE FIRST VIDEO");
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


            while(mVideoPart[mCurrentPart].isEmpty())
            {

            }



            mAscii.pushLine("seems like it is ready");
            Log.d("PLAYER", "file path:" + mVideoPart[mCurrentPart]);



        }

        Log.d("PLAYER", "getting file");
        //mAscii.pushLine("one more second..");
        mVideoUri= Uri.fromFile(new File(mVideoPart[mCurrentPart].getFilePath()));
        Log.d("PLAYER", "got file");
        Log.d("PLAYER", "URI" + mVideoUri);

        return true;
    }

    public void preparePlayer() {
        Log.d("PLAYER", "URI" + mVideoUri);
        mVideoView=MediaPlayer.create(tAct,mVideoUri);

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
                    finishVideo();
                }

            }
        });
    }

    public void startVideo()
    {
        /*mPlayButton.setVisibility(View.GONE);
        mPlayerMessage.setAlpha(0.0f);
        mPlayerMessage.setVisibility(View.GONE);
        mPlayButton.setAlpha(0.0f);*/

        mVideoView.start();
        Log.d("PLAYER","VIDEO LINE");
        //mAscii.pushLine(mVideoPart[mCurrentPart].getQuestion());
    }




    //LOAD PARTS
    public void loadParts()
    {
        Log.d("PLAYER", "LOADING PARTS");
        for(int i=0;i<16;i++)
        {

            Log.d("PLAYER","part:"+i);
            mVideoPart[i]=mServer.loadPart(mParent,i);
            //Log.d("PLAYER","part:"+i+"->"+mVideoPart[i]);
        }
    }

    //PLAY VIDEO
    public void playVideo()
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
        mAscii.pushLine("TAP THE SCREEN TO PLAY IT");

        if(mCurrentPart>mStartPart)
        {
            mAscii.clear();
            startVideo();
        }


    }

    public void playNext()
    {

        mAscii.pushLine("loading another part..");
        while(mVideoPart[mCurrentPart].isEmpty())
        {

        }
        Log.d("PLAYER","loaded");
        if(mVideoPart[mCurrentPart].isLast())
        {
            Log.d("PLAYER","LAST");
            mCurrentPart=16;
            finishVideo();
            return;
        }

        mVideoUri=Uri.fromFile(new File(mVideoPart[mCurrentPart].getFilePath()));

        preparePlayer();

    }

    public void finishVideo()
    {
        //Intent intent = new Intent(this, RecorderActivity.class);
        //intent.putExtra(EXTRA_MESSAGE2, String.valueOf(mParent));
        //startActivity(intent);
        //finish();

        //GO DIRECTLY TO RECORDING
        /*mNextButton.setAlpha(1.0f);
        mNextButton.setVisibility(View.VISIBLE);*/
    }


}



