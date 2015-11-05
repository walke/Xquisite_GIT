package no.ice_9.xquisite;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;

public class PlayerActivity extends Activity {

    private ASCIIscreen mAscii;
    TextView mText;

    public PlayerActivity tAct;
    private Thread mTask;

    private Uri mVideoUri;
    private VideoView mVideoView;
    private boolean mVideoReady;
    private int mCurrentPart;

    private String[] mVideoPart;

    //ASCII ACTION
    public void asciiAction(View view)
    {

        //finishVideo();
        if(mVideoReady)
        {
            mAscii.clear();
            startVideo(view);
        }
        else
        {
            mAscii.pushLine("You look a bit impatient");
        }

    }

    Server mServer;

    //LOAD VIDEO
    public boolean loadVideo()
    {
        int storyindx = mServer.getLastStoryNdx();
        //int storyindx=0;
        //storyindx=-1;
        //ParentNdx=storyindx;
        String filePath;
        if(storyindx==0)
        {
            Log.d("PLAYER", "no video ");
            mAscii.pushLine("no video found");
            mAscii.pushLine("****************************************");
            mAscii.pushLine("TAP THE SCREEN TO RECORD THE FIRST VIDEO");
            mAscii.mAsciiStopUpdater();
            return false;
        }
        else
        {
            mAscii.pushLine("we found some cluster of a story for you");
            mAscii.pushLine("we will now try to get it ready for you");
            mVideoPart[0]=mServer.loadPart(0,1);
            if(mVideoPart[0]=="-1")
            {
                mAscii.pushLine("no sorry there was no cluster");
                mAscii.pushLine("try to create one");
                mAscii.pushLine("****************************************");
                mAscii.pushLine("TAP THE SCREEN TO RECORD THE FIRST VIDEO");
                mAscii.mAsciiStopUpdater();
                return false;
            }

            mAscii.pushLine("seems like it is ready");
            Log.d("PLAYER","file path:"+mVideoPart[0]);

            /*String storyaddr = mServer.getVideoFile(storyindx);
            Log.d("PLAYER","ndx; "+ParentNdx);

            StoryParent=storyaddr;*/
            loadRest();
        }


        //expectedResponse=3;
        //mServer.waitForResponse();

        /*SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String adr = sharedPref.getString(SettingsActivity.KEY_PREF_SYNC_CONN, "server_adr");*/

        //String adr = getResources().getString(R.string.server_address);

        //mVideoUri=Uri.parse(adr+"stories/"+StoryParent);
        //Log.d("VIDEO_LOG", "URI "+mVideoUri);
        //mVideoUri=Uri.("http://81.191.243.140/uploads/tmp.mp4");
        Log.d("PLAYER","getting file");
        mAscii.pushLine("one more second..");
        mVideoUri=Uri.fromFile(new File(mVideoPart[0]));
        Log.d("PLAYER","got file");
        Log.d("PLAYER","URI"+mVideoUri);




        return true;
    }

    public void preparePlayer()
    {
        Log.d("PLAYER","URI"+mVideoUri);
        mVideoView.setVideoURI(mVideoUri);
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer player) {
                playVideo();
            }
        });

        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer player) {
                if (mCurrentPart < 6) {
                    mVideoReady = false;
                    mCurrentPart++;
                    playNext();
                } else {
                    finishVideo();
                }

            }
        });



    }

    public void playNext()
    {
        boolean done=false;
        mAscii.pushLine("loading another part..");
        while(!done)
        {
            if(mVideoPart[mCurrentPart]!="-1"){done=true;}
        }
        mVideoUri=Uri.fromFile(new File(mVideoPart[mCurrentPart]));

        preparePlayer();

    }

    //PLAY VIDEO
    public void playVideo()
    {




        //mPlayButton.setVisibility(View.VISIBLE);
        //mPlayButton.setAlpha(1.0f);

        //mVideoView.start();
        //int totDuration=mVideoView.getDuration();
        //mVideoView.seekTo((totDuration/3)*2);
        //Log.d("PLAYER", "duration"+);

        //boolean done=false;
        /*while(!done)
        {
            if(mVideoView.isPlaying() && mVideoView.getCurrentPosition()>((totDuration/3)*2))
            {
                done=true;
                //mVideoView.pause();

                //mLoadingFrame.setAlpha(0.0f);
            }
            else{mVideoView.start();Log.d("PLAYER", "not playing");}

        }*/
        mVideoView.setAlpha(1.0f);

        mVideoReady=true;

        mAscii.pushLine("So now it looks ready");
        mAscii.pushLine("#########################");
        mAscii.pushLine("TAP THE SCREEN TO PLAY IT");

    }

    public void startVideo(View view)
    {
        /*mPlayButton.setVisibility(View.GONE);
        mPlayerMessage.setAlpha(0.0f);
        mPlayerMessage.setVisibility(View.GONE);
        mPlayButton.setAlpha(0.0f);*/
        mVideoView.start();


    }

    private void loadRest()
    {
        for(int i=0;i>5;i++)
        {
            mVideoPart[i+1]=mServer.loadPart(0,i+2);
            Log.d("PLAYER","part:"+i+1+"->"+mVideoPart[i+1]);
        }
    }

    public void finishVideo()
    {
        Intent intent = new Intent(this, RecorderActivity.class);
        //intent.putExtra(EXTRA_MESSAGE2, String.valueOf(ParentNdx));
        startActivity(intent);

        //GO DIRECTLY TO RECORDING
        /*mNextButton.setAlpha(1.0f);
        mNextButton.setVisibility(View.VISIBLE);*/
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        mCurrentPart=0;
        mVideoPart=new String[6];
        for(int i=0;i<6;i++){mVideoPart[i]="-1";}
        mVideoReady=false;
        mText=(TextView)findViewById(R.id.text_player);
        mAscii=new ASCIIscreen(this,mText);
        mAscii.mAsciiStartUpdater(100);
        mServer = new Server(this);
        mVideoView=(VideoView)findViewById(R.id.playerSurface);
        mVideoView.setAlpha(0.0f);
        tAct=this;
        mTask = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("PLAYER", "getting list ");
                Looper.prepare();

                boolean result=loadVideo();

                if(result)
                {
                    tAct.runOnUiThread(new Runnable() {
                        public void run() {
                            Log.d("PLAYER", "got list");
                            preparePlayer();

                            //playVideo();

                        }
                    });
                }


            }
        });

        mTask.start();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_player, menu);
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
