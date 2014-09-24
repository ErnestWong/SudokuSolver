package com.example.sudokusolver.android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;

/**
 * Custom view to draw rectangle on preview screen
 * 
 * @author E Wong
 * 
 */
public class RectangleView extends View {

	private Context mContext;
	private Paint mPaint;
	private float mRectDimens = 0;
	private float mTop = 0;
	private float mBottom = 0;
	private float mLeft = 0;
	private float mRight = 0;
	private Rect mRect;
	String s;

	public RectangleView(Context context) {
		super(context);
		mContext = context;
		mPaint = new Paint();

	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		mRectDimens = (float) (this.getWidth() * 0.9);
		mTop = this.getHeight() / 2 - mRectDimens / 2;
		mBottom = mTop + mRectDimens;
		mLeft = (float) (this.getWidth() * 0.05);
		mRight = mLeft + mRectDimens;

		if (mRect == null) {
			mRect = new Rect((int) mLeft, (int) mTop, (int) mRight,
					(int) mBottom);
		}

		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(1);
		canvas.drawRect(mLeft, mTop, mRight, mBottom, mPaint);

		s = String.format("t: %f, b: %f, l: %f, r: %f", mTop, mBottom, mLeft,
				mRight);
		String t = String.format("w: %d, h: %d", this.getWidth(),
				this.getHeight());
		Log.d("ondraw", s);
		Log.d("view dimen", t);

	}

	public Rect getRect() {
		if (mRect == null) {
			invalidate();
		}
		return mRect;

	}

	public float getTopRatio() {
		if (mTop == 0) {
			invalidate();
		}
		return mTop / this.getHeight();
	}

	public float getBottomRatio() {
		if (mBottom == 0) {
			invalidate();
		}
		return mBottom / this.getHeight();
	}

	public float getLeftRatio() {
		if (mLeft == 0) {
			invalidate();
		}
		return mLeft / this.getWidth();
	}

	public float getRightRatio() {
		if (mRight == 0) {
			invalidate();
		}
		return mRight / this.getWidth();
	}

	public void setPaintColor(int color) {
		mPaint.setColor(color);
		invalidate();
	}

}
