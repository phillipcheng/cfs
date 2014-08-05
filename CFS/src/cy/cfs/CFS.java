package cy.cfs;


import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cy.cfs.googledrive.GDCFSInstance;
import cy.cfs.onedrive.ODCFSInstance;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;

public class CFS {
	
	private static final String TAG = "CFS";
	
	public static final String PREF_KEY_CFS = "CFS";
	
	private static String uid;
	private static LinkedHashMap<String, CFSInstance> cfsTable;//uid's corresponding cfsTable (keep insertion order)
	
	
	public static CFSInstance getCFSInstanceById(String cfsid){
		return cfsTable.get(cfsid);
	}
	
	private static String getDriveId(String userId, String fileKey, Context context){
		Map<String, CFSInstance> table = getCFSInstances(userId, context);
		Iterator<CFSInstance> cfsIt = table.values().iterator();
		CFSInstance lastCfsInst = null;
		while (cfsIt.hasNext()){
			lastCfsInst = cfsIt.next();
		}
		String cfsDriveId = lastCfsInst.getId(); //set the cfs
		for (CFSInstance cfsInst:table.values()){
			if (fileKey.startsWith(cfsInst.getMatchRule())){
				cfsDriveId = cfsInst.getId();
				break;
			}
		}
		Log.i(TAG, String.format("file %s's driveId is %s", fileKey, cfsDriveId));
		return cfsDriveId;
	}
	
	public static String getUserId(String userId){
		if (userId==null){//mock
			userId = "empty";
		}
		return userId;
		
	}
	public static Map<String, CFSInstance> getCFSInstances(String userId, Context context){
		userId = getUserId(userId);
		
		if (!userId.equals(uid)){//switch user
			uid = userId;
			cfsTable = new LinkedHashMap<String, CFSInstance>();
			//get from shared preference
			SharedPreferences prefs = context.getSharedPreferences(PREF_KEY_CFS, Context.MODE_PRIVATE);
			String jsonCFS = prefs.getString(userId, null);
			if (jsonCFS!=null){
				try {
					Log.i(TAG, "jsonCFS got:" + jsonCFS);
					JSONArray jarray = new JSONArray(jsonCFS);
					for (int i=0; i<jarray.length(); i++){
						JSONObject jobj = (JSONObject) jarray.get(i);
						CFSInstance inst = CFSInstance.fromJson(jobj, userId);
						cfsTable.put(inst.getId(), inst);
					}
				} catch (JSONException e) {
					Log.e(TAG, "", e);
				}
			}
		}
		return cfsTable;
	}
	//
	public static void asyncSaveImageFile(String userId, String fileKey, Bitmap bmp, 
			CallbackOp callback, Context context){
		String driveId = getDriveId(userId, fileKey, context);
		CFSInstance cfsIns = getCFSInstanceById(driveId);
		if (cfsIns!=null){
			String fileName = cfsIns.getRootFolder() + fileKey;
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
	
	//
	public static void asyncGetContent(String userId, String fileKey, Object request, 
			CallbackOp callback, Context context, int width, int height){
		String driveId = getDriveId(userId, fileKey, context);
		CFSInstance cfsIns = getCFSInstanceById(driveId);
		if (cfsIns!=null){
			String fileName = cfsIns.getRootFolder() + fileKey;
			GetFileOp getFile = new GetFileOp(cfsIns, fileName, request, width, height);
			getFile.addCallback(callback);
			cfsIns.submit(getFile, context);
		}
	}
}
