package dhbw.smartmoderation.controller;

import org.briarproject.briar.api.privategroup.PrivateGroup;

import java.util.Collection;

import dhbw.smartmoderation.SmartModerationApplicationImpl;
import dhbw.smartmoderation.connection.ConnectionService;
import dhbw.smartmoderation.connection.synchronization.SynchronizationService;
import dhbw.smartmoderation.data.DataService;
import dhbw.smartmoderation.data.model.Meeting;
import dhbw.smartmoderation.exceptions.GroupNotFoundException;
import dhbw.smartmoderation.util.Util;

/**
 * Controllers can extends this class for easily accessing all the services.
 */
public abstract class SmartModerationController {

	protected final ConnectionService connectionService;
	protected final DataService dataService;
	protected final SynchronizationService synchronizationService;

	public SmartModerationController() {
		connectionService = ((SmartModerationApplicationImpl) SmartModerationApplicationImpl.getApp()).getConnectionService();
		dataService = ((SmartModerationApplicationImpl) SmartModerationApplicationImpl.getApp()).getDataService();
		synchronizationService = ((SmartModerationApplicationImpl) SmartModerationApplicationImpl.getApp()).getSynchronizationService();
	}

	public PrivateGroup getPrivateGroup(Long groupId) throws GroupNotFoundException {
		for (PrivateGroup group : connectionService.getGroups()) {
			if (groupId.equals(Util.bytesToLong(group.getId().getBytes())))
				return group;

		}
		throw new GroupNotFoundException();
	}
}
