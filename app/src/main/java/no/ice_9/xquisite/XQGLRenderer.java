package no.ice_9.xquisite;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.util.Log;

import java.util.Random;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by human on 16.03.16.
 */
public class XQGLRenderer implements GLSurfaceView.Renderer {

    private Tile[] mTile;
    private int[] textures = new int[2];
    public Context actContext;

    public int asciicols;
    public int asciirows;

    public ASCIIscreen view;

    private float mAngle;

    @Override
    public void onSurfaceCreated(GL10 gl, javax.microedition.khronos.egl.EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.3f, 0.0f, 0.5f);



        int textGridTex=loadTexture(actContext,R.drawable.textgrid);
        textures[0]=textGridTex;



        Log.d("ASCII", "tex" + textGridTex);
        int sx=asciicols;
        int sy=asciirows;


        GLES20.glGenTextures(1, textures, 1);
        Bitmap bitmap = Bitmap.createBitmap(asciicols,asciirows, Bitmap.Config.ARGB_8888 );

        Random r= new Random();

        for(int i=0;i<asciicols;i++)
        {
            for(int j=0;j<asciirows;j++)
            {
                bitmap.setPixel(i,j, Color.argb(r.nextInt(256), r.nextInt(256),r.nextInt(256),r.nextInt(256)));
                Log.d("GL","PX:"+bitmap.getPixel(i,j));
            }
        }



        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[1]);
        // Set filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);


        int screenTileValue=textures[1];


        mTile=new Tile[sx*sy];


        int k=0;

        for(int j=sy-1;j>=0;j--)
        {
            for(int i=0;i<sx;i++)
            {
                mTile[k] =new Tile(i,j,sx,sy,textGridTex,screenTileValue);
                k++;
            }
        }




    }

    /*public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    }*/

    public void onDrawFrame(GL10 unused) {
        // Redraw background color
        Random rnd=new Random();
        view.mReady=true;

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

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        int sx=asciicols;
        int sy=asciirows;
        int k=0;
        for(int j=0;j<sy;j++)

        {
            for(int i=0;i<sx;i++)
            {
                mTile[k].draw();
                k++;
            }
        }
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
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

    public void putString(String str, int row, int pos)
    {
        if(row<asciirows &&/* pos+str.length()<asciicols &&*/ view.mReady)
        {
            Log.d("ASCII","AS"+row+" :"+pos+"__"+str.length());
            for(int i=0;i<str.length();i++)
            {
                int ndx = ((asciicols*(row))+(pos))+i;
                Log.d("ASCII","CC"+ndx+ ":"+mTile.length);

                if(ndx>=mTile.length || ndx<0){continue;}

                if(mTile[ndx]!=null && ndx>0 )
                {
                    /*GLES20.glGenTextures(1, textures, 1);
                    Bitmap bitmap = Bitmap.createBitmap(asciicols,asciirows, Bitmap.Config.ARGB_8888 );

                    Random r= new Random();


                    bitmap.setPixel(i,row, Color.argb(r.nextInt(256), r.nextInt(256),r.nextInt(256),r.nextInt(256)));





                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[1]);
                    // Set filtering
                    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
                    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

                    // Load the bitmap into the bound texture.
                    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);


                    //int screenTileValue=textures[1];*/


                    Log.d("ASCII","CC"+mTile[ndx]+":"+mTile.length);
                    //mTile[ndx].putChar(str.charAt(i));
                }
            }
        }
    }


}