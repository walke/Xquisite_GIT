package no.ice_9.xquisite;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * Created by HUMAN on 15.10.2015.
 */
public class ASCIIscreen {

    Display display;
    DisplayMetrics displayMetrics;

    TextView mText;

    static private int lineCount=50;
    private float lineHeight;

    String line[];
    String allLines;

    public ASCIIscreen(Context context,TextView text)
    {
        line=new String[lineCount];


        mText=text;
        display = ((WindowManager)context.getSystemService(context.WINDOW_SERVICE)).getDefaultDisplay();
        displayMetrics=new DisplayMetrics();
        display.getMetrics(displayMetrics);

        lineHeight=displayMetrics.heightPixels/lineCount;
        Log.d("ASCII","dispH,lineH:"+displayMetrics.heightPixels+" "+ lineHeight);

        for(int i=0;i<lineCount;i++)
        {
            line[i]="001010001010 "+i;
        }

        allLines="";
        for(int i=0;i<lineCount;i++)
        {
            allLines+=line[i]+"\n";
        }

        mText.setText(allLines);

        mText.setTextSize(TypedValue.COMPLEX_UNIT_PX, lineHeight);
        float a = 100.0f/120.0f;
        mText.setLineSpacing(0.0f, a);

        Log.d("ASCII", "real size" + mText.getExtendedPaddingTop());
    }

    public float getTextSize()
    {
        return lineHeight;
    }
}
