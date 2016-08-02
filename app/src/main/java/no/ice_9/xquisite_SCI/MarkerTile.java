package no.ice_9.xquisite_SCI;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by human on 31.07.16.
 */
public class MarkerTile {




    public float midx;
    public float midy;
    public float sizx;
    public float sizy;

    public float midOx;
    public float midOy;
    public float sizOx;
    public float sizOy;

    public float midTx;
    public float midTy;
    public float sizTx;
    public float sizTy;

    private int textureRef = -1;
    private int fsTexture;



    /**
     * Shader here takes texture of the button and mixes it with dynamical color data
     */
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
                    "   col.a=col.a;"+
                    "   col.r=vColor.g;"+
                    "   col.b=vColor.g;"+
                    "   col.g=vColor.g;"+
                    "   gl_FragColor =  col;" +
                    "}";

    //shader program
    private final int mProgram;

    //buffers
    private FloatBuffer vertexBuffer;
    private ShortBuffer indexBuffer;
    private FloatBuffer textureBuffer;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    float tileCoords[] = {   // in counterclockwise order:
            -1.0f, -1.0f, 0.1f,
            -1.0f,  1.0f, 0.1f,
            1.0f, -1.0f, 0.1f,
            1.0f,  1.0f, 0.1f
    };

    static final int COORDS_PER_TEXTURE = 2;
    float TextureCoords[] = {   // in counterclockwise order:
            0f, 0f,
            0f, 1f,
            1f, 0f,
            1f, 1f
    };

    private short drawOrder[] = { 0, 1, 2, 1, 2, 3 }; // order to draw vertices

    // Initial color of the button ?
    float color[] = { 0.8f, 0.4f, 0.0f, 1.0f };

    public MarkerTile(float mx,float my, float sx, float sy, int texture)
    {
        midx=mx;
        midy=my;
        sizx=sx;
        sizy=sy;

        midOx=mx;
        midOy=my;
        sizOx=sx;
        sizOy=sy;

        midTx=mx;
        midTy=my;
        sizTx=sx;
        sizTy=sy;

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

    /**
     * drawing function
     * also calculates color value
     * @param mvpMatrix
     */
    public void draw(float[] mvpMatrix)
    {
        float dif=Math.abs(midx-midTx)+Math.abs(midy-midTy)+Math.abs(sizx-sizTx)+Math.abs(sizy-sizTy);
        //Log.d("ASCII","dif"+dif);
        if(dif>0.01)
        {
            midx+=(midTx-midx)/10.0f;
            midy+=(midTy-midy)/10.0f;
            sizx+=(sizTx-sizx)/10.0f;
            sizy+=(sizTy-sizy)/10.0f;
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

        //
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        //XQGLRenderer.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        //MyGLRenderer.checkGlError("glUniformMatrix4fv");

        //
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        //
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        GLES20.glVertexAttribPointer(mTextureHandle, COORDS_PER_TEXTURE,
                GLES20.GL_FLOAT, false,
                textureStride, textureBuffer);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureRef);
        GLES20.glUniform1i(fsTexture, 3);

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





    public void setTargetShape(float x,float y,float w,float h)
    {
        midTx=x;
        midTy=y;
        //sizTx=w;
        //sizTy=h;
    }


}
