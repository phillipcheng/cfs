package cy.cfs.googledrive;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

import cy.cfs.CFSTable;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.util.Log;

public class GDConnectActivity extends Activity implements
	GoogleApiClient.ConnectionCallbacks, 
	GoogleApiClient.OnConnectionFailedListener{

	private static final String TAG = "GoogleDriveConnectActivity";
	private static final int REQUEST_CODE_RESOLUTION = 1;
	
	public static final String INTENT_EXTRA_CFS_INSTANCE_ID="cfsInstanceId";
	public static final String CFS_ACTION_GOOGLEAPICONNECT="cfs.action.googleAPIConnect";
	private String cfsInstanceId;
	
	
	@Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        cfsInstanceId = intent.getStringExtra(INTENT_EXTRA_CFS_INSTANCE_ID);
        Log.i(TAG, "cfsInstanceId:" + cfsInstanceId);
        GDCFSInstance googleCFS = (GDCFSInstance)CFSTable.instanceMap.get(cfsInstanceId);
        GoogleApiClient mGoogleApiClient = googleCFS.getGoogleApiClient();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addScope(Drive.SCOPE_APPFOLDER) // required for App Folder sample
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            googleCFS.setGoogleApiClient(mGoogleApiClient);
        }
        mGoogleApiClient.connect();
    }
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_RESOLUTION && resultCode == RESULT_OK) {
			GDCFSInstance googleCFS = (GDCFSInstance)CFSTable.instanceMap.get(cfsInstanceId);
			GoogleApiClient mGoogleApiClient = googleCFS.getGoogleApiClient();
        	Log.i(TAG, "cfsInstanceId:" + cfsInstanceId);
        	if (mGoogleApiClient!=null)
        		mGoogleApiClient.connect();
        	else{
        		Log.e(TAG,"google api client should be put to the static map before send intent.");
        	}
        }
    }
    
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // show the localized error dialog.
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this, 0).show();
            return;
        }
        try {
        	//i need to put extra (cfsinstanceId) into this intent, so the resolution activity knows how to call back
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
	}

	@Override
	public void onConnected(Bundle arg0) {
		Log.i(TAG, "connection connected.");
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		Log.i(TAG, "connection suspended.");
		
	}

}
