package cy.cfs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

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
		
		TextView txtCFSId = (TextView) row.findViewById(R.id.txtCFSID);
		txtCFSId.setText(cfsInstance.getId());
		TextView txtStatus = (TextView) row.findViewById(R.id.txtStatus);
		txtStatus.setText(cfsInstance.getStatus());
		TextView txtEmail = (TextView) row.findViewById(R.id.txtEmailID);
		txtEmail.setText(cfsInstance.getAccount());
		TextView txtRootDir = (TextView) row.findViewById(R.id.txtRootDir);
		txtRootDir.setText(cfsInstance.getRootFolder());
		
		Button btnConnect = (Button) row.findViewById(R.id.btnConnect);
		
		btnConnect.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				//check cfsInstance
				if (CFSInstance.CONNECTED.equals(cfsInstance.getStatus())){
					//disconnect
					cfsInstance.disconnect();
				}else if (CFSInstance.UNCONNECTED.equals(cfsInstance.getStatus())||
						CFSInstance.UNKNOWN.equals(cfsInstance.getStatus())){
					//connect
					cfsInstance.connect(activity);
				}
			}
		});
		
		return row;
	}
}