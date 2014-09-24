package com.example.sudokusolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class SudokuSolver {
	private static int[][] grid = new int[9][9];

	/**
	 * checks if value at the cell is valid
	 **/
	private static boolean isValid(int row, int col, int num, int[][] grid) {
		if (!containsRow(grid[row], num) && !containsCol(col, num, grid)
				&& !containsSq(row, col, num, grid))
			return true;
		else
			return false;
	}

	/**
	 * returns true if there is a row conflict
	 **/
	private static boolean containsRow(int[] row, int n) {
		for (int i = 0; i < row.length; i++) {
			if (row[i] == n)
				return true;
		}
		return false;
	}

	/**
	 * returns true if there is a column conflict
	 **/
	private static boolean containsCol(int col, int n, int[][] grid) {
		for (int i = 0; i < 9; i++) {
			if (grid[i][col] == n)
				return true;
		}
		return false;
	}

	/**
	 * returns true if there is a box(square) conflict
	 **/
	private static boolean containsSq(int row, int col, int n, int[][] grid) {
		// rounds down to the nearest 3
		int rowinc = row - row % 3;
		int colinc = col - col % 3;
		// check if box contains the number
		for (int i = rowinc; i < rowinc + 3; i++) {
			for (int j = colinc; j < colinc + 3; j++) {
				if (grid[i][j] == n)
					return true;
			}
		}
		return false;
	}

	public static void display(int[][] grid) {
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

	/**
	 * returns first empty cell's row
	 **/
	private static int findRow(int[][] grid) {
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				if (grid[i][j] == 0)
					return i;
			}
		}
		return -1;
	}

	/**
	 * returns first empty cell's column
	 **/
	private static int findCol(int[][] grid) {
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				if (grid[i][j] == 0)
					return j;
			}
		}
		return -1;
	}

	/**
	 * populates list of valid numbers (1-9) and returns in random permutation
	 **/
	private static List<Integer> populateList() {
		List<Integer> rand = new ArrayList<Integer>(9);
		for (int i = 1; i <= 9; i++) {
			rand.add(i);
		}
		Collections.shuffle(rand);

		return rand;
	}

	public static int[][] solve(int[][] grid) {
		int[][] copy = clone(grid);
		solve(SudokuSolver.emptyRow(copy), SudokuSolver.emptyCol(copy), copy);
		return copy;
	}

	/**
	 * recursive method to fill in the sudoku grid
	 **/
	private static boolean fill(int row, int col, int[][] grid) {

		if (row == 9)
			return true;

		// find next empty cell
		row = findRow(grid);
		col = findCol(grid);
		List<Integer> avail = populateList();

		while (avail.size() > 0) {
			// loop through possible values
			int n = avail.get(0);
			avail.remove(0);

			// if value is valid, then check next cell
			if (isValid(row, col, n, grid)) {
				grid[row][col] = n;

				int tmprow;
				if (col == 8)
					tmprow = row + 1;
				else
					tmprow = col;

				// recursively check next cell; if return false then clear
				// cell and move back; terminates when all cells are full
				if (fill(tmprow, (col + 1) % 9, grid))
					return true;

			}
			// clear cell
			grid[row][col] = 0;

		}
		return false;

	}

	/**
	 * non-recursive implementation of fill method
	 **/
	private static void fill2() {
		boolean done = false;
		int row = 0;
		int col = 0;
		boolean backtrack = false;
		int n;
		while (!done) {
			if (row == 9)
				break;
			if (backtrack) {
				if (col == 0)
					row--;
				col = (col - 1) % 9;
				n = grid[row][col] + 1;
				grid[row][col] = 0;
				backtrack = false;
			} else
				n = 1;
			boolean found = false;
			while (n <= 9 && !found) {
				if (isValid(row, col, n, grid)) {
					grid[row][col] = n;
					found = true;
				} else
					n++;
			}

			if (!found) {
				backtrack = true;
				grid[row][col] = 0;
			} else {
				if (col == 8)
					row++;
				col = (col + 1) % 9;
			}

			if (row == 9)
				done = true;
		}
	}

	private static int emptyCol(int[][] grid) {
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				if (grid[i][j] == 0) {
					return j;
				}
			}
		}
		return -1;
	}

	private static int emptyRow(int[][] grid) {
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				if (grid[i][j] == 0) {
					return i;
				}
			}
		}
		return -1;
	}

	static boolean solve(int i, int j, int[][] cells) {
		if (i == 9) {
			i = 0;
			if (++j == 9)
				return true;
		}
		if (cells[i][j] != 0) // skip filled cells
			return solve(i + 1, j, cells);

		for (int val = 1; val <= 9; ++val) {
			if (legal(i, j, val, cells)) {
				cells[i][j] = val;
				if (solve(i + 1, j, cells))
					return true;
			}
		}
		cells[i][j] = 0; // reset on backtrack
		return false;
	}

	static boolean legal(int i, int j, int val, int[][] cells) {
		for (int k = 0; k < 9; ++k)
			// row
			if (val == cells[k][j])
				return false;

		for (int k = 0; k < 9; ++k)
			// col
			if (val == cells[i][k])
				return false;

		int boxRowOffset = (i / 3) * 3;
		int boxColOffset = (j / 3) * 3;
		for (int k = 0; k < 3; ++k)
			// box
			for (int m = 0; m < 3; ++m)
				if (val == cells[boxRowOffset + k][boxColOffset + m])
					return false;

		return true; // no violations, so it's legal
	}
	
	public static void main(String[] args) {
		int[][] grid = { { 0, 0, 3, 1, 0, 0, 7, 0, 0 },
				{ 5, 6, 0, 0, 8, 7, 0, 0, 0 }, { 8, 0, 0, 5, 0, 0, 0, 6, 0 },
				{ 9, 0, 0, 0, 6, 0, 0, 0, 1 }, { 0, 0, 4, 0, 0, 0, 3, 0, 0 },
				{ 1, 0, 0, 0, 9, 0, 0, 0, 2 }, { 0, 8, 0, 0, 0, 3, 0, 0, 5 },
				{ 0, 0, 0, 7, 1, 0, 0, 3, 9 }, { 0, 0, 9, 0, 0, 2, 1, 0, 0 }, };

		int[][] grid2 = { { 0, 0, 0, 2, 6, 0, 7, 0, 1 },
				{ 6, 8, 0, 0, 7, 0, 0, 9, 0 }, { 1, 9, 0, 0, 0, 4, 5, 0, 0 },
				{ 8, 2, 0, 1, 0, 0, 0, 4, 0 }, { 0, 0, 4, 6, 0, 2, 9, 0, 0 },
				{ 0, 5, 0, 0, 0, 3, 0, 2, 8 }, { 0, 0, 9, 3, 0, 0, 0, 7, 4 },
				{ 0, 4, 0, 0, 5, 0, 0, 3, 6 }, { 7, 0, 3, 0, 1, 8, 0, 0, 0 }, };

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
		// fill(0,0, grid2);
		display(grid2);
		display(clone(grid2));

	}

	private static int[][] clone(int[][] array) {
		int[][] clone = new int[array.length][array[0].length];
		for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < array[0].length; j++) {
				clone[i][j] = array[i][j];
			}
		}
		return clone;
	}
}