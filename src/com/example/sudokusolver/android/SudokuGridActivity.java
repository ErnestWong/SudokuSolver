package com.example.sudokusolver.android;

import com.example.sudokusolver.R;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class SudokuGridActivity extends Activity {
	private int[][] unsolved;
	private int[][] solved;
	private SudokuView mSudokuView;
	private LinearLayout layout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sudoku_layout);
		final LinearLayout linLay = (LinearLayout) findViewById(R.id.sudoku_layout);
		Log.d("activyt", "in sudokugrid");

		Bundle b = this.getIntent().getExtras();
		unsolved = to2DArray(b.getIntArray("unsolved"));
		solved = to2DArray(b.getIntArray("solved"));

		mSudokuView = new SudokuView(this, unsolved, solved);
		linLay.addView(mSudokuView);

	}

	private int[][] to2DArray(int[] input) {
		int index = 0;
		int[][] output = new int[9][9];
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				output[i][j] = input[index];
				index++;
			}
		}
		return output;
	}
}
