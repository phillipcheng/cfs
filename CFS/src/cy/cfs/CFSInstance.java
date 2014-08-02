package cy.cfs;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.collections.set.ListOrderedSet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


//the current mapping instance of the cfs
public abstract class CFSInstance {
	protected static final String TAG = "CFS";
	
	public static final String VENDOR_GOOGLE_DRIVE="google.drive";
	public static final String VENDOR_DROPBOX="dropbox";
	public static final String VENDOR_MICROSOFT="ms.onedrive";
	
	public static final String UNCONNECTED="disconnected";
	public static final String CONNECTING="connecting";
	public static final String CONNECTED="connected";
	public static final String UNKNOWN="unknown";
	
	
	public static final String CFS_ACTION_CONNECT="cfs.connect.action";
	public static final String INTENT_EXTRA_CFS_INSTANCE_ID="cfsInstanceId";
	public static final String INTENT_EXTRA_CFS_USER_ID="userId";
	
	private String id;
	private String vendor;
	private String account;//usually email address
	private long quota; //number of bytes
	private String rootFolder;
	private long used; //in bytes
	private String status=UNCONNECTED;
	private String userId;
	
	private transient ExecutorService exeService = Executors.newFixedThreadPool(2);
	private transient ListOrderedSet pendingList = new ListOrderedSet();
	private transient Context ctxt;//save for future intent firing, should be not necessary
	
	//virtual file/dir name map to cloud specific resource id cache
	private ConcurrentHashMap<String, String> dirMap = new ConcurrentHashMap<String, String>();
	private ConcurrentHashMap<String, String> fileMap = new ConcurrentHashMap<String, String>();
	//worker map
	private ConcurrentHashMap<String, DriveOp> workerMap = new ConcurrentHashMap<String, DriveOp>();
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("id:" + id);
		sb.append(",vendor:" + vendor);
		sb.append(",account:" + account);
		sb.append(",quota:" + quota);
		sb.append(",rootFolder:" + rootFolder);
		sb.append(",used:" + used);
		sb.append(",status:" + status);
		
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
	
	public CFSInstance(String id, String vendor, String account, long quota, String rootFolder, String userId){
		this.id = id;
		this.vendor = vendor;
		this.account = account;
		this.quota = quota;
		this.setRootFolder(rootFolder);
		this.used = 0;
		this.userId = userId;
	}

	public abstract boolean isConnected();
	public abstract void connect(Activity activity);//real connect
	public abstract void disconnect();
	
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
	
	//call back after cfs get connected
	protected void startPendingOp(){
		synchronized(pendingList){
			for (Object op : this.pendingList){
				this.submit((DriveOp) op);
			}
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

	public void setStatus(String status) {
		this.status = status;
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
	
}
