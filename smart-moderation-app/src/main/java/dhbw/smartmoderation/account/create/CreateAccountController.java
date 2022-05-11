package dhbw.smartmoderation.account.create;

import org.briarproject.bramble.api.identity.LocalAuthor;

import dhbw.smartmoderation.SmartModerationApplicationImpl;
import dhbw.smartmoderation.controller.SmartModerationController;
import dhbw.smartmoderation.data.model.LocalAuthorDao;
import dhbw.smartmoderation.util.Util;

class CreateAccountController extends SmartModerationController {

	LocalAuthorDao localAuthorDao = ((SmartModerationApplicationImpl) SmartModerationApplicationImpl.getApp()).getDaoSession().getLocalAuthorDao();

	void createAccount(String username, String password) {
		connectionService.createAccount(username, password);
		connectionService.setLocalAuthor();
		LocalAuthor localAuthor = connectionService.getLocalAuthor();
		synchronizationService.setLocalAuthor(localAuthor);

		if(localAuthorDao.loadAll().size() == 0) {
			dhbw.smartmoderation.data.model.LocalAuthor author = new dhbw.smartmoderation.data.model.LocalAuthor(Util.bytesToLong(localAuthor.getId().getBytes()));
			localAuthorDao.insertOrReplaceInTx(author);
		}
	}

}
