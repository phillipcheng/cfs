package cy.cfs.googledrive;

import java.io.IOException;
import java.util.List;

import android.util.Log;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.DriveApi.ContentsResult;
import com.google.android.gms.drive.DriveApi.DriveIdResult;
import com.google.android.gms.drive.DriveFolder.DriveFileResult;
import com.google.android.gms.drive.DriveFolder.DriveFolderResult;
import com.google.android.gms.drive.events.ChangeEvent;
import com.google.android.gms.drive.events.DriveEvent.Listener;

import cy.cfs.DriveOp;
import cy.cfs.OpCallback;

public class GDCreateFileInFolderOp extends DriveOp{
	
	protected static final String TAG = "GDCreateFileInFolderOp";
	
	private String requestFileName;
	private String folderResourceId;
	private DriveId mFolderDriveId;
	private String fileName;
	private String mimeType;
	private GDCFSInstance cfsIns;
	private byte[] binary;
	
	public GDCreateFileInFolderOp(String requestFileName, String folderResourceId, 
			String fileName, String mimeType, byte[] binary, GDCFSInstance gdcfsIns){
		super(gdcfsIns);
		this.requestFileName = requestFileName;
		this.folderResourceId = folderResourceId;
		this.fileName = fileName;
		this.mimeType = mimeType;
		this.binary = binary;
		this.cfsIns = gdcfsIns;
	}
	
	@Override
	public void myRun(){
        Drive.DriveApi.fetchDriveId(cfsIns.getGoogleApiClient(), folderResourceId)
        		.setResultCallback(idCallback);
    };

    final private ResultCallback<DriveIdResult> idCallback = new ResultCallback<DriveIdResult>() {
        @Override
        public void onResult(DriveIdResult result) {
            if (!result.getStatus().isSuccess()) {
                Log.e(TAG, "Cannot find DriveId. Are you authorized to view this file?");
                return;
            }
            mFolderDriveId = result.getDriveId();
            Drive.DriveApi.newContents(cfsIns.getGoogleApiClient())
                    .setResultCallback(contentsResult);
        }
    };
    
    final private ResultCallback<ContentsResult> contentsResult = new
            ResultCallback<ContentsResult>() {
        @Override
        public void onResult(ContentsResult result) {
            if (!result.getStatus().isSuccess()) {
                Log.e(TAG, "Error while trying to create new file contents");
                return;
            }
            DriveFolder folder = Drive.DriveApi.getFolder(cfsIns.getGoogleApiClient(), mFolderDriveId);
            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                    .setTitle(fileName)
                    .setMimeType(mimeType)
                    .build();
            try {
				result.getContents().getOutputStream().write(binary);
			} catch (IOException e) {
				Log.e(TAG, "", e);
			}
            folder.createFile(cfsIns.getGoogleApiClient(), changeSet, result.getContents())
                    .setResultCallback(fileCallback);
        }
    };

    final private ResultCallback<DriveFileResult> fileCallback = new
            ResultCallback<DriveFileResult>() {
        @Override
        public void onResult(DriveFileResult result) {
            if (!result.getStatus().isSuccess()) {
                Log.e(TAG, "Error while trying to create the file");
                List<OpCallback> cbList = getCallback();
	            for (OpCallback cb: cbList){
	            	cb.onFailure(result.getDriveFile().getDriveId().getResourceId());
	            }
            }else{
            	result.getDriveFile().addChangeListener(cfsIns.getGoogleApiClient(), (new Listener<ChangeEvent>() {
            	    @Override
            	    public void onEvent(ChangeEvent event) {
            	    	String resourceId = event.getDriveId().getResourceId();
            	    	if (resourceId!=null){
	            	    	Log.i(TAG, "get the resourcid: " + resourceId);
	        	            List<OpCallback> cbList = getCallback();
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
