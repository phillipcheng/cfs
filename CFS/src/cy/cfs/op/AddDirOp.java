package cy.cfs.op;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import cy.cfs.CFSInstance;
import cy.cfs.DriveOp;
import cy.cfs.OpFactory;

import android.util.Log;

public class AddDirOp extends DriveOp{
	
	private String fileName; //
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public AddDirOp(CFSInstance cfsInstance) {
		super(cfsInstance);
	}
	
	
	@Override
	public void myRun() {
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
			if (currentOpId==null || (WORKING_MARK+getId()).equals(currentOpId)){
				//I am/My Team is working on this
				DriveOp cfifop = OpFactory.getCreateFolderInFolderOp(preString, 
						preResourceId, t, getCfsInst());
				if (i<tokens.size()-1){
					cfifop.addCallback(this);
				}else{
					//last
					cfifop.addCallbackList(getCallback());
				}
				getCfsInst().submit(cfifop);
				break;//since process is async, we need to put follow-up actions in the callback.
			}else if (currentOpId.startsWith(WORKING_MARK)){
				if (i<tokens.size()-1){
					//others is working on my dependency, there is more work, i will wait for him
					getCfsInst().submit(this);
					Log.i(TAG, "someone is working on my dependency:" + dirName + ":" + currentOpId);
				}else{
					//all i need to do is done, no more work
				}
				break;
			}else if (currentOpId.startsWith(ERROR_MAKE)){
				//found error so i do not work on this any more
				Log.e(TAG, String.format("found error %s for %s", currentOpId, preString));
			}else{
				//already has the folder no create
				preResourceId = currentOpId;
			}
		}
	}
}
