package dhbw.smartmoderation;

import androidx.lifecycle.ViewModelProvider;

import org.briarproject.bramble.BrambleCoreEagerSingletons;
import org.briarproject.bramble.BrambleCoreModule;
import org.briarproject.bramble.account.AccountModule;
import org.briarproject.bramble.api.FeatureFlags;
import org.briarproject.bramble.api.account.AccountManager;
import org.briarproject.bramble.api.connection.ConnectionRegistry;
import org.briarproject.bramble.api.contact.ContactExchangeManager;
import org.briarproject.bramble.api.contact.ContactManager;
import org.briarproject.bramble.api.crypto.CryptoExecutor;
import org.briarproject.bramble.api.crypto.PasswordStrengthEstimator;
import org.briarproject.bramble.api.db.DatabaseExecutor;
import org.briarproject.bramble.api.db.TransactionManager;
import org.briarproject.bramble.api.event.EventBus;
import org.briarproject.bramble.api.identity.IdentityManager;
import org.briarproject.bramble.api.keyagreement.KeyAgreementTask;
import org.briarproject.bramble.api.keyagreement.PayloadEncoder;
import org.briarproject.bramble.api.keyagreement.PayloadParser;
import org.briarproject.bramble.api.lifecycle.IoExecutor;
import org.briarproject.bramble.api.lifecycle.LifecycleManager;
import org.briarproject.bramble.api.plugin.PluginManager;
import org.briarproject.bramble.api.settings.SettingsManager;
import org.briarproject.bramble.api.system.AndroidExecutor;
import org.briarproject.bramble.api.system.AndroidWakeLockManager;
import org.briarproject.bramble.api.system.Clock;
import org.briarproject.bramble.api.system.LocationUtils;
import org.briarproject.bramble.plugin.file.RemovableDriveModule;
import org.briarproject.bramble.plugin.tor.CircumventionProvider;
import org.briarproject.bramble.system.AndroidSystemModule;
import org.briarproject.bramble.system.ClockModule;
import org.briarproject.briar.BriarCoreEagerSingletons;
import org.briarproject.briar.BriarCoreModule;
import org.briarproject.briar.api.attachment.AttachmentReader;
import org.briarproject.briar.api.autodelete.AutoDeleteManager;
import org.briarproject.briar.api.blog.BlogManager;
import org.briarproject.briar.api.blog.BlogPostFactory;
import org.briarproject.briar.api.blog.BlogSharingManager;
import org.briarproject.briar.api.client.MessageTracker;
import org.briarproject.briar.api.conversation.ConversationManager;
import org.briarproject.briar.api.feed.FeedManager;
import org.briarproject.briar.api.forum.ForumManager;
import org.briarproject.briar.api.forum.ForumSharingManager;
import org.briarproject.briar.api.identity.AuthorManager;
import org.briarproject.briar.api.introduction.IntroductionManager;
import org.briarproject.briar.api.messaging.MessagingManager;
import org.briarproject.briar.api.messaging.PrivateMessageFactory;
import org.briarproject.briar.api.privategroup.GroupMessageFactory;
import org.briarproject.briar.api.privategroup.PrivateGroupFactory;
import org.briarproject.briar.api.privategroup.PrivateGroupManager;
import org.briarproject.briar.api.privategroup.invitation.GroupInvitationFactory;
import org.briarproject.briar.api.privategroup.invitation.GroupInvitationManager;
import org.briarproject.briar.attachment.AttachmentModule;

import java.util.concurrent.Executor;

import javax.inject.Singleton;

import dagger.Component;
import dhbw.smartmoderation.account.contactexchange.ContactExchangeActivity;
import dhbw.smartmoderation.account.contactexchange.ContactExchangeErrorFragment;
import dhbw.smartmoderation.account.contactexchange.IntroFragment;
import dhbw.smartmoderation.account.contactexchange.KeyAgreementActivity;
import dhbw.smartmoderation.account.contactexchange.KeyAgreementFragment;
import dhbw.smartmoderation.connection.ConnectionService;

@Singleton
@Component(
		modules = {
				BrambleCoreModule.class,
				BriarCoreModule.class,
				AccountModule.class,
				SmartModerationModule.class,
				AttachmentModule.class,
				ClockModule.class,
				AndroidSystemModule.class,
				RemovableDriveModule.class,
		}
)

public interface SmartModerationComponent extends BrambleCoreEagerSingletons, BriarCoreEagerSingletons/*, BrambleAndroidEagerSingletons ,BrambleAppComponent*/ {

	ConnectionService getSmartModerationBriarService();

	// Exposed objects
	@CryptoExecutor
	Executor cryptoExecutor();

	PasswordStrengthEstimator passwordStrengthIndicator();

	@DatabaseExecutor
	Executor databaseExecutor();

	TransactionManager transactionManager();

	MessageTracker messageTracker();

	LifecycleManager lifecycleManager();

	IdentityManager identityManager();

	AttachmentReader attachmentReader();

	AuthorManager authorManager();

	PluginManager pluginManager();

	EventBus eventBus();

	ConnectionRegistry connectionRegistry();

	ContactManager contactManager();

	ConversationManager conversationManager();

	MessagingManager messagingManager();

	PrivateMessageFactory privateMessageFactory();

	PrivateGroupManager privateGroupManager();

	GroupInvitationFactory groupInvitationFactory();

	GroupInvitationManager groupInvitationManager();

	PrivateGroupFactory privateGroupFactory();

	GroupMessageFactory groupMessageFactory();

	ForumManager forumManager();

	ForumSharingManager forumSharingManager();

	BlogSharingManager blogSharingManager();

	BlogManager blogManager();

	BlogPostFactory blogPostFactory();

	SettingsManager settingsManager();

	ContactExchangeManager contactExchangeManager();

	KeyAgreementTask keyAgreementTask();

	PayloadEncoder payloadEncoder();

	PayloadParser payloadParser();

	IntroductionManager introductionManager();

	AndroidExecutor androidExecutor();

	FeedManager feedManager();

	Clock clock();

	//TestDataCreator testDataCreator();

	@IoExecutor
	Executor ioExecutor();

	AccountManager accountManager();

	LocationUtils locationUtils();

	CircumventionProvider circumventionProvider();

	ViewModelProvider.Factory viewModelFactory();

	FeatureFlags featureFlags();

	AndroidWakeLockManager wakeLockManager();

	//Thread.UncaughtExceptionHandler exceptionHandler();

	AutoDeleteManager autoDeleteManager();

	void inject(KeyAgreementActivity keyAgreementActivity);

	void inject(ContactExchangeActivity contactExchangeActivity);

	void inject(KeyAgreementFragment keyAgreementFragment);

	void inject(IntroFragment introFragment);

	void inject(ContactExchangeErrorFragment contactExchangeErrorFragment);

}
