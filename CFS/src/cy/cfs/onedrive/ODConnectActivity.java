package cy.cfs.onedrive;


import java.util.Arrays;

import com.microsoft.live.LiveAuthClient;
import com.microsoft.live.LiveAuthException;
import com.microsoft.live.LiveAuthListener;
import com.microsoft.live.LiveConnectClient;
import com.microsoft.live.LiveConnectSession;
import com.microsoft.live.LiveStatus;

import cy.cfs.CFS;
import cy.cfs.CFSInstance;
import cy.cfs.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class ODConnectActivity extends Activity implements LiveAuthListener {

	private static final String TAG = "ODConnectActivity";
	
	public static final String CFS_ACTION_ONE_DRIVE_CONNECT="cfs.action.oneDrive.APIConnect";
	
	private TextView tvID;
	private TextView tvStatus;
	private String cfsId;
    private TextView resultTextView;   
    
    private ODCFSInstance msCFS;
	private LiveAuthClient auth;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connect_activity);
        this.resultTextView = (TextView)findViewById(R.id.resultTextView);
        auth=new LiveAuthClient(this, ODCFSInstance.APP_CLIENT_ID);
    }

    @Override
    protected void onStart() {
        super.onStart();
        
        Intent intent = getIntent();
        cfsId = intent.getStringExtra(CFSInstance.INTENT_EXTRA_CFS_INSTANCE_ID);
        tvID = (TextView) findViewById(R.id.txtCFSID);
        tvID.setText(cfsId);
        msCFS = (ODCFSInstance)CFS.getCFSInstanceById(cfsId);
        
        tvStatus = (TextView) findViewById(R.id.txtStatus);
        tvStatus.setText(msCFS.getStatus());
        
        Iterable<String> scopes = Arrays.asList(Scopes.BASICS);
        auth.login(this, scopes, this);
    }
    
    public void onAuthComplete(LiveStatus status, LiveConnectSession session, Object userState) {
        if(status == LiveStatus.CONNECTED) {
            this.resultTextView.setText("Signed in.");
            msCFS.setClient(new LiveConnectClient(session));
        }
        else {
            this.resultTextView.setText("Not signed in.");
            msCFS.setClient(null);
        }        
    }

    public void onAuthError(LiveAuthException exception, Object userState) {
        this.resultTextView.setText("Error signing in: " + exception.getMessage());        
        msCFS.setClient(null);  
    }	

}
