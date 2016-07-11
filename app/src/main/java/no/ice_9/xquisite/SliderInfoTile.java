package no.ice_9.xquisite;

import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by human on 30.05.16.
 *
 * Background of the slider
 */
public class SliderInfoTile {
    public boolean isRecording=false;
    private float recBlink=0.0f;
    private boolean recBlinkUp=true;

    public  boolean isDown=false;

    public float midx;
    public float midy;
    public float sizx;
    public float sizy;

    private int textureRef = -1;
    private int fsTexture;

    private final String vertexInfoTileShaderCode =
            "attribute vec2 TexCoordIn;" +
                    "varying vec2 TexCoordOut;" +
                    "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "void main() {" +
                    "  TexCoordOut = TexCoordIn;" +
                    "  gl_Position = uMVPMatrix*vPosition;" +
                    "}";

    private final String fragmentInfoTileShaderCode =

            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "uniform sampler2D Texture;" +
                    "varying lowp vec2 TexCoordOut;" +
                    "void main() {" +
                    "   vec4 col = texture2D(Texture, TexCoordOut);"+//
                    "   col.a=(vColor.r*col.r)+((1.0-vColor.r)*col.b);"+
                    "   col.r=col.r;"+
                    "   col.b=col.b;"+
                    "   col.g=col.g;"+
                    "   gl_FragColor =  col;" +
                    "}";

    private final int mProgram;

    private FloatBuffer vertexBuffer;
    private ShortBuffer indexBuffer;
    private FloatBuffer textureBuffer;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    float tileCoords[] = {   // in counterclockwise order:
            -1.0f, 1.0f, -0.12f, // bottom left
            -1.0f,  -1.0f, -0.12f, // bottom left
            0.0f,  1.0f, -0.12f, // bottom left
            0.0f, -1.0f, -0.12f, // top

            0.0f,  1.0f, -0.12f, // bottom left
            0.0f, -1.0f, -0.12f, // top
            1.0f, 1.0f, -0.12f, // top
            1.0f,  -1.0f, -0.12f,  // bottom right

    };

    static final int COORDS_PER_TEXTURE = 2;
    float TextureCoords[] = {   // in counterclockwise order:
            0f, 0f,  // top
            0f, 0.5f,  // bottom left
            1f, 0f,  // bottom left
            1f, 0.5f,  // bottom right

            0f, 0.5f,  // top
            0f, 1f,  // bottom left
            1f, 0.5f,  // bottom left
            1f, 1f  // bottom right
    };

    private short drawOrder[] = { 0, 1, 2, 1, 2, 3, 4, 5, 6, 5, 6, 7 }; // order to draw vertices

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = { 0.8f, 0.8f, 0.0f, 1.0f };

    public SliderInfoTile(float mx,float my, float sx, float sy, int texture)
    {
        midx=mx;
        midy=my;
        sizx=sx;
        sizy=sy;

        textureRef = texture;

        int vertexInfoShader = XQGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                vertexInfoTileShaderCode);
        int fragmentInfoShader = XQGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentInfoTileShaderCode);

        mProgram = GLES20.glCreateProgram();

        // add the vertex shader to program
        GLES20.glAttachShader(mProgram, vertexInfoShader);

        // add the fragment shader to program
        GLES20.glAttachShader(mProgram, fragmentInfoShader);

        // creates OpenGL ES program executables
        GLES20.glLinkProgram(mProgram);



        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(tileCoords.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        vertexBuffer = byteBuffer.asFloatBuffer();
        vertexBuffer.put(tileCoords);
        vertexBuffer.position(0);
        byteBuffer = ByteBuffer.allocateDirect(drawOrder.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        indexBuffer = byteBuffer.asShortBuffer();
        indexBuffer.put(drawOrder);
        indexBuffer.position(0);
        byteBuffer = ByteBuffer.allocateDirect(TextureCoords.length*4);
        byteBuffer.order(ByteOrder.nativeOrder());
        textureBuffer = byteBuffer.asFloatBuffer();
        textureBuffer.put(TextureCoords);
        textureBuffer.position(0);
    }

    /*DRAW*/

    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    private final int textureStride = COORDS_PER_TEXTURE * 4; // 4 bytes per vertex

    //handles
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;
    private int mTextureHandle;

    public void draw(float[] mvpMatrix)
    {
        if(isRecording)
        {

            if(recBlinkUp)
            {
                recBlink+=0.04f;
                if(recBlink>=0.5f)
                {
                    recBlinkUp=false;
                }
            }
            else
            {
                recBlink -= 0.04f;
                if (recBlink <= 0.0f) {
                    recBlinkUp = true;
                }
            }


        }
        else
        {
            if(recBlink>0.0)
                recBlink-=0.02f;
        }
        if(recBlink>0.4)
        {
            color[0]=1.0f;
            color[1]=1.0f;
            color[2]=1.0f-recBlink/5;
        }
        else
        {
            color[0]=1.0f;
            color[1]=1.0f-recBlink/5;
            color[2]=0.0f;
        }
        //color[0]= recBlink;
        //color[1]=1.0f-recBlink/5;
        //color[2]=1.0f-recBlink/5;

        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        //get handle to texture coordinate variable
        mTextureHandle = GLES20.glGetAttribLocation(mProgram, "TexCoordIn");
        //if (mTextureHandle == -1) Log.e("ASCII", "TexCoordIn not found");

        //get handle to shape's texture reference
        fsTexture = GLES20.glGetUniformLocation(mProgram, "Texture");
        //if (fsTexture == -1) Log.e("ASCII", "Texture not found");

        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        //XQGLRenderer.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        //MyGLRenderer.checkGlError("glUniformMatrix4fv");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        GLES20.glVertexAttribPointer(mTextureHandle, COORDS_PER_TEXTURE,
                GLES20.GL_FLOAT, false,
                textureStride, textureBuffer);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE4);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureRef);
        GLES20.glUniform1i(fsTexture, 4);

        //GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glEnableVertexAttribArray(mTextureHandle);

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        //Draw the shape
        GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, indexBuffer);

        GLES20.glDisable(GLES20.GL_BLEND);

        //Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTextureHandle);
    }

    public void setDown()
    {
        isDown=true;

        recBlink=1.0f;
        color[1]= 1.0f;




    }
    public void setUp()
    {
        isDown=false;

        color[1]= 0.8f;




    }

    public void hideShowNext(boolean show)
    {
        Log.d("ASCII","hide next");

        if(!show)
        {
            tileCoords[18]=0f;
            tileCoords[21]=0f;
        }
        else
        {
            tileCoords[18]=1f;
            tileCoords[21]=1f;
        }

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(tileCoords.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        vertexBuffer = byteBuffer.asFloatBuffer();
        vertexBuffer.put(tileCoords);
        vertexBuffer.position(0);
        byteBuffer = ByteBuffer.allocateDirect(drawOrder.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        indexBuffer = byteBuffer.asShortBuffer();
        indexBuffer.put(drawOrder);
        indexBuffer.position(0);

    }
}
