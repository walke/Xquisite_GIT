package no.ice_9.xquisite;

import android.app.Activity;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.util.TypedValue;

import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends Activity {

    private ASCIIscreen mAscii;
    private TextView mText;
    private int mTime;
    private boolean mInitDone;

    private int mServerConnection;
    private Server mServer;

    //Start new activity for creating new part of a story.
    public void CreateNewStory(View view)
    {
        if(mInitDone)
        {
            Intent intent = new Intent(this, PlayerActivity.class);
            startActivity(intent);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTime=0;
        mText=(TextView)findViewById(R.id.text_main);
        mAscii=new ASCIIscreen(this,mText);
        mAscii.mAsciiStartUpdater(50);
        mInitDone=false;




        mServer=new Server(this);
        mServerConnection=0;

        final Random rnd = new Random();

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                //mAscii.fillTrash();

                //mAscii.fillTrash();

                if(mAscii.mReady)
                {

                    if(mTime==0){mAscii.fillTrash();/*mAscii.setRage(true);*/}
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
                    if(mTime>20){mAscii.setRage(false);mAscii.clear();}
                    if(mTime>21 && !mAscii.isRage())
                    {
                        mAscii.pushLine("########################");
                        mAscii.pushLine("#scienceFuture xquisite#");
                        mAscii.pushLine("########################");
                        mAscii.pushLine("Initializing sequence...");
                    }
                    if(mTime>26 && !mAscii.isRage())
                    {
                        mAscii.pushLine("Testing connection to the server...");
                        if(mServer.checkConnection())
                        {
                            mServerConnection=1;
                        }
                        else{mServerConnection=-1;}
                        Log.d("MAIN","servResp"+mServerConnection);
                    }

                    if(mServerConnection==1  && !mAscii.isRage())
                    {
                        mAscii.pushLine("Connection succesed");
                        mAscii.pushLine("");
                        mAscii.pushLine("!TAP THE SCREEN TO CONTINUE!");mInitDone=true;

                        this.cancel();
                        mAscii.mAsciiStopUpdater();
                    }
                    if(mServerConnection==-1  && !mAscii.isRage())
                    {
                        mAscii.pushLine("Connection failed");
                        mAscii.pushLine("");
                        mAscii.pushLine("THERE WAS A PROBLEM WITH A CONNECTION TO SERVER");
                        mAscii.pushLine("try to check your internet connection");
                        mAscii.pushLine("if your internet works fine, the problem is on server side");
                        this.cancel();
                        mAscii.mAsciiStopUpdater();
                    }
                    mTime++;
                }

            }
        },0,60);


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
