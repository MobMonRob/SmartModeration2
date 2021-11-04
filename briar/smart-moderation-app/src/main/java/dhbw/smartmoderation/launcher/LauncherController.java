package dhbw.smartmoderation.launcher;

import dhbw.smartmoderation.controller.SmartModerationController;

/**
 * Controller for {@link LauncherActivity}.
 */
class LauncherController extends SmartModerationController {

	boolean accountExists() {
		return connectionService.accountExists();
	}

}
