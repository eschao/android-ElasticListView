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
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * The load header extends from ViewGroup and allows you customize your
 * loading content
 * <p>It is used in {@link ElasticListView} as a footer item to implement
 * loading operation through pre-defined gestures</p>
 *
 * @author eschao
 */
public class LoadFooter extends ViewGroup {

    private final static String TAG = "LoadFooter";

    int                 mHeight;
    int                 mMinHeight;
    boolean             mIsLoading;
    LoadAction          mLoadAction;
    VerticalAlignment   mAlignment;
    OnLoadStateListener mOnLoadStateListener;

    /**
     * Constructor
     *
     * @param context Android context
     */
    public LoadFooter(Context context) {
        super(context);
        init();
    }

    /**
     * Constructor
     *
     * @param context   Android context
     * @param attrs     attributes of view
     */
    public LoadFooter(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Constructor
     *
     * @param context   Android context
     * @param attrs     Attributes of view
     * @param defStyle  Style of view
     */
    public LoadFooter(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * Overrides ViewGroup::onLayout to layout its content by specified vertical
     * alignment
     * <p>You can set alignment through setAlignment with TOP, CENTER or BOTTOM
     * values</p>
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        View view = getChildView();
        if (null == view) {
            Log.w(TAG, "No child view!");
            return;
        }

        // get measured width and height
        int childWidth  = view.getMeasuredWidth();
        int childHeight = view.getMeasuredHeight();
        int height      = getMeasuredHeight();

        // layout child by specified alignment
        switch (mAlignment) {
        case TOP:
            view.layout(0, 0, childWidth, childHeight);
            break;

        case CENTER:
            final int top = (height-childHeight)/2;
            view.layout(0, top, childWidth, top+childHeight);
            break;

        case BOTTOM:
            view.layout(0, height-childHeight, childWidth, height);
            break;
        }
    }

    /**
     * Overrides onMeasure to measure the minimum size of content view
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        if (mHeight < 0) {
            mHeight = 0;
        }

        setMeasuredDimension(width, mHeight);

        // measure child view as a minimum height
        final View childView = getChildView();
        if (childView != null) {
            childView.measure(widthMeasureSpec, heightMeasureSpec);
            mMinHeight = childView.getMeasuredHeight();
        }
    }

    /**
     * Gets child view
     *
     * @return Child view
     */
    public View getChildView() {
        if (getChildCount() > 0) {
            return getChildAt(0);
        }
        else {
            return null;
        }
    }

    /**
     * Adds child view
     * <p>LoaderView only can contain one child view. The exception will be
     * raised if you add more than one </p>
     * <strong>Don't use it to customize your content view, please use
     * {@link #setContentView(View, boolean)} to set</strong>
     * </p>
     *
     * @param child Child view
     */
    @Override
    public void addView(View child) {
        final int childCount = getChildCount();
        if (childCount > 0) {
            throw new IllegalStateException("LoaderView can only have one "
                    + "child view");
        }
        super.addView(child);
    }

    /**
     * Customizes your content view of load footer
     *
     * @param resId         Resource id of content view
     * @param forceLayout   Force to layout content view
     */
    public void setContentView(int resId, boolean forceLayout) {
        View view = LayoutInflater.from(getContext()).inflate(resId, null);
        setContentView(view, forceLayout);
    }

    /**
     * Customizes your content view of load footer
     *
     * @param view          Content root view
     * @param forceLayout   Force to layout content view
     */
    public LoadFooter setContentView(View view, boolean forceLayout) {
        removeAllViews();
        addView(view);

        if (forceLayout) {
            requestLayout();
        }

        return this;
    }

    /**
     * Sets vertical alignment
     *
     * @see VerticalAlignment
     * @param alignment The vertical alignment of content view
     */
    public LoadFooter setAlignment(VerticalAlignment alignment) {
        mAlignment = alignment;
        requestLayout();
        return this;
    }

    /**
     * Sets height
     *
     * @param height New height of header
     */
    protected void setHeight(int height) {
        mHeight = height;
        if (mHeight < 0) {
            mHeight = 0;
        }
        requestLayout();
    }

    /**
     * Sets height by offset
     *
     * @param offsetY Height offset
     */
    protected void setHeightBy(int offsetY) {
        final int oldHeight = mHeight;
        mHeight += offsetY;
        if (mHeight < 0) {
            mHeight = 0;
        }

        if (LoadAction.AUTO_LOAD == mLoadAction) {
            if (mHeight < mMinHeight) {
                mHeight = mMinHeight;
            }
        }
        else if (!mIsLoading && null != mOnLoadStateListener) {
            if (mHeight > mMinHeight) {
                mOnLoadStateListener.onWillRelease(getChildView());
            }
            else if ((oldHeight <= 0 && mHeight > 0) ||
                      (oldHeight > mMinHeight && mHeight <= mMinHeight)) {
                mOnLoadStateListener.onPullingUp(getChildView());
            }
        }

        requestLayout();
    }

    /**
     * Gets current height
     *
     * @return Current height
     */
    protected int getCurHeight() {
        return mHeight;
    }

    /**
     * Gets minimum height
     *
     * @return Minimum height
     */
    protected int getMinHeight() {
        return mMinHeight;
    }

    /**
     * Gets bounce height when view start bounce to invisible or minimum
     * height
     *
     * @return Bounce height
     */
    protected int getBounceHeight() {
        if (mHeight > mMinHeight) {
            return mMinHeight - mHeight;
        }
        else if (mIsLoading) {
            return 0;
        }
        else {
            return -mHeight;
        }
    }

    /**
     * Is a visible height
     *
     * @return True if height is bigger than 0
     */
    protected boolean isHeightVisible() {
        return mHeight > 0;
    }

    /**
     * Can load?
     *
     * @return True if state is not loading and height is bigger than minimum
     *              height
     */
    public boolean canLoad() {
        return !mIsLoading && mHeight > mMinHeight;
    }

    /**
     * Is loading ongoing?
     *
     * @return True if the loading is ongoing
     */
    public boolean isLoading() {
        return mIsLoading;
    }

    /**
     * Sets loading status
     *
     * @param isLoading True if loading is ongoing
     */
    public LoadFooter setLoading(boolean isLoading) {
        final boolean old = mIsLoading;
        mIsLoading = isLoading;

        if (null != mOnLoadStateListener && old != mIsLoading) {
            if (mIsLoading) {
                mOnLoadStateListener.onLoading(getChildView());
            }
            else {
                mOnLoadStateListener.onDidLoad(getChildView());
            }
        }

        return this;
    }

    /**
     * Is loading finished and load footer hidden?
     */
    public boolean isFinished() {
        return !mIsLoading && mHeight <= 0;
    }

    /**
     * Sets listener for loading state change
     *
     * @param l Listener
     */
    public LoadFooter setOnLoadStateListener(OnLoadStateListener l) {
        mOnLoadStateListener = l;
        return this;
    }

    /**
     * Gets loading action
     *
     * @see LoadAction
     * @return Load action
     */
    public LoadAction getLoadAction() {
        return mLoadAction;
    }

    /**
     * Sets loading action
     *
     * @see LoadAction
     * @param action Load action
     */
    public LoadFooter setLoadAction(LoadAction action) {
        setClickable(action == LoadAction.CLICK_TO_LOAD);
        mLoadAction = action;
        return this;
    }

    /**
     * initialize
     */
    private void init() {
        mOnLoadStateListener    = new DefaultLoadStateListener();
        mAlignment              = VerticalAlignment.TOP;
        mIsLoading              = false;
        mLoadAction             = LoadAction.AUTO_LOAD;
    }

    /**
     * Listener class for loading state change
     * <p>Defines 4 state for the whole loading process:</p>
     * <ul>
     * <li>Pulling Up: When you're pulling up the footer from the bottom of
     * list</li>
     * <li>WillRelease: When the height of footer is enough high to release your
     * finger for loading</li>
     * <li>Loading: Loading is ongoing</li>
     * <li>DidLoad: Loading is finished</li>
     * </ul>
     * @author eschao
     */
    public interface OnLoadStateListener {

        void onPullingUp(View root);

        void onWillRelease(View root);

        void onLoading(View root);

        void onDidLoad(View root);
    }

    /**
     * A default listener implementation for loading state change
     *
     * @author eschao
     */
    public static class DefaultLoadStateListener implements OnLoadStateListener
    {
        @Override
        public void onPullingUp(View root) {
            View progress = root.findViewById(R.id.load_progressbar);
            progress.setVisibility(View.GONE);

            TextView text = (TextView)root.findViewById(R.id.load_text);
            text.setText(R.string.loading_more);
        }

        @Override
        public void onWillRelease(View root) {
            onPullingUp(root);
        }

        @Override
        public void onLoading(View root) {
            View progress = root.findViewById(R.id.load_progressbar);
            progress.setVisibility(View.VISIBLE);

            TextView text = (TextView)root.findViewById(R.id.load_text);
            text.setText(R.string.loading);
        }

        @Override
        public void onDidLoad(View root) {
            onPullingUp(root);
        }
    }

    /**
     * Defines loading action
     *
     * @author eschao
     */
    public enum LoadAction {

        /**
         * Click loaderView to perform loading
         */
        CLICK_TO_LOAD,

        /**
         * Release touching to perform loading
         */
        RELEASE_TO_LOAD,

        /**
         * Automatically perform loading once the loaderView is visible
         */
        AUTO_LOAD,
    }
}
