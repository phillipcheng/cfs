package cy.cfs.onedrive;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.microsoft.live.LiveOperation;
import com.microsoft.live.LiveOperationException;
import com.microsoft.live.LiveOperationListener;

import cy.cfs.CFSInstance;
import cy.cfs.CloudImageUtil;
import cy.cfs.DriveOp;

public class ODGetFileInFolderOp extends DriveOp{
	
	
	private String requestFileName;
	private String folderResourceId;
	private String fileName;
	private String parentFolderPath;
	private String fileResourceId;
	private int width;
	private int height;
	
	public String toString(){
		String str = super.toString();
		return str + ", requestFileName:" + requestFileName
				+ ",folderResourceId:" + folderResourceId
				+ ",fileName:" + fileName
				+ ",parentFolderPath:" + parentFolderPath;
	}
	
	public ODGetFileInFolderOp(String requestFileName, String folderResourceId, 
			String fileName, String fileResourceId, CFSInstance cfsIns, Object req, int width, int height){
		super(cfsIns, req);
		this.requestFileName = requestFileName;
		this.folderResourceId = folderResourceId;
		this.fileName = fileName;
		this.fileResourceId = fileResourceId;
		this.parentFolderPath = requestFileName.substring(0, requestFileName.lastIndexOf(fileName));
		this.width = width;
		this.height = height;
	}
	
	@Override
	public void run(){
		if (fileResourceId==null){
			String path = folderResourceId + "/files";
			((ODCFSInstance)getCfsInst()).getClient().getAsync(path, getFileCallback);
		}else{
			((ODCFSInstance)getCfsInst()).getClient().getAsync(fileResourceId, getFileCallback);
		}
	}
	
	
	final LiveOperationListener getFileCallback = new LiveOperationListener() {
        public void onError(LiveOperationException exception, LiveOperation operation) {
           Log.e(TAG, "Error getting folder: " + exception.getMessage());
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
            	String picSource=null;
            	boolean hasError=false;
				try {
					if (fileResourceId == null){
						//get files under directory 
						if (result.has(JsonKeys.DATA)){
							items = result.getJSONArray(JsonKeys.DATA);
			            	for (int i=0; i<items.length(); i++){
			            		JSONObject obj = (JSONObject) items.get(i);
			                    String name = obj.getString(JsonKeys.NAME);
			                    Log.i(TAG, String.format("in folder %s found %s", parentFolderPath, name));
			                    if (fileName.equals(name)){
			                    	found=true;
			                    	resourceId = obj.getString(JsonKeys.ID);
			                    	picSource = obj.getString(JsonKeys.SOURCE);
			                    }else{
			                    	//TODO can be folder, update the cache
			                    	sysCallback(true, true, parentFolderPath+name, obj.getString(JsonKeys.ID));
			                    }
			                }
						}else{
							hasError=true;
							Log.e(TAG, String.format("no data returned for request resourceId:%s for file %s", 
									fileResourceId, requestFileName));
						}
					}else{
						//get a file directly
						if (fileName.equals(result.getString(JsonKeys.NAME))){
	                    	found=true;
	                    	resourceId = result.getString(JsonKeys.ID);
	                    	picSource = result.getString(JsonKeys.SOURCE);
	                    }else{
	                    	hasError=true;
							Log.e(TAG, String.format("data returned %s for id:%s, name:%s does not have name.", 
									result.toString(), fileResourceId, requestFileName));
	                    }
					}
				} catch (JSONException e) {
					Log.e(TAG, "", e);
				}
				if (!hasError){
					if (!found){
						sysCallback(true, true, requestFileName, EMPTY_MARK);
		            	usrCallback(false, null);
					}else{
						sysCallback(true, true, requestFileName, resourceId);
						download(picSource);
					}
				}else{
					sysCallback(false, true, requestFileName, null);
					//no usr callback
				}
            }
       }
   };
   
   private void download(String picSource){
	   Log.i(TAG, "download: " + picSource + " for:" + requestFileName);
	   (new AsyncTask<String, Void, Bitmap>(){
		@Override
		protected Bitmap doInBackground(String... params) {
			return CloudImageUtil.getBitmap(params[0], width, height);
		}
		
		@Override
        protected void onPostExecute(Bitmap result) {
			if (result!=null){
				usrCallback(true, result);
			}else{
				usrCallback(false, result);
			}
        }
	   }).execute(picSource);
   }
	 
    @Override
    public boolean equals(Object o){
    	if (o instanceof ODGetFileInFolderOp){
    		ODGetFileInFolderOp adOp = (ODGetFileInFolderOp)o;
    		if (this.requestFileName.equals(adOp.requestFileName) &&
    				this.folderResourceId.equals(adOp.folderResourceId)&&
    				this.fileName.equals(adOp.fileName)){
				return true;
			}else{
				return false;
			}
    	}else{
    		return false;
    	}
    }
}
