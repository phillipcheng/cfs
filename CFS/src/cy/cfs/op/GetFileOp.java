package cy.cfs.op;

import cy.cfs.CFSInstance;
import cy.cfs.DriveOp;


public class GetFileOp extends DriveOp{

	public GetFileOp(CFSInstance cfsInstance) {
		super(cfsInstance);
	}

	private String fileName;

	
	@Override
	public void myRun() {
		
	}


	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}
