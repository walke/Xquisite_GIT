package no.ice_9.xquisite_SCI;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;



import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by human on 20.07.16.
 */
public class Server {
    private static final String MAIN_URL = "http://xq-storytime.herokuapp.com/stories.json";

    private boolean online=false;

    private MainActivity tAct;

    public Server(MainActivity act, DeviceData devData)
    {
        tAct=act;
    }

    public void Run()
    {

        //CHECK CONNECTION not used in offline version TODO: rewrite to own function
        TimerTask conCheck= new TimerTask() {
            @Override
            public void run() {
                if(online)
                {
                    tAct.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tAct.mAscii.mGLView.mRenderer.setLed(true, false);
                        }
                    });

                }
                else
                {
                    tAct.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tAct.mAscii.mGLView.mRenderer.setLed(false,false);
                        }
                    });


                }


            }};


        new Timer().scheduleAtFixedRate(conCheck, 0, 16000);

        //SYNC THREAD
        /*TimerTask sync= new TimerTask() {
            @Override
            public void run() {
                if(mSync)
                {
                    /*if(appData.sync())
                    {
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run() {
                                mAscii.mGLView.mRenderer.setLed(true, true);
                            }
                        });
                    }*/
                /*}





            }
        };

        new Timer().scheduleAtFixedRate(sync, 0, 8000);*/

        /*try {

            MovieCreator.build(new File(getExternalFilesDir("VID").getPath(), "sub").getPath()+"/SUB_0.mp4");
        }catch (Exception io)
        {
            Log.e("MAIN","err "+io);
            StackTraceElement[] st=io.getStackTrace();
            for(int i=0;i<st.length;i++)
            {
                Log.e("MAIN","msg "+st[i].toString());
            }

        }*/
    }



    private void checkConnection()
    {
        PingServer(MAIN_URL);

    }

    private void PingServer(String url) {
        TaskJsonPing getTasksTask = new TaskJsonPing(tAct);
        getTasksTask.setMessageLoading("Loading tasks...");
        getTasksTask.execute(url);
    }

    private class TaskJsonPing extends UrlJsonAsyncTask {
        public TaskJsonPing(Context context) {
            super(context);
        }

        @Override
        protected JSONObject doInBackground(String... urls) {
            URL url;
            try
            {
                url = new URL(urls[0]);
                try{
                HttpURLConnection client = (HttpURLConnection) url.openConnection();}
                catch (IOException e){Log.e("JSON", "" + e);}
            }catch (MalformedURLException e){Log.e("JSON", "" + e);}


           // HttpPost post = new HttpPost(urls[0]);
            JSONObject holder = new JSONObject();
            JSONObject userObj = new JSONObject();
            String response = null;
            JSONObject json = new JSONObject();

            /*try {
                try {
                    // setup the returned values in case
                    // something goes wrong
                    json.put("success", false);
                    json.put("info", "Something went wrong. Retry!");
                    // add the user email and password to
                    // the params
                    /*userObj.put("email", mUserEmail);
                    userObj.put("password", mUserPassword);
                    holder.put("user", userObj);*/
                    /*StringEntity se = new StringEntity(holder.toString());
                    post.setEntity(se);

                    // setup the request headers
                    post.setHeader("Accept", "application/json");
                    post.setHeader("Content-Type", "application/json");

                    ResponseHandler<String> responseHandler = new BasicResponseHandler();
                    response = client.execute(post, responseHandler);
                    json = new JSONObject(response);

                } catch (HttpResponseException e) {
                    e.printStackTrace();
                    Log.e("ClientProtocol", "" + e);
                    json.put("info", "Email and/or password are invalid. Retry!");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("IO", "" + e);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("JSON", "" + e);
            }*/

            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                if (json.getBoolean("success")) {
                    // everything is ok
                    /*SharedPreferences.Editor editor = mPreferences.edit();
                    // save the returned auth_token into
                    // the SharedPreferences
                    editor.putString("AuthToken", json.getJSONObject("data").getString("auth_token"));
                    editor.commit();

                    // launch the HomeActivity and close this one
                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                    startActivity(intent);
                    finish();*/
                }
                Toast.makeText(context, json.getString("info"), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                // something went wrong: show a Toast
                // with the exception message
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            } finally {
                super.onPostExecute(json);
            }
        }
    }

}
