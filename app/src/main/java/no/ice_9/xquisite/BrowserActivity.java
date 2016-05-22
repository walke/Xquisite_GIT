package no.ice_9.xquisite;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BrowserActivity extends AppCompatActivity {

    DeviceData mData;
    String mDataText;

    LinearLayout mDataList;
    dataleaf mainLeaf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);


        mDataList=(LinearLayout)findViewById(R.id.datalist);
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
        /*LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        TextView text = new TextView(this);
        text.setText("!!!");
        ll.addView(text);
        mDataList.addView(ll);*/
        mDataText+="DEVICE DATA:\n";


        mDataText+=("device id: "+mData.getDeviceId()+"\n");

        mainLeaf=mData.getAllData();
        mainLeaf.print(mDataList,this);
        //mDataText+=mData.getAllData();

        //mTextView.setText(mDataText);
    }

    public void expandLeaf(dataleaf leaf)
    {
        mDataList.removeAllViews();
        if(leaf.expanded)
        {
            leaf.expand(false);
        }
        else
        {
            leaf.expand(true);
        }

        mainLeaf.print(mDataList,this);
    }

}

class dataleaf
{
    String title;
    dataleaf[] dataleafs;
    boolean expanded;

    final dataleaf leaf = this;

    LinearLayout box;


    public dataleaf(String t)
    {
        expanded=false;
        title=t;
    }

    public void addLeaf(dataleaf leaf)
    {
        if(dataleafs==null)
        {
            dataleafs=new dataleaf[1];
        }
        else
        {
            dataleaf[] tmp=dataleafs;
            dataleafs=new dataleaf[tmp.length+1];
            for(int i=0;i<tmp.length;i++)
            {
                dataleafs[i]=tmp[i];
            }
        }
        dataleafs[dataleafs.length-1]=leaf;
    }

    public void expand(boolean exp)
    {

            expanded=exp;

    }

    public void print(LinearLayout parent,final BrowserActivity act)
    {
        LinearLayout titleLine=new LinearLayout(act);
        titleLine.setOrientation(LinearLayout.HORIZONTAL);
        box=new LinearLayout(act);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackgroundResource(R.drawable.customborder);
        TextView text = new TextView(act);
        text.setText(title);



        if(dataleafs!=null)
        {
            Button expBut=new Button(act);
            expBut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    act.expandLeaf(leaf);
                }
            });
            titleLine.addView(expBut);
        }
        titleLine.addView(text);
        box.addView(titleLine);

        if(dataleafs!=null && expanded)
        {
            for(int i=0;i<dataleafs.length;i++)
            {
                dataleafs[i].print(box,act);
            }

        }


        parent.addView(box);
    }
}
