package com.example.krishnachaitanya.installedapps;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.design.widget.SwipeDismissBehavior;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.example.krishnachaitanya.installedapps.Model.AppInfo;
import com.example.krishnachaitanya.installedapps.Adapter.AppAdapter;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    SwipeRefreshLayout swipeRefreshLayout;
    boolean mIncludeSystemApps;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar=(Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        listView=(ListView) findViewById(R.id.listView);
        swipeRefreshLayout=(SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        listView.setTextFilterEnabled(true);

       listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
           @Override
           public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

               AppInfo app = (AppInfo) parent.getItemAtPosition(position);

               Intent intent = getPackageManager().getLaunchIntentForPackage(app.info.packageName);

               if (intent==null)
               {
                   Toast.makeText(getApplicationContext(),"No launcher attached",Toast.LENGTH_LONG).show();

               }
               else
               {
                   if (app.info.packageName.contains("com.nemo.vidmate"))
                   {
                       Intent unin=new Intent(Intent.ACTION_DELETE);
                       unin.setData(Uri.parse("package:"+app.info.packageName));
                       unin.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                       startActivity(unin);
                   }
                   else
                   {
                       startActivity(intent);
                   }

               }

           }
       });


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                refreshIt();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        LoadAppInfoTask loadAppInfoTask = new LoadAppInfoTask();
        loadAppInfoTask.execute(PackageManager.GET_META_DATA);

    }

    private void refreshIt() {
        LoadAppInfoTask loadAppInfoTask = new LoadAppInfoTask();
        loadAppInfoTask.execute(PackageManager.GET_META_DATA);
    }



    class LoadAppInfoTask extends AsyncTask<Integer,Integer,List<AppInfo>>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected List<AppInfo> doInBackground(Integer... params) {

            List<AppInfo> apps=new ArrayList<>();
            PackageManager packageManager= getPackageManager();

            List<ApplicationInfo> infos=packageManager.getInstalledApplications(params[0]);

            for (ApplicationInfo info : infos)
            {
               if (!mIncludeSystemApps && (info.flags & ApplicationInfo.FLAG_SYSTEM)==1)
               {
                   continue;
               }

               AppInfo app=new AppInfo();
               app.info=info;
               app.label=(String) info.loadLabel(packageManager);
               apps.add(app);
            }

            Collections.sort(apps,new DNComparator());

            return apps;
        }


        @Override
        protected void onPostExecute(List<AppInfo> appInfos) {
            super.onPostExecute(appInfos);

            listView.setAdapter(new AppAdapter(MainActivity.this,appInfos));
            swipeRefreshLayout.setRefreshing(false);
            //Snackbar.make(listView,appInfos.size()+"Apps loaded",Snackbar.LENGTH_LONG).show();
            Toast.makeText(getApplicationContext(),appInfos.size()+" Applications Loaded",Toast.LENGTH_LONG).show();
        }
    }

    private class DNComparator implements Comparator<AppInfo> {
        @Override
        public int compare(AppInfo aa, AppInfo ab) {
            CharSequence sa = aa.label;
            CharSequence sb = ab.label;

            if (sa==null)
            {
                sa=aa.info.packageName;
            }
            if (sb==null)
            {
                sb=ab.info.packageName;
            }
            return Collator.getInstance().compare(sa.toString(),sb.toString());
        }
    }
}
