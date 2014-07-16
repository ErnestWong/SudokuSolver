package com.example.sudokusolver;

import android.graphics.Bitmap;
import android.graphics.Color;

public class BlobExtraction {

	private Mat img;
	List<Rect> blobs = new ArrayList<Rect>();
    private int BLACK = 0;
    
    public BlobExtraction(Mat mat){
        img = mat;
    }
    
    public void blobExtract(){
        for(int y = 0; y < img.rows(); y++){
            for(int x = 0; x < img.cols(); x++){
                if(int)img.get(y,x)[0] != BLACK){
                    Rect rect = new Rect();
                    Scalar scBlack = new Scalar(0);
                    Imgproc.floodFill(img, new Point(x,y), scBlack, rect, scBlack, scBlack, 8);
                    
                    if(isNumber(rect)){
                        blobs.add(rect);
                    }
                }
            }
        }
    }
    
    public List<Rect> getBlobs(){
        return blobs;
    }
    
    private boolean isNumber(Rect rect){
        int cellWidth = Mat.cols()/9;
        int cellHeight = Mat.rows()/9;
        
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
