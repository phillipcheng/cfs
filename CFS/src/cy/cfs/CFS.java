package cy.cfs;


import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import cy.cfs.googledrive.GDCFSInstance;
import cy.cfs.onedrive.ODCFSInstance;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

public class CFS {
	
	private static final String TAG = "CFS";
	
	private static Map<String, CFSInstance> cfsTable;
	
	public static CFSInstance getCFSInstanceById(String cfsid){
		return cfsTable.get(cfsid);
	}
	
	//parse the fileKey into cfsId and fileId
	private static String[] parse(String fileKey, CFSMapper mapper){
		String cfsFileId = mapper.getCFSFileId(fileKey);
		int index = cfsFileId.indexOf(":");
		if (index>-1){
			String cfsId = cfsFileId.substring(0, index);
			String fileId = cfsFileId.substring(index+1);
			return new String[]{cfsId, fileId};
		}else{
			Log.e(TAG, String.format("wrong mapping result %s got for %s", cfsFileId, fileKey));
			return null;
		}
	}
	
	public static Map<String, CFSInstance> getCFSInstances(String userId){
		if (cfsTable==null){
			cfsTable = new HashMap<String, CFSInstance>();
			//TODO, fetch from web services
			GDCFSInstance googleInstance1 = new GDCFSInstance("g1", CFSInstance.VENDOR_GOOGLE_DRIVE, 
					"phillipchengyi@gmail.com", 10000000000l, "/readall", userId);
			ODCFSInstance msInstance1 = new ODCFSInstance("m1", CFSInstance.VENDOR_MICROSOFT, 
					"phillipchengyi@gmail.com", 10000000000l, "/readall", userId);
			ODCFSInstance msInstance2 = new ODCFSInstance("m2", CFSInstance.VENDOR_MICROSOFT, 
					"phillipchengyi@hotmail.com", 10000000000l, "/readall", userId);
			
			cfsTable.put(googleInstance1.getId(), googleInstance1);
			cfsTable.put(msInstance1.getId(), msInstance1);
			cfsTable.put(msInstance2.getId(), msInstance2);
		}
		return cfsTable;
	}
	//
	public static void asyncSaveImageFile(String userId, String fileKey, CFSMapper mapper, Bitmap bmp, 
			CallbackOp callback, Context context){
		getCFSInstances(userId);
		String[] ret = parse(fileKey, mapper);
		if (ret!=null){
			CFSInstance cfsIns = getCFSInstanceById(ret[0]);
			if (cfsIns!=null){
				String fileName = cfsIns.getRootFolder() + ret[1];
				AddFileOp addFile = new AddFileOp(cfsIns, fileName);
				addFile.setMimeType("image/png");
				addFile.setSize(bmp.getRowBytes() * bmp.getHeight());
				
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
				byte[] byteArray = stream.toByteArray();
				addFile.setBinaryContent(byteArray);
				if (callback!=null){
					addFile.addCallback(callback);
				}
				cfsIns.submit(addFile, context);
			}
		}
	}
	
	//
	public static void asyncGetContent(String userId, String fileKey, CFSMapper mapper, 
			Object request, CallbackOp callback, Context context, int width, int height){
		getCFSInstances(userId);
		String[] ret = parse(fileKey, mapper);
		if (ret!=null){
			CFSInstance cfsIns = getCFSInstanceById(ret[0]);
			if (cfsIns!=null){
				String fileName = cfsIns.getRootFolder() + ret[1];
				GetFileOp getFile = new GetFileOp(cfsIns, fileName, request, width, height);
				getFile.addCallback(callback);
				cfsIns.submit(getFile, context);
			}
		}
	}
}
