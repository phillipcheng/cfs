package cy.cfs.googledrive;

import java.io.File;

import android.util.Log;

import cy.cfs.CFSInstance;
import cy.cfs.DriveOp;

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
		
		String currentFileOpId = ((GDCFSInstance)getCfsInst()).getFileMap().putIfAbsent(requestFileName, getId());
		if (currentFileOpId==null || getId().equals(currentFileOpId)){
			//i am/My Team is working on this
			String currentFolderOpId = ((GDCFSInstance)this.getCfsInst()).getDirMap().putIfAbsent(dirName, getId());
			
			if (currentFolderOpId==null || getId().equals(currentFolderOpId)){
				//i am/My Team is working on this
				//change to a AddDirOp
				AddDirOp addDir = new AddDirOp(getCfsInst());
				addDir.setId(getId());//belong to my team
				addDir.setFileName(dirName);
				addDir.insertCallback(this);
				getCfsInst().submit(addDir);
			}else if (currentFolderOpId.startsWith(File.separator)){
				//someone is working on my dependency, i will try later
				Log.i(TAG, "someone is working on my dependency." + dirName + ":" + currentFolderOpId);
				getCfsInst().submit(this);
			}else{
				GDCreateFileInFolderOp gdcfifop = new GDCreateFileInFolderOp(requestFileName, 
						currentFolderOpId, fileName, getMimeType(), getBinaryContent(), (GDCFSInstance) getCfsInst());
				gdcfifop.addCallback(new GDCreateItemCallback(requestFileName, (GDCFSInstance) getCfsInst()));
				gdcfifop.addCallbackList(getCallback());
				getCfsInst().submit(gdcfifop);
			}
		}else if (currentFileOpId.startsWith(File.separator)){
			//some one is working on that, i do not need to do this
			Log.i(TAG, "someone working on this." + requestFileName + ":" + currentFileOpId);
		}else{
			//already has the file no create.
			Log.i(TAG, "already done:" + requestFileName + ":" + currentFileOpId);
		}
	}
}
