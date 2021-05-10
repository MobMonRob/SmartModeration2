package dhbw.smartmoderation.login;

import dhbw.smartmoderation.controller.SmartModerationController;

/**
 * Controller for {@link LoginActivity}.
 */
class LoginController extends SmartModerationController {

	boolean login(String password) {

		boolean success = connectionService.login(password);
		connectionService.setLocalAuthor();
		synchronizationService.setLocalAuthor(connectionService.getLocalAuthor());
		return success;
	}

}
