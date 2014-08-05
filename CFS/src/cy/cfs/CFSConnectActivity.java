package cy.cfs;


import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;

import cy.cfs.CFS;
import cy.cfs.CFSInstance;
import cy.cfs.R;
import cy.cfs.googledrive.GDCFSInstance;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
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
        instanceMap = CFS.getCFSInstances(userId, this.getApplicationContext());
        cfsInstanceId = intent.getStringExtra(CFSInstance.INTENT_EXTRA_CFS_INSTANCE_ID);
        
        //just to show it, functionally not needed
        ListView lv = (ListView) findViewById(R.id.connectionListView);
        cfsAdapter = new ConnectionsGridViewAdapter(this, R.layout.cloud_connect_cell);
        cfsAdapter.addAll(instanceMap.values());
        lv.setAdapter(cfsAdapter);
        
        Button addBtn = (Button) findViewById(R.id.btnAddCfs);
        addBtn.setOnClickListener(new OnClickListener(){
        	@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(CFSConnectActivity.this);
			    builder.setTitle(R.string.select_vendor)
			           .setItems(R.array.cloudVendorList, new DialogInterface.OnClickListener() {
			               public void onClick(DialogInterface dialog, int which) {
			            	   String id = (instanceMap.size()+1) + "";
			            	   String vendor = CFSInstance.vendors[which];
			            	   CFSInstance inst = OpFactory.getCFSInstance(id, vendor, userId);
			            	   instanceMap.put(id, inst);
			            	   cfsAdapter.add(inst);
			            	   cfsAdapter.notifyDataSetChanged();
			               }
			    });
			    AlertDialog alert = builder.create();
			    alert.show();
			}});
        
        Button removeBtn = (Button)findViewById(R.id.btnDeleteCfs);
        removeBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				for (CFSInstance cfsInst:instanceMap.values()){
					if (cfsInst.isChecked()){
						instanceMap.remove(cfsInst.getId());
						cfsAdapter.remove(cfsInst);
					}
				}
				cfsAdapter.notifyDataSetChanged();
			}});
        
        Button saveBtn = (Button) findViewById(R.id.btnSaveToServer);
        saveBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				//save to preference
				JSONArray jarray = new JSONArray();
				for (CFSInstance cfs:instanceMap.values()){
					try {
						jarray.put(cfs.toJson());
					} catch (JSONException e) {
						Log.e(TAG, "", e);
					}
				}
				SharedPreferences prefs = getApplicationContext().getSharedPreferences(CFS.PREF_KEY_CFS, Context.MODE_PRIVATE);
				String strJarray = jarray.toString();
				String uid = CFS.getUserId(userId);
				prefs.edit().putString(uid, strJarray).apply();
				Log.i(TAG, "uid:" + uid + ",strJarray:" + strJarray);
			}});
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
        	if (cfsInstance!=null){
        		cfsInstance.connect(this, cfsAdapter);
        	}else{
        		Log.e(TAG, String.format("%s not found in instanceMap %s", cfsInstanceId, instanceMap));
        	}
        }else if (requestCode == GDCFSInstance.REQUEST_CODE_AUTH && resultCode==RESULT_OK){
        	GDCFSInstance gcfsInstance = (GDCFSInstance) instanceMap.get(cfsInstanceId);
        	if (gcfsInstance!=null){
            	String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            	Log.w(TAG, String.format("cfs id %s corresponding account name is %s", cfsInstanceId, accountName));
        		gcfsInstance.setAccount(accountName);
        		gcfsInstance.myConnect2(this);
        	}else{
        		Log.e(TAG, String.format("%s not found in instanceMap %s", cfsInstanceId, instanceMap));
        	}
        }
    }
	
	public String getCfsInstanceId() {
		return cfsInstanceId;
	}

	public void setCfsInstanceId(String cfsInstanceId) {
		this.cfsInstanceId = cfsInstanceId;
	}
}
