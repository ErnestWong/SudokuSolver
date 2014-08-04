package com.example.sudokusolver;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

public class SudokuView extends View {
	public static final int BOLD_WIDTH = 5;
	public static final int BORDER_WIDTH = 2;
	private Paint mBorderLine;
	private Paint mBoldLine;
	private Paint mGreyLine;
	private float top, bottom, left, right;
	private float rectDimens;
	private float width, height;

	public SudokuView(Context context) {
		super(context);
		mBorderLine = new Paint();
		mBorderLine.setColor(getResources().getColor(R.color.black));
		mBorderLine.setStrokeWidth(BORDER_WIDTH);

		mBoldLine = new Paint();
		mBoldLine.setColor(getResources().getColor(R.color.black));
		mBoldLine.setStrokeWidth(BOLD_WIDTH);

		mGreyLine = new Paint();
		mGreyLine.setColor(getResources().getColor(R.color.grey));

	}

	@Override
	protected void onDraw(Canvas canvas) {

		rectDimens = (float) (this.getWidth() * 0.9);
		top = this.getHeight() / 2 - rectDimens / 2;
		bottom = top + rectDimens;
		left = (float) (this.getWidth() * 0.05);
		right = left + rectDimens;

		width = right - left;
		height = bottom - top;

		drawGreyLines(canvas);
		drawBoldLines(canvas);
		drawBorderLines(canvas);
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
