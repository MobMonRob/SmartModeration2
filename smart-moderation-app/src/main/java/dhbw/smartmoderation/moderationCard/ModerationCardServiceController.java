package dhbw.smartmoderation.moderationCard;

import org.briarproject.briar.api.privategroup.PrivateGroup;

import dhbw.smartmoderation.SmartModerationApplicationImpl;
import dhbw.smartmoderation.connection.ConnectionService;
import dhbw.smartmoderation.connection.synchronization.SynchronizationService;
import dhbw.smartmoderation.data.DataService;
import dhbw.smartmoderation.exceptions.GroupNotFoundException;
import dhbw.smartmoderation.util.Util;

public class ModerationCardServiceController {

    private final ConnectionService connectionService;
    private final DataService dataService;
    private final SynchronizationService synchronizationService;

    public ModerationCardServiceController() {
        connectionService = ((SmartModerationApplicationImpl) SmartModerationApplicationImpl.getApp()).getConnectionService();
        dataService = ((SmartModerationApplicationImpl) SmartModerationApplicationImpl.getApp()).getDataService();
        synchronizationService = ((SmartModerationApplicationImpl) SmartModerationApplicationImpl.getApp()).getSynchronizationService();
    }

    public ConnectionService getConnectionService() {
        return connectionService;
    }

    public DataService getDataService() {
        return dataService;
    }

    public SynchronizationService getSynchronizationService() {
        return synchronizationService;
    }

    public PrivateGroup getPrivateGroup(Long groupId) throws GroupNotFoundException {
        for (PrivateGroup group : connectionService.getGroups()) {
            if (groupId.equals(Util.bytesToLong(group.getId().getBytes())))
                return group;

        }
        throw new GroupNotFoundException();
    }

}
