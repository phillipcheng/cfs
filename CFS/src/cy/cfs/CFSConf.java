package cy.cfs;

public class CFSConf {
	
	public static final String VENDOR_GOOGLE_DRIVE="google.drive";
	public static final String VENDOR_AMAZON_DRIVE="amazon.drive";
	
	private String id;
	private String vendor;
	private String account;//usually email address
	private long quota; //number of bytes
	
	public CFSConf(String id, String vendor, String account, long quota, String rootFolder){
		this.id = id;
		this.vendor = vendor;
		this.account = account;
		this.quota = quota;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getVendor() {
		return vendor;
	}
	public void setVendor(String vendor) {
		this.vendor = vendor;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public long getQuota() {
		return quota;
	}
	public void setQuota(long quota) {
		this.quota = quota;
	}
	

}
