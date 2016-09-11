package com.eschao.android.widget.sample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.eschao.android.widget.R;

import java.util.ArrayList;

/**
 * Adapter for list view
 */
public class MyListAdapter extends BaseAdapter {
    ArrayList<String> mData;
    int mUpdateIndex;
    int mLoadIndex;
    LayoutInflater mInflater;

    public MyListAdapter(Context context) {
        super();

        mUpdateIndex = 0;
        mLoadIndex = 0;
        mData = new ArrayList<String>();
        mInflater = LayoutInflater.from(context);

        for (int i=0; i<20; ++i) {
            mData.add("List Item: " + i);
        }
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int i) {
        return mData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = mInflater.inflate(R.layout.mylist_item, null);
        }

        TextView textView = (TextView)view.findViewById(R.id.text);
        textView.setText(mData.get(i));
        return view;
    }

    public void addUpdatedData(String[] data) {
        for (int i=data.length-1; i>=0; --i) {
            mData.add(0, data[i]);
        }

        notifyDataSetChanged();
    }

    public void addLoadedData(String[] data) {
        for (int i=0; i<data.length; ++i) {
            mData.add(data[i]);
        }

        notifyDataSetChanged();
    }
}
