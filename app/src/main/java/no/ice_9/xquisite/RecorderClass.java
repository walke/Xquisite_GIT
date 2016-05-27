package no.ice_9.xquisite;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.camera2.CameraManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by human on 25.03.16.
 */
public class RecorderClass extends SubAct{

    class Question
    {
        public String question;
        public int time;

        public Question(String q,int t)
        {
            question=q;
            time=t;
        }


    }

    //ENUMS
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    public static final int FTIME = 10;//90;//60
    public static final int QTIME = 10;//30;//20

    public static final int NPARTS= 3;
    //public static final int TOT_TIME=FTIME+(NPARTS-1)*QTIME;

    Camera mCamera;//Deprecated.. don't know yet what to do about it
    Preview mPreview;
    MediaRecorder mRecorder;

    ASCIIscreen mAscii;
    Server mServer;
    Activity tAct;

    Thread recThread;

    Timer UItimer;
    Timer recTimer;

    boolean isRecording;
    boolean mUserReady;
    boolean mMainDone;
    int mCurrentPart;

    static Question[] mQuestion;
    static int mQuestionTime;
    int mTimeLeft=0;
    int mTimeElapsed;
    int mTimeElapsedPq=0;

    boolean mWorking;
    StoryPart[] mVideoPart;
    int[] mPartReady;//0:NOT READY, 1:READY TO UPLOAD, 2:UPLOADED

    //VARS USED TO UPLOAD FILE TO SERVER
    int mCurrentNdx;
    int mCurrentParent;
    int mCurrentUser;

    int mServerReserved;
    int mPartOffset;

    static String fileToUpload;
    static String mFilePath;

    int mTime=0;

    private boolean[] mPartDone;

    public RecorderClass(Activity activity,ASCIIscreen ascii,Server server,int parent, int reserved, int offset)
    {
        mAscii=ascii;
        mServer=server;
        tAct=activity;


        mWorking=true;
        mVideoPart = new StoryPart[NPARTS];
        mPartReady=new int[NPARTS];
        for(int i=0;i<NPARTS;i++)
        {
            mVideoPart[i]=new StoryPart();
            mPartReady[i]=0;
        }

        //SERVER INIT
        //mServer=new Server(tAct);
        mCurrentUser=-1;
        mCurrentNdx=-1;

        mCurrentParent=parent;
        //Intent intent = getIntent();
        //mCurrentParent = Integer.parseInt(intent.getStringExtra(PlayerActivity.EXTRA_MESSAGE2));
        mMainDone=false;
        mServerReserved=reserved;
        mPartOffset=offset;
        recThread=new Thread(new Runnable() {
            @Override
            public void run() {
                if(mServerReserved<=0)
                {
                    mServerReserved = mServer.reserveNdx(mCurrentParent);
                }
                boolean done=false;
                int allDone=0;
                while(!done)
                {

                    for(int i=0;i<NPARTS;i++)
                    {
                        if(mPartReady[i]==1)
                        {
                            mServer.uploadPart(mVideoPart[i], mCurrentPart+mPartOffset, mServerReserved, mCurrentParent, mCurrentUser);
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
        });

        recThread.start();


        initQuestions();

        //ASCII INIT
        //mText=(TextView)findViewById(R.id.text_recorder);
        //mAscii=new ASCIIscreen(this,mText,"RECORDER");
        mAscii.mAsciiStartUpdater(100);
        mAscii.clear();
        //setContentView(mAscii.mGLView);


        mFilePath = tAct.getExternalFilesDir("VID").getPath();
        //mFilePath = getDir("VID", 0).getPath();
        //mFilePath=getApplicationInfo().dataDir;
        Log.d("RECORDER","filePath:"+mFilePath);

        mCurrentPart=0;
        //INIT CAMERA AND ALL IT DEPENDS ON
        initCamera(1);//for now camId = 1; asuming front facing camera.
        // TODO: Will later be fixed to search for frontfacing camera
    }


    @Override
    public int[] action(int act)
    {
        int[] result=new int[1];
        result[0]=-1;
        if(!isRecording)
        {
            mAscii.fillTrash();
            mUserReady=true;
        }
        if(mTime==1)
        {
            mTime++;

        }
        if(mTime==2)
        {
            mTime++;
            mTimeLeft=0;
        }
        if(mMainDone)
        {
            finishRecording();
            result[0]=1;

        }

        return result;
    }

    @Override
    public TimerTask getTimerTask()
    {
        return new TimerTask() {
            @Override
            public void run() {

                //Log.d("PLAYER","::"+mVideoView.isActivated()+","+mVideoView);
                if(mAscii.mReady)
                {
                    if(mTime==0)
                    {
                        mTime++;
                    }
                    if(mTime==1)
                    {
                        mTime++;

                    }
                    if(mTime>2 && isRecording)
                    {
                        mAscii.mGLView.mRenderer.setProgress((float) (mTimeElapsedPq+1) / (float) mQuestion[mCurrentPart].time,1);
                        mAscii.mGLView.mRenderer.setRecording(true);
                    }

                }

            }
        };
    }

    private void initQuestions()
    {
        mQuestionTime=QTIME;
        /*mQuestion=new String[]
                {
                        "ENJOY FREEDOM",
                        "SOMEBODY IS WATCHING YOU!"//,
                        // "Where is X now??",
                        // "What is she doing now??",
                        //"What is she feeling??",
                        //"What is she thinking??",
                        // "What is her biggest challenge??"
                };
        mAscii.minimizeInfo();*/

        mQuestion=new Question[NPARTS];
        mQuestion[0]=new Question("What are the key words from what you just heard? " ,10);
        mQuestion[1]=new Question("Think of what X might do next. Put yourself in her shoes, and challenge yourself to be dramatic. ",120);
        mQuestion[2]=new Question("You now have one more minute to add to your story, or summarise for the next player. ",60);

        /*for (int i=0;i<mQuestion.length;i++)
        {
            mTotalTime+=mQuestion[i].time;
        }*/


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
            //mCamera= (CameraManager) tAct.getSystemService(Context.CAMERA_SERVICE);
            if(mCamera==null)
            {
                //mCamera=getCameraInstance(0);

            }

            if(mCamera==null){return false;}
            else
            {
                Log.d("RECORDER", "got Camera instance");
            }

            //try to get preview
            //result = initPreview();
            result=true;
            try{
                mCamera.setPreviewTexture(mAscii.mGLView.mRenderer.mSurface);
            }catch (IOException ioe)
            {
                Log.d("REC","ERROR SETTING TEXTURE");
                result=false;
            }

            if(!result){return false;}
            else{Log.d("RECORDER","got Preview");}
            mCamera.startPreview();
            mTimeLeft=10;


            //COUNT DOWN TIMER BEFORE RECORDING
            new Timer().scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    Log.d("RECORDER", "TIMER: cdown" + mWorking + " " + mTimeLeft);
                    //mTimeLeft--;
                    if (mTimeLeft <= 0 || !mWorking) {
                        this.cancel();
                    }
                    if (tAct != null) {
                        tAct.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                //mRecorderTimeText.setText(""+mTimeLeft);
                                mAscii.modLine("" + mQuestion[mCurrentPart].question, 0, -1);
                                //mAscii.modLine("current part:" + mCurrentPart, 1, -1);
                                //mAscii.modLine("recording time: " + mQuestion[mCurrentPart].time + " seconds", 1, -1);
                                //mAscii.modLine("PUSH BUTTON TO RECORD ("+mQuestion[mCurrentPart].time+" sec)", 3, -1);

                                mAscii.modLine("", 1, -1);
                                mAscii.modLine("***************", 2, -1);
                                mAscii.modLine("PUSH BUTTON TO RECORD ("+mQuestion[mCurrentPart].time+" sec)", 3, -1);
                                mAscii.modLine("", 4, 0);
                                //mAscii.modLine("***************", 2, -1);
                                //mAscii.modLine("", 4, 0);
                                //mAscii.modLine("PUSH THE BUTTON TO CONTINUE", 3, -1);

                                //mAscii.modLine("recording will start in " + mTimeLeft + "seconds", 0, -1);
                                if (mTimeLeft <= 0) {
                                    mTimeElapsed=0;
                                    mTimeElapsedPq=0;
                                    forceStartCapture();
                                }//TODO: yes that is target entry point of the crash
                            }
                        });
                    }

                    Log.d("RECORDER", "TIMER: cdown end" + mWorking);


                }
            }, 0, 1000);




        }
        return true;
    }

    /** A safe way to get an instance of the Camera object. */
    private static Camera getCameraInstance(int camId){
        Camera c = null;
        try {
            c = Camera.open(camId); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    //INIT PREVIEW
    private boolean initPreview()
    {
        mPreview = new Preview(tAct,mCamera);

        //Log.d("RECORDER","preview created "+mPreview.getHolder().getSurface());

        //TODO: FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        //preview.addView(mPreview);
        //SurfaceHolder SH = mPreview.getHolder();
        if(mPreview==null){return false;}

        return true;
    }

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
    private void releasePreview() {

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

    //FORCE TO START CAPTURING
    private void forceStartCapture()
    {
        mUserReady=true;
        //mCurrentPart++;
        startRecordingSequence();







    }

    private void startRecordingSequence()
    {
        UItimer=new Timer();
        UItimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (isRecording) {
                    tAct.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mRecorder != null && mAscii.mReady) {
                                mAscii.mGLView.mRenderer.setAudio(mRecorder.getMaxAmplitude());
                            }
                        }
                    });

                }


            }
        }, 0, 50);
        recTimer=new Timer();
        recTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Log.d("RECORDER", "TIMER: rec");
                if (mMainDone && mWorking) {
                    Log.d("RECORDER", "TIMER: exiting");
                    this.cancel();

                } else if (tAct != null && mWorking) {
                    Log.d("RECORDER", "user ready:" + mUserReady);
                    //IF NOT RECORDING START RECORDING CURRENT PART
                    if (!isRecording && mUserReady && !mMainDone) {

                        //mAscii.clear();
                        mUserReady = false;
                        if (initMediaRecorder()) {

                            mRecorder.start();


                            isRecording = true;//Probably can get that from mRecorder..
                        }
                        mTimeLeft = mQuestion[mCurrentPart].time;

                        /*if (mCurrentPart == 0) {
                            mTimeLeft = FTIME;//FREE TIME
                        }
                        if (mCurrentPart > 0) {
                            mTimeLeft = mQuestionTime;
                        }*/
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
                                //mPreview.setAlpha(1.0f);
                                // mRecorderTimeText.setText("-" + (mTimeLeft / 60 + ":" + (mTimeLeft % 60)));
                                mAscii.modLine(mQuestion[mCurrentPart].question, 0, -1);
                                //mAscii.modLine("RECORDING", 1, -1);
                                mAscii.modLine("-" + (mTimeLeft / 60 + ":" + (mTimeLeft % 60)), 3, -1);

                                //mAscii.modLine("current part:" + mCurrentPart, 1, -1);
                                if (mCurrentPart >= 0) {
                                    //TODO:mAscii.modLine("" + mQuestion[mCurrentPart], 3, -1);
                                }
                                if (mTimeLeft <= 0) {
                                    //mCurrentPart++;
                                    mUserReady = false;

                                    forceStopCapture();

                                }
                            }
                        });
                        mTimeLeft--;
                        mTimeElapsed++;
                        mTimeElapsedPq++;
                    }
                } else {
                    this.cancel();//TODO: or make destroying sequence if user panics
                }


            }
        }, 0, 1000);
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
        mRecorder.setProfile(CamcorderProfile.get(1, CamcorderProfile.QUALITY_HIGH));
        mRecorder.setVideoEncodingBitRate(69000);

        Log.d("RECORDER", "setting outputfile ");
        mRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());

        mRecorder.setOrientationHint(270);

        //Log.d("RECORDER", "SURFACE: " + mPreview.getHolder().getSurface());
        // Create our Preview view and set it as the content of our activity.
        //mRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());
        mRecorder.setPreviewDisplay(new Surface(mAscii.mGLView.mRenderer.mSurface));

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

    //WHEN TIME IS OUT
    private void forceStopCapture()
    {
        Log.d("RECORDER", "vpart:" + mVideoPart[mCurrentPart]);

        Log.d("RECORDER", "current part" + mCurrentPart);

        if (isRecording) {//<-MAYBE UNNECCESARY TODO: check that

            mAscii.mGLView.mRenderer.setProgress(0.0f,1);
            mAscii.mGLView.mRenderer.setRecording(false);
            mAscii.mGLView.mRenderer.setAudio(0);
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


                mVideoPart[mCurrentPart].populate("", mQuestion[mCurrentPart].question, fileToUpload);
                Log.d("RECORDER", "filename:" + mVideoPart[mCurrentPart].getFilePath());
                Log.d("RECORDER", "quest:" + mVideoPart[mCurrentPart].getQuestion());

                if((mCurrentPart+1)<mQuestion.length)
                {
                    mAscii.modLine("" + mQuestion[mCurrentPart+1].question, 0, -1);
                    //mAscii.modLine("recording time: " + mQuestion[mCurrentPart+1].time + " seconds", 1, -1);
                    //mAscii.modLine("current part:" + (mCurrentPart+1), 1, -1);

                   /* mAscii.modLine("***************", 2, -1);
                    mAscii.modLine("", 4, 0);
                    mAscii.modLine("TAP THE SCREEN TO CONTINUE", 3, -1);*/

                    mAscii.modLine("***************", 2, -1);
                    mAscii.modLine("", 4, 0);
                    mAscii.modLine("PUSH BUTTON TO RECORD ("+mQuestion[mCurrentPart].time+" sec)", 3, -1);

                }

                /*new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //mServer.uploadPart(mVideoPart[mCurrentPart],mCurrentPart,mServerReserved,mCurrentParent,mCurrentUser);

                    }
                }).start();*/
                Log.d("RECORDER", "CHECK" + mVideoPart[mCurrentPart] + "...CP:" + mCurrentPart);
                mPartReady[mCurrentPart]=1;
                Log.d("RECORDER", "CHECK" + mPartReady[mCurrentPart]);
                mCurrentPart++;//TODO: MAYBE ADD RECORDER NOT READY
                mTimeElapsedPq=0;

            }
            if(mCurrentPart>=mQuestion.length)
            {
                mMainDone=true;
                mAscii.modLine("Thank you!", 0, -1);
                mAscii.modLine("", 1, -1);
                mAscii.modLine("", 2, -1);
                mAscii.modLine("", 3, -1);

                /*new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("RECORDER","up part"+mCurrentPart);
                        //mServer.uploadPart(mVideoPart[mCurrentPart - 1], mCurrentPart, mServerReserved, mCurrentParent, mCurrentUser);
                        //mServer.completeNdx(mServerReserved);


                    }
                }).start();*/
                mCurrentPart++;
            }
            mAscii.modLine("***************", 2, -1);
            if(mCurrentPart<mQuestion.length)mAscii.modLine("PUSH BUTTON TO RECORD ("+mQuestion[mCurrentPart].time+" sec)", 3, -1);
            else{mAscii.modLine("PUSH BUTTON FINISH ", 3, -1);}
            //mAscii.modLine("PUSH THE BUTTON TO CONTINUE", 3, -1);


            //mPreview.setAlpha(0.0f);
            //}





            //mNextButton.setAlpha(1.0f);
            //mNextButton.setVisibility(View.VISIBLE);





            //forceToSubmit();
        }
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

    private boolean finishRecording()
    {

        releaseCamera();
        releasePreview();

        mAscii.modLine("DONE!", 0, -1);


        //TODO: do cleanup on device, print some messages to user.
        //finish();

        return true;
    }

    @Override
    public void destroy() {
        super.destroy();

        forceStopCapture();
        if(UItimer!=null)
        {
            UItimer.cancel();
            UItimer.purge();
        }
        if(recTimer!=null)
        {
            recTimer.cancel();
            recTimer.purge();
        }

        if(recThread!=null)recThread.interrupt();

        if(mRecorder!=null)mRecorder.release();
        mCamera.release();


    }
}
