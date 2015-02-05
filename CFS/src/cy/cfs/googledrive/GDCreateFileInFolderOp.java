package cy.cfs.googledrive;

import java.io.IOException;

import android.util.Log;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveApi.DriveIdResult;
import com.google.android.gms.drive.DriveApi.MetadataBufferResult;
import com.google.android.gms.drive.DriveFolder.DriveFileResult;
import com.google.android.gms.drive.events.ChangeEvent;
import com.google.android.gms.drive.events.ChangeListener;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import cy.cfs.CFSInstance;
import cy.cfs.DriveOp;

public class GDCreateFileInFolderOp extends DriveOp{
	
	
	private String requestFileName;
	private String folderResourceId;
	private DriveId mFolderDriveId;
	private String fileName;
	private String mimeType;
	private byte[] binary;
	
	public GDCreateFileInFolderOp(String requestFileName, String folderResourceId, 
			String fileName, String mimeType, byte[] binary, CFSInstance gdcfsIns){
		super(gdcfsIns, requestFileName);
		this.requestFileName = requestFileName;
		this.folderResourceId = folderResourceId;
		this.fileName = fileName;
		this.mimeType = mimeType;
		this.binary = binary;
	}
	
	public String toString(){
		String str = super.toString();
		return str + ", requestFileName:" + requestFileName
				+ ",folderResourceId:" + folderResourceId
				+ ",fileName:" + fileName
				+ ",size:" + binary.length;
	}
	
	@Override
	public void run(){
		Log.i(TAG, "start to run:" + this + "\n");
        Drive.DriveApi.fetchDriveId(((GDCFSInstance)getCfsInst()).getGoogleApiClient(), folderResourceId)
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
            query();
        }
    };
    
    private void query(){
    	Query query = new Query.Builder()
			.addFilter(Filters.eq(SearchableField.TITLE, fileName))
			.build();
		DriveFolder folder = Drive.DriveApi
                .getFolder(((GDCFSInstance)getCfsInst()).getGoogleApiClient(), mFolderDriveId);
        folder.queryChildren(((GDCFSInstance)getCfsInst()).getGoogleApiClient(), query)
                .setResultCallback(queryCallback);
    	
    }
    
    final private ResultCallback<MetadataBufferResult> queryCallback = new
            ResultCallback<MetadataBufferResult>() {
        @Override
        public void onResult(MetadataBufferResult result) {
            if (!result.getStatus().isSuccess()) {
                Log.e(TAG, "Problem while retrieving results");
                return;
            }
            if (result.getMetadataBuffer().getCount()>0){
            	Metadata md = result.getMetadataBuffer().get(0);
            	String resourceId = md.getDriveId().getResourceId();
            	finalCallback(true, true, requestFileName, resourceId);
            	Log.i(TAG, String.format("%s exists with resourceId:%s", requestFileName, resourceId));
            }else{
            	//new content
            	Drive.DriveApi.newDriveContents(((GDCFSInstance)getCfsInst()).getGoogleApiClient())
                	.setResultCallback(contentsResult);
            }
            result.getMetadataBuffer().close();
        }
    };
    
    final private ResultCallback<DriveContentsResult> contentsResult = new
            ResultCallback<DriveContentsResult>() {
        @Override
        public void onResult(DriveContentsResult result) {
            if (!result.getStatus().isSuccess()) {
                Log.e(TAG, "Error while trying to create new file contents");
                return;
            }
            DriveFolder folder = Drive.DriveApi.getFolder(((GDCFSInstance)getCfsInst()).getGoogleApiClient(), mFolderDriveId);
            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                    .setTitle(fileName)
                    .setMimeType(mimeType)
                    .build();
            try {
				result.getDriveContents().getOutputStream().write(binary);
			} catch (IOException e) {
				Log.e(TAG, "", e);
			}
            folder.createFile(((GDCFSInstance)getCfsInst()).getGoogleApiClient(), changeSet, result.getDriveContents())
                    .setResultCallback(fileChangedCallback);
        }
    };

    final private ResultCallback<DriveFileResult> fileChangedCallback = new
            ResultCallback<DriveFileResult>() {
        @Override
        public void onResult(DriveFileResult result) {
            if (!result.getStatus().isSuccess()) {
                Log.e(TAG, "Error while trying to create the file");
                finalCallback(false, true, requestFileName, null);
            }else{
            	result.getDriveFile().addChangeListener(((GDCFSInstance)getCfsInst()).getGoogleApiClient(), (new ChangeListener() {
            	    @Override
            	    public void onChange(ChangeEvent event) {
            	    	String resourceId = event.getDriveId().getResourceId();
            	    	if (resourceId!=null){
	            	    	Log.i(TAG, "get the resourcid: " + resourceId);
	        	            finalCallback(true, true, requestFileName, resourceId);
            	    	}
            	    }
            	}));
            }
        }
    };
    
    @Override
    public boolean equals(Object o){
    	if (o instanceof GDCreateFileInFolderOp){
    		GDCreateFileInFolderOp adOp = (GDCreateFileInFolderOp)o;
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
