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
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.TextView;

/**
 * The update header view extends from ViewGroup and allows you customize your
 * content view
 * <p>It is used in {@link ElasticListView} as a header item to implement
 * updating operation through defined gesture</p>
 *
 * @author chao
 */
public class UpdateHeader extends ViewGroup {

    private final static String TAG = "UpdateHeader";

    int                     mHeight;
    int                     mMinHeight;
    boolean                 mIsUpdating;
    VerticalAlignment       mAlignment;
    OnUpdateStateListener   mOnUpdateStateListener;

    /**
     * Constructor
     *
     * @param context Android context
     */
    public UpdateHeader(Context context) {
        super(context);
        init();
    }

    /**
     * Constructor
     *
     * @param context   Android context
     * @param attrs     attributes of view
     */
    public UpdateHeader(Context context, AttributeSet attrs) {
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
    public UpdateHeader(Context context, AttributeSet attrs, int defStyle) {
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
     * <p>UpdateHeader only can contain one child view. The exception will be
     * raised if you add more than one.</p>
     * <p>
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
            throw new IllegalStateException(TAG + ": Can only have one "
                    + "child view");
        }
        super.addView(child);
    }

    /**
     * Customizes your content view of update header
     *
     * @param resId         Resource id of content view
     * @param forceLayout   Force to layout content view
     */
    public void setContentView(int resId, boolean forceLayout) {
        View view = LayoutInflater.from(getContext()).inflate(resId, null);
        setContentView(view, forceLayout);
    }

    /**
     * Customizes your content view of update header
     *
     * @param view          Content root view
     * @param forceLayout   Force to layout content view
     */
    public UpdateHeader setContentView(View view, boolean forceLayout) {
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
    public UpdateHeader setAlignment(VerticalAlignment alignment) {
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
        if (mHeight <= 0) {
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
        final int old = mHeight;

        mHeight += offsetY;
        if (mHeight <= 0) {
            mHeight = 0;
        }

        // if not updating, handle change event of update state
        if (!mIsUpdating && null != mOnUpdateStateListener) {
            // from PullDown state to PreRelease state
            if (mHeight > mMinHeight && old <= mMinHeight) {
                mOnUpdateStateListener.onWillRelease(getChildView());
            }
            // enter PullDown state
            else if ((old <= 0 && mHeight > 0) ||
                      (old > mMinHeight && mHeight <= mMinHeight)) {
                mOnUpdateStateListener.onPullingDown(getChildView());
            }
        }

        requestLayout();
    }

    /**
     * Gets bounce height when header start bounce to invisible or minimum
     * height
     *
     * @return Bounce height
     */
    protected int getBounceHeight() {
        if (mHeight >= mMinHeight && mIsUpdating) {
            return mMinHeight - mHeight;
        }
        else {
            return -mHeight;
        }
    }

    /**
     * Can update?
     *
     * @return True if not updating state and height is bigger than minimum
     * height
     */
    protected boolean canUpdate() {
        return !mIsUpdating && mHeight > mMinHeight;
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
     * Is a visible height
     *
     * @return True if height is bigger than 0
     */
    protected boolean isHeightVisible() {
        return mHeight > 0;
    }

    /**
     * Is updating ongoing?
     *
     * @return True if the updating is ongoing
     */
    public boolean isUpdating() {
        return mIsUpdating;
    }

    /**
     * Sets updating status
     *
     * @param isUpdating True if updating is ongoing
     */
    public UpdateHeader setUpdating(boolean isUpdating) {
        final boolean old = mIsUpdating;
        mIsUpdating = isUpdating;

        // handle state change event
        if (null != mOnUpdateStateListener && old != mIsUpdating) {
            // from willRelease to Updating
            if (mIsUpdating) {
                mOnUpdateStateListener.onUpdating(getChildView());
            }
            // from Updating to didUpdate
            else {
                mOnUpdateStateListener.onDidUpdate(getChildView());
            }
        }

        return this;
    }

    /**
     * Sets listener for update state change
     *
     * @param l Listener
     */
    public UpdateHeader setOnUpdateStateListener(OnUpdateStateListener l) {
        mOnUpdateStateListener = l;
        return this;
    }

    /**
     * Is updating finished and update header hidden?
     */
    public boolean isFinished() {
        return !mIsUpdating && mHeight <= 0;
    }

    /**
     * initialize
     */
    private void init() {
        mAlignment              = VerticalAlignment.BOTTOM;
        mOnUpdateStateListener  = new DefaultUpdateStateListener();
        mIsUpdating             = false;
    }

    /**
     * Listener class for update state change
     * <p>Defines 4 state for the whole updating process:</p>
     * <ul>
     * <li>Pulling Down: When you're pulling down the header from the top of
     * list</li>
     * <li>PreRelease: When the height of header is enough high to release your
     * finger for updating</li>
     * <li>Updating: Updating is ongoing</li>
     * <li>EndUpdating: Updating is finished</li>
     * </ul>
     * @author chao
     */
    public interface OnUpdateStateListener {

        /**
         * Pulling down
         * @param root Root view of header content
         */
        void onPullingDown(View root);

        /**
         * Will release
         * @param root Root view of header content
         */
        void onWillRelease(View root);

        /**
         * Updating
         * @param root root view of header content
         */
        void onUpdating(View root);

        /**
         * Did update
         * @param root root view of header content
         */
        void onDidUpdate(View root);
    }

    /**
     * A default listener implementation for updating state change
     *
     * @author chao
     */
    public static class DefaultUpdateStateListener implements
            OnUpdateStateListener {

        // Animation for rotating indicator arrow
        RotateAnimation mRotate0To180;
        RotateAnimation mRotate180To0;

        public DefaultUpdateStateListener() {
            mRotate0To180 = new RotateAnimation(0, 180,
                                                Animation.RELATIVE_TO_SELF,
                                                0.5f, Animation.RELATIVE_TO_SELF,
                                                0.5f);
            mRotate0To180.setDuration(250);
            mRotate0To180.setFillAfter(true);

            mRotate180To0 = new RotateAnimation(180, 0,
                                                Animation.RELATIVE_TO_SELF, 0.5f,
                                                Animation.RELATIVE_TO_SELF,
                                                0.5f);
            mRotate180To0.setDuration(250);
            mRotate180To0.setFillAfter(true);
        }

        @Override
        public void onPullingDown(View root) {
            TextView text = (TextView)root.findViewById(R.id.update_text);
            text.setText(R.string.pulldown_to_refresh);

            View arrow = root.findViewById(R.id.update_arrow);
            if (null != arrow) {
                arrow.setVisibility(View.VISIBLE);
                arrow.startAnimation(mRotate0To180);
            }
        }

        @Override
        public void onWillRelease(View root) {
            TextView text = (TextView)root.findViewById(R.id.update_text);
            text.setText(R.string.release_to_refresh);

            View arrow = root.findViewById(R.id.update_arrow);
            if (null != arrow) {
                arrow.setVisibility(View.VISIBLE);
                arrow.startAnimation(mRotate180To0);
            }
        }

        @Override
        public void onUpdating(View root) {
            TextView text = (TextView)root.findViewById(R.id.update_text);
            text.setText(R.string.updating);

            View arrow = root.findViewById(R.id.update_arrow);
            if (null != arrow) {
                arrow.setVisibility(View.GONE);
                arrow.clearAnimation();
            }

            View progress = root.findViewById(R.id.update_progressbar);
            if (null != progress) {
                progress.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onDidUpdate(View root) {
            TextView text = (TextView)root.findViewById(R.id.update_text);
            text.setText(R.string.pulldown_to_refresh);

            View arrow = root.findViewById(R.id.update_arrow);
            if (null != arrow) {
                arrow.setVisibility(View.VISIBLE);
                arrow.clearAnimation();
            }

            View progress = root.findViewById(R.id.update_progressbar);
            if (null != progress) {
                progress.setVisibility(View.GONE);
            }
        }
    }
}
