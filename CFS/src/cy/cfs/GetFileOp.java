package cy.cfs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import android.util.Log;

public class GetFileOp extends DriveOp{

	private String fileName;
	private int width;
	private int height;
	
	public GetFileOp(CFSInstance cfsInstance, String fileName, Object request, int width, int height) {
		super(cfsInstance, request);
		this.fileName = fileName;
		this.width = width;
		this.height=height;
	}
	
	public String toString(){
		String str = super.toString();
		return str + ", fileName:" + fileName;
	}
	
	@Override
	public void run() {
		// fileName sample: /this/is/a/directory/file
		StringTokenizer st = new StringTokenizer(fileName, File.separator);
		List<String> tokens = new ArrayList<String>();
		while (st.hasMoreTokens()){
			tokens.add(st.nextToken());
		}
		String preString="";
		String preResourceId=null;
		int i=0;
		boolean found=false;
		for (String t:tokens){
			i++;
			preString = preString + File.separatorChar + t; 
			if (i<tokens.size()){//dir
				getCfsInst().addWorker(this);
				String currentOpId = getCfsInst().getDirMap().putIfAbsent(preString, WORKING_MARK + getId());
				if (EMPTY_MARK.equals(currentOpId)){//no exist on cloud
					found=false;//no found
					getCfsInst().removeWorker(this);
					break;
				}else if (currentOpId==null || //I can work on this
						(WORKING_MARK+getId()).equals(currentOpId)){//I am work on this
					DriveOp gfifop = OpFactory.getGetFolderInFolderOp(preString, 
							preResourceId, t, getCfsInst());
					gfifop.addCallback(this);//since i am get a file and this only get directory so i need to follow up
					getCfsInst().replaceOp(false, preString, gfifop,currentOpId);
					break;
				}else if (currentOpId.startsWith(WORKING_MARK)){//other is working on this
					String opId = currentOpId.substring(WORKING_MARK.length());
					DriveOp worker = getCfsInst().getWorker(opId);
					worker.insertCallback(this);
					break;
				}else if (currentOpId.startsWith(ERROR_MARK)){
					//found error
					Log.e(TAG, String.format("found error %s for %s", currentOpId, preString));
					found=false;//no found
					getCfsInst().removeWorker(this);
					break;
				}else{
					//already has the folder no get from cloud
					preResourceId = currentOpId;
					getCfsInst().removeWorker(this);
				}
			}else{//file
				getCfsInst().addWorker(this);
				String currentOpId = getCfsInst().getFileMap().putIfAbsent(preString, WORKING_MARK + getId());
				if (EMPTY_MARK.equals(currentOpId)){//no exist on cloud
					found=false;//no found
					getCfsInst().removeWorker(this);
					break;
				}else if (currentOpId==null || 
						(WORKING_MARK+getId()).equals(currentOpId)){//I/My Team should work on this
					DriveOp gfifop = OpFactory.getGetFileInFolderOp(preString, 
							preResourceId, t, null, getCfsInst(), getRequest(), width, height);
					gfifop.addCallbackList(this.getCallback());//this is the final op
					getCfsInst().replaceOp(true, preString, gfifop,currentOpId);
					break;
				}else if (currentOpId.startsWith(WORKING_MARK)){//other is working on this
					String opId = currentOpId.substring(WORKING_MARK.length());
					DriveOp worker = getCfsInst().getWorker(opId);
					if (worker!=null){
						worker.insertCallback(this);
					}else{
						Log.e(TAG, "work not found: " + opId);
					}
					break;
				}else if (currentOpId.startsWith(ERROR_MARK)){
					//found error
					Log.e(TAG, String.format("found error %s for %s", currentOpId, preString));
					preResourceId = null;
					getCfsInst().removeWorker(this);
					break;
				}else{
					//already has the file resource id, get the file
					DriveOp gfifop = OpFactory.getGetFileInFolderOp(preString, 
							preResourceId, t, currentOpId, getCfsInst(), getRequest(), width, height);
					gfifop.addCallbackList(getCallback());//this is the final op
					getCfsInst().submit(gfifop);
					getCfsInst().removeWorker(this);
					break;
				}
			}
		}
		if (!found){
			this.usrCallback(false, null);
		}
	}


	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}
