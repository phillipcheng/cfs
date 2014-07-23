package cy.cfs;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import android.util.Log;

public abstract class DriveOp implements Runnable, OpCallback{
	
	protected static final String TAG = "DriveOp";
	
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
	public void setCfsInst(CFSInstance cfsInst) {
		this.cfsInst = cfsInst;
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
	private List<OpCallback> callbackList = new ArrayList<OpCallback>(); 
	
	public List<OpCallback> getCallback() {
		return callbackList;
	}
	public void insertCallback(OpCallback callback){
		callbackList.add(0, callback);
	}
	public void addCallback(OpCallback callback) {
		this.callbackList.add(callback);
	}
	public void addCallbackList(List<OpCallback> callbacks) {
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
	
	public void onSuccess(Object result){
		run();
	}
	
	public void onFailure(Object result){
		//
	}

}
