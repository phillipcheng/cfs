package cy.cfs;


import java.util.Map;

import cy.cfs.CFS;
import cy.cfs.CFSInstance;
import cy.cfs.R;
import cy.cfs.googledrive.GDCFSInstance;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

public class CFSConnectActivity extends Activity {

	private static final String TAG = "CFSConnectActivity";
	
	private String cfsInstanceId;
	private String userId;
	Map<String, CFSInstance> instanceMap;
	ConnectionsGridViewAdapter cfsAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        setContentView(R.layout.connect_activity);
        
        Intent intent = getIntent();
        userId = intent.getStringExtra(CFSInstance.INTENT_EXTRA_CFS_USER_ID);
        instanceMap = CFS.getCFSInstances(userId);
        cfsInstanceId = intent.getStringExtra(CFSInstance.INTENT_EXTRA_CFS_INSTANCE_ID);
        
        //just to show it, functionally not needed
        ListView lv = (ListView) findViewById(R.id.connectionListView);
        cfsAdapter = new ConnectionsGridViewAdapter(this, R.layout.cloud_connect_cell);
        cfsAdapter.addAll(instanceMap.values());
        lv.setAdapter(cfsAdapter);
        
        if (cfsInstanceId!=null){//connect to this instance
	        CFSInstance cfsInst = instanceMap.get(cfsInstanceId);
	        if (cfsInst!=null){
	        	cfsInst.connect(this);
	        }else{
	        	Log.e(TAG, "cfs not found:" + cfsInstanceId);
	        }
        }
	}
	
	@Override
    protected void onResume() {
        super.onResume();
    }
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GDCFSInstance.REQUEST_CODE_RESOLUTION && resultCode == RESULT_OK) {
        	CFSInstance cfsInstance = instanceMap.get(cfsInstanceId);
        	cfsInstance.connect(this);
        }
    }
}
