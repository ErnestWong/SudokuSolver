package com.example.sudokusolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;

import org.opencv.core.Point;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.Log;

public class BlobExtractv2 {

	private Bitmap mBitmap;
	private Bitmap fixedBmp;
	private int tileWidth;
	private int tileHeight;
	private static final int BUFFER = 0;
	private static int GAP;
	private List<Rect> tileRects = new ArrayList<Rect>();

	public BlobExtractv2(Bitmap bmp) {
		mBitmap = bmp;
		tileWidth = mBitmap.getWidth() / 9;
		tileHeight = mBitmap.getHeight() / 9;
		GAP = tileHeight / 2;
		fixedBmp = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(),
				mBitmap.getConfig());
		fixedBmp.eraseColor(Color.BLACK);
	}

	/**
	 * performs blob extraction and stores found blobs in tileRects Must call
	 * this method before getTileRects()
	 */
	public void blobExtract() {
		Log.d("extract", "extracting");
		int count = 0;
		int numcount = 0;
		for (int y = 1; y < mBitmap.getHeight() - 1; y++) {
			for (int x = 1; x < mBitmap.getWidth() - 1; x++) {
				if (mBitmap.getPixel(x, y) == Color.BLACK) {
					continue;
				}

				Rect r = floodfill(new Point(x, y));
				if (r != null) {
					numcount++;
					tileRects.add(r);
				}

				count++;

			}
		}
		Log.d("number of blobs", count + "," + numcount);
	}

	/**
	 * performs floodfill at start point and fills connected whitespace to black
	 * 
	 * @param start
	 * @return
	 */
	private Rect floodfill(Point start) {
		// keeps track of checked pixels
		boolean[][] checked = new boolean[mBitmap.getWidth()][mBitmap.getHeight()];
		List<Integer> xCoords = new ArrayList<Integer>();
		List<Integer> yCoords = new ArrayList<Integer>();
		List<Point> pixels = new ArrayList<Point>();
		// queue of points to store the pixels; add initial pixel
		Queue<Point> q = new LinkedList<Point>();
		q.add(start);

		// remove pixel and check adjacent pixels until queue is empty
		while (!q.isEmpty()) {
			Point p = q.remove();

			if (!checked[(int) p.x][(int) p.y]) {
				if (mBitmap.getPixel((int) p.x, (int) p.y) == Color.WHITE && !outOfBounds(p)) {
					xCoords.add((int) p.x);
					yCoords.add((int) p.y);
					pixels.add(p);

					mBitmap.setPixel((int) p.x, (int) p.y, Color.BLACK);
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
			checked[(int) p.x][(int) p.y] = true;
		}

		Rect r = isNumber(xCoords, yCoords);

		if (r != null) {
			setNumToBitmap(pixels);
		}
		xCoords.clear();
		yCoords.clear();
		return r;
	}
	
	/**
	 * set bitmap pixels of the fixedBmp to white
	 * @param pixels
	 */
	private void setNumToBitmap(List<Point> pixels){
		for(int i = 0; i < pixels.size(); i++){
			fixedBmp.setPixel((int)pixels.get(i).x, (int)pixels.get(i).y, Color.WHITE);
		}
	}

	/**
	 * @return sorted queue of Rect objects Must call blobExtract() first
	 */
	public Queue<Rect> getTileRects() {
		return sortRects(tileRects);
	}

	/**
	 * @return fixed bitmap removing noise and blobs that aren't numbers
	 *  Must call blobExtract() first
	 **/
	public Bitmap getFixedBitmap() {
		return fixedBmp;
	}

	/**
	 * check if pixel is out of bounds from bitmap
	 * 
	 * @param point-- target pixel
	 * @return
	 */
	private boolean outOfBounds(Point point) {
		if (point.x >= mBitmap.getWidth() - 2
				|| point.y >= mBitmap.getHeight() - 2 || point.x <= 0
				|| point.y <= 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * uses selection sort on input rects and outputs sorted queue
	 * 
	 * @param rects-- input list of Rect
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
	 * @param xCoords-- list containing x coordinate pixels in rect
	 * @param yCoords-- list containing y coordinate pixels in rect
	 * @return null if not number, non-null otherwise
	 */
	private Rect isNumber(List<Integer> xCoords, List<Integer> yCoords) {

		// if pixels are empty return null
		if (xCoords.size() == 0 || yCoords.size() == 0) {
			return null;
		}
		// sort list of pixels; first and last of each list will be the bounds
		// of rect
		Collections.sort(xCoords);
		Collections.sort(yCoords);
		int width = xCoords.get(xCoords.size() - 1) - xCoords.get(0);
		int height = yCoords.get(yCoords.size() - 1) - yCoords.get(0);
		int x = xCoords.get(0);
		int y = yCoords.get(0);
		if (x - BUFFER >= 0)
			x -= BUFFER;
		if (y - BUFFER >= 0)
			y -= BUFFER;
		if (width + BUFFER + x < mBitmap.getWidth())
			width += BUFFER;
		if (height + BUFFER + y < mBitmap.getHeight())
			height += BUFFER;

		// check if rect dimensions is greater than a tile's
		if (width > tileWidth || height > tileHeight) {
			return null;
		}

		// check this because a number rect should be narrow
		if (width > height) {
			return null;
		}

		// arbitrary parameters to check if rect is too small to be number
		if (height < tileHeight / 3 || width < tileWidth / 5) {
			return null;
		}

		return new Rect(x, y, x + width, y + height);
	}
}
