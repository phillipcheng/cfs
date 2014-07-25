package cy.cfs.googledrive;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

import cy.cfs.CFS;
import cy.cfs.CFSInstance;
import cy.cfs.R;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class GDConnectActivity extends Activity implements
	GoogleApiClient.ConnectionCallbacks, 
	GoogleApiClient.OnConnectionFailedListener{

	private static final String TAG = "GoogleDriveConnectActivity";
	private static final int REQUEST_CODE_RESOLUTION = 1;
	
	public static final String CFS_ACTION_GOOGLEAPICONNECT="cfs.action.googleAPIConnect";

	private TextView tvID;
	private TextView tvStatus;
	private String cfsId;
	private GDCFSInstance googleCFS;
	GoogleApiClient mGoogleApiClient;
	
	private void connectIfNot(){
		if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            googleCFS.setGoogleApiClient(mGoogleApiClient);
        }
        mGoogleApiClient.connect();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        setContentView(R.layout.connect_activity);

        Intent intent = getIntent();
        cfsId = intent.getStringExtra(CFSInstance.INTENT_EXTRA_CFS_INSTANCE_ID);
        tvID = (TextView) findViewById(R.id.txtCFSID);
        tvID.setText(cfsId);
        googleCFS = (GDCFSInstance)CFS.getCFSInstanceById(cfsId);
        mGoogleApiClient= googleCFS.getGoogleApiClient();
        
        tvStatus = (TextView) findViewById(R.id.txtStatus);
        tvStatus.setText(googleCFS.getStatus());
        
        connectIfNot();
	}
	
	@Override
    protected void onResume() {
        super.onResume();
        connectIfNot();
    }
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_RESOLUTION && resultCode == RESULT_OK) {
        	Log.i(TAG, "cfsInstanceId:" + cfsId);
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
            googleCFS.setStatus(CFSInstance.UNCONNECTED);
            tvStatus.setText(googleCFS.getStatus());
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
        googleCFS.setStatus(CFSInstance.CONNECTED);
        tvStatus.setText(googleCFS.getStatus());
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		Log.i(TAG, "connection suspended.");
	}

}
