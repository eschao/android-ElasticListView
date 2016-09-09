package com.eschao.android.widget.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.eschao.android.widget.R;
import com.eschao.android.widget.elasticlistview.ElasticListView;
import com.eschao.android.widget.elasticlistview.LoadFooter.*;
import com.eschao.android.widget.elasticlistview.OnLoadListener;
import com.eschao.android.widget.elasticlistview.OnUpdateListener;

public class MainActivity extends AppCompatActivity
                            implements OnUpdateListener, OnLoadListener,
                                        OnItemClickListener {

    ElasticListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });*/

        mListView.setHorizontalFadingEdgeEnabled(false);
        mListView.setAdapter(new MyListAdapter(this));
        mListView.enableLoadFooter(true)
                 .getLoadFooter().setLoadAction(LoadAction.RELEASE_TO_LOAD);
        mListView.setOnUpdateListener(this)
                 .setOnLoadListener(this)
                 .setOnItemClickListener(this);
        mListView.requestUpdate();
    }

    public void onUpdate() {

    }

    public void onLoad() {

    }

    public void onItemClick(AdapterView<?> parent, View view, int position,
                             long id) {

    }
}
