package cy.cfs;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//the current mapping instance of the cfs
public abstract class CFSInstance {

	private CFSConf conf;
	private long used; //in bytes
	private ExecutorService exeService = Executors.newFixedThreadPool(1);

	
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
	
	public abstract boolean isConnected();
	public abstract void connect();
	
	public void submit(Runnable a){
		exeService.submit(a);
	}
}
