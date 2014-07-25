package cy.cfs;


import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import cy.cfs.googledrive.GDCFSInstance;
import cy.cfs.onedrive.ODCFSInstance;
import cy.cfs.op.AddFileOp;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

public class CFS {
	
	private static final String TAG = "CFS";
	
	private static Map<String, CFSConf> confTable = new HashMap<String, CFSConf>();
	public static Map<String, CFSInstance> cfsTable = new HashMap<String, CFSInstance>();
	
	public CFS(String userId, Context context){
		//TODO
		//get cfs conf and mapping rules from persistence/WS
		//String jsonCFSConfTableString = wsClient.getCFSConfTable(userId);
		//CFSConfTable confTable = CFSConfTable.fromJson(jsonCFSConfTableString);
		CFSConf googleConf1 = new CFSConf("g1", CFSConf.VENDOR_GOOGLE_DRIVE, "phillipchengyi@gmail.com", 10000000000l, "/readall");
		CFSConf msConf1 = new CFSConf("m1", CFSConf.VENDOR_MICROSOFT, "phillipchengyi@gmail.com", 10000000000l, "/readall");
		
		confTable.put(googleConf1.getId(), googleConf1);
		confTable.put(msConf1.getId(), msConf1);
		
		//TODO
		//implementation class will be configured and reflected to support multiple drive
		GDCFSInstance googleInstance1 = new GDCFSInstance(googleConf1, context);
		ODCFSInstance msInstance1 = new ODCFSInstance(msConf1, context);
		
		cfsTable.put(googleInstance1.getConf().getId(), googleInstance1);
		cfsTable.put(msInstance1.getConf().getId(), msInstance1);
	}
	
	public static CFSInstance getCFSInstanceById(String id){
		return cfsTable.get(id);
	}
	
	//parse the fileKey into cfsId and fileId
	private String[] parse(String fileKey, CFSMapper mapper){
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
	
	//
	public void asyncSaveImageFile(String fileKey, CFSMapper mapper, Bitmap bmp, 
			CallbackOp callback){
		String[] ret = parse(fileKey, mapper);
		if (ret!=null){
			CFSInstance cfsIns = getCFSInstanceById(ret[0]);
			if (cfsIns!=null){
				AddFileOp addFile = new AddFileOp(cfsIns);
				String fileName = cfsIns.getConf().getRootFolder() + ret[1];
				addFile.setFileName(fileName);
				addFile.setMimeType("image/png");
				addFile.setSize(bmp.getRowBytes() * bmp.getHeight());
				
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
				byte[] byteArray = stream.toByteArray();
				addFile.setBinaryContent(byteArray);
				if (callback!=null){
					addFile.addCallback(callback);
				}
				cfsIns.submit(addFile);
			}
		}
	}
	
	//
	public void asyncGetContent(String fileKey, CFSMapper mapper, CallbackOp callback){
		String[] ret = parse(fileKey, mapper);
		if (ret!=null){
			CFSInstance cfsIns = getCFSInstanceById(ret[0]);
			if (cfsIns!=null){
				
			}
		}
	}
}
