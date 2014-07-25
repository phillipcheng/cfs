package cy.cfs.onedrive;

import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.content.Intent;

import com.microsoft.live.LiveAuthClient;
import com.microsoft.live.LiveConnectClient;

import cy.cfs.CFSConf;
import cy.cfs.CFSInstance;

public class ODCFSInstance extends CFSInstance{

	private static final String TAG = "GoogleDriveCFSInstance";

	private transient Context context;
	
	public static final String APP_CLIENT_ID="00000000401250E1";
    private LiveConnectClient client;
    
	//virtual file name map to cloud specific resource id cache
	private ConcurrentHashMap<String, String> dirMap = new ConcurrentHashMap<String, String>();//since i need null value for DriveId placeholder
	private ConcurrentHashMap<String, String> fileMap = new ConcurrentHashMap<String, String>();
	
	public ConcurrentHashMap<String, String> getDirMap(){
		return dirMap;
	}
	public ConcurrentHashMap<String, String> getFileMap(){
		return fileMap;
	}
	
	public ODCFSInstance(CFSConf conf, Context context) {
		super(conf);
		this.context = context;
	}

	//for CFSInstance
	@Override
	public boolean isConnected() {
		if (client!=null){
			setStatus(CONNECTED);
			return true;
		}
		return false;
	}

	@Override
	public void connect() {
		if (!isConnected()){
			if (!CONNECTING.equals(getStatus())){
				//fire event
				Intent intent = new Intent();
				intent.setAction(ODConnectActivity.CFS_ACTION_ONE_DRIVE_CONNECT);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra(CFSInstance.INTENT_EXTRA_CFS_INSTANCE_ID, getConf().getId());
				context.startActivity(intent);
				setStatus(CONNECTING);
			}
		}
	}
	
	public LiveConnectClient getClient() {
		return client;
	}
	public void setClient(LiveConnectClient client) {
		this.client = client;
	}
}
