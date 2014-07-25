package cy.cfs;

/**
 * Mapping the virtual file key to CFS file
 * @author chengyi
 *
 */
public interface CFSMapper {
	
	//the cfs field format is "cfs-conf-id:cfs-file"
	public String getCFSFileId(String fileKey); 

}
