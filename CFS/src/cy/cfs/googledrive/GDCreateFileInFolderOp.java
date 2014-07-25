package cy.cfs.googledrive;

import java.io.IOException;

import android.util.Log;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.DriveApi.ContentsResult;
import com.google.android.gms.drive.DriveApi.DriveIdResult;
import com.google.android.gms.drive.DriveApi.MetadataBufferResult;
import com.google.android.gms.drive.DriveFolder.DriveFileResult;
import com.google.android.gms.drive.events.ChangeEvent;
import com.google.android.gms.drive.events.DriveEvent.Listener;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import cy.cfs.CFSInstance;
import cy.cfs.CallbackOp;
import cy.cfs.DriveOp;

public class GDCreateFileInFolderOp extends DriveOp{
	
	protected static final String TAG = "GDCreateFileInFolderOp";
	
	private String requestFileName;
	private String folderResourceId;
	private DriveId mFolderDriveId;
	private String fileName;
	private String mimeType;
	private byte[] binary;
	
	public GDCreateFileInFolderOp(String requestFileName, String folderResourceId, 
			String fileName, String mimeType, byte[] binary, CFSInstance gdcfsIns){
		super(gdcfsIns);
		this.requestFileName = requestFileName;
		this.folderResourceId = folderResourceId;
		this.fileName = fileName;
		this.mimeType = mimeType;
		this.binary = binary;
	}
	
	@Override
	public void myRun(){
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
            	Drive.DriveApi.newContents(((GDCFSInstance)getCfsInst()).getGoogleApiClient())
                	.setResultCallback(contentsResult);
            }
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
            DriveFolder folder = Drive.DriveApi.getFolder(((GDCFSInstance)getCfsInst()).getGoogleApiClient(), mFolderDriveId);
            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                    .setTitle(fileName)
                    .setMimeType(mimeType)
                    .build();
            try {
				result.getContents().getOutputStream().write(binary);
			} catch (IOException e) {
				Log.e(TAG, "", e);
			}
            folder.createFile(((GDCFSInstance)getCfsInst()).getGoogleApiClient(), changeSet, result.getContents())
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
            	result.getDriveFile().addChangeListener(((GDCFSInstance)getCfsInst()).getGoogleApiClient(), (new Listener<ChangeEvent>() {
            	    @Override
            	    public void onEvent(ChangeEvent event) {
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
}
