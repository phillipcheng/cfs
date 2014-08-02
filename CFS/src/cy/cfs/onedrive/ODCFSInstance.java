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
	
	public ODCFSInstance(String id, String vendor, String account, long quota, String rootFolder, String userId) {
		super(id, vendor, account, quota, rootFolder, userId);
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
	public void connect(Activity activity) {
		if (auth==null){
			auth=new LiveAuthClient(activity, APP_CLIENT_ID);
		}
		Iterable<String> scopes = Arrays.asList(Scopes.BASICS);
        auth.login(activity, scopes, this);
	}
	@Override
	public void disconnect() {
		if (auth!=null){
			auth.logout(this);
		}
	}
	@Override
	public void onAuthComplete(LiveStatus status, LiveConnectSession session, Object userState) {
        if(status == LiveStatus.CONNECTED) {
            setClient(new LiveConnectClient(session));
            setStatus(CFSInstance.CONNECTED);
            startPendingOp();
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
