package cy.cfs;

public interface CallbackOp {
	
	public void onSuccess(Object request, Object result);
	
	public void onFailure(Object request, Object result);
}
