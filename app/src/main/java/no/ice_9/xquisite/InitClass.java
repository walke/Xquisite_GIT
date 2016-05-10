package no.ice_9.xquisite;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.util.TimerTask;

/**
 * Created by human on 23.03.16.
 */
public class InitClass extends SubAct{

    ASCIIscreen mAscii;
    Server mServer;
    int mServerConnection=0;
    int mReconnectTime=-1;
    boolean mScreenSaver=false;
    Data appData;

    int mTime;
    public boolean mInitDone=false;
    Dialog mLoadingDialog;

    //@Override
    public InitClass(Activity activity,ASCIIscreen ascii,Server server,Data data)
    {
        mLoadingDialog = ProgressDialog.show(activity, "",
                      "Loading. Please wait...", true);

        appData=data;
        mServer=server;
        mAscii = ascii;
        mTime=0;

    }

    @Override
    public int[] action()
    {
        int[]result=new int[1];
        result[0]=-1;
        if(mInitDone)result[0]=1;

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

                    //if(mTime>=0 && mTime<20){mAscii.fillTrash();/*mAscii.setRage(true);*/mTime++;}

                    /*if(mTime==20)
                    {
                        mAscii.setRage(false);
                        //mAscii.clear();
                        mTime++;
                    }*/
                    if(mTime==0 && !mAscii.isRage())
                    {
                        mLoadingDialog.dismiss();
                        mAscii.putImage(BitmapFactory.decodeResource(mAscii.tAct.getResources(), R.drawable.logogsm));
                        //mAscii.pushLine("########################");
                        //mAscii.pushLine("#scienceFuture xquisite#");
                        //mAscii.pushLine("########################");
                        //mAscii.pushLine("Initializing sequence...");


                       // mAscii.pushLine("Xquisite takes roughly 5 minutes to play.");
                        //mAscii.pushLine("Before you play, we'd like to do a 3-minute interview which helps us develop the project further.");
                        mAscii.modLine("", 1, 0);
                        mAscii.modLine("Welcome to Xquisite! Questions appear here and you respond to the camera. This game takes under 10min. ",0,0);
                        //mAscii.pushLine("Try and center your face in the window,");
                        //mAscii.pushLine("and speak directly into the device.");
                        mTime++;
                    }
                    if(mTime==1 && !mAscii.isRage())
                    {
                        //mAscii.pushLine("Testing connection to the server...");
                        mInitDone=false;
                        mTime++;
                    }
                    if(mTime==2 && !mAscii.isRage())
                    {

                        if(mServer.checkConnection())
                        {

                            if(appData.sync())
                            {
                                mServerConnection=1;
                            }
                            else
                            {
                                mServerConnection=-1;
                            }

                        }
                        else
                        {
                            mServerConnection=1;
                        }
                        Log.d("MAIN", "servResp" + mServerConnection);
                        mTime++;
                    }

                    if(mServerConnection==1  && !mAscii.isRage() && !mInitDone)
                    {
                        //mAscii.pushLine("Connection succesed");
                        //mAscii.pushLine("");
                        mAscii.modLine("!PRESS THE RED BUTTON TO CONTINUE!",2,0);
                        //mAscii.pushLine("");
                        mInitDone=true;

                        //this.cancel();
                        //mAscii.mAsciiStopUpdater();
                        //mTime++;
                    }
                    if(mInitDone)
                    {

                        //Log.d("MAIN","WAITING FOR TOUCH");//mTime++;
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
                        //mAscii.fillTrash();
                        //mAscii.putImage();
                        mInitDone=false;
                    }
                    //Log.d("MAIN","conn"+mServerConnection);
                    //Log.d("MAIN","time"+mTime);
                }

            }
        };
    }

    @Override
    public void destroy()
    {

    }

}
