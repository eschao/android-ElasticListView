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

import com.eschao.android.widget.elasticlistview.FooterView.OnLoadStateListener;
import com.eschao.android.widget.elasticlistview.HeaderView.OnUpdateStateListener;

/**
 * <p>ElasticListView, like iOS feature, allow you pull down from the top of
 * ListView to update data and pull up from the bottom of ListView to load data.
 * It provides default header and footer for updating and loading, you can
 * customize their UI and listen their state changing events to show different
 * hints.</p>
 * <p>ElasticListView is easy to use, there are some simples steps and examples
 * for you:</p>
 * <ul>
 * <li>Use it in XML resource like native ListView, All attributes of native
 * ListView are supported</li>
 * <li>Gets ElasticListView object by {@link android.view.View#findViewById(int)}
 * in your activity</li>
 * <li><p>Set updating and loading listener through {@link #setOnUpdateListener
 * (OnUpdateListener)} and
 * {@link #setOnLoadListener(OnLoadListener)}, your updating and loading
 * operations should be run in a separate thread and notify ElasticListView
 * through {@link #notifyUpdated()} and {@link #notifyLoaded()}
 * if the task is finished</p></li>
 * <li>Set content view for header by {@link #setUpdateHeaderView(int, boolean)}
 * or {@link #setUpdateHeaderView(View, boolean)} if need</li>
 * <li>Set content view for footer by {@link #setLoadFooterView(int, boolean)}
 * or {@link #setLoadFooterView(View, boolean)} if need</li>
 * <li><p>Set alignment for header and footer through {@link
 * #setUpdateHeaderAlignment(VerticalAlignment)} and {@link
 * #setLoadFooterAlignment(VerticalAlignment)} if need</p></li>
 * <li>Set loading mode for footer through {@link #setLoadFooterMode(LoadMode)}
 * if need</li>
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
 * 		&lt;com...elastic.listview.ElasticListView
 * 		xmlns:android="http://schemas.android.com/apk/res/android"
 *			xmlns:tools="http://schemas.android.com/tools"
 *			android:layout_width="match_parent"
 *			android:layout_height="match_parent"
 *			android:background="@android:color/white"
 *			android:id="@+id/elasticList"&gt;
 *
 *		&lt;/com...elastic.listview.ElasticListView&gt;
 * </pre>
 * <p>Codes example:</p>
 * <pre>
 * 		ElasticListView mListView;
 *
 * 		protected void onCreate(Bundle savedInstanceState) {
 * 			...
 * 			mListView = (ElasticListView)findViewById(R.id.elasticList);
 * 			mListView.setOnUpdateListener(this);
 *			mListView.setOnLoadListener(this);
 *			mListView.setUpdateHeaderAlignment(VerticalAlignment.CENTER);
 *			mListView.setLoadFooterAlignment(VerticalAlignment.CENTER);
 *			mListView.setLoadFooterMode(LoadMode.AUTO_LOAD);
 *			mListView.enableLoadFooter(true);
 *			...
 *			<some ListView setting, e.g: set adapter, set item click listener>
 *		}
 *
 *		// If you need to automatically run updating operation once the ListView
 *		// is shown,
 *		// you can call requestUpdate() here or in onStart()
 *		protected void onResume() {
 *			super.onResume();
 *			mListView.requestUpdate();
 *		}
 * </pre>
 *
 * @see android.widget.ListView
 * @see FooterView
 * @see HeaderView
 * @author eschao
 * @since Android API level 9
 * @version 1.0
 */
public class ElasticListView extends ListView {

	// defines messages
	final static int MSG_SET_UPDATE_HEADER_HEIGHT		= 0;
	final static int MSG_END_UPDATING					= 1;
	final static int MSG_BEGIN_UPDATE					= 2;
	final static int MSG_SET_LOAD_FOOTER_HEIGHT			= 3;
	final static int MSG_END_LOADING					= 4;
	final static int MSG_BEGIN_LOAD						= 5;

	int 				mLastY;
	HeaderView 			mHeaderView;
	FooterView 			mFooterView;
	Scroller			mScroller;
	OnUpdateListener	mOnUpdateListener;
	OnLoadListener		mOnLoadListener;
	boolean				mUpdateRequest;
	boolean				mEnableLoadFooter;
	Typeface			mFont;

	/**
	 * Constructor
	 *
	 * @param context	Android context
	 */
	public ElasticListView(Context context) {
		super(context);
		init();
	}

	/**
	 * Constructor
	 *
	 * @param context	Android context
	 * @param attrs		Attributes of ListView
	 */
	public ElasticListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	/**
	 * Constructor
	 *
	 * @param context	Android context
	 * @param attrs		Attributes of ListView
	 * @param defStyle	Styles of ListView
	 */
	public ElasticListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	/**
	 * Sets updating listener
	 *
	 * @param l Updating listener
	 */
	public void setOnUpdateListener(OnUpdateListener l) {
		mOnUpdateListener = l;
	}

	/**
	 * Sets loading listener
	 *
	 * @param l Loading listener
	 */
	public void setOnLoadListener(OnLoadListener l) {
		mOnLoadListener = l;
	}

	/**
	 * Notifies the updating is completed
	 * <p>When your updating task is finished, you should call it to notify
     * ElasticListView to accordingly change the status of updating header</p>
	 */
	public void notifyUpdated() {
		mHandler.sendEmptyMessage(MSG_END_UPDATING);
	}

	/**
	 * Notifies the loading is completed
	 * <p>When your loading task is finished, you should call it to notify
     * ElasticListView to accordingly change the status of loading footer</p>
	 */
	public void notifyLoaded() {
		mHandler.sendEmptyMessage(MSG_END_LOADING);
	}

	/**
	 * Sets listener for updating state changes
	 *
	 * @param l Listener
	 */
	public void setOnUpdateStateListener(OnUpdateStateListener l) {
		mUpdateHeader.setOnUpdateStateListener(l);
	}

	/**
	 * Sets listener for loading state changes
	 *
	 * @param l Listener
	 */
	public void setOnLoadStateListener(OnLoadStateListener l) {
		mFooterView.setOnLoadStateListener(l);
	}

	/**
	 * Sets vertical alignment of content view in updating header
	 * <p>The alignment can be: TOP, CENTER or BOTTOM. See
     * {@link VerticalAlignment}</p>
	 *
	 * @param alignment Vertical alignment
	 */
	public void setUpdateHeaderAlignment(VerticalAlignment alignment) {
		mUpdateHeader.setAlignment(alignment);
	}

	public void setFont(Typeface tf) {
		mFont = tf;
		if (null != mFont) {
			setFont(mUpdateHeader);
			setFont(mFooterView);
		}
	}
	/**
	 * Sets vertical alignment of content view in loading footer
	 *
	 * @param alignment One of {@link VerticalAlignment#TOP},
     *                  {@link VerticalAlignment#CENTER} or
	 * 					{@link VerticalAlignment#BOTTOM}
	 */
	public void setLoadFooterAlignment(VerticalAlignment alignment) {
		mFooterView.setAlignment(alignment);
	}

	/**
	 * Requests a manual updating operation
	 * <p>The updating operation will be only executed once when the ListView is
     * shown, The operation will be preserved if the ListView is not ready and
     * be executing later. You can call it in some startup points to achieve an
	 * automatic updating once the ListView is displayed</p>
	 */
	public void requestUpdate() {
		if (null == mUpdateHeader.getChildView() || null != mOnUpdateListener) {
			mUpdateRequest = true;
		} else {
			final int minHeight = mUpdateHeader.getMinHeight();
			mUpdateHeader.setHeight(minHeight);
			mUpdateHeader.isUpdating(true);
			mOnUpdateListener.onUpdate();
		}
	}

	/**
	 * Sets loading mode for loading footer
	 * <p>There are 3 modes defined in {@link LoadMode}, you can set your
     * desirable mode for loading operation</p>
	 *
	 * @param mode One of {@link LoadMode#CLICK_TO_LOAD},
     *             {@link LoadMode#RELEASE_TO_LOAD} or
     *             {@link LoadMode#AUTO_LOAD}
	 */
	public void setLoadFooterMode(LoadMode mode) {
		mFooterView.setClickable(LoadMode.CLICK_TO_LOAD == mode);
		mFooterView.setLoadMode(mode);
	}

	/**
	 * Customize your content view of update header
	 *
	 * @param resId			Resource id of content view
	 * @param forceLayout	Force to layout content view
	 */
	public void setUpdateHeaderView(int resId, boolean forceLayout) {
		View view = LayoutInflater.from(getContext()).inflate(resId, null);
		setUpdateHeaderView(view, forceLayout);
	}

	/**
	 * Customize your content view of update header
	 *
	 * @param view			Content root view
	 * @param forceLayout	Force to layout content view
	 */
	public void setUpdateHeaderView(View view, boolean forceLayout) {
		mUpdateHeader.removeAllViews();
		mUpdateHeader.addView(view);

		if (null != mFont) {
			setFont(mUpdateHeader);
		}

		if (forceLayout) {
			mUpdateHeader.requestLayout();
		}
	}

	/**
	 * Customize your content view of load footer
	 *
	 * @param resId			Resource id of content view
	 * @param forceLayout	Force to layout content view
	 */
	public void setLoadFooterView(int resId, boolean forceLayout) {
		View view = LayoutInflater.from(getContext()).inflate(resId, null);
		setLoadFooterView(view, forceLayout);
	}

	/**
	 * Customize your content view of load footer
	 *
	 * @param view			Content root view
	 * @param forceLayout	Force to layout content view
	 */
	public void setLoadFooterView(View view, boolean forceLayout) {
		mFooterView.removeAllViews();
		mFooterView.addView(view);

		if (null != mFooterView) {
			setFont(mFooterView);
		}

		if (forceLayout) {
			mFooterView.requestLayout();
		}
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
		return mEnableLoadFooter;
	}

	/**
	 * Enables or disables load footer
	 *
	 * @param enable True if enable load footer
	 */
	public void enableLoadFooter(boolean enable) {
		mEnableLoadFooter = enable;
	}

	/**
	 * Overrides {@link android.view.ViewGroup#dispatchDraw(Canvas)} to run a
     * manual updating operation if requested
	 */
	@Override
	public void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);

		// if request a manual updating, execute it
		if (mUpdateRequest) {
			mUpdateRequest = false;

			if (null != mOnUpdateListener) {
				mUpdateHeader.setHeight(mUpdateHeader.getMinHeight());
				mUpdateHeader.isUpdating(true);
				mOnUpdateListener.onUpdate();
			}
		}
	}

	/**
	 * Overrides {@link android.view.View#dispatchTouchEvent(MotionEvent)}
	 */
	@Override
	public boolean dispatchTouchEvent(MotionEvent e) {
		int action 	= e.getAction();
		int y		= (int)e.getRawY();

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
		int action 	= e.getAction();
		int y		= (int)e.getRawY();

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			// do nothing, we handled it in dispatchTouchEvent()
			break;

		case MotionEvent.ACTION_MOVE:
			int deltaY = y-mLastY;

			// is operating on update header or is update header visible or is
            // it can be showing?
			if (mFooterView.isFinished() &&
				(mUpdateHeader.isHeightVisible()
                 || canShowUpdateHeader(deltaY))) {
				// set half of movements as its height to simulate a elastic
                // effect
				mUpdateHeader.setHeightBy(deltaY/2);
				mLastY = y;

				// don't let parent continue to handle this message
				return true;
			}

			// is operating on update header or is update header visible or is
            // it can be showing?
			if (mEnableLoadFooter && mUpdateHeader.isFinished() &&
				(mFooterView.isHeightVisible() || canShowLoadFooter(deltaY))) {
				// set half of movements as its height to simulate a elastic
                // effect
				mFooterView.setHeightBy(-deltaY/2);

				// if AUTO_LOAD mode is set, execute loading operation
				if (LoadMode.AUTO_LOAD == mFooterView.getLoadMode() &&
					null != mOnLoadListener && !mFooterView.isLoading()) {
					mFooterView.isLoading(true);
					mOnLoadListener.onLoad();
				}

				if (mFooterView.getCurHeight() > 0) {
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
			if (mUpdateHeader.isHeightVisible()) {
				// check if can execute updating
				if (mUpdateHeader.canUpdate() && null != mOnUpdateListener) {
					mUpdateHeader.isUpdating(true);
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

			if (mFooterView.isHeightVisible()) {
				// check if can execute updating, as we explain in ACTION_MOVE,
                // we should use the real visible height of footer to judge if
                // the loading operation can be executing
				if (mFooterView.canLoad()
                    && !mFooterView.isClickable()
                    && null != mOnLoadListener) {
					mFooterView.isLoading(true);
					mOnLoadListener.onLoad();
				}

				// compute bounce distance and start a scroll controller
				final int dy = mFooterView.getBounceHeight();
				mScroller.startScroll(0, mFooterView.getCurHeight(), 0, dy,
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
    	if (deltaY < 0 && mScroller.isFinished() && mFooterView.isFinished()
    		&& !mUpdateHeader.isHeightVisible()) {
			mUpdateHeader.setHeightBy(-deltaY);

			// check if we can run updating operation
			if (mUpdateHeader.canUpdate() && null != mOnUpdateListener) {
				mUpdateHeader.isUpdating(true);
				mOnUpdateListener.onUpdate();
			}

			// compute bounce distance and start a scroll controller
			final int dy = mUpdateHeader.getBounceHeight();
			mScroller.startScroll(0, mUpdateHeader.getCurHeight(), 0, dy, 1000);
			mHandler.sendEmptyMessage(MSG_SET_UPDATE_HEADER_HEIGHT);
    	}

    	// the deltaY > 0 means the scrolling is from bottom to top
    	if (deltaY > 0 && mEnableLoadFooter && mUpdateHeader.isFinished()
    		&& !mFooterView.isHeightVisible() && mScroller.isFinished()
            && isItemFilledScreen()) {
    		mFooterView.setHeightBy(deltaY);

    		// use the magic function to make sure footer is added in current
            // showing view list
			if (mFooterView.getCurHeight() > 0) {
				setSelection(getCount());
			}

			// check if we can run loading operations
			if (null != mOnLoadListener && !mFooterView.isLoading() &&
				(LoadMode.AUTO_LOAD == mFooterView.getLoadMode() ||
				(mFooterView.canLoad() && !mFooterView.isClickable()))) {
				mFooterView.isLoading(true);
				mOnLoadListener.onLoad();
			}

			// compute bounce distance and start a scroll controller
			final int dy = mFooterView.getBounceHeight();
			mScroller.startScroll(0, mFooterView.getCurHeight(), 0, dy, 1000);
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
		mFooterView = new FooterView(getContext());
		view = LayoutInflater.from(context).inflate(R.layout.load_footer, null);
		mFooterView.addView(view);
		addFooterView(mFooterView);
		mFooterView.setOnClickListener(mLoadClickListener);

		// others
		mScroller 			= new Scroller(context);
		mUpdateRequest 		= false;
		mEnableLoadFooter	= false;
		mFont				= null;
		setFooterDividersEnabled(false);
		setHeaderDividersEnabled(false);
	}

	/**
	 * Checks if the update header can be showing?
	 *
	 * @param 	deltaY delta vertical movement
	 * @return	True if it can be showing
	 */
	private boolean canShowUpdateHeader(int deltaY) {
		final int firstVisibleItem	= getFirstVisiblePosition();
		final int fistViewTop		= getChildAt(0).getTop();
		final int topPadding		= getListPaddingTop();
		return (firstVisibleItem == 0
                && fistViewTop >= topPadding
                && deltaY > 0);
	}

	/**
	 * Checks if the load footer can be showing?
	 *
	 * @param 	deltaY delta vertical movement
	 * @return	True if it can be showing
	 */
	private boolean canShowLoadFooter(int deltaY) {
		final int itemsCount = getCount();
		// won't show footer if no item
		if (itemsCount < 0)
			return false;

		final int viewsCount		= getChildCount();
		final int firstVisibleItem	= getFirstVisiblePosition();
		final int lastVisibleItem	= getLastVisiblePosition();
		// won't show footer if the list items can not fill the screen
		if (lastVisibleItem-firstVisibleItem+1 >= itemsCount)
			return false;

		final int lastViewBottom	= getChildAt(viewsCount-1).getBottom();
		final int listBottom		= getHeight()-getListPaddingBottom();
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
		return mFooterView == getChildAt(getChildCount()-1);
	}

    public final boolean isUpdating() {
        return (mUpdateHeader == getChildAt(0) && mUpdateHeader.getHeight() > 0);
    }

    public final boolean isLoading() {
        return (mFooterView == getChildAt(0) && mFooterView.getHeight() > 0);
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
				mFooterView.isLoading(true);
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
					} else if (!isUpdateHeaderVisible()) {
						mUpdateHeader.setHeight(0);
					} else {
						mHandler.sendEmptyMessage(MSG_SET_UPDATE_HEADER_HEIGHT);
					}
				}
				break;

			case MSG_END_UPDATING:
				if (mUpdateHeader.isUpdating()) {
					mUpdateHeader.isUpdating(false);
					// if the height of header is valid, start scroll to
                    // hide header
					if (isUpdateHeaderVisible()) {
						height = mUpdateHeader.getCurHeight();
						mScroller.startScroll(0, height, 0, -height);
						mHandler.sendEmptyMessage(MSG_SET_UPDATE_HEADER_HEIGHT);
					} else {
						mUpdateHeader.setHeight(0);
					}
				}
				break;

			case MSG_SET_LOAD_FOOTER_HEIGHT:
				// handles the height updating of footer
				mScroller.computeScrollOffset();
				y = mScroller.getCurrY();
				mFooterView.setHeight(y);

				if (!mScroller.isFinished()) {
					if (y <= 0) {
						mScroller.abortAnimation();
					} else if (!isLoadFooterVisible()) {
						mFooterView.setHeight(0);
					} else {
						mHandler.sendEmptyMessage(MSG_SET_LOAD_FOOTER_HEIGHT);
					}
				}
				break;

			case MSG_END_LOADING:
				if (mFooterView.isLoading()) {
					mFooterView.isLoading(false);
					// hide footer if need
					if (isLoadFooterVisible()) {
						height = mFooterView.getCurHeight();
						mScroller.startScroll(0, height, 0, -height);
						mHandler.sendEmptyMessage(MSG_SET_LOAD_FOOTER_HEIGHT);
					} else {
						mFooterView.setHeight(0);
					}
				}
				break;
			}

		}
	};
}
