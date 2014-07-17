package com.example.sudokusolver;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.graphics.Bitmap;
import android.util.Log;



public class BlobExtraction {

	private Mat img;
	private Mat mask;
	Queue<Rect> blobs = new LinkedList<Rect>();
    private int WHITE = 255;
    
    public BlobExtraction(Mat mat){
        img = mat;
        Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2GRAY);
        mask = new Mat(new Size(img.cols()+2, img.rows()+2), CvType.CV_8UC1);
        Log.d("type", img.type() + "." + mask.type());
        Imgproc.copyMakeBorder(img, mask, 1, 1, 1, 1, Imgproc.BORDER_CONSTANT, new Scalar(0));
    }
    
    public void blobExtract(){
        for(int y = 0; y < img.rows(); y++){
            for(int x = 0; x < img.cols(); x++){
            	//Log.d("blob extract", "x: " + x + ", y: " + y);
            	//Log.d(x + "," + y, img.get(y, x)[0] + "");
                
            	if((int)img.get(y,x)[0] == WHITE){
                    Rect rect = new Rect();
                    Log.d("before rect", rect.width + "," + rect.height);
                    Scalar scBlack = new Scalar(0,0,0);
                    Imgproc.floodFill(img, mask, new Point(x,y), scBlack, rect, scBlack, scBlack, 8);
                    
                    Log.d("rect dimens", rect.width + "," + rect.height);
                    //if(isNumber(rect)){
                        blobs.add(rect);
                    //}
                }
                
            }
        }
        Bitmap bmp = Bitmap.createBitmap(img.cols(),img.rows(), Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(img, bmp);
        FileSaver.storeImage(bmp, "floodfilled");
        Log.d("size", blobs.size() + "");
    }
    
    public Queue<Rect> getBlobs(){
        return blobs;
    }
    
    private boolean isNumber(Rect rect){
        int cellWidth = img.cols()/9;
        int cellHeight = img.rows()/9;
        
        //series of tests to ensure that rectangle contains a number
        //(needs further testing)
        if(rect.width > cellWidth || rect.height > cellHeight){
            return false;
        } 
        if(cellWidth > cellHeight){
            return false;
        }
        if(rect.height < cellHeight/3 || rect.width < cellWidth/5){
            return false;
        }
        return true;
    }
}
