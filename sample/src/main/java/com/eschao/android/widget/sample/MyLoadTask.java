package com.eschao.android.widget.sample;

import android.os.AsyncTask;
import android.widget.HeaderViewListAdapter;

import com.eschao.android.widget.elasticlistview.ElasticListView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by chao on 9/10/16.
 */
public class MyLoadTask extends AsyncTask<Void, Void, Void> {

    ElasticListView mListView;
    String[] mData;

    public MyLoadTask(ElasticListView listView) {
        mListView = listView;
    }

    @Override
    protected Void doInBackground(Void... params) {
        DateFormat dateFmt = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        mData = new String[5];

        for (int i=0; i<5; ++i) {
            mData[i] = "Load: " + dateFmt.format(Calendar.getInstance()
                                                    .getTime());
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException e) {
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void param) {
        HeaderViewListAdapter t = (HeaderViewListAdapter)mListView.getAdapter();
        MyListAdapter adapter = (MyListAdapter)t.getWrappedAdapter();
        adapter.addLoadedData(mData);
        mListView.notifyLoaded();
    }
}
