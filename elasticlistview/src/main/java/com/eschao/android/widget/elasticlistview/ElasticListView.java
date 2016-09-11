/*
 * Copyright (C) 2016 eschao <esc.chao@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.eschao.android.widget.elasticlistview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Scroller;
import android.widget.TextView;

import com.eschao.android.widget.elasticlistview.LoadFooter.*;
import com.eschao.android.widget.elasticlistview.UpdateHeader.*;

/**
 * <p>ElasticListView, like iOS feature, allows you pull down from the top of
 * ListView to update data and pull up from the bottom of ListView to load data.
 * It provides default header and footer for updating and loading, you can
 * customize their UI and listen their state changing events to show different
 * hints.</p>
 * <p>ElasticListView is easy to use, there are some simple steps and examples
 * for you:</p>
 * <ul>
 * <li>Use it in XML resource like native ListView, All attributes of native
 * ListView are supported</li>
 * <li>Gets ElasticListView object by {@link android.view.View#findViewById(int)}
 * in your activity</li>
 * <li><p>Set updating and loading listener through
 * {@link #setOnUpdateListener(OnUpdateListener)} and
 * {@link #setOnLoadListener(OnLoadListener)}, your updating and loading
 * operations should be run in a separate thread and notify ElasticListView
 * through {@link #notifyUpdated()} and {@link #notifyLoaded()}
 * when the task is finished</p></li>
 * <li>Set content view for header by
 * {@link UpdateHeader#setContentView(int, boolean)}
 * or {@link UpdateHeader#setContentView(View, boolean)} if need</li>
 * <li>Set content view for footer by
 * {@link LoadFooter#setContentView(int, boolean)}
 * or {@link LoadFooter#setContentView(View, boolean)} if need</li>
 * <li><p>Set alignment for header and footer through {@link
 * UpdateHeader#setAlignment(VerticalAlignment)} and {@link
 * LoadFooter#setAlignment(VerticalAlignment)} if need</p></li>
 * <li>Set loading action for footer through
 * {@link LoadFooter#setLoadAction(LoadAction)} if need</li>
 * <li>Enable loading function through {@link #enableLoadFooter(boolean)}, the
 * function is disabled by default</li>
 * <li>Set your list adapter like native ListView</li>
 * <li><p>Call {@link #requestUpdate()} if you need manually execute updating
 * operation. Sometimes you want to update once the list view is shown, you can
 * call it in some startup points, for instance: call it in
 * {@link android.app.Activity#onStart()} or
 * {@link android.app.Activity#onResume()}. This request is only executed one
 * time</p></li>
 * </ul>
 * <p>XML layout example:</p>
 * <pre>
 *      &lt;com...elasticlistview.ElasticListView
 *      xmlns:android="http://schemas.android.com/apk/res/android"
 *          xmlns:tools="http://schemas.android.com/tools"
 *          android:layout_width="match_parent"
 *          android:layout_height="match_parent"
 *          android:background="@android:color/white"
 *          android:id="@+id/elasticList"&gt;
 *
 *      &lt;/com...elasticlistview.ElasticListView&gt;
 * </pre>
 * <p>Codes example:</p>
 * <pre>
 *      ElasticListView mListView;
 *
 *      protected void onCreate(Bundle savedInstanceState) {
 *          ...
 *          mListView = (ElasticListView)findViewById(R.id.elasticList);
 *          mListView.setOnUpdateListener(this);
 *          mListView.setOnLoadListener(this);
 *          mListView.getUpdateHeader().setAlignment(VerticalAlignment.CENTER);
 *          mListView.getLoadFooter()
 *                   .setAlignment(VerticalAlignment.CENTER);
 *                   .setLoadAction(LoadAction.AUTO_LOAD);
 *          mListView.enableLoadFooter(true);
 *          ...
 *          <some ListView setting, e.g: set adapter, set item click listener>
 *      }
 *
 *      // If you need to automatically run updating operation once the ListView
 *      // is shown,
 *      // you can call requestUpdate() here or in onStart()
 *      protected void onResume() {
 *          super.onResume();
 *          mListView.requestUpdate();
 *      }
 * </pre>
 *
 * @see android.widget.ListView
 * @see LoadFooter
 * @see UpdateHeader
 * @see OnLoadListener
 * @see OnUpdateListener
 * @author eschao
 * @since Android API level 9
 * @version 1.0
 */
public class ElasticListView extends ListView {

    // defines messages
    final static int MSG_SET_UPDATE_HEADER_HEIGHT   = 0;
    final static int MSG_DID_UPDATE                 = 1;
    final static int MSG_SET_LOAD_FOOTER_HEIGHT     = 2;
    final static int MSG_DID_LOAD                   = 3;

    int                 mLastY;
    UpdateHeader        mUpdateHeader;
    LoadFooter          mLoadFooter;
    Scroller            mScroller;
    OnUpdateListener    mOnUpdateListener;
    OnLoadListener      mOnLoadListener;
    boolean             mUpdateRequest;
    boolean             mEnableLoader;
    boolean             mEnableUpdater;
    Typeface            mFont;

    /**
     * Constructor
     *
     * @param context   Android context
     */
    public ElasticListView(Context context) {
        super(context);
        init();
    }

    /**
     * Constructor
     *
     * @param context   Android context
     * @param attrs     Attributes of ListView
     */
    public ElasticListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Constructor
     *
     * @param context   Android context
     * @param attrs     Attributes of ListView
     * @param defStyle  Styles of ListView
     */
    public ElasticListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * Sets updating listener
     *
     * @param l Updating listener
     * @return self
     */
    public ElasticListView setOnUpdateListener(OnUpdateListener l) {
        mOnUpdateListener = l;
        return this;
    }

    /**
     * Sets loading listener
     *
     * @param l Loading listener
     */
    public ElasticListView setOnLoadListener(OnLoadListener l) {
        mOnLoadListener = l;
        return this;
    }

    /**
     * Notifies the updating is completed
     * <p>When your updating task is finished, you should call it to notify
     * ElasticListView to accordingly change the status of header view</p>
     */
    public void notifyUpdated() {
        mHandler.sendEmptyMessage(MSG_DID_UPDATE);
    }

    /**
     * Notifies the loading is completed
     * <p>When your loading task is finished, you should call it to notify
     * ElasticListView to accordingly change the status of footer view</p>
     */
    public void notifyLoaded() {
        mHandler.sendEmptyMessage(MSG_DID_LOAD);
    }

    /**
     * Gets update header
     *
     * @return {@link UpdateHeader} object
     */
    public UpdateHeader getUpdateHeader() {
        return mUpdateHeader;
    }

    /**
     * Gets load footer
     *
     * @return {@link LoadFooter} object
     */
    public LoadFooter getLoadFooter() {
        return mLoadFooter;
    }

    /**
     * Sets customized font
     *
     * @param tf Font object
     */
    public ElasticListView setFont(Typeface tf) {
        mFont = tf;
        if (null != mFont) {
            setFont(mUpdateHeader);
            setFont(mLoadFooter);
        }

        return this;
    }

    /**
     * Requests a manual updating operation
     *
     * <p>The updating operation will be only executed once when the ListView is
     * shown, The operation will be preserved if the ListView is not ready and
     * be executing later. You can call it in some startup points to achieve an
     * automatic updating once the ListView is displayed</p>
     * <p>Use {@link enableUpdater(bool)} to make sure update header is enabled
     * first</p>
     *
     */
    public ElasticListView requestUpdate() {
        if (mEnableUpdater) {
            if (null == mUpdateHeader.getChildView()
                || null != mOnUpdateListener) {
                // will update later
                mUpdateRequest = true;
            } else {
                final int minHeight = mUpdateHeader.getMinHeight();
                mUpdateHeader.setHeight(minHeight);
                mUpdateHeader.setUpdating(true);
                mOnUpdateListener.onUpdate();
            }
        }

        return this;
    }

    /**
     * Is load footer enabled
     * <p>Sometimes you don't want to show load footer and enable loading
     * function, you can disable it by {@link #enableLoadFooter(boolean)}. The
     * default value is <strong>False</strong>.</p>
     *
     * @return True if enabled
     */
    public boolean isLoadFooterEnabled() {
        return mEnableLoader;
    }

    /**
     * Enables or disables load footer
     *
     * @param enable True if enable load footer
     */
    public ElasticListView enableLoadFooter(boolean enable) {
        if (enable) {
           if (!mEnableLoader) {
               super.addFooterView(mLoadFooter, null, true);
           }
        }
        else {
            if (mEnableLoader) {
                removeFooterView(mLoadFooter);
            }
        }

        mEnableLoader = enable;
        return this;
    }

    /**
     * Is update header enabled
     * <p>The update header is enabled by default, you can disable it by
     * {@link #enableLoadFooter(boolean)}.</p>
     *
     * @return True if enabled
     */
    public boolean isUpdateHeaderEnabled() {
        return mEnableUpdater;
    }

    /**
     * Enables or disables update header
     *
     * @param enable True if enable update header
     */
    public ElasticListView enableUpdateHeader(boolean enable) throws
                                                IllegalStateException {
        if (enable) {
            if (!mEnableUpdater) {
                if (getHeaderViewsCount() > 0) {
                    throw new IllegalStateException("Make sure there is no " +
                    "other headers in ListView before enable update header");
                }
                super.addHeaderView(mUpdateHeader, null, true);
            }
        } else {
            if (mEnableUpdater) {
                removeHeaderView(mUpdateHeader);
            }
        }

        mEnableUpdater = enable;
        return this;
    }

    /**
     * Overrides {@link ListView#addFooterView(View)} to assure LoadFooter is
     * the last item
     */
    @Override
    public void addFooterView(View v) {
        if (v != mLoadFooter) {
            removeFooterView(mLoadFooter);
            super.addFooterView(v, null, true);

            if (mEnableLoader) {
                super.addFooterView(mLoadFooter, null, true);
            }
        }
    }

    /**
     * Overrides {@link ListView#addFooterView(View, Object, boolean)} to assure
     * LoadFooter is the last item
     */
    @Override
    public void addFooterView(View v, Object data, boolean isSelectable) {
        if (v != mLoadFooter) {
            removeFooterView(mLoadFooter);
            super.addFooterView(v, data, isSelectable);

            if (mEnableLoader) {
                super.addFooterView(mLoadFooter, null, true);
            }
        }
    }

    /**
     * Overrides {@link android.view.ViewGroup#dispatchDraw(Canvas)} to run a
     * manual updating operation if requested
     */
    @Override
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        // if request a manual updating, run it
        if (mEnableUpdater && mUpdateRequest) {
            mUpdateRequest = false;

            if (null != mOnUpdateListener) {
                mUpdateHeader.setHeight(mUpdateHeader.getMinHeight());
                mUpdateHeader.setUpdating(true);
                mOnUpdateListener.onUpdate();
            }
        }
    }

    /**
     * Overrides {@link android.view.View#dispatchTouchEvent(MotionEvent)}
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent e) {
        int action  = e.getAction();
        int y       = (int)e.getRawY();

        // handle ACTION_DOWN to save the y position, we can not do it in
        // onTouchEvent() since the user defined click listener will intercept
        // it before onTouchEvent()
        if (MotionEvent.ACTION_DOWN == action) {
            mLastY = y;

            // stop scroller
            if (!mScroller.isFinished()) {
                mScroller.abortAnimation();
            }
        }

        return super.dispatchTouchEvent(e);
    }

    /**
     * Overrides {@link android.view.View#onTouchEvent(MotionEvent)}
     */
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        int action  = e.getAction();
        int y       = (int)e.getRawY();

        switch (action) {
        case MotionEvent.ACTION_DOWN:
            // do nothing, we handled it in dispatchTouchEvent()
            break;

        case MotionEvent.ACTION_MOVE:
            int deltaY = y-mLastY;

            // is operating on update header or is update header visible or is
            // it can be showing?
            if (mEnableUpdater && mLoadFooter.isFinished() &&
                 (mUpdateHeader.isHeightVisible()
                 || canShowUpdaterView(deltaY))) {
                // set half of movements as its height to simulate a elastic
                // effect
                mUpdateHeader.setHeightBy(deltaY/2);
                mLastY = y;

                // don't let parent continue to handle this message
                return true;
            }

            // is operating on update header or is update header visible or is
            // it can be showing?
            if (mEnableLoader && mUpdateHeader.isFinished() &&
                (mLoadFooter.isHeightVisible() || canShowLoaderView(deltaY))) {
                // set half of movements as its height to simulate a elastic
                // effect
                mLoadFooter.setHeightBy(-deltaY/2);

                // if AUTO_LOAD mode is set, execute loading operation
                if (LoadAction.AUTO_LOAD == mLoadFooter.getLoadAction() &&
                    null != mOnLoadListener && !mLoadFooter.isLoading()) {
                    mLoadFooter.setLoading(true);
                    mOnLoadListener.onLoad();
                }

                if (mLoadFooter.getCurHeight() > 0) {
                    // a magic function: setSelection can help us add footer in
                    // current showing list and make sure it can be seen on
                    // screen
                    setSelection(getCount());
                    mLastY = y;
                }
                return true;
            }
            break;

        case MotionEvent.ACTION_UP:
            if (mEnableUpdater && mUpdateHeader.isHeightVisible()) {
                // check if can execute updating
                if (mUpdateHeader.canUpdate() && null != mOnUpdateListener) {
                    mUpdateHeader.setUpdating(true);
                    mOnUpdateListener.onUpdate();
                }

                // whatever if the updating is executed or header need to be
                // hidden, there is a distance for header to springback, compute
                // it and start a scroll controller
                final int dy = mUpdateHeader.getBounceHeight();
                mScroller.startScroll(0, mUpdateHeader.getCurHeight(), 0, dy,
                                      1000);
                mHandler.sendEmptyMessage(MSG_SET_UPDATE_HEADER_HEIGHT);
                return true;
            }

            if (mEnableLoader && mLoadFooter.isHeightVisible()) {
                // check if can execute updating, as we explain in ACTION_MOVE,
                // we should use the real visible height of footer to judge if
                // the loading operation can be executing
                if (mLoadFooter.canLoad()
                    && !mLoadFooter.isClickable()
                    && null != mOnLoadListener) {
                    mLoadFooter.setLoading(true);
                    mOnLoadListener.onLoad();
                }

                // compute bounce distance and start a scroll controller
                final int dy = mLoadFooter.getBounceHeight();
                mScroller.startScroll(0, mLoadFooter.getCurHeight(), 0, dy,
                                      1000);
                mHandler.sendEmptyMessage(MSG_SET_LOAD_FOOTER_HEIGHT);
                return true;
            }
            break;
        }

        // remember current Y position and let parent continue to handle
        // motion event
        mLastY = y;
        return super.onTouchEvent(e);
    }

    /**
     * Overrides {@link android.view.View#overScrollBy(int, int, int, int, int,
     * int, int, int, boolean)} to show update header and load footer if the
     * scrolling is over the boundary of ListView
     */
    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX,
                                   int scrollY, int scrollRangeX,
                                   int scrollRangeY, int maxOverScrollX,
                                   int maxOverScrollY, boolean isTouchEvent) {

        // the deltaY < 0 means the scrolling is from top to bottom
        if (deltaY < 0 && mScroller.isFinished()
            && mEnableUpdater
            && mLoadFooter.isFinished()
            && !mUpdateHeader.isHeightVisible()) {
            mUpdateHeader.setHeightBy(-deltaY);

            // check if we can run updating operation
            if (mUpdateHeader.canUpdate() && null != mOnUpdateListener) {
                mUpdateHeader.setUpdating(true);
                mOnUpdateListener.onUpdate();
            }

            // compute bounce distance and start a scroll controller
            final int dy = mUpdateHeader.getBounceHeight();
            mScroller.startScroll(0, mUpdateHeader.getCurHeight(), 0, dy, 1000);
            mHandler.sendEmptyMessage(MSG_SET_UPDATE_HEADER_HEIGHT);
        }

        // the deltaY > 0 means the scrolling is from bottom to top
        if (deltaY > 0 && mScroller.isFinished()
            && mEnableLoader
            && mUpdateHeader.isFinished()
            && !mLoadFooter.isHeightVisible()
            && isItemFilledScreen()) {
            mLoadFooter.setHeightBy(deltaY);

            // use the magic function to make sure footer is added in current
            // showing view list
            if (mLoadFooter.getCurHeight() > 0) {
                setSelection(getCount());
            }

            // check if we can run loading operations
            if (null != mOnLoadListener && !mLoadFooter.isLoading() &&
                (LoadAction.AUTO_LOAD == mLoadFooter.getLoadAction() ||
                (mLoadFooter.canLoad() && !mLoadFooter.isClickable()))) {
                mLoadFooter.setLoading(true);
                mOnLoadListener.onLoad();
            }

            // compute bounce distance and start a scroll controller
            final int dy = mLoadFooter.getBounceHeight();
            mScroller.startScroll(0, mLoadFooter.getCurHeight(), 0, dy, 1000);
            mHandler.sendEmptyMessage(MSG_SET_LOAD_FOOTER_HEIGHT);
        }

        return false;
    }

    /**
     * initialize
     */
    private void init() {
        // init update header
        Context context = getContext();
        mUpdateHeader = new UpdateHeader(getContext());
        View view = LayoutInflater.from(context).inflate(R.layout.update_header,
                                                         null);
        mUpdateHeader.addView(view);
        addHeaderView(mUpdateHeader);

        // init load footer
        mLoadFooter = new LoadFooter(getContext());
        view = LayoutInflater.from(context).inflate(R.layout.load_footer, null);
        mLoadFooter.addView(view);
        //addFooterView(mLoadFooter);
        mLoadFooter.setOnClickListener(mLoadClickListener);

        // others
        mScroller           = new Scroller(context);
        mUpdateRequest      = false;
        mEnableLoader       = false;
        mEnableUpdater      = true;
        mFont               = null;
        setFooterDividersEnabled(false);
        setHeaderDividersEnabled(false);
    }

    /**
     * Checks if the update header can be showing?
     *
     * @param   deltaY delta vertical movement
     * @return  True if it can be showing
     */
    private boolean canShowUpdaterView(int deltaY) {
        final int firstVisibleItem  = getFirstVisiblePosition();
        final int fistViewTop       = getChildAt(0).getTop();
        final int topPadding        = getListPaddingTop();
        return (firstVisibleItem == 0
                && fistViewTop >= topPadding
                && deltaY > 0);
    }

    /**
     * Checks if the load footer can be showing?
     *
     * @param   deltaY delta vertical movement
     * @return  True if it can be showing
     */
    private boolean canShowLoaderView(int deltaY) {
        final int itemsCount = getCount();
        // won't show footer if no item
        if (itemsCount < 0) {
            return false;
        }

        final int viewsCount        = getChildCount();
        final int firstVisibleItem  = getFirstVisiblePosition();
        final int lastVisibleItem   = getLastVisiblePosition();
        // won't show footer if the list items can not fill the screen
        if (lastVisibleItem-firstVisibleItem+1 >= itemsCount)
            return false;

        final int lastViewBottom    = getChildAt(viewsCount-1).getBottom();
        final int listBottom        = getHeight()-getListPaddingBottom();
        return (lastVisibleItem >= itemsCount-1
                && lastViewBottom == listBottom
                && deltaY < 0);
    }

    /**
     * Gets the visible height of load footer
     */
    private final boolean isItemFilledScreen() {
        return getLastVisiblePosition()-getFirstVisiblePosition()+1 < getCount();
    }

    /**
     * Is update header visible?
     */
    protected final boolean isUpdateHeaderVisible() {
        return mUpdateHeader == getChildAt(0);
    }

    /**
     * Is load footer visible?
     */
    protected final boolean isLoadFooterVisible() {
        return mLoadFooter == getChildAt(getChildCount()-1);
    }

    public final boolean isUpdating() {
        return (mUpdateHeader == getChildAt(0) && mUpdateHeader.getHeight() > 0);
    }

    public final boolean isLoading() {
        return (mLoadFooter == getChildAt(getChildCount()-1)
                && mLoadFooter.getHeight() > 0);
    }

    /**
     * Sets font for specified ViewGroup
     *
     * @param group ViewGroup
     */
    private void setFont(ViewGroup group) {
        assert(null != mFont);
        int count = group.getChildCount();
        for (int i=0; i<count; ++i) {
            View view = group.getChildAt(i);
            if (view instanceof TextView) {
                ((TextView)view).setTypeface(mFont);
            } else if (view instanceof ViewGroup) {
                setFont((ViewGroup)view);
            }
        }
    }

    /**
     * Listener for clicking footer to load
     */
    private OnClickListener mLoadClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (null != mOnLoadListener) {
                mLoadFooter.setLoading(true);
                mOnLoadListener.onLoad();
            }
        }

    };

    /**
     * Handles messages
     */
    protected Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            int height = 0;
            int y = 0;

            switch(msg.what) {
            case MSG_SET_UPDATE_HEADER_HEIGHT:
                // handles the height updating of header
                mScroller.computeScrollOffset();
                y = mScroller.getCurrY();
                mUpdateHeader.setHeight(y);

                if (!mScroller.isFinished()) {
                    if (y <= 0) {
                        mScroller.abortAnimation();
                    }
                    else if (!isUpdateHeaderVisible()) {
                        mUpdateHeader.setHeight(0);
                    }
                    else {
                        mHandler.sendEmptyMessage(MSG_SET_UPDATE_HEADER_HEIGHT);
                    }
                }
                break;

            case MSG_DID_UPDATE:
                if (mUpdateHeader.isUpdating()) {
                    mUpdateHeader.setUpdating(false);
                    // if the height of header is valid, start scroll to
                    // hide header
                    if (isUpdateHeaderVisible()) {
                        height = mUpdateHeader.getCurHeight();
                        mScroller.startScroll(0, height, 0, -height);
                        mHandler.sendEmptyMessage(MSG_SET_UPDATE_HEADER_HEIGHT);
                    }
                    else {
                        mUpdateHeader.setHeight(0);
                    }
                }
                break;

            case MSG_SET_LOAD_FOOTER_HEIGHT:
                // handles the height updating of footer
                mScroller.computeScrollOffset();
                y = mScroller.getCurrY();
                mLoadFooter.setHeight(y);

                if (!mScroller.isFinished()) {
                    if (y <= 0) {
                        mScroller.abortAnimation();
                    }
                    else if (!isLoadFooterVisible()) {
                        mLoadFooter.setHeight(0);
                    }
                    else {
                        mHandler.sendEmptyMessage(MSG_SET_LOAD_FOOTER_HEIGHT);
                    }
                }
                break;

            case MSG_DID_LOAD:
                if (mLoadFooter.isLoading()) {
                    mLoadFooter.setLoading(false);
                    // hide footer if need
                    if (isLoadFooterVisible()) {
                        height = mLoadFooter.getCurHeight();
                        mScroller.startScroll(0, height, 0, -height);
                        mHandler.sendEmptyMessage(MSG_SET_LOAD_FOOTER_HEIGHT);
                    }
                    else {
                        mLoadFooter.setHeight(0);
                    }
                }
                break;
            }
        }
    };
}
