package com.example.sudokusolver.imageproc.ccl;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;

import android.graphics.Point;
import android.util.Log;

/**
 * Uses implementation of connected component labelling algorithm(blob
 * extraction) to extract the numbers in the sudoku image while eliminating
 * non-number noise (i.e. gridlines, small blobs, etc) and returns byte array
 * representation of image that contains only the numbers-- which is needed for
 * OCR
 **/
public class ConnectedComponentLabel {

	private final int BLACK = 0;

	public ConnectedComponentLabel() {
	}

	/**
	 * wrapper method for blobextract
	 * 
	 * @param mat
	 *            input source image
	 * @return byte array containing only the numbers-- ready for OCR usage
	 **/
	public byte[][] getByteArrayForOCR(Mat mat) {
		int[][] image = blobExtract(mat);
		return intToByteArray(image);
	}

	/**
	 * converts 2D int array to byte array representation
	 **/
	public byte[][] intToByteArray(int[][] img) {
		byte[][] stream = new byte[img.length][img[0].length];
		// int index = 0;
		for (int i = 0; i < img.length; i++) {
			for (int j = 0; j < img[0].length; j++) {
				if (img[i][j] == 0) {
					// black
					stream[i][j] = 0;
				} else {
					// white
					stream[i][j] = 127;
				}
				// index++;
			}
		}
		return stream;
	}

	/**
	 * performs ccl on Mat image; implements two-pass algorithm to label each
	 * pixel and partitions each unique label into a list to identify numbers
	 * 
	 * @param matImage
	 *            source Mat sudoku image
	 * @return int array representation of image containing only the numbers
	 *         (without noise)
	 **/
	public int[][] blobExtract(Mat matImage) {
		Log.d("Blob extracting", "starting");
		int[][] img = matToIntArray(matImage);
		UnionFind unionFind = new UnionFind();
		int currentLabel = 1;

		// initialize labels to default zero
		int[][] label = new int[img.length][img[0].length];
		Log.d("blob extracting", "starting iteration");
		// iterate through each element
		for (int y = 0; y < img.length; y++) {
			for (int x = 0; x < img[0].length; x++) {
				// if element in background, skip
				if (img[y][x] == BLACK) {
					continue;
				}

				// if no non-background neighbors (or no labelled neighbors),
				// label as currentLabel and increment currentLabel
				// Record label to unionFind
				if (!hasNeighbors(y, x, label)) {
					label[y][x] = currentLabel;
					unionFind.addLabel(currentLabel);
					currentLabel++;
				} else {
					// find neighbor with smallest label and assign it to
					// current
					// element
					int[] neighbors = getNeighborLabels(y, x, label);
					label[y][x] = findMin(neighbors);

					// store equivalence between neighboring labels
					int first = neighbors[0];
					for (int i = 1; i < neighbors.length; i++) {
						if (neighbors[i] != BLACK) {
							unionFind.union(first, neighbors[i]);
						}
					}
				}

			}
		}
		Log.d("Blob extracting", "done first pass");
		// second pass; iterate through each element
		for (int y = 0; y < img.length; y++) {
			for (int x = 0; x < img[0].length; x++) {
				// if element in background, continue
				if (img[y][x] == BLACK) {
					continue;
				}
				// relabel the label to its root
				label[y][x] = unionFind.find(label[y][x]);
			}
		}
		Log.d("Blob extracting", "done second pass, current label: "
				+ currentLabel);

		List<List<Point>> regions = new ArrayList<List<Point>>();

		// initialize empty arrayList to each element in regions
		for (int i = 0; i < currentLabel; i++) {
			regions.add(new ArrayList<Point>());
		}
		Log.d("Blob extracting", "done initializing regions");
		// store coordinate of pixel to corresponding label
		for (int y = 0; y < label.length; y++) {
			for (int x = 0; x < label[0].length; x++) {
				regions.get(label[y][x]).add(new Point(x, y));
			}
		}

		removeNoise(regions, img);
		return img;
	}

	/**
	 * fills in non-number blobs with black-- uses isNumber() to determine
	 * whether it is a number or not
	 * 
	 * @param blobs
	 *            List of List storing the pixels of each blob
	 * @param img
	 *            int array representation of image (the method edits this
	 *            directly)
	 **/
	public void removeNoise(List<List<Point>> blobs, int[][] img) {
		int numberCount = 0;
		for (int i = 0; i < blobs.size(); i++) {
			if (!isNumber(blobs.get(i), img)) {
				for (Point p : blobs.get(i)) {
					img[p.y][p.x] = BLACK;
				}
			} else {
				numberCount++;
			}
		}
		Log.d("blob extracting", "blobs size: " + blobs.size()
				+ ", number count: " + numberCount);

	}

	/**
	 * convert Mat representation of image to int
	 **/
	private int[][] matToIntArray(Mat mat) {
		Log.d("blob extracting", "matToIntArray");
		int[][] image = new int[mat.rows()][mat.cols()];
		for (int i = 0; i < mat.rows(); i++) {
			for (int j = 0; j < mat.cols(); j++) {
				image[i][j] = (int) mat.get(i, j)[0];
			}
		}
		return image;
	}

	/**
	 * checks if the List of coordinates representing a blob is a number
	 * according to the size of its bounding rectangle
	 * 
	 * @return true if isNumber, false otherwise
	 **/
	private boolean isNumber(List<Point> pixels, int[][] img) {
		int tileHeight = img.length / 9;
		int tileWidth = img[0].length / 9;

		// if pixels are empty return null
		if (pixels.size() == 0) {
			return false;
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
	 * checks if pixel has neighboring pixels that are not background
	 * 
	 * @return true if contains at least one neighboring non-background pixel
	 *         false if no neighboring non-background pixel
	 **/
	public boolean hasNeighbors(int y, int x, int[][] label) {
		for (int i = y - 1; i <= y + 1; i++) {
			for (int j = x - 1; j <= x + 1; j++) {
				if (outOfBounds(j, i, label))
					continue;
				if (i == y && j == x)
					continue;

				if (label[i][j] != BLACK) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * returns labels of neighboring pixels
	 * 
	 * @return array of neighbor's pixels (value is 0 if not labelled)
	 **/
	public int[] getNeighborLabels(int y, int x, int[][] label) {
		int[] neighbors = new int[8];
		int index = 0;
		for (int i = y - 1; i <= y + 1; i++) {
			for (int j = x - 1; j <= x + 1; j++) {
				if (i == y && j == x)
					continue;

				if (!outOfBounds(j, i, label) && label[i][j] != BLACK) {
					neighbors[index] = label[i][j];
					index++;
				}
			}
		}
		return neighbors;
	}

	/**
	 * determines if pixel is out of bound
	 * 
	 * @param x
	 *            x coordinate of pixel
	 * @param y
	 *            y coordinate of pixel
	 * @param label
	 *            array representing image
	 **/
	private boolean outOfBounds(int x, int y, int[][] label) {
		if (x < 0 || x >= label[0].length) {
			return true;
		}
		if (y < 0 || y >= label.length) {
			return true;
		}
		return false;
	}

	/**
	 * returns minimum non-zero value in array
	 **/
	public int findMin(int[] list) {
		int min = Integer.MAX_VALUE;
		for (int i = 0; i < list.length; i++) {
			if (list[i] != BLACK) {
				if (list[i] < min) {
					min = list[i];
				}
			}
		}
		return min;
	}

}
