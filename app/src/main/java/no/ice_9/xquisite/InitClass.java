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

    int mTime;
    public boolean mInitDone=false;
    Dialog mLoadingDialog;

    //@Override
    public InitClass(Activity activity,ASCIIscreen ascii,Server server)
    {
        mLoadingDialog = ProgressDialog.show(activity, "",
                      "Loading. Please wait...", true);

        mServer=server;
        mAscii = ascii;
        mTime=0;

    }

    @Override
    public int action()
    {
        if(mInitDone)return 1;
        else return -1;
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

                    if(mTime>=0 && mTime<20){mAscii.fillTrash();/*mAscii.setRage(true);*/mTime++;}
                    // if(mTime<2000){mAscii.modLine("scienceFuture xquisite",rnd.nextInt(50),rnd.nextInt(100));}
                    /*if(mTime==10){mAscii.putImage(((BitmapDrawable)getResources().getDrawable(R.drawable.xq_01)).getBitmap());}
                    if(mTime==15){mAscii.putImage(((BitmapDrawable) getResources().getDrawable(R.drawable.xq_02)).getBitmap());}
                    if(mTime==20){mAscii.putImage(((BitmapDrawable)getResources().getDrawable(R.drawable.xq_03)).getBitmap());}
                    if(mTime==25){mAscii.putImage(((BitmapDrawable) getResources().getDrawable(R.drawable.xq_04)).getBitmap());}
                    if(mTime==30){mAscii.putImage(((BitmapDrawable)getResources().getDrawable(R.drawable.xq_05)).getBitmap());}
                    if(mTime==35){mAscii.putImage(((BitmapDrawable) getResources().getDrawable(R.drawable.xq_06)).getBitmap());}
                    if(mTime==40){mAscii.putImage(((BitmapDrawable)getResources().getDrawable(R.drawable.xq_07)).getBitmap());}
                    if(mTime==45){mAscii.putImage(((BitmapDrawable) getResources().getDrawable(R.drawable.xq_08)).getBitmap());}
                    if(mTime==50){mAscii.putImage(((BitmapDrawable)getResources().getDrawable(R.drawable.xq_09)).getBitmap());}
                    if(mTime==55){mAscii.putImage(((BitmapDrawable) getResources().getDrawable(R.drawable.xq_10)).getBitmap());}
                    if(mTime==60){mAscii.putImage(((BitmapDrawable)getResources().getDrawable(R.drawable.xq_11)).getBitmap());}
                    if(mTime==65){mAscii.putImage(((BitmapDrawable) getResources().getDrawable(R.drawable.xq_12)).getBitmap());}
                    if(mTime==70){mAscii.putImage(((BitmapDrawable)getResources().getDrawable(R.drawable.xq_13)).getBitmap());}
                    if(mTime==75){mAscii.putImage(((BitmapDrawable) getResources().getDrawable(R.drawable.xq_14)).getBitmap());}
                    if(mTime==80){mAscii.putImage(((BitmapDrawable)getResources().getDrawable(R.drawable.xq_15)).getBitmap());}
                    if(mTime==85){mAscii.putImage(((BitmapDrawable)getResources().getDrawable(R.drawable.xq_16)).getBitmap());}*/
                    if(mTime==20)
                    {
                        mAscii.setRage(false);
                        //mAscii.clear();
                        mTime++;
                    }
                    if(mTime==21 && !mAscii.isRage())
                    {
                        mLoadingDialog.dismiss();
                        mAscii.putImage(BitmapFactory.decodeResource(mAscii.tAct.getResources(),R.drawable.logogsm));
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
                        mAscii.pushLine("!PRESS THE ORANGE BUTTON TO CONTINUE!");
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
                        //mAscii.putImage();
                        mInitDone=false;
                    }
                    Log.d("MAIN","conn"+mServerConnection);
                    Log.d("MAIN","time"+mTime);
                }

            }
        };
    }

    @Override
    public void destroy()
    {

    }

}
