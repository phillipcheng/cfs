package cy.cfs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


import android.util.Log;

public class AddDirOp extends DriveOp{
	
	private String fileName; //
	
	public AddDirOp(CFSInstance cfsInstance, String request) {
		super(cfsInstance, request);
		this.fileName = request;
	}
	
	public String toString(){
		String str = super.toString();
		return str + ", fileName:" + fileName;
	}
	
	@Override
	public void run() {
		Log.i(TAG, "start to run:" + this + "\n");
		// directory sample: /this/is/a/directory
		String dirName = getFileName();
		StringTokenizer st = new StringTokenizer(dirName, File.separator);
		List<String> tokens = new ArrayList<String>();
		while (st.hasMoreTokens()){
			tokens.add(st.nextToken());
		}
		String preString="";
		String preResourceId=null;
		int i=0;
		for (String t:tokens){
			i++;
			preString = preString + File.separatorChar + t; 
			
			String currentOpId = getCfsInst().getDirMap().putIfAbsent(preString, WORKING_MARK + getId());
			Log.i(TAG, String.format("for %s current op is:%s", preString, currentOpId));
			if (currentOpId==null || //no one tried
					(WORKING_MARK+getId()).equals(currentOpId) || //My previous task is working on this, i am following up now
					(EMPTY_MARK.equals(currentOpId))){ //someone has marked it as empty, not exist on cloud
				getCfsInst().addWorker(this);
				//1
				DriveOp cfifop = OpFactory.getCreateFolderInFolderOp(preString, 
						preResourceId, t, getCfsInst());
				if (i<tokens.size()){
					cfifop.addCallback(this);
				}else{
					cfifop.addCallbackList(this.getCallback());
				}
				getCfsInst().replaceOp(false, preString, cfifop, currentOpId);
				break;//since process is async, we need to put follow-up actions in the callback.
			}else if (currentOpId.startsWith(WORKING_MARK)){
				//2
				if (i<tokens.size()){//others is working on my dependency, there is more work, i will wait for him
					//get the worker
					String opId = currentOpId.substring(WORKING_MARK.length());
					DriveOp worker = getCfsInst().getWorker(opId);
					worker.insertCallback(this);
					Log.i(TAG, "someone is working on my dependency:" + dirName + ":" + currentOpId);
				}else{
					//all i need to do is done, no more work
				}
				break;
			}else if (currentOpId.startsWith(ERROR_MARK)){
				//3. found error so i do not work on this any more
				Log.e(TAG, String.format("found error %s for %s", currentOpId, preString));
			}else{
				//4. already has the folder no create
				preResourceId = currentOpId;
			}
		}
	}
	
    @Override
    public boolean equals(Object o){
    	if (o instanceof AddDirOp){
    		AddDirOp adOp = (AddDirOp)o;
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
}
