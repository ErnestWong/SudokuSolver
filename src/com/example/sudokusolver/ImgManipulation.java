package com.example.sudokusolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Environment;
import android.util.Log;

public class ImgManipulation {

	private Context mContext;
	private Bitmap mBitmap;
	public final float CONST_RATIO = (float) 0.1;
	public ImgManipulation(Context context, Bitmap bitmap) {
		mContext = context;
		mBitmap = bitmap;
	}

	public Mat bitmapToMat(Bitmap bmp) {
		Mat mat = new Mat(bmp.getHeight(), bmp.getWidth(), CvType.CV_8UC1);
		Utils.bitmapToMat(bmp, mat);
		Log.d("Mat info", mat.cols() + "," + mat.rows() + ": "
				+ mat.size().height + " " + mat.size().width);
		return mat;
	}

	public Bitmap matToBitmap(Mat mat) {
		Bitmap bmp = Bitmap.createBitmap(mat.cols(), mat.rows(),
				Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(mat, bmp);
		return bmp;
	}

	public void doStoreBitmap(Bitmap src) {
		Mat m = bitmapToMat(src);
		// Imgproc.cvtColor(m, m, Imgproc.COLOR_BGR2GRAY);
		// Imgproc.adaptiveThreshold(m, m, 255,
		// Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);
		//Mat lines = new Mat();
		Imgproc.Canny(m, m, 50, 200);
		
		
		Bitmap bmp = findSubMat(matToBitmap(m));
		Mat m2 = bitmapToMat(bmp);
		//Imgproc.Canny(m2, m2, 50, 200);
		Imgproc.cvtColor(m2, m2, Imgproc.COLOR_RGB2GRAY);
		Log.d("checkpoint", "afterConvertMat");
		Mat lines = new Mat();
		
		List<double[]> horizontalLines = new ArrayList<double[]>();
		List<double[]> verticalLines = new ArrayList<double[]>();
		
		Imgproc.HoughLinesP(m2, lines, 1, Math.PI/180, 150);
		for(int i = 0; i < lines.cols(); i++){
			double[] line = lines.get(0, i);
			double x1 = line[0];
			double y1 = line[1];
			double x2 = line[2];
			double y2 = line[3];
			if(Math.abs(y2 - y1) < 10){
				horizontalLines.add(line);
			}
			else if(Math.abs(x2 - x1) < 10){
				verticalLines.add(line);
			}
			//Point start = new Point(x1, y1);
			//Point end = new Point(x2, y2);
			Log.d("line points", x1 + "," + y1 + " " + x2 + "," + y2);
			//Core.line(m2, start, end, new Scalar(255, 255, 255), 3);
		}
		Log.d("line stats", "horizontal: " + horizontalLines.size() + ", vertical: " + verticalLines.size() + ", total: " + lines.cols());
		
		
		
		//lines for four boundaries of sudoku grid
		double[] topLine = horizontalLines.get(0);
		double[] bottomLine = horizontalLines.get(0);
		double[] leftLine = verticalLines.get(0);
		double[] rightLine = verticalLines.get(0);
		
		double xMin = 1000;
		double xMax = 0;
		double yMin = 1000;
		double yMax = 0;
		
		for(int i = 0; i < horizontalLines.size(); i++){
			if(horizontalLines.get(i)[1] < yMin || horizontalLines.get(i)[3] < yMin){
				topLine = horizontalLines.get(i);
				yMin = horizontalLines.get(i)[1];
			}
			else if(horizontalLines.get(i)[1] > yMax || horizontalLines.get(i)[3] > yMax){
				bottomLine = horizontalLines.get(i);
				yMax = horizontalLines.get(i)[1];
			}
		}
		
		for(int i = 0; i < verticalLines.size(); i++){
			if(verticalLines.get(i)[0] < xMin || verticalLines.get(i)[2] < xMin){
				leftLine = verticalLines.get(i);
				xMin = verticalLines.get(i)[0];
			}
			else if(verticalLines.get(i)[0] > xMax || verticalLines.get(i)[2] > xMax){
				rightLine = verticalLines.get(i);
				xMax = verticalLines.get(i)[0];
			}
		}
		
		drawLine(topLine, m2);
		drawLine(bottomLine, m2);
		drawLine(leftLine, m2);
		drawLine(rightLine, m2);
		
		Log.d("houglines", lines.cols() + "");
		Log.d("bmp dimens", "h: " + bmp.getHeight() + ", w: " + bmp.getWidth());
		
		storeImage(matToBitmap(m2));
		//Log.d("number of Lines", lines.cols() + " ");
		//storeImage(matToBitmap(m));
		//storeImage(matToBitmap(findSubMat(m)));
	}

	private void drawLine(double[] line, Mat m){
		double x1 = line[0];
		double y1 = line[1];
		double x2 = line[2];
		double y2 = line[3];
		
		Point start = new Point(x1, y1);
		Point end = new Point(x2, y2);
		Log.d("boundaries", x1 + "," + y1 + " " + x2 + "," + y2);
		Core.line(m, start, end, new Scalar(255, 255, 255), 3);
	}
	private Bitmap findSubMat(Bitmap bmp){
		int left = findBorders(1, bmp) - 5;
		int right = findBorders(2, bmp) + 5;
		int top = findBorders(3, bmp) - 5;
		int bot = findBorders(4, bmp) + 5;
		
		if(Math.abs(right - left + (bot - top)) > 80){
			Log.d("submat error", "not square");
		}
		
		Log.d("submat", left + "," + right + "," + top + "," + bot);
		Bitmap subBmp = Bitmap.createBitmap(bmp, left, top, right-left, bot-top);
		return subBmp;
	}
	
	private boolean isBorderWidth(int height, Bitmap bmp) {
		int streak = 0;
		for (int i = 2*bmp.getWidth()/5; i < 3*bmp.getWidth()/5; i++) {
			// if pixel is black
			//if (bmp.getPixel(i, height) == Color.BLACK) {
			//	streak++;
			//} else {
			//	streak = 0;
			//}
			if(bmp.getPixel(i, height) == Color.WHITE){
				return false;
			}
		}

		//if (streak > bmp.getWidth() * CONST_RATIO)
		//	return true;
		//else
		//	return false;
		return true;
	}

	private boolean isBorderHeight(int width, Bitmap bmp) {
		int streak = 0;
		for (int i = 2*bmp.getHeight()/5; i < 3*bmp.getHeight()/5; i++) {
			// if pixel is not black
			//if (bmp.getPixel(width, i) == Color.BLACK) {
			//	streak++;
			//} else {
			//	streak = 0;
			//}
			if(bmp.getPixel(width, i) == Color.WHITE) return false;
		}

		//if (streak > bmp.getWidth() * CONST_RATIO)
		//	return true;
		//else
		//	return false;
		return true;
	}

	private int findBorders(int side, Bitmap bmp) {
		switch (side) {
		// left
		case 1:
			for(int i = bmp.getWidth()/3; i > 0; i--){
				if(isBorderHeight(i, bmp)) return i;
			}
			break;
		// right
		case 2:
			for(int i = 2*bmp.getWidth()/3; i < bmp.getWidth(); i++){
				if(isBorderHeight(i, bmp)) return i;
			}
			break;
		// top
		case 3:
			for(int i = bmp.getHeight()/3; i > 0; i--){
				if(isBorderWidth(i,bmp)) return i;
			}
			break;
		// bottom
		case 4:
			for(int i = 2*bmp.getHeight()/3; i < bmp.getHeight(); i++){
				if(isBorderWidth(i,bmp)) return i;
			}
			break;
		}

		return -1;
	}

	private void storeImage(Bitmap image) {
		if (image == null)
			Log.d("null bitmap", "storeImage");
		File pictureFile = getOutputMediaFile();
		Log.d("filename", pictureFile + "");
		if (pictureFile == null) {
			Log.d(TAG, "Error creating media file, check storage permissions: ");// e.getMessage());
			return;
		}
		try {
			FileOutputStream fos = new FileOutputStream(pictureFile);
			image.compress(Bitmap.CompressFormat.PNG, 90, fos);
			fos.close();
			Log.d("FILe", "Success");
		} catch (FileNotFoundException e) {
			Log.d(TAG, "File not found: " + e.getMessage());
		} catch (IOException e) {
			Log.d(TAG, "Error accessing file: " + e.getMessage());
		}
	}

	String TAG = "file save";

	/** Create a File for saving an image or video */
	private File getOutputMediaFile() {
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.
		File mediaStorageDir = new File(
				Environment.getExternalStorageDirectory() + "/Android/data/"
						+ mContext.getPackageName() + "/Files");

		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				return null;
			}
		}
		// Create a media file name
		String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm")
				.format(new Date());
		File mediaFile;
		String mImageName = "MI_" + timeStamp + ".jpg";
		mediaFile = new File(mediaStorageDir.getPath() + File.separator
				+ mImageName);
		return mediaFile;
	}

}
