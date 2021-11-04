package dhbw.smartmoderation.connection;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;

import org.briarproject.bramble.api.FeatureFlags;
import org.briarproject.bramble.api.battery.BatteryManager;
import org.briarproject.bramble.api.db.DatabaseConfig;
import org.briarproject.bramble.api.event.EventBus;
import org.briarproject.bramble.api.io.TimeoutMonitor;
import org.briarproject.bramble.api.lifecycle.IoExecutor;
import org.briarproject.bramble.api.network.NetworkManager;
import org.briarproject.bramble.api.nullsafety.NotNullByDefault;
import org.briarproject.bramble.api.plugin.BackoffFactory;
import org.briarproject.bramble.api.plugin.BluetoothConstants;
import org.briarproject.bramble.api.plugin.LanTcpConstants;
import org.briarproject.bramble.api.plugin.PluginConfig;
import org.briarproject.bramble.api.plugin.TransportId;
import org.briarproject.bramble.api.plugin.duplex.DuplexPluginFactory;
import org.briarproject.bramble.api.plugin.simplex.SimplexPluginFactory;
import org.briarproject.bramble.api.system.AndroidExecutor;
import org.briarproject.bramble.api.system.AndroidWakeLockManager;
import org.briarproject.bramble.api.system.Clock;
import org.briarproject.bramble.api.system.LocationUtils;
import org.briarproject.bramble.api.system.ResourceProvider;
import org.briarproject.bramble.api.system.TaskScheduler;
import org.briarproject.bramble.api.system.WakefulIoExecutor;
import org.briarproject.bramble.battery.DefaultBatteryManagerModule;
import org.briarproject.bramble.network.AndroidNetworkModule;
import org.briarproject.bramble.plugin.bluetooth.AndroidBluetoothPluginFactory;
import org.briarproject.bramble.plugin.tcp.AndroidLanTcpPluginFactory;
import org.briarproject.bramble.plugin.tor.AndroidTorPluginFactory;
import org.briarproject.bramble.plugin.tor.CircumventionModule;
import org.briarproject.bramble.plugin.tor.CircumventionProvider;
import org.briarproject.bramble.socks.SocksModule;
import org.briarproject.bramble.system.DefaultTaskSchedulerModule;
import org.briarproject.bramble.system.DefaultWakefulIoExecutorModule;

import java.io.File;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.inject.Singleton;
import javax.net.SocketFactory;

import dagger.Module;
import dagger.Provides;
import dhbw.smartmoderation.SmartModerationApplication;
import dhbw.smartmoderation.account.contactexchange.ContactExchangeModule;
import dhbw.smartmoderation.account.contactexchange.ViewModelModule;

import static android.content.Context.MODE_PRIVATE;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;

@Module(
		includes = {
				AndroidNetworkModule.class,
				CircumventionModule.class,
				DefaultBatteryManagerModule.class,
				SocksModule.class,
				DefaultWakefulIoExecutorModule.class,
				DefaultTaskSchedulerModule.class,
				ViewModelModule.class,
				ContactExchangeModule.class
		}
)
class SmartModerationModule {

	@Provides
	@Singleton
	Application provideApplication() {
		return SmartModerationApplication.getApp();
	}

	@Provides
	@Singleton
	DatabaseConfig provideDatabaseConfig(Application app) {
		StrictMode.ThreadPolicy tp = StrictMode.allowThreadDiskReads();
		StrictMode.allowThreadDiskWrites();
		File dbDir = app.getApplicationContext().getDir("db", MODE_PRIVATE);
		File keyDir = app.getApplicationContext().getDir("key", MODE_PRIVATE);
		StrictMode.setThreadPolicy(tp);
		return new SmartModerationDatabaseConfig(dbDir, keyDir);
	}

	@Provides
	PluginConfig providePluginConfig(@IoExecutor Executor ioExecutor,
									 @WakefulIoExecutor Executor wakefulIoExecutor,
									 AndroidExecutor androidExecutor, AndroidWakeLockManager wakeLockManager,
									 SecureRandom random,
									 TimeoutMonitor timeoutMonitor,
									 SocketFactory torSocketFactory, BackoffFactory backoffFactory,
									 Application app, NetworkManager networkManager,
									 LocationUtils locationUtils, EventBus eventBus,
									 ResourceProvider resourceProvider,
									 CircumventionProvider circumventionProvider,
									 BatteryManager batteryManager, Clock clock) {
		DuplexPluginFactory bluetooth =
				new AndroidBluetoothPluginFactory(ioExecutor,
						wakefulIoExecutor,
						androidExecutor,
						wakeLockManager,
						app, random, eventBus, clock,timeoutMonitor, backoffFactory);
		DuplexPluginFactory tor =
				new AndroidTorPluginFactory(ioExecutor,
						wakefulIoExecutor,
						app,
						networkManager,
						locationUtils,
						eventBus,
						torSocketFactory,
						backoffFactory,
						resourceProvider,
						circumventionProvider,
						batteryManager,
						wakeLockManager,
						clock,app.getApplicationContext().getDir("tor",MODE_PRIVATE)
						);
		DuplexPluginFactory lan = new AndroidLanTcpPluginFactory(ioExecutor,wakefulIoExecutor,
				eventBus, backoffFactory, app);
		Collection<DuplexPluginFactory> duplex = asList(bluetooth, tor, lan);




		@NotNullByDefault
		PluginConfig pluginConfig = new PluginConfig() {

			@Override
			public Collection<DuplexPluginFactory> getDuplexFactories() {
				return duplex;
			}

			@Override
			public Collection<SimplexPluginFactory> getSimplexFactories() {
				return emptyList();
			}

			@Override
			public boolean shouldPoll() {
				return true;
			}

			@Override
			public Map<TransportId, List<TransportId>> getTransportPreferences() {
				return singletonMap(BluetoothConstants.ID,
						singletonList(LanTcpConstants.ID));
			}
		};
		return pluginConfig;
	}

	@Provides
	FeatureFlags provideFeatureFlags() {
		return new FeatureFlags() {
			@Override
			public boolean shouldEnableImageAttachments() {
				return false;
			}

			@Override
			public boolean shouldEnableProfilePictures() {
				return false;
			}
		};
	}

}
