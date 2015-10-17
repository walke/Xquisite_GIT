package no.ice_9.xquisite;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * Created by HUMAN on 17.10.2015.
 */
public class Preview extends SurfaceView implements SurfaceHolder.Callback
{
    SurfaceHolder mHolder;
    Camera mCamera;

    Preview(Context context,Camera camera)
    {
     super(context);
        mCamera = camera;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();

        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            Log.d("VIDEO_LOG", "Setting preview display ");
            mCamera.setPreviewDisplay(holder);
            Log.d("VIDEO_LOG", "Starting preview ");
            mCamera.startPreview();
        } catch (IOException e) {
            Log.e("VIDEO_LOG", "Error setting camera preview: " + e.getMessage());
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        Log.d("VIDEO_LOG", "Surface changed ");
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {

            Camera.Size pSize=mCamera.getParameters().getPreviewSize();

            mHolder.setFormat(format);
            mHolder.setFixedSize(pSize.width,pSize.height);

            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e){
            Log.d("VIDEO_LOG", "Error starting camera preview: " + e.getMessage());
        }


    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }
    }
}