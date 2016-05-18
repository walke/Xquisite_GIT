package no.ice_9.xquisite;

import android.app.Activity;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.IBinder;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.util.TypedValue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends Activity {

    DeviceData mDeviceData;


    Data appData;

    SubAct currentSubActivity;


    //CURRENT ACTION
    private int mCurrentAction=0;

    //ACII LAYER
    public ASCIIscreen mAscii;


    //MAIN LOOP
    public TimerTask mTimerLoop;
    public Timer mTimer;
    public TimerTask mConnectionThread;



    //SERVER
    private Server mServer;
    private boolean mSync=false;

    public int mParent=-1;
    public int mParentParts=-1;
    public int mReservedStory=-1;
    public int mPartOffset=-1;


    boolean mUserWait=true;
   boolean interSkip=false;


    public void diaBut(boolean skip)
    {
        mUserWait=false;
        interSkip=skip;
        glTouch();

    }

    public void glTouch()
    {
        Log.d("GL","TOUCH");
        //CreateNewStory();
        int[] result;
        int res=-1;
        if(mUserWait)
        {
            //mCurrentAction=-2;
            DialogFragment dg= new InterviewSkip();
            dg.show(getFragmentManager(),"Skip Interview");

            return;
            //while(mUserWait);
         }
        switch(mCurrentAction)
        {
            case 0:
                result=currentSubActivity.action();
                res=result[0];
                if(interSkip) {
                    mCurrentAction++;
                    interSkip=false;
                }
                //if(res==1)mAscii.maximizeInfo();
                break;
            case 1:
                result=currentSubActivity.action();
                res=result[0];
                mParent=result[0];
                mParentParts=result[1];
                mReservedStory=result[2];
                mPartOffset=result[3];
                //if (result==1){mCurrentAction++;}
                break;

            case 2:
                result=currentSubActivity.action();
                res=result[0];
                mParent=result[1];
                mParentParts=result[2];

                //mParent=result;
                break;
            case 3:
                result=currentSubActivity.action();
                res=result[0];
                //if (result==1){mCurrentAction++;}
                break;

            case 4:
                result=currentSubActivity.action();
                res=result[0];
                mCurrentAction=-1;
                mUserWait=true;
                mParent=-1;
                mParentParts=-1;
                mReservedStory=-1;
                mPartOffset=-1;
                break;
        }


        if(res!=-1)
        {

            mAscii.mAsciiStopUpdater(1);
            while(mAscii.mUpdating);
            mTimerLoop.cancel();

            mTimer.cancel();
            mTimer.purge();

            //mTimerLoop=null;
            //mTimer=null;

            currentSubActivity.destroy();
            currentSubActivity=null;

            mCurrentAction++;
            createTimerTask();


            mTimer=new Timer();

            //mAscii.mGLView.onPause();
            mTimer.scheduleAtFixedRate(mTimerLoop, 0, 60);
            //mAscii.mGLView.onResume();


        }
    }

    private void createTimerTask()
    {
        switch(mCurrentAction)
        {
            case 0:
                currentSubActivity=new InitClass(this,mAscii,mServer,appData);
                //currentSubActivity.Create(this,mAscii,mServer);
                //initClass=new InitClass(this,mAscii,mServer);
                mTimerLoop=currentSubActivity.getTimerTask();
                break;

            case 1:
                currentSubActivity=new PreRecorderClass(this,mAscii,mServer);
                //recorderClass=new RecorderClass(this,mAscii,mServer,mParent);
                mTimerLoop=currentSubActivity.getTimerTask();
                break;

            case 2:
                currentSubActivity=new PlayerClass(this,mAscii,mServer,mParent,mParentParts);
                //currentSubActivity=new PlayerClass(this,mAscii,mServer,1,14);
                //playerClass=new PlayerClass(this,mAscii,mServer);
                mTimerLoop=currentSubActivity.getTimerTask();
                break;

            case 3:
                currentSubActivity=new RecorderClass(this,mAscii,mServer,mParent,mReservedStory,mPartOffset);
                //recorderClass=new RecorderClass(this,mAscii,mServer,mParent);
                mTimerLoop=currentSubActivity.getTimerTask();
                break;
            case 4:
                currentSubActivity=new FinalizeClass(this,mAscii,mServer);
                //recorderClass=new RecorderClass(this,mAscii,mServer,mParent);
                mTimerLoop=currentSubActivity.getTimerTask();
                break;
        }



    }





    //TODO: fix on touch event
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        super.onTouchEvent(event);

        glTouch();
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
        Log.d("MAIN", "paused");
        mAscii.mAsciiStopUpdater(1);
        mTimerLoop.cancel();
        mTimer.cancel();
        mTimer.purge();

        mAscii.mGLView.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTimerLoop=null;

    }

    @Override
    protected void onResume() {
        super.onResume();
        mAscii.mAsciiStartUpdater(50);
        createTimerTask();
        mTimer=new Timer();

        mTimer.scheduleAtFixedRate(mTimerLoop, 0, 60);

        mAscii.mGLView.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDeviceData=new DeviceData(this);

        //mDeviceData.setDeviceId(2);

        //int id=mDeviceData.getDeviceId();

        //Log.d("MAIN","DEVID:"+id);

        //if(mDeviceData==null)return;

        //SERVER
        //mServer=new Server(this,appData);
        mServer=new Server(this,mDeviceData);

        //GET DATA
        //appData=new Data(this,mServer);

        //mServer.mData=appData;


        //CHECK CONNECTION DURING RUNNING






        /*try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // Restore interrupt status.
            Thread.currentThread().interrupt();
        }*/


        //finish();

        //setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //GET GLES
        mAscii=new ASCIIscreen(this,"MAIN");
        setContentView(mAscii.mGLView);



        //MAIN TREAD
        mTimer=new Timer();


        //AUTOBOT
        /*TimerTask auto= new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        glTouch();
                    }
                });

            }};

        new Timer().scheduleAtFixedRate(auto, 0, 4000);*/

        TimerTask conCheck= new TimerTask() {
            @Override
            public void run() {
                if(mServer.checkConnection())
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAscii.mGLView.mRenderer.setLed(true, false);
                        }
                    });
                    mSync=true;
                }
                else
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAscii.mGLView.mRenderer.setLed(false,false);
                        }
                    });
                    mSync=false;
                    //
                }
                /*runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });*/

            }};

        new Timer().scheduleAtFixedRate(conCheck, 0, 16000);

        TimerTask sync= new TimerTask() {
            @Override
            public void run() {
                if(mSync)
                {
                    if(appData.sync())
                    {
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run() {
                                mAscii.mGLView.mRenderer.setLed(true, true);
                            }
                        });
                    }
                }





            }
        };

        new Timer().scheduleAtFixedRate(sync, 0, 8000);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(this, BrowserActivity.class);
        startActivity(intent);
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

    public static class InterviewSkip extends DialogFragment {
        /*MainActivity acti

        public InterviewSkip(MainActivity activity){}*/

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("If you are here for the first time, we would like you to answer few questions first, but you can skip it if you want!")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ((MainActivity)getActivity()).diaBut(false);
                        }
                    })
                    .setNegativeButton("skip", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                            ((MainActivity)getActivity()).diaBut(true);
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }


}

class SubAct
{


    public TimerTask getTimerTask()
    {
        return new TimerTask() {
            @Override
            public void run() {

            }
        };
    }

    public int[] action()
    {
        int[] result=new int[1];
        return result;
    }

    public void destroy()
    {

    }
}

class Data
{
    Server mServer;
    File mData;
    DevData mDevData;


    public class DevData
    {
        public class Story
        {
            public class Part
            {
                int bufsize;
                public String mQuestion;
                public String mFileName;
            }
            int bufsize;
            int mId;
            int mParent;
            int mNoParts;
            boolean complete;
            Part[] mPart;
            public Story(int id,int parent,int compl)
            {
                bufsize=0;
                mId=id;
                mParent =parent;
                mNoParts=0;
                if(compl==0)complete=false;
                else complete=true;

            }
            int addPart(int ndx,StoryPart part)
            {
                mPart[ndx]=new Part();
                mPart[ndx].mQuestion=part.getQuestion();
                mPart[ndx].mFileName=part.getFilePath();
                return ndx;
            }
        }
        public byte[] mBuffer;

        public int mDeviceId;
        public int mStatus;
        public int mNoStories;
        public Story[] mStory;
        public DevData()
        {

        }
        public void addStory(int ndx,int id, int parent,int complete)
        {
            Log.d("DATA","adding story to "+ndx);
            mStory[ndx]=new Story(id,parent,complete);
        }
    }

    public Data(Activity act,Server server)
    {

        mServer=server;
        mData=new File(act.getFilesDir(),"data.txt");
        //clearData();
        mDevData=new DevData();

        //CHECK IF DATA FILE IS PRESENT ON THE DEVICE
        if(!mData.exists())
        {
            Log.d("DATA", "no data file");

            try
            {
                mData.createNewFile();



            }
            catch (Exception io){Log.e("MAIN","could not create data file");}

            initData();
        }
        else if(mData.length()<4)
        {
            initData();
        }
        getData();
    }

    private void clearData()
    {
        byte[] buf={0,0,0,1};

        try
        {
            FileOutputStream fo=new FileOutputStream(mData);
            fo.write(buf,0,4);
            fo.close();

        }
        catch (Exception io)
        {
            Log.e("DATA","could not initialize data");
        }
        /*mDevData.mDeviceId=0;
        mDevData.mStatus=0;
        mDevData.mNoStories=0;
        mDevData.mBuffer=buf;*/
    }

    public boolean check()
    {
        return true;
    }

    public int[] getLastStory()
    {
        int[] result=new int[2];
        result[0]=0;
        if(mDevData.mStory==null)
        {
            return result;
        }
        if(mDevData.mStory.length==0)
        {

        }
        else
        {
            for(int i=0;i<mDevData.mStory.length;i++)
            {
                if(mDevData.mStory[i].mId>result[0] && mDevData.mStory[i].complete)result[0]=mDevData.mStory[i].mId;
            }
            result[1]=mDevData.mStory[getStoryNdx(result[0])].mPart.length;
        }


        return result;
    }

    public int addStory(int id,int parent,int complete)
    {
        Log.d("DATA", "sts: "+mDevData.mStory);
        if(mDevData.mStory==null)
        {
            mDevData.mStory=new DevData.Story[1];
            mDevData.addStory(0,id,parent,complete);

            return 1;
        }
        else
        {
            DevData.Story[] tmpStory=mDevData.mStory;
            mDevData.mStory=new DevData.Story[tmpStory.length+1];
            for(int i=0;i<tmpStory.length;i++)
            {
                mDevData.mStory[i]=tmpStory[i];

            }
            mDevData.addStory(tmpStory.length,id,parent,complete);
            return 1;
        }

    }

    public int getEmptyStoryId()
    {
        int id=1;
        if(mDevData.mStory==null)return id;
        for(int i=0;i<mDevData.mStory.length;i++)
        {
            if(mDevData.mStory[i].mId==id)id++;
        }
        return id;
    }

    public int getStoryNdx(int id)
    {
        for(int i=0;i<mDevData.mStory.length;i++)
        {
            Log.d("DATA", "ids: "+id+","+mDevData.mStory[i].mId);
            if(id==(mDevData.mStory[i].mId))
            {
                return i;
            }
        }
        return -1;
    }

    public int addStoryPart(int ndx,StoryPart part,boolean up)
    {
        Log.d("DATA","to ndx:"+ndx);
        if(mDevData.mStory[ndx].mPart==null)
        {
            mDevData.mStory[ndx].mPart=new DevData.Story.Part[1];
            mDevData.mStory[ndx].addPart(0,part);

            //Log.d("DATA", "prt: " + ndx + "_" + mDevData.mStory[ndx].mPart[0].mQuestion + "," + mDevData.mStory[ndx].mPart[0].mFileName);
            if(up)updateData();
            return 1;
        }
        else
        {
            DevData.Story.Part[] tmpPart=mDevData.mStory[ndx].mPart;
            mDevData.mStory[ndx].mPart=new DevData.Story.Part[mDevData.mStory[ndx].mPart.length+1];
            for(int i=0;i<tmpPart.length;i++)
            {
                mDevData.mStory[ndx].mPart[i]=tmpPart[i];

            }
            mDevData.mStory[ndx].addPart(tmpPart.length, part);

            //Log.d("DATA", "prt: " + mDevData.mStory[ndx].mPart[tmpPart.length].mQuestion + "," + mDevData.mStory[ndx].mPart[tmpPart.length].mFileName);
            if(up)updateData();
            return tmpPart.length;
        }
        //return 0;
    }

    public int completeStory(int id,boolean up)
    {
        int ndx=getStoryNdx(id);
        if (mDevData.mStory.length<=ndx)return -1;
        mDevData.mStory[ndx].complete=true;
        if(up)updateData();
        return ndx;

    }

    public boolean sync()
    {
        if (mServer==null)
        {
            Log.e("DATA", "server uninitialized");
            return false;
        }
        Log.d("DATA", "syncing");
        getData();
        Log.d("DATA", "registered id: " + mDevData.mDeviceId);
        //Log.d("DATA","registered id: "+mDevData.mBuffer.length);

        int newDeviceId;

        newDeviceId=mServer.requestDeviceId(mDevData.mDeviceId);
        Log.d("DATA","nid"+newDeviceId);
        if(newDeviceId!=mDevData.mDeviceId)
        {
            mDevData.mDeviceId=newDeviceId;
            updateData();
            //mServer.sendDeviceData(mDevData);
        }

        getData();
        int stat=getDeviceStatus();
        if(stat==1)
        {
            String data=mServer.requestDeviceData(mDevData.mBuffer);
            Log.d("DATA","data: "+data);
            data.toCharArray();
        }




        if(mDevData.mDeviceId==0)return false;


        return true;
    }

    private void getData()
    {
        Log.d("DATA", "###Reading data");
        try
        {

            FileInputStream fi=new FileInputStream(mData);
            int fs=fi.available();
            Log.d("DATA", "siz:" + fs);
            byte[] buf=new byte[fs];
            fi.read(buf, 0, fs);
            fi.close();
            mDevData.mBuffer=buf;

            int n=0;
            if(fs<4)return;
            mDevData.mDeviceId= (buf[n] << 24) | (buf[n+1] << 16) | (buf[n+2] << 8) | buf[n+3];n+=4;
            Log.d("DATA", "did" + mDevData.mDeviceId);
            if(fs<8)return;
            mDevData.mStatus=(buf[n] << 24) | (buf[n+1] << 16) | (buf[n+2] << 8) | buf[n+3];n+=4;

            if(fs<12)return;
            mDevData.mNoStories=(buf[n] << 24) | (buf[n+1] << 16) | (buf[n+2] << 8) | buf[n+3];n+=4;
            mDevData.mNoStories=0;
            if(fs==12)return;
            n+=0;

            while(n<buf.length)
            {
                mDevData.mNoStories++;
                int id=((0xFF & buf[n]) << 24) |((0xFF & buf[n+1]) << 16) |((0xFF & buf[n+2]) << 8) |(0xFF & buf[n+3]);n+=4;
                //Log.d("DATA","story size: "+buf[n]+":"+buf[n+1]+":"+buf[n+2]+":"+(int)(char)buf[n+3]);
                int size=((0xFF & buf[n]) << 24) |((0xFF & buf[n+1]) << 16) |((0xFF & buf[n+2]) << 8) |(0xFF & buf[n+3]);n+=4;
                //int size=((char)buf[n] << 24) | (char)(buf[n+1] << 16) | (char)(buf[n+2] << 8) | (char)buf[n+3];n+=4;
                int parent=((0xFF & buf[n]) << 24) |((0xFF & buf[n+1]) << 16) |((0xFF & buf[n+2]) << 8) |(0xFF & buf[n+3]);n+=4;
                int complete=((0xFF & buf[n]) << 24) |((0xFF & buf[n+1]) << 16) |((0xFF & buf[n+2]) << 8) |(0xFF & buf[n+3]);n+=4;
                addStory(id,parent,complete);
                Log.d("DATA","story size: "+size+" id:"+id);
                int sp=0;
                size-=16;
                while(size>0)
                {
                    StoryPart part=new StoryPart();
                    int pid=((0xFF & buf[n]) << 24) |((0xFF & buf[n+1]) << 16) |((0xFF & buf[n+2]) << 8) |(0xFF & buf[n+3]);n+=4;size-=4;
                    int psiz=((0xFF & buf[n]) << 24) |((0xFF & buf[n+1]) << 16) |((0xFF & buf[n+2]) << 8) |(0xFF & buf[n+3]);n+=4;size-=4;
                    int pqsiz=((0xFF & buf[n]) << 24) |((0xFF & buf[n+1]) << 16) |((0xFF & buf[n+2]) << 8) |(0xFF & buf[n+3]);n+=4;size-=4;
                    Log.d("DATA","pqsiz"+pqsiz);
                    byte[] qbuf=new byte[pqsiz];
                    for(int i=0;i<pqsiz;i++)
                    {
                        qbuf[i]=buf[n];n++;size-=1;
                    }

                    String q=new String(qbuf);

                    int pfsiz=((0xFF & buf[n]) << 24) |((0xFF & buf[n+1]) << 16) |((0xFF & buf[n+2]) << 8) |(0xFF & buf[n+3]);n+=4;size-=4;

                    byte[] fbuf=new byte[pfsiz];
                    Log.d("DATA","pfsiz"+pfsiz+" "+fbuf.length+" "+buf.length);
                    for(int i=0;i<pfsiz;i++)
                    {
                        fbuf[i]=buf[n];n++;size-=1;
                    }

                    String f=new String(fbuf);
                    part.populate("", q, f);
                    addStoryPart(getStoryNdx(id), part,false);

                    //Log.d("DATA","N:"+n+" bufl:"+buf.length+" s:"+size);
                }
                sp++;

            }



            Log.d("DATA", "local devId:" + mDevData.mDeviceId);
            Log.d("DATA", "local devStat:" + mDevData.mStatus);
            Log.d("DATA","local stories:"+ mDevData.mNoStories);

            for(int i=0;i<mDevData.mStory.length;i++)
            {
                Log.d("DATA"," story Id:"+ mDevData.mStory[i].mId);
                Log.d("DATA"," story parent:"+ mDevData.mStory[i].mParent);
                Log.d("DATA"," story complete:"+ mDevData.mStory[i].complete);
                Log.d("DATA"," story parts:"+ mDevData.mStory[i].mPart.length);
                for(int j=0;j<mDevData.mStory[i].mPart.length;j++)
                {
                    //Log.d("DATA","  part Id:"+ mDevData.mStory[i].mPart[j].mQuestion);
                    //Log.d("DATA","  part Id:"+ mDevData.mStory[i].mPart[j].mFileName);
                }
            }

        }
        catch (Exception io)
        {
            Log.e("DATA","could not get data GD"+io);
        }


    }

    int updateData()
    {
        mDevData.mStatus=1;

        Log.d("DATA","UPDATING DATA");

        //BUFFER SIZE=
        int bufsize=12;
        if(mDevData.mStory!=null) {
            for (int i = 0; i < mDevData.mStory.length; i++) {
                Log.d("DATA", "stories" + mDevData.mStory.length);
                bufsize += 16;
                int storysize = 16;
                if (mDevData.mStory[i].mPart != null) {
                    Log.d("DATA", "story " + i + " parts" + mDevData.mStory[i].mPart.length);
                    for (int j = 0; j < mDevData.mStory[i].mPart.length; j++) {
                        bufsize += 16;
                        storysize += 16;
                        bufsize += mDevData.mStory[i].mPart[j].mQuestion.length();
                        bufsize += mDevData.mStory[i].mPart[j].mFileName.length();
                        Log.d("DATA", "qlen:" + mDevData.mStory[i].mPart[j].mQuestion.length());
                        Log.d("DATA", "flen:" + mDevData.mStory[i].mPart[j].mFileName.length());
                        storysize += mDevData.mStory[i].mPart[j].mQuestion.length();
                        storysize += mDevData.mStory[i].mPart[j].mFileName.length();

                        mDevData.mStory[i].mPart[j].bufsize = mDevData.mStory[i].mPart[j].mFileName.length() + mDevData.mStory[i].mPart[j].mQuestion.length();
                        mDevData.mStory[i].mPart[j].bufsize += 16;

                    }
                }
                mDevData.mStory[i].bufsize = storysize;
            }
        }
        Log.d("DATA","buffsize:"+bufsize);

        try
        {
            Log.d("DATA","writing head");
            int n=0;
            mDevData.mBuffer=new byte[bufsize];
            mDevData.mBuffer[n] = (byte)(mDevData.mDeviceId >> 24);n++;
            mDevData.mBuffer[n] = (byte)(mDevData.mDeviceId >> 16);n++;
            mDevData.mBuffer[n] = (byte)(mDevData.mDeviceId >> 8);n++;
            mDevData.mBuffer[n] = (byte)(mDevData.mDeviceId >> 0);n++;

            mDevData.mBuffer[n] = (byte)(mDevData.mStatus >> 24);n++;
            mDevData.mBuffer[n] = (byte)(mDevData.mStatus >> 16);n++;
            mDevData.mBuffer[n] = (byte)(mDevData.mStatus >> 8);n++;
            mDevData.mBuffer[n] = (byte)(mDevData.mStatus >> 0);n++;

            if(mDevData.mStory!=null)
            {mDevData.mNoStories=mDevData.mStory.length;}
            else
            {
                mDevData.mNoStories=0;
            }

            mDevData.mBuffer[n] = (byte)(mDevData.mNoStories >> 24);n++;
            mDevData.mBuffer[n] = (byte)(mDevData.mNoStories >> 16);n++;
            mDevData.mBuffer[n] = (byte)(mDevData.mNoStories >> 8);n++;
            mDevData.mBuffer[n] = (byte)(mDevData.mNoStories >> 0);n++;

            if(mDevData.mStory!=null)
            {
                for (int i = 0; i < mDevData.mStory.length; i++) {
                    Log.d("DATA", "writing story" + i);
                    mDevData.mBuffer[n] = (byte) (mDevData.mStory[i].mId >> 24);
                    n++;
                    mDevData.mBuffer[n] = (byte) (mDevData.mStory[i].mId >> 16);
                    n++;
                    mDevData.mBuffer[n] = (byte) (mDevData.mStory[i].mId >> 8);
                    n++;
                    mDevData.mBuffer[n] = (byte) (mDevData.mStory[i].mId >> 0);
                    n++;

                    mDevData.mBuffer[n] = (byte) (mDevData.mStory[i].bufsize >> 24);
                    n++;
                    mDevData.mBuffer[n] = (byte) (mDevData.mStory[i].bufsize >> 16);
                    n++;
                    mDevData.mBuffer[n] = (byte) (mDevData.mStory[i].bufsize >> 8);
                    n++;
                    mDevData.mBuffer[n] = (byte) (mDevData.mStory[i].bufsize >> 0);
                    n++;

                    mDevData.mBuffer[n] = (byte) (mDevData.mStory[i].mParent >> 24);
                    n++;
                    mDevData.mBuffer[n] = (byte) (mDevData.mStory[i].mParent >> 16);
                    n++;
                    mDevData.mBuffer[n] = (byte) (mDevData.mStory[i].mParent >> 8);
                    n++;
                    mDevData.mBuffer[n] = (byte) (mDevData.mStory[i].mParent >> 0);
                    n++;

                    if (mDevData.mStory[i].complete) {
                        mDevData.mBuffer[n] = (byte) (1 >> 24);
                        n++;
                        mDevData.mBuffer[n] = (byte) (1 >> 16);
                        n++;
                        mDevData.mBuffer[n] = (byte) (1 >> 8);
                        n++;
                        mDevData.mBuffer[n] = (byte) (1 >> 0);
                        n++;
                    } else {
                        mDevData.mBuffer[n] = (byte) (0 >> 24);
                        n++;
                        mDevData.mBuffer[n] = (byte) (0 >> 16);
                        n++;
                        mDevData.mBuffer[n] = (byte) (0 >> 8);
                        n++;
                        mDevData.mBuffer[n] = (byte) (0 >> 0);
                        n++;
                    }
                    Log.d("DATA", "N:" + n);

                    for (int j = 0; j < mDevData.mStory[i].mPart.length; j++) {
                        Log.d("DATA", "writing story part" + j + "<-" + i);
                        Log.d("DATA", "Nps:" + n);
                        mDevData.mBuffer[n] = (byte) (j >> 24);
                        n++;
                        mDevData.mBuffer[n] = (byte) (j >> 16);
                        n++;
                        mDevData.mBuffer[n] = (byte) (j >> 8);
                        n++;
                        mDevData.mBuffer[n] = (byte) (j >> 0);
                        n++;

                        mDevData.mBuffer[n] = (byte) (mDevData.mStory[i].mPart[j].bufsize >> 24);
                        n++;
                        mDevData.mBuffer[n] = (byte) (mDevData.mStory[i].mPart[j].bufsize >> 16);
                        n++;
                        mDevData.mBuffer[n] = (byte) (mDevData.mStory[i].mPart[j].bufsize >> 8);
                        n++;
                        mDevData.mBuffer[n] = (byte) (mDevData.mStory[i].mPart[j].bufsize >> 0);
                        n++;

                        mDevData.mBuffer[n] = (byte) (mDevData.mStory[i].mPart[j].mQuestion.length() >> 24);
                        n++;
                        mDevData.mBuffer[n] = (byte) (mDevData.mStory[i].mPart[j].mQuestion.length() >> 16);
                        n++;
                        mDevData.mBuffer[n] = (byte) (mDevData.mStory[i].mPart[j].mQuestion.length() >> 8);
                        n++;
                        mDevData.mBuffer[n] = (byte) (mDevData.mStory[i].mPart[j].mQuestion.length() >> 0);
                        n++;

                        for (int k = 0; k < mDevData.mStory[i].mPart[j].mQuestion.length(); k++) {
                            //Log.d("DATA","Npm:"+n);
                            mDevData.mBuffer[n] = (byte) mDevData.mStory[i].mPart[j].mQuestion.charAt(k);
                            n++;
                        }

                        mDevData.mBuffer[n] = (byte) (mDevData.mStory[i].mPart[j].mFileName.length() >> 24);
                        n++;
                        mDevData.mBuffer[n] = (byte) (mDevData.mStory[i].mPart[j].mFileName.length() >> 16);
                        n++;
                        mDevData.mBuffer[n] = (byte) (mDevData.mStory[i].mPart[j].mFileName.length() >> 8);
                        n++;
                        mDevData.mBuffer[n] = (byte) (mDevData.mStory[i].mPart[j].mFileName.length() >> 0);
                        n++;


                        for (int k = 0; k < mDevData.mStory[i].mPart[j].mFileName.length(); k++) {
                            //Log.d("DATA","Npm:"+n+" "+mDevData.mStory[i].mPart[j].mFileName.length()+":"+k);
                            mDevData.mBuffer[n] = (byte) mDevData.mStory[i].mPart[j].mFileName.charAt(k);
                            n++;
                        }


                        Log.d("DATA", "Npe:" + n);
                    }
                }
            }




            FileOutputStream fi=new FileOutputStream(mData);


            byte[] buf=mDevData.mBuffer;
            fi.write(buf,0,buf.length);
            fi.close();



            Log.d("DATA","local devId:"+ mDevData.mDeviceId);

        }
        catch (Exception io)
        {
            Log.e("DATA","could not put data"+io);
        }
        return 0;
    }

    int getDeviceStatus()
    {
        int stat=mDevData.mStatus;

        return stat;
    }

    private void initData()
    {
        byte[] buf={0,0,0,1};

        try
        {
            FileOutputStream fo=new FileOutputStream(mData);
            fo.write(buf,0,4);
            fo.close();

        }
        catch (Exception io)
        {
            Log.e("DATA","could not initialize data");
        }
        mDevData.mDeviceId=0;
        mDevData.mStatus=0;
        mDevData.mNoStories=0;
        mDevData.mBuffer=buf;
    }




}


