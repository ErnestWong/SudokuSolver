public class ImgManipulation{

    public ImgManipulation(){
        
    }
    
    private Mat rotateImage(Mat src, float angle, float dist){
        Point centre = new Point(src.cols()/2, src.rows()/2);
        Mat matrix = ImgProc.getRotationMatrix2D(centre, angle, 1.0);
        Mat result = new Mat();
        ImgProc.warpAffine(src, result, matrix, src.size());
    }
    
    public Mat bitmapToMat(Bitmap bmp){
        mat = new Mat(bmp.getHeight(), bmp.getWidth(), CvType.CV_8UC1);
		Utils.bitmapToMat(bmp, mat);
        return mat;
    }
    
    public Bitmap matToBitmap(Mat mat){
        Bitmap bmp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bmp);
        return bmp;
    }
    
    public Mat doRotation(Mat mat){
        Mat lines = new Mat();
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY);
	    Imgproc.adaptiveThreshold(mat, mat, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);
        Imgproc.Canny(mat, mat, 100, 200); //min threshold: max threshold should be (1:2 or 1:3)
	    Imgproc.HoughLine(mat, lines, 1, Math.PI/180, 200); //may change threshold
        
        for(int i = 0; i < lines.col(); i++){
            boolean leftSkew;
            float rho = lines.get(i, 0);
            float theta = lines.get(i, 1);
                    
            //leftskew if theta  > 90
            if(theta > 0 && theta < 45) {
                mat = rotateImage(theta, rho);
                break;
            }
            else if (theta > 315 && theta < 360){
                mat = rotateImage(theta, rho);
                break;
            }
            
            Log.d("angles", i + ": " + theta);
        }
        return mat;
    }
    
    /**
    * returns true if there are black pixels along the line;
    * otherwise returns false
    **/
    private boolean isBlackLine(Bitmap bmp, int line, boolean isRow){
        int lenient = 0;
        int bound;
        //determine bound depending on row or column
        if(isRow) bound = bmp.getWidth();
        else bound = bmp.getHeight();
        //check every 3 pixels on the line
        for(int i = 0; i < bound; i += 3){
            int pixelColor;
            if(isRow) pixelColor = bmp.getPixel(line, i);
            else pixelColor = bmp.getPixel(i, line);
            //if more than 10 pixels is black, return true
            if(pixelColor == Color.BLACK){
                if(lenient > 10) return true;
                lenient++;
            }
        }
        return false;
    }
    
    /**
    *finds the boundaries of the sudoku grid (top, bottom, left, right)
    **/
    private int findBoundary(Bitmap bmp,boolean isRow, boolean firstLine){
        int bound;
        
        //if finding rows (top/bottom), then bound is height
        //if finding columns (left/right), then bound is width
        if(isRow) bound = bmp.getHeight();
        else bound = bmp.getWidth();
        
        //check every 3 lines
        for(int i = 0; i < bound - 2; i += 3){
            //if finding the upper/left boundaries, check if 
            //white line followed black line
            if(firstLine){
                if(!isBlackLine(bmp, i, isRow) && isBlackLine(bmp, i+1, isRow)){
                    return i;
                }
            }
            //if finding lower/right boundaries, check if 
            //black line followed by white line
            else{
                if(isBlackLine(bmp, i, isRow) && !isBlackLine(bmp, i+1, isRow)){
                    return i;
                }
            }
        }
        return -1;
    }
    public Mat findSubGrid(Mat mat){
        Bitmap bitmap = matToBitmap(mat);
        
        //first param: top+bottom isRow
        //2nd param: top and left are firstLines
        int top = findBoundary(Bitmap bitmap,true, true);
        int bottom = findBoundary(Bitmap bitmap,true, false);
        int left = findBoundary(Bitmap bitmap,false, true);
        int right = findBoundary(Bitmap bitmap,false, false);
        
        int width = right - left;
        int height = bottom - top;
        
        //extract sub-bitmap from bitmap
        Bitmap subBmp = Bitmap.createBitmap(left, top, width, height);
        return bitmapToMat(subBmp);
    }
    
    public void findNums(Bitmap bmp){
        Mat mat = bitmapToMat(bmp);
        mat = doRotation(mat);
        mat = findSubGrid(mat);
        
        //set up tesseract here
    }
}
