package dhbw.smartmoderation.login;

import org.briarproject.bramble.api.identity.LocalAuthor;

import dhbw.smartmoderation.SmartModerationApplication;
import dhbw.smartmoderation.SmartModerationApplicationImpl;
import dhbw.smartmoderation.controller.SmartModerationController;
import dhbw.smartmoderation.data.model.LocalAuthorDao;
import dhbw.smartmoderation.util.Util;

/**
 * Controller for {@link LoginActivity}.
 */
class LoginController extends SmartModerationController {

	LocalAuthorDao localAuthorDao = ((SmartModerationApplicationImpl)SmartModerationApplicationImpl.getApp()).getDaoSession().getLocalAuthorDao();

	boolean login(String password) {

		boolean success = connectionService.login(password);
		connectionService.setLocalAuthor();
		LocalAuthor localAuthor = connectionService.getLocalAuthor();
		synchronizationService.setLocalAuthor(localAuthor);

		if(localAuthorDao.loadAll().size() == 0) {

			dhbw.smartmoderation.data.model.LocalAuthor author = new dhbw.smartmoderation.data.model.LocalAuthor(Util.bytesToLong(localAuthor.getId().getBytes()));
			localAuthorDao.saveInTx(author);
		}

		return success;
	}

}
