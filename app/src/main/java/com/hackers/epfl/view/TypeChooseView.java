package com.hackers.epfl.view;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.hackers.epfl.R;

/**
 * @author Ivan Gavrilovic
 */
public class TypeChooseView extends FrameLayout {
	/**
	 * Interface to listen for changes on the view layout.
	 */
	public interface Listener {
		/** Notified of a change in the view. */
		public void onChange();
	}

	private final TextView notificationCount;
	private static final int DELAY_MILLIS = 41;

	private final Handler mHandler = new Handler();
	private final Runnable mUpdateTextRunnable = new Runnable() {

		@Override
		public void run() {
			if (mRunning) {
				notificationCount.setText("0 messages");
				postDelayed(mUpdateTextRunnable, DELAY_MILLIS);
			}
		}
	};

	private boolean mStarted;
	private boolean mForceStart;
	private boolean mVisible;
	private boolean mRunning;

	private long mBaseMillis;

	private Listener mChangeListener;

	public TypeChooseView(Context context) {
		this(context, null, 0);
	}

	public TypeChooseView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TypeChooseView(Context context, AttributeSet attrs, int style) {
		super(context, attrs, style);
		LayoutInflater.from(context).inflate(R.layout.activity_main, this);

		notificationCount = (TextView) findViewById(R.id.notification_count);
		setBasicValue(0);
	}

	/**
	 * Sets the base value of the chronometer in milliseconds.
	 */
	public void setBasicValue(long baseMillis) {
		notificationCount.setText("0 messages");
	}

	/**
	 * Sets a {@link Listener}.
	 */
	public void setListener(Listener listener) {
		mChangeListener = listener;
	}

	/**
	 * Returns the set {@link Listener}.
	 */
	public Listener getListener() {
		return mChangeListener;
	}

	@Override
	public boolean postDelayed(Runnable action, long delayMillis) {
		return mHandler.postDelayed(action, delayMillis);
	}

	@Override
	public boolean removeCallbacks(Runnable action) {
		mHandler.removeCallbacks(action);
		return true;
	}
}
