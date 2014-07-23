package cy.cfs.googledrive;

import java.util.Map;

import com.google.android.gms.drive.DriveId;

import cy.cfs.OpCallback;

public class GDCreateItemCallback implements OpCallback{
	
	private String requestFileName;
	private GDCFSInstance cfsInst;
	
	public GDCreateItemCallback(String requestFileName, GDCFSInstance cfsInst){
		this.requestFileName = requestFileName;
		this.cfsInst = cfsInst;
	}
	
	public void onSuccess(Object result){
		//update the result
		String resourceId = (String)result;
		Map<String, String> dirMap = cfsInst.getDirMap();
		dirMap.put(requestFileName, resourceId);
		
	}

	@Override
	public void onFailure(Object result) {
		//remove the entry in map
		Map<String, String> dirMap = cfsInst.getDirMap();
		dirMap.remove(requestFileName);
	}
}
