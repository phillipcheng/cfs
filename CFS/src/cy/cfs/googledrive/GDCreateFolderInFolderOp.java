package cy.cfs.googledrive;


import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.DriveApi.DriveIdResult;
import com.google.android.gms.drive.DriveApi.MetadataBufferResult;
import com.google.android.gms.drive.DriveFolder.DriveFolderResult;
import com.google.android.gms.drive.events.ChangeEvent;
import com.google.android.gms.drive.events.DriveEvent.Listener;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import cy.cfs.CFSInstance;
import cy.cfs.DriveOp;

public class GDCreateFolderInFolderOp extends DriveOp{
	
	private String requestFolderName;//this is the full path
	private String parentResourceId;
	private String folderName;
	private DriveId folderDriveId;
	
	
	public GDCreateFolderInFolderOp(String requestFolderName, String parentResourceId, 
			String folderName, CFSInstance gdcfsIns){
		super(gdcfsIns, requestFolderName);
		this.requestFolderName = requestFolderName;
		this.parentResourceId = parentResourceId;
		this.folderName = folderName;
	}
	
	public String toString(){
		String str = super.toString();
		return str + ", requestFolderName:" + requestFolderName
				+ ",parentResourceId:" + parentResourceId
				+ ",folderName:" + folderName;
	}
	
	
	@Override
	public void run(){
		Log.i(TAG, "start to run:" + this + "\n");
		if (TextUtils.isEmpty(parentResourceId)){
			query();
		}else{
			//get parent driveId
			Drive.DriveApi.fetchDriveId(((GDCFSInstance)getCfsInst()).getGoogleApiClient(), parentResourceId)
    			.setResultCallback(idCallback);
		}
	}
    
    final ResultCallback<DriveIdResult> idCallback = new ResultCallback<DriveIdResult>() {
        @Override
        public void onResult(DriveIdResult result) {
            if (!result.getStatus().isSuccess()) {
                Log.e(TAG, "Cannot find DriveId:"+result.getStatus().toString());
                finalCallback(false, false, requestFolderName, result.getStatus().toString());
                return;
            }
            folderDriveId = result.getDriveId();
            query();
        }
    };
	
    private void query(){
    	Query query = new Query.Builder()
			.addFilter(Filters.eq(SearchableField.TITLE, folderName))
			.build();
    	if (TextUtils.isEmpty(parentResourceId)){
			//add folder to root
			Drive.DriveApi.query(((GDCFSInstance)getCfsInst()).getGoogleApiClient(), query)
	        	.setResultCallback(queryCallback);
    	}else{
    		DriveFolder folder = Drive.DriveApi
                    .getFolder(((GDCFSInstance)getCfsInst()).getGoogleApiClient(), folderDriveId);
            folder.queryChildren(((GDCFSInstance)getCfsInst()).getGoogleApiClient(), query)
                    .setResultCallback(queryCallback);
    	}
    }
    
	final private ResultCallback<MetadataBufferResult> queryCallback = new
            ResultCallback<MetadataBufferResult>() {
        @Override
        public void onResult(MetadataBufferResult result) {
            if (!result.getStatus().isSuccess()) {
                Log.e(TAG, "Problem while retrieving results");
                finalCallback(false, false, requestFolderName, result.getStatus().toString());
                return;
            }
            if (result.getMetadataBuffer().getCount()>0){
            	Metadata md= result.getMetadataBuffer().get(0);
            	String resourceId = md.getDriveId().getResourceId();
            	Log.i(TAG, String.format("%s exists, resourceId is:%s with title:%s", 
            			requestFolderName, resourceId, md.getTitle()));
            	finalCallback(true, false, requestFolderName, resourceId);
            }else{
            	addFolder();
            }
            result.getMetadataBuffer().close();
        }
    };
    
    private void addFolder(){
    	if (TextUtils.isEmpty(parentResourceId)){
			MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setTitle(folderName).build();
			Drive.DriveApi.getRootFolder(((GDCFSInstance)getCfsInst()).getGoogleApiClient()).
					createFolder(((GDCFSInstance)getCfsInst()).getGoogleApiClient(), changeSet).
					setResultCallback(addFolderCallback);
		}else{
			//add folder to parent
            DriveFolder folder = Drive.DriveApi
                    .getFolder(((GDCFSInstance)getCfsInst()).getGoogleApiClient(), folderDriveId);
            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                    .setTitle(folderName).build();
            folder.createFolder(((GDCFSInstance)getCfsInst()).getGoogleApiClient(), changeSet)
                    .setResultCallback(addFolderCallback);
		}
    }

	final ResultCallback<DriveFolderResult> addFolderCallback = new ResultCallback<DriveFolderResult>() {
        @Override
        public void onResult(DriveFolderResult result) {
            if (!result.getStatus().isSuccess()) {
                Log.e(TAG, "Error while trying to create the folder");
                finalCallback(false, false, requestFolderName, null);
            }else{
            	Log.i(TAG, "Created a folder: " + result.getDriveFolder().getDriveId());
            	result.getDriveFolder().addChangeListener(((GDCFSInstance)getCfsInst()).getGoogleApiClient(), (new Listener<ChangeEvent>() {
            	    @Override
            	    public void onEvent(ChangeEvent event) {
            	    	String resourceId = event.getDriveId().getResourceId();
            	    	if (resourceId!=null){
	            	    	Log.i(TAG, "get the resourcid: " + resourceId);
	        	            finalCallback(true, false, requestFolderName, resourceId);
            	    	}else{
            	    		Log.e(TAG, "no resourceId found for:" + event);
            	    		finalCallback(false, false, requestFolderName, event.toString());
            	    	}
            	    }
            	}));
            }
        }
    };

    @Override
    public boolean equals(Object o){
    	if (o instanceof GDCreateFolderInFolderOp){
    		GDCreateFolderInFolderOp adOp = (GDCreateFolderInFolderOp)o;
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
