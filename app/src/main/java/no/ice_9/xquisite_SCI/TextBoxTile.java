package no.ice_9.xquisite_SCI;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by human on 07.07.16.
 */
public class TextBoxTile {
    boolean mReady=true;
    int drorder=0;

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
                    //"  gl_FragColor =  vColor;" +
                    "}";

    private int textureRef = -1;
    private int fsTexture;

    boolean empty=true;
    int mSelfNr;
    public int mLineCount=0;

    private final int mProgram;

    private FloatBuffer vertexBuffer;
    private ShortBuffer indexBuffer;
    private FloatBuffer textureBuffer;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    float tileCoords[] = {   // in counterclockwise order:
            -0.9f, -0.9f, 0.1f, // top
            -0.9f,  0.9f, 0.1f, // bottom left
            0.9f, -0.9f, 0.1f, // bottom left
            0.9f,  0.9f, 0.1f  // bottom right
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


    // Set color with red, green, blue and alpha (opacity) values
    float color[] = { 1.0f, 1.0f, 1.0f, 1.0f };

    public TextBoxTile(int texture, String text)
    {
        /*TEXT LINE INIT*/
        int vertexTextShader = XQGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                vertexTextTileShaderCode);
        int fragmentTextShader = XQGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentTextTileShaderCode);

        mProgram = GLES20.glCreateProgram();

        // add the vertex shader to program
        GLES20.glAttachShader(mProgram, vertexTextShader);

        // add the fragment shader to program
        GLES20.glAttachShader(mProgram, fragmentTextShader);

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
        byteBuffer = ByteBuffer.allocateDirect(32);
        byteBuffer.order(ByteOrder.nativeOrder());
        textureBuffer = byteBuffer.asFloatBuffer();
        textureBuffer.put(tileTextureCoords);
        textureBuffer.position(0);

        textureRef = texture;

        set(text);

    }

     /*DRAW*/

    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    private final int textureStride = COORDS_PER_TEXTURE * 4; // 4 bytes per vertex

    //handles
    private int mPositionHandle;
    private int mColorHandle;
    private int mTextureHandle;
    private int mMVPMatrixHandle;

    public void draw(float[] mvpMatrix)
    {
        if(mReady) {
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
            //GLES20.glEnableVertexAttribArray(mPositionHandle);

            // Prepare the triangle coordinate data
            GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                    GLES20.GL_FLOAT, false,
                    vertexStride, vertexBuffer);

            GLES20.glVertexAttribPointer(mTextureHandle, COORDS_PER_TEXTURE,
                    GLES20.GL_FLOAT, false,
                    textureStride, textureBuffer);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureRef);
            GLES20.glUniform1i(fsTexture, 0);

            GLES20.glEnableVertexAttribArray(mPositionHandle);
            GLES20.glEnableVertexAttribArray(mTextureHandle);

            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);


            //Draw the shape

            GLES20.glDrawElements(GLES20.GL_TRIANGLES, drorder, GLES20.GL_UNSIGNED_SHORT, indexBuffer);//TODO: out of bounds controll textlines

            GLES20.glDisable(GLES20.GL_BLEND);

            //Disable vertex array
            GLES20.glDisableVertexAttribArray(mTextureHandle);
            GLES20.glDisableVertexAttribArray(mPositionHandle);
        }
    }

    public void set(String str)
    {
        mReady=false;
        empty=true;
        //boolean searchSpace=false;

        //int vertCount=str.length()*12;

        float tileCoords[]=new float[str.length()*12];
        drawOrder=new short[str.length()*6];
        tileTextureCoords=new float[str.length()*8];

        float sx=-0.0f;
        float sy=0.03f;
        float sw=0.06f;
        float sh=0.06f;

        float charWidth=0.04f;

        float dx=1.0f/32.0f;
        float dy=1.0f/8.0f;

        mLineCount=0;
        float charpos=((float)str.length())/-2.0f;
        for(int i=0;i<str.length();i++)
        {
            if(drawOrder.length==0 || drawOrder.length<(i*6))break;

            tileCoords[(i*12)]=sx+(charpos*charWidth);
            tileCoords[(i*12)+1]=sy-(sh+0.01f)*mLineCount;
            tileCoords[(i*12)+2]=0.1f;
            tileCoords[(i*12)+3]=sx+(charpos*charWidth);
            tileCoords[(i*12)+4]=sy-sh-(sh+0.01f)*mLineCount;
            tileCoords[(i*12)+5]=0.1f;
            tileCoords[(i*12)+6]=sx+(charpos*charWidth)+sw;
            tileCoords[(i*12)+7]=sy-(sh+0.01f)*mLineCount;
            tileCoords[(i*12)+8]=0.1f;
            tileCoords[(i*12)+9]=sx+(charpos*charWidth)+sw;
            tileCoords[(i*12)+10]=sy-sh-(sh+0.01f)*mLineCount;
            tileCoords[(i*12)+11]=0.1f;

            drawOrder[(i*6)]=(short)(i*4);
            drawOrder[(i*6)+1]=(short)(i*4+1);
            drawOrder[(i*6)+2]=(short)(i*4+2);
            drawOrder[(i*6)+3]=(short)(i*4+1);
            drawOrder[(i*6)+4]=(short)(i*4+2);
            drawOrder[(i*6)+5]=(short)(i*4+3);


            float px=dx*(float)((int)str.charAt(i)%32);
            float py=dy*(float)((int)str.charAt(i)/32);
            tileTextureCoords[(i*8)]=px;
            tileTextureCoords[(i*8)+1]=py;
            tileTextureCoords[(i*8)+2]=px;
            tileTextureCoords[(i*8)+3]=py+dy;
            tileTextureCoords[(i*8)+4]=px+dx;
            tileTextureCoords[(i*8)+5]=py;
            tileTextureCoords[(i*8)+6]=px+dx;
            tileTextureCoords[(i*8)+7]=py+dy;

            charpos++;
            /*if (charpos>35)
            {
                searchSpace=true;

            }
            if(searchSpace)
            {
                if((int)str.charAt(i)==32)
                {
                    searchSpace=false;
                    mLineCount++;
                    charpos=0;
                }
            }*/
        }

        drorder=drawOrder.length;



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
        byteBuffer = ByteBuffer.allocateDirect(tileTextureCoords.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        textureBuffer = byteBuffer.asFloatBuffer();
        textureBuffer.put(tileTextureCoords);
        textureBuffer.position(0);

        empty=false;

        mReady=true;
    }

    public boolean isEmpty()
    {
        return empty;
    }
}
