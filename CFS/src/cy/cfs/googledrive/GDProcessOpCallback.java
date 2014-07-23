package cy.cfs.googledrive;

import cy.cfs.DriveOp;
import cy.cfs.OpCallback;

public class GDProcessOpCallback implements OpCallback{
	DriveOp op;
	GDCFSInstance cfsInst;
	
	public GDProcessOpCallback(DriveOp op, GDCFSInstance cfsInst){
		this.op = op;
		this.cfsInst = cfsInst;
	}
	
	public void execute(Object result){
		cfsInst.processOp(op);
	}

}
