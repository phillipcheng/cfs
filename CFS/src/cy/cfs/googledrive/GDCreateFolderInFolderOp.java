package cy.cfs.googledrive;

import java.util.List;

import android.util.Log;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.DriveApi.DriveIdResult;
import com.google.android.gms.drive.DriveFolder.DriveFolderResult;

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
		this.requestFileName = requestFileName;
		this.parentResourceId = parentResourceId;
		this.folderName = folderName;
		this.cfsIns = gdcfsIns;
	}
	
	public String getParentResourceId() {
		return parentResourceId;
	}
	public void setParentResourceId(String parentResourceId) {
		this.parentResourceId = parentResourceId;
	}
	public String getFolderName() {
		return folderName;
	}
	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}
	
	public void process(){
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
                return;
            }
            Log.i(TAG, "Created a folder: " + result.getDriveFolder().getDriveId());
            List<OpCallback> cbList = GDCreateFolderInFolderOp.this.getCallback();
            for (OpCallback cb: cbList){
            	cb.execute(result.getDriveFolder().getDriveId());
            }
        }
    };

}
