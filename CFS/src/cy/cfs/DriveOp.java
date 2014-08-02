package cy.cfs;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.util.Log;

public abstract class DriveOp implements Runnable, CallbackOp{
	
	protected static final String TAG = "CFS";
	protected static final String STATUSTAG = "CFSStatus";
	
	//following are marks might found on the entry of the dirMap and fileMap
	public static final String WORKING_MARK="working:";
	public static final String ERROR_MARK="error:";
	public static final String EMPTY_MARK="empty:";
	
	protected CFSInstance cfsInst;
	private String id;	
	private Object request;//this is the request

	public Object getRequest() {
		return request;
	}

	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("\nclass:" + this.getClass());
		sb.append(",id:" + id);
		sb.append(",cfs conf id:" + cfsInst.getId());
		sb.append(", \n callback list:" + getCallback());
		return sb.toString();
	}
	
	public DriveOp(CFSInstance cfsInstance, Object request){
		this.cfsInst = cfsInstance;
		id = File.separator + (new Date()).getTime();
		this.request = request;
	}
	
	//callback in the calling order, only the last can be asynchronous, the other should be synchronous
	private List<CallbackOp> callbackList = new ArrayList<CallbackOp>(); 
	
	public List<CallbackOp> getCallback() {
		return callbackList;
	}
	public void insertCallback(CallbackOp callback){
		callbackList.add(0, callback);
	}
	public void addCallback(CallbackOp callback) {
		this.callbackList.add(callback);
	}
	public void addCallbackList(List<CallbackOp> callbacks) {
		this.callbackList.addAll(callbacks);
	}
	public void insertCallbackList(List<CallbackOp> callbacks){
		this.callbackList.addAll(0, callbacks);
	}

	@Override
	public void onSuccess(Object reqeust, Object result){
		run();
	}
	@Override
	public void onFailure(Object request, Object result){
		//skip run, call back right now
		usrCallback(false, result);
	}
	
	
	/**
     * one callback, call system and user, can be separated into 2 below
     * @param isSuccess
     * @param isFile
     * @param result: resourceId or error message
     */
    public void finalCallback(boolean isSuccess, boolean isFile, String requestFileName, String result){
    	Log.i(TAG, String.format("callback success:%b, isFile:%b, requestFile:%s, result:%s", 
    			isSuccess, isFile, requestFileName, result));
    	sysCallback(isSuccess, isFile, requestFileName, result);
    	usrCallback(isSuccess, result);
    }
    
    //system call back, must call
    public void sysCallback(boolean isSuccess, boolean isFile, String requestFileName, String result){
		cfsInst.callback(isSuccess, isFile, requestFileName, result);
		cfsInst.removeWorker(this);
		Log.d(STATUSTAG, "cfsInst:\n" + cfsInst.toString());
	}
    
	/**
     * user call back
     * @param isSuccess
     * @param result: resourceId or error message
     */
    public void usrCallback(boolean isSuccess, Object result){
    	CallbackOp cbOp;
    	if (isSuccess){
    		List<CallbackOp> cbList = getCallback();
            for (int i=0; i<cbList.size(); i++){//using (cb:cbList) will have ConcurrentModificationException
            	cbOp = cbList.get(i);
            	cbOp.onSuccess(request, result);
            }
    	}else{
    		//
    		List<CallbackOp> cbList = getCallback();
    		for (int i=0; i<cbList.size(); i++){
    			cbOp = cbList.get(i);
            	cbOp.onFailure(request, result);
            }
    	}
    }
    
    
	public CFSInstance getCfsInst() {
		return cfsInst;
	}	
	public String getId() {
		return id;
	}
}
