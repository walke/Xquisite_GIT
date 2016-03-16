package no.ice_9.xquisite;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
    private int[] textures = new int[1];
    public Context actContext;

    public int asciicols;
    public int asciirows;



    private float mAngle;

    @Override
    public void onSurfaceCreated(GL10 gl, javax.microedition.khronos.egl.EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.3f, 0.0f, 0.5f);

        int textGridTex=loadTexture(actContext,R.drawable.textgrid);
        Log.d("ASCII", "tex" + textGridTex);
        int sx=asciicols;
        int sy=asciirows;

        mTile=new Tile[sx*sy];


        int k=0;

        for(int j=0;j<sy;j++)
        {
            for(int i=0;i<sx;i++)
            {
                mTile[k] =new Tile(i,j,sx,sy,textGridTex);
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


        /*for(int i=0;i<mTile.length;i++)
        {
            mTile[i].putChar("helasdlo woasdasdrld".charAt(rnd.nextInt(8)));
        }*/

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        int sx=asciicols;
        int sy=asciirows;
        int k=0;
        for(int i=0;i<sx;i++)
        {
            for(int j=0;j<sy;j++)
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
        if(row<asciirows && pos+str.length()<asciicols)
        {
            for(int i=0;i<str.length();i++)
            {
                Log.d("ASCII","CC"+(row*32+pos+i));
                mTile[row * 32 + pos + i].putChar(str.charAt(i));
            }
        }
    }


}

