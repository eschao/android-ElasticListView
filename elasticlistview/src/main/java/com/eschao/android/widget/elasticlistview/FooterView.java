package com.eschao.android.widget.elasticlistview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.eschao.android.widget.R;

/**
 * The load footer view extends from ViewGroup to allow you customize your
 * content view
 * <p>It is used in {@link ElasticListView} as a footer item to implement
 * loading operation through defined gesture</p>
 *
 * @author chao
 */
public class FooterView extends ViewGroup {

	int 				mHeight;
	int 				mMinHeight;
	boolean				mIsLoading;
	LoadAction			mLoadAction;
	VerticalAlignment	mAlignment;
	OnLoadStateListener mOnLoadStateListener;

	/**
	 * Constructor
	 *
	 * @param context Android context
	 */
	public FooterView(Context context) {
		super(context);
		init();
	}

	/**
	 * Constructor
	 *
	 * @param context	Android context
	 * @param attrs		attributes of view
	 */
	public FooterView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	/**
	 * Constructor
	 *
	 * @param context	Android context
	 * @param attrs		Attributes of view
	 * @param defStyle	Style of view
	 */
	public FooterView(Context context, AttributeSet attrs, int defStyle) {
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
		if (null == view)
			return;

		// get measured width and height
		int childWidth 	= view.getMeasuredWidth();
		int childHeight	= view.getMeasuredHeight();
		int height		= getMeasuredHeight();

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
		int count = getChildCount();
		if (count > 0)
			return getChildAt(0);

		return null;
	}

	/**
	 * Adds child view
	 * <p>LoadFooter only can contain one child view. The exception will be
	 * raised if you add more than one </p>
	 *
	 * @param child Child view
	 */
	@Override
	public void addView(View child) {
		final int childCount = getChildCount();
		if (childCount > 0) {
			throw new IllegalStateException("LoadFooter can only have one child"
					+ " view");
		}
		super.addView(child);
	}

	/**
	 * Sets vertical alignment
	 *
	 * @see VerticalAlignment
	 * @param alignment The vertical alignment of content view
	 */
	public void setAlignment(VerticalAlignment alignment) {
		mAlignment = alignment;
		requestLayout();
	}

	/**
	 * Sets height
	 *
	 * @param height New height of header
	 */
	public void setHeight(int height) {
		mHeight = height;
		if (mHeight < 0)
			mHeight = 0;

		requestLayout();
	}

	/**
	 * Sets height by offset
	 *
	 * @param offsetY Height offset
	 */
	public void setHeightBy(int offsetY) {
		final int oldHeight = mHeight;
		mHeight += offsetY;
		if (mHeight < 0)
			mHeight = 0;

		if (LoadAction.AUTO_LOAD == mLoadAction) {
			if (mHeight < mMinHeight) {
				mHeight = mMinHeight;
			}
		} else if (!mIsLoading && null != mOnLoadStateListener) {
			if (mHeight > mMinHeight) {
				mOnLoadStateListener.onPreRelease(getChildView());
			} else if ((oldHeight <= 0 && mHeight > 0) ||
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
	public int getCurHeight() {
		return mHeight;
	}

	/**
	 * Gets minimum height
	 *
	 * @return Minimum height
	 */
	public int getMinHeight() {
		return mMinHeight;
	}

	/**
	 * Gets bounce height when header start bounce to invisible or minimum
	 * height
	 *
	 * @return Bounce height
	 */
	public int getBounceHeight() {
		if (mHeight > mMinHeight)
			return mMinHeight - mHeight;

		if (mIsLoading)
			return 0;

		return -mHeight;
	}

	/**
	 * Is a visible height
	 *
	 * @return True if height is bigger than 0
	 */
	public boolean isHeightVisible() {
		return mHeight > 0;
	}

	/**
	 * Can load?
	 *
	 * @return True if not loading state and height is bigger than minimum
	 * 				height
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
	public void isLoading(boolean isLoading) {
		final boolean old = mIsLoading;
		mIsLoading = isLoading;

		if (null != mOnLoadStateListener && old != mIsLoading) {
			if (mIsLoading) {
				mOnLoadStateListener.onLoading(getChildView());
			} else {
				mOnLoadStateListener.onEndLoading(getChildView());
			}
		}

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
	public void setOnLoadStateListener(OnLoadStateListener l) {
		mOnLoadStateListener = l;
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
	 * @param mode Load action
	 */
	public void setLoadAction(LoadAction action) {
		mLoadAction = action;
	}

	/**
	 * initialize
	 */
	private void init() {
		mOnLoadStateListener 	= new DefaultLoadStateListener();
		mAlignment 				= VerticalAlignment.TOP;
		mIsLoading				= false;
		mLoadAction				= LoadAction.AUTO_LOAD;
	}

	/**
	 * Listener class for loading state change
	 * <p>Defines 4 state for the whole loading process:</p>
	 * <ul>
	 * <li>Pulling Up: When you're pulling up the footer from the bottom of
	 * list</li>
	 * <li>PreRelease: When the height of footer is enough high to release your
	 * finger for loading</li>
	 * <li>Loading: Loading is ongoing</li>
	 * <li>EndLoading: Loading is finished</li>
	 * </ul>
	 * @author chao
	 */
	public static interface OnLoadStateListener {

		public void onPullingUp(View root);

		public void onPreRelease(View root);

		public void onLoading(View root);

		public void onEndLoading(View root);
	}

	/**
	 * A default listener implementation for loading state change
	 *
	 * @author chao
	 */
	public static class DefaultLoadStateListener implements OnLoadStateListener
	{
		@Override
		public void onPullingUp(View root) {
			View progress = root.findViewById(R.id.load_progressbar);
			progress.setVisibility(View.GONE);

			TextView text = (TextView)root.findViewById(R.id.load_text);
			text.setText("Load More...");
		}

		@Override
		public void onPreRelease(View root) {
			onPullingUp(root);
		}

		@Override
		public void onLoading(View root) {
			View progress = root.findViewById(R.id.load_progressbar);
			progress.setVisibility(View.VISIBLE);

			TextView text = (TextView)root.findViewById(R.id.load_text);
			text.setText("Loading ......");
		}

		@Override
		public void onEndLoading(View root) {
			onPullingUp(root);
		}
	}

	/**
	 * Defines loading action for load footer
	 *
	 * @author chao
	 */
	public static enum LoadAction {

		/**
		 * Click footer to perform loading
		 */
		CLICK_TO_LOAD,

		/**
		 * Release touching to perform loading
		 */
		RELEASE_TO_LOAD,

		/**
		 * Automatically perform loading once the footer is visible
		 */
		AUTO_LOAD,
	}
}
