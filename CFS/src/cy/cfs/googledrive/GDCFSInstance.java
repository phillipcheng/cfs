package cy.cfs.googledrive;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.plus.Plus;

import cy.cfs.CFSInstance;

public class GDCFSInstance extends CFSInstance implements
	GoogleApiClient.ConnectionCallbacks, 
	GoogleApiClient.OnConnectionFailedListener{

	public static final int REQUEST_CODE_RESOLUTION = 1;
	public static final int REQUEST_CODE_AUTH = 2;
	
	private Activity activity;
	private transient GoogleApiClient mGoogleApiClient;
	
	public GDCFSInstance(String id, String userId) {
		super(id, userId);
		this.setVendor(CFSInstance.VENDOR_GOOGLE_DRIVE);
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
	public void myConnect(Activity activity) {
		//Account curAccnt = new Account(this.getAccount(), GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
		Intent intent = AccountPicker.newChooseAccountIntent(null, null, 
	               new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, false, null, null, null, null);
	    activity.startActivityForResult(intent, REQUEST_CODE_AUTH);
	}
	
	public void myConnect2(Activity activity){
		this.activity = activity;
		Intent intent = activity.getIntent();
		Log.w(TAG, "intent in login:" + intent.toString());
		Log.w(TAG, "intent's extra:" + intent.getExtras());
		if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(activity)
                    .addApi(Drive.API)
                    .addApi(Plus.API)
                    .addScope(Drive.SCOPE_FILE)
                    .setAccountName(this.getAccount())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
		mGoogleApiClient.connect();
	}

	@Override
	public void myDisconnect() {
		Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
        mGoogleApiClient.disconnect();
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
		getConnected();
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		
	}
	
	public GoogleApiClient getGoogleApiClient() {
		return mGoogleApiClient;
	}

	public void setGoogleApiClient(GoogleApiClient mGoogleApiClient) {
		this.mGoogleApiClient = mGoogleApiClient;
	}

}
