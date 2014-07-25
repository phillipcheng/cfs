package cy.cfs;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.util.Log;

public abstract class DriveOp implements Runnable, CallbackOp{
	
	protected static final String TAG = "DriveOp";
	
	public static final String WORKING_MARK="working:";
	public static final String ERROR_MAKE="error:";
	
	public static int OP_ADD_FILE=1;
	public static int OP_GET_FILE=2;
	public static int OP_ADD_DIR=3;
	public static int OP_DEL_FILE=4;
	public static int OP_DEL_DIR=5;
	
	//
	private CFSInstance cfsInst;
	public CFSInstance getCfsInst() {
		return cfsInst;
	}
	
	//
	private String id;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public DriveOp(CFSInstance cfsInstance){
		this.cfsInst = cfsInstance;
		id = File.separator + (new Date()).getTime();
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


	
	public abstract void myRun();
	
	@Override
	public void run(){
		try{
			if (cfsInst.isConnected()){
				myRun();
			}else{
				cfsInst.connect();
				try {
					//wait 3 second
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					Log.e(TAG, "", e);
				}
				cfsInst.submit(this);
			}
		}catch(Throwable t){
			Log.e(TAG, "caught in run", t);
		}
	}
	
	public void onSuccess(Object request, Object result){
		run();
	}
	
	public void onFailure(Object request, Object result){
		//
	}
	
	public void cfsCallback(boolean isSuccess, boolean isFile, String requestFileName, String result){
		cfsInst.callback(isSuccess, isFile, requestFileName, result);
	}
	/**
     * 
     * @param isSuccess
     * @param isFile
     * @param result: resourceId or error message
     */
    public void finalCallback(boolean isSuccess, boolean isFile, String requestFileName, String result){
    	cfsCallback(isSuccess, isFile, requestFileName, result);
    	if (isSuccess){
    		List<CallbackOp> cbList = getCallback();
            for (CallbackOp cb: cbList){
            	cb.onSuccess(requestFileName, result);
            }
    	}else{
    		//
    		List<CallbackOp> cbList = getCallback();
            for (CallbackOp cb: cbList){
            	cb.onFailure(requestFileName, result);
            }
    	}
    }

}
