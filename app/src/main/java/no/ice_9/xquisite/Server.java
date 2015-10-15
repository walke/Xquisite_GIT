package no.ice_9.xquisite;

import android.content.Context;
import android.util.Log;

/**
 * Created by HUMAN on 15.10.2015.
 */
public class Server {

    static String CODE_CHECK_CONNECTION="23354700000";//check connection

    String adress;

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

        if(response=="1"){result=true;};

        return result;
    }

    private String postToServer(String code,String data)
    {
        return "1";
    }
}
