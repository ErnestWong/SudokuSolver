package com.example.sudokusolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;

import org.opencv.core.Point;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.Log;

public class BlobExtractv2 {

	private Bitmap fixedBmp;
	private int tileWidth;
	private int tileHeight;
	private static final int BUFFER = 0;
    private static final int GAP;
	private Queue<Rect> tileRects = new LinkedList<Rect>();

	public BlobExtractv2(Bitmap bmp){
		fixedBmp = bmp;
		tileWidth = fixedBmp.getWidth()/9;
		tileHeight = fixedBmp.getHeight()/9;
        GAP = fixedBmp.getWidth()/10;
	}

	public void blobExtract(){
		Log.d("extract", "extracting");
		int count = 0;
		int numcount = 0;
		for(int y = 1; y < fixedBmp.getHeight()-1; y++){
			for(int x = 1; x < fixedBmp.getWidth()-1; x++){
				if(fixedBmp.getPixel(x,y) == Color.BLACK){
					continue;
				}

				Rect r = floodfill(new Point(x,y));
				if(r != null){
					numcount++;
					tileRects.add(r);
				}

				count++;



			}
		}
		Log.d("number of blobs", count + "," + numcount);
	}

	private Rect floodfill(Point start){
		//keeps track of checked pixels
		boolean[][] checked = new boolean[fixedBmp.getWidth()][fixedBmp.getHeight()];
		List<Integer> xCoords = new ArrayList<Integer>();
		List<Integer> yCoords = new ArrayList<Integer>();
		//queue of points to store the pixels; add initial pixel
		Queue<Point> q = new LinkedList<Point>();
		q.add(start);

		//remove pixel and check adjacent pixels until queue is empty
		while(!q.isEmpty()){
			Point p = q.remove();

			if(!checked[(int)p.x][(int)p.y]){
				if(fixedBmp.getPixel((int)p.x, (int)p.y) == Color.WHITE && !outOfBounds(p)){
					xCoords.add((int)p.x);
					yCoords.add((int)p.y);

					fixedBmp.setPixel((int)p.x, (int)p.y, Color.BLACK);
					q.add(new Point(p.x-1, p.y-1));
					q.add(new Point(p.x-1, p.y));
					q.add(new Point(p.x-1, p.y+1));
					q.add(new Point(p.x, p.y-1));
					q.add(new Point(p.x, p.y+1));
					q.add(new Point(p.x+1, p.y-1));
					q.add(new Point(p.x+1, p.y));
					q.add(new Point(p.x+1, p.y+1));
				}
			}
			checked[(int)p.x][(int)p.y] = true;
		}

		return isNumber(xCoords, yCoords);
	}

	/**
	 * check if pixel is out of bounds from bitmap
	 * @param point-- target pixel
	 * @return
	 */
	private boolean outOfBounds(Point point){
		if(point.x >= fixedBmp.getWidth()-2 || point.y >= fixedBmp.getHeight()-2 || point.x <= 0 || point.y <= 0){
			return true;
		} else {
			return false;
		}
	}

    private Queue<Rect> sortRects(List<Rect> rects){
        List <Rect> tmp = cloneRect(rects);
        Queue<Rect> sorted = new LinkedList<Rect>(tmp.size());
        while(!tmp.isEmpty()){
            Rect min = tmp.get(0);
            for(int i = 0; i < tmp.size(); i++){
                if(compareRect(min, tmp.get(i))){
                    min = tmp.get(i);
                }
            }
            tmp.remove(min);
            sorted.add(min);
        }
    }
    
    public Queue<Rect> getTileRects(){
        return sortRects(tileRects);
    }
    
    private List<Rect> cloneRect(List<Rect> original){
        List<Rect> tmpRect = new ArrayList<Rect>(rects.size());
        for(int i = 0; i < rects.size(); i++){
            tmpRect.add(rects.get(i));
        }
        return tmpRect;
    }
    
    //returns true if r1 > r2, false if r1 < r2
    private boolean compareRect(Rect r1, Rect r2){
        int n1 = r1.centerY();
        int n2 = r2.centerY();
        if(n1 > n2+GAP){
            return true;
        }
        else if(n2 > n1+GAP){
            return false;
        }
        else{
            if(r1.centerX() > r2.centreX()){
                return true;
            }
            else{
                return false;
            }
        }
    
    }
    
	private Rect isNumber(List<Integer> xCoords, List<Integer> yCoords){

		if(xCoords.size() == 0 || yCoords.size() == 0){
			return null;
		}
		Collections.sort(xCoords);
		Collections.sort(yCoords);
		int width = xCoords.get(xCoords.size()-1) - xCoords.get(0) + BUFFER;
		int height = yCoords.get(yCoords.size()-1) - yCoords.get(0) + BUFFER;
		int x = xCoords.get(0) - BUFFER;
		int y = yCoords.get(0) - BUFFER;

		if(width > tileWidth || height > tileHeight){
			return null;
		}

		if(width > height){
			return null;
		}

		if(height < tileHeight / 3 || width < tileWidth / 5){
			return null;
		}

		return new Rect(x, y, x+width, y+height);
	}
}
