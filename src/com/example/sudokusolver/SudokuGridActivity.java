package com.example.sudokusolver;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class SudokuGridActivity extends Activity{
	private int[][]unsolved;
	private int[][]solved;
	private SudokuView mSudokuView;
	private LinearLayout layout;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sudoku_layout);
		final LinearLayout linLay = (LinearLayout) findViewById(R.id.sudoku_layout);
		Log.d("activyt", "in sudokugrid");
		
		Bundle b = this.getIntent().getExtras();
		//unsolved = (int[][]) b.getSerializable("unsolved");
		//solved = (int[][]) b.getSerializable("solved");
		
		mSudokuView = new SudokuView(this);
		linLay.addView(mSudokuView);
		
	}
}
