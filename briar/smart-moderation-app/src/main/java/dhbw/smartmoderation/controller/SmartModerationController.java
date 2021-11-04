package dhbw.smartmoderation.controller;

import dhbw.smartmoderation.SmartModerationApplication;
import dhbw.smartmoderation.connection.ConnectionService;
import dhbw.smartmoderation.connection.synchronization.SynchronizationService;
import dhbw.smartmoderation.data.DataService;

/**
 * Controllers can extends this class for easily accessing all the services.
 */
public abstract class SmartModerationController {

	protected final ConnectionService connectionService;
	protected final DataService dataService;
	protected final SynchronizationService synchronizationService;

	public SmartModerationController() {
		connectionService = ((SmartModerationApplication) SmartModerationApplication.getApp()).getConnectionService();
		dataService = ((SmartModerationApplication) SmartModerationApplication.getApp()).getDataService();
		synchronizationService = ((SmartModerationApplication) SmartModerationApplication.getApp()).getSynchronizationService();
	}

}
