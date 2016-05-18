package no.ice_9.xquisite;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class BrowserActivity extends AppCompatActivity {

    DeviceData mData;
    String mDataText;
    TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);

        mTextView=(TextView)findViewById(R.id.datatext);
        mData=new DeviceData(this);
        mDataText="";
        loadData();



        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);*/
    }

    public void clearData(View view)
    {
        Log.d("BROWSER","clearing");
        mDataText="cleared\n";

        mData.clear();
        loadData();
    }

    public void loadData()
    {
        mDataText+="DEVICE DATA:\n";


        mDataText+=("device id: "+mData.getDeviceId()+"\n");

        mDataText+=mData.getAllData();

        mTextView.setText(mDataText);
    }

}
