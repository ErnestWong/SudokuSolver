package com.example.sudokusolver;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

public class RectangleView extends View{

	private Context mContext;
	private Paint mPaint;
	private float mRectDimens;
	private float mTop;
	private float mBottom;
	private float mLeft;
	private float mRight;
	String s;
	
	public RectangleView(Context context) {
		super(context);
		mContext = context;
		mPaint = new Paint();
		
	}

	@Override
	protected void onDraw(Canvas canvas){
		super.onDraw(canvas);
		
		mRectDimens = (float) (this.getWidth() * 0.9);
		mTop = this.getHeight() / 2 - mRectDimens / 2;
		mBottom = mTop + mRectDimens;
		mLeft = (float) (this.getWidth() * 0.05);
		mRight = mLeft + mRectDimens;
		
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(1);
		canvas.drawRect(mLeft, mTop, mRight, mBottom, mPaint);
		
		s = String.format("t: %f, b: %f, l: %f, r: %f", mTop, mBottom, mLeft, mRight);
		String t = String.format("w: %d, h: %d", this.getWidth(), this.getHeight());
		Log.d("ondraw", s);
		Log.d("ondraw dimen", t);
		
	}
	
	public void setPaintColor(int color){
		mPaint.setColor(color);
		invalidate();
	}
	
}
