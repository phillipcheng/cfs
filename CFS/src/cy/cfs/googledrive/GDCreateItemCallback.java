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
	
	public void execute(Object result){
		DriveId driveId = (DriveId)result;
		Map<String, DriveId> dirMap = cfsInst.getDirMap();
		dirMap.put(requestFileName, driveId);
	}

}
