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

        lineHeight=displayMetrics.heightPixels/lineCount;
        Log.d("ASCII","dispH,lineH:"+displayMetrics.heightPixels+" "+ lineHeight);

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

        mText.setTextSize(TypedValue.COMPLEX_UNIT_PX, lineHeight);
        float a = 100.0f/120.0f;
        mText.setLineSpacing(0.0f, a);

        Log.d("ASCII", "real size" + mText.getExtendedPaddingTop());

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                /*Random R=new Random();
                for(int i=0;i<lineCount;i++)
                {
                    mLine[i]=""+(R.nextInt()%2+1)/2+(R.nextInt()%2+1)/2+(R.nextInt()%2+1)/2+(R.nextInt()%2+1)/2+(R.nextInt()%2+1)/2+(R.nextInt()%2+1)/2+(R.nextInt()%2+1)/2;
                }*/
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
        },0,100);
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
}
