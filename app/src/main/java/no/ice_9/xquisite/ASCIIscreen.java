package no.ice_9.xquisite;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.graphics.BitmapCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by HUMAN on 15.10.2015.
 */
public class ASCIIscreen {

    //SCREEN VARIABLES
    Display display;
    DisplayMetrics displayMetrics;

    TextView mText;

    //STATIC LINE NUMBER
    static private int lineCount=30;
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



    public ASCIIscreen(Context context,TextView text)
    {


        mAsciiCharSet=new AsciiCharSet("ASCII",null);
        mUpdating=false;
        mLine=new String[lineCount];
        mLinePointer=0;
        tAct=(Activity)context;
        mText=text;
        display = ((WindowManager)context.getSystemService(context.WINDOW_SERVICE)).getDefaultDisplay();
        displayMetrics=new DisplayMetrics();
        display.getMetrics(displayMetrics);
        mRage=false;
        mRequestStop=false;

        //WORDS TO BE USED
        mWordList=new String[]{"science","life","corruption","future","source","utopia","time","order","chaos"};



        lineHeight=displayMetrics.heightPixels/lineCount;
        //Log.d("ASCII", "dispH,lineH:" + displayMetrics.heightPixels + " " + lineHeight);

        mText.setTextSize(TypedValue.COMPLEX_UNIT_PX, lineHeight);
        float a = 100.0f/120.0f;
        mText.setLineSpacing(0.0f, a);

        mSymbolsPerLine=-1;



        mText.post(new Runnable()
        {
            @Override
            public void run()
            {

                Log.d("ASCII", "run");
                boolean done = false;
                String tmpstr = "";
                int i = 0;
                while (!done)
                {
                    mText.setText(tmpstr);
                    tmpstr += "#";
                    if (mText.getLineCount() == 2)
                    {
                        done = true;
                        mSymbolsPerLine = i - 1;
                        //Log.d("ASCII", "symi" + mSymbolsPerLine);

                        Random rnd=new Random();
                        mTrash=new String[100];
                        for(int k=0;k<100;k++)
                        {
                            String tmpStr="";
                            //rnd.nextBytes(buf);

                            for(int l = 0;l<mSymbolsPerLine;l++)
                            {
                                tmpStr+=(char)(rnd.nextInt(223)+32);
                            }
                            //tmpStr=new String(buf);
                            mTrash[k]=tmpStr;



                        }
                        mReady=true;
                    }
                    i++;

                }
            }
        });


        /*while(mSymbolsPerLine==-1)
        {
            Log.d("ASCII","wait");
        }*/

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


        //Log.d("ASCII","runAQ"+3);
        //Log.d("ASCII", "real size" + mText.getExtendedPaddingTop());
        createUpdater();


        //mAsciiStartUpdater(50);
    }

    private void createUpdater()
    {
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

                        mText.setText(mAllLines + "a");
                    }
                });

                if(mRequestStop){ mUpdater.cancel();}
                Log.d("ASCII","RUNNING");

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

    public void mAsciiStopUpdater()
    {
        mUpdater.cancel();
        mRequestStop=true;
        mUpdating=false;
    }

    public float getTextSize()
    {
        return lineHeight;
    }

    public void pushLine(String line)
    {
        int i;
        if(mLinePointer<lineCount)
        {
            mLine[mLinePointer]=line;
            mLinePointer++;
        }
        else
        {
            for(i=0;i<lineCount-1;i++)
            {

                mLine[i]=mLine[i+1];
            }
            mLine[i]=line;
        }

    }

    public void modLine(String line, int ndx,int pos)
    {
        //Log.d("ASCII","mll"+mLine[ndx].length());
        if(ndx<mLine.length )
        {

            if(pos==-1)
            {
                mLine[ndx]=line;
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

                mLine[ndx]=String.copyValueOf(tmpCh);
            }
        }


    }

    public void putImage(Bitmap btm)
    {
        /*Uri uri=Uri.parse("/mnt/sdcard/tmp/tmp.jpg");
        Bitmap btm=BitmapFactory.decodeFile("/mnt/sdcard/tmp/tmp.jpg");
        Log.d("ASCII", "bm" + btm.getByteCount());*/

        //OutputStream os=new ByteArrayOutputStream(256);
        //btm.compress(Bitmap.CompressFormat.JPEG, 0, os);





        Bitmap btm2;
        Log.d("ASCII","W,H"+btm.getWidth()+", "+btm.getHeight());

        btm2=Bitmap.createScaledBitmap(btm, mSymbolsPerLine,lineCount, false);

        ByteBuffer mChBuff = ByteBuffer.allocate(btm2.getByteCount());
        btm2.copyPixelsToBuffer(mChBuff);



        /*String sttm="";
        for(int i=0;i<32;i++)
        {
            sttm+=(char)i;
        }
        CharSequence chseq=new String(sttm);*/
        String s;
        for(int i=0;i<lineCount;i++)
        {

            s=new String(mChBuff.array(),i*mSymbolsPerLine*4,mSymbolsPerLine*4, mAsciiCharSet);
            s=s.replace("\n", "#");
            //String s=String.copyValueOf(str);

            modLine(s, i, -1);
        }
    }




    public void fillTrash()
    {



        if(mReady)
        {
            int i=0;
            Random rnd=new Random();
            for(i=0;i<lineCount;i++)
            {




                //tmpStr=new String(buf);
                mLine[i]=mTrash[rnd.nextInt(100)];
            }
            mLinePointer=i;
        }

    }

    public void setRage(boolean on)
    {
        if(on){mRage=true;}
        else{mRage=false;}
    }

    public void clear()
    {
        mReady=false;
        for(int i=0;i<lineCount;i++)
        {

            mLine[i]="";
        }
        mLinePointer=0;
        mReady=true;
    }

    public boolean isRage()
    {
        return mRage;
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
