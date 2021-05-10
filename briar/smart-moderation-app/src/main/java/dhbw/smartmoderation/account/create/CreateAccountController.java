package dhbw.smartmoderation.account.create;

import dhbw.smartmoderation.controller.SmartModerationController;

/**
 * Controller for {@link CreateAccountActivity}.
 */
class CreateAccountController extends SmartModerationController {

	void createAccount(String username, String password) {
		connectionService.createAccount(username, password);
		connectionService.setLocalAuthor();
		synchronizationService.setLocalAuthor(connectionService.getLocalAuthor());
	}

}
