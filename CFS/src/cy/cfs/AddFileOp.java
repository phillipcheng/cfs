package cy.cfs;

import java.io.File;


import android.util.Log;


public class AddFileOp extends DriveOp{
	
	public AddFileOp(CFSInstance cfsInstance, String fileName) {
		super(cfsInstance, fileName);
		this.fileName = fileName;
	}

	private String fileName; //
	private String mimeType;
	private long size;
	private byte[] binaryContent;
	
	public String toString(){
		String str = super.toString();
		return str + ", fileName:" + fileName
				   + ", mimeType:" + mimeType
				   + ", size:" + size;
	}
	
	@Override
	public void run() {
		Log.i(TAG, "start to run:" + this + "\n");
		String requestFileName = getFileName();
		String dirName = requestFileName.substring(0, requestFileName.lastIndexOf(File.separator));
		String fileName = requestFileName.substring(dirName.length()+1);//1 is the length of File.separator
		
		String currentFileOpId = getCfsInst().getFileMap().putIfAbsent(requestFileName, WORKING_MARK+getId());
		if (currentFileOpId==null ||  //no one tried
				(WORKING_MARK+getId()).equals(currentFileOpId) ||//My previous task is working on this, i am following up now
				(EMPTY_MARK.equals(currentFileOpId))){//someone has marked it as empty, not exist on cloud
			getCfsInst().addWorker(this);
			//1
			
			String currentFolderOpId = getCfsInst().getDirMap().putIfAbsent(dirName, WORKING_MARK+getId());
			
			if (currentFolderOpId==null || //no one tried
					(WORKING_MARK+getId()).equals(currentFolderOpId) ||//My previous task is working on this, i am following up now
					(EMPTY_MARK.equals(currentFolderOpId))){//someone has marked it as empty, not exist on cloud
				//1. change to a AddDirOp
				AddDirOp addDir = new AddDirOp(getCfsInst(), dirName);
				addDir.insertCallback(this);
				getCfsInst().replaceOp(false, dirName, addDir, currentFileOpId);
			}else if (currentFolderOpId.startsWith(WORKING_MARK)){
				//2. someone is working on my dependency, i will try later
				String opId = currentFolderOpId.substring(WORKING_MARK.length());
				Log.i(TAG, "some one is working on " + dirName + ":" + opId);
				DriveOp worker = getCfsInst().getWorker(opId);
				if (worker!=null){
					worker.insertCallback(this);
				}else{
					Log.e(TAG, "worker can't be found for." + dirName + ":" + opId);
				}
				
			}else if (currentFolderOpId.startsWith(ERROR_MARK)){//
				//3. some one found there is error there, so i do not work on this again.
				Log.e(TAG, String.format("error found:%s for %s", currentFolderOpId, dirName));
				getCfsInst().removeWorker(this);
			}else{
				//4. dir dependency is resolved already, move ahead with file creation
				DriveOp cfifop = OpFactory.getCreateFileInFolderOp(requestFileName, 
						currentFolderOpId, fileName, getMimeType(), getBinaryContent(), getCfsInst());
				cfifop.addCallbackList(getCallback());
				getCfsInst().submit(cfifop);
				//I need to remove myself
				getCfsInst().removeWorker(this);
			}
		}else if (currentFileOpId.startsWith(WORKING_MARK)){
			//2. some one is working on that, i do not need to do this, since there is no follow up tasks
			String opId = currentFileOpId.substring(WORKING_MARK.length());
			DriveOp worker = getCfsInst().getWorker(opId);
			Log.i(TAG, "someone working on this." + requestFileName + ":" + opId + "," + worker);
		}else if (currentFileOpId.startsWith(ERROR_MARK)){
			//3. some one found there is error there, so i do not work on this again.
			Log.e(TAG, String.format("error found:%s for %s", currentFileOpId, requestFileName));
		}else{
			//4. already has the file create.
			Log.i(TAG, "already done:" + requestFileName + ":" + currentFileOpId);
		}
	}
	@Override
    public boolean equals(Object o){
    	if (o instanceof AddFileOp){
    		AddFileOp adOp = (AddFileOp)o;
    		if (this.getFileName().equals(adOp.getFileName())){
				return true;
			}else{
				return false;
			}
    	}else{
    		return false;
    	}
    }
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
}
