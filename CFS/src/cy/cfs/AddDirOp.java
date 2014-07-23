package cy.cfs;

public class AddDirOp extends DriveOp{
	
	public AddDirOp(){
		setOpCode(DriveOp.OP_ADD_DIR);
	}
	
	private String fileName; //
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}
