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

import cy.cfs.DriveOp;
import cy.cfs.OpCallback;

public class GDCreateFileInFolderOp extends DriveOp{
	
	protected static final String TAG = "GDCreateFileInFolderOp";
	
	private String requestFileName;
	private DriveId folderDriverId;
	private String fileName;
	private String mimeType;
	private GDCFSInstance cfsIns;
	private byte[] binary;
	
	public GDCreateFileInFolderOp(String requestFileName, DriveId folderDriverId, 
			String fileName, String mimeType, byte[] binary, GDCFSInstance gdcfsIns){
		this.requestFileName = requestFileName;
		this.folderDriverId = folderDriverId;
		this.fileName = fileName;
		this.mimeType = mimeType;
		this.binary = binary;
		this.cfsIns = gdcfsIns;
	}
	
	public void process(){
        Drive.DriveApi.newContents(cfsIns.getGoogleApiClient())
                .setResultCallback(contentsResult);
    };

    final private ResultCallback<ContentsResult> contentsResult = new
            ResultCallback<ContentsResult>() {
        @Override
        public void onResult(ContentsResult result) {
            if (!result.getStatus().isSuccess()) {
                Log.e(TAG, "Error while trying to create new file contents");
                return;
            }
            DriveFolder folder = Drive.DriveApi.getFolder(cfsIns.getGoogleApiClient(), folderDriverId);
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
                return;
            }
            Log.i(TAG, "Created a file: " + result.getDriveFile().getDriveId());
        }
    };

}
