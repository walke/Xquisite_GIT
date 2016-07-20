package no.ice_9.xquisite;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * Created by HUMAN on 15.10.2015.
 *
 * Server_OLD class is used to communicate with server and sync stories
 * TODO: right now is in an OFFLINE state and have many functions that are not used or being forced to return communicate with offline database instead of the server
 * TODO: probably call it OFFLINE-Server_OLD and make an ONLINE SERVER that passes all database blocks to the server
 *
 */
public class Server_OLD {

    boolean offline=true;

    static String CODE_SERVER_PIN="3547";//SERVER PIN
    static String CODE_PACK_ID_PINC="23";//PACKET ID PIN
    static String CODE_PACK_ID_TASK="95";//PACKET ID TASK
    static String CODE_PACK_ID_TSKD="100";//PACKET ID TASK DATA
    static String CODE_PACK_ID_FILE="108";//PACKET ID FILE
    static String CODE_PACK_ID_FDAT="109";//PACKET ID FILE
    static String CODE_PACK_ID_PRTQ="110";//PACKET ID DONE
    static String CODE_PACK_ID_DATA="111";//PACKET ID FILE
    static String CODE_PACK_ID_DONE="124";//PACKET ID DONE

    static String CODE_CHECK_CONNECTION="0000";//check connection
    static String CODE_REQUST_DEVICE_ID="0001";//request device id

    static String CODE_GET_LST_STRY_NDX="0003";//get last story ndx
    static String CODE_UPLOAD_STORY_PRT="0005";//upload part of the story
    static String CODE_RESRV_NDX_ON_SRV="0006";//reserve ndx for recording story
    static String CODE_COMPL_NDX_ON_SRV="0007";//complete reserved ndx
    static String CODE_LOAD_STORY_PARTS="0008";//load story parts
    static String CODE_REQUST_DEVICE_DT="0009";//request device data

    static String DATATYPE_DEVDATA ="0500";//device data

    String adress;
    int serverResponseCode = 0;
    Context mContext;
    DeviceData mData;

    Server_OLD(Context context, DeviceData data)
    {
        mData=data;
        mContext=context;
        String adr = context.getResources().getString(R.string.server_address);

        adress = adr;
        Log.d("SERVER", "got server address:" + adress);
    }

    //'PING' TO SERVER
    public boolean checkConnection()
    {
        String response=postToServer(CODE_CHECK_CONNECTION, "0000".getBytes());

        boolean result=false;
        Log.d("SERVER","response"+response+";");
        if(response.matches("succ"))
        {
            offline=false;
            Log.d("SERVER","got connection");
            result=true;
        }

        return result;
    }

    public int requestDeviceId(int id)
    {
        if(offline)
        {
            return 1;
        }
        byte[] bid=new byte[4];
        XQUtils.Int2ByteArr(bid,id,0);

        String response = postToServer(CODE_REQUST_DEVICE_ID, bid);
        Log.d("SERVER","IDrecieved:"+response);
        if(response.length()>=4)
        {
            Log.d("SERVER","IDrecieved:"+(int)XQUtils.ByteArr2Int(response.getBytes(),0));
            return (int)XQUtils.ByteArr2Int(response.getBytes(),0);
        }
        else
        {
            return 1;
        }



    }

    public String requestDeviceData(byte[] buf)
    {
        //byte[] bid=XQUtils.Int2ByteArr(id);
        String response = uploadDataToServer(CODE_REQUST_DEVICE_DT, buf);

        if(response.length()>=4)
        {

            return response;
        }
        else
        {
            return "null";
        }
    }



    //RESERVE INDEX ON SERVER FOR CURRENTLY RECORDING STORY
    public int reserveNdx(int parent)
    {
        if(offline)
        {
            int id=mData.getEmptyTypeId(1);
            mData.addStory(id,parent,0);
            return id;
        }
        byte[] parentStr = new byte[4];
        parentStr[0]=(byte)((((parent/256)/256)/256)%256);
        parentStr[1]=(byte)(((parent/256)/256)%256);
        parentStr[2]=(byte)((parent/256)%256);
        parentStr[3]=(byte)(parent%256);



        String response=postToServer(CODE_RESRV_NDX_ON_SRV, parentStr);
        Log.d("SERVER","check res:"+response);

        int result=0;
        result=response.getBytes()[3];
        result+=response.getBytes()[2]*256;
        result+=response.getBytes()[1]*256*256;
        result+=response.getBytes()[0]*256*256*256;
        Log.d("SERVER","responseResStory:"+result+";");


        return result;
    }

    //COMPLETE INDEX ON SERVER FOR CURRENTLY RECORDING STORY
    public int completeNdx(int ndx)
    {

        int result=-1;

        if(offline)
        {
            return mData.completeStory(ndx,true);
        }

        byte[] ndxStr = new byte[4];
        ndxStr[0]=(byte)((((ndx/256)/256)/256)%256);
        ndxStr[1]=(byte)(((ndx/256)/256)%256);
        ndxStr[2]=(byte)((ndx/256)%256);
        ndxStr[3]=(byte)(ndx%256);
        String response=postToServer(CODE_COMPL_NDX_ON_SRV,ndxStr);


        Log.d("SERVER","response"+response+";");
        if(!response.matches(""))
        {
            result=Integer.parseInt(response);

        }

        return result;
    }

    public boolean uploadPart(StoryPart part,int storyPart, int ndx, int parent, int user)
    {
        if(offline)
        {

            DataBase.Block dndx=mData.getStoryNdx(ndx);
            mData.addStoryPart(dndx,part,true);
            return true;
        }

        byte[] ndxStr = new byte[4];
        ndxStr[0]=(byte)((((ndx/256)/256)/256)%256);
        ndxStr[1]=(byte)(((ndx/256)/256)%256);
        ndxStr[2]=(byte)((ndx/256)%256);
        ndxStr[3]=(byte)(ndx%256);

        byte[] partQuest = new byte[64];
        byte[] tmpQ=part.getQuestion().getBytes();
        for(int i=0;i<64;i++)
        {
            if(i<tmpQ.length) {
                partQuest[i] = tmpQ[i];
            }
            else
            {
                partQuest[i]=0;
            }
        }

        //TODO:COMBINE ALL INPUTS and POST IT TO SERVER
        String response=uploadToServer(part.getFilePath(), partQuest,ndxStr,String.valueOf(parent),String.valueOf(user));

        boolean result=false;
        Log.d("SERVER","response"+response+";");
        if(response.matches("-1")){result=false;}
        else{result=true;}

        return result;
    }

    public StoryPart loadPart(int ndx, int storyPart)
    {
        if(offline)
        {
            //StoryPart part=new StoryPart();
            StoryPart part=mData.getStoryPart(ndx,storyPart);
            //String fname=mData.mDevData.mStory[mData.getStoryNdx(ndx)].mPart[storyPart].mFileName;
            //String quest=mData.mDevData.mStory[mData.getStoryNdx(ndx)].mPart[storyPart].mQuestion;
            //part.populate("",quest,fname);
            return part;
        }
        Log.d("SERVER","loading part:"+storyPart);
        String fpath=mContext.getExternalFilesDir("VID").toString()+"/part"+storyPart+".mp4";
        //String result;
        Log.d("SERVER","fpath:"+fpath);

        StoryPart response = loadPartFromServer(ndx, storyPart);
        Log.d("SERVER","fpathReal:"+response.getFilePath());
        Log.d("SERVER","empty:"+response.isEmpty());

        //Log.d("SERVER","response"+response+";");
        /*if(response.matches("1")){result=fpath;}
        else{result="-1";}*/

        return response;
    }

    public int[] getLastStoryNdx()
    {
        String response="";
        //TODO: get correct unsigned!!
        int result[]=new int[2];

        if(offline)
        {
            result=mData.getLastStory();
        }
        else
        {
            response = postToServer(CODE_GET_LST_STRY_NDX, "0000".getBytes());
            result[0] = ((0xFF & response.getBytes()[0]) << 24) |
                    ((0xFF & response.getBytes()[1]) << 16) |
                    ((0xFF & response.getBytes()[2]) << 8) |
                    (0xFF & response.getBytes()[3]);

            result[1]=response.getBytes()[7];
            result[1]+=response.getBytes()[6]*256;
            result[1]+=response.getBytes()[5]*256*256;
            result[1]+=response.getBytes()[4]*256*256*256;
        }





        /*result[0]=response.getBytes()[3];
        result[0]+=response.getBytes()[2]*256;
        result[0]+=response.getBytes()[1]*256*256;
        result[0]+=response.getBytes()[0]*256*256*256;*/


        Log.d("SERVER","responseLastNdx: "+result[0]+"; prts "+result[1]);


        return result;
    }

    private String postToServer(String code,byte[] data) {
        String result="-1";
        Socket sck=null;
        Log.d("SERVER","CODE:"+code+",  DATA:"+data[0]+""+data[1]+""+data[2]+""+data[3]);
        try
        {
            InetAddress serverAdrr = InetAddress.getByName(adress);

            sck = new Socket(serverAdrr,237);


            DataOutputStream out = new DataOutputStream(sck.getOutputStream());
            InputStream input =  new BufferedInputStream(sck.getInputStream());

            //Log.d("SERVER","#PIN");
            out.write(createPacketChr(Integer.parseInt(CODE_PACK_ID_PINC), 4, CODE_SERVER_PIN));
            //Log.d("SERVER", "#CODE");
            out.write(createPacketChr(Integer.parseInt(CODE_PACK_ID_TASK), 4, code));
            //Log.d("SERVER", "#DATA");
            out.write(createPacketBin(Integer.parseInt(CODE_PACK_ID_TSKD), 4, data));
            //Log.d("SERVER", "#DONE");
            out.write(createPacketChr(Integer.parseInt(CODE_PACK_ID_DONE), 4, CODE_SERVER_PIN));
            //Log.d("SERVER", "#ALL SENT");

            byte[] bbuf=new byte[4];
            int a=input.read(bbuf, 0, 4);

            int size = (int)XQUtils.ByteArr2Int(bbuf,0);/*
                    (((0x00 << 24 | bbuf[0] & 0xff) * 256 * 256 * 256) +
                            ((0x00 << 24 | bbuf[1] & 0xff) * 256 * 256) +
                            ((0x00 << 24 | bbuf[2] & 0xff) * 256) +
                            (0x00 << 24 | bbuf[3] & 0xff));*/

            bbuf=new byte[size];

            input.read(bbuf,0,size);
            Log.d("SERVER", "resp:" +new String(bbuf,"ASCII")+" code:"+a);

            result=new String(bbuf,"ASCII");
            out.flush();
            sck.close();
            Log.d("SERVER","closed socket");



        } catch(IOException ex)
        {
            ex.printStackTrace();
        }
        finally {
            if(sck!=null)
            {
                try {
                    sck.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        /*HttpURLConnection conn = null;
        String result="-1";

        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";

        try
        {
            //URL url = new URL(adress+"comm/");
            URL url = new URL(adress);
            conn = (HttpURLConnection) url.openConnection();

            conn.setDoInput(true); // Allow Inputs
            conn.setDoOutput(true); // Allow Outputs
            conn.setUseCaches(false); // Don't use a Cached Copy

            /*conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            //conn.setRequestProperty("Cache-Control", "no-cache");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            */

            //DataOutputStream outSt = new DataOutputStream(conn.getOutputStream());

            /*outSt.writeBytes(twoHyphens + boundary + lineEnd);
            outSt.writeBytes("Content-Disposition: form-data; name='code'" + lineEnd + lineEnd);
            outSt.writeBytes(code + lineEnd);

            outSt.writeBytes(twoHyphens + boundary + lineEnd);
            outSt.writeBytes("Content-Disposition: form-data; name='data'" + lineEnd + lineEnd);
            outSt.writeBytes(data + lineEnd);*/

            //InputStream in = new BufferedInputStream(conn.getInputStream());

            //String inS="";
            /*char A;
            boolean done=false;
            int iter=0;
            while(!done)
            {
                iter++;
                A=(char)in.read();
                //Log.d("SERVER","reading: "+A);
                if(A==36 || iter>1000)
                {
                    done=true;
                }
                else
                {
                    inS=inS+A;
                }

                //inS=inS+in.read();

            }
            result=inS;

            serverResponseCode = conn.getResponseCode();

            String serverResponseMessage = conn.getResponseMessage();
            Log.d("SERVER", "resp code:" + serverResponseCode);
            Log.d("SERVER", "resp msg:" + serverResponseMessage);

        }
        catch (MalformedURLException ex)
        {
            ex.printStackTrace();
        }
        catch (Exception e)
        {

            Log.e("SERVER","FUCK");
            //dialog.dismiss();
            e.printStackTrace();



        }
        finally {
            if(conn!=null)
            conn.disconnect();}*/

        return result;
    }


    private String uploadToServer(String sourceFileUri,byte[] partQuestion,byte[] ndx, String parent,String user)
    {

        String result="-1";
        Socket sck=null;
        int bytesRead, bytesAvailable, bufferSize;
        File sourceFile = new File(sourceFileUri);
        int maxBufferSize = 1* 1024 * 1024;
        byte[] buffer;

        if(!sourceFile.isFile())
        {
            Log.e("uploadFile", "Source File not exist :"
                    + sourceFileUri);



            return "-1";
        }

        try
        {
            FileInputStream fileInputStream = new FileInputStream(sourceFile);
            InetAddress serverAdrr = InetAddress.getByName(adress);

            sck = new Socket(serverAdrr,237);
            //sck.setSendBufferSize(maxBufferSize);

            //sck.setKeepAlive(true);

            //BufferedOutputStream out = new BufferedOutputStream(sck.getOutputStream());
            DataOutputStream out = new DataOutputStream(sck.getOutputStream());
            InputStream input = new BufferedInputStream(sck.getInputStream());

            out.write(createPacketChr(Integer.parseInt(CODE_PACK_ID_PINC), 4, CODE_SERVER_PIN));
            //out.flush();
            out.write(createPacketChr(Integer.parseInt(CODE_PACK_ID_TASK), 4, CODE_UPLOAD_STORY_PRT));
            //out.flush();
            out.write(createPacketBin(Integer.parseInt(CODE_PACK_ID_TSKD), 4, ndx));
            //out.flush();
            out.write(createPacketBin(Integer.parseInt(CODE_PACK_ID_PRTQ), 64, partQuestion));
            //out.flush();

            bytesAvailable = fileInputStream.available();
            Log.d("SERVER", "AVAILABLE " + bytesAvailable + " bytes");

            out.write(createFileStartPacket(Integer.parseInt(CODE_PACK_ID_FILE), bytesAvailable));
            //out.flush();
            /*
            byte[] buf = new byte[1024*16];
            int i;
            for(i=0;i<100;i++)
            {

                out.write(buf,0,1024*16);
                out.flush();
                Log.d("SERVER","W"+i+","+(1024*16*i)+","+bytesAvailable);
            }*/
            //Log.d("SERVER", "CHCH" + bytesAvailable + " bytes" +1024*32*i+ "");

            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[maxBufferSize+1];
            Log.d("SERVER","ch1 "+bufferSize);
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            int tot=0;
            tot+=bytesRead;
            Log.d("SERVER", "ch2 " + bytesRead + "," + maxBufferSize);
            while (bytesRead > 0) {
                Log.d("SERVER", "ch3 ");
                out.write(buffer, 0, bytesRead);
                //out.flush();
                //out.write(buffer);
                Log.d("SERVER", "SENT " + bytesRead + " bytes");
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                Log.d("SERVER", "ch4 "+bytesRead);
                tot+=bytesRead;
            }

            out.write(createPacketChr(Integer.parseInt(CODE_PACK_ID_DONE), 4, CODE_SERVER_PIN));

            Log.d("SERVER", "SENT TOTAL " + tot + " bytes");

            byte[] bbuf=new byte[4];
            input.read(bbuf, 0, 4);

            int size =
                    (((0x00 << 24 | bbuf[0] & 0xff) * 256*256*256)+
                            ((0x00 << 24 | bbuf[1] & 0xff) * 256*256)+
                            ((0x00 << 24 | bbuf[2] & 0xff) * 256) +
                            (0x00 << 24 | bbuf[3] & 0xff));

            bbuf=new byte[size];

            input.read(bbuf, 0, size);
            Log.d("SERVER", "resp:" +new String(bbuf,"ASCII"));

            result=new String(bbuf,"ASCII");
            out.flush();
            sck.close();
            Log.d("SERVER", "closed socket");



        } catch(IOException ex)
        {
            Log.d("SERVER","EXP");
            ex.printStackTrace();
        }
        finally {
            Log.d("SERVER","FIN FCLOSE");
            if(sck!=null)
            {

                try {
                    sck.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return result;
        /*String fileName = sourceFileUri;
        HttpURLConnection conn = null;
        String result="-1";
        DataOutputStream outSt;

        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1* 1024 * 1024;
        File sourceFile = new File(sourceFileUri);

        if(!sourceFile.isFile())
        {
            Log.e("uploadFile", "Source File not exist :"
                    + sourceFileUri);



            return 0;
        }
        else
        {
            try
            {
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(adress+"comm/");
                conn = (HttpURLConnection) url.openConnection();

                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

                conn.setRequestProperty("uploaded_file", fileName);

                outSt = new DataOutputStream(conn.getOutputStream());

                outSt.writeBytes(twoHyphens + boundary + lineEnd);
                outSt.writeBytes("Content-Disposition: form-data; name='code'" + lineEnd + lineEnd);
                outSt.writeBytes(CODE_UPLOAD_STORY_PRT + lineEnd);

                outSt.writeBytes(twoHyphens + boundary + lineEnd);
                outSt.writeBytes("Content-Disposition: form-data; name='part'" + lineEnd + lineEnd);
                outSt.writeBytes(part + lineEnd);

                outSt.writeBytes(twoHyphens + boundary + lineEnd);
                outSt.writeBytes("Content-Disposition: form-data; name='ndx'" + lineEnd + lineEnd);
                outSt.writeBytes(ndx + lineEnd);

                outSt.writeBytes(twoHyphens + boundary + lineEnd);
                outSt.writeBytes("Content-Disposition: form-data; name='parent'" + lineEnd + lineEnd);
                outSt.writeBytes(parent + lineEnd);

                outSt.writeBytes(twoHyphens + boundary + lineEnd);
                outSt.writeBytes("Content-Disposition: form-data; name='user'" + lineEnd + lineEnd);
                outSt.writeBytes(user + lineEnd);

                outSt.writeBytes(twoHyphens + boundary + lineEnd);
                outSt.writeBytes("Content-Disposition: form-data; name='uploaded_file';filename=" + fileName + lineEnd);

                outSt.writeBytes(lineEnd);

                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    outSt.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                outSt.writeBytes(lineEnd);
                outSt.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);

                if(serverResponseCode == 200){


                }

                InputStream in = new BufferedInputStream(conn.getInputStream());
                //readStream(in);

                String inS="";
                char A;
                boolean done=false;
                int iter=0;
                while(!done && iter<1000)
                {
                    iter++;
                    A=(char)in.read();



                    inS=inS+A;

                }
                Log.d("SERVER","reading: "+inS);

                //close the streams //
                fileInputStream.close();
                outSt.flush();
                outSt.close();

            }catch (FileNotFoundException e1)
            {
                e1.printStackTrace();
            }catch (MalformedURLException e2)
            {
                e2.printStackTrace();
            }catch (IOException e3)
            {
                e3.printStackTrace();
            }finally {
                if(conn!=null)
                    conn.disconnect();}
        }


        return serverResponseCode;*/
    }

    private StoryPart loadPartFromServer(int ndx,int part) {

        StoryPart retPart;

        //String result = retPart;
        retPart=new StoryPart();
        Socket sck = null;
        int bytesRead, bufferSize;
        File destenationFile = new File(mContext.getExternalFilesDir("VID").getPath(),"part"+part+".mp4");
        int maxBufferSize = 1 * 1024 * 1024;
        byte[] buffer, bufferQ;

        byte[] ndxB=new byte[4];
        ndxB[0]=(byte)((((ndx/256)/256)/256)%256);
        ndxB[1]=(byte)(((ndx/256)/256)%256);
        ndxB[2]=(byte)((ndx/256)%256);
        ndxB[3]=(byte)(ndx%256);

        byte[] partB=new byte[4];
        partB[0]=(byte)((((part/256)/256)/256)%256);
        partB[1]=(byte)(((part/256)/256)%256);
        partB[2]=(byte)((part/256)%256);
        partB[3]=(byte)(part%256);

        if (destenationFile.isFile()) {
            Log.d("SERVER", "DESTINATION File exist :part"+part);

            destenationFile.delete();
            //return "-1";
        }

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(destenationFile);
            InetAddress serverAdrr = InetAddress.getByName(adress);

            sck = new Socket(serverAdrr, 237);


            DataOutputStream out = new DataOutputStream(sck.getOutputStream());
            InputStream input = new BufferedInputStream(sck.getInputStream());

            out.write(createPacketChr(Integer.parseInt(CODE_PACK_ID_PINC), 4, CODE_SERVER_PIN));

            out.write(createPacketChr(Integer.parseInt(CODE_PACK_ID_TASK), 4, CODE_LOAD_STORY_PARTS));

            out.write(createPacketBin(Integer.parseInt(CODE_PACK_ID_TSKD), 4, ndxB));

            out.write(createPacketBin(Integer.parseInt(CODE_PACK_ID_FDAT), 4, partB));

            out.write(createPacketChr(Integer.parseInt(CODE_PACK_ID_DONE), 4, CODE_SERVER_PIN));

            int i=0;
            byte[] bbuf = new byte[4];
            int size=0;
            bytesRead=0;

            while(i<4)
            {
                bytesRead=input.read(bbuf, 0, 1);
                if(bytesRead>0)
                {
                    int mult=1;
                    for(int j=(3-i);j>0;j--){mult*=256;}
                    size+=(((0x00 << 24 | bbuf[0] & 0xff) * mult));
                    i++;
                }
            }

            //input.read(bbuf, 0, 4);

            /*int size =
                    (((0x00 << 24 | bbuf[0] & 0xff) * 256*256*256)+
                            ((0x00 << 24 | bbuf[1] & 0xff) * 256*256)+
                            ((0x00 << 24 | bbuf[2] & 0xff) * 256)+
                            (0x00 << 24 | bbuf[3] & 0xff));*/



            Log.d("SERVER", "AVAILABLE " + size + " bytes");

            //TODO: WAIT FOR 64 BYTES
            ByteArrayOutputStream questStream = new ByteArrayOutputStream(64);

            bufferQ= new byte[64];
            int got=0;
            bytesRead=0;
            while(got<64)
            {
                bytesRead = input.read(bufferQ, 0, 64-got);
                got+=bytesRead;
            }
            Log.d("SERVER","Bytes read:"+got);
            Log.d("SERVER","quest:"+new String(bufferQ, "ASCII"));

            size=size-bytesRead;

            bufferSize = Math.min(size, maxBufferSize);
            buffer = new byte[bufferSize];
            bytesRead = input.read(buffer, 0, bufferSize);
            Log.d("SERVER", "bufsize:" + bufferSize);

            //bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            int tot = 0;
            tot += bytesRead;
            while (bytesRead > 0)
            {
                fileOutputStream.write(buffer, 0, bytesRead);
                //Log.d("SERVER", "bRead:" + bytesRead);
                //Log.d("SERVER","size:"+size);

                size = size-bytesRead;
                bufferSize = Math.min(size, maxBufferSize);
                bytesRead = input.read(buffer, 0, bufferSize);
                tot += bytesRead;
            }

            fileOutputStream.close();

            Log.d("SERVER", "GOT " + tot + " bytes");


            if(buffer[0]==-1){retPart.setLast();}
            else
            {
                retPart.populate("part" + part + ".mp4", new String(bufferQ, "ASCII"), mContext.getExternalFilesDir("VID").toString() + "/part" + part + ".mp4",StoryPart.PART_TYPE_VIDEO,"",0);
            }
                    //result = "1";

                    sck.close();
            Log.d("SERVER", "closed socket");


        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (sck != null) {
                try {
                    sck.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return retPart;
    }

    private String uploadDataToServer(String code,byte[] buf)
    {
        Log.d("SERVER","    DATA UPLOAD"+code);
        String result="-1";
        if(buf==null)return result;
        Socket sck=null;
        int bytesRead, bytesAvailable, bufferSize;

        int maxBufferSize = 1* 1024 * 1024;
        byte[] buffer;




        try
        {

            InetAddress serverAdrr = InetAddress.getByName(adress);

            sck = new Socket(serverAdrr,237);
            //sck.setSendBufferSize(maxBufferSize);

            //sck.setKeepAlive(true);

            //BufferedOutputStream out = new BufferedOutputStream(sck.getOutputStream());
            DataOutputStream out = new DataOutputStream(sck.getOutputStream());
            InputStream input = new BufferedInputStream(sck.getInputStream());

            out.write(createPacketChr(Integer.parseInt(CODE_PACK_ID_PINC), 4, CODE_SERVER_PIN));
            //out.flush();
            out.write(createPacketChr(Integer.parseInt(CODE_PACK_ID_TASK), 4, code));
            //out.flush();
            //out.write(createPacketBin(Integer.parseInt(CODE_PACK_ID_TSKD), 4, DATATYPE_DEVDATA));


            bytesAvailable = buf.length;
            Log.d("SERVER", "AVAILABLE " + bytesAvailable + " bytes");

            out.write(createFileStartPacket(Integer.parseInt(CODE_PACK_ID_DATA), bytesAvailable));
            //out.flush();
            /*
            byte[] buf = new byte[1024*16];
            int i;
            for(i=0;i<100;i++)
            {

                out.write(buf,0,1024*16);
                out.flush();
                Log.d("SERVER","W"+i+","+(1024*16*i)+","+bytesAvailable);
            }*/
            //Log.d("SERVER", "CHCH" + bytesAvailable + " bytes" +1024*32*i+ "");

            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[maxBufferSize+1];
            Log.d("SERVER","ch1 "+bufferSize);

            //bytesRead = read(buffer, 0, bufferSize);
            ByteBuffer bbbuf=ByteBuffer.wrap(buf,0,buf.length);
            buffer=bbbuf.array();
            bytesRead=buffer.length;

            int tot=0;
            tot+=bytesRead;
            Log.d("SERVER", "ch2 " + bytesRead + "," + maxBufferSize);
            while (bytesRead > 0) {
                Log.d("SERVER", "ch3 ");
                out.write(buffer, 0, bytesRead);
                //out.flush();
                //out.write(buffer);
                Log.d("SERVER", "SENT " + bytesRead + " bytes");
                //bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                //bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                bytesRead=0;
                Log.d("SERVER", "ch4 "+bytesRead);
                tot+=bytesRead;
            }

            out.write(createPacketChr(Integer.parseInt(CODE_PACK_ID_DONE), 4, CODE_SERVER_PIN));

            Log.d("SERVER", "SENT TOTAL " + tot + " bytes");

            byte[] bbuf=new byte[4];
            input.read(bbuf, 0, 4);

            int size =
                    (((0x00 << 24 | bbuf[0] & 0xff) * 256*256*256)+
                            ((0x00 << 24 | bbuf[1] & 0xff) * 256*256)+
                            ((0x00 << 24 | bbuf[2] & 0xff) * 256) +
                            (0x00 << 24 | bbuf[3] & 0xff));

            bbuf=new byte[size];

            input.read(bbuf, 0, size);
            Log.d("SERVER", "resp:" +new String(bbuf,"ASCII"));

            result=new String(bbuf,"ASCII");
            out.flush();
            sck.close();
            Log.d("SERVER", "closed socket");



        } catch(IOException ex)
        {
            Log.d("SERVER","EXP");
            ex.printStackTrace();
        }
        finally {
            Log.d("SERVER","FIN FCLOSE");
            if(sck!=null)
            {

                try {
                    sck.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return result;

    }

    private byte[] createPacketChr(int id,int size, String cont)
    {
        Log.d("DBG", "CHAECK2");
        int totsize=5+size;
        byte[] result=new byte[totsize];


        String packet=""+
                (char)id+
                (char)((((size/256)/256)/256)%256)+
                (char)(((size/256)/256)%256)+
                (char)((size/256)%256)+
                (char)(size%256);

        packet+=cont;


        result=packet.getBytes();



        return result;
    }

    private byte[] createPacketBin(int id,int size, byte[] cont)
    {
        Log.d("DBG", "CHAECK2");
        int totsize=5+size;
        byte[] result=new byte[totsize];

        /*String packet=""+
                (char)id+
                (char)((((size/256)/256)/256)%256)+
                (char)(((size/256)/256)%256)+
                (char)((size/256)%256)+
                (char)(size%256);*/

        //packet+=cont;


        //result=packet.getBytes();

        result[0]=(byte)id;
        result[1]=(byte)((((size/256)/256)/256)%256);
        result[2]=(byte)(((size/256)/256)%256);
        result[3]=(byte)((size/256)%256);
        result[4]=(byte)(size%256);

        for(int i=5;i<size+5;i++)
        {
            result[i]=cont[i-5];
        }



        return result;
    }

    private byte[] createFileStartPacket(int id,int size)
    {
        Log.d("DBG", "CHAECK8");
        int totsize=5;
        byte[] result=new byte[totsize];

        /*String packet=""+
                (char)id+
                (char)((((size/256)/256)/256)%256)+
                (char)(((size/256)/256)%256)+
                (char)((size/256)%256)+
                (char)(size%256);*/

        result[0]=(byte)id;
        result[1]=(byte)((((size/256)/256)/256)%256);
        result[2]=(byte)(((size/256)/256)%256);
        result[3]=(byte)((size/256)%256);
        result[4]=(byte)(size%256);


        //result=packet.getBytes();



        return result;
    }


}
