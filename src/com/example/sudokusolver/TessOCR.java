package com.example.sudokusolver;

import android.graphics.Bitmap;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

public class TessOCR{
    //public static final String DATA_PATH;
	Bitmap mBitmap;
    
	public TessOCR(Bitmap bitmap){
		mBitmap = bitmap;
	}
	
    boolean trainTess(){
		return false;
        
    }
    
    void initOCR(){
        int[][] grid = new int[9][9];
        TessBaseAPI tessOCR = new TessBaseAPI();
        
        //datapath is in parent directory of tessdata
        tessOCR.init(DATA_PATH, "eng");
        tessOCR.setImage(mBitmap);
        int width = mBitmap.getWidth() / 9;
        int height = mBitmap.getHeight() / 9;
        
        for(int i = 0; i < 9; i++){
            for(int j = 0; j < 9; j++){
                int x = width * i;
                int y = height * j;
                tessOCR.setRectangle(x, y, width, height);
                String result = tessOCR.getUTF8Text();
                if(result == null && result == "") {
                    grid[i][j] = 0;
                } else {
                    grid[i][j] = Integer.parseInt(result);
                }
                Log.i("Sudoku " + i + "," + j, grid[i][j] + "");
            }
        }
        tessOCR.end();
    }
    
}
