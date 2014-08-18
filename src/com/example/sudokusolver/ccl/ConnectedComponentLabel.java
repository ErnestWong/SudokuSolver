public class ConnectedComponentLabel{

    public ConnectedComponentLabel(){}
    
    /**
    * convert Mat representation of image to int
    **/
    public int[][] matToIntArray(Mat mat){
        int[][] image = new int[mat.rows()][mat.cols()];
        for(int i = 0; i < mat.rows(); i++){
            for(int j = 0; j < mat.cols(); j++){
                image[i][j] = mat.get(i, j)[0];
            }
        }
        return image;
    }
    
    public void blobExtract(int[][]img){
        
        UnionFind unionFind = new UnionFind();
        int currentLabel = 1;
        int[][] label = new int[img.length][img[0].length];
        
        //iterate through each element 
        for(int y = 0; y < img.length; y++){
            for(int x = 0; x < img[0].length; x++){
                //if element in background, skip
                if(img[y][x] == 0){
                    continue;
                }
                
                //if no neighbors (or unlabelled neighbors),
                //label as current label and increment current label
                //Record label to unionFind
                if(!hasNeighbors(y, x, label)){
                    label[y][x] = currentLabel;
                    UnionFind.addLabel(currentLabel);
                    currentLabel++;
                } else {
                    //find neighbor with smallest label and assign it to current
                    //element
                    int[] neighbors = getNeighborLabels(y, x, label);
                    label[y][x] = findMin(neighbors);
                    
                    //store equivalence between neighboring labels
                    int first = neighbors[0];
                    for(int i = 1; i < neighbors.length; i++){
                        if(neighbors[i] != 0){
                            unionFind.union(first, neighbors[i]);
                        }
                    }
                }
                
                
            }
        }
        
        //second pass; iterate through each element
        for(int y = 0; y < img.length; y++){
            for(int x = 0; x < img[0].length; x++){
                //if element in background, continue
                if(img[y][x] == 0){
                    continue;
                }
                //relabel the label to its root
                label[y][x] = unionFind.find(label[y][x]);
            }
        }
       
        
        List<List<Point>> regions = new ArrayList<List<Point>>(currentLabel-1);
        
        //initialize empty arrayList to each element in regions
        for(int i = 0; i < regions.size(); i++){
            regions.add(new ArrayList<Point>());
        }
        //store coordinate of pixel to corresponding label
        for(int y = 0; y < img.length; y++){
            for(int x = 0; x < img[0].length; x++){
                regions.get(label[y][x]).add(new Point(x,y));
            }
        }
    }
    
    /**
    * checks if pixel has neighboring pixels that are not background
    *@return true if contains at least one neighboring non-background pixel
    *false if no neighboring non-background pixel
    **/
    public boolean hasNeighbors(int y, int x, int[][]label){
        for(int i = y-1; i <= y+1; i++){
            for(int j = x-1; j <= x+1; j++){
                if(i == y && j == x) continue;
                
                if(label[i][j] != 0){
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
    * returns labels of neighboring pixels
    * @return array of neighbor's pixels (value is 0 if not labelled)
    **/
    public int[] getNeighborLabels(int y, int x, int[][] label){
        int[] neighbors = new int[8];
        int index = 0;
        for(int i = y-1; i <= y+1; i++){
            for(int j = x-1; j <= x+1; j++){
                if(i == y && j == x) continue;
                
                if(!outOfBounds(i, j, label) && label[i][j] != 0){
                    neighbors[index] = label[i][j];
                    index++;
                }
            }
        }
        return neighbors;
    }
    
    /**
    * determines if pixel is out of bound
    * @param x x coordinate of pixel
    * @param y y coordinate of pixel
    * @param label array representing image
    **/
    private boolean outOfBounds(int x, int y, int[][] label){
        if(x < 0 || x >= label[0].length){
            return true;
        }
        if(y < 0 || y >= label.length){
            return true;
        }
        return false;
    }
    
    /**
    * returns minimum non-zero value in array
    **/
    public int findMin(int[] list){
        int min = Integer.MAX_VALUE;
        for(int i = 0; i < list.length; i++){
            if(list[i] != 0){
                if(list[i] < min){
                    min = list[i];
                }
            }
        }
        return min;
    }
    
    
}
