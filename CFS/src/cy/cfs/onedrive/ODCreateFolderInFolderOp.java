package cy.cfs.onedrive;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.microsoft.live.LiveOperation;
import com.microsoft.live.LiveOperationException;
import com.microsoft.live.LiveOperationListener;

import android.text.TextUtils;
import android.util.Log;

import cy.cfs.CFSInstance;
import cy.cfs.DriveOp;

public class ODCreateFolderInFolderOp extends DriveOp{
	
    private static final String HOME_FOLDER = "me/skydrive";
	
	private String requestFolderName; //the full path folder name
	private String parentResourceId;
	private String folderName;
	private String parentFolderPath=null;//
	
	public ODCreateFolderInFolderOp(String requestFolderName, String parentResourceId, 
			String folderName, CFSInstance cfsIns){
		super(cfsIns, requestFolderName);
		this.requestFolderName = requestFolderName;
		this.parentResourceId = parentResourceId;
		this.folderName = folderName;
		if (parentResourceId!=null){
			this.parentFolderPath = requestFolderName.substring(0, requestFolderName.lastIndexOf(folderName));
		}
	}
	
	public String toString(){
		String str = super.toString();
		return str + ", requestFolderName:" + requestFolderName
				+ ",parentResourceId:" + parentResourceId
				+ ",folderName:" + folderName
				+ ",parentFolderPath:" + parentFolderPath;
	}
	
	@Override
	public void run(){
		String path = "";
		if (TextUtils.isEmpty(parentResourceId)){
			path = HOME_FOLDER + "/files";
		}else{
			path = parentResourceId + "/files";
		}
		
		Log.i(TAG, String.format("try to create %s in create fif op.", requestFolderName));

		((ODCFSInstance)getCfsInst()).getClient().getAsync(path, getFolderCallback);
	}
	
	final LiveOperationListener getFolderCallback = new LiveOperationListener() {
        public void onError(LiveOperationException exception, LiveOperation operation) {
           Log.e(TAG, "Error creating folder: " + exception.getMessage());
           finalCallback(false, false, requestFolderName, exception.getMessage());
        }
        
        public void onComplete(LiveOperation operation) {
        	JSONObject result = operation.getResult();
            if (result.has(JsonKeys.ERROR)) {
                JSONObject error = result.optJSONObject(JsonKeys.ERROR);
                String message = error.optString(JsonKeys.MESSAGE);
                String code = error.optString(JsonKeys.CODE);
                Log.e(TAG, String.format("%s, msg:%s, error:%s", requestFolderName, message, code));
            } else {
            	JSONArray items;
            	boolean found = false;
            	String resourceId=null;
				try {
					items = result.getJSONArray(JsonKeys.DATA);
	            	for (int i=0; i<items.length(); i++){
	            		JSONObject obj = (JSONObject) items.get(i);
	                    String name = obj.getString(JsonKeys.NAME);
	                    if (folderName.equals(name)){
	                    	found=true;
	                    	resourceId = obj.getString(JsonKeys.ID);
	                    }else{
	                    	//update the cache
	                    	if (parentFolderPath!=null){
	                    		sysCallback(true, false, parentFolderPath+name, obj.getString(JsonKeys.ID));
	                    	}
	                    }
	                }
				} catch (JSONException e) {
					Log.e(TAG, "", e);
				}
				if (!found){
					createFolder();
				}else{
					Log.i(TAG, String.format("Folder %s exists with id %s", requestFolderName, resourceId));
					finalCallback(true, false, requestFolderName, resourceId);
				}
            }
       }
   };
   
	private void createFolder(){
		JSONObject body = new JSONObject();
        try {
			body.put(JsonKeys.NAME, folderName);
			body.put(JsonKeys.DESCRIPTION, folderName);
		} catch (JSONException e) {
			Log.e(TAG, "", e);
		}
        if (TextUtils.isEmpty(parentResourceId)){
        	parentResourceId=HOME_FOLDER;
        }
        ((ODCFSInstance)getCfsInst()).getClient().postAsync(parentResourceId, body, createFolderCallback);
	}
	
	final LiveOperationListener createFolderCallback = new LiveOperationListener() {
        public void onError(LiveOperationException exception, LiveOperation operation) {
           Log.e(TAG, "Error creating folder: " + exception.getMessage());
           finalCallback(false, false, requestFolderName, exception.getMessage());
        }
        
        public void onComplete(LiveOperation operation) {
        	JSONObject result = operation.getResult();
            if (result.has(JsonKeys.ERROR)) {
                JSONObject error = result.optJSONObject(JsonKeys.ERROR);
                String message = error.optString(JsonKeys.MESSAGE);
                String code = error.optString(JsonKeys.CODE);
                Log.e(TAG, code + ":" + message);
                finalCallback(false, false, requestFolderName, error.toString());
            } else {
            	String resourceId = result.optString("id");
            	Log.i(TAG, String.format("Create folder %s with id %s", requestFolderName, resourceId));
	            finalCallback(true, false, requestFolderName, resourceId);
            }
       }
   };
   
   @Override
   public boolean equals(Object o){
   	if (o instanceof ODCreateFolderInFolderOp){
   		ODCreateFolderInFolderOp adOp = (ODCreateFolderInFolderOp)o;
   		if (this.requestFolderName.equals(adOp.requestFolderName) &&
   				this.parentResourceId.equals(adOp.parentResourceId)&&
   				this.folderName.equals(adOp.folderName)){
				return true;
			}else{
				return false;
			}
   	}else{
   		return false;
   	}
   }
}
