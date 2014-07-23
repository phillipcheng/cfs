package cy.cfs;

public class AddFileOp extends DriveOp{
	
	public AddFileOp(){
		setOpCode(DriveOp.OP_ADD_FILE);
	}
	
	private String fileName; //
	private String mimeType;
	private long size;
	private byte[] binaryContent;
	
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getMimeType() {
		return mimeType;
	}
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public byte[] getBinaryContent() {
		return binaryContent;
	}
	public void setBinaryContent(byte[] binaryContent) {
		this.binaryContent = binaryContent;
	}
}
