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
	
	//virtual file name map to cloud specific resource id, the display name in the cloud the same as virtual file name
	private ConcurrentHashMap<String, String> dirMap = new ConcurrentHashMap<String, String>();//since i need null value for DriveId placeholder
	private ConcurrentHashMap<String, String> fileMap = new ConcurrentHashMap<String, String>();
	
	public ConcurrentHashMap<String, String> getDirMap(){
		return dirMap;
	}
	public ConcurrentHashMap<String, String> getFileMap(){
		return fileMap;
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
}
