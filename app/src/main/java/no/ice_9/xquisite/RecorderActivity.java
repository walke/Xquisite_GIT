package no.ice_9.xquisite;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class RecorderActivity extends Activity {
    /*TODO: crashed on forceStopCapture after closing activity, probably needs to be forced to stop "onActivityPause"*/
    //TODO: doesn't crash if recording already started before closing activity.. then yes.. fix it later
    //TODO: nope still crashes when expected time for recording runs out.. fix it anyway

    //ENUMS
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    public static final int FTIME = 5;
    public static final int QTIME = 5;

    public static final int NPARTS= 7;

    //CLASS VARIABLES
    RecorderActivity tAct;

    Server mServer;

    Camera mCamera;//Deprecated.. don't know yet what to do about it
    Preview mPreview;
    MediaRecorder mRecorder;

    ASCIIscreen mAscii;
    TextView mText;

    int mTimeLeft;
    boolean isRecording;
    int mCurrentPart;

    static String fileToUpload;
    static String mFilePath;

    private boolean[] mPartDone;

    static String[] mQuestion;
    static int mQuestionTime;
    boolean mUserReady;
    boolean mMainDone;
    StoryPart[] mVideoPart;
    int[] mPartReady;//0:NOT READY, 1:READY TO UPLOAD, 2:UPLOADED

    //VARS USED TO UPLOAD FILE TO SERVER
    int mCurrentNdx;
    int mCurrentParent;
    int mCurrentUser;

    int mServerReserved;

    //ASCII ACTION
    public void asciiAction(View view)
    {
        if(!isRecording)
        {
            mAscii.fillTrash();
            mUserReady=true;
        }

    }

    private void initQuestions()
    {
        mQuestionTime=QTIME;
        mQuestion=new String[]
                {
                        "ENJOY FREEDOM",
                        "How old is X now??",
                        "Where is X now??",
                        "What is she doing now??",
                        "What is she feeling??",
                        "What is she thinking??",
                        "What is her biggest challenge??"
                };


        //there are as many parts as questions +1 free part;
        //initially they are not done
        mPartDone=new boolean[mQuestion.length+1];
        for(int i=0;i<mPartDone.length;i++){mPartDone[i]=false;}
    }

    //INIT CAMERA
    private boolean initCamera(int camId)
    {
        boolean result;

        //CHECK IF CAMERA IS INITIALIZED ALREADY
        if(mCamera==null)
        {



            //try to get camera instance
            mCamera=getCameraInstance(camId);//TODO: get real fronfacing camera here or in <-this function
            if(mCamera==null){return false;}
            else
            {
                Log.d("VIDEO_LOG", "got Camera instance");
            }

            //try to get preview
            result = initPreview();
            if(!result){return false;}
            else{Log.d("VIDEO_LOG","got Preview");}

            mTimeLeft=10;


            //COUNT DOWN TIMER BEFORE RECORDING
            new Timer().scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    mTimeLeft--;
                    if(mTimeLeft<=0){this.cancel();}
                    tAct.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            //mRecorderTimeText.setText(""+mTimeLeft);
                            mAscii.modLine("recording will start in "+mTimeLeft+"seconds",0,-1);
                            if(mTimeLeft<=0){forceStartCapture();}//TODO: yes that is target entry point of the crash
                        }
                    });



                }
            },0,1000);




        }
        return true;
    }

    //INIT PREVIEW
    private boolean initPreview()
    {
        mPreview = new Preview(this,mCamera);
        Log.d("RECORDER","preview created "+mPreview.getHolder().getSurface());

        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        SurfaceHolder SH = mPreview.getHolder();
        if(mPreview==null){return false;}

        return true;
    }

    //INIT MEDIA_RECORDER
    private boolean initMediaRecorder()
    {

        Log.d("RECORDER", "creating media recorder ");
        mRecorder = new MediaRecorder();

        Log.d("RECORDER", "unlocking camera ");
        mCamera.unlock();

        Log.d("RECORDER", "setting camera ");
        mRecorder.setCamera(mCamera);

        Log.d("RECORDER", "setting A saource ");
        mRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);

        Log.d("RECORDER", "setting V saource ");
        mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        Log.d("RECORDER", "setting profile ");
        mRecorder.setProfile(CamcorderProfile.get(1, CamcorderProfile.QUALITY_LOW));

        Log.d("RECORDER", "setting outputfile ");
        mRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());

        //Log.d("RECORDER", "SURFACE: " + mPreview.getHolder().getSurface());
        // Create our Preview view and set it as the content of our activity.
        mRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());


        try {
            mRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d("RECORDER", "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseCamera();
            releasePreview();
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d("RECORDER", "IOException preparing MediaRecorder: " + e.getMessage());
            releaseCamera();
            releasePreview();
            releaseMediaRecorder();
            return false;
        }

        return true;
    }
    //____________________________________________

    //RELEASE CAMERA
    private void releaseCamera()
    {

        if (mCamera != null) {
            Log.d("RECORDER", "releasing camera main ");
            mCamera.release();
            Log.d("RECORDER", "camera released ");
            mCamera = null;
        }
    }

    //RELEASE PREVIEW
    private void releasePreview()
    {

        if (mPreview != null) {
            Log.d("RECORDER", "releasing preview ");
            mPreview = null;
            Log.d("RECORDER", "preview released ");

        }
    }

    //RELEASE MEDIA_RECORDER
    private void releaseMediaRecorder()
    {
        if (mRecorder != null) {
            mRecorder.reset();   // clear recorder configuration
            mRecorder.release(); // release the recorder object
            mRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(int camId){
        Camera c = null;
        try {
            c = Camera.open(camId); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    private void startRecordingSequence()
    {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (mMainDone) {
                    this.cancel();
                }
                if (tAct != null) {
                    Log.d("RECORDER", "user ready:" + mUserReady);
                    //IF NOT RECORDING START RECORDING CURRENT PART
                    if (!isRecording && mUserReady && !mMainDone) {

                        mAscii.clear();
                        mUserReady = false;
                        if (initMediaRecorder()) {

                            mRecorder.start();


                            isRecording = true;//Probably can get that from mRecorder..
                        }
                        if (mCurrentPart == 0) {
                            mTimeLeft = FTIME;//FREE TIME
                        }
                        if (mCurrentPart > 0) {
                            mTimeLeft = mQuestionTime;
                        }
                        if (mCurrentPart > mQuestion.length) {
                            Log.d("RECORDER", "finising");
                            finishRecording();
                            this.cancel();
                            isRecording = false;
                        }
                    } else if (isRecording) {
                        Log.d("RECORDER", "recording");
                        tAct.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mPreview.setAlpha(1.0f);
                                // mRecorderTimeText.setText("-" + (mTimeLeft / 60 + ":" + (mTimeLeft % 60)));
                                mAscii.modLine("-" + (mTimeLeft / 60 + ":" + (mTimeLeft % 60)), 0, -1);
                                mAscii.modLine("current part:" + mCurrentPart, 1, -1);
                                if (mCurrentPart >= 0) {
                                    mAscii.modLine("" + mQuestion[mCurrentPart], 3, -1);
                                }
                                if (mTimeLeft <= 0) {
                                    //mCurrentPart++;
                                    mUserReady = false;

                                    forceStopCapture();

                                }
                            }
                        });
                        mTimeLeft--;
                    }
                } else {
                    this.cancel();//TODO: or make destroying sequence if user panics
                }


            }
        }, 0, 1000);
    }

    //FORCE TO START CAPTURING
    public void forceStartCapture()
    {
        mUserReady=true;
        //mCurrentPart++;
        startRecordingSequence();







    }

    //WHEN TIME IS OUT
    public void forceStopCapture()
    {
        Log.d("RECORDER", "vpart:" + mVideoPart[mCurrentPart]);

        Log.d("RECORDER", "current part" + mCurrentPart);

        if (isRecording) {//<-MAYBE UNNECCESARY TODO: check that

            isRecording = false;
            // stop recording and release camera
            mRecorder.stop();  // stop the recording
            releaseMediaRecorder(); // release the MediaRecorder object
            mCamera.lock();         // take camera access back from MediaRecorder
            Log.d("RECORDER", "part:" + mCurrentPart);

            /*if(mCurrentPart==0)
            {
                //mCurrentNdx=mServer.reserveNdx(String.valueOf(mCurrentParent));//DELETE OR MODIFY
            }


            else
            {*/

                if(mCurrentPart<mQuestion.length)
                {


                    mVideoPart[mCurrentPart].populate("", mQuestion[mCurrentPart], fileToUpload);
                    Log.d("RECORDER", "filename:" + mVideoPart[mCurrentPart].getFilePath());
                    Log.d("RECORDER", "quest:" + mVideoPart[mCurrentPart].getQuestion());

                    mAscii.modLine("Question:" + mQuestion[mCurrentPart], 0, -1);
                    mAscii.modLine("current part:" + mCurrentPart, 1, -1);

                    mAscii.modLine("***************", 2, -1);
                    mAscii.modLine("TAP THE SCREEN TO CONTINUE", 3, -1);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            //mServer.uploadPart(mVideoPart[mCurrentPart],mCurrentPart,mServerReserved,mCurrentParent,mCurrentUser);

                        }
                    }).start();
                    Log.d("RECORDER", "CHECK" + mVideoPart[mCurrentPart] + "...CP:" + mCurrentPart);
                    mPartReady[mCurrentPart]=1;
                    mCurrentPart++;//TODO: MAYBE ADD RECORDER NOT READY

                }
                else
                {
                    mMainDone=true;
                    mAscii.modLine("DONE!", 0, -1);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("RECORDER","up part"+mCurrentPart);
                            //mServer.uploadPart(mVideoPart[mCurrentPart - 1], mCurrentPart, mServerReserved, mCurrentParent, mCurrentUser);
                            //mServer.completeNdx(mServerReserved);


                        }
                    }).start();
                    mCurrentPart++;
                }
                mAscii.modLine("***************", 2, -1);
                mAscii.modLine("TAP THE SCREEN TO CONTINUE", 3, -1);


                mPreview.setAlpha(0.0f);
            //}





            //mNextButton.setAlpha(1.0f);
            //mNextButton.setVisibility(View.VISIBLE);





            //forceToSubmit();
        }
    }

    private boolean finishRecording()
    {

            releaseCamera();
            releasePreview();

        mAscii.modLine("DONE!", 0, -1);


        //TODO: do cleanup on device, print some messages to user.
        cleanUp();

        return true;
    }

    /*getOutputFile*/
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.


        //File mediaStorageDir = new File("/mnt/sdcard/", "tmp");
        File mediaStorageDir = new File(mFilePath, "tmp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("tmp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
            fileToUpload = "IMG_"+ timeStamp + ".jpg";
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
            fileToUpload = mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4";
            Log.d("RECORDER","fpath:"+fileToUpload);
            //mediaFile = new File(mediaStorageDir.getPath() + File.separator +
            //      "tmp.mp4");
            //    fileToUpload = "tmp.mp4";
        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorder);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //get pointer to this activity
        tAct=this;

        mVideoPart = new StoryPart[NPARTS];
        mPartReady=new int[NPARTS];
        for(int i=0;i<NPARTS;i++)
        {
            mVideoPart[i]=new StoryPart();
            mPartReady[i]=0;
        }

        //SERVER INIT
        mServer=new Server(this);
        mCurrentUser=-1;
        mCurrentNdx=-1;

        Intent intent = getIntent();
        mCurrentParent = Integer.parseInt(intent.getStringExtra(PlayerActivity.EXTRA_MESSAGE2));
        mMainDone=false;

        new Thread(new Runnable() {
            @Override
            public void run() {
                mServerReserved=mServer.reserveNdx(mCurrentParent);
                boolean done=false;
                int allDone=0;
                while(!done)
                {

                    for(int i=0;i<NPARTS;i++)
                    {
                        if(mPartReady[i]==1)
                        {
                            mServer.uploadPart(mVideoPart[i], mCurrentPart, mServerReserved, mCurrentParent, mCurrentUser);
                            mPartReady[i]=2;
                            Log.d("RECORDER to SERVER", "uploaded part "+i);
                        }
                    }
                    allDone=0;
                    for(int i=0;i<NPARTS;i++)
                    {

                        if(mPartReady[i]==2)
                        {

                            allDone++;

                        }
                    }
                    if(allDone>=NPARTS)
                    {
                        mServer.completeNdx(mServerReserved);
                        done=true;
                        Log.d("RECORDER to SERVER", "all done completing ");
                    }
                }

            }
        }).start();




        initQuestions();

        //ASCII INIT
        mText=(TextView)findViewById(R.id.text_recorder);
        mAscii=new ASCIIscreen(this,mText);
        mAscii.mAsciiStartUpdater(100);


        mFilePath = getExternalFilesDir("VID").getPath();
        //mFilePath = getDir("VID", 0).getPath();
        //mFilePath=getApplicationInfo().dataDir;
        Log.d("RECORDER","filePath:"+mFilePath);

        mCurrentPart=0;
        //INIT CAMERA AND ALL IT DEPENDS ON
        initCamera(1);//for now camId = 1; asuming front facing camera.
        // Will later be fixed to search for frontfacing camera
    }

    //REMEMBER TO TURN OFF THE CAMERA WHEN YOU LEAVE
    @Override
    protected void onPause() {
        super.onPause();
        Log.d("VIDEO_LOG", "PAUSE main ");
        releaseMediaRecorder();
        releaseCamera();
        releasePreview();

    }

    private void cleanUp()
    {
        String deleteCmd = "rm -r " + getExternalFilesDir("VID/tmp").getPath();
        Runtime runtime = Runtime.getRuntime();
        try {
            runtime.exec(deleteCmd);
        } catch (IOException e) { }
    }

    //MAKE SURE IT IS OFF
    @Override
    protected void onDestroy()
    {

        super.onDestroy();

        Log.d("VIDEO_LOG", "DESTROYING main ");
        releaseCamera();
        releasePreview();
        tAct=null;

        cleanUp();
    }

    //TODO:SORT THIS OUT
    @Override
    protected void onRestart()
    {
        super.onRestart();
        this.finish();
        Log.d("VIDEO_LOG", "RESTARTING main ");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_recorder, menu);
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
