package no.ice_9.xquisite;

import android.app.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.util.TypedValue;

import java.io.IOException;
import java.util.Calendar;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends Activity {

    //DEBUG TIME MEASURE
    long mMesTime=0;
    long mLasTime=0;

    InitClass initClass;
    PlayerClass playerClass;
    RecorderClass recorderClass;

    //CURRENT ACTION
    private int mCurrentAction=0;

    //ACII LAYER
    public ASCIIscreen mAscii;
    private TextView mText;

    ProgressDialog mLoadingDialog;

    //MAIN LOOP
    public TimerTask mTimerLoop;
    public Timer mTimer;

    //TIMINGS
    private int mTime;
    private boolean mInitDone;
    private int mReconnectTime;
    private boolean mScreenSaver;

    //SERVER
    private int mServerConnection;
    private Server mServer;

    public int mParent=-1;

    //Start new activity for creating new part of a story.
    /*public void CreateNewStory()
    {

        if(mScreenSaver)
        {
            mTime=19;
            mScreenSaver=false;
        }
        else if(initClass.mInitDone)
        {
            Log.d("PLAYER","IN");
            mAscii.mAsciiStopUpdater(1);
            mTimerLoop.cancel();
            mTimer.cancel();
            //Intent intent = new Intent(this, PlayerActivity.class);
            //intent.putExtra("ascii",mAscii);
            //startActivity(intent);
            //view.setVisibility(View.GONE);
            mCurrentAction++;
            createTimerTask();

            mTimer=new Timer();
            mInitDone=false;
            mTimer.scheduleAtFixedRate(mTimerLoop, 0, 60);
            mTime=0;
        }

    }*/

    public void glTouch()
    {
        Log.d("GL","TOUCH");
        //CreateNewStory();
        int result=-1;
        switch(mCurrentAction)
        {
            case 0:
                result=initClass.action();
                break;
            case 3:
                result=playerClass.action();
                mParent=result;
                break;
            case 4:
                result=recorderClass.action();
                break;
        }

        if(result!=0)
        {
            mAscii.mAsciiStopUpdater(1);
            mTimerLoop.cancel();
            mTimer.cancel();

            mCurrentAction++;
            createTimerTask();

            mTimer=new Timer();
            mInitDone=false;
            mTimer.scheduleAtFixedRate(mTimerLoop, 0, 60);
            mTime=0;
        }
    }

    private void createTimerTask()
    {
        switch(mCurrentAction)
        {
            case 0:
                initClass=new InitClass(this,mAscii,mServer);
                mTimerLoop=initClass.getTimerTask();
                break;

            case 3:

                playerClass=new PlayerClass(this,mAscii,mServer);
                mTimerLoop=playerClass.getTimerTask();
                break;

            case 4:

                recorderClass=new RecorderClass(this,mAscii,mServer,mParent);
                mTimerLoop=recorderClass.getTimerTask();
                break;
        }



    }



    //TODO: fix on touch event
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        super.onTouchEvent(event);

        glTouch();
        //mAscii.modLine("tatatat", 0, -1);
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
        Log.d("MAIN","paused");
        mAscii.mAsciiStopUpdater(1);
        mTimerLoop.cancel();
        mTimer.cancel();
        mTimer.purge();
        //mTimerLoop=null;
        mAscii.mGLView.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mAscii.mAsciiStartUpdater(50);
        createTimerTask();
        mTimer=new Timer();
        mInitDone=false;
        mTimer.scheduleAtFixedRate(mTimerLoop, 0, 60);
        mTime=0;
        mAscii.mGLView.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMesTime= Calendar.getInstance().getTimeInMillis();
        mLasTime=mMesTime;


        //setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mMesTime= Calendar.getInstance().getTimeInMillis();
        Log.d("TIME","start "+(mMesTime-mLasTime)+"ms");
        mLasTime=mMesTime;



        //GET GLES
        mTime=0;
        mAscii=new ASCIIscreen(this,mText,"MAIN");
        setContentView(mAscii.mGLView);
        //mAscii.mAsciiStartUpdater(50);

        mMesTime= Calendar.getInstance().getTimeInMillis();
        Log.d("TIME","GLES INIT "+(mMesTime-mLasTime)+"ms");
        mLasTime=mMesTime;

        //STAT VARS
        mInitDone=false;
        mReconnectTime=-1;
        mScreenSaver=false;



        //SERVER
        mServer=new Server(this);
        mServerConnection=0;

        mMesTime= Calendar.getInstance().getTimeInMillis();
        Log.d("TIME","SRV INIT "+(mMesTime-mLasTime)+"ms");
        mLasTime=mMesTime;


        //MAIN TREAD
        mTimer=new Timer();

        mMesTime= Calendar.getInstance().getTimeInMillis();
        Log.d("TIME","TIMER INIT "+(mMesTime-mLasTime)+"ms");
        mLasTime=mMesTime;

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


}
