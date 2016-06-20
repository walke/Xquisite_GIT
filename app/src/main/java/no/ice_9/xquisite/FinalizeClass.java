package no.ice_9.xquisite;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.util.TimerTask;

/**
 * Created by human on 03.04.16.
 *
 * Last subactivity, mostly used to thank user for participating and sending him back to start
 */
public class FinalizeClass extends SubAct{

    ASCIIscreen mAscii;
    //Server mServer;
    DBmanager mDBmanager;
    int mServerConnection=0;
    int mReconnectTime=-1;
    boolean mScreenSaver=false;

    int mTime;
    public boolean mInitDone=false;



    public FinalizeClass(Activity activity,ASCIIscreen ascii,DBmanager dBman)
    {

        mDBmanager=dBman;
        //mServer=server;
        mAscii = ascii;
        mTime=0;

    }

    @Override
    public int[] action(int act)
    {
        int[] result=new int[1];
        System.gc();//garbage collector, maybe not necessary
        result[0]=1;
        return result;
    }

    @Override
    public TimerTask getTimerTask()
    {
        return new TimerTask() {
            @Override
            public void run() {
                //mAscii.fillTrash();

                //mAscii.fillTrash();

                if(mAscii.mReady)
                {

                    mAscii.mGLView.mRenderer.setProgress(0.0f,1);
                    mAscii.mGLView.mRenderer.setProgress(0.0f,0);
                    mAscii.modLine("THANK YOU!",0,0);
                }

            }
        };
    }

    @Override
    public void destroy()
    {

    }

}
