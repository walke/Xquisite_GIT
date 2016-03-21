package no.ice_9.xquisite;

import android.app.Activity;

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
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends Activity {

    //ACII LAYER
    public ASCIIscreen mAscii;
    private TextView mText;



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

    //Start new activity for creating new part of a story.
    public void CreateNewStory()
    {
        if(mScreenSaver)
        {
            mTime=19;
            mScreenSaver=false;
        }
        else if(mInitDone)
        {
            mAscii.mAsciiStopUpdater(1);
            mTimerLoop.cancel();
            Intent intent = new Intent(this, PlayerActivity.class);
            startActivity(intent);
            //view.setVisibility(View.GONE);
        }

    }

    public void glTouch()
    {
        CreateNewStory();
    }

    private void createTimerTask()
    {

        mTimerLoop = new TimerTask() {
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
                    if(mTime==20){mAscii.setRage(false);mAscii.clear();mTime++;}
                    if(mTime==21 && !mAscii.isRage())
                    {
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
                    if(mInitDone){Log.d("MAIN","WAITING FOR TOUCH");mTime++;}
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
                        mInitDone=false;
                    }
                    Log.d("MAIN","conn"+mServerConnection);
                    Log.d("MAIN","time"+mTime);
                }

            }
        };
    }



    //TODO: fix on touch event
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        super.onTouchEvent(event);

        CreateNewStory();
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
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mTime=0;
        mText=(TextView)findViewById(R.id.text_main);

        //mGlview=(TextView)findViewById(R.id.text_main);
        mAscii=new ASCIIscreen(this,mText,"MAIN");
        setContentView(mAscii.mGLView);
        //mAscii.mAsciiStartUpdater(50);
        mInitDone=false;
        mReconnectTime=-1;
        mScreenSaver=false;




        mServer=new Server(this);
        mServerConnection=0;

        final Random rnd = new Random();


        mTimer=new Timer();
                //new Timer().scheduleAtFixedRate(mTimerLoop,0,60);


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
