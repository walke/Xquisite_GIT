package no.ice_9.xquisite;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.view.View;
import android.widget.EditText;

import java.util.Calendar;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by human on 16.03.16.
 */
public class XQGLRenderer implements GLSurfaceView.Renderer {

    public EditText inputField;

    private float infoStatus=0.0f;
    private float infoTarget=0.0f;
    private float infoHeight=1.0f;
    private float infoTop=0.0f;


    private float activeLine=0;
    private float activeLineTarget=0;

    private float progress=0.0f;
    private float progressTarget=0.0f;

    private float slider=0.0f;
    private float sliderTarget=0.0f;
    private boolean mSlidGrub=false;


    private float mRatio=1f;


    float[] scratch;// = new float[16];
    float[] scratch2;// = new float[16];
    float[] scratch3;// = new float[16];
    float[] scratch4;// = new float[16];
    float[] scratch5;// = new float[16];
    float[] scratch6;// = new float[16];

    float[] mtx = new float[16];



    //DEBUG TIME MEASURE
    long mMesTime=0;
    long mLasTime=0;


    //GL OBJECTS
    private AsciiTiles mAsciiTiles;
    private InfoTile mInfoTile;
    private TextLine[] mTextLine;
    private ButtonTile mContinueButton;
    private AudioTile mAudioTile;
    private ProgressTile mProgressTile;
    private NetworkLed mNetworkLed;
    private SliderInfoTile mSliderInfo;


    //TEXTURES
    public int[] textures = new int[5];
    Bitmap mBitmap;
    public SurfaceTexture mSurface;

    public Context actContext;

    //ANIMS
    public boolean upAval;
    public boolean upVid;
    private float mAngle;


    //DIMENSIONS
    public int asciicols;
    public int asciirows;

    //INTERFACE
    public ASCIIscreen view;


    boolean mRecSequence=false;


    private boolean clearDone=true;

    private final float[] mMVPMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mTranslationMatrix = new float[16];

    @Override
    public void onSurfaceCreated(GL10 gl, javax.microedition.khronos.egl.EGLConfig config) {

        mAngle=0f;

        inputField = new EditText(actContext);

        //this.actContext.
        //Looper.prepare();
        //inputField.requestFocus();
        //time measure
        /*mMesTime= Calendar.getInstance().getTimeInMillis();
        mLasTime=mMesTime;*/



        /*RENDER INIT*/
        GLES20.glClearColor(0.0f, 0.3f, 0.0f, 0.5f);

        /*TEXTURES INIT*/
        initTextures();

        int sx=asciicols;
        int sy=asciirows;

        mRatio=(float)sx/(float)sy;

        mMesTime= Calendar.getInstance().getTimeInMillis();
        Log.d("TIME","  GLREND TX2 INIT "+(mMesTime-mLasTime)+"ms");
        mLasTime=mMesTime;



        mAsciiTiles =       new AsciiTiles(sx,sy,textures[0],textures[1],textures[2]);

        mTextLine=          new TextLine[sy];

        mContinueButton =   new ButtonTile(0.0f,-0.45f,0.2f,0.2f*mRatio,textures[3]);

        mAudioTile =        new AudioTile();

        mProgressTile =     new ProgressTile();

        mNetworkLed=        new NetworkLed(-0.95f,0.95f,0.02f,0.02f*mRatio,textures[3]);

        mSliderInfo=        new SliderInfoTile(0.0f,-0.45f,0.5f,0.1f,textures[4]);




        for(int j=0;j<sy;j++)
        {


            mTextLine[j] = new TextLine(j,textures[0]);
        }



        /*INFO TILE BUILD*/
        mInfoTile=new InfoTile();



        view.mReady=true;

    }




    public void onDrawFrame(GL10 unused) {
        // Redraw background color
        //Random rnd=new Random();
       // view.mReady=true;
 //
        //inputField.requestFocus();
        putString(inputField.getText().toString(),0,0);
        if(inputField.getText().length()>0)
        {

        }


        /*int l=0;
        for(int j=0;j<asciirows;j++)
        {
            for(int i=0;i<asciicols;i++)
            {
                int ndx = i+(j*asciicols);// (asciicols*(asciirows*(j-1))-i-1);
                mTile[ndx].putChar("helasdlo woasdasdfasdasfdafssdfgsdfsdfssdrld".charAt(j));
                l++;
            }
        }*/
        //mAngle+=0.01f;
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        updateAval();
        animInfo();

        // Set the camera position (View matrix)
        //Matrix.setLookAtM(mViewMatrix, 0, 0, 1, -1f - mAngle, 0f, -1f, 0f, 0f, 1f, 0f);
        // Calculate the projection and view transformation
        //Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        //Matrix.translateM(mTranslationMatrix, 0, 0f, 0f, -2f);
        Matrix.setIdentityM(mTranslationMatrix, 0);
        scratch2=mTranslationMatrix.clone();
        scratch3=mTranslationMatrix.clone();
        scratch=mTranslationMatrix.clone();
        //scratch4=mTranslationMatrix.clone();
        scratch5=mTranslationMatrix.clone();
        scratch6=mTranslationMatrix.clone();
        Matrix.translateM(mTranslationMatrix, 0, mInfoTile.midx, mInfoTile.midy, 0f);


        Matrix.scaleM(mTranslationMatrix, 0, mInfoTile.sizx, mInfoTile.sizy, 1.0f);

        //Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mTranslationMatrix, 0);

        //int sx=asciicols;
        //int sy=asciirows;
        //int k=0;

        //ASCII TILES
        mAsciiTiles.draw();
        /*for(int j=0;j<sy;j++)
        {
            for(int i=0;i<sx;i++)
            {
                mTile[k].draw();
                k++;
            }
        }*/



        //TEXT BACKGROUND
        mInfoTile.draw(mTranslationMatrix);

        //PROGRESS BAR
        /*Matrix.translateM(scratch4, 0, mProgressTile.midx, mProgressTile.midy, 1.0f);
        Matrix.scaleM(scratch4, 0, mProgressTile.sizx+progress*2f, mProgressTile.sizy, 0.0f);

        mProgressTile.draw(scratch4);*/

        //TEXT LINES
        float hoffset=0.0f;
        float tothoffset=0.0f;
        for(int j=0;j<activeLine+1.0f;j++)
        {tothoffset+=mTextLine[j].mLineCount*0.07f;}
        Matrix.translateM(scratch, 0, 0.0f, tothoffset + 0.05f * activeLine - 1.75f, 0.0f);
        for(int j=0;j<asciirows;j++)
        {
            if(!mTextLine[j].isEmpty())
            {
                Matrix.translateM(scratch, 0, 0f, -0.05f-hoffset*0.07f, 0f);
                mTextLine[j].draw(scratch);
                hoffset=mTextLine[j].mLineCount;
            }

        }

        //SLIDER INFO
        if(mRecSequence)
        {
            Matrix.translateM(scratch5, 0, mSliderInfo.midx, mSliderInfo.midy, 1.0f);
            Matrix.scaleM(scratch5, 0, mSliderInfo.sizx, mSliderInfo.sizy, 0.0f);

            mSliderInfo.draw(scratch5);
        }


        //BUTTON
        mContinueButton.midx=slider;
        Matrix.translateM(scratch2, 0, mContinueButton.midx, mContinueButton.midy, 1.0f);
        Matrix.scaleM(scratch2, 0, mContinueButton.sizx, mContinueButton.sizy, 0.0f);

        mContinueButton.draw(scratch2);



        //AUDIO BAR
        Matrix.translateM(scratch3, 0, mAudioTile.midx, mAudioTile.midy, 1.0f);
        Matrix.scaleM(scratch3, 0, mAudioTile.sizx, mAudioTile.sizy, 0.0f);

        mAudioTile.draw(scratch3);

        //NETWORK LED
        Matrix.translateM(scratch5, 0, mNetworkLed.midx, mNetworkLed.midy, 1.0f);
        Matrix.scaleM(scratch5, 0, mNetworkLed.sizx, mNetworkLed.sizy, 0.0f);
        mNetworkLed.draw(scratch5);
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;
        mRatio=ratio;



        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    public void updateAval()
    {
        //mSurface.updateTexImage();
        //mSurface.
        if(upVid)
        {
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[2]);
            mSurface.updateTexImage();
            mSurface.getTransformMatrix(mtx);

            //GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 1);
        }
        if (upAval)
        {


            //GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 1);

            //Log.d("GL","bpup"+mBitmap.getPixel(0,0));
            //Log.d("GL", "upval");
            //GLES20.glGenTextures(1, textures, 1);

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[1]);
            // Set filtering
            // GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            // GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

            // Load the bitmap into the bound texture.
            //GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, mBitmap);
            GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0,0,0, mBitmap);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 1);




            upAval=false;
        }
    }

    private void animInfo()
    {
        if(infoStatus!=infoTarget)

        {


            infoStatus+=(infoTarget-infoStatus)/10.0f;

            mInfoTile.sizy=(infoStatus+0.2f)*2f;



            mContinueButton.midy=infoStatus*0.8f+(1.0f-infoStatus)*-0.5f;




        }

        if (activeLine!=activeLineTarget)
        {
            activeLine+=(activeLineTarget-activeLine)/10.0f;
        }

       /* if (progress!=progressTarget)
        {
            progress+=(progressTarget-progress)/10.0f;
        }*/

        if(slider!=sliderTarget)
        {
            slider+=(sliderTarget-slider)/10.0f;
        }
    }

    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    private void initTextures()
    {
        upAval=true;
        upAval=false;

        //TEXTURE 0
        mBitmap = Bitmap.createBitmap(asciicols,asciirows, Bitmap.Config.ARGB_8888 );


        int textGridTex=loadTexture(actContext,R.drawable.textgrid);
        textures[0]=textGridTex;

        mMesTime= Calendar.getInstance().getTimeInMillis();
        Log.d("TIME","  GLREND TX0 INIT "+(mMesTime-mLasTime)+"ms");
        mLasTime=mMesTime;

        Log.d("ASCII", "tex" + textGridTex);



        //GLES20.glGenTextures(1, textures, 1);
        //mBitmap = Bitmap.createBitmap(asciicols,asciirows, Bitmap.Config.ARGB_8888 );

        //GLES20.glGenTextures(2, textures, 2);

        //Random r= new Random();

        for(int i=0;i<asciicols;i++)
        {
            for(int j=0;j<asciirows;j++)
            {
                //mBitmap.setPixel(i,j, Color.argb(r.nextInt(256), r.nextInt(256),r.nextInt(256),r.nextInt(256)));
                mBitmap.setPixel(i, j, Color.argb(0, 0, 0, 0));
                //Log.d("GL","PX:"+mBitmap.getPixel(i,j));
            }
        }



        //TEXTURE 1
        GLES20.glGenTextures(1, textures, 1);


        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[1]);
        // Set filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0);

        //bitmap.recycle();

        int screenTileValue=textures[1];

        //TEXTURE 2
        GLES20.glGenTextures(1, textures, 2);
        //mSurface.setUseExternalTextureID();
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[2]);

        // Set filtering
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        //GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);




        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_REPEAT);



        mSurface = new SurfaceTexture(textures[2]);
        mSurface.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                //surfaceTexture.updateTexImage();


                upVid = true;
            }
        });

        int videoTex=textures[2];

        //TEXTURE 3
        mBitmap = Bitmap.createBitmap(asciicols,asciirows, Bitmap.Config.ARGB_8888 );


        int butMaskTex=loadTexture(actContext,R.drawable.continuebutfull);
        textures[3]=butMaskTex;

        //TEXTURE 4
        mBitmap = Bitmap.createBitmap(asciicols,asciirows, Bitmap.Config.ARGB_8888 );


        int SliderMaskTex=loadTexture(actContext,R.drawable.slider);
        textures[4]=SliderMaskTex;










    }

    public static int loadTexture(Context context,final int resourceId)
    {
        final int[] textureHandle = new int[1];

        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0)
        {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;   // No pre-scaling

            // Read in the resource
            final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

            // Bind to the texture in OpenGL
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

            // Set filtering
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

            // Recycle the bitmap, since its data has been loaded into OpenGL.
            bitmap.recycle();
        }

        if (textureHandle[0] == 0)
        {
            throw new RuntimeException("Error loading texture.");
        }

        return textureHandle[0];
    }

    public float getAngle() {
        return mAngle;
    }

    public void setAngle(float angle) {
        mAngle = angle;
    }

    public void putImage(Bitmap bitmap)
    {
        Random r = new Random();
        for(int i=0;i<asciicols;i++)
        {
            if(i<bitmap.getWidth())
            {
                for (int j = 0; j < asciirows; j++)
                {
                    if(j<bitmap.getHeight())
                    {
                        //mBitmap.setPixel(i, j, Color.argb(r.nextInt(), 0, 0, 255));
                        mBitmap.setPixel(i, j, bitmap.getPixel(i, j));
                    }
                }
            }
        }
        bitmap.recycle();
        //mBitmap=bitmap.copy(Bitmap.Config.ARGB_8888,true);

        upAval=true;
    }

    public void putMsgString(String str, int row)
    {
        if(row<mTextLine.length)
        {
            mTextLine[row].set(str);
            activeLineTarget=(float)row;
        }

    }

    public void hideMsgString(int row)
    {
        if(row<mTextLine.length)
        {
            mTextLine[row].set("");

        }

    }

    public void putString(String str, int row, int pos)
    {

        if(row<asciirows &&/* pos+str.length()<asciicols &&*/ view.mReady)
        {

            //GLES20.glGenTextures(1, textures, 1);
            //Bitmap bitmap = Bitmap.createBitmap(asciicols,asciirows, Bitmap.Config.ARGB_8888 );

            Random r= new Random();

            //Log.d("ASCII", "AS" + row + " :" + pos + "__" + str.length());
            for(int i=0;i<str.length();i++)
            {
                int ndx = ((asciicols*(row))+(pos))+i;
                //Log.d("ASCII","CC"+ndx+ ":"+mTile.length);



                if(ndx>0 && ((pos+i)%asciicols)>=0)
                {

                    mBitmap.setPixel((pos+i)%asciicols, row, Color.argb(str.charAt(i), 0, 0, 255));
                    //mBitmap.setPixel(1, 0, Color.argb(r.nextInt(256), r.nextInt(256), r.nextInt(256), 255));
                    //Log.d("ASCII", "CC" + mTile[ndx] + ":" + mTile.length);
                    //mTile[ndx].putChar(str.charAt(i));
                }


            }
            //Log.d("GL","bp"+mBitmap.getPixel(0,0));
            upAval=true;
            //GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[1]);
            // Set filtering
            // GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            // GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

            // Load the bitmap into the bound texture.
            //GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, bitmap);


            //int screenTileValue=textures[1];
            //bitmap.recycle();
        }
    }

    public void clearAscii()
    {
        /*clearDone=false;
        TimerTask clearTrhead=new TimerTask() {
            @Override
            public void run() {
                clearDone=true;
                for(int i=0;i<asciicols;i++)
                {
                    for(int j=0;j<asciirows;j++)
                    {
                        int px=mBitmap.getPixel(i,j);
                        if (px>0){clearDone=false;}
                        mBitmap.setPixel(i,j,0);
                    }
                }
                if(clearDone){this.cancel();}
                else{upAval=true;}
            }
        };
        new Timer().scheduleAtFixedRate(clearTrhead, 0, 5);*/

    }

    public void minimizeInfo()
    {
        infoTarget=0.0f;
    }

    public void maximizeInfo()
    {
        infoTarget=1.0f;
    }

    public void setClick(float x, float y)
    {

        if (x>mContinueButton.midx-(mContinueButton.sizx/1.0f) &&
                x<mContinueButton.midx+(mContinueButton.sizx/1.0f) &&
                y>mContinueButton.midy-(mContinueButton.sizy/1.0f) &&
                y<mContinueButton.midy+(mContinueButton.sizy/1.0f))
        {
            mContinueButton.setDown();
            mSlidGrub=true;
        }

    }
    public  boolean releaseClick(float x, float y)
    {
        mSlidGrub=true;
        sliderTarget=0.0f;
        if(slider>0.26 || slider<-0.26){return true;}

        return false;
    }

    public boolean getClick(float x, float y)
    {

        if(mContinueButton.isDown)
        {
            if (x > mContinueButton.midx - (mContinueButton.sizx / 1.0f) &&
                    x < mContinueButton.midx + (mContinueButton.sizx / 1.0f) &&
                    y > mContinueButton.midy - (mContinueButton.sizy / 1.0f) &&
                    y < mContinueButton.midy + (mContinueButton.sizy / 1.0f))
            {
                mContinueButton.setUp();
                return true;
            }
            mContinueButton.setUp();
        }
        return false;
    }

    public int holdAndMove(float x,float y)
    {

        if (mSlidGrub && mRecSequence)
        {
            if(x>-0.3f && x<0.3f)
            {
                slider=x;
                sliderTarget=x;
            }
            else if(x<-0.3f)
            {
                slider=-0.3f;
                sliderTarget=-0.3f;
            }
            else if(x>0.3f)
            {
                slider=0.3f;
                sliderTarget=0.3f;
            }

            if(slider>=0.26f){return 1;}

            if(slider<=-0.26f){return 2;}

        }



        return 0;
    }

    public void setAudio(int value)
    {
        float flValue=(float)value/10000.0f;
        mAudioTile.sizy=flValue/10;
        //mAudioTile.color[0]=flValue;
        //mAudioTile.color[1]=1.0f-flValue;

    }

    public void setProgress(float value,int state)
    {

        if(state==0)
        {
            //mProgressTile.color[0]=0.0f;
            //mProgressTile.color[1]=1.0f;
        }
        else
        {
            progressTarget=value;
            //mProgressTile.color[0]=1.0f;
            //mProgressTile.color[1]=0.0f;
        }

    }

    public void setRecording(boolean isRec)
    {
        mContinueButton.isRecording=isRec;
    }

    public void setLed(boolean onin,boolean load)
    {
        if(mNetworkLed!=null) {
            mNetworkLed.setLed(onin);
            if(load)
            {
                mNetworkLed.setLedLoad();
            }
        }
    }

    public void setRecSequence(boolean rec)
    {
        mRecSequence=rec;
    }


}