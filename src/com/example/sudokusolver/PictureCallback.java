package com.example.sudokusolver;

import org.opencv.core.Mat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Color;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.Log;

public class PictureCallback implements Camera.PictureCallback {

	private RectangleView mRectView;
	private SurfaceHolderCallback mShCB;
	private Context mContext;
	
	public PictureCallback(RectangleView rectView, SurfaceHolderCallback shCB, Context c){
		mRectView = rectView;
		mShCB = shCB;
		mContext = c;
	}
	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		Log.d("Taken Picture", "pic");
		mShCB.stopPreview();
		if (data != null) {
			try{	
				
				Bitmap fullbmp = decodeByteAndScale(data, 5);
				//BitmapRegionDecoder regionDecoder = BitmapRegionDecoder.newInstance(data, 0, data.length, true);
				//Rect r = findROI(fullbmp);
 				//Bitmap bmp = regionDecoder.decodeRegion(r, null);
 				
				ImgManipulation imgManip = new ImgManipulation(mContext, fullbmp);
				imgManip.doStoreBitmap();
				mRectView.setPaintColor(Color.GREEN);
				mShCB.startPreview();
				//Log.d("fullfmp dimens", fullbmp.getWidth() + "," + fullbmp.getHeight());
				//Log.d("rect dimens", r.top + "," + r.bottom + "," + r.left + "," + r.right);
				//Log.d("bmp dimens", bmp.getWidth() + "," + bmp.getHeight());
			}
			catch(Exception e){
				mRectView.setPaintColor(Color.RED);
				mShCB.startPreview();
				Log.d("Error", e + "");
			}
		}
	
	}
	
	private Bitmap decodeByteAndScale(byte[] data, int scaleBy){
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		opts.inSampleSize = scaleBy;
		opts.inJustDecodeBounds = false;
		Bitmap fullbmp = BitmapFactory.decodeByteArray(data, 0, data.length, opts);
		return fullbmp;
	}
	
	private Rect findROI(Bitmap fullbmp){
		int top = (int) (mRectView.getTopRatio() * fullbmp.getWidth());
		int bot = (int) (mRectView.getBottomRatio() * fullbmp.getWidth());
		int left = (int) (mRectView.getLeftRatio() * fullbmp.getHeight());
		int right = (int) (mRectView.getRightRatio() * fullbmp.getHeight());
		
		Rect r = new Rect(top, left, bot, right);
		return r;
	}

}
