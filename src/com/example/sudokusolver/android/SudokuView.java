package com.example.sudokusolver.android;

import com.example.sudokusolver.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;

public class SudokuView extends View {
	public static final int BOLD_WIDTH = 5;
	public static final int BORDER_WIDTH = 2;
	private Paint mBorderLine;
	private Paint mBoldLine;
	private Paint mGreyLine;
	private Paint mTextBold;
	private Paint mTextNormal;
	private float top, bottom, left, right;
	private float rectDimens;
	private float width, height;

	private int[][] mSolved;
	private int[][] mUnsolved;

	public SudokuView(Context context, int[][] unsolved, int[][] solved) {
		super(context);
		mSolved = solved;
		mUnsolved = unsolved;

		mBorderLine = new Paint();
		mBorderLine.setColor(getResources().getColor(R.color.black));
		mBorderLine.setStrokeWidth(BORDER_WIDTH);

		mBoldLine = new Paint();
		mBoldLine.setColor(getResources().getColor(R.color.black));
		mBoldLine.setStrokeWidth(BOLD_WIDTH);

		mGreyLine = new Paint();
		mGreyLine.setColor(getResources().getColor(R.color.grey));

		mTextBold = new Paint();

		mTextNormal = new Paint();
	}

	@Override
	protected void onDraw(Canvas canvas) {

		// get dimensions for sudoku grid
		rectDimens = (float) (this.getWidth() * 0.9);
		top = this.getHeight() / 2 - rectDimens / 2;
		bottom = top + rectDimens;
		left = (float) (this.getWidth() * 0.05);
		right = left + rectDimens;

		width = right - left;
		height = bottom - top;

		// draw lines
		drawGreyLines(canvas);
		drawBoldLines(canvas);
		drawBorderLines(canvas);

		// set up paint for text
		mTextBold.setTextAlign(Paint.Align.CENTER);
		mTextBold.setTextSize(width / 9 * 0.75f);
		mTextBold.setTypeface(Typeface.DEFAULT_BOLD);

		mTextNormal.setTextAlign(Paint.Align.CENTER);
		mTextNormal.setTextSize(width / 9 * 0.75f);
		mTextNormal.setColor(Color.GRAY);

		// find offset to center line
		FontMetrics fm = mTextBold.getFontMetrics();
		float offset = (fm.ascent + fm.descent) / 2;

		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				Log.d("array: " + i + "," + j, mSolved[i][j] + ","
						+ mUnsolved[i][j]);
			}
		}
		drawDigits(canvas, offset);
	}

	/**
	 * draws the digits to the sudoku grid
	 * 
	 * @param canvas
	 * @param offset
	 */
	private void drawDigits(Canvas canvas, float offset) {
		float tileWidth = width / 9;
		float tileHeight = height / 9;

		for (int i = 0; i < mSolved.length; i++) {
			for (int j = 0; j < mSolved[0].length; j++) {
				float x = left + tileWidth * i + tileWidth / 2;
				float y = top + tileHeight * j + tileHeight / 2 - offset;

				// if it an original number, then draw with bold font
				if (mUnsolved[i][j] != 0) {
					canvas.drawText(Integer.toString(mSolved[i][j]), x, y,
							mTextBold);
					// if it is part of solution, then draw with regular grey
					// font
				} else {
					canvas.drawText(Integer.toString(mSolved[i][j]), x, y,
							mTextNormal);
				}
			}
		}
	}

	private void drawGreyLines(Canvas canvas) {
		// inner grey lines
		for (int i = (int) left; i < (int) right; i += width / 9) {
			canvas.drawLine(i, top, i, bottom, mGreyLine);
		}
		for (int i = (int) top; i < (int) bottom; i += height / 9) {
			canvas.drawLine(left, i, right, i, mGreyLine);
		}
	}

	private void drawBoldLines(Canvas canvas) {
		// major bold lines
		for (int i = (int) (left + (right - left) / 3); i < right; i += width / 3) {
			canvas.drawLine(i, top, i, bottom, mBoldLine);
		}
		for (int i = (int) (top + (bottom - top) / 3); i < bottom; i += height / 3) {
			canvas.drawLine(left, i, right, i, mBoldLine);
		}
	}

	private void drawBorderLines(Canvas canvas) {
		// border
		canvas.drawLine(left, top, left, bottom, mBorderLine); // left
		canvas.drawLine(left, top, right, top, mBorderLine); // top
		canvas.drawLine(right, top, right, bottom, mBorderLine); // right
		canvas.drawLine(left, bottom, right, bottom, mBorderLine); // bottom
	}

}
