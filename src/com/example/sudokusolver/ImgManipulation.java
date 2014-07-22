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
import android.graphics.Rect;
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
		ImgManipUtil.dilateMat(result, 3);
        //ImgManipUtil.openMat(clean, 1);
		Imgproc.threshold(result, result, 128, 255, Imgproc.THRESH_BINARY);
        
        //Bitmap bmp = ImgManipUtil.matToBitmap(result);
		//FileSaver.storeImage(bmp, "full");

        BlobExtractv2 blobext = new BlobExtractv2(result);
        blobext.blobExtract();
        
        Queue<Rect> numRects = blobext.getTileRects();
        //bmp = blobext.getFixedBitmap();
        //bmp = matToBitmap(tmp);
        Bitmap bmp = ImgManipUtil.matToBitmap(clean);
        FileSaver.storeImage(bmp, "laterFater");
        
        int count = 0;
        TessOCR ocr = new TessOCR(bmp, mContext);
		ocr.initOCR();

        while(!numRects.isEmpty()){
        	Rect r = numRects.remove();
        	Mat tmp = ImgManipUtil.cropSubMat(r, clean, 10);
        	Bitmap b = blobext.removeNoise(tmp);
			//Mat m = bitmapToMat(b);
			//Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(2, 2));
			//Imgproc.morphologyEx(m, m, Imgproc.MORPH_CLOSE, kernel);
			//Imgproc.Canny(m, m, 50, 200);
			//b = matToBitmap(m);
			FileSaver.storeImage(b, "num " + count );
			String ans = ocr.doOCR(b);
            Log.d(TAG_TILE_STATUS, count + ", _ nonempty " + ans); 
            count++;
        }
        
        /*
		//9x9 array to store each number
		Bitmap[][] nums = new Bitmap[9][9];
		int width = bmp.getWidth() / 9;
		int height = bmp.getHeight() / 9;
		int c = 0;
		for(int i = 0; i < 9; i++){
			for(int j = 0; j < 9; j++){
				int x = width * j;
				int y = height * i;


				nums[i][j] = Bitmap.createBitmap(bmp, x, y, width, height);
				//FileSaver.storeImage(nums[i][j], i + "," + j);
				if(ImgManipUtil.findEmptyTile(nums[i][j], CONST_RATIO)){
					Log.d(TAG_TILE_STATUS, i + "," + j + ": empty");
					FileSaver.storeImage(nums[i][j], i + "," + j + "EMPTY");
				} else {
					Log.d(TAG_TILE_STATUS, i + "," + j + ": nonempty");
					FileSaver.storeImage(nums[i][j], i + "," + j + "NOTEMPTY");
					c++;

					//Rect r = numRects.remove();
					//Bitmap b = Bitmap.createBitmap(bmp, r.left, r.top, r.right-r.left, r.bottom-r.top);
					//Mat tmpB = bitmapToMat(b);
					//Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new Size(5, 5));
					//Imgproc.dilate(tmpB, tmpB, kernel);
					//b = matToBitmap(tmpB);
					//FileSaver.storeImage(b, c+ "num " + i + "," + j);
					//String ans = ocr.doOCR(b);
		            //Log.d(TAG_TILE_STATUS + "nonempty",count + ": "+ i + "," + j + ", "+ ans); 

				}
			}
		}
         */
		//Log.d("count", c + "," + numRects.size());
        
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
	
	
}