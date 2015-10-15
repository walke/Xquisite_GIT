package no.ice_9.xquisite;

import android.app.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.util.TypedValue;

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
        mInitDone=false;

        mServer=new Server(this);
        mServerConnection=0;

        mAscii.pushLine("########################");
        mAscii.pushLine("#scienceFuture xquisite#");
        mAscii.pushLine("########################");

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                if(mTime==0){mAscii.pushLine("Initializing sequence...");}
                if(mTime==3)
                {
                    mAscii.pushLine("Testing connection to the server...");
                    if(mServer.checkConnection())
                    {
                        mServerConnection=1;
                    }
                    else{mServerConnection=-1;}

                }

                if(mServerConnection==1)
                {
                    mAscii.pushLine("Connection succesed");
                    mAscii.pushLine("");
                    mAscii.pushLine("!TAP THE SCREEN TO CONTINUE!");mInitDone=true;

                    this.cancel();
                }
                if(mServerConnection==-1)
                {
                    mAscii.pushLine("Connection failed");
                    mAscii.pushLine("");
                    mAscii.pushLine("THERE WAS A PROBLEM WITH A CONNECTION TO SERVER");
                    mAscii.pushLine("try to check your internet connection");
                    mAscii.pushLine("if your internet works fine, the problem is on server side");
                    this.cancel();
                }
                mTime++;
            }
        },0,500);


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
