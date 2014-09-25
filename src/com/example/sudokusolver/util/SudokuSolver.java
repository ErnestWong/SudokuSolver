package com.example.sudokusolver.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

/**
 * Util class that contains algorithm to solve sudoku puzzle. Accepts 9x9 in
 * array and returns solved 9x9 array
 * 
 * @author E Wong
 * 
 */
public class SudokuSolver {

	/**
	 * exposed method called to solve sudoku puzzle
	 * 
	 * @param grid
	 * @return
	 */
	public static int[][] solveSudoku(int[][] grid) {
		int[][] copy = deepCopy(grid);
		solve(copy);
		return copy;
	}

	/**
	 * recursively solves puzzle
	 * 
	 * @param grid
	 * @return
	 */
	private static boolean solve(int[][] grid) {

		int row = -1;
		int col = -1;

		// find empty cell
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[0].length; j++) {
				if (grid[i][j] == 0) {
					row = i;
					col = j;
				}
			}
		}

		// return true if cannot find empty cell
		if (row < 0 || col < 0) {
			return true;
		}

		// recursively solve
		for (int i = 1; i <= 9; i++) {
			if (legal(i, row, col, grid)) {
				grid[row][col] = i;

				if (solve(grid)) {
					return true;
				}

				grid[row][col] = 0;
			}
		}
		return false;
	}

	/**
	 * checks if move is legal
	 * 
	 * @param num
	 * @param row
	 * @param col
	 * @param grid
	 * @return
	 */
	private static boolean legal(int num, int row, int col, int[][] grid) {
		// check for row violation
		for (int i = 0; i < 9; i++) {
			if (i == col)
				continue;
			if (grid[row][i] == num) {
				return false;
			}
		}

		// check for col violation
		for (int j = 0; j < 9; j++) {
			if (j == row)
				continue;
			if (grid[j][col] == num) {
				return false;
			}
		}

		// check for box violation
		int startRow = (row / 3) * 3;
		int startCol = (col / 3) * 3;
		for (int i = startRow; i < startRow + 3; i++) {
			for (int j = startCol; j < startCol + 3; j++) {
				if (i == row && j == col)
					continue;
				if (grid[i][j] == num) {
					return false;
				}
			}
		}
		return true;
	}

	private static int[][] deepCopy(int[][] grid) {
		int[][] copy = new int[grid.length][grid[0].length];
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[0].length; j++) {
				copy[i][j] = grid[i][j];
			}
		}
		return copy;
	}

	private static void display(int[][] grid) {
		System.out.println("-------------------------");
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				if (j == 0)
					System.out.print("| ");
				System.out.print(grid[i][j] + " ");
				if ((j + 1) % 3 == 0)
					System.out.print("| ");
			}
			System.out.println();
			if ((i + 1) % 3 == 0) {
				System.out.println("-------------------------");
			}
		}
	}

	public static void main(String[] args) {
		int[][] grid = { { 0, 0, 3, 1, 0, 0, 7, 0, 0 },
				{ 5, 6, 0, 0, 8, 7, 0, 0, 0 }, { 8, 0, 0, 5, 0, 0, 0, 6, 0 },
				{ 9, 0, 0, 0, 6, 0, 0, 0, 1 }, { 0, 0, 4, 0, 0, 0, 3, 0, 0 },
				{ 1, 0, 0, 0, 9, 0, 0, 0, 2 }, { 0, 8, 0, 0, 0, 3, 0, 0, 5 },
				{ 0, 0, 0, 7, 1, 0, 0, 3, 9 }, { 0, 0, 9, 0, 0, 2, 1, 0, 0 }, };

		int[][] grid2 = { { 0, 0, 2, 0, 0, 0, 6, 1, 0 },
				{ 4, 0, 0, 0, 0, 2, 0, 8, 0 }, { 7, 0, 0, 0, 6, 0, 0, 5, 9 },
				{ 0, 0, 0, 0, 0, 4, 0, 0, 0 }, { 3, 0, 0, 0, 5, 8, 0, 6, 0 },
				{ 0, 0, 1, 2, 0, 0, 5, 9, 0 }, { 0, 0, 8, 3, 2, 5, 0, 0, 0 },
				{ 0, 0, 0, 0, 0, 0, 0, 0, 0 }, { 2, 0, 4, 0, 0, 0, 0, 0, 6 } };

		int[][] testgrid172 = { { 5, 0, 4, 0, 9, 3, 0, 0, 0 },
				{ 0, 0, 0, 0, 0, 1, 0, 0, 0 }, { 0, 0, 0, 8, 5, 0, 0, 0, 3 },
				{ 0, 0, 3, 0, 0, 0, 0, 0, 9 }, { 6, 8, 0, 2, 0, 0, 3, 0, 0 },
				{ 7, 0, 9, 1, 0, 0, 0, 0, 8 }, { 0, 0, 5, 7, 0, 0, 0, 0, 0 },
				{ 3, 0, 0, 0, 4, 6, 2, 0, 0 }, { 8, 0, 2, 0, 0, 0, 0, 0, 0 } };

		int[][] testgrid175 = { { 0, 0, 2, 1, 0, 0, 0, 0, 3 },
				{ 0, 1, 0, 7, 0, 3, 0, 0, 0 }, { 0, 6, 0, 0, 0, 0, 5, 8, 0 },
				{ 0, 2, 0, 0, 1, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0, 9, 0, 6 },
				{ 0, 0, 0, 9, 0, 0, 0, 0, 0 }, { 7, 0, 4, 0, 0, 9, 0, 0, 0 },
				{ 0, 8, 0, 0, 0, 0, 0, 0, 0 }, { 9, 0, 0, 0, 6, 0, 4, 0, 5 } };

		int[][] testgrid17 = { { 0, 0, 5, 0, 0, 0, 0, 7, 0 },
				{ 0, 0, 9, 0, 0, 5, 8, 2, 0 }, { 0, 0, 0, 0, 0, 3, 0, 0, 0 },
				{ 0, 0, 0, 0, 0, 0, 1, 0, 0 }, { 0, 0, 0, 6, 0, 7, 0, 5, 4 },
				{ 0, 0, 0, 0, 5, 0, 0, 0, 9 }, { 4, 0, 0, 0, 0, 1, 0, 0, 0 },
				{ 0, 2, 0, 0, 8, 4, 0, 0, 0 }, { 0, 7, 0, 0, 0, 0, 0, 3, 6 } };

		solveSudoku(testgrid17);
		display(testgrid17);
	}
}