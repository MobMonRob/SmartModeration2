package dhbw.smartmoderation.connection.synchronization;

import android.util.Log;

import org.briarproject.briar.api.privategroup.PrivateGroup;

import java.util.Collection;

import dhbw.smartmoderation.connection.ConnectionService;
import dhbw.smartmoderation.exceptions.GroupNotFoundException;

public class UpdateChecker implements Runnable{

    public final static String TAG = UpdateChecker.class.getSimpleName();

    SynchronizationService SynchronizationService;
    ConnectionService ConnectionService;

    public UpdateChecker(SynchronizationService synchronizationService, ConnectionService connectionService){
        SynchronizationService = synchronizationService;
        ConnectionService = connectionService;
    }

    @Override
    public void run() {
        try{
            Collection<PrivateGroup> Groups = ConnectionService.getGroups();
            for (PrivateGroup group: Groups){
                SynchronizationService.pull(group);
            }
        }catch (GroupNotFoundException exception){
            Log.d(TAG,exception.getMessage());
        }


        try {
            Thread.sleep(10000);
        }catch (InterruptedException exception){
        }
    }


}
