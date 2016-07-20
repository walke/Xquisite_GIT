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
 *
 * First subactivity
 * Introduces user to the application
 */
public class InitClass extends SubAct{

    public static final char NB_UO=(char)128;
    public static final char NB_AE=(char)129;
    public static final char NB_OY=(char)130;
    public static final char NB_uo=(char)131;
    public static final char NB_ae=(char)132;
    public static final char NB_oy=(char)133;

    ASCIIscreen mAscii;
    MainActivity tAct;
    //Server_OLD mServer;
    DBmanager mDBmanager;
    int mServerConnection=0;
    int mReconnectTime=-1;
    boolean mScreenSaver=false;
    //Data appData;

    int mTime;
    public boolean mInitDone=false;
    Dialog mLoadingDialog;
    boolean loading=true;
    boolean mFromIntro;

    //@Override
    public InitClass(final MainActivity activity,ASCIIscreen ascii,DBmanager dBman,boolean fromintro)//,Data data)
    {
        String ldMsg=activity.getResources().getString(R.string.LoadingMsg);

        tAct=activity;
        mLoadingDialog = ProgressDialog.show(activity, "",
                ldMsg, true);
        loading=true;

        Thread loadingWait=new Thread(new Runnable() {
            @Override
            public void run() {
                while(!mAscii.mReady && loading);
                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run() {
                        mLoadingDialog.dismiss();
                        loading=false;
                    }
                });

            }
        });
        loadingWait.start();

        //appData=data;
        //mServer=server;
        mDBmanager=dBman;
        mAscii = ascii;
        mTime=0;

        mFromIntro=fromintro;


    }

    @Override
    public int[] action(int act)
    {
        Log.d("MAIN","initact"+act+","+mInitDone);
        int[]result=new int[1];
        result[0]=-1;
        if(act==3)
        {
            if(mInitDone)result[0]=1;
        }
        if(act==5)
        {
            result[0]=2;
        }




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

                    if(mFromIntro && mInitDone){
                        Log.d("MAIN","INIT");
                        tAct.glTouch(3);
                        mAscii.modLine("",0,0,false);
                        mAscii.modLine("",1,0,false);
                        mAscii.mGLView.mRenderer.clearAscii();
                        this.cancel();
                        return;


                    }
                    //if(mTime>=0 && mTime<20){mAscii.fillTrash();/*mAscii.setRage(true);*/mTime++;}

                    /*if(mTime==20)
                    {
                        mAscii.setRage(false);
                        mAscii.clear();
                        mTime++;
                    }*/
                    if(mTime==0 && !mAscii.isRage())
                    {
                        if(!mFromIntro) {
                            Log.d("ASCII", "toclear");
                            //mAscii.clear();
                            //mLoadingDialog.dismiss();
                            mAscii.putImage(BitmapFactory.decodeResource(mAscii.tAct.getResources(), R.drawable.logogsm));
                            //mAscii.pushLine("########################");
                            //mAscii.pushLine("#scienceFuture xquisite#");
                            //mAscii.pushLine("########################");
                            //mAscii.pushLine("Initializing sequence...");


                            // mAscii.pushLine("Xquisite takes roughly 5 minutes to play.");
                            //mAscii.pushLine("Before you play, we'd like to do a 3-minute interview which helps us develop the project further.");
                            //mAscii.modLine("", 1, 0,false);
                            mAscii.modLine(tAct.getResources().getString(R.string.InitMsg_welcome), 0, 0, true);
                        }
                        //mAscii.mGLView.mRenderer.setMode(mAscii.mGLView.mRenderer.MODE_INPT);
                        //for(int g=0;g<3;g++)mAscii.modLine("ll"+g, g, 0,true);

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

                        /*if(mServer.checkConnection())
                        {

                            /*if(appData.sync())
                            {
                                mServerConnection=1;
                            }
                            else
                            {
                                mServerConnection=1;
                            }*/
                            mServerConnection=1;

                        //}
                        /*else
                        {
                            mServerConnection=1;
                        }*/
                        Log.d("MAIN", "servResp" + mServerConnection);
                        mTime++;
                    }

                    if(mServerConnection==1  && !mAscii.isRage() && !mInitDone)
                    {
                        if(!mFromIntro) {
                            //mAscii.pushLine("Connection succesed");
                            //mAscii.pushLine("");
                            mAscii.modLine(tAct.getResources().getString(R.string.GlobMsg_continue), 2, 0, false);
                            //mAscii.pushLine("");
                        }
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
