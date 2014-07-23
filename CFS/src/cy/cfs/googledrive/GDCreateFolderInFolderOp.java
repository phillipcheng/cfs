package cy.cfs.googledrive;

import java.util.List;

import android.util.Log;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.DriveApi.DriveIdResult;
import com.google.android.gms.drive.DriveFolder.DriveFolderResult;
import com.google.android.gms.drive.events.ChangeEvent;
import com.google.android.gms.drive.events.DriveEvent.Listener;

import cy.cfs.DriveOp;
import cy.cfs.OpCallback;

public class GDCreateFolderInFolderOp extends DriveOp{
	
	protected static final String TAG = "GoogleDriveCreateFolderInFolderOp";
	
	private String requestFileName;
	private String parentResourceId;
	private String folderName;
	private GDCFSInstance cfsIns;
	
	public GDCreateFolderInFolderOp(String requestFileName, String parentResourceId, 
			String folderName, GDCFSInstance gdcfsIns){
		super(gdcfsIns);
		this.requestFileName = requestFileName;
		this.parentResourceId = parentResourceId;
		this.folderName = folderName;
		this.cfsIns = gdcfsIns;
	}
	
	@Override
	public void myRun(){
		if (parentResourceId==null){
			MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setTitle(folderName).build();
			Drive.DriveApi.getRootFolder(cfsIns.getGoogleApiClient()).
					createFolder(cfsIns.getGoogleApiClient(), changeSet).
					setResultCallback(callback);
		}else{
			Drive.DriveApi.fetchDriveId(cfsIns.getGoogleApiClient(), parentResourceId)
    			.setResultCallback(idCallback);
		}
	}
	
	final ResultCallback<DriveIdResult> idCallback = new ResultCallback<DriveIdResult>() {
        @Override
        public void onResult(DriveIdResult result) {
            if (!result.getStatus().isSuccess()) {
                Log.e(TAG, "Cannot find DriveId. Are you authorized to view this file?");
                return;
            }
            DriveFolder folder = Drive.DriveApi
                    .getFolder(cfsIns.getGoogleApiClient(), result.getDriveId());
            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                    .setTitle(folderName).build();
            folder.createFolder(cfsIns.getGoogleApiClient(), changeSet)
                    .setResultCallback(callback);
        }
    };
    
	final ResultCallback<DriveFolderResult> callback = new ResultCallback<DriveFolderResult>() {
        @Override
        public void onResult(DriveFolderResult result) {
            if (!result.getStatus().isSuccess()) {
                Log.e(TAG, "Error while trying to create the folder");
                List<OpCallback> cbList = getCallback();
	            for (OpCallback cb: cbList){
	            	cb.onFailure(result.getDriveFolder().getDriveId().getResourceId());
	            }
            }else{
            	Log.i(TAG, "Created a folder: " + result.getDriveFolder().getDriveId());
            	result.getDriveFolder().addChangeListener(cfsIns.getGoogleApiClient(), (new Listener<ChangeEvent>() {
            	    @Override
            	    public void onEvent(ChangeEvent event) {
            	    	String resourceId = event.getDriveId().getResourceId();
            	    	if (resourceId!=null){
	            	    	Log.i(TAG, "get the resourcid: " + resourceId);
	        	            List<OpCallback> cbList = GDCreateFolderInFolderOp.this.getCallback();
	        	            for (OpCallback cb: cbList){
	        	            	cb.onSuccess(resourceId);
	        	            }
            	    	}
            	    }
            	}));
	            
            }
        }
    };

}
