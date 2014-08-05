package cy.cfs;

import cy.cfs.googledrive.GDCFSInstance;
import cy.cfs.googledrive.GDCreateFileInFolderOp;
import cy.cfs.googledrive.GDCreateFolderInFolderOp;
import cy.cfs.googledrive.GDGetFileInFolderOp;
import cy.cfs.googledrive.GDGetFolderInFolderOp;
import cy.cfs.onedrive.ODCFSInstance;
import cy.cfs.onedrive.ODCreateFileInFolderOp;
import cy.cfs.onedrive.ODCreateFolderInFolderOp;
import cy.cfs.onedrive.ODGetFileInFolderOp;
import cy.cfs.onedrive.ODGetFolderInFolderOp;

public class OpFactory {
	
	public static CFSInstance getCFSInstance(String id, String vendor, String userId){
		CFSInstance cfsInstance = null;
		if (CFSInstance.VENDOR_GOOGLE_DRIVE.equals(vendor)){
			cfsInstance = new GDCFSInstance(id, userId);
		}else if (CFSInstance.VENDOR_MICROSOFT.equals(vendor)){
			cfsInstance = new ODCFSInstance(id, userId);
		}
		return cfsInstance;
	}
	public static DriveOp getCreateFolderInFolderOp(String requestFolderName, String parentFolderResourceId, 
			String folderName, CFSInstance cfsInst){
		if (CFSInstance.VENDOR_GOOGLE_DRIVE.equals(cfsInst.getVendor())){
			return new GDCreateFolderInFolderOp(requestFolderName, parentFolderResourceId, folderName, cfsInst);
		}else if (CFSInstance.VENDOR_MICROSOFT.equals(cfsInst.getVendor())){
			return new ODCreateFolderInFolderOp(requestFolderName, parentFolderResourceId, folderName, cfsInst);
		}
		return null;
	}
	
	public static DriveOp getCreateFileInFolderOp(String requestFileName, String parentFolderResourceId, 
			String fileName, String mimeType, byte[] binary, CFSInstance cfsInst){
		if (CFSInstance.VENDOR_GOOGLE_DRIVE.equals(cfsInst.getVendor())){
			return new GDCreateFileInFolderOp(requestFileName, parentFolderResourceId, fileName, 
					mimeType, binary, cfsInst);
		}else if (CFSInstance.VENDOR_MICROSOFT.equals(cfsInst.getVendor())){
			return new ODCreateFileInFolderOp(requestFileName, parentFolderResourceId, fileName, 
					mimeType, binary, cfsInst);
		}
		return null;
	}
	
	public static DriveOp getGetFolderInFolderOp(String requestFolderName, String parentFolderResourceId, 
			String folderName, CFSInstance cfsInst){
		if (CFSInstance.VENDOR_GOOGLE_DRIVE.equals(cfsInst.getVendor())){
			return new GDGetFolderInFolderOp(requestFolderName, parentFolderResourceId, folderName, cfsInst);
		}else if (CFSInstance.VENDOR_MICROSOFT.equals(cfsInst.getVendor())){
			return new ODGetFolderInFolderOp(requestFolderName, parentFolderResourceId, folderName, cfsInst);
		}
		return null;
	}
	
	public static DriveOp getGetFileInFolderOp(String requestFileName, String parentFolderResourceId, 
			String fileName, String fileResourceId, CFSInstance cfsInst, Object request, int width, int height){
		if (CFSInstance.VENDOR_GOOGLE_DRIVE.equals(cfsInst.getVendor())){
			return new GDGetFileInFolderOp(requestFileName, parentFolderResourceId, fileName, fileResourceId, 
					cfsInst, request, width, height);
		}else if (CFSInstance.VENDOR_MICROSOFT.equals(cfsInst.getVendor())){
			return new ODGetFileInFolderOp(requestFileName, parentFolderResourceId, fileName, fileResourceId, 
					cfsInst, request, width, height);
		}
		return null;
	}

}
