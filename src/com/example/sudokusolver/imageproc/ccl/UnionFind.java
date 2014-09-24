package com.example.sudokusolver.imageproc.ccl;

import java.util.HashMap;
import java.util.Map;

public class UnionFind {

	private Map<Integer, Integer> parent;
	private Map<Integer, Integer> size;

	public UnionFind() {
		/*
		 * parent = new int[rows*cols]; size = new int[rows*cols]; for(int i =
		 * 0; i < row*cols; i++){ parent[i] = i; size[i] = i; }
		 */
		// parent = new ArrayList<Integer>();
		// size = new ArrayList<Integer>();

		parent = new HashMap<Integer, Integer>();
		size = new HashMap<Integer, Integer>();
	}

	public int find(int n) {
		if (parent.get(n) == n) {
			return n;
		} else {
			parent.put(n, find(parent.get(n)));
			return parent.get(n);
		}
	}

	public void union(int i, int j) {
		i = find(i);
		j = find(j);

		if (i == j)
			return;

		if (size.get(i) < size.get(j)) {
			parent.put(i, j);
			size.put(j, size.get(j) + size.get(i));
		} else {
			parent.put(j, i);
			size.put(i, size.get(i) + size.get(j));
		}
	}

	public void addLabel(int i) {
		parent.put(i, i); // make sure intial key == value
		size.put(i, 1); // initial size == 1
	}
}
