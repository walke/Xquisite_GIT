package no.ice_9.xquisite;

import android.app.Activity;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.IBinder;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.util.TypedValue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends Activity {




    Data appData;

    SubAct currentSubActivity;


    //CURRENT ACTION
    private int mCurrentAction=0;

    //ACII LAYER
    public ASCIIscreen mAscii;


    //MAIN LOOP
    public TimerTask mTimerLoop;
    public Timer mTimer;



    //SERVER
    private Server mServer;

    public int mParent=-1;
    public int mParentParts=-1;
    public int mReservedStory=-1;
    public int mPartOffset=-1;


    boolean mUserWait=true;
   boolean interSkip=false;


    public void diaBut(boolean skip)
    {
        mUserWait=false;
        interSkip=skip;
        glTouch();
    }

    public void glTouch()
    {
        Log.d("GL","TOUCH");
        //CreateNewStory();
        int[] result;
        int res=-1;
        if(mUserWait)
        {
            //mCurrentAction=-2;
            DialogFragment dg= new InterviewSkip();
            dg.show(getFragmentManager(),"Skip Interview");

            return;
            //while(mUserWait);
         }
        switch(mCurrentAction)
        {
            case 0:
                result=currentSubActivity.action();
                res=result[0];
                if(interSkip)mCurrentAction++;
                //if(res==1)mAscii.maximizeInfo();
                break;
            case 1:
                result=currentSubActivity.action();
                res=result[0];
                mParent=result[0];
                mParentParts=result[1];
                mReservedStory=result[2];
                mPartOffset=result[3];
                //if (result==1){mCurrentAction++;}
                break;

            case 2:
                result=currentSubActivity.action();
                res=result[0];
                //mParent=result;
                break;
            case 3:
                result=currentSubActivity.action();
                res=result[0];
                //if (result==1){mCurrentAction++;}
                break;

            case 4:
                result=currentSubActivity.action();
                res=result[0];
                mCurrentAction=-1;
                break;
        }


        if(res!=-1)
        {

            mAscii.mAsciiStopUpdater(1);
            while(mAscii.mUpdating);
            mTimerLoop.cancel();

            mTimer.cancel();
            mTimer.purge();

            //mTimerLoop=null;
            //mTimer=null;

            currentSubActivity.destroy();
            currentSubActivity=null;

            mCurrentAction++;
            createTimerTask();


            mTimer=new Timer();

            //mAscii.mGLView.onPause();
            mTimer.scheduleAtFixedRate(mTimerLoop, 0, 60);
            //mAscii.mGLView.onResume();


        }
    }

    private void createTimerTask()
    {
        switch(mCurrentAction)
        {
            case 0:
                currentSubActivity=new InitClass(this,mAscii,mServer,appData);
                //currentSubActivity.Create(this,mAscii,mServer);
                //initClass=new InitClass(this,mAscii,mServer);
                mTimerLoop=currentSubActivity.getTimerTask();
                break;

            case 1:
                currentSubActivity=new PreRecorderClass(this,mAscii,mServer);
                //recorderClass=new RecorderClass(this,mAscii,mServer,mParent);
                mTimerLoop=currentSubActivity.getTimerTask();
                break;

            case 2:
                currentSubActivity=new PlayerClass(this,mAscii,mServer,mParent,mParentParts);
                //currentSubActivity=new PlayerClass(this,mAscii,mServer,1,14);
                //playerClass=new PlayerClass(this,mAscii,mServer);
                mTimerLoop=currentSubActivity.getTimerTask();
                break;

            case 3:
                currentSubActivity=new RecorderClass(this,mAscii,mServer,mParent,mReservedStory,mPartOffset);
                //recorderClass=new RecorderClass(this,mAscii,mServer,mParent);
                mTimerLoop=currentSubActivity.getTimerTask();
                break;
            case 4:
                currentSubActivity=new FinalizeClass(this,mAscii,mServer);
                //recorderClass=new RecorderClass(this,mAscii,mServer,mParent);
                mTimerLoop=currentSubActivity.getTimerTask();
                break;
        }



    }





    //TODO: fix on touch event
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        super.onTouchEvent(event);

        glTouch();
        Log.d("MAIN","touch");

        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("MAIN", "paused");
        mAscii.mAsciiStopUpdater(1);
        mTimerLoop.cancel();
        mTimer.cancel();
        mTimer.purge();

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("MAIN", "paused");
        mAscii.mAsciiStopUpdater(1);
        mTimerLoop.cancel();
        mTimer.cancel();
        mTimer.purge();

        mAscii.mGLView.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTimerLoop=null;

    }

    @Override
    protected void onResume() {
        super.onResume();
        mAscii.mAsciiStartUpdater(50);
        createTimerTask();
        mTimer=new Timer();

        mTimer.scheduleAtFixedRate(mTimerLoop, 0, 60);

        mAscii.mGLView.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        //SERVER
        mServer=new Server(this);

        //GET DATA
        appData=new Data(this,mServer);



        /*try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // Restore interrupt status.
            Thread.currentThread().interrupt();
        }*/


        //finish();

        //setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //GET GLES
        mAscii=new ASCIIscreen(this,"MAIN");
        setContentView(mAscii.mGLView);



        //MAIN TREAD
        mTimer=new Timer();


        //AUTOBOT
        /*TimerTask auto= new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        glTouch();
                    }
                });

            }};

        new Timer().scheduleAtFixedRate(auto, 0, 4000);*/

    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class InterviewSkip extends DialogFragment {
        /*MainActivity acti

        public InterviewSkip(MainActivity activity){}*/

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("If you are here for the first time, we would like you to answer few questions first, but you can skip it if you want!")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ((MainActivity)getActivity()).diaBut(false);
                        }
                    })
                    .setNegativeButton("skip", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                            ((MainActivity)getActivity()).diaBut(true);
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }


}

class SubAct
{


    public TimerTask getTimerTask()
    {
        return new TimerTask() {
            @Override
            public void run() {

            }
        };
    }

    public int[] action()
    {
        int[] result=new int[1];
        return result;
    }

    public void destroy()
    {

    }
}

class Data
{
    Server mServer;
    File mData;

    int mDeviceId;

    public Data(Activity act,Server server)
    {
        mServer=server;
        mData=new File(act.getFilesDir(),"data");

        //CHECK IF DATA FILE IS PRESENT ON THE DEVICE
        if(!mData.exists())
        {
            Log.d("MAIN", "no data file");

            try
            {
                mData.createNewFile();



            }
            catch (Exception io){Log.e("MAIN","could not create data file");}

            initData();
        }
        else if(mData.length()<4)
        {
            initData();
        }
    }

    public boolean sync()
    {
        getData();


        mDeviceId=mServer.requestDeviceId(mDeviceId);

        String data=mServer.requestDeviceData(mDeviceId);

        data.toCharArray();


        if(mDeviceId==0)return false;


        return true;
    }

    private void getData()
    {
        try
        {
            FileInputStream fi=new FileInputStream(mData);

            byte[] buf=new byte[4];
            fi.read(buf,0,4);
            fi.close();

            mDeviceId= (buf[0] << 24) | (buf[1] << 16) | (buf[2] << 8) | buf[3];

            Log.d("DATA","local devId:"+ mDeviceId);

        }
        catch (Exception io)
        {
            Log.e("DATA","could not get data");
        }


    }

    private void initData()
    {
        byte[] buf={0,0,0,0};

        try
        {
            FileOutputStream fo=new FileOutputStream(mData);
            fo.write(buf,0,4);
            fo.close();

        }
        catch (Exception io)
        {
            Log.e("DATA","could not initialize data");
        }
        mDeviceId=0;
    }

   /* private void getAppData()
    {


        FileOutputStream fo;
        FileInputStream fi;

        if(!data.exists())
        {

            try
            {
                data.createNewFile();
            }catch (Exception io)
            {

            }

        }
        if(data.exists() && data.length()==0)
        {
            Log.d("MAIN","data file found");
            try
            {

                fo=new FileOutputStream(data);





                fo.close();
            }catch (Exception io)
            {
                Log.e("MAIN","could not read data");
            }

        }
        else if(data.exists())
        {

        }


    }*/


}


