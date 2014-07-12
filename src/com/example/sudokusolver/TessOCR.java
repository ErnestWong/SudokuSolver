package com.example.sudokusolver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

public class TessOCR{
    //public static final String DATA_PATH;
	private Bitmap mBitmap;
    private Context mContext;
    public final String TRAINED_DATA_DIRECTORY = "tessdata/";
    public final String TRAINED_DATA_FILENAME = "eng.traineddata";
    private String DATA_PATH;
    
	public TessOCR(Bitmap bitmap, Context context){
		mBitmap = bitmap;
		mContext = context;
	}
	
    boolean trainTess(){
		return false;
        
    }
    
    
    public void initOCR(){
        int[][] grid = new int[9][9];
        TessBaseAPI tessAPI = new TessBaseAPI();
        copyTessFileToStorage();
        //datapath is in parent directory of tessdata
        tessAPI.init(DATA_PATH, "eng");
        tessAPI.setImage(mBitmap);
        
        int width = mBitmap.getWidth() / 9;
        int height = mBitmap.getHeight() / 9;
        
        for(int i = 0; i < 9; i++){
            for(int j = 0; j < 9; j++){
                int x = width * i;
                int y = height * j;
                tessAPI.setRectangle(x, y, width, height);
                String result = tessAPI.getUTF8Text();
        
                Log.i("Sudoku " + i + "," + j ,result);
            }
        }
        
        String result = tessAPI.getUTF8Text();
        Log.d("Results sudoku OCR", result);
        tessAPI.end();
    }
    
    private void copyTessFileToStorage(){
    	try {
    		DATA_PATH = Environment.getExternalStorageDirectory() + "/Android/data/"
					+ mContext.getPackageName() + "/Files/";
    		
			InputStream in = mContext.getAssets().open("tessdata/" + TRAINED_DATA_FILENAME);
			File dir = new File(DATA_PATH + TRAINED_DATA_DIRECTORY);
			File file = new File(DATA_PATH + TRAINED_DATA_DIRECTORY + TRAINED_DATA_FILENAME);
			

				
			
			if(dir.mkdirs()){
				Log.d("directory created", dir.toString());
			} else {
				Log.d("directory creation failed", dir.toString());
			}
			
			if(!file.exists()){
				byte[] buffer = new byte[1024];
				FileOutputStream out = new FileOutputStream(file);
				int length;
				
				while((length = in.read(buffer)) > 0){
					out.write(buffer, 0, length);
				}
				
				out.close();
				in.close();
				Log.d("file copied", " tess success");
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d("file error TessOCR", e.toString());
			e.printStackTrace();
		}
    }
    
}
