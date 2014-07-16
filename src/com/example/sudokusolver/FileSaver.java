public class FileSaver{

  private FileSaver(){
  
  }
  
	/**
	 * stores bitmap to internal storage
	 * @param image
	 */
	public static void storeImage(Bitmap image, String filename) {
		if (image == null){
			Log.d("null bitmap", "storeImage");
		}

		File pictureFile = getOutputMediaFile(filename);
		Log.d(TAG_FILENAME, pictureFile.toString());
		try {
			FileOutputStream fos = new FileOutputStream(pictureFile);
			image.compress(Bitmap.CompressFormat.PNG, 90, fos);
			fos.close();
		} catch (FileNotFoundException e) {
			Log.d(TAG_ERROR_FILESAVE, "File not found: " + e.getMessage());
		} catch (IOException e) {
			Log.d(TAG_ERROR_FILESAVE, "Error accessing file: " + e.getMessage());
		}
	}

	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(String filename) {
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.
		File mediaStorageDir = new File(
				Environment.getExternalStorageDirectory() + "/Android/data/"
						+ "com.example.sudokusolver" + "/Files");

		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				return null;
			}
		}
		// Create a media file name
		String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm")
				.format(new Date());
		File mediaFile;
		String mImageName = "MI_" +  filename + ".jpg";
		mediaFile = new File(mediaStorageDir.getPath() + File.separator
				+ mImageName);
		return mediaFile;
	}
}
