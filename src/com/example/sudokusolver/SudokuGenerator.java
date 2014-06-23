package com.example.sudokusolver;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;


public class SudokuGenerator{
static int[][] grid = new int[9][9];
    
    /**
    *checks if value at the cell is valid
    **/
    static boolean isValid(int row, int col, int num){
        if(!containsRow(grid[row], num) && !containsCol(col, num) && !containsSq(row, col, num)) 
            return true;
        else return false;
    }
    
    /**
    *returns true if there is a row conflict
    **/
    static boolean containsRow(int[] row, int n){
        for(int i = 0; i < row.length; i++){
            if(row[i] == n) return true;
        }
        return false;
    }
    
    /**
    *returns true if there is a column conflict
    **/
    static boolean containsCol(int col, int n){
        for(int i = 0; i < 9; i++){
            if(grid[i][col] == n) return true;
        }
        return false;
    }
    
    /**
    *returns true if there is a box(square) conflict
    **/
    static boolean containsSq(int row, int col, int n){   
        //rounds down to the nearest 3 
    	int rowinc = row - row % 3;
    	int colinc = col - col % 3;
        //check if box contains the number
        for(int i = rowinc; i < rowinc + 3; i++){
            for(int j = colinc; j < colinc + 3; j++){
                if(grid[i][j] == n) return true;
            }
        }
        return false;
    }
    

    
    static void display(){
	System.out.println("-------------------------");
        for(int i = 0; i < 9; i++){
            for(int j = 0; j < 9; j++){
		if(j == 0) System.out.print("| ");
                System.out.print(grid[i][j] + " ");
		if((j + 1)%3 == 0) System.out.print("| ");
            }
            System.out.println();
	    if((i + 1)%3 == 0){
	        System.out.println("-------------------------");
	    }
        }
    }
    
    /**
    *returns first empty cell's row
    **/
    static int findRow(){
    	for(int i = 0; i < 9; i++){
    		for(int j = 0; j < 9; j++){
    			if(grid[i][j] == 0) return i;
    		}
    	}
    	return -1;
    }
    
    /**
    *returns first empty cell's column
    **/
    static int findCol(){
    	for(int i = 0; i < 9; i++){
    		for(int j = 0; j < 9; j++){
    			if(grid[i][j] == 0) return j;
    		}
    	}
    	return -1;
    }
    
    /**
    *populates list of valid numbers (1-9) and returns in random permutation
    **/ 
    static List<Integer> populateList(){
        List<Integer> rand = new ArrayList<Integer>(9);
        for(int i = 1; i <= 9; i++){
            rand.add(i);
        }
        Collections.shuffle(rand);
        
        return rand;
    }
    
    /**
    *recursive method to fill in the sudoku grid
    **/
    static boolean fill(int row, int col){
        
        if(row == 9) return true;
        
        //find next empty cell
        row = findRow();
        col = findCol();
        List<Integer>avail = populateList();
        
        while(avail.size() > 0){
            //loop through possible values
            int n = avail.get(0);
            avail.remove(0);
        
            //if value is valid, then check next cell
            if(isValid(row, col, n)){
                grid[row][col] = n;
            
                int tmprow;
                if(col == 8) tmprow = row + 1;
                else tmprow = col;
                
                //recursively check next cell; if return false then clear 
                //cell and move back; terminates when all cells are full
                if(fill(tmprow, (col + 1) % 9)) return true;
                
               
            }
            //clear cell
            grid[row][col] = 0;	 
            
        }
        return false;
        
    }
    
    
    /**
    *non-recursive implementation of fill method
    **/
    static void fill2(){
    	boolean done = false;
    	int row = 0;
    	int col = 0;
    	boolean backtrack = false;
    	int n;
    	while(!done){
		if(row == 9) break;
    		if(backtrack){
    			if(col == 0) row--;
    			col = (col - 1)%9;
    			n = grid[row][col] + 1;
    			grid[row][col] = 0;
    			backtrack = false;
    		}
    		else n = 1;
    		boolean found = false;
    		while(n <= 9 && !found){
    			if(isValid(row, col, n)){
    				grid[row][col] = n;
    				found = true;
    			}
    			else n++;
    		}

    		if(!found){
    			backtrack = true;
    			grid[row][col] = 0;
    		}
    		else{
    			if(col == 8) row++;
    			col = (col + 1)%9;
    		}

    		if(row == 9) done = true;
    	}
    }
    public static void main(String[] args){
    	fill(0,0);
        display();
       
    }
}