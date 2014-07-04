package com.example.sudokusolver;



import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Environment;
import android.util.Log;

public class ImgManipulation{

	private Context mContext;
	private Bitmap mBitmap;
	
    public ImgManipulation(Context context, Bitmap bitmap){
        mContext = context;
        mBitmap = bitmap;
    }
    
    public Mat bitmapToMat(Bitmap bmp){
        Mat mat = new Mat(bmp.getHeight(), bmp.getWidth(), CvType.CV_8UC1);
		Utils.bitmapToMat(bmp, mat);
        return mat;
    }
    
    
    public Bitmap matToBitmap(Mat mat){
        Bitmap bmp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bmp);
        return bmp;
    }
    
    public void doStoreBitmap(Bitmap src){
    	Mat m = bitmapToMat(src);
    	//Imgproc.cvtColor(m, m, Imgproc.COLOR_BGR2GRAY);
    	//Imgproc.adaptiveThreshold(m, m, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);
    	Log.d("checkpoint", "dostorebitmap");
 
    	
    	storeImage(src);
    }
    
    private void storeImage(Bitmap image) {
    	if(image == null) Log.d("null bitmap", "storeImage");
        File pictureFile = getOutputMediaFile();
        Log.d("filename", pictureFile + "");
        if (pictureFile == null) {
            Log.d(TAG, "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        } 
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
            Log.d("FILe", "Success");
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }  
    }
    
    String TAG = "file save";
    /** Create a File for saving an image or video */
    private  File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this. 
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + mContext.getPackageName()
                + "/Files"); 

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        } 
        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
            String mImageName="MI_"+ timeStamp +".jpg";
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);  
        return mediaFile;
    } 
   
}
