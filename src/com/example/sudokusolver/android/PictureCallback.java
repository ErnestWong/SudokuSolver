package com.example.sudokusolver.android;

import org.opencv.core.Mat;

import com.example.sudokusolver.imageproc.ImgManipulation;
import com.example.sudokusolver.util.SudokuSolver;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Color;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;

/**
 * Implements callback method that handles image capture
 * 
 * @author E Wong
 * 
 */
public class PictureCallback implements Camera.PictureCallback {

	private RectangleView mRectView;
	private SurfaceHolderCallback mShCB;
	private Context mContext;

	public PictureCallback(RectangleView rectView, SurfaceHolderCallback shCB,
			Context c) {
		mRectView = rectView;
		mShCB = shCB;
		mContext = c;
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		Log.d("Taken Picture", "pic");
		mShCB.stopPreview();
		if (data != null) {
			try {
				Bitmap fullbmp = decodeByteAndScale(data, 5);

				ImgManipulation imgManip = new ImgManipulation(mContext,
						fullbmp);
				int[][] unsolved = imgManip.getSudokuGridNums();

				if (unsolved == null || imgManip.getError()) {
					mRectView.setPaintColor(Color.RED);
					Log.d("getSudokuGridNums Error", "returned null");
				} else {
					int[][] solved = SudokuSolver.solveSudoku(unsolved);
					mRectView.setPaintColor(Color.GREEN);
					startIntent(unsolved, solved);
				}

			} catch (Exception e) {
				mRectView.setPaintColor(Color.RED);

				Log.d("Error", e + "");
			}
			mShCB.startPreview();
		}

	}

	private Bitmap decodeByteAndScale(byte[] data, int scaleBy) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		opts.inSampleSize = scaleBy;
		opts.inJustDecodeBounds = false;
		Bitmap fullbmp = BitmapFactory.decodeByteArray(data, 0, data.length,
				opts);
		return fullbmp;
	}

	private Rect findROI(Bitmap fullbmp) {
		int top = (int) (mRectView.getTopRatio() * fullbmp.getWidth());
		int bot = (int) (mRectView.getBottomRatio() * fullbmp.getWidth());
		int left = (int) (mRectView.getLeftRatio() * fullbmp.getHeight());
		int right = (int) (mRectView.getRightRatio() * fullbmp.getHeight());

		Rect r = new Rect(top, left, bot, right);
		return r;
	}

	private void startIntent(int[][] unsolved, int[][] solved) {
		Bundle bundle = new Bundle();
		bundle.putIntArray("unsolved", toArray(unsolved));
		bundle.putIntArray("solved", toArray(solved));

		Intent intent = new Intent(mContext, SudokuGridActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtras(bundle);
		mContext.startActivity(intent);
	}

	private int[] toArray(int[][] input) {
		int[] output = new int[input[0].length * input.length];
		int index = 0;
		for (int y = 0; y < input.length; y++) {
			for (int x = 0; x < input[0].length; x++) {
				output[index] = input[x][y];
				index++;
			}
		}
		return output;
	}

}
