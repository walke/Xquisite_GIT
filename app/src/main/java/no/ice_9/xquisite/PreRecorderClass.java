package no.ice_9.xquisite;

import android.app.Activity;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by human on 04.04.16.
 *
 * TODO: To be removed, was replaced by Interview class
 */
public class PreRecorderClass extends SubAct{

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

    public static final int NPARTS= 12;


    Camera mCamera;//Deprecated.. don't know yet what to do about it
    Preview mPreview;
    MediaRecorder mRecorder;

    ASCIIscreen mAscii;
    Server mServer;
    Activity tAct;

    Thread recThread;

    Timer UItimer;
    Timer recTimer;

    int mTotalTime=0;

    boolean isRecording;
    boolean mUserReady;
    boolean mMainDone;
    int mCurrentPart;
    int mCurrentSubPart;

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
    int mCurrentParent=-2;
    int mCurrentUser;
    int mParentStoryParts;

    int mServerReserved;

    static String fileToUpload;
    static String mFilePath;

    int mTime=0;

    private boolean[] mPartDone;

    public PreRecorderClass(Activity activity,ASCIIscreen ascii,Server server)
    {
        mAscii=ascii;
        mServer=server;
        tAct=activity;


        mWorking=true;
        mVideoPart = new StoryPart[NPARTS];
        mPartReady=new int[NPARTS];

        mCurrentParent=-2;
        Thread mTask = new Thread(new Runnable() {
            @Override
            public void run() {

                //Looper.prepare();
                int res[] = mServer.getLastStoryNdx();
                int storyindx = res[0];
                int storyParts= res[1];
                mCurrentParent=storyindx;
                mParentStoryParts=storyParts;

            }
        });

        mTask.start();



        for(int i=0;i<NPARTS;i++)
        {
            mVideoPart[i]=new StoryPart();
            mPartReady[i]=0;
        }

        //SERVER INIT
        //mServer=new Server(tAct);
        mCurrentUser=-1;
        mCurrentNdx=-1;




        //Intent intent = getIntent();
        //mCurrentParent = Integer.parseInt(intent.getStringExtra(PlayerActivity.EXTRA_MESSAGE2));
        mMainDone=false;

        recThread=new Thread(new Runnable() {
            @Override
            public void run() {
                while(mCurrentParent==-2);
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
                            Log.d("RECORDER to SERVER", "uploaded part " + i);
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
                        //mServer.completeNdx(mServerReserved);
                        done=true;
                        Log.d("RECORDER to SERVER", "all done completing ");

                    }
                }

            }
        });

        recThread.start();


        initQuestions();

        //ASCII INIT
        //mText=(TextView)findViewById(R.id.text_remTimeElapsedPqcorder);
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

        int[] result=new int[4];
        result[0]=-1;
        if(act==0)
        {
            if (isRecording)
            {
                mUserReady = false;
                forceStopCapture();
                mCurrentSubPart++;
                //PAUSE
            }
        }
        else if(act==1)
        {
            //NEXT PART
            if (!isRecording) {
                mAscii.fillTrash();//?
                mUserReady = true;
                result[0] = -1;
            }
            if (mTime == 1) {
                mTime++;
            }
            if (mTime == 3) {
                mTime++;
                mTimeLeft = 0;
            }
            if (mCurrentPart >= mQuestion.length)//if(mMainDone)
            {
                finishRecording();
                mAscii.clear();//?
                result[0] = mCurrentParent;
                result[1] = mParentStoryParts;
                result[2] = mServerReserved;
                result[3] = mCurrentPart;
                //return result;
            }
        }
        else if(act==2)
        {
            //RECORD
            if (!isRecording) {
                mAscii.fillTrash();//?
                mUserReady = true;
                result[0] = -1;

                if (mTime == 1) {
                    mTime++;
                }
                if (mTime == 3) {
                    mTime++;
                    mTimeLeft = 0;
                }
            }

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
                        mAscii.clear();
                        //mAscii.maximizeInfo();
                        //mAscii.pushLine("loading video..");
/*
                        mAscii.pushLine("");
                        mAscii.pushLine("Xquisite is a story game that is part of scienceFUTURE,");
                        mAscii.pushLine("a project where scientists, artists, and young people will collectively");
                        mAscii.pushLine("imagine the many potential dramas in the life of one woman");
                        mAscii.pushLine("born in 2045 (29 years in the future).");
                        mAscii.pushLine("");

                        mAscii.pushLine("scienceFUTURE is a space to dream freely about what life might be like in the future.");
                        mAscii.pushLine("We encourage you to be funny, provocative, and dramatic.");
                        mAscii.pushLine("Be wild. The more you fun it is for you, the more fun it is for everyone.");
                        mAscii.pushLine("(Xquisite is a fiction game, and your contribution comes from you as a private person,");
                        mAscii.pushLine("and is not a professional statement or prediction.)");
                        mAscii.pushLine("");

                        mAscii.pushLine("Xquisite takes roughly 5 minutes to play.");
                        mAscii.pushLine("Before you play, we'd like to do a 3-minute interview which helps us develop the project further.");
                        mAscii.pushLine("The camera will record your answers.");
                        mAscii.pushLine("Try and center your face in the window,");
                        mAscii.pushLine("and speak directly into the device.");
                        mAscii.pushLine("");
                        mAscii.pushLine("PUSH THE BUTTON TO START");*/
                        mTime++;




                    }

                    if(mTime==1)
                    {
                        mAscii.modLine("",2,0);
                        mAscii.modLine("",1,0);
                        mAscii.modLine("",0,0);
                        mTime++;

                    }

                    if(mTime==2)
                    {
                        mAscii.minimizeInfo();
                        mAscii.modLine("", 2, 0);
                        mAscii.modLine("",1,0);

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

        mQuestion=new Question[NPARTS];
        mQuestion[0]=new Question("What is your name?" ,8);
        mQuestion[1]=new Question("What is your email? (spell if needed)",12);
        mQuestion[2]=new Question("Are you happy to be credited for your contribution? (answer yes or no)",3);
        mQuestion[3]=new Question("Are you willing to engage in online or live dialogue with young people and artists? (answer yes or no)",3);
        mQuestion[4]=new Question("Where did you grow up?",5);
        mQuestion[5]=new Question("Would your close friends call you an optimist or pessimist about the future?",5);
        mQuestion[6]=new Question("What is your favorite science fiction story?",12);
        mQuestion[7]=new Question("Briefly describe your work and your research themes.",20);
        mQuestion[8]=new Question("What is the most exciting element of your research? ",12);
        mQuestion[9]=new Question("What is the most challenging element of your work?",12);
        mQuestion[10]=new Question("What are the most important environmental changes likely to happen in your field in the next 100 years?",20);
        mQuestion[11]=new Question("How might this change affect human lives?",20);

        for (int i=0;i<mQuestion.length;i++)
        {
            mTotalTime+=mQuestion[i].time;
        }






        /*mQuestion=new String[]
                {
                        "ENJOY FREEDOM",
                        "SOMEBODY IS WATCHING YOU!"//,
                        // "Where is X now??",
                        // "What is she doing now??",
                        //"What is she feeling??",
                        //"What is she thinking??",
                        // "What is her biggest challenge??"
                };*/


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
            else
            {
                Log.d("RECORDER", "got Preview1");
                Camera.Parameters parm=mCamera.getParameters();
                Log.d("RECORDER", "PARM:" + parm.getPreviewSize().height + "," + parm.getPreviewSize().width);
                parm.setPreviewSize(1280, 720);
                parm.setVideoStabilization(true);

                //parm.setAutoExposureLock(true);
                mCamera.setParameters(parm);

            }

            //mCamera.setParameters(new Camera.Parameters());
            mCamera.startPreview();
            mTimeLeft=10;


            //COUNT DOWN TIMER BEFORE RECORDING
            new Timer().scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    Log.d("RECORDER", "TIMER: cdown" + mWorking + " " + mTimeLeft);
                    //if(mTime>2)mTimeLeft--;
                    if (mTimeLeft <= 0 || !mWorking) {
                        this.cancel();
                    }
                    if (tAct != null && mTime>2) {
                        tAct.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                //mRecorderTimeText.setText(""+mTimeLeft);
                                mAscii.modLine("" + mQuestion[mCurrentPart].question, 0, -1);
                                //mAscii.modLine("current part:" + mCurrentPart, 1, -1);
                                //mAscii.modLine("recording time: " + mQuestion[mCurrentPart].time + " seconds", 1, -1);

                                mAscii.modLine("***************", 2, -1);
                                mAscii.modLine("PUSH BUTTON TO RECORD ("+mQuestion[mCurrentPart].time+" sec)", 3, -1);
                                mAscii.modLine("", 4, 0);
                                //mAscii.modLine("recording will start in " + mTimeLeft + "seconds", 0, -1);
                                if (mTimeLeft <= 0) {
                                    mTimeElapsed=0;
                                    mTimeElapsedPq=0;
                                    //forceStopCapture();
                                    forceStartCapture();
                                }
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
        Camera.Parameters parm=mCamera.getParameters();
        Log.d("RECORDER", "PARM:" + parm.getPreviewSize().height + "," + parm.getPreviewSize().width);


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
                        //forceStopCapture();
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
        //mRecorder.setProfile(CamcorderProfile.get(1, 1));
        mRecorder.setVideoEncodingBitRate(69000);

        Log.d("RECORDER", "setting outputfile ");
        mRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());

        mRecorder.setOrientationHint(270);



        mRecorder.setVideoSize(1280, 720);


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
                //mAscii.modLine("Thanks! The interview is finished.", 0, -1);

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
            mAscii.modLine("",1 , -1);
            mAscii.modLine("***************", 2, -1);
            //mAscii.modLine("PUSH THE BUTTON TO CONTINUE", 3, -1);
            if(mCurrentPart<mQuestion.length)mAscii.modLine("PUSH BUTTON TO RECORD ("+mQuestion[mCurrentPart].time+" sec)", 3, -1);
            else
            {
                mAscii.modLine("Thanks. Get ready to play Xquisite! The year is 2062. Our main character X is 17 years old",0,0);
                mAscii.modLine("",1,0);
                mAscii.modLine("PUSH BUTTON TO CONTINUE",3,0);
            }

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
        //super.destroy();
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
