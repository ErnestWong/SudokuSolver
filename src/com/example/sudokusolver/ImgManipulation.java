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

	public final String TAG_SUBMAT_DIMENS = "Submat dimensions";
	public final String TAG_WHITE_POINT = "White point coorinates";
	public final String TAG_TILE_STATUS = "tile status";
	public final String TAG_HOUGHLINES = "HoughLines info";
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
		Mat result = ImgManipUtil.extractSudokuGrid(mBitmap);
		Imgproc.cvtColor(result, result, Imgproc.COLOR_BGR2GRAY);
		Imgproc.adaptiveThreshold(result, result, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 11, 2);
		FileSaver.storeImage(ImgManipUtil.matToBitmap(result), "threshold");
		//ImgManipUtil.dilateMat(result, 3);
        //Imgproc.threshold(result, result, 128, 255, Imgproc.THRESH_BINARY);
		//erodeBitmap();
        
        Bitmap bmp = ImgManipUtil.matToBitmap(result);
        Mat tmp = ImgManipUtil.bitmapToMat(bmp);
		FileSaver.storeImage(bmp, "full");

        BlobExtractv2 blobext = new BlobExtractv2(bmp);
        blobext.blobExtract();
        
        Queue<Rect> numRects = blobext.getTileRects();
        bmp = blobext.getFixedBitmap();
        //bmp = matToBitmap(tmp);
        
        FileSaver.storeImage(bmp, "laterFater");
        
        int count = 0;
        TessOCR ocr = new TessOCR(bmp, mContext);
		ocr.initOCR();

        while(!numRects.isEmpty()){
        	Rect r = numRects.remove();
        	Bitmap b = cropSubBitmap(r, bmp);
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

	private Bitmap cropSubBitmap(Rect r, Bitmap bmp) {
		if (r.left - 10 >= 0) {
			r.left -= 10;
		} else {
			r.left = 0;
		}

		if (r.top - 10 >= 0) {
			r.top -= 10;
		} else {
			r.top = 0;
		}

		if (r.right + 10 < bmp.getWidth()) {
			r.right += 10;
		} else {
			r.right = bmp.getWidth() - 1;
		}

		if (r.bottom + 10 < bmp.getHeight()) {
			r.bottom += 10;
		} else {
			r.bottom = bmp.getHeight() - 1;
		}
		
		return Bitmap.createBitmap(bmp, r.left, r.top, r.right - r.left, r.bottom - r.top);
	}

	
}