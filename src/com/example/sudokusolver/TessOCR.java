package com.example.sudokusolver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

/**
 * Handles OCR portion of application-- uses tess-two API to recognize digits
 * @author E Wong
 *
 */
public class TessOCR{
 
    private Bitmap mBitmap;
    private Context mContext;
    private TessBaseAPI tessAPI;
    private boolean isInit = false;
    private boolean isEnded = false;
  
    public final String TRAINED_DATA_DIRECTORY = "tessdata/";
    public final String TRAINED_DATA_FILENAME = "eng.traineddata";
    private String DATA_PATH;
    public static final String TAG_DIR_CREATE_SUCCESS = "directory created success";
    public static final String TAG_DIR_CREATE_FAIL = "directory failed create";
    
    /**
     * constructor to obtain context+bitmap and initializes DATA_PATH needed for class methods
     **/ 
    public TessOCR(Context context){
    	mContext = context;
    	DATA_PATH = Environment.getExternalStorageDirectory() + "/Android/data/" + mContext.getPackageName() + "/Files/";
    }
    
    /**
     * initializes OCR-- copies traineddata file from assets to external storage 
     * (which is required by tess-two API) and accesses tess API
     **/ 
    public void initOCR(){
        tessAPI = new TessBaseAPI();
        copyTessFileToStorage();
        
        //datapath is in parent directory of tessdata
        tessAPI.init(DATA_PATH, "eng");
        tessAPI.setVariable("tessedit_char_whitelist", "123456789");
        isInit = true;
       
    }
    
    public boolean isInit(){
    	return isInit;
    }
    
    public boolean isEnded(){
    	return isEnded;
    }
    
    public String doOCR(Bitmap bmp){
    	tessAPI.setImage(bmp);
    	String result = tessAPI.getUTF8Text();
        return result;
    }
    
    public void endTessOCR(){
    	tessAPI.end();
    	isEnded = true;
    }
    
    /**
     * copies traineddata file from assets folder to external storage (destination is DATA_PATH)
     **/
    private void copyTessFileToStorage(){
    	try {
    	    //initializes file and parent directory of file
	    File dir = new File(DATA_PATH + TRAINED_DATA_DIRECTORY);
	    File file = new File(DATA_PATH + TRAINED_DATA_DIRECTORY + TRAINED_DATA_FILENAME);
			
	    //checks if file already exists
	    if(!file.exists()){
		//copies file in assets folder to stream
	        InputStream in = mContext.getAssets().open(TRAINED_DATA_DIRECTORY + TRAINED_DATA_FILENAME);
				
		    //create parent directories
		    if(dir.mkdirs()){
			Log.d(TAG_DIR_CREATE_SUCCESS, dir.toString());
		    } else {
			Log.d(TAG_DIR_CREATE_FAIL, dir.toString());
		    }
			
	 	    //set outputstream to the destination in external storage
	 	    //copies inputstream to outputstream
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
	    Log.d("file error TessOCR", e.toString());
	    e.printStackTrace();
	}
   }
    
}

