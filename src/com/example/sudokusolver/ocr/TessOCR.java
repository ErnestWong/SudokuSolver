package com.example.sudokusolver.ocr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

/**
 * Handles OCR portion of application-- uses tess-two API to recognize digits
 * 
 * @author E Wong
 * 
 */
public class TessOCR {

	private TessBaseAPI tessAPI;
	private boolean isInit = false;
	private boolean isEnded = false;

	public final String TRAINED_DATA_DIRECTORY = "tessdata/";
	public final String TRAINED_DATA_FILENAME = "eng.traineddata";
	private String DATA_PATH;
	public static final String TAG_DIR_CREATE_SUCCESS = "directory created success";
	public static final String TAG_DIR_CREATE_FAIL = "directory failed create";

	/**
	 * constructor to obtain context+bitmap and initializes DATA_PATH needed for
	 * class methods
	 **/
	public TessOCR(Context context) {
		DATA_PATH = Environment.getExternalStorageDirectory()
				+ "/Android/data/" + context.getPackageName() + "/Files/";
	}

	/**
	 * initializes OCR-- copies traineddata file from assets to external storage
	 * (which is required by tess-two API) and accesses tess API
	 **/
	public void initOCR() {
		tessAPI = new TessBaseAPI();
		// copyTessFileToStorage();

		// datapath is in parent directory of tessdata
		tessAPI.init(DATA_PATH, "eng");
		tessAPI.setVariable("tessedit_char_whitelist", "123456789");
		isInit = true;

	}

	public boolean isInit() {
		return isInit;
	}

	public boolean isEnded() {
		return isEnded;
	}

	public String doOCR(Bitmap bmp) {
		tessAPI.setImage(bmp);
		String result = tessAPI.getUTF8Text();
		return result;
	}

	public String doOCR(byte[][] byteArray) {
		Log.d("doing OCR", "byte array");
		byte[] stream = convertToByteStream(byteArray);
		tessAPI.setImage(stream, byteArray[0].length, byteArray.length, 1, 1);
		String result = tessAPI.getUTF8Text();
		return result;
	}

	public void endTessOCR() {
		tessAPI.end();
		isEnded = true;
	}

	public static byte[] convertToByteStream(byte[][] byteArray) {
		int index = 0;
		byte[] stream = new byte[byteArray.length * byteArray[0].length];
		for (int i = 0; i < byteArray.length; i++) {
			for (int j = 0; j < byteArray[0].length; j++) {
				stream[index] = byteArray[i][j];
				index++;
			}
		}
		return stream;
	}
}
