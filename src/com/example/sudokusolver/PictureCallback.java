package com.example.sudokusolver;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.util.Log;

public class PictureCallback implements Camera.PictureCallback {

	private RectangleView rectView;
	private SurfaceHolderCallback shCB;
	
	public PictureCallback(RectangleView rectView, SurfaceHolderCallback shCB){
		this.rectView = rectView;
		this.shCB = shCB;
	}
	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		Log.d("Taken Picture", "pic");
		shCB.stopPreview();
		Mat mat;
		Bitmap bmp;
		if (data != null) {
		try{	
			rectView.setPaintColor(Color.GREEN);
			bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
			mat = new Mat(bmp.getHeight(), bmp.getWidth(), CvType.CV_8UC1);
			Utils.bitmapToMat(bmp, mat);
            //Imgproc.adaptiveThreshold(mat, mat, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);
            
		}
		catch(Exception e){
			rectView.setPaintColor(Color.RED);
			shCB.startPreview();
		}
	}
	
	}

}
