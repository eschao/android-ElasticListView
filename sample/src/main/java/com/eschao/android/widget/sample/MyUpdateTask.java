package com.eschao.android.widget.sample;

import android.os.AsyncTask;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;

import com.eschao.android.widget.elasticlistview.ElasticListView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by chao on 9/10/16.
 */
public class MyUpdateTask extends AsyncTask<Void, Void, Void> {

    ElasticListView mListView;
    String[] mData;

    public MyUpdateTask(ElasticListView listView) {
        mListView = listView;
    }

    @Override
    protected Void doInBackground(Void... adapters) {
        DateFormat dateFmt = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

        mData = new String[5];
        for (int i=4; i>=0; --i) {
            mData[i] = "Update: " + dateFmt.format(Calendar.getInstance()
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
        adapter.addUpdatedData(mData);
        mListView.notifyUpdated();
    }
}
