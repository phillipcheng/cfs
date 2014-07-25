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
	
	public GDCFSInstance(CFSConf conf, Context context) {
		super(conf);
		this.context = context;
	}

	//for CFSInstance
	@Override
	public boolean isConnected() {
		if (mGoogleApiClient!=null && mGoogleApiClient.isConnected()){
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
				intent.setAction(GDConnectActivity.CFS_ACTION_GOOGLEAPICONNECT);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra(INTENT_EXTRA_CFS_INSTANCE_ID, getConf().getId());
				context.startActivity(intent);
				setStatus(CONNECTING);
			}
		}
	}
}
