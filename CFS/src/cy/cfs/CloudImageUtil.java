package cy.cfs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.util.Log;

public class CloudImageUtil {
	
	private static final String TAG = "CloudImageUtil";

	public static Bitmap getBitmap(InputStream is, int lwidth, int lheight){
        try{
	        BitmapFactory.Options o2 = new BitmapFactory.Options();
	        Bitmap bmp = BitmapFactory.decodeStream(is, null, o2);
	        if (bmp!=null){
		        bmp = Bitmap.createScaledBitmap(bmp, lwidth, lheight, true);
	        }
	        return bmp;
		}finally{
			try {
				if (is!=null)
					is.close();
			} catch (IOException e) {
				Log.e(TAG, "close stream exception.", e);
			}
		}
	}
	
	private static InputStream getInputStream(String bgUri) {
		//from url
		int toM=2000;
		HttpURLConnection connection = null;
		try {
	        URL url = new URL(bgUri);
	        connection = (HttpURLConnection) url.openConnection();
	        connection.setDoInput(true);
	        connection.connect();
	        connection.setConnectTimeout(toM);
	        InputStream input = connection.getInputStream();
	        return input;
	    } catch (Throwable ioe) {
	    	Log.e(TAG, "connect ul error:" + bgUri, ioe);
	        return null;
	    } finally{
	    }
	}

	/*
	 * width < height for portrait mode, this is the screen width and height
	 */
	public static Bitmap getBitmap(String bgUri, int lwidth, int lheight){
		if (bgUri!=null){
			 int scale=1;
			 boolean rotate=false;
			 InputStream is = null;
			try {
				BitmapFactory.Options o = new BitmapFactory.Options();
		        o.inJustDecodeBounds = true;
		        is = getInputStream(bgUri);
		        BitmapFactory.decodeStream(is,null,o);		        
		        if (o.outWidth>o.outHeight){
		        	rotate = true;
		        	//rotate = false;
		        }
		        if (rotate){
		        	int tmp = lwidth;
		        	lwidth=lheight;
		        	lheight=tmp;
		        }
		       
		        while(o.outWidth/scale/2>=lwidth && o.outHeight/scale/2>=lheight)
		            scale*=2;
			}finally{
				try {
					if (is!=null)
						is.close();
					else
						return null;
				} catch (IOException e) {
					Log.e(TAG, "close stream exception.", e);
				}
			}
			
	        try{
		        BitmapFactory.Options o2 = new BitmapFactory.Options();
		        o2.inSampleSize=scale;
		        is = getInputStream(bgUri);
		        Bitmap bmp = BitmapFactory.decodeStream(is, null, o2);
		        if (bmp!=null){
			        bmp = Bitmap.createScaledBitmap(bmp, lwidth, lheight, true);
			        if (rotate){
			        	Matrix mat = new Matrix();
						mat.postRotate(90f);
			        	bmp = Bitmap.createBitmap(bmp, 0, 0, lwidth, lheight, mat, true);
			        }
		        }
		        return bmp;
			}finally{
				try {
					if (is!=null)
						is.close();
				} catch (IOException e) {
					Log.e(TAG, "close stream exception.", e);
				}
			}
		}else{
			return null;
		}
	}
}
