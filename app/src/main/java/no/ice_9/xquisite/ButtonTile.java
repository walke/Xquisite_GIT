package no.ice_9.xquisite;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by human on 01.04.16.
 */
public class ButtonTile {

    public  boolean isDown=false;

    public float midx;
    public float midy;
    public float sizx;
    public float sizy;

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

    private final int mProgram;

    private FloatBuffer vertexBuffer;
    private ShortBuffer indexBuffer;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    float tileCoords[] = {   // in counterclockwise order:
            -1.0f, -1.0f, 0.1f, // top
            -1.0f,  1.0f, 0.1f, // bottom left
            1.0f, -1.0f, 0.1f, // bottom left
            1.0f,  1.0f, 0.1f  // bottom right
    };

    private short drawOrder[] = { 0, 1, 2, 1, 2, 3 }; // order to draw vertices

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = { 0.4f, 0.5f, 0.3f, 1.0f };

    public ButtonTile(float mx,float my, float sx, float sy)
    {
        midx=mx;
        midy=my;
        sizx=sx;
        sizy=sy;

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
    }

    /*DRAW*/

    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    //handles
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;

    public void draw(float[] mvpMatrix)
    {
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

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

        GLES20.glEnableVertexAttribArray(mPositionHandle);

        //Draw the shape
        GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, indexBuffer);

        //Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    public void setDown()
    {
        isDown=true;
        color[0]= 0.5f;
        color[1]= 0.6f;
        color[2]= 0.4f;



    }
    public void setUp()
    {
        isDown=false;
        color[0]= 0.4f;
        color[1]= 0.5f;
        color[2]= 0.3f;



    }
}
