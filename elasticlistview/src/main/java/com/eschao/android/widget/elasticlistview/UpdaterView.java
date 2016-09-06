package com.eschao.android.widget.elasticlistview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.TextView;

import com.eschao.android.widget.R;

/**
 * The update header view extends from ViewGroup to allow you customize your
 * content view
 * <p>It is used in {@link ElasticListView} as a header item to implement
 * updating operation through defined gesture</p>
 *
 * @author chao
 */
public class UpdaterView extends ViewGroup {

	int 					mHeight;
	int 					mMinHeight;
	boolean 				mIsUpdating;
	VerticalAlignment		mAlignment;
	OnUpdateStateListener	mOnUpdateStateListener;

	/**
	 * Constructor
	 *
	 * @param context Android context
	 */
	public UpdaterView(Context context) {
		super(context);
		init();
	}

	/**
	 * Constructor
	 *
	 * @param context	Android context
	 * @param attrs		attributes of view
	 */
	public UpdaterView(Context context, AttributeSet attrs) {
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
	public UpdaterView(Context context, AttributeSet attrs, int defStyle) {
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
	 * <p>UpdateHeader only can contain one child view. The exception will be
	 * raised if you add more than one </p>
	 *
	 * @param child Child view
	 */
	@Override
	public void addView(View child) {
		final int childCount = getChildCount();
		if (childCount > 0) {
			throw new IllegalStateException("UpdateHeader: Can only have one " +
					"child view");
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
	public void setHeightBy(int offsetY) {
		final int old = mHeight;

		mHeight += offsetY;
		if (mHeight <= 0) {
			mHeight = 0;
		}

		// if not updating, handle change event of update state
		if (!mIsUpdating && null != mOnUpdateStateListener) {
			// from PullDown state to PreRelease state
			if (mHeight > mMinHeight && old <= mMinHeight) {
				mOnUpdateStateListener.onPreRelease(getChildView());
			// enter PullDown state
			} else if ((old <= 0 && mHeight > 0) ||
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
	public int getBounceHeight() {
		if (mHeight >= mMinHeight && mIsUpdating)
			return mMinHeight-mHeight;

		return -mHeight;
	}

	/**
	 * Can update?
	 *
	 * @return True if not updating state and height is bigger than minimum
	 * height
	 */
	public boolean canUpdate() {
		return !mIsUpdating && mHeight > mMinHeight;
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
	 * Is a visible height
	 *
	 * @return True if height is bigger than 0
	 */
	public boolean isHeightVisible() {
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
	public void isUpdating(boolean isUpdating) {
		final boolean old = mIsUpdating;
		mIsUpdating = isUpdating;

		// handle state change event
		if (null != mOnUpdateStateListener && old != mIsUpdating) {
			if (mIsUpdating) {
				// from PreRelease to Updating
				mOnUpdateStateListener.onUpdating(getChildView());
			} else {
				// from Updating to EndUpdating
				mOnUpdateStateListener.onEndUpdating(getChildView());
			}
		}

	}

	/**
	 * Sets listener for update state change
	 *
	 * @param l Listener
	 */
	public void setOnUpdateStateListener(OnUpdateStateListener l) {
		mOnUpdateStateListener = l;
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
		mAlignment 				= VerticalAlignment.BOTTOM;
		mOnUpdateStateListener 	= new DefaultUpdateStateListener();
		mIsUpdating 			= false;
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
	public static interface OnUpdateStateListener {

		/**
		 * Pulling down
		 * @param root Root view of header content
		 */
		public void onPullingDown(View root);

		/**
		 * Prepare release
		 * @param root Root view of header content
		 */
		public void onPreRelease(View root);

		/**
		 * Updating
		 * @param root root view of header content
		 */
		public void onUpdating(View root);

		/**
		 * End updating
		 * @param root root view of header content
		 */
		public void onEndUpdating(View root);
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
			text.setText("Pull down to refresh...");

			View arrow = root.findViewById(R.id.update_arrow);
			if (null != arrow) {
				arrow.setVisibility(View.VISIBLE);
				arrow.startAnimation(mRotate0To180);
			}
		}

		@Override
		public void onPreRelease(View root) {
			TextView text = (TextView)root.findViewById(R.id.update_text);
			text.setText("Release to refresh...");

			View arrow = root.findViewById(R.id.update_arrow);
			if (null != arrow) {
				arrow.setVisibility(View.VISIBLE);
				arrow.startAnimation(mRotate180To0);
			}
		}

		@Override
		public void onUpdating(View root) {
			TextView text = (TextView)root.findViewById(R.id.update_text);
			text.setText("Updating...");

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
		public void onEndUpdating(View root) {
			TextView text = (TextView)root.findViewById(R.id.update_text);
			text.setText("Pull down to refresh...");

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
