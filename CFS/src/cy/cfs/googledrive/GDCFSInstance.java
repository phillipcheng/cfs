package cy.cfs.googledrive;

import android.app.Activity;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

import cy.cfs.CFSInstance;

public class GDCFSInstance extends CFSInstance implements
	GoogleApiClient.ConnectionCallbacks, 
	GoogleApiClient.OnConnectionFailedListener{


	public static final int REQUEST_CODE_RESOLUTION = 1;
	private Activity activity;
	
	private transient GoogleApiClient mGoogleApiClient;
	public GoogleApiClient getGoogleApiClient() {
		return mGoogleApiClient;
	}

	public void setGoogleApiClient(GoogleApiClient mGoogleApiClient) {
		this.mGoogleApiClient = mGoogleApiClient;
	}

	public GDCFSInstance(String id, String vendor, String account, long quota, String rootFolder, String userId) {
		super(id, vendor, account, quota, rootFolder, userId);
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
	public void connect(Activity activity) {
		this.activity = activity;
		if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(activity)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        mGoogleApiClient.connect();
		
	}

	@Override
	public void disconnect() {
        mGoogleApiClient.disconnect();
        setStatus(CFSInstance.UNCONNECTED);
        mGoogleApiClient=null;
	}
	
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // show the localized error dialog.
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), activity, 0).show();
            setStatus(CFSInstance.UNCONNECTED);
            return;
        }
        try {
        	//i need to put extra (cfsinstanceId) into this intent, so the resolution activity knows how to call back
            result.startResolutionForResult(activity, REQUEST_CODE_RESOLUTION);
        } catch (SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
		
	}

	@Override
	public void onConnected(Bundle arg0) {
		Log.i(TAG, "connection connected.");
        setStatus(CFSInstance.CONNECTED);
        //fire all pending operations
        startPendingOp();
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		
	}

}
