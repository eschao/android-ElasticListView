package com.eschao.android.widget.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.eschao.android.widget.R;
import com.eschao.android.widget.elasticlistview.ElasticListView;
import com.eschao.android.widget.elasticlistview.LoadFooter.*;
import com.eschao.android.widget.elasticlistview.OnLoadListener;
import com.eschao.android.widget.elasticlistview.OnUpdateListener;

public class MainActivity extends AppCompatActivity
                          implements OnUpdateListener, OnLoadListener {

    ElasticListView mListView;
    MyListAdapter mListAdapter;
    View mListHeaderView;
    View mListFooterView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mListHeaderView = getLayoutInflater().inflate(
                                R.layout.mylist_header, null);
        mListFooterView = getLayoutInflater().inflate(
                                R.layout.mylist_footer, null);

        mListAdapter = new MyListAdapter(this);
        mListView = (ElasticListView)findViewById(R.id.listview);
        mListView.addHeaderView(mListHeaderView);
        mListView.setHorizontalFadingEdgeEnabled(true);
        mListView.setAdapter(mListAdapter);
        mListView.enableLoadFooter(true)
                 .getLoadFooter().setLoadAction(LoadAction.RELEASE_TO_LOAD);
        mListView.setOnUpdateListener(this)
                 .setOnLoadListener(this);
        mListView.addFooterView(mListFooterView);
        mListView.requestUpdate();
    }

    @Override
    public void onUpdate() {
        MyUpdateTask task = new MyUpdateTask(mListView);
        task.execute((Void)null);
    }

    @Override
    public void onLoad() {
        MyLoadTask task = new MyLoadTask(mListView);
        task.execute((Void)null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        item.setChecked(true);

        if (id == R.id.auto_load) {
            mListView.getLoadFooter().setLoadAction(LoadAction.AUTO_LOAD);
        }
        else if (id == R.id.click_to_load) {
            mListView.getLoadFooter().setLoadAction(LoadAction.CLICK_TO_LOAD);
        }
        else if (id == R.id.release_to_load) {
            mListView.getLoadFooter().setLoadAction(LoadAction.RELEASE_TO_LOAD);
        }
        else if (id == R.id.enable_updater) {
            if (mListView.isUpdating()) {
                Toast.makeText(this, R.string.toast_enable_update,
                        Toast.LENGTH_LONG).show();
            } else {
                boolean isEnabled = mListView.isUpdateHeaderEnabled();
                item.setChecked(!isEnabled);

                if (isEnabled) {
                    mListView.enableUpdateHeader(false);
                } else {
                    mListView.removeHeaderView(mListHeaderView);
                    mListView.enableUpdateHeader(true);
                    mListView.addHeaderView(mListHeaderView);
                }
            }
        }
        else if (id == R.id.enable_loader) {
            if (mListView.isLoading()) {
                Toast.makeText(this, R.string.toast_enable_load,
                        Toast.LENGTH_LONG).show();
            }
            else {
                boolean isEnabled = mListView.isLoadFooterEnabled();
                item.setChecked(!isEnabled);
                mListView.enableLoadFooter(!isEnabled);
            }
        }
        else {
            return super.onOptionsItemSelected(item);
        }

        return true;
    }
}
