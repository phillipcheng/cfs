package cy.cfs.googledrive;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import android.util.Log;

import cy.cfs.CFSInstance;
import cy.cfs.DriveOp;

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
			
			String currentOpId = ((GDCFSInstance)this.getCfsInst()).getDirMap().putIfAbsent(preString, getId());
			if (currentOpId==null || getId().equals(currentOpId)){
				//I am/My Team is working on this
				GDCreateFolderInFolderOp gdcfifop = new GDCreateFolderInFolderOp(
						preString, preResourceId, t, (GDCFSInstance) getCfsInst());
				gdcfifop.addCallback(new GDCreateItemCallback(preString, (GDCFSInstance) getCfsInst()));
				if (i<tokens.size()-1){
					gdcfifop.addCallback(this);
				}else{
					//last
					gdcfifop.addCallbackList(getCallback());
				}
				getCfsInst().submit(gdcfifop);
				break;//since process is async, we need to put follow-up actions in the callback.
			}else if (currentOpId.startsWith(File.separator)){
				//others is working on my dependency, i will wait for him
				getCfsInst().submit(this);
				Log.i(TAG, "someone is working on my dependency:" + dirName + ":" + currentOpId);
				break;
			}else{
				//already has the folder no create
				preResourceId = currentOpId;
			}
		}
	}
}
