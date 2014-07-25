package com.example.sudokusolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import com.example.sudokusolver.util.FileSaver;
import com.example.sudokusolver.util.ImgManipUtil;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Environment;
import android.util.Log;


/**
 * Handles the image processing portion of the application-- receives raw bitmap from 
 * PictureCallback and extracts the sudoku layout from image
 * Uses OpenCV library
 * @author E Wong
 *
 */
public class ImgManipulation {

	private Context mContext;
	private final float CONST_RATIO = (float) 0.03;
	private Bitmap mBitmap;
	private Mat clean;

	public final String TAG_SUBMAT_DIMENS = "Submat dimensions";
	public final String TAG_WHITE_POINT = "White point coorinates";
	public final String TAG_TILE_STATUS = "tile status";
	public final static String TAG_HOUGHLINES = "HoughLines info";
	public final String TAG_ERROR_FIND_GRID = "findGridArea error";
	public final String TAG_ERROR_FLOODFILL = "Floodfill setPixel error";

	public ImgManipulation(Context context, Bitmap bitmap) {
		mContext = context;
		mBitmap = bitmap;
	}



	/**
	 * performs all the required image processing to find sudoku grid numbers
	 */
	public void doStoreBitmap() {
		clean = ImgManipUtil.bitmapToMat(mBitmap);
		Mat result = extractSudokuGrid(clean);
		Imgproc.cvtColor(clean, clean, Imgproc.COLOR_BGR2GRAY);
		Imgproc.adaptiveThreshold(clean, clean, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 11, 2);
		FileSaver.storeImage(ImgManipUtil.matToBitmap(clean), "threshold");
		ImgManipUtil.dilateMat(result, 4);
        //ImgManipUtil.openMat(clean, 1);
		Imgproc.threshold(result, result, 128, 255, Imgproc.THRESH_BINARY);
		FileSaver.storeImage(ImgManipUtil.matToBitmap(result), "resultb4 ");
		BlobExtract bE = new BlobExtract();
		//bE.getBoundingRects(result);
		List<Rect> boundingRects = bE.getBoundingRects(result);
		List<Mat> listmats = bE.findCleanNumbers(clean, boundingRects);
		Mat rectMat = bE.drawRectsToMat(clean, boundingRects);
		FileSaver.storeImage(ImgManipUtil.matToBitmap(rectMat), "blobext");
        

        boolean[][]emptyorNot = findNumTiles(rectMat, boundingRects);
        for(int i = 0; i < 9; i++){
        	for(int j = 0; j < 9; j++){
        		Log.d("empty or not", emptyorNot[i][j]+" " + i + "," + j);
        	}
        }
        int count = 0;
        TessOCR ocr = new TessOCR(mContext);
		ocr.initOCR();

        for(int i = 0; i < listmats.size(); i++){
        	Bitmap b = ImgManipUtil.matToBitmap(listmats.get(i));
			FileSaver.storeImage(b, "num " + i );
			String ans = ocr.doOCR(b);
            Log.d(TAG_TILE_STATUS, i + ", _ nonempty " + ans); 
            count++;
        }
      
		ocr.endTessOCR();
		
	}

	/**
	 * performs OpenCV image manipulations to extract and undistort sudoku puzzle from image
	 * @param bitmap source bitmap
	 * @return Mat image of fixed puzzle
	 */
	public Mat extractSudokuGrid(Mat mat){
		//convert source bitmap to mat; use canny operation
		Mat edges = new Mat(mat.size(), mat.type());
		Imgproc.Canny(mat, edges, 50, 200);

		//trim external noise to localize the sudoku puzzle and stores in bmp then m2
		int[] bounds = ImgManipUtil.findGridBounds(edges);
		edges = subMat(edges, bounds);
		clean = subMat(clean, bounds);
		
		List<Point> corners = findCorners(edges);
		Point topLeft = corners.get(0);
		Point topRight = corners.get(1);
		Point bottomLeft = corners.get(2);
		Point bottomRight = corners.get(3);
		
		edges = ImgManipUtil.fixPerspective(topLeft, topRight, bottomLeft, bottomRight, edges);
		clean = ImgManipUtil.fixPerspective(topLeft, topRight, bottomLeft, bottomRight, clean);
		FileSaver.storeImage(ImgManipUtil.matToBitmap(edges), "edges");
		FileSaver.storeImage(ImgManipUtil.matToBitmap(clean), "clean");
		return edges;
	}
	
	private Mat subMat(Mat mat, int[] bounds){
		int left = bounds[0];
		int right = bounds[1];
		int top = bounds[2];
		int bot = bounds[3];
		
		return mat.submat(top, bot, left, right);
	}
	
	private List<Point> findCorners(Mat mat){
		Mat lines = new Mat();
		List<double[]> horizontalLines = new ArrayList<double[]>();
		List<double[]> verticalLines = new ArrayList<double[]>();

		Imgproc.HoughLinesP(mat, lines, 1, Math.PI/180, 150);

		for(int i = 0; i < lines.cols(); i++){
			double[] line = lines.get(0, i);
			double x1 = line[0];
			double y1 = line[1];
			double x2 = line[2];
			double y2 = line[3];
			if(Math.abs(y2 - y1) < Math.abs(x2 - x1)){
				horizontalLines.add(line);
			}
			else if(Math.abs(x2 - x1) < Math.abs(y2 - y1)){
				verticalLines.add(line);
			}
		}
		String lineInfo = String.format("horizontal: %d, vertical: %d, total: %d", horizontalLines.size(), verticalLines.size(), lines.cols());
		Log.d(TAG_HOUGHLINES, lineInfo);

		//lines for four boundaries of sudoku grid; find edges of the sudoku grid 
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
		
		//obtain four corners of sudoku grid and apply perspective transform to undistort image
		Point topLeft = ImgManipUtil.findCorner(topLine, leftLine);
		Point topRight = ImgManipUtil.findCorner(topLine, rightLine);
		Point bottomLeft = ImgManipUtil.findCorner(bottomLine, leftLine);
		Point bottomRight = ImgManipUtil.findCorner(bottomLine, rightLine);
		
		List<Point> corners = new ArrayList<Point>(4);
		corners.add(topLeft);
		corners.add(topRight);
		corners.add(bottomLeft);
		corners.add(bottomRight);
		
		return corners;
	}
	
	private boolean[][] findNumTiles(Mat m, List<Rect> rects){
		byte[][] arrayMat = addNumsToMat(m, rects);
		boolean[][] numTileArray = new boolean[9][9];

		for(int i = 0; i < 9; i++){
			for(int j = 0; j < 9; j++){
				numTileArray[i][j] = containsNumberTile(arrayMat, j, i);
			}
		}
		return numTileArray;
	}

	/**
	 * determines if array holding mat contains a number 
	 * @param matarray array containing pixel info for mat
	 * @param ratio percentage of the tile that must be white
	 * @param xBound from 0 to 8 the number of the tile
	 * @param yBound from 0 to 8 the number of the tile
	 * @return true if empty, false otherwise
	 */
	private boolean containsNumberTile(byte[][]matarray, int xBound, int yBound){
		int area = matarray.length * matarray[0].length;
		int totalWhite = 0;
		int xStart = xBound * matarray[0].length/9;
		int xEnd = xStart + matarray[0].length/9 - 5;
		int yStart = yBound * matarray.length/9;
		int yEnd = yStart + matarray.length/9 - 5;

		for(int y = yStart; y < yEnd; y++){
			for(int x = xStart; x < xEnd; x++){
				if(matarray[y][x] == 1){
					totalWhite++;
				}
			}
		}
		if(totalWhite > 0 * area){
			return true;
		} else {
			return false;
		}
	}

	private byte[][] addNumsToMat(Mat m, List<Rect>nums){
		byte[][] matArray = new byte[m.rows()][m.cols()];

		for(Rect r: nums){
			for(int y = r.y; y < r.y+r.height-1; y++){
				for(int x = r.x; x < r.x+r.width-1; x++){
					//set to 1 (white)
					matArray[y][x] = 1;
				}
			}
		}
		return matArray;
	}
	
	
}