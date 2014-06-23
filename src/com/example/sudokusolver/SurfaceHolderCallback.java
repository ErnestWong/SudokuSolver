package com.example.sudokusolver;

import java.io.IOException;

import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;

/**
 * class that manages changes made to surface
 * @author E Wong
 *
 */
public class SurfaceHolderCallback implements SurfaceHolder.Callback {

	private Camera mCamera;
	private boolean mPreviewRunning;
	private PictureCallback mPictureCB;
	String TAG = "preview"; 
	
	public SurfaceHolderCallback(Camera camera, RectangleView rectView){
		mCamera = camera;
		mPictureCB = new PictureCallback(rectView, this);
	}
	// called immediately when surface is first created(initialize camera)
	@Override
	public void surfaceCreated(SurfaceHolder sh) {
		
		try {
			mCamera = Camera.open();	
			mCamera.setDisplayOrientation(90);
			mCamera.setPreviewDisplay(sh);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// called when change made to surface; called at least once immediately
	// after onsurfacecreated
	@Override
	public void surfaceChanged(SurfaceHolder sh, int format, int width,
			int height) {
		startPreview();

	}

	// called immediately after surface destroyed(stop preview and release cam)
	@Override
	public void surfaceDestroyed(SurfaceHolder sh) {
		stopPreview();
		mCamera.release();
		mCamera = null;
	}
	
	public void startPreview(){
		
		if(!mPreviewRunning && mCamera != null){
			mCamera.startPreview();
			Log.i(TAG, "start preview");
			mPreviewRunning = true;
		}
	}
	
	public void stopPreview(){
		Log.i(TAG, "stop preview");
		if(mPreviewRunning && mCamera != null){
			mCamera.stopPreview();
			mPreviewRunning = false;
		}
	}
	
	public void takePicture(){
		mCamera.takePicture(null, null, mPictureCB);
	}

}
