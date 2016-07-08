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

    public static final int MODE_INIT=0;
    public static final int MODE_REC =1;
    public static final int MODE_PLAY=2;
    public static final int MODE_INPT=3;
    public static final int MODE_CHOS=4;

    class MsgLines
    {
        int maxChars=0;
        int mMaxRows;
        TextLine[] mLine;
        InfoTile bg;
        float mActiveLine=0;
        float activeLineTarget=0;
        int visibleLines=4;
        public MsgLines(int maxRows)
        {
            mMaxRows=maxRows;
            mLine=new TextLine[maxRows];
            bg=new InfoTile();

            for(int j=0;j<maxRows;j++)
            {


                mLine[j] = new TextLine(j,textures[0]);
            }
        }

        public void setLine(String str, int row,boolean active)
        {
            if(maxChars<str.length())
            {
                maxChars=str.length();
            }

            if(str.length()<maxChars){for(int i=0;i<(maxChars-str.length());i++){str=str+(char)0;}}
            mLine[row].set(str);
            if(active)
            {
                Log.d("ASCII","setline active"+row);
                //mLine.setTargetLine(row);
                mActiveLine=row;

            }


        }

        public void setVisibleLines()
        {

        }

        public void setPosition(int top)
        {
            if(top==0)
            {
                bg.setTargetShape(0.0f,-0.6f,1.0f,0.4f);
            }
            else if(top==1)
            {
                bg.setTargetShape(0.0f,1.0f,1.0f,0.4f);
            }
        }

        public void clear()
        {
            for(int j=0;j<mMaxRows;j++)
            {


                mLine[j].set("");
                mLine[j].empty=true;
            }
        }

        public void draw()
        {
            Matrix.translateM(mTranslationMatrix, 0, bg.midx, bg.midy, 0f);
            scratch=mTranslationMatrix.clone();//TEXTLINES

            Matrix.scaleM(mTranslationMatrix, 0, bg.sizx, bg.sizy, 1.0f);

            bg.draw(mTranslationMatrix);

            float hoffset=0.0f;
            float tothoffset=0.0f;
            for(int j=0;j<mActiveLine+1.0f;j++)
            {tothoffset+=mLine[j].mLineCount*0.07f;}
            float prevOffset=0.1f;
            //Log.d("ASCII","act line:"+activeLine);
            float a1=tothoffset + prevOffset * mActiveLine - 1.0f;
            Matrix.translateM(scratch, 0, 0.0f, a1, 0.0f);
            for(int j=0;j<mMaxRows;j++)
            {
                if(!mLine[j].isEmpty())
                {
                    float a2=-prevOffset-hoffset*0.07f;
                    //Log.d("ASCII","Line"+a1+"+"+a2+"="+(a1+a2));
                    //Log.d("ASCII","Line"+mActiveLine+" "+mLine[0].mLineCount);
                    Matrix.translateM(scratch, 0, 0f, a2, 0f);

                    mLine[j].draw(scratch);
                    hoffset=mLine[j].mLineCount;
                }

            }
        }
    }

    class InputBox
    {
        public float midx=0.0f;
        public float midy=0.0f;
        public float siz=1.0f;

        int mMaxRows;
        int mMaxCols;


        int currentLine=0;
        String[] line;
        boolean ready=false;
        public InputBox(int rows,int cols)
        {
            mMaxRows=rows;
            mMaxCols=cols;
            line=new String[1];
            line[0]="";
            ready=true;
        }
        public String getLine(int ndx)
        {
           //Log.d("ASCII",""+ndx+""+line);
            if(line==null){return null;}

            if(line.length<=ndx){return null;}

            if(line[ndx]==null){return null;}

            return line[ndx];


        }



        public void setText(CharSequence text)
        {
            Log.d("ASCII","setting text");
            ready=false;
            int linecounter=1;
            for(int i=0;i<text.length();i++)
            {
                if(text.charAt(i)==10){
                    linecounter++;
                    currentLine=linecounter-1;
                }
            }

            line=new String[linecounter];

            int last=0;
            int lineP=0;

            int i;
            for(i=0;i<text.length();i++)
            {
                if(text.charAt(i)==10)
                {
                    line[lineP]=text.subSequence(last,i).toString();
                    last=i+1;
                    lineP++;

                }
            }

            line[lineP]=text.subSequence(last,i).toString();

            int maxchars=0;
            for(i=0;i<linecounter;i++)
            {
                if(line[i].length()>maxchars){maxchars=line[i].length();}
            }

            float LvsCH= (float)maxchars/(float)linecounter;
            float ScrAsp= (float)mMaxCols/((float)mMaxRows/3.0f);

            Log.d("ASCII", "aspectratios:"+LvsCH+","+ScrAsp+line[0]);

            if(LvsCH<ScrAsp){siz=((float)mMaxRows/3.0f)/(float)(linecounter+4);}
            else{siz=(float)mMaxCols/(float)(maxchars+4);}
            if(siz<1.0f)siz=1.0f;


            midx=(-siz*((float)(maxchars+4)/(float)mMaxCols))+siz;
            midy=(((float)(linecounter+4)/(float)mMaxRows))-siz;

            if(midx>(siz-1.0f))midx=siz-1.0f;
            if(siz<=1.0f)midx=0.0f;
            midy=-siz+0.6f;






            ready=true;

        }
        public void clear()
        {
            Log.d("ASCII","CLEAR"+line);
            currentLine=0;
            for(int i=0;i<line.length;i++)
            {
                line[i]=null;
            }
            //line=new String[1];
            //line[0]=null;
            line=null;
            Log.d("ASCII","CLEAR"+line);
        }


    }

    public int mMode=MODE_INIT;

    public EditText inputField;
    public InputBox inputBox;

    MsgLines msgLines;

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
    float[] scratch7;// = new float[16];
    float[] scratch8;// = new float[16];
    float[] scratch9;// = new float[16];

    float[] mtx = new float[16];



    //DEBUG TIME MEASURE
    long mMesTime=0;
    long mLasTime=0;


    //GL OBJECTS
    private AsciiTiles mAsciiTiles;
    private InfoTile mInfoTile;
    //private TextLine[] mTextLine;
    private ButtonTile mContinueButton;
    private ButtonTile mExtraButton;
    private AudioTile mAudioTile;
    private ProgressTile mProgressTile;
    private NetworkLed mNetworkLed;
    private SliderInfoTile mSliderInfo;
    private TextBoxTile mContButText;
    private TextBoxTile mExtButText;


    //TEXTURES
    public int[] textures = new int[5];
    Bitmap mBitmap;
    Bitmap mCleanBitmap;
    Bitmap[] mCountDownBitmap=new Bitmap[3];
    Bitmap mIdleBitmap;
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

    int mCDown=0;
    boolean mCDonting=false;

    @Override
    public void onSurfaceCreated(GL10 gl, javax.microedition.khronos.egl.EGLConfig config) {

        mAngle=0f;



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

        inputBox=new InputBox(sy,sx);
        inputField = new EditText(actContext);

        mRatio=(float)sx/(float)sy;

        mMesTime= Calendar.getInstance().getTimeInMillis();
        Log.d("TIME","  GLREND TX2 INIT "+(mMesTime-mLasTime)+"ms");
        mLasTime=mMesTime;



        mAsciiTiles =       new AsciiTiles(sx,sy,textures[0],textures[1],textures[2]);

        //mTextLine=          new TextLine[sy];
        msgLines=           new MsgLines(sy);

        mContinueButton =   new ButtonTile(0.0f,-0.45f,0.2f,0.2f*mRatio,textures[3]);

        mContButText =      new TextBoxTile(textures[0],"skip");

        mExtraButton =      new ButtonTile(0.0f,-0.20f,0.2f,0.2f*mRatio,textures[3]);

        mExtButText =      new TextBoxTile(textures[0],"intro");

        mAudioTile =        new AudioTile();

        mProgressTile =     new ProgressTile();

        mNetworkLed=        new NetworkLed(-0.95f,0.95f,0.02f,0.02f*mRatio,textures[3]);

        mSliderInfo=        new SliderInfoTile(0.0f,-0.45f,0.5f,0.1f,textures[4]);




        /*for(int j=0;j<sy;j++)
        {


            mTextLine[j] = new TextLine(j,textures[0]);
        }*/



        /*INFO TILE BUILD*/
        mInfoTile=new InfoTile();



        view.mReady=true;

    }


    public int inpRow=2;
    int cursBlink=0;
    char curs = "_".charAt(0);

    public void onDrawFrame(GL10 unused) {
        // Redraw background color
        //Random rnd=new Random();
       // view.mReady=true;
 //
        //inputField.requestFocus();

        /*if(inputField.getText().length()>0)
        {
            putString(inputField.getText().toString(),inpRow,2);
        }*/


        if(mMode==MODE_INPT)
        {
            cursBlink++;
            if(cursBlink==20){curs = "_".charAt(0);}
            if(cursBlink>40){curs = " ".charAt(0);cursBlink=0;}
            String line=inputBox.getLine(0);
            int c=1;
            while(line!=null)
            {
                //Log.d("ASCII","cl"+line);
                if(inputBox.currentLine==c-1)putString(inputBox.line[c-1]+curs+" ", c, 2);
                if(line.length()==0)putString("     ", c, 2);
                line=inputBox.getLine(c);
                c++;
            }
            if(inputBox.getLine(0)==null)putString(curs+" ", 1, 2);
            else if(inputBox.getLine(0).length()==0)putString(curs+" ", 1, 2);
        }

        /*if(!inputBox.isEmpty())
        {
            for(int i=0;i<inputBox.line.length;i++) {
                putString(inputBox.line[i], i, 2);
            }
        }*/


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
        Matrix.setIdentityM(mTranslationMatrix, 0);//INFOTILE
        scratch2=mTranslationMatrix.clone();//BUTTON
        scratch3=mTranslationMatrix.clone();//AUDIO BAR

        //scratch4=mTranslationMatrix.clone();
        scratch5=mTranslationMatrix.clone();//NETLED
        scratch6=mTranslationMatrix.clone();//ASCII
        scratch7=mTranslationMatrix.clone();//EXTRA BUTTON


        //Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mTranslationMatrix, 0);

        //int sx=asciicols;
        //int sy=asciirows;
        //int k=0;

        //ASCII TILES
        Matrix.translateM(scratch6, 0, mAsciiTiles.midx, mAsciiTiles.midy, 0f);
        Matrix.scaleM(scratch6,0,mAsciiTiles.sizx,mAsciiTiles.sizy,1.0f);
        mAsciiTiles.draw(scratch6);
        /*for(int j=0;j<sy;j++)
        {
            for(int i=0;i<sx;i++)
            {
                mTile[k].draw();
                k++;
            }
        }*/



        //TEXT BACKGROUND
        //mInfoTile.draw(mTranslationMatrix);

        //PROGRESS BAR
        /*Matrix.translateM(scratch4, 0, mProgressTile.midx, mProgressTile.midy, 1.0f);
        Matrix.scaleM(scratch4, 0, mProgressTile.sizx+progress*2f, mProgressTile.sizy, 0.0f);

        mProgressTile.draw(scratch4);*/

        //TEXT LINES
        msgLines.draw();
        /*float hoffset=0.0f;
        float tothoffset=0.0f;
        for(int j=0;j<activeLine+1.0f;j++)
        {tothoffset+=mTextLine[j].mLineCount*0.07f;}
        Matrix.translateM(scratch, 0, 0.0f, tothoffset + 0.05f * activeLine - 0.75f, 0.0f);
        for(int j=0;j<asciirows;j++)
        {
            if(!mTextLine[j].isEmpty())
            {
                Matrix.translateM(scratch, 0, 0f, -0.05f-hoffset*0.07f, 0f);
                mTextLine[j].draw(scratch);
                hoffset=mTextLine[j].mLineCount;
            }

        }*/

        //SLIDER INFO
        if(mRecSequence)
        {
            Matrix.translateM(scratch5, 0, mSliderInfo.midx, mSliderInfo.midy, 1.0f);
            Matrix.scaleM(scratch5, 0, mSliderInfo.sizx, mSliderInfo.sizy, 0.0f);

            mSliderInfo.draw(scratch5);
        }


        //BUTTON
        if(!mContinueButton.isHidden()){
        mContinueButton.midx=slider;
        mContinueButton.midTx=slider;}
        Matrix.translateM(scratch2, 0, mContinueButton.midx, mContinueButton.midy, 1.0f);

        Matrix.scaleM(scratch2, 0, mContinueButton.sizx, mContinueButton.sizy, 0.0f);
        scratch8=scratch2.clone();//AUDIO BAR
        Matrix.scaleM(scratch8, 0, 5.0f, 5.0f, 0.0f);
        mContinueButton.draw(scratch2);
        mContButText.draw(scratch8);

        //BUTTON EXTRA
        if(!mExtraButton.isHidden()){
            mExtraButton.midx=0.0f;
            //mExtraButton.midTx=0.0f;
        }
        Matrix.translateM(scratch7, 0, mExtraButton.midx, mExtraButton.midy, 1.0f);

        Matrix.scaleM(scratch7, 0, mExtraButton.sizx, mExtraButton.sizy, 0.0f);
        scratch9=scratch7.clone();//AUDIO BAR
        Matrix.scaleM(scratch9, 0, 5.0f, 5.0f, 0.0f);

        mExtraButton.draw(scratch7);
        mExtButText.draw(scratch9);


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
        switch(mMode)
        {
            case MODE_INIT:
                break;
            case MODE_REC:
                mAsciiTiles.setTargetShape(0.0f,0.0f,1.0f,1.0f);
                msgLines.setPosition(0);
                //clearAscii();
                break;
            case MODE_INPT:
                mAsciiTiles.setTargetShape(inputBox.midx,inputBox.midy,inputBox.siz,inputBox.siz);
                msgLines.setPosition(1);
                break;
            default:
                break;
        }

        /*if(infoStatus!=infoTarget)

        {


            infoStatus+=(infoTarget-infoStatus)/10.0f;

            mInfoTile.sizy=(infoStatus+0.2f)*2f;



            mContinueButton.midy=infoStatus*0.8f+(1.0f-infoStatus)*-0.5f;




        }*/

        if (activeLine!=activeLineTarget)
        {
            //activeLine+=(activeLineTarget-activeLine)/10.0f;
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
        mCleanBitmap= Bitmap.createBitmap(asciicols,asciirows, Bitmap.Config.ARGB_8888 );

        mCountDownBitmap[0]=BitmapFactory.decodeResource(actContext.getResources(),R.drawable.bc1);
        mCountDownBitmap[1]=BitmapFactory.decodeResource(actContext.getResources(),R.drawable.bc2);
        mCountDownBitmap[2]=BitmapFactory.decodeResource(actContext.getResources(),R.drawable.bc3);

        mIdleBitmap=BitmapFactory.decodeResource(actContext.getResources(),R.drawable.idle);



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

    public void putImage(Bitmap bitmap,boolean erase)
    {
        //Random r = new Random();
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
        if(erase)bitmap.recycle();
        //mBitmap=bitmap.copy(Bitmap.Config.ARGB_8888,true);

        upAval=true;
    }

    public void putMsgString(String str, int row,boolean active)
    {
        Log.d("ASCII","putting string: "+str+","+row+" "+active);
        msgLines.setLine(str,row,active);
        /*if(row<mTextLine.length)
        {
            mTextLine[row].set(str);
            activeLineTarget=(float)row;
        }*/

    }

    public void hideMsgString(int row)
    {
        /*if(row<mTextLine.length)
        {
            mTextLine[row].set("");

        }*/

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
        putImage(mCleanBitmap,false);
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

        if (x>mExtraButton.midx-(mExtraButton.sizx/1.0f) &&
                x<mExtraButton.midx+(mExtraButton.sizx/1.0f) &&
                y>mExtraButton.midy-(mExtraButton.sizy/1.0f) &&
                y<mExtraButton.midy+(mExtraButton.sizy/1.0f))
        {
            Log.d("ASCII","render setclick");
            mExtraButton.setDown();

        }

    }
    public  boolean releaseClick(float x, float y)
    {
        mSlidGrub=true;
        sliderTarget=0.0f;
        if(slider>0.26 || slider<-0.26){return true;}

        return false;
    }

    public int getClick(float x, float y)
    {

        if(mContinueButton.isDown)
        {
            if (x > mContinueButton.midx - (mContinueButton.sizx / 1.0f) &&
                    x < mContinueButton.midx + (mContinueButton.sizx / 1.0f) &&
                    y > mContinueButton.midy - (mContinueButton.sizy / 1.0f) &&
                    y < mContinueButton.midy + (mContinueButton.sizy / 1.0f))
            {
                mContinueButton.setUp();
                return 1;
            }
            mContinueButton.setUp();
        }

        if(mExtraButton.isDown)
        {
            Log.d("ASCII","extra down");
            if (x > mExtraButton.midx - (mExtraButton.sizx / 1.0f) &&
                    x < mExtraButton.midx + (mExtraButton.sizx / 1.0f) &&
                    y > mExtraButton.midy - (mExtraButton.sizy / 1.0f) &&
                    y < mExtraButton.midy + (mExtraButton.sizy / 1.0f))
            {
                Log.d("ASCII","extra down");
                mExtraButton.setUp();
                return 2;
            }
            mExtraButton.setUp();
        }
        return 0;
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

    public void setIdleRec(boolean isIdle)
    {
        if(isIdle)
        {
            putImage(mIdleBitmap, false);
        }
    }

    public void setRecording(boolean isRec)
    {


        mContinueButton.isRecording=isRec;
    }

    public void countDown()
    {
        if(mCDonting)return;
        mCDonting=true;

        Log.d("ASCII","countdown");
        //putImage(mCountDownBitmap[mCDown], false);
        TimerTask cdown= new TimerTask() {
            @Override
            public void run() {

                if(mCDown<0)
                {
                    clearAscii();
                    this.cancel();
                    mCDonting=false;
                }
                if(mCDown>=0) {
                    putImage(mCountDownBitmap[mCDown], false);
                    mCDown--;
                }

                /*((MainActivity )actContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run()
                    {

                    }
                });*/







            }};

        mCDown=2;
        new Timer().scheduleAtFixedRate(cdown, 0, 500);
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

    public void hideShowButton(int button,boolean show)
    {

        switch (button)
        {
            case 0:

                mContinueButton.hideShow(show);
                break;
            case 1:
                mExtraButton.hideShow(show);
                break;
        }
    }

    public void setRecSequence(boolean rec)
    {
        mRecSequence=rec;
    }

    public void setMode(int mode)
    {
        if(mode==MODE_INIT)
        {
            mContButText.set("");
        }
        if(mode==MODE_INPT)
        {
            hideShowButton(0,false);
            hideShowButton(1,false);

        }
        if(mode==MODE_REC){
            hideShowButton(0,true);
            hideShowButton(1,false);
            mContButText.set("");

        }
        if(mode==MODE_PLAY)
        {
            hideShowButton(0,true);
            hideShowButton(1,false);
            mContButText.set("play");

        }
        clearAscii();
        mMode=mode;
        //inputBox.clear();
    }

}