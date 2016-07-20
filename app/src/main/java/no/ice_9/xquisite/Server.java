package no.ice_9.xquisite;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by human on 20.07.16.
 */
public class Server {
    private static final String MAIN_URL = "http://xq-storytime.herokuapp.com";

    private boolean online=false;

    public Server(MainActivity tAct, DeviceData devData)
    {

    }

    public void Run()
    {

        //CHECK CONNECTION not used in offline version TODO: rewrite to own function
        TimerTask conCheck= new TimerTask() {
            @Override
            public void run() {
                if(checkConnection())
                {
                    /*runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAscii.mGLView.mRenderer.setLed(true, false);
                        }
                    });
                    mSync=true;*/
                }
                else
                {
                    /*runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAscii.mGLView.mRenderer.setLed(false,false);
                        }
                    });
                    mSync=false;*/
                    //
                }
                /*runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });*/

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

    private boolean checkConnection()
    {
        return true;
    }

    private class TaskJson extends UrlJsonAsyncTask {
        public TaskJson(Context context) {
            super(context);
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            try {
                JSONArray jsonTasks = json.getJSONObject("data").getJSONArray("tasks");
                int length = jsonTasks.length();
                List<String> tasksTitles = new ArrayList<String>(length);

                for (int i = 0; i < length; i++) {
                    tasksTitles.add(jsonTasks.getJSONObject(i).getString("title"));
                }

                /*ListView tasksListView = (ListView) findViewById (R.id.tasks_list_view);
                if (tasksListView != null) {
                    tasksListView.setAdapter(new ArrayAdapter<String>(HomeActivity.this,
                            android.R.layout.simple_list_item_1, tasksTitles));
                }*/
            } catch (Exception e) {
                Toast.makeText(context, e.getMessage(),
                        Toast.LENGTH_LONG).show();
            } finally {
                super.onPostExecute(json);
            }
        }
    }

}
