package com.example.sudokusolver;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class SudokuGridActivity extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sudoku_layout);
		Log.d("activyt", "in sudokugrid");
	}
}
