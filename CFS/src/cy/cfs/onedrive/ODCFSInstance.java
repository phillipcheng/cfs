package cy.cfs.onedrive;

import java.util.Arrays;

import android.app.Activity;

import com.microsoft.live.LiveAuthClient;
import com.microsoft.live.LiveAuthException;
import com.microsoft.live.LiveAuthListener;
import com.microsoft.live.LiveConnectClient;
import com.microsoft.live.LiveConnectSession;
import com.microsoft.live.LiveStatus;

import cy.cfs.CFSInstance;

public class ODCFSInstance extends CFSInstance implements LiveAuthListener {


	public static final String APP_CLIENT_ID="00000000401250E1";
	
    private LiveConnectClient client;
	private LiveAuthClient auth;
	
	public ODCFSInstance(String id, String userId) {
		super(id, userId);
		this.setVendor(CFSInstance.VENDOR_MICROSOFT);
	}

	//for CFSInstance
	@Override
	public boolean isConnected() {
		if (client!=null){
			setStatus(CONNECTED);
			return true;
		}
		return false;
	}

	@Override
	public void myConnect(Activity activity) {
		if (auth==null){
			auth=new LiveAuthClient(activity, APP_CLIENT_ID);
			auth.logout(this);
		}
		Iterable<String> scopes = Arrays.asList(Scopes.BASICS);
        auth.login(activity, scopes, this);
	}
	
	@Override
	public void myDisconnect() {
		if (auth!=null){
			auth.logout(this);
		}
		auth=null;
	}
	
	@Override
	public void onAuthComplete(LiveStatus status, LiveConnectSession session, Object userState) {
        if(status == LiveStatus.CONNECTED) {
            setClient(new LiveConnectClient(session));
            getConnected();
        }else if (status == LiveStatus.NOT_CONNECTED){
            setClient(null);
            setStatus(CFSInstance.UNCONNECTED);
            auth = null;
        }else if (status == LiveStatus.UNKNOWN){
        	setStatus(CFSInstance.UNKNOWN);
        }
    }

	@Override
    public void onAuthError(LiveAuthException exception, Object userState) {
        setClient(null);  
    }
	
	
	public LiveConnectClient getClient() {
		return client;
	}
	public void setClient(LiveConnectClient client) {
		this.client = client;
	}


	
}
