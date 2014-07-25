package cy.cfs;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


//the current mapping instance of the cfs
public abstract class CFSInstance {
	private CFSConf conf;
	private long used; //in bytes
	private ExecutorService exeService = Executors.newFixedThreadPool(1);

	public static final String UNCONNECTED="disconnected";
	public static final String CONNECTING="connecting";
	public static final String CONNECTED="connected";
	
	private String status=UNCONNECTED;
	
	public static final String INTENT_EXTRA_CFS_INSTANCE_ID="cfsInstanceId";
	
	public CFSInstance(CFSConf conf){
		this.conf = conf;
		this.used = 0;
	}

	public abstract boolean isConnected();
	public abstract void connect();
	
	public void submit(Runnable a){
		exeService.submit(a);
	}
	
	//virtual file name map to cloud specific resource id cache
	private ConcurrentHashMap<String, String> dirMap = new ConcurrentHashMap<String, String>();//since i need null value for DriveId placeholder
	private ConcurrentHashMap<String, String> fileMap = new ConcurrentHashMap<String, String>();
	
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

	public CFSConf getConf() {
		return conf;
	}
	public void setConf(CFSConf conf) {
		this.conf = conf;
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
		}else{
			String errorMessage = DriveOp.ERROR_MAKE + result;
			if (isFile){
				getFileMap().put(requestFileName, errorMessage);
			}else{
				getDirMap().put(requestFileName, errorMessage);
			}
		}
		
	}
}
