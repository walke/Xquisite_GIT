package no.ice_9.xquisite;

import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * Created by human on 16.03.16.
 */
public class Tile {

    private int textureRef = -1;
    private int fsTexture;
    private int avalTextureRef = -1;
    private int avalfsTexture;

    private final String vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "attribute vec2 TexCoordIn;" +
                    "varying vec2 TexCoordOut;" +
                    "attribute vec2 AvalTexCoordIn;" +
                    "varying vec2 AvalTexCoordOut;" +
                    "void main() {" +
                    //the matrix must be included as a modifier of gl_Position
                    "  gl_Position = vPosition;" +
                    "  TexCoordOut = TexCoordIn;" +
                    "  AvalTexCoordOut = AvalTexCoordIn;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "uniform sampler2D Texture;" +
                    "varying lowp vec2 TexCoordOut;" +
                    "uniform sampler2D AvalTexture;" +
                    "varying lowp vec2 AvalTexCoordOut;" +
                    "void main() {" +
                    "vec4 col1 = vec4(256.0,256.0,256.0,256.0)*texture2D(AvalTexture, AvalTexCoordOut);"+//
                    "float i1=floor(col1.b);"+
                    "float i2=col1.g;"+
                    "float i3=col1.r;"+
                    "float i4=col1.a;"+
                    "vec4 AvalData=vec4(i1,0.0,0.0,1.0);"+
                    //"i1=i1/8.0;"+
                    "float avalRow = (1.0/8.0)*floor(i1/32.0) + TexCoordOut.t;"+//1.0/floor(i1/32).0+
                    "float avalCol = (1.0/32.0)*floor(mod(i1,32.0)) + TexCoordOut.s;"+//1.0/mod(i1,32.0) +
                    "vec2 avalCoords=vec2(avalCol,avalRow);"+//+TexCoordOut;"+

                    "  gl_FragColor = (vColor * texture2D(Texture, avalCoords));" +
                    //"  gl_FragColor = vec4(avalRow,0.0,0.0,1.0);" +
                    "}";




    private FloatBuffer vertexBuffer;
    private ShortBuffer indexBuffer;
    private FloatBuffer textureBuffer;
    private FloatBuffer avalTextureBuffer;



    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    float tileCoords[] = {   // in counterclockwise order:
            0.3f,  1.0f, 0.0f, // top
            0.3f, 0.0f, 0.0f, // bottom left
            1.0f, 0.0f, 0.0f, // bottom left
            1.0f, 1.0f, 0.0f  // bottom right
    };

    private short drawOrder[] = { 0, 1, 2, 1, 2, 3 }; // order to draw vertices


    static final int COORDS_PER_TEXTURE = 2;
    float[] tileTextureCoords =
            {
                    // Front face


                    0.0f,       1.0f/8.0f,
                    1.0f/32.0f, 1.0f/8.0f,
                    0.0f,       0.0f,
                    1.0f/32.0f, 0.0f






            };

    float[] tileAvalTextureCoords =
            {
                    // Front face


                    0.0f, 1.0f,
                    1.0f, 1.0f,
                    0.0f, 0.0f,
                    1.0f, 0.0f

            };

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };

    private final int mProgram;


    public Tile(int x,int y,int totx,int toty, int texture, int avalTexture) {





        float xscal=2/(float)totx;
        float yscal=2/(float)toty;//TODO: REMOVE OFFSET
        tileCoords[0]=((xscal*x))-1.0f;
        tileCoords[1]=((yscal*y))-1.0f;
        tileCoords[3]=((xscal*x))+xscal-1.0f;
        tileCoords[4]=((yscal*y))-1.0f;
        tileCoords[6]=((xscal*x))-1.0f;
        tileCoords[7]=((yscal*y))+yscal-1.0f;
        tileCoords[9]=((xscal*x))+xscal-1.0f;
        tileCoords[10]=((yscal*y))+yscal-1.0f;

        xscal=1.0f/(float)totx;
        yscal=1.0f/(float)toty;
        float xd=xscal*(float)x;
        float yd=yscal*(float)(toty-y-1);
        tileAvalTextureCoords[0]=xd+xscal*0.0f;
        tileAvalTextureCoords[1]=yd+yscal*1.0f;
        tileAvalTextureCoords[2]=xd+xscal*1.0f;
        tileAvalTextureCoords[3]=yd+yscal*1.0f;
        tileAvalTextureCoords[4]=xd+xscal*0.0f;
        tileAvalTextureCoords[5]=yd+yscal*0.0f;
        tileAvalTextureCoords[6]=xd+xscal*1.0f;
        tileAvalTextureCoords[7]=yd+yscal*0.0f;

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(tileCoords.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        vertexBuffer = byteBuffer.asFloatBuffer();
        vertexBuffer.put(tileCoords);
        vertexBuffer.position(0);
        byteBuffer = ByteBuffer.allocateDirect(drawOrder.length*4);
        byteBuffer.order(ByteOrder.nativeOrder());
        indexBuffer = byteBuffer.asShortBuffer();
        indexBuffer.put(drawOrder);
        indexBuffer.position(0);
        byteBuffer = ByteBuffer.allocateDirect(tileTextureCoords.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        textureBuffer = byteBuffer.asFloatBuffer();
        textureBuffer.put(tileTextureCoords);
        textureBuffer.position(0);

        byteBuffer = ByteBuffer.allocateDirect(tileAvalTextureCoords.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        avalTextureBuffer = byteBuffer.asFloatBuffer();
        avalTextureBuffer.put(tileAvalTextureCoords);
        avalTextureBuffer.position(0);



        int vertexShader = XQGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = XQGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        // create empty OpenGL ES Program
        mProgram = GLES20.glCreateProgram();

        // add the vertex shader to program
        GLES20.glAttachShader(mProgram, vertexShader);

        // add the fragment shader to program
        GLES20.glAttachShader(mProgram, fragmentShader);

        // creates OpenGL ES program executables
        GLES20.glLinkProgram(mProgram);

        textureRef = texture;
        avalTextureRef = avalTexture;

    }

    private int mPositionHandle;
    private int mColorHandle;
    private int mTextureHandle;
    private int mAvalTextureHandle;

    private final int vertexCount = tileCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    private final int textureStride = COORDS_PER_TEXTURE * 4; // 4 bytes per vertex

    public void draw() {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");


        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        //if (mColorHandle == -1) Log.e("ASCII", "vColor not found");

        //get handle to texture coordinate variable
        mTextureHandle = GLES20.glGetAttribLocation(mProgram, "TexCoordIn");
        //if (mTextureHandle == -1) Log.e("ASCII", "TexCoordIn not found");

        //get handle to shape's texture reference
        fsTexture = GLES20.glGetUniformLocation(mProgram, "Texture");
        //if (fsTexture == -1) Log.e("ASCII", "Texture not found");


        //get handle to texture coordinate variable
        mAvalTextureHandle = GLES20.glGetAttribLocation(mProgram, "AvalTexCoordIn");
        //if (mAvalTextureHandle == -1) Log.e("ASCII", "AvalTexCoordIn not found");

        //get handle to shape's texture reference
        avalfsTexture = GLES20.glGetUniformLocation(mProgram, "AvalTexture");
        //if (avalfsTexture == -1) Log.e("ASCII", "AvalTexture not found");




        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        GLES20.glVertexAttribPointer(mTextureHandle, COORDS_PER_TEXTURE,
                GLES20.GL_FLOAT, false,
                textureStride, textureBuffer);

        GLES20.glVertexAttribPointer(mAvalTextureHandle, COORDS_PER_TEXTURE,
                GLES20.GL_FLOAT, false,
                textureStride, avalTextureBuffer);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureRef);
        GLES20.glUniform1i(fsTexture, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, avalTextureRef);
        GLES20.glUniform1i(avalfsTexture, 1);


        GLES20.glEnableVertexAttribArray(mPositionHandle);

        GLES20.glEnableVertexAttribArray(mTextureHandle);

        GLES20.glEnableVertexAttribArray(mAvalTextureHandle);







        //Draw the shape
        GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, indexBuffer);


        //Disable vertex array
        GLES20.glDisableVertexAttribArray(mTextureHandle);
        GLES20.glDisableVertexAttribArray(mAvalTextureHandle);
        GLES20.glDisableVertexAttribArray(mPositionHandle);



        /*// Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexCount);
        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);*/


    }

    public void putChar(char ch)
    {

        int chcode=(int)ch;

        int chx=chcode%32;
        int chy=chcode/32;
        float xd=1.0f/32;
        float yd=1.0f/8;
        tileTextureCoords[0]=chx*xd;
        tileTextureCoords[1]=(chy*yd)+yd;
        tileTextureCoords[2]=chx*xd+xd;
        tileTextureCoords[3]=(chy*yd)+yd;
        tileTextureCoords[4]=chx*xd;
        tileTextureCoords[5]=(chy*yd);
        tileTextureCoords[6]=chx*xd+xd;
        tileTextureCoords[7]=(chy*yd);

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(tileTextureCoords.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        textureBuffer = byteBuffer.asFloatBuffer();
        textureBuffer.put(tileTextureCoords);
        textureBuffer.position(0);

        byteBuffer = ByteBuffer.allocateDirect(tileTextureCoords.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        avalTextureBuffer = byteBuffer.asFloatBuffer();
        avalTextureBuffer.put(tileAvalTextureCoords);
        avalTextureBuffer.position(0);




    }

}