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
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.TextureView;

import java.util.Calendar;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by human on 16.03.16.
 */
public class XQGLRenderer implements GLSurfaceView.Renderer {

    private int infoStatus=2;
    private int infoTarget=2;
    private float infoHeight=1.0f;
    private float infoTop=0.0f;

    //ASCI TILES SHADERS
    private final String vertexTileShaderCode =
            //"#extension GL_OES_EGL_image_external : require \n"+
            "attribute vec4 vPosition;" +
                    "attribute vec2 TexCoordIn;" +
                    "varying vec2 TexCoordOut;" +
                    "attribute vec2 AvalTexCoordIn;" +
                    "varying vec2 AvalTexCoordOut;" +
                    "attribute vec2 VidTexCoordIn;" +
                    "varying vec2 VidTexCoordOut;" +
                    "void main() {" +
                    //the matrix must be included as a modifier of gl_Position
                    "  gl_Position = vPosition;" +
                    "  TexCoordOut = TexCoordIn;" +
                    "  AvalTexCoordOut = AvalTexCoordIn;" +
                    "  VidTexCoordOut = VidTexCoordIn;" +
                    "}";

    private final String fragmentTileShaderCode =
            "#extension GL_OES_EGL_image_external : require \n"+
                    "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "uniform sampler2D Texture;" +
                    "varying lowp vec2 TexCoordOut;" +
                    "uniform sampler2D AvalTexture;" +
                    "varying lowp vec2 AvalTexCoordOut;" +
                    "uniform samplerExternalOES VidTexture;" +
                    //"uniform sampler2D VidTexture;" +
                    "varying lowp vec2 VidTexCoordOut;" +
                    "void main() {" +

                    "int si = int(VidTexCoordOut.s * 100.0);"+
                    "int sj = int(VidTexCoordOut.t * 100.0);"+
                    "vec2 vidCoords=vec2(float(si) / 100.0, float(sj) / 100.0);"+
                    "vec4 col2 = vec4(256.0,256.0,256.0,256.0)* texture2D(VidTexture, vidCoords);"+

                    "vec4 col1 = vec4(256.0,256.0,256.0,256.0)*texture2D(AvalTexture, AvalTexCoordOut);"+//
                    "float i1=(floor((col2.b+col2.r+col2.g)/6.0));"+
                    //"if(i1>=256.0){i1=512.0-i1;}"+

                    "float vidcol=floor(col2.b+col2.r+col2.g)/768.0;"+
                    "if(col1.b>=0.01){"+
                    "vidcol=1.0;"+
                    "i1=floor(col1.b);}"+
                    "float i2=col1.g;"+
                    "float i3=col1.r;"+
                    "float i4=col1.a;"+
                    "vec4 AvalData=vec4(i1,0.0,0.0,1.0);"+
                    //"i1=i1/8.0;"+
                    "float avalRow = (1.0/8.0)*floor(i1/32.0) + TexCoordOut.t;"+//1.0/floor(i1/32).0+
                    "float avalCol = (1.0/32.0)*floor(mod(i1,32.0)) + TexCoordOut.s;"+//1.0/mod(i1,32.0) +
                    "vec2 avalCoords=vec2(avalCol,avalRow);"+//+TexCoordOut;"+

                    //"  gl_FragColor = ( vColor * (i1/256.0));" +
                    "  gl_FragColor = ( vidcol * vColor * texture2D(Texture, avalCoords));" +
                    //"  gl_FragColor = ( vidcol * vColor * texture2D(Texture, avalCoords) + texture2D(VidTexture, vidCoords));" +
                    //"  gl_FragColor = ( vColor * texture2D(VidTexture, VidTexCoordOut));" +
                    //"  gl_FragColor = vec4(avalRow,0.0,0.0,1.0);" +
                    "}";

    //ASCII TILE PROGRAM
    private int mProgram;

    /*INFO TILES SHADERS*/
    private final String vertexInfoTileShaderCode =
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix*vPosition;" +
                    "}";

    private final String fragmentInfoTileShaderCode =

                    "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor =  vColor ;" +
                    "}";

    //INFO  TILE PROGRAM
    private int mInfoProgram;

    /*TEXT LINE SHADERS*/
    private final String vertexTextTileShaderCode =
            "uniform mat4 uMVPMatrix;" +
            "attribute vec2 TexCoordIn;" +
            "varying vec2 TexCoordOut;" +
            "attribute vec4 vPosition;" +
                    "void main() {" +
                    "  TexCoordOut = TexCoordIn;" +
                    "  gl_Position = uMVPMatrix*vPosition;" +
                    "}";

    private final String fragmentTextTileShaderCode =

            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "uniform sampler2D Texture;" +
                    "varying lowp vec2 TexCoordOut;" +
                    "void main() {" +
                    "vec4 col = texture2D(Texture, TexCoordOut);"+//
                    "col.a=col.r;"+
                    "  gl_FragColor =  ( vColor * col);" +
                    "}";

    //INFO  TILE PROGRAM
    private int mTextProgram;

    //DEBUG TIME MEASURE
    long mMesTime=0;
    long mLasTime=0;

    private Tile[] mTile;
    private InfoTile mInfoTile;
    private TextLine[] mTextLine;

    public int[] textures = new int[3];
    public Context actContext;
    public boolean upAval;
    public boolean upVid;

    Bitmap mBitmap;

    public int asciicols;
    public int asciirows;

    public ASCIIscreen view;

    private float mAngle;

    public SurfaceTexture mSurface;

    private boolean clearDone=true;

    private final float[] mMVPMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mTranslationMatrix = new float[16];

    @Override
    public void onSurfaceCreated(GL10 gl, javax.microedition.khronos.egl.EGLConfig config) {

        mAngle=0f;

        mMesTime= Calendar.getInstance().getTimeInMillis();
        mLasTime=mMesTime;

        /*TEXT LINE INIT*/
        int vertexTextShader = XQGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                vertexTextTileShaderCode);
        int fragmentTextShader = XQGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentTextTileShaderCode);

        mTextProgram = GLES20.glCreateProgram();

        // add the vertex shader to program
        GLES20.glAttachShader(mTextProgram, vertexTextShader);

        // add the fragment shader to program
        GLES20.glAttachShader(mTextProgram, fragmentTextShader);

        // creates OpenGL ES program executables
        GLES20.glLinkProgram(mTextProgram);

        /*INFO TILE INIT*/
        int vertexInfoShader = XQGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                vertexInfoTileShaderCode);
        int fragmentInfoShader = XQGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentInfoTileShaderCode);

        mInfoProgram = GLES20.glCreateProgram();

        // add the vertex shader to program
        GLES20.glAttachShader(mInfoProgram, vertexInfoShader);

        // add the fragment shader to program
        GLES20.glAttachShader(mInfoProgram, fragmentInfoShader);

        // creates OpenGL ES program executables
        GLES20.glLinkProgram(mInfoProgram);

        /*ASCII TILES INIT*/
        int vertexShader = XQGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                vertexTileShaderCode);
        int fragmentShader = XQGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentTileShaderCode);

        // create empty OpenGL ES Program
        mProgram = GLES20.glCreateProgram();

        // add the vertex shader to program
        GLES20.glAttachShader(mProgram, vertexShader);

        // add the fragment shader to program
        GLES20.glAttachShader(mProgram, fragmentShader);

        // creates OpenGL ES program executables
        GLES20.glLinkProgram(mProgram);

        /*RENDER INIT*/
        GLES20.glClearColor(0.0f, 0.3f, 0.0f, 0.5f);

        /*TEXTURES INIT*/
        upAval=true;
        upAval=false;
        mBitmap = Bitmap.createBitmap(asciicols,asciirows, Bitmap.Config.ARGB_8888 );


        int textGridTex=loadTexture(actContext,R.drawable.textgrid);
        textures[0]=textGridTex;

        mMesTime= Calendar.getInstance().getTimeInMillis();
        Log.d("TIME","  GLREND TX0 INIT "+(mMesTime-mLasTime)+"ms");
        mLasTime=mMesTime;

        Log.d("ASCII", "tex" + textGridTex);
        int sx=asciicols;
        int sy=asciirows;


        //GLES20.glGenTextures(1, textures, 1);
        //mBitmap = Bitmap.createBitmap(asciicols,asciirows, Bitmap.Config.ARGB_8888 );

        //GLES20.glGenTextures(2, textures, 2);

        Random r= new Random();

        for(int i=0;i<asciicols;i++)
        {
            for(int j=0;j<asciirows;j++)
            {
                //mBitmap.setPixel(i,j, Color.argb(r.nextInt(256), r.nextInt(256),r.nextInt(256),r.nextInt(256)));
                mBitmap.setPixel(i, j, Color.argb(0, 0, 0, 0));
                //Log.d("GL","PX:"+mBitmap.getPixel(i,j));
            }
        }




        GLES20.glGenTextures(1, textures, 1);


        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[1]);
        // Set filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0);

        //bitmap.recycle();

        int screenTileValue=textures[1];

        mMesTime= Calendar.getInstance().getTimeInMillis();
        Log.d("TIME", "  GLREND TX1 INIT " + (mMesTime - mLasTime) + "ms");
        mLasTime=mMesTime;

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

        mMesTime= Calendar.getInstance().getTimeInMillis();
        Log.d("TIME","  GLREND TX2 INIT "+(mMesTime-mLasTime)+"ms");
        mLasTime=mMesTime;

        /*ASCII TILE BUILD*/
        mTile=new Tile[sx*sy];

        mTextLine=new TextLine[sy];


        int k=0;

        for(int j=0;j<sy;j++)
        {
            for(int i=0;i<sx;i++)
            {
                mTile[k] =new Tile(i,j,sx,sy,textGridTex,screenTileValue,videoTex,mProgram);
                k++;
            }

            mTextLine[j] = new TextLine(j,textGridTex,mTextProgram);
        }



        /*INFO TILE BUILD*/
        mInfoTile=new InfoTile(mInfoProgram);



/*
        mMesTime= Calendar.getInstance().getTimeInMillis();
        Log.d("TIME","  GLREND TILE INIT "+(mMesTime-mLasTime)+"ms");
        mLasTime=mMesTime;
*/

    }

    /*public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    }*/



    public void onDrawFrame(GL10 unused) {
        // Redraw background color
        //Random rnd=new Random();
        view.mReady=true;
        float[] scratch = new float[16];



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
        mAngle+=0.01f;
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        updateAval();
        animInfo();

        // Set the camera position (View matrix)
        //Matrix.setLookAtM(mViewMatrix, 0, 0, 1, -1f - mAngle, 0f, -1f, 0f, 0f, 1f, 0f);
        // Calculate the projection and view transformation
        //Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        //Matrix.translateM(mTranslationMatrix, 0, 0f, 0f, -2f);
        Matrix.setIdentityM(mTranslationMatrix,0);
        Matrix.translateM(mTranslationMatrix, 0, 0f, infoTop, 0f);
        scratch=mTranslationMatrix.clone();
        Matrix.scaleM(mTranslationMatrix, 0, 1.0f, infoHeight, 1.0f);

        //Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mTranslationMatrix, 0);

        int sx=asciicols;
        int sy=asciirows;
        int k=0;
        /*for(int j=0;j<sy;j++)

        {
            for(int i=0;i<sx;i++)
            {
                mTile[k].draw();
                k++;
            }
        }*/

        mInfoTile.draw(mTranslationMatrix);

        for(int j=0;j<sy;j++)
        {
            if(!mTextLine[j].isEmpty())
            {
                Matrix.translateM(scratch, 0, 0f, -0.05f, 0f);
                mTextLine[j].draw(scratch);
            }

        }
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    public void updateAval()
    {
        //mSurface.updateTexImage();
        //mSurface.
        if(upVid)
        {   float[] mtx = new float[16];
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[2]);
            mSurface.updateTexImage();
            mSurface.getTransformMatrix(mtx);

            //GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 1);
        }
        if (upAval)
        {


            //GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 1);

            //Log.d("GL","bpup"+mBitmap.getPixel(0,0));
            Log.d("GL", "upval");
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
        if(infoTarget!=infoStatus)
        {
            switch(infoTarget)
            {
                case 1:
                    infoHeight=infoHeight+(0.3f-infoHeight)/10.0f;
                    infoTop=infoTop+(-0.85f-infoTop)/10.0f;
                    if(infoHeight==0.3f){infoStatus=infoTarget;}
                    break;

            }
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
            for(int j=0;j<asciirows;j++)
            {
                //mBitmap.setPixel(i, j, Color.argb(r.nextInt(), 0, 0, 255));
                mBitmap.setPixel(i, j, bitmap.getPixel(i, j));
            }
        }
        //mBitmap=bitmap.copy(Bitmap.Config.ARGB_8888,true);

        upAval=true;
    }

    public void putString(String str, int row, int pos)
    {
        mTextLine[row].set(str);
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

                if(ndx>=mTile.length || ndx<0){continue;}

                if(mTile[ndx]!=null && ndx>0 && ((pos+i)%asciicols)>=0)
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
        clearDone=false;
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
                        mBitmap.setPixel(i,j,px/2);
                    }
                }
                if(clearDone){this.cancel();}
                else{upAval=true;}

            }
        };
        new Timer().scheduleAtFixedRate(clearTrhead, 0, 5);

    }

    public void minimizeInfo()
    {
        infoTarget=1;
    }

    public void maximizeInfo()
    {
        infoTarget=2;
    }


}