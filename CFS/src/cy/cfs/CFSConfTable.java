package cy.cfs;

import java.util.HashMap;
import java.util.Map;

public class CFSConfTable {
	
	//cloud id to CFSConf
	Map<String, CFSConf> cfsConfTable = new HashMap<String, CFSConf>();
	
	public void put(String id, CFSConf cfsConf){
		cfsConfTable.put(id, cfsConf);
	}

}
