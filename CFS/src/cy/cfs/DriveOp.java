package cy.cfs;

import java.util.ArrayList;
import java.util.List;

public class DriveOp {
	
	public static int OP_ADD_FILE=1;
	public static int OP_GET_FILE=2;
	public static int OP_ADD_DIR=3;
	public static int OP_DEL_FILE=4;
	public static int OP_DEL_DIR=5;
	
	
	private int opCode;
	//callback in the calling order, only the last can be asynchronous, the other should be synchronous
	private List<OpCallback> callbackList = new ArrayList<OpCallback>(); 
	
	public int getOpCode() {
		return opCode;
	}
	public void setOpCode(int opCode) {
		this.opCode = opCode;
	}
	
	public List<OpCallback> getCallback() {
		return callbackList;
	}
	public void addCallback(OpCallback callback) {
		this.callbackList.add(callback);
	}
	public void addCallbackList(List<OpCallback> callbacks) {
		this.callbackList.addAll(callbacks);
	}
}
