package com.hackers.epfl.drawer;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;

import com.google.android.glass.timeline.DirectRenderingCallback;
import com.hackers.epfl.view.TypeChooseView;

/**
 * @author Ivan Gavrilovic
 */
public class TypeChooserDrawer implements DirectRenderingCallback {

	private static final String TAG = TypeChooserDrawer.class.getSimpleName();

	private final TypeChooseView mCountDownView;
	boolean mRenderingPaused = false;

	private SurfaceHolder mHolder;

	public TypeChooserDrawer(Context context) {
		this(new TypeChooseView(context));
	}

	public TypeChooserDrawer(TypeChooseView countDownView) {
		mCountDownView = countDownView;
	}

	/**
	 * Uses the provided {@code width} and {@code height} to measure and layout the inflated
	 */
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		// Measure and layout the view with the canvas dimensions.
		int measuredWidth = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
		int measuredHeight = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);

		mCountDownView.measure(measuredWidth, measuredHeight);
		mCountDownView.layout(	0, 0, mCountDownView.getMeasuredWidth(),
								mCountDownView.getMeasuredHeight());
	}

	/**
	 * Keeps the created {@link SurfaceHolder} and updates this class' rendering state.
	 */
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// The creation of a new Surface implicitly resumes the rendering.
		mHolder = holder;
	}

	/**
	 * Removes the {@link SurfaceHolder} used for drawing and stops rendering.
	 */
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mHolder = null;
	}

	/**
	 * Updates this class' rendering state according to the provided {@code paused} flag.
	 */
	@Override
	public void renderingPaused(SurfaceHolder holder, boolean paused) {
		mRenderingPaused = paused;
	}

	/**
	 * Draws the view in the SurfaceHolder's canvas.
	 */
	private void draw(View view) {
		Canvas canvas;
		try {
			canvas = mHolder.lockCanvas();
		} catch (Exception e) {
			Log.e(TAG, "Unable to lock canvas: " + e);
			return;
		}
		if (canvas != null) {
			view.draw(canvas);
			mHolder.unlockCanvasAndPost(canvas);
		}
	}
}
