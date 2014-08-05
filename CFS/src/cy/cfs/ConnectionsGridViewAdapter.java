package cy.cfs;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

public class ConnectionsGridViewAdapter extends ArrayAdapter<CFSInstance> {
	
	private static final String TAG = "ReadingGridViewAdapter";
	
	private Activity activity;
	private int layoutResourceId;
	
	public ConnectionsGridViewAdapter(Activity context, int layoutResourceId) {
		super(context, layoutResourceId);
		this.layoutResourceId = layoutResourceId;
		this.activity = context;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		final CFSInstance cfsInstance = getItem(position);
		
		LayoutInflater inflater = ((Activity) activity).getLayoutInflater();
		row = inflater.inflate(layoutResourceId, parent, false);
		
		
		CheckBox cbCFSId = (CheckBox) row.findViewById(R.id.cbCfs);
		cbCFSId.setText(cfsInstance.getId());
		cbCFSId.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {	
				cfsInstance.setChecked(isChecked);
			}
		});
		
		TextView txtStatus = (TextView) row.findViewById(R.id.txtStatus);
		txtStatus.setText(cfsInstance.getStatus());
		
		TextView txtEmail = (TextView) row.findViewById(R.id.txtEmailID);
		txtEmail.setText(cfsInstance.getAccount());
		
		final EditText txtRootDir = (EditText) row.findViewById(R.id.txtRootDir);
		txtRootDir.setText(cfsInstance.getRootFolder());
		txtRootDir.setOnFocusChangeListener(new OnFocusChangeListener(){
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				cfsInstance.setRootFolder(txtRootDir.getText().toString());
	        	Log.i(TAG, "root folder get:" + txtRootDir.getText().toString());
			}});
		
		final EditText txtMatchRule = (EditText) row.findViewById(R.id.txtMatchRule);
		txtMatchRule.setText(cfsInstance.getMatchRule());
		txtMatchRule.addTextChangedListener(new TextWatcher(){
	        public void afterTextChanged(Editable s) {
	        	cfsInstance.setMatchRule(s.toString());
	        	Log.i(TAG, "match rule get:" + s.toString());
	        	txtMatchRule.requestFocus();
	        }
	        public void beforeTextChanged(CharSequence s, int start, int count, int after){}
	        public void onTextChanged(CharSequence s, int start, int before, int count){}
	    }); 
		
		TextView txtVendor = (TextView) row.findViewById(R.id.txtVendor);
		txtVendor.setText(cfsInstance.getVendor());
		
		Button btnConnect = (Button) row.findViewById(R.id.btnConnect);
		btnConnect.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				//check cfsInstance
				((CFSConnectActivity)activity).setCfsInstanceId(cfsInstance.getId());
				
				if (CFSInstance.CONNECTED.equals(cfsInstance.getStatus())){
					//disconnect
					cfsInstance.disconnect();
				}else{
					//connect
					cfsInstance.connect(activity, ConnectionsGridViewAdapter.this);
				}
			}
		});
		
		return row;
	}
}