package no.ice_9.xquisite;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by HUMAN on 15.10.2015.
 */
public class Server {

    static String CODE_CHECK_CONNECTION="23354700000";//check connection

    String adress;
    int serverResponseCode = 0;

    Server(Context context)
    {

        String adr = context.getResources().getString(R.string.server_address);

        adress = adr;
        Log.d("SERVER", "got server address:" + adress);
    }

    public boolean checkConnection()
    {
        String response=postToServer(CODE_CHECK_CONNECTION,null);

        boolean result=false;
        Log.d("SERVER","response"+response+";");
        if(response.matches("1"))
        {
            Log.d("SERVER","got connection");
            result=true;
        }

        return result;
    }

    private String postToServer(String code,String data)
    {
        HttpURLConnection conn = null;
        String result="-1";

        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";

        try
        {
            URL url = new URL(adress+"comm/");
            conn = (HttpURLConnection) url.openConnection();

            conn.setDoInput(true); // Allow Inputs
            conn.setDoOutput(true); // Allow Outputs
            conn.setUseCaches(false); // Don't use a Cached Copy

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            DataOutputStream outSt = new DataOutputStream(conn.getOutputStream());

            outSt.writeBytes(twoHyphens + boundary + lineEnd);
            outSt.writeBytes("Content-Disposition: form-data; name='code'" + lineEnd + lineEnd);
            outSt.writeBytes(code + lineEnd);

            outSt.writeBytes(twoHyphens + boundary + lineEnd);
            outSt.writeBytes("Content-Disposition: form-data; name='data'" + lineEnd + lineEnd);
            outSt.writeBytes(data + lineEnd);

            InputStream in = new BufferedInputStream(conn.getInputStream());

            String inS="";
            char A;
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

        }
        catch (Exception e)
        {

            Log.e("SERVER","FUCK");
            //dialog.dismiss();
            e.printStackTrace();



        }
        finally {
            if(conn!=null)
            conn.disconnect();}

        return result;
    }
}
