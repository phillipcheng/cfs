package cy.cfs;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.collections.set.ListOrderedSet;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


//the current mapping instance of the cfs
public abstract class CFSInstance {
	protected static final String TAG = "CFS";
	
	public static final String VENDOR_GOOGLE_DRIVE="google.drive";
	public static final String VENDOR_MICROSOFT="ms.onedrive";
	public static final String VENDOR_DROPBOX="dropbox";
	
	public static String[] vendors = new String[]{VENDOR_GOOGLE_DRIVE, VENDOR_MICROSOFT, VENDOR_DROPBOX};
	
	public static final String UNCONNECTED="disconnected";
	public static final String CONNECTING="connecting";
	public static final String CONNECTED="connected";
	public static final String UNKNOWN="unknown";
	
	
	public static final String CFS_ACTION_CONNECT="cfs.connect.action";
	public static final String INTENT_EXTRA_CFS_INSTANCE_ID="cfsInstanceId";
	public static final String INTENT_EXTRA_CFS_USER_ID="userId";
	

	public static final String ID_KEY="id";
	public static final String VENDOR_KEY="vendor";
	public static final String ACCOUNT_KEY="account";
	public static final String ROOT_FOLDER_KEY="rootFolder";
	public static final String MATCH_RULE="matchRule";
	
	private boolean checked;
	private String id;
	private String vendor;
	private String account;//usually email address
	private long quota; //number of bytes
	private String rootFolder;
	private long used; //in bytes
	private String status=UNCONNECTED;
	private String matchRule;
	
	private String userId;
	
	private transient ExecutorService exeService = Executors.newFixedThreadPool(2);
	private transient ListOrderedSet pendingList = new ListOrderedSet();
	private transient Context ctxt;//save for future intent firing, should be not necessary
	private transient ConnectionsGridViewAdapter adapter;
	
	//virtual file/dir name map to cloud specific resource id cache
	private ConcurrentHashMap<String, String> dirMap = new ConcurrentHashMap<String, String>();
	private ConcurrentHashMap<String, String> fileMap = new ConcurrentHashMap<String, String>();
	//worker map
	private ConcurrentHashMap<String, DriveOp> workerMap = new ConcurrentHashMap<String, DriveOp>();
	
	public static int getIdxVendor(String vendor){
		for (int i=0; i<vendors.length; i++){
			if (vendors[i].equals(vendor)){
				return i;
			}
		}
		return -1;
	}
	
	public JSONObject toJson() throws JSONException{
		JSONObject obj = new JSONObject();
		obj.put(ID_KEY, id);
		obj.put(VENDOR_KEY, this.vendor);
		obj.put(ACCOUNT_KEY, this.account);
		obj.put(ROOT_FOLDER_KEY, this.rootFolder);
		obj.put(MATCH_RULE, this.matchRule);
		return obj;
	}
	
	public static CFSInstance fromJson(JSONObject obj, String userId) throws JSONException{
		String vendor = obj.getString(VENDOR_KEY);
		String id = obj.getString(ID_KEY);
		CFSInstance cfs = OpFactory.getCFSInstance(id, vendor, userId);
		if (obj.has(ACCOUNT_KEY))
			cfs.setAccount(obj.getString(ACCOUNT_KEY));
		if (obj.has(ROOT_FOLDER_KEY))
			cfs.setRootFolder(obj.getString(ROOT_FOLDER_KEY));
		if (obj.has(MATCH_RULE))
			cfs.setMatchRule(obj.getString(MATCH_RULE));
		return cfs;
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("id:" + id);
		sb.append(",vendor:" + vendor);
		sb.append(",account:" + account);
		sb.append(",quota:" + quota);
		sb.append(",rootFolder:" + rootFolder);
		sb.append(",used:" + used);
		sb.append(",status:" + status);
		sb.append(",matchRule:" + matchRule);
		
		
		sb.append("\n dirMap:" + dirMap);
		sb.append("\n fileMap:" + fileMap);
		sb.append("\n workerMap:" + workerMap);
		
		return sb.toString();
		
	}
	
	public void removeWorker(DriveOp op){
		workerMap.remove(op.getId());
	}
	public void addWorker(DriveOp op){
		workerMap.put(op.getId(), op);
	}
	public DriveOp getWorker(String id){
		return workerMap.get(id);
	}
	
	public CFSInstance(String id, String userId){
		this.id = id;
		this.userId = userId;
	}

	public abstract boolean isConnected();
	public abstract void myConnect(Activity activity);//real connect
	public abstract void myDisconnect();
	
	//call back after cfs get connected
	protected void startPendingOp(){
		synchronized(pendingList){
			for (Object op : this.pendingList){
				this.submit((DriveOp) op);
			}
		}
	}

	//I am connected, called by my sub classes
	protected void getConnected(){
		Log.i(TAG, "connection connected.");
		setStatus(CFSInstance.CONNECTED);
        //fire all pending operations
        startPendingOp();
        if (adapter!=null)
        	adapter.notifyDataSetChanged();
	}
	
	public void connect(Activity activity, ConnectionsGridViewAdapter adapter){
		myConnect(activity);
		this.adapter = adapter;
	}
	
	public void disconnect(){
		myDisconnect();//this is synchronous
        setStatus(CFSInstance.UNCONNECTED);
	}
	
	//to trigger the manual connection
	private void showConnectActivity(Context context){
		//show connect activity
		if (!isConnected()){
			if (!CONNECTING.equals(getStatus())){
				//fire event
				Intent intent = new Intent();
				intent.setAction(CFS_ACTION_CONNECT);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra(INTENT_EXTRA_CFS_INSTANCE_ID, getId());
				intent.putExtra(INTENT_EXTRA_CFS_USER_ID, userId);
				context.startActivity(intent);
				setStatus(CONNECTING);
			}
		}
	}
	
	//internal function called by internally generated operations
	protected void submit(DriveOp op){
		if (ctxt==null){
			Log.e(TAG, "internal submit invoked without externally setting the context.");
		}else{
			submit(op, ctxt);
		}
	}
	
	//public interface for functions in cfs facade to call
	public void submit(DriveOp op, Context context){
		this.ctxt = context;
		if (isConnected()){
			exeService.submit(op);
		}else{
			synchronized(pendingList){
				if (pendingList.size()==0){
					showConnectActivity(context);
				}
				pendingList.add(op);
			}
			//onConnected callback to submit these pending operations
		}
	}
	
	//newOp is working on the fileName
	public void replaceOp(boolean isFile, String fileName, DriveOp newOp, String currentOpId){
		String oldId = null;
		if (!isFile){
			oldId = getDirMap().replace(fileName, DriveOp.WORKING_MARK + newOp.getId());
		}else{
			oldId = getFileMap().replace(fileName, DriveOp.WORKING_MARK + newOp.getId());
		}
		DriveOp oldOp=null;
		if (oldId!=null){
			if (oldId.contains(DriveOp.WORKING_MARK)){
				oldId = oldId.substring(DriveOp.WORKING_MARK.length());
				oldOp = getWorker(oldId);
			}else{
				//oldId is resouceId
				Log.e(TAG, String.format("error: resouceId %s replaced with op id %s, currentOpId:%s", 
						oldId, newOp.getId(), currentOpId));
			}
		}else{
			//equal to submit
		}
		
		
		//newOp.addCallbackList(oldOp.getCallback());//callback should be in the right order
		addWorker(newOp);
		if (oldOp!=null){
			removeWorker(oldOp);
		}
		submit(newOp);
		Log.i(TAG, String.format("replace op for file:%s from %s to %s", fileName, oldOp, newOp));
	}
	
	public void callback(boolean isSuccess, boolean isFile, String requestFileName, Object result){
		if (isSuccess){
			String resourceId = (String)result;
			if (isFile){
				getFileMap().put(requestFileName, resourceId);
			}else{
				getDirMap().put(requestFileName, resourceId);
			}
			Log.i(TAG, String.format("put to map: isFile %b, %s:%s",  isFile, requestFileName, result));
		}else{
			//do nothing, let client retry
		}
	}
	
	public ConcurrentHashMap<String, String> getDirMap(){
		return dirMap;
	}
	public ConcurrentHashMap<String, String> getFileMap(){
		return fileMap;
	}
	
	public long getUsed() {
		return used;
	}
	public void setUsed(long used) {
		this.used = used;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(final String status) {
		this.status = status;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getVendor() {
		return vendor;
	}
	public void setVendor(String vendor) {
		this.vendor = vendor;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public long getQuota() {
		return quota;
	}
	public void setQuota(long quota) {
		this.quota = quota;
	}
	public String getRootFolder() {
		return rootFolder;
	}
	public void setRootFolder(String rootFolder) {
		this.rootFolder = rootFolder;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getMatchRule() {
		return matchRule;
	}

	public void setMatchRule(String matchRule) {
		this.matchRule = matchRule;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}
	
}
