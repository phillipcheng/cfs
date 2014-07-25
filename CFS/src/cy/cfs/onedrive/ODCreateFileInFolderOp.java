package cy.cfs.onedrive;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;

import com.microsoft.live.LiveOperation;
import com.microsoft.live.LiveOperationException;
import com.microsoft.live.LiveOperationListener;
import com.microsoft.live.LiveUploadOperationListener;

import cy.cfs.CFSInstance;
import cy.cfs.CallbackOp;
import cy.cfs.DriveOp;

public class ODCreateFileInFolderOp extends DriveOp{
	
	protected static final String TAG = "ODCreateFileInFolderOp";
	
	private String requestFileName;
	private String folderResourceId;
	private String fileName;
	private String mimeType;
	private String parentFolderPath;
	private byte[] binary;
	
	public ODCreateFileInFolderOp(String requestFileName, String folderResourceId, 
			String fileName, String mimeType, byte[] binary, CFSInstance cfsIns){
		super(cfsIns);
		this.requestFileName = requestFileName;
		this.folderResourceId = folderResourceId;
		this.fileName = fileName;
		this.mimeType = mimeType;
		this.binary = binary;
		this.parentFolderPath = requestFileName.substring(0, requestFileName.lastIndexOf(fileName));
		
	}
	
	@Override
	public void myRun(){
		String path = folderResourceId + "/files";
		((ODCFSInstance)getCfsInst()).getClient().getAsync(path, getFileCallback);
	}
	
	final LiveOperationListener getFileCallback = new LiveOperationListener() {
        public void onError(LiveOperationException exception, LiveOperation operation) {
           Log.e(TAG, "Error creating folder: " + exception.getMessage());
           finalCallback(false, true, requestFileName, exception.getMessage());
        }
        
        public void onComplete(LiveOperation operation) {
        	JSONObject result = operation.getResult();
            if (result.has(JsonKeys.ERROR)) {
                JSONObject error = result.optJSONObject(JsonKeys.ERROR);
                String message = error.optString(JsonKeys.MESSAGE);
                String code = error.optString(JsonKeys.CODE);
                Log.e(TAG, String.format("%s, msg:%s, error:%s", requestFileName, message, code));
            } else {
            	JSONArray items;
            	boolean found = false;
            	String resourceId=null;
				try {
					items = result.getJSONArray(JsonKeys.DATA);
	            	for (int i=0; i<items.length(); i++){
	            		JSONObject obj = (JSONObject) items.get(i);
	                    String name = obj.getString(JsonKeys.NAME);
	                    if (fileName.equals(name)){
	                    	found=true;
	                    	resourceId = obj.getString(JsonKeys.ID);
	                    }else{
	                    	//update the cache
	                    	cfsCallback(true, true, parentFolderPath+name, obj.getString(JsonKeys.ID));
	                    }
	                }
				} catch (JSONException e) {
					Log.e(TAG, "", e);
				}
				if (!found){
					uploadFile();
				}else{
					Log.i(TAG, String.format("Folder %s exists with id %s", requestFileName, resourceId));
					finalCallback(true, true, requestFileName, resourceId);
				}
            }
       }
   };
	
	private void uploadFile(){
		final InputStream is = new ByteArrayInputStream(binary);
		((ODCFSInstance)getCfsInst()).getClient().uploadAsync(folderResourceId, fileName, is, new LiveUploadOperationListener() {
			@Override
            public void onUploadFailed(LiveOperationException exception, LiveOperation operation) {
                Log.e(TAG, "Error uploading file: " + exception.getMessage());
                finalCallback(false, true, requestFileName, exception.getMessage());
            }
            public void onUploadCompleted(LiveOperation operation) {
            	JSONObject result = operation.getResult();
            	try {
                    is.close();
                }    
                catch(IOException ioe) {
                    Log.e(TAG, "Error closing is: " + ioe.getMessage());
                }
            	
            	if (result.has(JsonKeys.ERROR)) {
                    JSONObject error = result.optJSONObject(JsonKeys.ERROR);
                    String message = error.optString(JsonKeys.MESSAGE);
                    String code = error.optString(JsonKeys.CODE);
                    Log.e(TAG, code + ":" + message);
                    finalCallback(false, true, requestFileName, error.toString());
                }else{
                	String resourceId = result.optString("id");
	                Log.i(TAG, String.format("Create file %s with id %s", requestFileName, resourceId));
	                finalCallback(true, true, requestFileName, resourceId);
                }
            }
            public void onUploadProgress(int totalBytes, int bytesRemaining, LiveOperation operation) {
            }
        });
	}
}
