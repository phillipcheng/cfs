package cy.cfs.googledrive;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.DriveApi.ContentsResult;
import com.google.android.gms.drive.DriveApi.DriveIdResult;
import com.google.android.gms.drive.DriveApi.MetadataBufferResult;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import cy.cfs.CFSInstance;
import cy.cfs.CloudImageUtil;
import cy.cfs.DriveOp;

public class GDGetFileInFolderOp extends DriveOp{
	
	private String requestFileName;
	
	private String folderResourceId;//1
	private DriveId mFolderDriveId;
	private String fileName;
	
	private String fileResourceId;//2
	
	private int width;
	private int height;
	
	public GDGetFileInFolderOp(String requestFileName, String folderResourceId, 
			String fileName, String fileResourceId, CFSInstance gdcfsIns, Object req, int width, int height){
		super(gdcfsIns, req);
		this.requestFileName = requestFileName;
		this.folderResourceId = folderResourceId;
		this.fileName = fileName;
		this.fileResourceId = fileResourceId;
		this.width = width;
		this.height = height;
	}
	
	public String toString(){
		String str = super.toString();
		return str + ", requestFileName:" + requestFileName
				+ ",folderResourceId:" + folderResourceId
				+ ",fileName:" + fileName;
	}
	
	@Override
	public void run(){
		if (fileResourceId==null){
			Drive.DriveApi.fetchDriveId(((GDCFSInstance)getCfsInst()).getGoogleApiClient(), folderResourceId)
        			.setResultCallback(idCallback);
		}else{
			Drive.DriveApi.fetchDriveId(((GDCFSInstance)getCfsInst()).getGoogleApiClient(), fileResourceId)
				.setResultCallback(idCallback);
		}
    };

    final private ResultCallback<DriveIdResult> idCallback = new ResultCallback<DriveIdResult>() {
        @Override
        public void onResult(DriveIdResult result) {
            if (!result.getStatus().isSuccess()) {
                Log.e(TAG, "Cannot find DriveId. Are you authorized to view this file?");
                return;
            }
            if (fileResourceId==null){
            	mFolderDriveId = result.getDriveId();
            	query();
            }else{
            	new RetrieveDriveFileContentsAsyncTask().execute(result.getDriveId());
            }
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
            	sysCallback(true, true, requestFileName, resourceId);
            	new RetrieveDriveFileContentsAsyncTask().execute(md.getDriveId());
            }else{//not found
            	Log.e(TAG, String.format("file:%s not found under folder resourceId:%s", fileName, folderResourceId));
            	sysCallback(true, true, requestFileName, EMPTY_MARK);
            	usrCallback(false, null);
            }
            result.getMetadataBuffer().close();
        }
    };
    

    private GoogleApiClient getGoogleApiClient(){
    	return ((GDCFSInstance)cfsInst).getGoogleApiClient();
    }
    
    final private class RetrieveDriveFileContentsAsyncTask
            extends AsyncTask<DriveId, Boolean, Bitmap> {

        public RetrieveDriveFileContentsAsyncTask() {
        }

        @Override
        protected Bitmap doInBackground(DriveId... params) {
            Bitmap bmp = null;
            DriveFile file = Drive.DriveApi.getFile(getGoogleApiClient(), params[0]);
            ContentsResult contentsResult =
                    file.openContents(getGoogleApiClient(), DriveFile.MODE_READ_ONLY, null).await();
            if (!contentsResult.getStatus().isSuccess()) {
                return null;
            }
            bmp = CloudImageUtil.getBitmap(contentsResult.getContents().getInputStream(), width, height);

            file.discardContents(getGoogleApiClient(), contentsResult.getContents()).await();
            return bmp;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
           usrCallback(true, result);
        }
    }
    
    @Override
    public boolean equals(Object o){
    	if (o instanceof GDGetFileInFolderOp){
    		GDGetFileInFolderOp adOp = (GDGetFileInFolderOp)o;
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
