package com.example.sudokusolver;

import android.graphics.Bitmap;
import android.os.AsyncTask;

public class SaveImageAsync extends AsyncTask<ImgManipulation, Void, int[][]>{

	/*
	@Override
	protected void onPreExecute(Object... arg0){
		
	}
	*/
	
	@Override
	protected int[][] doInBackground(ImgManipulation... imgManipulation) {
		int[][] grid = imgManipulation[0].doStoreBitmap();
		return grid;
	}

	@Override
	protected void onPostExecute(int[][]result){
		
	}
}
