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
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;

import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;
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

    int activeLine=0;

    //DEBUG TIME MEASURE
    long mMesTime=0;
    long mLasTime=0;

    //SCREEN VARIABLES
    Display display;
    DisplayMetrics displayMetrics;

    //TextView mText;
    XQGLSurfaceView mGLView;


    //STATIC LINE NUMBER
    static private int lineCount=120;
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

    //AsciiCharSet mAsciiCharSet;

    TimerTask mUpdater;
    boolean mUpdating;

    boolean mRequestStop;
    int mStopTime;

    String mActParent;

    public Canvas canvas;


    /**
     * Ascii screen constructor
     * meant to be a visual representation of video data
     * and other UI related content
     * TODO: change class and objects names to be more straight forward
     * @param context activity context
     * @param actParent activity from where it is created
     */
    public ASCIIscreen(Context context,String actParent)
    {
        mMesTime= Calendar.getInstance().getTimeInMillis();
        mLasTime=mMesTime;


        mActParent=actParent;
        //mAsciiCharSet=new AsciiCharSet("ASCII",null);
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

    //TODO: remove?
    private void createUpdater()
    {
        mStopTime=-1;
        //final Random Rnd=new Random();
        /*mUpdater=new TimerTask() {
            @Override
            public void run() {
                mUpdater = this;
                //if(mSymbolsPerLine!=-1 && !mReady){mReady=true;}
                /*if (mRage) {
                    pushLine("&/¤(&/" + mWordList[Rnd.nextInt() % 4 + 4] + "2/(&%¤76KLJ))=/(¤");
                }*/
                /*mAllLines = "";
                for (int i = 0; i < lineCount; i++) {

                    mAllLines += mLine[i] + "\n";
                }*/

                /*tAct.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        //mGLView.setText(mAllLines + "");
                    }
                });

                //if(mRequestStop){ mStopTime--;if(mStopTime<=0){mUpdater.cancel();}}
                //Log.d("ASCII","RUNNING from "+ mActParent);

            }
        };*/
    }


    /**
     * used to start updating the screen data
     * might be not used anymore
     * TODO: check usefullness
     * @param rate
     */
    public void mAsciiStartUpdater(int rate)
    {
        Log.d("ASCII","CREATING UPDATER");
        if(!mUpdating)
        {

            mUpdating=true;
            mRequestStop=false;
            //createUpdater();
            //new Timer().scheduleAtFixedRate(mUpdater,0,rate);
        }

    }

    /**
     * stop updating related to above
     * @param delay
     */
    public void mAsciiStopUpdater(int delay)
    {
        Log.d("ASCII","STOPPING UPDATER");
        mStopTime=delay;
        //mUpdater.cancel();
        mRequestStop=true;
        mUpdating=false;
    }

    /**
     * was used to get line height that can be fit to the screen
     * @return
     */
    public float getTextSize()
    {
        return lineHeight;
    }

    /**
     * pushes line of text into info frame
     * @param line line to be pushed
     */
    public void pushLine(String line)
    {
        //Log.d("ASCII","line:"+line);

        int i;
        if(mLinePointer<lineCount)
        {
            mGLView.mRenderer.putMsgString(line, mLinePointer,true);
            //mGLView.putString(line,mLinePointer,0);
            mLine[mLinePointer]=line;
            mLinePointer++;
        }
        else
        {
            for(i=0;i<lineCount-1;i++)
            {
                mGLView.mRenderer.putMsgString(line, mLinePointer,true);
                //mGLView.putString(mLine[i+1],i,0);
                //mLine[i]=mLine[i+1];
            }
            mLine[i]=line;
            mGLView.mRenderer.putMsgString(line, mLinePointer,true);
            //mGLView.putString(line,i,0);
        }

    }

    /**
     * modifies line of text in given position
     * @param line Text to be replaced to
     * @param ndx line index
     * @param pos was used before as offset from first character
     */
    public void modLine(String line, int ndx,int pos, boolean active) {
        mGLView.mRenderer.putMsgString(line, ndx, active);
        //mGLView.putString(line,ndx,pos);
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

    /**
     * currently not used, was used to make info frame smaller and bring video to focus
     */
    public void minimizeInfo()
    {
        Log.d("ASCII", "MINIMIZING");
        mGLView.mRenderer.minimizeInfo();
    }

    /**
     * also not used, befor it brought info frame to focus
     */
    public void maximizeInfo()
    {
        Log.d("ASCII","MAXIMIZING");
        mGLView.mRenderer.maximizeInfo();
    }

    /**
     * puts a custom image as ascii to the screen
     * @param btm
     */
    public void putImage(Bitmap btm)
    {


        mGLView.putImage(btm);

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

    /**
     * puts canvas as ascii to the screen
     * TODO: check if necessary and if working
     * currently not used
     * @param cnvs
     */
    public void putCanvas(Canvas cnvs)
    {
        Bitmap bitmap= Bitmap.createBitmap(cnvs.getWidth(),cnvs.getHeight(),Bitmap.Config.ARGB_8888);
        cnvs.drawBitmap(bitmap, new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()), new Rect(0, 0, cnvs.getWidth(), cnvs.getHeight()), null);
        mGLView.putImage(bitmap);
        bitmap.recycle();


    }


    /**
     * PUTS Random chars as ascii
     * TODO: remove or remake
     */
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

    /**
     * RAGE MODE- time when ascii are randomly generated
     * TODO: remove or remake
     * currently not used
     * @param on
     */
    public void setRage(boolean on)
    {
        if(on){mRage=true;}
        else{mRage=false;}
    }
    public boolean isRage()
    {
        return mRage;
    }

    /**
     * clears all ascii and text from info frame
     */
    public void clear()
    {

        //mReady=false;

        //Bitmap
        for(int i=0;i<lineCount;i++)
        {

            mLine[i]="";
            mGLView.mRenderer.hideMsgString(i);
        }
        mLinePointer=0;

        Log.d("ASCII","CLEAR");
        mGLView.mRenderer.clearAscii();
        mGLView.putImage(Bitmap.createBitmap(mGLView.mRenderer.asciicols, mGLView.mRenderer.asciirows, Bitmap.Config.ARGB_8888));


        //mReady=true;
    }





}

/**
 * GL Surface class
 * MAIN UI surface responsible for all visual and interactive events
 */
class XQGLSurfaceView extends GLSurfaceView{
    //DEBUG TIME MEASURE
    long mMesTime=0;
    long mLasTime=0;

    public final XQGLRenderer mRenderer;
    private final Activity actContext;
    DisplayMetrics mMetrics;

    boolean mMotionEngaged=false;

    /**
     * Constructor:
     * @param context Context from main activity
     * @param metrics Screen metrics
     * @param lineCount Line count fitting to the screen TODO: remake for small screens
     * @param asciiscreen passed UI-controller to GL Surface
     */
    public XQGLSurfaceView(Context context,DisplayMetrics metrics,int lineCount,ASCIIscreen asciiscreen)
    {
        super(context);
        //setFocusable(true);
        //setFocusableInTouchMode(true);
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
        requestFocus();


    }




    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        Log.d("ASCII","INPUT!!");

        outAttrs.inputType = InputType.TYPE_CLASS_TEXT;// InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE | InputType.TYPE_CLASS_TEXT;
        outAttrs.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_FLAG_NO_FULLSCREEN;
        return super.onCreateInputConnection(outAttrs);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.d("ASCII", "KEY"+String.valueOf(event.getKeyCode()));
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyDown(int KeyCode, KeyEvent event) {
        super.onKeyDown(KeyCode,event);
        Log.d("ASCII","!!!!!!!");
        //mAscii.mGLView.mRenderer.inputField.setText(mAscii.mGLView.mRenderer.inputField.getText()+"123");
        //mAscii.mGLView.mRenderer.inputField.setText("1231231");
        /*switch (keyCode) {
            case KeyEvent.KEYCODE_D:

                return true;
            case KeyEvent.KEYCODE_F:

                return true;
            case KeyEvent.KEYCODE_J:

                return true;
            case KeyEvent.KEYCODE_K:

                return true;
            default:
                return super.onKeyUp(keyCode, event);
        }*/
        //return true;
        return super.onKeyDown(KeyCode,event);
    }

    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private float mPreviousX;
    private float mPreviousY;

    /**
     * Here GL passes touch actions to the activity
     * move, press, release
     * @param e motion event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent e) {


        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        //Log.d("ASCII",""+e.get());

        float x = (e.getX()/mMetrics.widthPixels*2)-1.0f;
        float y = 1.0f-(e.getY()/mMetrics.heightPixels*2);

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:

                int action=mRenderer.holdAndMove(x,y);
                if(action!=0)mMotionEngaged=true;
                e.setAction(action);
                //actContext.onTouchEvent(e);
                Log.d("ASCII", "ACT:"+action);
                //if(mRenderer.holdAndMove(x,y)==2)
                break;
            case MotionEvent.ACTION_DOWN:
                mRenderer.setClick(x,y);
                //actContext.onTouchEvent(e);
                break;
            case MotionEvent.ACTION_UP:
                /*if(mRenderer.releaseClick(x,y))
                {
                    mMotionEngaged=false;
                    e.setAction(0);
                    Log.d("ASCII","ACT0");
                    actContext.onTouchEvent(e);
                }*/
                int clickres=mRenderer.getClick(x,y);
                Log.d("ASCII","GOT CLICK "+clickres);
                if(clickres==1 && !mMotionEngaged)
                {
                    e.setAction(3);
                    Log.d("ASCII","ACT3");
                    actContext.onTouchEvent(e);
                    break;
                }
                if(clickres==7 && !mMotionEngaged)
                {
                    e.setAction(7);
                    Log.d("ASCII","ACT7");
                    actContext.onTouchEvent(e);
                    break;
                }
                if(clickres==6 && !mMotionEngaged)
                {
                    e.setAction(6);
                    Log.d("ASCII","ACT6");
                    actContext.onTouchEvent(e);
                    break;
                }
                else if(clickres==2)
                {
                    e.setAction(5);
                    Log.d("ASCII","ACT5");
                    actContext.onTouchEvent(e);
                    break;
                }
                else
                {
                    e.setAction(clickres);
                    Log.d("ASCII","ACTD"+clickres);
                    actContext.onTouchEvent(e);
                    break;
                }

        }

        mPreviousX = x;
        mPreviousY = y;
        return true;
    }

    /**
     * not used anymore
     * was used to put line of text inbetween ascii characters
     * @param str text
     * @param row offset from top
     * @param pos offset from right
     */
    public void putString(String str,int row,int pos)
    {
        Log.d("ASCII","line:"+str);
        mRenderer.putString(str, row, pos);
    }

    /**
     * put image as ascii
     * @param bitmap
     */
    public void putImage(Bitmap bitmap)
    {
        int h=bitmap.getHeight();
        int w=bitmap.getWidth();

        float rb=(float)w/(float)h;
        float ra=(float)mRenderer.asciicols/(float)mRenderer.asciirows;
        int nw=1;
        int nh=1;
        if(rb>ra)
        {
            nw=mRenderer.asciicols;
            nh=(int)((float)nw/rb);
        }
        else
        {
            nh=mRenderer.asciirows;
            nw=(int)(rb*(float)nh);
        }
        Log.d("ASCII",mRenderer.asciirows+":"+mRenderer.asciicols+","+nw+","+nh+","+rb);

        /*double y = Math.sqrt(
                / (((double) w) / h));
        double x = (y / h) * w;*/


        mRenderer.putImage(Bitmap.createScaledBitmap(bitmap, nw,nh,true),true);
        bitmap.recycle();
    }



}


//OLD ASCII SET
//TODO: remove

/*
class AsciiCharSet extends Charset {*/
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
    /*protected AsciiCharSet(String canonicalName, String[] aliases) {
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
                int a;*/
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
                /*while(in.remaining()>0)
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
*/

