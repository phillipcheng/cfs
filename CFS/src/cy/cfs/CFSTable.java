package cy.cfs;

import java.util.HashMap;
import java.util.Map;

//the current mapping from virtual file system to cloud drives
public class CFSTable {
	
	public static Map<String, CFSInstance> instanceMap = new HashMap<String, CFSInstance>();
	
	public void putInstance(CFSInstance instance){
		instanceMap.put(instance.getConf().getId(), instance);
	}
	
	//
	public CFSInstance getFitInstance(long l){
		for (CFSInstance inst:instanceMap.values()){
			if (inst.getConf().getQuota()-inst.getUsed()>l){
				return inst;
			}
		}
		return null;
	}
}
