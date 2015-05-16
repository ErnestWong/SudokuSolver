SudokuSolver
============

Android application that captures image of Sudoku puzzle using camera and processes solution using image processing and OCR technology. 


#Screenshots
<img src="https://raw.githubusercontent.com/ErnestWong/SudokuSolver/master/img/sudoku-before.png"
alt="sudoku-before" width="325" height="350"/>
<img src="https://raw.githubusercontent.com/ErnestWong/SudokuSolver/master/img/sudoku-after.png"
alt="sudoku-solved" width="325" height="350"/>

#Libraries Used
* [OpenCV 2.4.9](http://opencv.org/)
* [tess-two](https://github.com/rmtheis/tess-two)—[Tesseract](https://code.google.com/p/tesseract-ocr/) fork for Android

#Future Optimizations
* <b>Sodoku solving algorithm</b>
  1. currently using greedy backtracking algorithm- could use different heuristics to improve performance of algorithm.
  

* <b>Image processing</b>
  1. <b>Noise reduction</b>: currently using connected component labelling algorithm to remove parts of the image which aren't numbers which requires a pass-through of all the pixels in the image. Could possibly use a more efficient approach to reduce image noise.
  
  2. <b>Puzzle detection</b>: currently scanning through image from center across all four directions until outer gridlines of puzzle is reached, which is really inefficient. Could use an algorithm to detect the largest rectangle/square in the image and assume that to be the puzzle.
  
  3. <b>Continuous feed detection feature</b>: currently only executes image processing when button is pressed. Could have a feature that continuously detects a sudoku puzzle and overlays the solution onto the preview.
  
  4. <b>Rotation detection</b>: currently can detect a puzzle with a few degrees of rotation. Could have a pre-processing feature that detects the angle of the rotation and rotates the image before using the image processing algorithms. 
