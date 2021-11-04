package dhbw.smartmoderation.connection;

import org.briarproject.bramble.BrambleCoreEagerSingletons;
import org.briarproject.bramble.BrambleCoreModule;
import org.briarproject.bramble.account.AccountModule;
import org.briarproject.bramble.system.AndroidSystemModule;
import org.briarproject.briar.BriarCoreEagerSingletons;
import org.briarproject.briar.BriarCoreModule;

import javax.inject.Singleton;

import dagger.Component;
import dhbw.smartmoderation.account.contactexchange.ContactExchangeActivity;
import dhbw.smartmoderation.account.contactexchange.ContactExchangeErrorFragment;
import dhbw.smartmoderation.account.contactexchange.IntroFragment;
import dhbw.smartmoderation.account.contactexchange.KeyAgreementActivity;
import dhbw.smartmoderation.account.contactexchange.KeyAgreementFragment;

@Component(
		modules = {
				BrambleCoreModule.class,
				BriarCoreModule.class,
				AccountModule.class,
				AndroidSystemModule.class,
				SmartModerationModule.class,
		}
)
@Singleton
public interface SmartModerationComponent extends BrambleCoreEagerSingletons, BriarCoreEagerSingletons {

	ConnectionService getSmartModerationBriarService();

	void inject(KeyAgreementActivity keyAgreementActivity);
	void inject(ContactExchangeActivity contactExchangeActivity);
	void inject(KeyAgreementFragment keyAgreementFragment);
	void inject(IntroFragment introFragment);
	void inject(ContactExchangeErrorFragment contactExchangeErrorFragment);

}
