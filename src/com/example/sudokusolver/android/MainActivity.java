package com.example.sudokusolver.android;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import com.example.sudokusolver.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ToggleButton;

public class MainActivity extends Activity {
	private Context mContext;
	private Camera mCamera;
	// private SurfaceView mSurfaceView;
	private SurfaceHolder mSurfaceHolder;
	private Camera.Parameters mCameraParams;
	private SurfaceHolderCallback mSurfaceCB;
	private RectangleView mRectView;
	public ImageView imgview;
	// private Button mCaptureButton;

	// private CameraBridgeViewBase opencvCameraView;

	/*
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i("OpenCVLoad", "OpenCV loaded successfully");
				// opencvCameraView.enableView();
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};
	*/
	
	static{
		if (!OpenCVLoader.initDebug()) {
	        Log.d("OpenCV load", "couldn't load library");
	    }
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mContext = getApplicationContext();
		final SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceviewcamera);
		final Button captureButton = (Button) findViewById(R.id.takepicture);
		final FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_layout);
		final ToggleButton flashButton = (ToggleButton) findViewById(R.id.toggle_flash);
		imgview = (ImageView) findViewById(R.id.imgview);
		mRectView = new RectangleView(this);
		frameLayout.addView(mRectView);
		
		// get the holder providing access+control over the surfaceview
		mSurfaceHolder = surfaceView.getHolder();

		mSurfaceCB = new SurfaceHolderCallback(mCamera, mRectView, mContext);
		// add callback interface to holder
		mSurfaceHolder.addCallback(mSurfaceCB);
		/*
		flashButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(flashButton.isChecked()){
					mSurfaceCB.toggleFlash(true);
					flashButton.toggle();
					flashButton.setText("Flash on");
				} else {
					mSurfaceCB.toggleFlash(false);
					flashButton.toggle();
					flashButton.setText("Flash off");
				}
				
			}
		});
		*/
		// button
		captureButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.d("Button Clicked", "button");
				mSurfaceCB.takePicture();
				// mRectView.setPaintColor(Color.GREEN);

			}
		});
		
		//set on touch listener for autofocus
		surfaceView.setOnTouchListener(new OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mSurfaceCB.startAutoFocus();		
				return true;
			}
					
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
		mSurfaceCB.stopPreview();

	}

	@Override
	protected void onResume() {
		super.onResume();
		mRectView.setPaintColor(Color.BLACK);
		//OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
		mSurfaceCB.startPreview();

	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
