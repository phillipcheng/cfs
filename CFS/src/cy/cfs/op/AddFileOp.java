package cy.cfs.op;

import java.io.File;

import cy.cfs.CFSInstance;
import cy.cfs.DriveOp;
import cy.cfs.OpFactory;

import android.util.Log;


public class AddFileOp extends DriveOp{
	
	public AddFileOp(CFSInstance cfsInstance) {
		super(cfsInstance);
	}

	private String fileName; //
	private String mimeType;
	private long size;
	private byte[] binaryContent;
	
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getMimeType() {
		return mimeType;
	}
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public byte[] getBinaryContent() {
		return binaryContent;
	}
	public void setBinaryContent(byte[] binaryContent) {
		this.binaryContent = binaryContent;
	}
	
	@Override
	public void myRun() {
		String requestFileName = getFileName();
		String dirName = requestFileName.substring(0, requestFileName.lastIndexOf(File.separator));
		String fileName = requestFileName.substring(dirName.length());
		
		String currentFileOpId = getCfsInst().getFileMap().putIfAbsent(requestFileName, WORKING_MARK+getId());
		if (currentFileOpId==null || (WORKING_MARK+getId()).equals(currentFileOpId)){
			//i am/My Team is working on this
			String currentFolderOpId = getCfsInst().getDirMap().putIfAbsent(dirName, WORKING_MARK+getId());
			
			if (currentFolderOpId==null || (WORKING_MARK+getId()).equals(currentFolderOpId)){
				//i am/My Team is working on this
				//change to a AddDirOp
				AddDirOp addDir = new AddDirOp(getCfsInst());
				addDir.setId(getId());//belong to my team
				addDir.setFileName(dirName);
				addDir.insertCallback(this);
				getCfsInst().submit(addDir);
			}else if (currentFolderOpId.startsWith(WORKING_MARK)){
				//someone is working on my dependency, i will try later
				Log.i(TAG, "someone is working on my dependency." + dirName + ":" + currentFolderOpId);
				getCfsInst().submit(this);
			}else{
				DriveOp cfifop = OpFactory.getCreateFileInFolderOp(requestFileName, 
						currentFolderOpId, fileName, getMimeType(), getBinaryContent(), getCfsInst());
				cfifop.addCallbackList(getCallback());
				getCfsInst().submit(cfifop);
			}
		}else if (currentFileOpId.startsWith(WORKING_MARK)){
			//some one is working on that, i do not need to do this, since there is no follow up tasks
			Log.i(TAG, "someone working on this." + requestFileName + ":" + currentFileOpId);
		}else if (currentFileOpId.startsWith(ERROR_MAKE)){
			//some one found there is error there, so i do not work on this again.
			Log.e(TAG, String.format("error found:%s for %s", currentFileOpId, requestFileName));
		}else{
			//already has the file create.
			Log.i(TAG, "already done:" + requestFileName + ":" + currentFileOpId);
		}
	}
}
