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
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;

public class PlayerActivity extends Activity {

    public final static String EXTRA_MESSAGE2 = "no.ice_9.xquisite.MESSAGE2";

    private ASCIIscreen mAscii;
    TextView mText;

    public PlayerActivity tAct;
    private Thread mTask;

    private Uri mVideoUri;
    private VideoView mVideoView;
    private boolean mVideoReady;
    private boolean mError;

    private int mParent;
    private int mCurrentPart;
    private static int mStartPart=1;

    static String[] mQuestion;


    private StoryPart[] mVideoPart;

    //ASCII ACTION
    public void asciiAction(View view)
    {
        if(mError)
        {
            finishVideo();
        }
        else if(mVideoReady)
        {
            mAscii.clear();
            startVideo();
        }
        else
        {
            mAscii.pushLine("You look a bit impatient");
        }

    }

    Server mServer;

    //LOAD STORY DATA
    public boolean loadStoryData()
    {
        int storyindx = mServer.getLastStoryNdx();
        mParent=storyindx;

        if(storyindx==0)
        {
            Log.d("PLAYER", "no video ");
            mAscii.pushLine("no video found");
            mAscii.pushLine("****************************************");
            mAscii.pushLine("TAP THE SCREEN TO RECORD THE FIRST VIDEO");
            Log.d("PLAYER", "no video1 ");

            mError=true;
            return false;
        }
        else
        {
            mAscii.pushLine("we found some cluster of a story for you");
            mAscii.pushLine("we will now try to get it ready for you");





            new Thread(new Runnable() {
                @Override
                public void run() {

                    loadParts();

                }
            }).start();


            while(mVideoPart[mCurrentPart].isEmpty())
            {

            }



            mAscii.pushLine("seems like it is ready");
            Log.d("PLAYER", "file path:" + mVideoPart[mCurrentPart]);



        }

        Log.d("PLAYER", "getting file");
        //mAscii.pushLine("one more second..");
        mVideoUri=Uri.fromFile(new File(mVideoPart[mCurrentPart].getFilePath()));
        Log.d("PLAYER","got file");
        Log.d("PLAYER", "URI" + mVideoUri);

        return true;
    }

    //LOAD VIDEO
    public boolean loadVideo()
    {
        int storyindx = mServer.getLastStoryNdx();
        //int storyindx=0;
        //storyindx=-1;
        //ParentNdx=storyindx;
        mParent=storyindx;
        String filePath;
        if(storyindx==0)
        {
            Log.d("PLAYER", "no video ");
            mAscii.pushLine("no video found");
            mAscii.pushLine("****************************************");
            mAscii.pushLine("TAP THE SCREEN TO RECORD THE FIRST VIDEO");
            mAscii.mAsciiStopUpdater(1);
            mError=true;
            return false;
        }
        else
        {
            mAscii.pushLine("we found some cluster of a story for you");
            mAscii.pushLine("we will now try to get it ready for you");

            mAscii.pushLine("");
            mAscii.pushLine("This is a story game about XX chromosome human born in 2045");
            mAscii.pushLine("Her name is X");
            mAscii.pushLine("The person before you made up part of her story. You are invited to continue her story");
            mAscii.pushLine("");
            Log.d("PLAYER", "DESCINST");

            mVideoPart[0]=mServer.loadPart(storyindx,0);



            if(mVideoPart[0].isEmpty())
            {
                mAscii.pushLine("no sorry there was no cluster");
                mAscii.pushLine("try to create one");
                mAscii.pushLine("****************************************");
                mAscii.pushLine("TAP THE SCREEN TO RECORD THE FIRST VIDEO");
                mAscii.mAsciiStopUpdater(1);
                return false;
            }



            //mAscii.pushLine("seems like it is ready");
            Log.d("PLAYER", "file path:" + mVideoPart[0]);

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
        Log.d("PLAYER", "getting file");
        //mAscii.pushLine("one more second..");
        mVideoUri=Uri.fromFile(new File(mVideoPart[0].getFilePath()));
        Log.d("PLAYER","got file");
        Log.d("PLAYER", "URI" + mVideoUri);




        return true;
    }

    public void preparePlayer()
    {
        Log.d("PLAYER", "URI" + mVideoUri);
        mVideoView.setVideoURI(mVideoUri);
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer player) {
                playVideo();
            }
        });

        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer player) {

                if (mCurrentPart < 16) {

                    mVideoReady = false;
                    mCurrentPart++;
                    Log.d("PLAYER", "PARTQ" + mVideoPart[mCurrentPart].getQuestion());
                    playNext();
                } else {
                    finishVideo();
                }

            }
        });



    }

    public void playNext()
    {

        mAscii.pushLine("loading another part..");
        while(mVideoPart[mCurrentPart].isEmpty())
        {

        }
        Log.d("PLAYER","loaded");
        if(mVideoPart[mCurrentPart].isLast())
        {
            Log.d("PLAYER","LAST");
            mCurrentPart=16;
            finishVideo();
            return;
        }

        mVideoUri=Uri.fromFile(new File(mVideoPart[mCurrentPart].getFilePath()));

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

        //mAscii.pushLine("So now it looks ready");




        mAscii.pushLine("#########################");
        mAscii.pushLine("TAP THE SCREEN TO PLAY IT");

        if(mCurrentPart>mStartPart)
        {
            mAscii.clear();
            startVideo();
        }


    }

    public void startVideo()
    {
        /*mPlayButton.setVisibility(View.GONE);
        mPlayerMessage.setAlpha(0.0f);
        mPlayerMessage.setVisibility(View.GONE);
        mPlayButton.setAlpha(0.0f);*/

        mVideoView.start();
        Log.d("PLAYER","VIDEO LINE");
        //mAscii.pushLine(mVideoPart[mCurrentPart].getQuestion());
    }

    public void loadRest()
    {
        Log.d("PLAYER", "LOADING REST");
        for(int i=0;i<16;i++)
        {

            Log.d("PLAYER","part:"+(i+1));
            mVideoPart[(i+1)]=mServer.loadPart(mParent,(i+1));
            Log.d("PLAYER","part:"+(i+1)+"->"+mVideoPart[(i+1)]);
        }
    }

    //LOAD PARTS
    public void loadParts()
    {
        Log.d("PLAYER", "LOADING PARTS");
        for(int i=0;i<16;i++)
        {

            Log.d("PLAYER","part:"+i);
            mVideoPart[i]=mServer.loadPart(mParent,i);
            //Log.d("PLAYER","part:"+i+"->"+mVideoPart[i]);
        }
    }

    public void finishVideo()
    {
        Intent intent = new Intent(this, RecorderActivity.class);
        intent.putExtra(EXTRA_MESSAGE2, String.valueOf(mParent));
        startActivity(intent);
        finish();

        //GO DIRECTLY TO RECORDING
        /*mNextButton.setAlpha(1.0f);
        mNextButton.setVisibility(View.VISIBLE);*/
    }

    private void initQuestions()
    {

        mQuestion=new String[]
                {
                        "How old is X now?",
                        "Where is X now?",
                        "What is she doing now?",
                        "What is she feeling?",
                        "What is she thinking?",
                        "What is her biggest challenge?"
                };


        //there are as many parts as questions +1 free part;
        //initially they are not done

    }

    @Override
    protected void onPause() {
        super.onPause();

        mAscii.mAsciiStopUpdater(1);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mError=false;

        mCurrentPart=mStartPart;
        mVideoPart=new StoryPart[16];
        for(int i=0;i<16;i++){mVideoPart[i]=new StoryPart();}



        mQuestion =new String[7];
        initQuestions();

        //for(int i=0;i<6;i++){mVideoPart[i]="-1";}
        mVideoReady=false;
        mText=(TextView)findViewById(R.id.text_player);
        mAscii=new ASCIIscreen(this,mText,"PLAYER");
        mAscii.mAsciiStartUpdater(100);
        mAscii.pushLine("loading video..");

        mAscii.pushLine("");
        mAscii.pushLine("This is a story game about XX chromosome human born in 2045");
        mAscii.pushLine("Her name is X");
        mAscii.pushLine("The person before you made up part of her story. You are invited to continue her story");
        mAscii.pushLine("");
        Log.d("PLAYER", "DESCINST");
        mServer = new Server(this);
        mVideoView=(VideoView)findViewById(R.id.playerSurface);
        mVideoView.setAlpha(0.0f);
        tAct=this;
        mTask = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("PLAYER", "getting list ");
                Looper.prepare();

                //boolean result=loadVideo();
                boolean result=loadStoryData();

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
                else
                {
                    mAscii.mAsciiStopUpdater(100);
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
