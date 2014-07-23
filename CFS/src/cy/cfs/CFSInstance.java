package cy.cfs;

import java.util.ArrayList;
import java.util.List;

//the current mapping instance of the cfs
public abstract class CFSInstance {

	private List<DriveOp> dopList = new ArrayList<DriveOp>(); //synchronization needed
	private CFSConf conf;
	private long used; //in bytes
	
	public CFSInstance(CFSConf conf){
		this.conf = conf;
		this.used = 0;
	}

	
	public long getUsed() {
		return used;
	}
	public void setUsed(long used) {
		this.used = used;
	}

	public CFSConf getConf() {
		return conf;
	}
	public void setConf(CFSConf conf) {
		this.conf = conf;
	}
	
	public void addOp(DriveOp dop){
		if (isConnected()){
			processOp(dop);
		}else{
			synchronized(dopList){
				dopList.add(dop);
			}
			connect();
		}
	}
	
	public void processOp(){
		DriveOp dop = null;
		synchronized (dopList){
			if (dopList.size()>0){
				dop = dopList.remove(0);
			}
		}
		processOp(dop);
	}
	
	abstract public void processOp(DriveOp dop);
	
	public abstract boolean isConnected();
	public abstract void connect();
}
