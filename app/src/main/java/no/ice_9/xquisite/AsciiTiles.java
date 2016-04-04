package no.ice_9.xquisite;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by human on 01.04.16.
 */
public class AsciiTiles
{
    //ASCI TILES FULL SHADERS
    private final String vertexTileFullShaderCode =
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

    private final String fragmentTileFullShaderCode =
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

                    "int si = int(VidTexCoordOut.s * 150.0);"+
                    "int sj = int(VidTexCoordOut.t * 150.0);"+
                    "vec2 vidCoords=vec2(float(si) / 150.0, 1.0-(float(sj) / 150.0));"+
                    "vec4 col2 = vec4(256.0,256.0,256.0,256.0)* texture2D(VidTexture, vidCoords);"+

                    "vec4 col1 = vec4(256.0,256.0,256.0,256.0)*texture2D(AvalTexture, AvalTexCoordOut);"+//
                    "float i1=(floor((col2.b+col2.r+col2.g)/6.0));"+
                    //"if(i1>=256.0){i1=512.0-i1;}"+

                    "float vidcol=floor(col2.b+col2.r+col2.g)/768.0*3.0;"+
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

                    // "  gl_FragColor = ( vColor * texture2D(Texture, TexCoordOut));" +
                    //"  gl_FragColor =  vColor ;" +
                    //"  gl_FragColor = ( vColor * (i1/256.0));" +
                    "  gl_FragColor = ( vidcol * vColor * texture2D(Texture, avalCoords));" +
                    //"  gl_FragColor = ( vidcol * vColor * texture2D(Texture, avalCoords) + texture2D(VidTexture, vidCoords));" +
                    //"  gl_FragColor = ( vColor * texture2D(VidTexture, VidTexCoordOut));" +
                    //"  gl_FragColor = vec4(avalRow,0.0,0.0,1.0);" +
                    "}";

    private int textureRef = -1;
    private int fsTexture;
    private int avalTextureRef = -1;
    private int avalfsTexture;
    private int vidTextureRef = -1;
    private int vidfsTexture;

    private FloatBuffer vertexBuffer;
    private ShortBuffer indexBuffer;
    private FloatBuffer textureBuffer;
    private FloatBuffer avalTextureBuffer;
    private FloatBuffer vidTextureBuffer;


    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    float tileCoords[];
    /*float tileCoords[] = {   // in counterclockwise order:
            0.1f, 0.1f, 0.0f, // top
            0.1f, 0.9f, 0.0f, // bottom left
            0.9f, 0.1f, 0.0f, // bottom left
            0.9f, 0.9f, 0.0f  // bottom right
    };*/

    //private short drawOrder[] = { 0, 1, 2, 1, 2, 3 }; // order to draw vertices
    private short drawOrder[];

    static final int COORDS_PER_TEXTURE = 2;
    float[] tileTextureCoords;
    float[] tileAvalTextureCoords;
    float[] tileVidTextureCoords;



    // Set color with red, green, blue and alpha (opacity) values
    float color[] = { 1.0f, 1.0f, 1.0f, 1.0f };

    private final int mProgram;


    public AsciiTiles(int totx,int toty, int texture, int avalTexture, int videoTexture) {

        int vertexFullShader = XQGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                vertexTileFullShaderCode);
        int fragmentFullShader = XQGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentTileFullShaderCode);

        // create empty OpenGL ES Program
        mProgram = GLES20.glCreateProgram();

        // add the vertex shader to program
        GLES20.glAttachShader(mProgram, vertexFullShader);

        // add the fragment shader to program
        GLES20.glAttachShader(mProgram, fragmentFullShader);

        // creates OpenGL ES program executables
        GLES20.glLinkProgram(mProgram);

        tileCoords            =   new float[totx*toty*COORDS_PER_VERTEX*4];
        tileTextureCoords     =   new float[totx*toty*COORDS_PER_TEXTURE*4];
        tileAvalTextureCoords =   new float[totx*toty*COORDS_PER_TEXTURE*4];
        tileVidTextureCoords  =   new float[totx*toty*COORDS_PER_TEXTURE*4];
        drawOrder             =   new short[totx*toty*6];

        //vertcord
        float xscal=2/(float)totx;
        float yscal=2/(float)toty;//TODO: REMOVE OFFSET

        //avalcord
        float xavscal=1.0f/(float)totx;
        float yavscal=1.0f/(float)toty;

        //vidcord
        float xvdscal=xavscal;
        float yvdscal=xavscal;



        for(int j=0;j<toty;j++)
        {
            float yd=yscal*j;
            float ydav=yavscal*(float)(toty-j-1);
            float ydvd=yavscal*(float)j;

            for (int i = 0; i < totx; i++) {

                float xd=xscal*i;
                float xdav=xavscal*(float)i;
                float xdvd=xdav;

                int offs=(i+j*totx);
                int offsvx=offs*12;
                int offstx=offs*8;
                int offsdo=offs*6;
                tileCoords[offsvx]=(xd)-1.0f;
                tileCoords[offsvx+1]=(yd)-1.0f;
                tileCoords[offsvx+2]=0.0f;
                tileCoords[offsvx+3]=(xd)+xscal-1.0f;
                tileCoords[offsvx+4]=(yd)-1.0f;
                tileCoords[offsvx+5]=0.0f;
                tileCoords[offsvx+6]=(xd)-1.0f;
                tileCoords[offsvx+7]=(yd)+yscal-1.0f;
                tileCoords[offsvx+8]=0.0f;
                tileCoords[offsvx+9]=(xd)+xscal-1.0f;
                tileCoords[offsvx+10]=(yd)+yscal-1.0f;
                tileCoords[offsvx+11]=0.0f;

                tileTextureCoords[offstx]=0.0f;
                tileTextureCoords[offstx+1]=1.0f/8.0f;
                tileTextureCoords[offstx+2]= 1.0f/32.0f;
                tileTextureCoords[offstx+3]= 1.0f/8.0f;
                tileTextureCoords[offstx+4]= 0.0f;
                tileTextureCoords[offstx+5]= 0.0f;
                tileTextureCoords[offstx+6]= 1.0f/32.0f;
                tileTextureCoords[offstx+7]= 0.0f;
                /*tileTextureCoords[offstx]=0.0f;
                tileTextureCoords[offstx+1]=1.0f;
                tileTextureCoords[offstx+2]= 1.0f;
                tileTextureCoords[offstx+3]= 1.0f;
                tileTextureCoords[offstx+4]= 0.0f;
                tileTextureCoords[offstx+5]= 0.0f;
                tileTextureCoords[offstx+6]= 1.0f;
                tileTextureCoords[offstx+7]= 0.0f;*/





                tileAvalTextureCoords[offstx]=xdav;
                tileAvalTextureCoords[offstx+1]=ydav+yavscal;
                tileAvalTextureCoords[offstx+2]=xdav+xavscal;
                tileAvalTextureCoords[offstx+3]=ydav+yavscal;
                tileAvalTextureCoords[offstx+4]=xdav;
                tileAvalTextureCoords[offstx+5]=ydav;
                tileAvalTextureCoords[offstx+6]=xdav+xavscal;
                tileAvalTextureCoords[offstx+7]=ydav;

                tileVidTextureCoords[offstx+0]=ydvd;
                tileVidTextureCoords[offstx+1]=xdvd;
                tileVidTextureCoords[offstx+2]=ydvd;
                tileVidTextureCoords[offstx+3]=xdvd+xvdscal;
                tileVidTextureCoords[offstx+6]=ydvd+yvdscal;
                tileVidTextureCoords[offstx+7]=xdvd+xvdscal;
                tileVidTextureCoords[offstx+4]=ydvd+yvdscal;
                tileVidTextureCoords[offstx+5]=xdvd;


                drawOrder[offsdo]=(short)(offs*4);
                drawOrder[offsdo+1]=(short)(offs*4+1);
                drawOrder[offsdo+2]=(short)(offs*4+2);
                drawOrder[offsdo+3]=(short)(offs*4+1);
                drawOrder[offsdo+4]=(short)(offs*4+2);
                drawOrder[offsdo+5]=(short)(offs*4+3);

            }
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
        byteBuffer = ByteBuffer.allocateDirect(tileTextureCoords.length*4);
        byteBuffer.order(ByteOrder.nativeOrder());
        textureBuffer = byteBuffer.asFloatBuffer();
        textureBuffer.put(tileTextureCoords);
        textureBuffer.position(0);

        byteBuffer = ByteBuffer.allocateDirect(tileAvalTextureCoords.length*4);
        byteBuffer.order(ByteOrder.nativeOrder());
        avalTextureBuffer = byteBuffer.asFloatBuffer();
        avalTextureBuffer.put(tileAvalTextureCoords);
        avalTextureBuffer.position(0);

        byteBuffer = ByteBuffer.allocateDirect(tileVidTextureCoords.length*4);
        byteBuffer.order(ByteOrder.nativeOrder());
        vidTextureBuffer = byteBuffer.asFloatBuffer();
        vidTextureBuffer.put(tileVidTextureCoords);
        vidTextureBuffer.position(0);





        textureRef = texture;
        avalTextureRef = avalTexture;
        vidTextureRef = videoTexture;



    }

    private int mPositionHandle;
    private int mColorHandle;
    private int mTextureHandle;
    private int mAvalTextureHandle;
    private int mVidTextureHandle;

    //private final int vertexCount = tileCoords.length / COORDS_PER_VERTEX;
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

        //get handle to texture coordinate variable
        mVidTextureHandle = GLES20.glGetAttribLocation(mProgram, "VidTexCoordIn");
        //if (mAvalTextureHandle == -1) Log.e("ASCII", "AvalTexCoordIn not found");

        //get handle to shape's texture reference
        vidfsTexture = GLES20.glGetUniformLocation(mProgram, "VidTexture");
        //if (avalfsTexture == -1) Log.e("ASCII", "AvalTexture not found");



        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        // Enable a handle to the triangle vertices
        //GLES20.glEnableVertexAttribArray(mPositionHandle);

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

        GLES20.glVertexAttribPointer(mVidTextureHandle, COORDS_PER_TEXTURE,
                GLES20.GL_FLOAT, false,
                textureStride, vidTextureBuffer);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureRef);
        GLES20.glUniform1i(fsTexture, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, avalTextureRef);
        GLES20.glUniform1i(avalfsTexture, 1);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, vidTextureRef);
        GLES20.glUniform1i(vidfsTexture, 2);


        GLES20.glEnableVertexAttribArray(mPositionHandle);

        GLES20.glEnableVertexAttribArray(mTextureHandle);

        GLES20.glEnableVertexAttribArray(mAvalTextureHandle);

        GLES20.glEnableVertexAttribArray(mVidTextureHandle);







        //Draw the shape
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, indexBuffer);


        //Disable vertex array
        GLES20.glDisableVertexAttribArray(mTextureHandle);
        GLES20.glDisableVertexAttribArray(mAvalTextureHandle);
        GLES20.glDisableVertexAttribArray(mVidTextureHandle);
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
