package no.ice_9.xquisite;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.opengl.EGLConfig;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;


import android.text.BoringLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;

import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.TextView;


import java.io.Serializable;
import java.nio.ByteBuffer;

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.IllegalCharsetNameException;
import java.util.Calendar;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.khronos.opengles.GL10;


/**
 * Created by HUMAN on 15.10.2015.
 */
@SuppressWarnings("serial")
public class ASCIIscreen implements Serializable{

    //DEBUG TIME MEASURE
    long mMesTime=0;
    long mLasTime=0;

    //SCREEN VARIABLES
    Display display;
    DisplayMetrics displayMetrics;

    //TextView mText;
    XQGLSurfaceView mGLView;

    //STATIC LINE NUMBER
    static private int lineCount=70;
    private float lineHeight;


    private int mLinePointer;
    private String mLine[];
    private String mAllLines;
    private int mSymbolsPerLine;


    public boolean mReady;
    private boolean mRage;
    String mWordList[];

    String[] mTrash;

    Activity tAct;

    AsciiCharSet mAsciiCharSet;

    TimerTask mUpdater;
    boolean mUpdating;

    boolean mRequestStop;
    int mStopTime;

    String mActParent;

    public Canvas canvas;







    public ASCIIscreen(Context context,TextView text,String actParent)
    {
        mMesTime= Calendar.getInstance().getTimeInMillis();
        mLasTime=mMesTime;


        mActParent=actParent;
        mAsciiCharSet=new AsciiCharSet("ASCII",null);
        mUpdating=false;
        mLine=new String[lineCount];
        mLinePointer=0;
        tAct=(Activity)context;
        //mText=text;
        display = ((WindowManager)context.getSystemService(context.WINDOW_SERVICE)).getDefaultDisplay();
        displayMetrics=new DisplayMetrics();
        display.getMetrics(displayMetrics);
        mRage=false;
        mRequestStop=false;

        mGLView = new XQGLSurfaceView(context,displayMetrics,lineCount,this);

        //WORDS TO BE USED
        mWordList=new String[]{"science","life","corruption","future","source","utopia","time","order","chaos"};




        lineHeight=displayMetrics.heightPixels/lineCount;
        //Log.d("ASCII", "dispH,lineH:" + displayMetrics.heightPixels + " " + lineHeight);

        //mText.setTextSize(TypedValue.COMPLEX_UNIT_PX, lineHeight);
        float a = 100.0f/120.0f;
        //mText.setLineSpacing(0.0f, a);

        mSymbolsPerLine=-1;



        for(int i=0;i<lineCount;i++)
        {
            mLine[i]="";
        }

        mAllLines="";
        for(int i=0;i<lineCount;i++)
        {
            mAllLines+=mLine[i]+"\n";
        }

        //mText.setText(allLines);
        canvas=new Canvas(Bitmap.createBitmap((int)(lineCount*((float)displayMetrics.widthPixels)/(float)displayMetrics.heightPixels),lineCount, Bitmap.Config.ARGB_8888));

        //Log.d("ASCII","runAQ"+3);
        //Log.d("ASCII", "real size" + mText.getExtendedPaddingTop());
        createUpdater();

        mMesTime= Calendar.getInstance().getTimeInMillis();
        Log.d("TIME","  ASCII INIT "+(mMesTime-mLasTime)+"ms");
        mLasTime=mMesTime;
        //mAsciiStartUpdater(50);
    }

    private void createUpdater()
    {
        mStopTime=-1;
        final Random Rnd=new Random();
        mUpdater=new TimerTask() {
            @Override
            public void run() {
                mUpdater = this;
                //if(mSymbolsPerLine!=-1 && !mReady){mReady=true;}
                if (mRage) {
                    pushLine("&/¤(&/" + mWordList[Rnd.nextInt() % 4 + 4] + "2/(&%¤76KLJ))=/(¤");
                }
                mAllLines = "";
                for (int i = 0; i < lineCount; i++) {

                    mAllLines += mLine[i] + "\n";
                }

                tAct.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        //mGLView.setText(mAllLines + "");
                    }
                });

                if(mRequestStop){ mStopTime--;if(mStopTime<=0){mUpdater.cancel();}}
                //Log.d("ASCII","RUNNING from "+ mActParent);

            }
        };
    }



    public void mAsciiStartUpdater(int rate)
    {
        if(!mUpdating)
        {

            mUpdating=true;
            mRequestStop=false;
            createUpdater();
            new Timer().scheduleAtFixedRate(mUpdater,0,rate);
        }

    }

    public void mAsciiStopUpdater(int delay)
    {
        mStopTime=delay;
        //mUpdater.cancel();
        mRequestStop=true;
        mUpdating=false;
    }

    public float getTextSize()
    {
        return lineHeight;
    }

    public void pushLine(String line)
    {
        //Log.d("ASCII","line:"+line);

        int i;
        if(mLinePointer<lineCount)
        {
            mGLView.putString(line,mLinePointer,0);
            mLine[mLinePointer]=line;
            mLinePointer++;
        }
        else
        {
            for(i=0;i<lineCount-1;i++)
            {
               mGLView.putString(mLine[i+1],i,0);
               //mLine[i]=mLine[i+1];
            }
            mLine[i]=line;
            mGLView.putString(line,i,0);
        }

    }

    public void modLine(String line, int ndx,int pos) {
        mGLView.putString(line,ndx,pos);
        //Log.d("ASCII","mll"+mLine[ndx].length());
        /*if(ndx<mLine.length )
        {

            if(pos==-1)
            {
                //mLine[ndx]=line;
            }
            else if(mReady && pos+line.length()<mLine[ndx].length())
            {
                char[] tmpCh=mLine[ndx].toCharArray();

                for(int i=0;i<line.length();i++)
                {
                    if((pos+i)<mLine[ndx].length())
                    {
                        tmpCh[pos+i]=line.charAt(i);
                    }

                }

                //mLine[ndx]=String.copyValueOf(tmpCh);
            }
        }*/


    }

    public void minimizeInfo()
    {
        mGLView.mRenderer.minimizeInfo();
    }

    public void maximizeInfo()
    {
        mGLView.mRenderer.maximizeInfo();
    }

    public void putImage()
    {
        mGLView.putImage(Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888));

        /*Uri uri=Uri.parse("/mnt/sdcard/tmp/tmp.jpg");
        Bitmap btm=BitmapFactory.decodeFile("/mnt/sdcard/tmp/tmp.jpg");
        Log.d("ASCII", "bm" + btm.getByteCount());*/

        //OutputStream os=new ByteArrayOutputStream(256);
        //btm.compress(Bitmap.CompressFormat.JPEG, 0, os);





       /* Bitmap btm2;
        Log.d("ASCII","W,H"+btm.getWidth()+", "+btm.getHeight());

        btm2=Bitmap.createScaledBitmap(btm, mSymbolsPerLine,lineCount, false);

        btm2.setPixel(0,0,0);

        ByteBuffer mChBuff = ByteBuffer.allocate(btm2.getByteCount());
        btm2.copyPixelsToBuffer(mChBuff);*/



        /*String sttm="";
        for(int i=0;i<32;i++)
        {
            sttm+=(char)i;
        }
        CharSequence chseq=new String(sttm);*/
        /*String s;
        for(int i=0;i<lineCount;i++)
        {

            s=new String(mChBuff.array(),i*mSymbolsPerLine*4,mSymbolsPerLine*4, mAsciiCharSet);
            s=s.replace("\n", "#");
            //String s=String.copyValueOf(str);

            modLine(s, i, -1);
        }*/
    }

    public void putCanvas(Canvas cnvs)
    {
        Bitmap bitmap= Bitmap.createBitmap(cnvs.getWidth(),cnvs.getHeight(),Bitmap.Config.ARGB_8888);
        cnvs.drawBitmap(bitmap, new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()), new Rect(0, 0, cnvs.getWidth(), cnvs.getHeight()), null);
        mGLView.putImage(bitmap);
        bitmap.recycle();


    }




    public void fillTrash()
    {



        /*if(mReady)
        {
            int i=0;
            Random rnd=new Random();
            for(i=0;i<lineCount;i++)
            {




                //tmpStr=new String(buf);
                mLine[i]=mTrash[rnd.nextInt(100)];

            }
            mLinePointer=i;
        }*/

    }

    public void setRage(boolean on)
    {
        if(on){mRage=true;}
        else{mRage=false;}
    }

    public void clear()
    {

        mReady=false;

        //Bitmap
        for(int i=0;i<lineCount;i++)
        {

            mLine[i]="";
        }
        mLinePointer=0;

        Log.d("ASCII","CLEAR");
        mGLView.mRenderer.clearAscii();



        mReady=true;
    }

    public boolean isRage()
    {
        return mRage;
    }


}

class XQGLSurfaceView extends GLSurfaceView{
    //DEBUG TIME MEASURE
    long mMesTime=0;
    long mLasTime=0;

    public final XQGLRenderer mRenderer;
    private final Activity actContext;
    DisplayMetrics mMetrics;

    public XQGLSurfaceView(Context context,DisplayMetrics metrics,int lineCount,ASCIIscreen asciiscreen)
    {
        super(context);

        mMetrics=metrics;
        mMesTime= Calendar.getInstance().getTimeInMillis();
        mLasTime=mMesTime;

        actContext=(Activity)context;
        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        mRenderer = new XQGLRenderer();
        mRenderer.asciicols=(int)(lineCount*((float)metrics.widthPixels)/(float)metrics.heightPixels);
        mRenderer.asciirows=lineCount;
        mRenderer.view=asciiscreen;
        //Log.d("ASCII","cr"+mRenderer.asciicols+":"+mRenderer.asciirows);
        mRenderer.actContext=context;
        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(mRenderer);

        mMesTime= Calendar.getInstance().getTimeInMillis();
        Log.d("TIME","    GL INIT "+(mMesTime-mLasTime)+"ms");
        mLasTime=mMesTime;

    }

    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private float mPreviousX;
    private float mPreviousY;

    @Override
    public boolean onTouchEvent(MotionEvent e) {

        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        float x = (e.getX()/mMetrics.widthPixels*2)-1.0f;
        float y = 1.0f-(e.getY()/mMetrics.heightPixels*2);

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:
                //actContext.onTouchEvent(e);
                break;
            case MotionEvent.ACTION_DOWN:
                mRenderer.setClick(x,y);
                //actContext.onTouchEvent(e);
                break;
            case MotionEvent.ACTION_UP:
                if(mRenderer.getClick(x,y))
                {
                    actContext.onTouchEvent(e);
                }
                break;
        }

        mPreviousX = x;
        mPreviousY = y;
        return true;
    }

    public void putString(String str,int row,int pos)
    {
        Log.d("ASCII","line:"+str);
        mRenderer.putString(str,row,pos);
    }

    public void putImage(Bitmap bitmap)
    {
        mRenderer.putImage(bitmap);
    }

    public void setText()
    {

    }



}





class AsciiCharSet extends Charset {
    /**
     * Constructs a <code>Charset</code> object. Duplicated aliases are
     * ignored.
     *
     * @param canonicalName the canonical name of the charset.
     * @param aliases       an array containing all aliases of the charset. May be null.
     * @throws IllegalCharsetNameException on an illegal value being supplied for either
     *                                     <code>canonicalName</code> or for any element of
     *                                     <code>aliases</code>.
     */
    protected AsciiCharSet(String canonicalName, String[] aliases) {
        super(canonicalName, aliases);
    }

    @Override
    public boolean contains(Charset charset) {
        return false;
    }

    @Override
    public CharsetDecoder newDecoder()
    {
        final String chseq=" ...,,-_'::;^=+/\\\"|)\\\\<>)iv%xclrs*}I?![1tao7zjLunT#Cwfy325Fp6mqShd4EgXPGZbYkOA&8U$@HBNR0#";
        return new CharsetDecoder(this,0.25f,0.25f) {
            @Override
            protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
                int a;
                /*while(in.remaining()>0)
                {
                    if(out.remaining()>0)
                    {
                        in.get();
                        out.put("#");
                    }
                    else
                    {
                       in.getChar();
                    }

                }*/
                while(in.remaining()>0)
                {
                    a=0;
                    a+=in.get();
                    a+=in.get();
                    a+=in.get();

                    a/=3;
                    a-=128;
                    a/=3;
                    out.put(chseq.charAt(-a));
                    a=in.get();
                }
                while(out.remaining()>0)
                {
                    out.put(" ");
                }
                return CoderResult.UNDERFLOW;
            }
        };
    }

    @Override
    public CharsetEncoder newEncoder()
    {
        return new CharsetEncoder(this,4,4) {
            @Override
            protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
                return null;
            }
        };
    }
}
