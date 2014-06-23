public class ImgManipulation{

    public ImgManipulation(){
        
    }
    
    Mat rotateImage(Mat src, float angle, float dist){
        Point centre = new Point(src.cols()/2, src.rows()/2);
        Mat matrix = ImgProc.getRotationMatrix2D(centre, angle, 1.0);
        Mat result = new Mat();
        ImgProc.warpAffine(src, result, matrix, src.size());
    }
    
    Mat bitmapToMat(Bitmap bmp){
        mat = new Mat(bmp.getHeight(), bmp.getWidth(), CvType.CV_8UC1);
		Utils.bitmapToMat(bmp, mat);
        return mat;
    }
    
    Mat doRotation(Mat mat){
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
    
}
