package com.example.sudokusolver.util;

import java.util.ArrayList;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.Log;

public class ImgManipUtil {

	public static final int NOTSQUARE = 25;

	public static final String TAG_MAT_DIMENS = "Mat dimensions";
	public static final String TAG_BMP_DIMENS = "Bitmap dimensions";
	public static final String TAG_SUBMAT_DIMENS = "Submat dimensions";
	public static final String TAG_WHITE_POINT = "White point coorinates";
	public static final String TAG_TILE_STATUS = "tile status";
	public static final String TAG_HOUGHLINES = "HoughLines info";
	public static final String TAG_ERROR_FIND_GRID = "findGridArea error";
	public static final String TAG_ERROR_FLOODFILL = "Floodfill setPixel error";

	private ImgManipUtil() {

	}

	/**
	 * converts bitmap to single channel 8 bit Mat
	 * 
	 * @param bmp
	 *            bitmap to convert
	 * @return Mat of same size as bmp; 8 channel single bit
	 */
	public static Mat bitmapToMat(Bitmap bmp) {
		Mat mat = new Mat(bmp.getHeight(), bmp.getWidth(), CvType.CV_8UC1);
		Utils.bitmapToMat(bmp, mat);

		String matInfo = String.format("cols: %d, rows: %d", mat.cols(),
				mat.rows(), mat.channels());
		Log.d(TAG_MAT_DIMENS, matInfo);

		return mat;
	}

	/**
	 * converts mat to RGB bitmap
	 * 
	 * @param mat
	 *            mat to convert
	 * @return bitmap of same size as mat, in ARGB8888 format
	 */
	public static Bitmap matToBitmap(Mat mat) {
		Bitmap bmp = Bitmap.createBitmap(mat.cols(), mat.rows(),
				Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(mat, bmp);

		String bmpInfo = String.format("width: %d, height %d", bmp.getWidth(),
				bmp.getHeight());
		Log.d(TAG_BMP_DIMENS, bmpInfo);

		return bmp;
	}

	public static Mat cropSubMat(Rect r, Mat mat, int CONST_CROP) {
		if (r.left - CONST_CROP >= 0) {
			r.left -= CONST_CROP;
		} else {
			r.left = 0;
		}

		if (r.top - CONST_CROP >= 0) {
			r.top -= CONST_CROP;
		} else {
			r.top = 0;
		}

		if (r.right + CONST_CROP < mat.cols()) {
			r.right += CONST_CROP;
		} else {
			r.right = mat.cols() - 1;
		}

		if (r.bottom + CONST_CROP < mat.rows()) {
			r.bottom += CONST_CROP;
		} else {
			r.bottom = mat.rows() - 1;
		}

		return mat.submat(r.top, r.bottom, r.left, r.right);
	}

	public static void adaptiveThreshold(Mat mat) {
		Imgproc.adaptiveThreshold(mat, mat, 255,
				Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV,
				11, 2);

	}

	public static void binaryThreshold(Mat mat) {
		Imgproc.threshold(mat, mat, 128, 255, Imgproc.THRESH_BINARY);

	}

	/**
	 * performs openCV erosion to source mat
	 * 
	 * @param mat
	 *            source on which to perform erosion
	 * @param factor
	 *            kernel size
	 **/
	public static void erodeMat(Mat mat, int factor) {
		// Mat manip = bitmapToMat(fixedBmp);
		Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS,
				new Size(factor, factor));
		Imgproc.erode(mat, mat, kernel);
		// fixedBmp = matToBitmap(manip);
	}

	public static void openMat(Mat mat, int factor) {
		Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS,
				new Size(factor, factor));
		Imgproc.morphologyEx(mat, mat, Imgproc.MORPH_OPEN, kernel);
	}

	/**
	 * performs openCV dilation to source mat
	 * 
	 * @param mat
	 *            source on which to perform dilation
	 * @param factor
	 *            kernel size
	 */
	public static void dilateMat(Mat mat, int factor) {
		Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS,
				new Size(factor, factor));
		Imgproc.dilate(mat, mat, kernel);
	}

	/**
	 * performs openCV close operation to source mat
	 * 
	 * @param mat
	 *            source on which to perform operation
	 * @param factor
	 *            kernel size
	 */
	public static void closeMat(Mat mat, int factor) {
		Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS,
				new Size(factor, factor));
		Imgproc.morphologyEx(mat, mat, Imgproc.MORPH_CLOSE, kernel);
	}

	/**
	 * determines if bitmap contains a number
	 * 
	 * @param bmp
	 *            source bitmap
	 * @param ratio
	 *            percentage of the bitmap that must be white
	 * @return true if empty, false otherwise
	 */
	public static boolean findEmptyTile(Bitmap bmp, float ratio) {
		int area = bmp.getWidth() * bmp.getHeight();
		int totalWhite = 0;
		for (int i = 0; i < bmp.getWidth(); i++) {
			for (int j = 0; j < bmp.getHeight(); j++) {
				if (bmp.getPixel(i, j) == Color.WHITE) {
					totalWhite++;
				}
			}
		}
		if (totalWhite > ratio * area) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * returns undistorted version of Mat using transformation from OpenCV
	 * library
	 * 
	 * @param upLeft
	 *            top left corner coordinates
	 * @param upRight
	 *            top right corner coordinates
	 * @param downLeft
	 *            bottom left corner coordinates
	 * @param downRight
	 *            bottom right corner coordinates
	 * @param source
	 *            source Mat
	 * @return
	 */
	public static Mat fixPerspective(Point upLeft, Point upRight,
			Point downLeft, Point downRight, Mat source) {
		List<Point> src = new ArrayList<Point>();
		List<Point> dest = new ArrayList<Point>();
		Mat result = new Mat(source.size(), source.type());

		// add the four corners to List
		src.add(upLeft);
		src.add(upRight);
		src.add(downLeft);
		src.add(downRight);

		Point topLeft = new Point(0, 0);
		Point topRight = new Point(source.cols(), 0);
		Point bottomLeft = new Point(0, source.rows());
		Point bottomRight = new Point(source.cols(), source.rows());

		// add destination corners to List (adjusted for rotation)
		dest.add(topRight);
		dest.add(bottomRight);
		dest.add(topLeft);
		dest.add(bottomLeft);

		// convert List to Mat
		Mat srcM = Converters.vector_Point2f_to_Mat(src);
		Mat destM = Converters.vector_Point2f_to_Mat(dest);

		// apply perspective transform using 3x3 matrix
		Mat perspectiveTrans = new Mat(3, 3, CvType.CV_32FC1);
		perspectiveTrans = Imgproc.getPerspectiveTransform(srcM, destM);
		Imgproc.warpPerspective(source, result, perspectiveTrans, result.size());

		return result;
	}

	/**
	 * returns point of intersection between two lines
	 * 
	 * @param l1
	 *            array containing x1, y1, x2, y2
	 * @param l2
	 *            array containing x1, y1, x2, y2
	 * @return Point of intersection between two lines
	 */
	public static Point findCorner(double[] l1, double[] l2) {
		double x1 = l1[0];
		double y1 = l1[1];
		double x2 = l1[2];
		double y2 = l1[3];
		double x3 = l2[0];
		double y3 = l2[1];
		double x4 = l2[2];
		double y4 = l2[3];

		double d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
		double x = ((x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2)
				* (x3 * y4 - y3 * x4))
				/ d;
		double y = ((x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2)
				* (x3 * y4 - y3 * x4))
				/ d;

		Point p = new Point(x, y);
		return p;
	}

	/**
	 * trims the bitmap to contain only the sudoku grid
	 * 
	 * @param bmp
	 *            source bitmap image
	 * @return int array containing bounds-- [0]=left, [1]=right, [2]=top,
	 *         [3]=bot
	 */
	public static int[] findGridBounds(Mat mat) {
		int[] bounds = new int[4];
		// find the four general edges of the sudoku grid; 5 pixel buffer region
		// in case any part of the grid gets cut off
		// Bitmap bmp = matToBitmap(mat);
		int left = findBorders(1, mat) - 5;
		int right = findBorders(2, mat) + 5;
		int top = findBorders(3, mat) - 5;
		int bot = findBorders(4, mat) + 5;

		bounds[0] = left;
		bounds[1] = right;
		bounds[2] = top;
		bounds[3] = bot;

		return bounds;
		/*
		 * //if sides differ by more than threshold amount of pixels, then
		 * //throw error since area is not square if(Math.abs(right - left -
		 * (bot - top)) > THRESHOLD){ Log.d(TAG_ERROR_FIND_GRID, "not square");
		 * }
		 * 
		 * //Bitmap subBmp = Bitmap.createBitmap(bmp, left, top, right-left,
		 * bot-top); Mat subMat = mat.submat(top, bot, left, right); String
		 * subMatInfo = String.format("left: %d, right: %d, top: %d, bot: %d",
		 * left, right, top, bot); Log.d(TAG_SUBMAT_DIMENS, subMatInfo);
		 * 
		 * return subMat; //return subBmp; *
		 */
	}

	/**
	 * find the borders of the sudoku grid; the check for white line begins 1/3
	 * away from the centre of the image
	 * 
	 * @param side
	 *            1=left, 2=right, 3=top, 4=bottom
	 * @param bmp
	 *            source bitmap
	 * @return the x or y coordinate of the border
	 */
	private static int findBorders(int side, Mat mat) {
		switch (side) {
		// left
		case 1:
			for (int i = mat.cols() / 3; i > 0; i--) {
				if (isBorderHeight(i, mat))
					return i;
			}
			break;
		// right
		case 2:
			for (int i = 2 * mat.cols() / 3; i < mat.cols(); i++) {
				if (isBorderHeight(i, mat))
					return i;
			}
			break;
		// top
		case 3:
			for (int i = mat.rows() / 3; i > 0; i--) {
				if (isBorderWidth(i, mat))
					return i;
			}
			break;
		// bottom
		case 4:
			for (int i = 2 * mat.rows() / 3; i < mat.rows(); i++) {
				if (isBorderWidth(i, mat))
					return i;
			}
			break;
		}

		// returns negative border if not found
		Log.d(TAG_ERROR_FIND_GRID, "boundary not found: side " + side);
		return -6;
	}

	/**
	 * checks if horizontal line(width) is outside the sudoku grid
	 * 
	 * @param height
	 *            y coordinate
	 * @param bmp
	 *            source bitmap
	 * @return true if line is outside of sudoku grid, false otherwise
	 */
	private static boolean isBorderWidth(int height, Mat mat) {
		for (int i = 2 * mat.cols() / 5; i < 3 * mat.cols() / 5; i++) {
			// if pixel is black
			if ((int) mat.get(height, i)[0] == 255) {
				return false;
			}
		}
		return true;
	}

	/**
	 * checks if vertical line(height) is outside the sudoku grid
	 * 
	 * @param width
	 *            x coordinate
	 * @param bmp
	 *            bitmap containing image
	 * @return true if line is outside of sudoku grid, false otherwise
	 */
	private static boolean isBorderHeight(int width, Mat mat) {
		for (int i = 2 * mat.rows() / 5; i < 3 * mat.rows() / 5; i++) {
			// if pixel is black
			if ((int) mat.get(i, width)[0] == 255) {
				return false;
			}
		}
		return true;
	}

	/**
	 * debugging method that draws white line to mat
	 * 
	 * @param line
	 *            contains x1, y1, x2, y2
	 * @param m
	 *            source mat
	 */
	public static void drawLine(double[] line, Mat m) {
		double x1 = line[0];
		double y1 = line[1];
		double x2 = line[2];
		double y2 = line[3];

		Point start = new Point(x1, y1);
		Point end = new Point(x2, y2);
		Log.d("boundaries", x1 + "," + y1 + " " + x2 + "," + y2);
		Core.line(m, start, end, new Scalar(255, 255, 255), 3);
	}

	public static boolean notSquare(int[] bounds) {
		int left = bounds[0];
		int right = bounds[1];
		int top = bounds[2];
		int bot = bounds[3];

		if (Math.abs(right - left - (bot - top)) > NOTSQUARE) {
			Log.d(TAG_ERROR_FIND_GRID, "not square");
			return true;
		}
		return false;
	}
}
