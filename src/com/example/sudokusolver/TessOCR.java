package com.example.sudokusolver;

import com.googlecode.tesseract.android.TessBaseAPI;

public class TessOCR{
    //public static final String DATA_PATH;
    
    boolean trainTess(){
		return false;
        
    }
    /*
    void initOCR(){
        int[][] grid = new int[9][9];
        TessBaseAPI tessOCR = new TessBaseAPI();
        
        //datapath is in parent directory of tessdata
        tessOCR.init(DATA_PATH, "eng");
        tessOCR.setImage(bmp);
        int xInc = bmp.getWidth() / 9;
        int yInc = bmp.getHeight() / 9;
        
        for(int i = 0; i < 9; i++){
            for(int j = 0; j < 9; j++){
                int x = xInc * i;
                int y = xInc * j;
                tessOCR.setRectangle(x, y, x + xInc, y + yInc);
                String result = tessOCR.getUTF8Text();
                if(result == null && result == "") {
                    grid[i][j] = 0;
                } else {
                    grid[i][j] = Integer.parseInt(result);
                }
            }
        }
        tessOCR.end();
    }
    */
}
