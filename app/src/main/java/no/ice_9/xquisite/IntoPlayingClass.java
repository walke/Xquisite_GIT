package no.ice_9.xquisite;

import android.app.Activity;
import android.os.Looper;
import android.util.Log;

import java.util.TimerTask;
import java.util.concurrent.RunnableFuture;

/**
 * Created by human on 05.07.16.
 */
public class IntoPlayingClass extends PlayerClass{

    public IntoPlayingClass(MainActivity activity, ASCIIscreen ascii, DBmanager dBman, int parent, int parentParts)
    {
        tAct=activity;
        mDBmanager=dBman;
        mAscii = ascii;
        mParent=-2;
        mStoryParts=1;

        init();
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
                            mAscii.modLine("ICE-9 invites you to help invent the life of X", 0, 0,true);
                            mAscii.modLine("", 1, 0,false);
                        }
                        //mAscii.modLine();

                        //mAscii.pushLine("PRESS THE BUTTON TO PLAY");


                        //mAscii.pushLine("");
                        mTime++;









                    }
                    else if(mTime>0 && mTime<2200)
                    {
                        if(mVideoView!=null)
                        {
                            if(mVideoView.isPlaying() )
                            {
                                if(mTime%20==0)Log.d("PLAYER","mtime"+(mTime/20)+","+mTime);
                                if(mTime==80)
                                {
                                    mAscii.modLine("you will hear part of X's story",0,0,true);
                                }
                                if(mTime==160)
                                {
                                    mAscii.modLine("you make up what X does next",0,0,true);
                                }
                                if(mTime==320)
                                {
                                    mAscii.modLine("this will be passed to the next player",0,0,true);
                                }
                                if(mTime==500)
                                {
                                    mAscii.modLine("we encourage you to be funny, provocative, and dramatic",0,0,true);
                                }
                                if(mTime==900)
                                {
                                    mAscii.modLine("your ideas will be reworked and recombined by kids and artists",0,0,true);
                                }
                                if(mTime==1300)
                                {
                                    mAscii.modLine("REMEMBER THAT XQUISITE IS A FICTION GAME. THE STORIES COME FROM YOU, AS A PRIVATE PERSON, AND ARE NOT A PROFESSIONAL STATEMENT.",0,0,true);
                                }
                                if(mVideoView.getCurrentPosition()>1)mTime++;
                            }
                        }

                    }

                    if(mVideoView!=null && mTime==1)
                    {
                        if(mVideoView.isPlaying() )
                        {
                            //mAscii.modLine("",0,0,false);
                            mAscii.modLine("",2,0,false);
                            mAscii.modLine("",3,0,false);
                            //mAscii.modLine("PLAYING..",1,0,true);

                            //mTime++;
                        }
                    }
                    if(mVideoView!=null && mTime>1)
                    {
                        int pos=mVideoView.getCurrentPosition();
                        if(mVideoView.isPlaying() )
                        {

                            int dur=mVideoView.getDuration();
                            mAscii.mGLView.mRenderer.setProgress((float)pos/(float)dur,1);
                        }
                        else if(pos>0 && mParent==-2)
                        {
                            mError=true;
                            tAct.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tAct.glTouch(3);
                                }
                            });

                        }
                    }

                }

                    /*if(mVideoView!=null)
                    {
                        if(mVideoView.isPlaying())
                        {

                        }



                    }*/

                }


        };
    }

}
