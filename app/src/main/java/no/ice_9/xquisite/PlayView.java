package no.ice_9.xquisite;

import android.content.Context;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

/**
 * Created by human on 23.03.16.
 */
public class PlayView extends SurfaceView implements SurfaceHolder.Callback
{
    SurfaceHolder mHolder;
    MediaPlayer mPlayer;

    boolean ready=false;


    PlayView(Context context,MediaPlayer player)
    {
        super(context);

        mPlayer=player;
        mHolder = getHolder();

        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        Log.d("PLAYER","surf created");
        mPlayer.setDisplay(holder);
        ready=true;

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        Log.d("VIDEO_LOG", "Surface changed "+format+","+width+","+height);
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }



        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings

            //Camera.Size pSize=mCamera.getParameters().getPreviewSize();


        mHolder.setFormat(format);







    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);



    }


}