package com.example.sudokusolver.imageproc;

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
	public final static Scalar WHITE = new Scalar(255);
	public final static Scalar BLACK = new Scalar(0);

	public BlobExtract() {

	}

	/**
	 * extracts digits from Mat image for OCR
	 * 
	 * @param cleanMat
	 *            unprocessed binary Mat image; used to extract numbers for OCR
	 * @param processedMat
	 *            processed binary Mat image; used to identify bounding
	 *            rectangles
	 * @return List of Mat containing clean images of individual numbers
	 **/
	public Queue<Mat> findCleanNumbers(Mat cleanMat, List<Rect> bounds) {
		Queue<Mat> numberMats = new LinkedList<Mat>();

		bounds = sortRects(bounds);
		for (Rect rect : bounds) {
			rect = resizeRect(rect, cleanMat, 2);
			Mat numMat = cleanMat.submat(rect);
			removeNoise(numMat);
			numMat = resizeMat(numMat, 1);
			numberMats.add(numMat);
		}
		return numberMats;
	}

	/**
	 * finds the approximate regions of the numbers in the source image
	 * 
	 * @param m
	 *            processed mat ready for finding bounds
	 * @return List of Rects that bounds each number in image
	 */
	public List<Rect> getBoundingRects(Mat m) {
		int tileHeight = m.rows() / 9;
		int tileWidth = m.cols() / 9;
		GAP = tileHeight / 2;

		List<Rect> boundRects = new ArrayList<Rect>();

		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(m, contours, new Mat(), Imgproc.RETR_LIST,
				Imgproc.CHAIN_APPROX_SIMPLE);
		for (MatOfPoint mPoint : contours) {
			Rect rect = Imgproc.boundingRect(mPoint);
			if (isNumber(rect.width, rect.height, tileWidth, tileHeight)) {
				boundRects.add(rect);
			}
		}
		// Mat image = drawRectsToMat(m, boundRects);
		// FileSaver.storeImage(ImgManipUtil.matToBitmap(image),
		// "testBlobextract");
		Log.d("rect count", boundRects.size() + "");
		return boundRects;
	}

	/**
	 * draws the list of filled rects to mat
	 * 
	 * @param src
	 *            Mat source image (just needed for sizing)
	 * @param rects
	 *            List of Rects to draw
	 * @return result Mat with filled rectangles drawn on it
	 **/
	public Mat drawRectsToMat(Mat src, List<Rect> rects) {
		Mat result = new Mat(src.size(), src.type());
		for (Rect rect : rects) {
			Core.rectangle(result, new Point(rect.x, rect.y), new Point(rect.x
					+ rect.width, rect.y + rect.height), WHITE, Core.FILLED);
		}
		return result;
	}

	/**
	 * removes small noise from the subMat, keeping only the number
	 * 
	 * @param submat
	 *            source subMat containing number image
	 * @return Mat without noise
	 **/
	private void removeNoise(Mat submat) {
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat tmp = new Mat(submat.size(), submat.type());
		submat.copyTo(tmp);

		Imgproc.findContours(tmp, contours, new Mat(), Imgproc.RETR_LIST,
				Imgproc.CHAIN_APPROX_SIMPLE);
		for (int i = 0; i < contours.size(); i++) {
			Rect r = Imgproc.boundingRect(contours.get(i));
			if (isNoise(r.width, r.height, submat.cols(), submat.rows())) {
				Imgproc.drawContours(submat, contours, i, BLACK, Core.FILLED);
			}
		}
		// FileSaver.storeImage(ImgManipUtil.matToBitmap(result), "d");
		// return result;
	}

	/**
	 * checks whether blob in Mat image is a number
	 * 
	 * @param width
	 *            width of number
	 * @param height
	 *            height of number
	 * @param tileWidth
	 *            width of 1/9 of the mat (one cell approx)
	 * @param tileHeight
	 *            height of 1/9 of mat (one cell approx)
	 * @return true if blob is a number, false otherwise
	 **/
	private boolean isNumber(int width, int height, int tileWidth,
			int tileHeight) {
		if (width > 7 * tileWidth / 9 || height > 7 * tileHeight / 9) {
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

	/**
	 * checks whether blob in subMat is noise (if small enough)
	 * 
	 * @param width
	 *            width of number
	 * @param height
	 *            height of number
	 * @param tileWidth
	 *            width of the subMat
	 * @param tileHeight
	 *            height of subMat
	 * @return true if blob is a number, false otherwise
	 **/
	private boolean isNoise(int width, int height, int tileWidth, int tileHeight) {
		if (width < tileWidth / 10 || height < tileHeight / 10) {
			return true;
		}
		return false;
	}

	/**
	 * extends and returns Rect by a given constant; checks bounds of Mat first
	 * 
	 * @param r
	 *            source Rect
	 * @param mat
	 *            Mat to which Rect is bound
	 * @param CONST_CROP
	 *            number of pixels Rect is being extended by
	 * @return modified Rect
	 **/
	private Rect resizeRect(Rect r, Mat mat, int CONST_CROP) {
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

		return new Rect(left, top, right - left, bot - top);
	}

	private Mat resizeMat(Mat m, int resizeBy) {
		if (resizeBy >= m.rows() || resizeBy >= m.cols()) {
			return m;
		}

		int x = resizeBy;
		int y = resizeBy;
		int width = m.cols() - resizeBy;
		int height = m.rows() - resizeBy;

		Rect r = new Rect(x, y, width, height);
		return m.submat(r);
	}

	/**
	 * uses selection sort on input rects and outputs sorted queue
	 * 
	 * @param rects
	 *            -- input list of Rect
	 * @return sorted List
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
	 * 
	 * @param original
	 *            -- input list
	 * @return clone of list
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
	 *            Rect 1
	 * @param r2
	 *            Rect 2
	 * @return true if r1 > r2, false if r1 < r2
	 */
	private boolean compareRect(Rect r1, Rect r2) {
		double c1X = r1.x + (r1.width / 2); // r1 centreX
		double c1Y = r1.y + (r1.height / 2); // r1 centreY
		double c2X = r2.x + (r2.width / 2); // r2 centreX
		double c2Y = r2.y + (r2.height / 2); // r2 centreY

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
}
