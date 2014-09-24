package com.example.sudokusolver.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.Log;

public class BlobExtractv2 {

	private Mat mat;
	private Bitmap fixedBmp;
	private int tileWidth;
	private int tileHeight;
	private static final int BUFFER = 0;
	private static int GAP;
	private List<Rect> tileRects = new ArrayList<Rect>();

	public BlobExtractv2(Mat mat) {
		this.mat = mat;
		tileWidth = mat.cols() / 9;
		tileHeight = mat.rows() / 9;
		GAP = tileHeight / 2;
	}

	/**
	 * performs blob extraction and stores found blobs in tileRects Must call
	 * this method before getTileRects()
	 */
	public void blobExtract() {
		int[][] pixelInfo = new int[mat.rows()][mat.cols()];
		for (int row = 0; row < mat.rows(); row++) {
			for (int col = 0; col < mat.cols(); col++) {
				pixelInfo[row][col] = (int) mat.get(row, col)[0];
			}
		}

		Log.d("extract", "extracting");
		int count = 0;
		int numcount = 0;
		for (int y = 1; y < mat.rows() - 1; y++) {
			for (int x = 1; x < mat.cols() - 1; x++) {
				if (pixelInfo[y][x] == 0) {
					continue;
				}

				Rect r = floodfill(new Point(x, y), pixelInfo);
				if (r != null) {
					numcount++;
					tileRects.add(r);
				}

				count++;

			}
		}
		Log.d("number of blobs", count + "," + numcount);
	}

	public Bitmap removeNoise(Mat src) {
		Bitmap result = Bitmap.createBitmap(src.cols(), src.rows(),
				Bitmap.Config.ARGB_8888);
		int[][] pixelInfo = new int[src.rows()][src.cols()];
		for (int row = 0; row < src.rows(); row++) {
			for (int col = 0; col < src.cols(); col++) {
				pixelInfo[row][col] = (int) src.get(row, col)[0];
			}
		}

		for (int y = 1; y < src.rows() - 1; y++) {
			for (int x = 1; x < src.cols() - 1; x++) {
				if (pixelInfo[y][x] == 0) {
					continue;
				}

				Rect r = floodfill(new Point(x, y), pixelInfo, result);

			}
		}
		return result;
	}

	/**
	 * performs floodfill at start point and fills connected whitespace to black
	 * 
	 * @param start
	 * @return
	 */
	private Rect floodfill(Point start, int[][] pixelInfo) {
		if (pixelInfo[(int) start.y][(int) start.x] == 0) {
			return null;
		}

		// keeps track of checked pixels
		boolean[][] checked = new boolean[pixelInfo.length][pixelInfo[0].length];

		List<Point> pixels = new ArrayList<Point>();
		// queue of points to store the pixels; add initial pixel
		Queue<Point> q = new LinkedList<Point>();
		q.add(start);

		// remove pixel and check adjacent pixels until queue is empty
		while (!q.isEmpty()) {
			Point p = q.remove();

			if (!checked[(int) p.y][(int) p.x]) {
				if (pixelInfo[(int) p.y][(int) p.x] == 255
						&& !outOfBounds(p, pixelInfo)) {
					pixels.add(p);

					pixelInfo[(int) p.y][(int) p.x] = 0;

					q.add(new Point(p.x - 1, p.y - 1));
					q.add(new Point(p.x - 1, p.y));
					q.add(new Point(p.x - 1, p.y + 1));
					q.add(new Point(p.x, p.y - 1));
					q.add(new Point(p.x, p.y + 1));
					q.add(new Point(p.x + 1, p.y - 1));
					q.add(new Point(p.x + 1, p.y));
					q.add(new Point(p.x + 1, p.y + 1));
				}
			}
			checked[(int) p.y][(int) p.x] = true;
		}

		Rect r = isNumber(pixels);

		if (r != null) {
			// setNumToBitmap(pixels);
		}
		pixels.clear();
		return r;
	}

	/**
	 * performs floodfill at start point and fills connected whitespace to black
	 * 
	 * @param start
	 * @return
	 */
	private Rect floodfill(Point start, int[][] pixelInfo, Bitmap bmp) {
		if (pixelInfo[(int) start.y][(int) start.x] == 0) {
			return null;
		}

		// keeps track of checked pixels
		boolean[][] checked = new boolean[pixelInfo.length][pixelInfo[0].length];

		List<Point> pixels = new ArrayList<Point>();
		// queue of points to store the pixels; add initial pixel
		Queue<Point> q = new LinkedList<Point>();
		q.add(start);

		// remove pixel and check adjacent pixels until queue is empty
		while (!q.isEmpty()) {
			Point p = q.remove();

			if (!checked[(int) p.y][(int) p.x]) {
				if (pixelInfo[(int) p.y][(int) p.x] == 255
						&& !outOfBounds(p, pixelInfo)) {
					pixels.add(p);

					pixelInfo[(int) p.y][(int) p.x] = 0;

					q.add(new Point(p.x - 1, p.y - 1));
					q.add(new Point(p.x - 1, p.y));
					q.add(new Point(p.x - 1, p.y + 1));
					q.add(new Point(p.x, p.y - 1));
					q.add(new Point(p.x, p.y + 1));
					q.add(new Point(p.x + 1, p.y - 1));
					q.add(new Point(p.x + 1, p.y));
					q.add(new Point(p.x + 1, p.y + 1));
				}
			}
			checked[(int) p.y][(int) p.x] = true;
		}

		Rect r = isNumber(pixels, pixelInfo[0].length, pixelInfo.length);

		if (r != null) {
			setNumToBitmap(pixels, bmp);
		}
		pixels.clear();
		return r;
	}

	private Mat arrayToMat(int[][] pixels) {
		Mat m = new Mat(new Size(pixels[0].length, pixels.length),
				CvType.CV_8UC4);
		for (int i = 0; i < pixels.length; i++) {
			for (int j = 0; j < pixels[i].length; j++) {
				double[] data = new double[4];
				data[0] = pixels[i][j];
				m.put(pixels.length, pixels[0].length, data);
			}
		}
		return m;
	}

	/**
	 * set bitmap pixels of the fixedBmp to white
	 * 
	 * @param pixels
	 */
	private void setNumToBitmap(List<Point> pixels, Bitmap bmp) {
		for (int i = 0; i < pixels.size(); i++) {
			bmp.setPixel((int) pixels.get(i).x, (int) pixels.get(i).y,
					Color.WHITE);
		}
	}

	/**
	 * @return sorted queue of Rect objects Must call blobExtract() first
	 */
	public Queue<Rect> getTileRects() {
		return sortRects(tileRects);
	}

	/**
	 * @return fixed bitmap removing noise and blobs that aren't numbers Must
	 *         call blobExtract() first
	 **/
	public Bitmap getFixedBitmap() {
		return fixedBmp;
	}

	/**
	 * check if pixel is out of bounds from bitmap
	 * 
	 * @param point
	 *            -- target pixel
	 * @return
	 */
	private boolean outOfBounds(Point point, int[][] pixelInfo) {
		if (point.x >= pixelInfo[0].length - 2
				|| point.y >= pixelInfo.length - 2 || point.x <= 0
				|| point.y <= 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * uses selection sort on input rects and outputs sorted queue
	 * 
	 * @param rects
	 *            -- input list of Rect
	 * @return
	 */
	private Queue<Rect> sortRects(List<Rect> rects) {
		List<Rect> tmp = cloneRect(rects);
		Queue<Rect> sorted = new LinkedList<Rect>();

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
		int n1 = r1.centerY();
		int n2 = r2.centerY();
		// check to see if r1 and r2 are in different rows(y value)
		if (n1 > n2 + GAP) {
			return true;
		} else if (n2 > n1 + GAP) {
			return false;
		}
		// check column (x value) if same row
		else {
			if (r1.centerX() > r2.centerX()) {
				return true;
			} else {
				return false;
			}
		}

	}

	/**
	 * checks if Rect encloses a number; uses series of tests to determine
	 * 
	 * @param xCoords
	 *            -- list containing x coordinate pixels in rect
	 * @param yCoords
	 *            -- list containing y coordinate pixels in rect
	 * @return null if not number, non-null otherwise
	 */
	private Rect isNumber(List<Point> pixels) {

		// if pixels are empty return null
		if (pixels.size() == 0) {
			return null;
		}
		// sort list of pixels; first and last of each list will be the bounds
		// of rect
		int xMax = (int) pixels.get(0).x;
		int xMin = (int) pixels.get(0).x;
		int yMax = (int) pixels.get(0).y;
		int yMin = (int) pixels.get(0).y;

		for (int i = 0; i < pixels.size(); i++) {
			if ((int) pixels.get(i).x > xMax) {
				xMax = (int) pixels.get(i).x;
			}
			if ((int) pixels.get(i).x < xMin) {
				xMin = (int) pixels.get(i).x;
			}
			if ((int) pixels.get(i).y > yMax) {
				yMax = (int) pixels.get(i).y;
			}
			if ((int) pixels.get(i).y < yMin) {
				yMin = (int) pixels.get(i).y;
			}
		}
		int width = xMax - xMin;
		int height = yMax - yMin;
		int x = xMin;
		int y = yMin;

		// check if rect dimensions is greater than a tile's
		if (width > tileWidth || height > tileHeight) {
			return null;
		}

		// check this because a number rect should be narrow
		if (width > height) {
			return null;
		}

		// arbitrary parameters to check if rect is too small to be number
		if (height < tileHeight / 3 || width < tileWidth / 6) {
			return null;
		}

		return new Rect(x, y, x + width, y + height);
	}

	/**
	 * checks if Rect encloses a number; uses series of tests to determine
	 * 
	 * @param xCoords
	 *            -- list containing x coordinate pixels in rect
	 * @param yCoords
	 *            -- list containing y coordinate pixels in rect
	 * @return null if not number, non-null otherwise
	 */
	private Rect isNumber(List<Point> pixels, int tilewidth, int tileheight) {

		// if pixels are empty return null
		if (pixels.size() == 0) {
			return null;
		}
		// sort list of pixels; first and last of each list will be the bounds
		// of rect
		int xMax = (int) pixels.get(0).x;
		int xMin = (int) pixels.get(0).x;
		int yMax = (int) pixels.get(0).y;
		int yMin = (int) pixels.get(0).y;

		for (int i = 0; i < pixels.size(); i++) {
			if ((int) pixels.get(i).x > xMax) {
				xMax = (int) pixels.get(i).x;
			}
			if ((int) pixels.get(i).x < xMin) {
				xMin = (int) pixels.get(i).x;
			}
			if ((int) pixels.get(i).y > yMax) {
				yMax = (int) pixels.get(i).y;
			}
			if ((int) pixels.get(i).y < yMin) {
				yMin = (int) pixels.get(i).y;
			}
		}
		int width = xMax - xMin;
		int height = yMax - yMin;
		int x = xMin;
		int y = yMin;

		// check if rect dimensions is greater than a tile's
		if (width > tilewidth || height > tileheight) {
			return null;
		}

		// check this because a number rect should be narrow
		if (width > height) {
			return null;
		}

		// arbitrary parameters to check if rect is too small to be number
		if (height < tileheight / 2 || width < tilewidth / 3) {
			return null;
		}

		return new Rect(x, y, x + width, y + height);
	}

}
