package no.ice_9.xquisite;

import android.app.Activity;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.Surface;


import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channel;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by human on 27.05.16.
 *
 * Recording subactivity
 * used to record video and audio in parts and store them in a chain/tree database with direct relation to other stories
 * TODO:fix mp4parser parts are not being combined properly
 */
public class RecorderBase extends SubAct{

    //ENUMS
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    int NPARTS= 0;

    ASCIIscreen mAscii;
    Server mServer;
    Activity tAct;

    Camera mCamera;//Deprecated.. don't know yet what to do about it
    Preview mPreview;
    MediaRecorder mRecorder;

    Thread recThread;

    Timer UItimer;
    Timer recTimer;

    int mCurrentParent=-2;
    int mParentStoryParts;
    int mServerReserved;

    int mTimeLeft=0;
    int mTimeElapsed;
    int mTimeElapsedPq=0;

    boolean lastRecorder=false;

    boolean mPauseRequest=false;


    boolean mWorking;
    StoryPart[] mVideoPart;
    int[] mPartReady;//0:NOT READY, 1:READY TO UPLOAD, 2:UPLOADED


    boolean isRecording;
    boolean mMainDone;
    int mCurrentUser;

    int mTotalTime=0;

    String mEndMessege="";

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

    class TmpPart
    {
        public TmpPart()
        {
            filearr=new String[0];
        }
        String[] filearr;
        public void newSubPart(String fname)
        {
            String[] tmp=filearr;
            filearr=new String[tmp.length+1];
            for(int i=0;i<tmp.length;i++)
            {
                filearr[i]=tmp[i];

            }

            filearr[tmp.length]=fname;
        }
        public void clear()
        {
            filearr=new String[0];
        }
    }
    TmpPart mTmpPart;

    boolean mUserReady;

    int mCurrentPart=0;
    int mCurrentSubPart=0;
    static Question[] mQuestion;
    boolean[] mPartDone;

    int mTime=0;

    static String fileToUpload;
    static String mFilePath;

    @Override
    public int[] action(int act)
    {
        int[] result=new int[4];
        result[0]=-1;
        Log.d("RECORDER","ACT"+act);
        switch(act)
        {
            case 0:

                //PAUSE
                if(isRecording)
                {
                    mUserReady = false;

                    pauseRecording();

                }
                else {mPauseRequest=true;}
                break;

            case 1:
                //NEXT PART
                if(mCurrentSubPart>0)
                {
                    mPauseRequest=false;
                    nextPart();

                }
                break;

            case 2:

                //RECORD
                if (!isRecording) {
                    mPauseRequest=false;
                    mUserReady = true;
                    result[0] = -1;
                }
                if (mTime == 1) {
                    mTime++;
                }
                if (mTime == 3) {
                    Log.d("RECORDER","mtiome");
                    mTime++;
                    mTimeLeft = 0;
                }
                break;
        }

        if (mCurrentPart >= mQuestion.length && act==3)//if(mMainDone)
        {
            finishRecording();

            mAscii.clear();//?
            result[0] = mCurrentParent;
            result[1] = mParentStoryParts;
            result[2] = mServerReserved;
            result[3] = mCurrentPart;
            //return result;
        }

        return result;
    }

    @Override
    public TimerTask getTimerTask()
    {
        return new TimerTask() {
            @Override
            public void run() {
                if(mAscii.mReady)
                {
                    if(mTime==0)
                    {
                        mAscii.mGLView.mRenderer.setRecSequence(true);
                        mAscii.clear();

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


    @Override
    public void destroy() {

    }

    public boolean init()
    {
        mWorking=true;
        mVideoPart = new StoryPart[NPARTS];
        mPartReady=new int[NPARTS];

        mTmpPart=new TmpPart();

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

        Log.d("RECORDER","parts:"+NPARTS);
        for(int i=0;i<NPARTS;i++)
        {
            mVideoPart[i]=new StoryPart();
            mPartReady[i]=0;
        }

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
                        if(lastRecorder)mServer.completeNdx(mServerReserved);
                        done=true;
                        Log.d("RECORDER to SERVER", "all done completing ");

                    }
                }

            }
        });

        recThread.start();

        mAscii.mAsciiStartUpdater(100);
        mAscii.clear();

        mFilePath = tAct.getExternalFilesDir("VID").getPath();

        mCurrentPart=0;
        //INIT CAMERA AND ALL IT DEPENDS ON
        initCamera(1);//for now camId = 1; asuming front facing camera.

        return true;
    }

    public boolean finishRecording()
    {
        releaseCamera();
        releasePreview();

        mAscii.modLine("DONE!", 0, -1);
        return true;
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
                //parm.setPreviewSize(1280, 720);
                //parm.setVideoStabilization(true);

                //parm.setAutoExposureLock(true);
                //mCamera.setParameters(parm);

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
                    Log.d("RECORDER","mtime"+mTime);
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

    //FORCE TO START CAPTURING
    private void forceStartCapture()
    {
        mUserReady=true;
        //mCurrentPart++;
        startRecordingSequence();
        Camera.Parameters parm = mCamera.getParameters();
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
                //Log.d("RECORDER", "TIMER: rec");
                if (mMainDone && mWorking) {
                    Log.d("RECORDER", "TIMER: exiting");
                    this.cancel();

                } else if (tAct != null && mWorking) {
                    //Log.d("RECORDER", "user ready:" + mUserReady);
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
                                //mAscii.modLine("-" + mTmpPart.filearr.length+":"+mCurrentSubPart, 3, -1);

                                //mAscii.modLine("current part:" + mCurrentPart, 1, -1);
                                if (mCurrentPart >= 0) {
                                    //TODO:mAscii.modLine("" + mQuestion[mCurrentPart], 3, -1);
                                }
                                if (mTimeLeft <= 0) {
                                    //mCurrentPart++;
                                    //mUserReady = false;

                                    //forceStopCapture();

                                }
                            }
                        });

                        mTimeLeft--;
                        mTimeElapsed++;
                        mTimeElapsedPq++;
                    }
                    if(mPauseRequest){pauseRecording();}
                } else {
                    this.cancel();//TODO: or make destroying sequence if user panics
                }


            }
        }, 0, 1000);
    }

    //INIT MEDIA_RECORDER
    private boolean initMediaRecorder() {

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
        mRecorder.setOutputFile(getSubPartFile(MEDIA_TYPE_VIDEO).toString());

        mRecorder.setOrientationHint(270);



        //mRecorder.setVideoSize(1280, 720);


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

    private void pauseRecording()
    {


        try {
            isRecording = false;
            mRecorder.stop();  // stop the recording

            releaseMediaRecorder(); // release the MediaRecorder object
            mCamera.lock();         // take camera access back from MediaRecorder
            mCurrentSubPart++;
            mAscii.mGLView.mRenderer.setRecording(false);
            mPauseRequest=false;

        }catch(Exception e){mPauseRequest=true;}

    }

    private void nextPart()
    {
        mAscii.mGLView.mRenderer.setProgress(0.0f,1);
        mAscii.mGLView.mRenderer.setRecording(false);
        mAscii.mGLView.mRenderer.setAudio(0);

        if((mCurrentPart)<mQuestion.length)
        {

            //TODO: if one subPart copy it to VID folder
            File f=getOutputMediaFile(MEDIA_TYPE_VIDEO);


            fileToUpload=f.toString();
            OutputStream os = null;
            try {

                Movie[] _clips=new Movie[mTmpPart.filearr.length];
                for(int i=0; i<mTmpPart.filearr.length;i++){

                    //Movie tm=MovieCreator.
                    Log.d("RECORDER","trying!!!");
                    Movie tm = MovieCreator.build(mTmpPart.filearr[i]);

                    _clips[i]=tm;

                    Log.d("RECORDER","TM:"+tm.toString());

                }

                List<Track> videoTracks = new LinkedList<Track>();
                List<Track> audioTracks = new LinkedList<Track>();

                for (Movie m : _clips) {
                    for (Track t : m.getTracks()) {
                        if (t.getHandler().equals("soun")) {

                            audioTracks.add(t);
                        }
                        if (t.getHandler().equals("vide")) {
                            videoTracks.add(t);
                        }
                    }
                }

                Movie result = new Movie();
                Track[] vidtrarr=new Track[videoTracks.size()];
                Track[] audtrarr=new Track[videoTracks.size()];

                for(int i=0;i<videoTracks.size();i++)
                {
                    vidtrarr[i]=videoTracks.get(i);
                    //result.addTrack(videoTracks.get(i));

                    //Log.d("RECORDER","durtrV "+i+"-"+vidtrarr[i].getDuration());
                }
                for(int i=0;i<audioTracks.size();i++)
                {
                    audtrarr[i]=audioTracks.get(i);
                    //result.addTrack(audioTracks.get(i));
                    //Log.d("RECORDER","durtrA "+i+"-"+vidtrarr[i].getDuration());
                }


                Track vidTot=new AppendTrack(vidtrarr);
                Track audTot=new AppendTrack(audtrarr);
                //Log.d("RECORDER","durtrV "+"-"+vidTot.getDuration());
                //Log.d("RECORDER","durtrA "+"-"+audTot.getDuration());

                result.addTrack(vidTot);
                result.addTrack(audTot);


                /*if (videoTracks.size() > 0) {
                    result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));
                }
                if (audioTracks.size() > 0) {
                    result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
                }*/

                Log.d("RECORDER","result"+result.toString());

                Container out = new DefaultMp4Builder().build(result);
                //IsoFile out = new DefaultMp4Builder().build(result);

                FileChannel fc = new RandomAccessFile(String.format(f.toString()), "rw").getChannel();
                //FileChannel fc2 =new FileOutputStream(f).getChannel();
                out.writeContainer(fc);

                fc.close();

                if (os != null) {
                    os.close();
                }
            }catch (Exception io){Log.e("RECORDER","err:" +io.getMessage());}

            mTmpPart.clear();






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
                //mAscii.modLine("PUSH BUTTON TO RECORD ("+mQuestion[mCurrentPart].time+" sec)", 3, -1);

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

            mCurrentSubPart = 0;
            mTimeElapsedPq=0;

        }
        if((mCurrentPart)>=mQuestion.length)
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
        if(mCurrentPart<mQuestion.length){}//mAscii.modLine("PUSH BUTTON TO RECORD ("+mQuestion[mCurrentPart].time+" sec)", 3, -1);
        else
        {
            mAscii.mGLView.mRenderer.setRecSequence(false);
            //mAscii.modLine("Thanks. Get ready to play Xquisite! The year is 2062. Our main character X is 17 years old",0,0);
            mAscii.modLine(mEndMessege,0,0);
            mAscii.modLine("",1,0);
            mAscii.modLine("PUSH BUTTON TO CONTINUE",3,0);
        }
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
                    //mAscii.modLine("PUSH BUTTON TO RECORD ("+mQuestion[mCurrentPart].time+" sec)", 3, -1);

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

    private File getSubPartFile(int type)
    {
        File mediaStorageDir = new File(mFilePath, "sub");

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("RECORDER", "failed to create directory");
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
                    "SUB_"+ mCurrentSubPart + ".mp4");
            String fname = mediaStorageDir.getPath() + File.separator +
                    "SUB_"+ mCurrentSubPart + ".mp4";
            mTmpPart.newSubPart(fname);
            Log.d("RECORDER","fpath:"+fname);
            //mediaFile = new File(mediaStorageDir.getPath() + File.separator +
            //      "tmp.mp4");
            //    fileToUpload = "tmp.mp4";
        } else {
            return null;
        }

        return mediaFile;
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

}
