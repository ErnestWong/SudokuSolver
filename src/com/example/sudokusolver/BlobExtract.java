package com.example.sudokusolver;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.R;
import android.util.Log;

import com.example.sudokusolver.util.FileSaver;
import com.example.sudokusolver.util.ImgManipUtil;

public class BlobExtract {
	
	private static int GAP;
	public BlobExtract(){
		
	}
	
	/**
	 * 
	 * @param m processed mat ready for finding bounds
	 * @return
	 */
	public List<Rect> getBoundingRects(Mat m){
		int tileHeight = m.rows()/9;
		int tileWidth = m.cols()/9;
		GAP = tileHeight/2;
		
		Mat image = new Mat(m.size(), m.type());
		List<Rect> boundRects = new ArrayList<Rect>();
		
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(m, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		for(MatOfPoint mPoint : contours){
			Rect rect = Imgproc.boundingRect(mPoint);
			if(isNumber(rect.width, rect.height, tileWidth, tileHeight)){
				boundRects.add(rect);
				//drawcontours negative thickness
				Imgproc.drawContours(image, mPoint, contourIdx, color);
				Core.rectangle(image, new Point(rect.x,rect.y), new Point(rect.x+rect.width,rect.y+rect.height),new Scalar(255,255,255));
			}
		}
		FileSaver.storeImage(ImgManipUtil.matToBitmap(image), "testBlobextract");
		Log.d("rect count", boundRects.size() + "");
		return boundRects;
	}
	
	public void rectToCleanMat(Mat cleanMat, Mat dirtyMat){
		List<Rect> bounds = getBoundingRects(dirtyMat);
		bounds = sortRects(bounds);
		int count = 0;
		for(Rect bound : bounds){
			bound = extendRect(bound, cleanMat, 5);
			Mat numMat = cleanMat.submat(bound);
			removeNoise(numMat);
			FileSaver.storeImage(ImgManipUtil.matToBitmap(numMat), count+"");
			count++;
		}
	}
	
	private void removeNoise(Mat submat){
		
	}
	
	/**
	 * uses selection sort on input rects and outputs sorted queue
	 * 
	 * @param rects-- input list of Rect
	 * @return
	 */
	private List<Rect> sortRects(List<Rect> rects) {
		List<Rect> tmp = cloneRect(rects);
		List<Rect> sorted = new LinkedList<Rect>();

		while (!tmp.isEmpty()) {
			Rect min = tmp.get(0);
			for (int i = 0; i < tmp.size(); i++) {
				if (compareRect(min, tmp.get(i))) {
					min = tmp.get(i);
				}
			}
			tmp.remove(min);
			sorted.add(min);
		}
		return sorted;
	}

	/**
	 * returns deep copy clone of list
	 * @param original-- input list
	 * @return
	 */
	private List<Rect> cloneRect(List<Rect> original) {
		List<Rect> tmpRect = new ArrayList<Rect>(original.size());
		for (int i = 0; i < original.size(); i++) {
			tmpRect.add(original.get(i));
		}
		return tmpRect;
	}

	/**
	 * helper method for sortRects-- sorts top left to bottom right with a
	 * buffer region to distinguish separate rows
	 * 
	 * @param r1
	 * @param r2
	 * @return true if r1 > r2, false if r1 < r2
	 */
	private boolean compareRect(Rect r1, Rect r2) {
		double c1X = r1.x + (r1.width/2);    //r1 centreX 
		double c1Y = r1.y + (r1.height/2);   //r1 centreY
		double c2X = r2.x + (r2.width/2);    //r2 centreX
		double c2Y = r2.y + (r2.height/2);   //r2 centreY
		
		// check to see if r1 and r2 are in different rows(y value)
		if (c1Y > c2Y + GAP) {
			return true;
		} else if (c2Y > c1Y + GAP) {
			return false;
		}
		// check column (x value) if same row
		else {
			if (c1X > c2X) {
				return true;
			} else {
				return false;
			}
		}

	}
	
	
	private boolean isNumber(int width, int height, int tileWidth, int tileHeight){
		if (width > 7*tileWidth/9 || height > 7*tileHeight/9) {
			return false;
		}

		// check this because a number rect should be narrow
		if (width > height) {
			return false;
		}

		// arbitrary parameters to check if rect is too small to be number
		if (height < tileHeight / 3 || width < tileWidth / 6) {
			return false;
		}
		
		return true;
	}
	
	private boolean isSubNumber(int width, int height, int tileWidth, int tileHeight){
		if (width > 8*tileWidth/9 || height > 8*tileHeight/9) {
			return false;
		}

		// check this because a number rect should be narrow
		if (width > height) {
			return false;
		}

		// arbitrary parameters to check if rect is too small to be number
		if (height < tileHeight / 2 || width < tileWidth / 3) {
			return false;
		}
		
		return true;
	}
	
	private Rect extendRect(Rect r, Mat mat, int CONST_CROP) {
		int left = r.x;
		int right = r.x + r.width;
		int top = r.y;
		int bot = r.y + r.height;
		if (left - CONST_CROP >= 0) {
			left -= CONST_CROP;
		} else {
			left = 0;
		}

		if (top - CONST_CROP >= 0) {
			top -= CONST_CROP;
		} else {
			top = 0;
		}

		if (right + CONST_CROP < mat.cols()) {
			right += CONST_CROP;
		} else {
			right = mat.cols() - 1;
		}

		if (bot + CONST_CROP < mat.rows()) {
			bot += CONST_CROP;
		} else {
			bot = mat.rows() - 1;
		}
		
		return new Rect(left, top, right-left, bot-top);
	}
}
