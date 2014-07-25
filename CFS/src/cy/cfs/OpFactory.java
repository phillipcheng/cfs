package cy.cfs;

import cy.cfs.googledrive.GDCreateFileInFolderOp;
import cy.cfs.googledrive.GDCreateFolderInFolderOp;
import cy.cfs.onedrive.ODCreateFileInFolderOp;
import cy.cfs.onedrive.ODCreateFolderInFolderOp;

public class OpFactory {
	
	public static DriveOp getCreateFolderInFolderOp(String requestFolderName, String parentFolderResourceId, 
			String folderName, CFSInstance cfsInst){
		if (CFSConf.VENDOR_GOOGLE_DRIVE.equals(cfsInst.getConf().getVendor())){
			return new GDCreateFolderInFolderOp(requestFolderName, parentFolderResourceId, folderName, cfsInst);
		}else if (CFSConf.VENDOR_MICROSOFT.equals(cfsInst.getConf().getVendor())){
			return new ODCreateFolderInFolderOp(requestFolderName, parentFolderResourceId, folderName, cfsInst);
		}
		return null;
	}
	
	public static DriveOp getCreateFileInFolderOp(String requestFileName, String parentFolderResourceId, 
			String fileName, String mimeType, byte[] binary, CFSInstance cfsInst){
		if (CFSConf.VENDOR_GOOGLE_DRIVE.equals(cfsInst.getConf().getVendor())){
			return new GDCreateFileInFolderOp(requestFileName, parentFolderResourceId, fileName, 
					mimeType, binary, cfsInst);
		}else if (CFSConf.VENDOR_MICROSOFT.equals(cfsInst.getConf().getVendor())){
			return new ODCreateFileInFolderOp(requestFileName, parentFolderResourceId, fileName, 
					mimeType, binary, cfsInst);
		}
		return null;
	}

}
