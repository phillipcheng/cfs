package cy.cfs.googledrive;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.content.Intent;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.DriveId;

import cy.cfs.AddDirOp;
import cy.cfs.AddFileOp;
import cy.cfs.CFSConf;
import cy.cfs.CFSInstance;
import cy.cfs.DriveOp;

public class GDCFSInstance extends CFSInstance{

	private static final String TAG = "GoogleDriveCFSInstance";
	
	private transient GoogleApiClient mGoogleApiClient;
	public GoogleApiClient getGoogleApiClient() {
		return mGoogleApiClient;
	}

	public void setGoogleApiClient(GoogleApiClient mGoogleApiClient) {
		this.mGoogleApiClient = mGoogleApiClient;
	}

	private transient Context context;
	private transient DriveId rootDriverId;
	
	private String rootResourceId;
	//virtual file name map to cloud specific resource id, the display name in the cloud the same as virtual file name
	private Map<String, DriveId> dirMap = new ConcurrentHashMap<String, DriveId>();
	private Map<String, String> fileMap = new ConcurrentHashMap<String, String>();
	
	public Map<String, DriveId> getDirMap(){
		return dirMap;
	}
	
	public GDCFSInstance(CFSConf conf, Context context) {
		super(conf);
		this.context = context;
	}

	//for CFSInstance
	@Override
	public boolean isConnected() {
		if (mGoogleApiClient!=null && mGoogleApiClient.isConnected()){
			return true;
		}
		return false;
	}

	@Override
	public void connect() {
		if (mGoogleApiClient!=null && mGoogleApiClient.isConnected()){
			return;
		}else{
			//fire event
			Intent intent = new Intent();
			intent.setAction(GDConnectActivity.CFS_ACTION_GOOGLEAPICONNECT);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra(GDConnectActivity.INTENT_EXTRA_CFS_INSTANCE_ID, getConf().getId());
			context.startActivity(intent);
		}
	}

	@Override
	public void processOp(DriveOp dop) {
		if (dop instanceof AddFileOp){//add file operation
			AddFileOp addFile = (AddFileOp)dop;
			String requestFileName = addFile.getFileName();
			String dirName = requestFileName.substring(0, requestFileName.lastIndexOf(File.separator));
			String fileName = requestFileName.substring(dirName.length());
			if (!dirMap.containsKey(dirName)){
				//change to a AddDirOp
				AddDirOp addDir = new AddDirOp();
				addDir.setFileName(dirName);
				addDir.addCallback(new GDProcessOpCallback(addFile, this));
				processOp(addDir);
			}else{
				DriveId did = dirMap.get(dirName);
				GDCreateFileInFolderOp gdcfifop = new GDCreateFileInFolderOp(requestFileName, 
						did, fileName, addFile.getMimeType(), addFile.getBinaryContent(), this);
				gdcfifop.addCallback(new GDCreateItemCallback(requestFileName, this));
				gdcfifop.addCallbackList(dop.getCallback());
				gdcfifop.process();
			}
		}else if (dop instanceof AddDirOp){
			// directory sample: /this/is/a/directory
			AddDirOp addDir = (AddDirOp)dop;
			String dirName = addDir.getFileName();
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
				//check folder
				if (!dirMap.containsKey(preString)){
					GDCreateFolderInFolderOp gdcfifop = new GDCreateFolderInFolderOp(
							preString, preResourceId, t, this);
					gdcfifop.addCallback(new GDCreateItemCallback(preString, this));
					if (i<tokens.size()-1){
						gdcfifop.addCallback(new GDProcessOpCallback(addDir, this));
					}else{
						//last
						gdcfifop.addCallbackList(dop.getCallback());
					}
					gdcfifop.process();
					break;//since process is async, we need to put follow-up actions in the callback.
				}else{
					preResourceId = dirMap.get(preString).getResourceId();
				}
			}
		}else if (dop.getOpCode()==DriveOp.OP_GET_FILE){//get file operation
			//find the resource id
			//get it
		}
	}
}
