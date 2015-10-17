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
import android.widget.VideoView;

import java.io.File;

public class PlayerActivity extends Activity {

    public PlayerActivity tAct;
    private Thread mTask;

    private Uri mVideoUri;
    private VideoView mVideoView;

    //LOAD VIDEO
    public boolean loadVideo()
    {
        //int storyindx = Integer.parseInt(mServer.getRandStoryNdx());
        int storyindx=0;

        //ParentNdx=storyindx;
        if(storyindx==-1)
        {
            Log.d("PLAYER","no video ");
        }
        else
        {
            /*String storyaddr = mServer.getVideoFile(storyindx);
            Log.d("PLAYER","ndx; "+ParentNdx);

            StoryParent=storyaddr;*/
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
        mVideoUri=Uri.fromFile(new File("/mnt/sdcard/tmp/tmp.mp4"));
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
            public void onCompletion(MediaPlayer player){finishVideo();}
        });



    }

    //PLAY VIDEO
    public void playVideo()
    {




        //mPlayButton.setVisibility(View.VISIBLE);
        //mPlayButton.setAlpha(1.0f);

        mVideoView.start();
        int totDuration=mVideoView.getDuration();
        mVideoView.seekTo((totDuration/3)*2);
        //Log.d("PLAYER", "duration"+);

        boolean done=false;
        while(!done)
        {
            if(mVideoView.isPlaying() && mVideoView.getCurrentPosition()>((totDuration/3)*2))
            {
                done=true;
                mVideoView.pause();

                //mLoadingFrame.setAlpha(0.0f);
            }
            else{mVideoView.start();Log.d("PLAYER", "not playing");}

        }
        mVideoView.setAlpha(1.0f);



    }

    public void startVideo(View view)
    {
        /*mPlayButton.setVisibility(View.GONE);
        mPlayerMessage.setAlpha(0.0f);
        mPlayerMessage.setVisibility(View.GONE);
        mPlayButton.setAlpha(0.0f);*/
        mVideoView.start();
    }

    public void finishVideo()
    {
        /*Intent intent = new Intent(this, RecorderActivity.class);
        intent.putExtra(EXTRA_MESSAGE2, String.valueOf(ParentNdx));
        startActivity(intent);*/

        //GO DIRECTLY TO RECORDING
        /*mNextButton.setAlpha(1.0f);
        mNextButton.setVisibility(View.VISIBLE);*/
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        mVideoView=(VideoView)findViewById(R.id.playerSurface);
        mVideoView.setAlpha(0.0f);
        tAct=this;
        mTask = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("PLAYER", "getting list ");
                Looper.prepare();

                loadVideo();


                tAct.runOnUiThread(new Runnable() {
                    public void run() {
                        Log.d("PLAYER", "got list");
                        preparePlayer();

                        //playVideo();

                    }
                });

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
