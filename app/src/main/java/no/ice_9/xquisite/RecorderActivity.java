package no.ice_9.xquisite;

import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Timer;
import java.util.TimerTask;

public class RecorderActivity extends AppCompatActivity {

    //CLASS VARIABLES
    Camera mCamera;//Deprecated.. don't know yet what to do about it
    Preview mPreview;

    //INIT CAMERA
    private boolean initCamera(int camId)
    {
        boolean result;
        if(mCamera==null)
        {



            //try to get camera instance
            mCamera=getCameraInstance(camId);
            if(mCamera==null){return false;}
            else
            {
                Log.d("VIDEO_LOG", "got Camera instance");
            }

            //try to get preview
            result = initPreview();
            if(!result){return false;}
            else{Log.d("VIDEO_LOG","got Preview");}

            mTimeLeft=10;



            new Timer().scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    mTimeLeft--;
                    if(mTimeLeft<=0){this.cancel();}
                    tAct.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mRecorderTimeText.setText(""+mTimeLeft);

                            if(mTimeLeft<=0){forceStartCapture();}
                        }
                    });



                }
            },0,1000);




        }
        return true;
    }

    //INIT PREVIEW
    private void releasePreview()
    {

        if (mPreview != null) {
            Log.d("RECORDER", "releasing preview ");
            mPreview = null;
            Log.d("RECORDER", "preview released ");

        }
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(int camId){
        Camera c = null;
        try {
            c = Camera.open(camId); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorder);

        //INIT CAMERA AND ALL IT DEPENDS ON
        initCamera(1);//for now camId = 1; asuming front facing camera.
        // Will later be fixed to search for frontfacing camera
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_recorder, menu);
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
}
