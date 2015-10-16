package no.ice_9.xquisite;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by HUMAN on 15.10.2015.
 */
public class ASCIIscreen {

    Display display;
    DisplayMetrics displayMetrics;

    TextView mText;

    static private int lineCount=50;
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

    public ASCIIscreen(Context context,TextView text)
    {
        mLine=new String[lineCount];
        mLinePointer=0;
        tAct=(Activity)context;
        mText=text;
        display = ((WindowManager)context.getSystemService(context.WINDOW_SERVICE)).getDefaultDisplay();
        displayMetrics=new DisplayMetrics();
        display.getMetrics(displayMetrics);
        mRage=false;

        mWordList=new String[]{"science","life","corruption","future","source","utopia","time","order","chaos"};


        lineHeight=displayMetrics.heightPixels/lineCount;
        Log.d("ASCII", "dispH,lineH:" + displayMetrics.heightPixels + " " + lineHeight);

        mText.setTextSize(TypedValue.COMPLEX_UNIT_PX, lineHeight);
        float a = 100.0f/120.0f;
        mText.setLineSpacing(0.0f, a);

        mSymbolsPerLine=-1;



            mText.post(new Runnable() {
                @Override
                public void run() {

                    Log.d("ASCII", "run");
                    boolean done = false;
                    String tmpstr = "";
                    int i = 0;
                    while (!done) {
                        mText.setText(tmpstr);
                        tmpstr += "#";
                        if (mText.getLineCount() == 2) {
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
        final Random Rnd=new Random();
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                //if(mSymbolsPerLine!=-1 && !mReady){mReady=true;}
                if(mRage){pushLine("&/¤(&/"+mWordList[Rnd.nextInt()%4+4]+"2/(&%¤76KLJ))=/(¤");}
                mAllLines="";
                for(int i=0;i<lineCount;i++)
                {

                    mAllLines+=mLine[i]+"\n";
                }

                tAct.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        mText.setText(mAllLines+"a");
                    }
                });


            }
        },0,50);
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
