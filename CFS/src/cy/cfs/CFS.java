package cy.cfs;


import java.io.ByteArrayOutputStream;

import cy.cfs.googledrive.AddFileOp;
import cy.cfs.googledrive.GDCFSInstance;
import android.content.Context;
import android.graphics.Bitmap;

public class CFS {
	
	private static final String TAG = "CFS";
	
	private CFSTable cfsTable;//file mapping table
	
	private CFSTable getCFSTable(String userId, Context context){
		//get cfs conf table from persistence/WS
		//String jsonCFSConfTableString = wsClient.getCFSConfTable(userId);
		//CFSConfTable confTable = CFSConfTable.fromJson(jsonCFSConfTableString);
		CFSConf googleConf = new CFSConf("g1", CFSConf.VENDOR_GOOGLE_DRIVE, "phillipchengyi@gmail.com", 10000000000l, "/readall");
		CFSConfTable confTable = new CFSConfTable();
		confTable.put(googleConf.getId(), googleConf);
		
		//get instance cfs table from persistence/WS
		//String jsonCFSTableString = wsClient.getCFSTable(userId);
		//CFSTable cfsTable = CFSTable.fromJson(jsonCFSTableString);
		GDCFSInstance googleInstance = new GDCFSInstance(googleConf, context);
		cfsTable = new CFSTable();
		cfsTable.putInstance(googleInstance);
		return cfsTable;
	}
	
	public CFS(String userId, Context context){
		cfsTable = getCFSTable(userId, context);
	}
	
	//
	public void asyncSaveImageFile(String fileKey, Bitmap bmp, CFSSaveFilesCallback callback){
		long size = bmp.getRowBytes() * bmp.getHeight();
		CFSInstance cfsIns = cfsTable.getFitInstance(size);
		AddFileOp addFile = new AddFileOp(cfsIns);
		addFile.setFileName(fileKey);
		addFile.setMimeType("image/png");
		addFile.setSize(size);
		
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
		byte[] byteArray = stream.toByteArray();
		addFile.setBinaryContent(byteArray);
		if (callback!=null){
			addFile.addCallback(callback);
		}
		cfsIns.submit(addFile);
	}
	
	//
	public void asyncGetContent(String file, CFSGetContentCallback callback){
		
	}
	

}
