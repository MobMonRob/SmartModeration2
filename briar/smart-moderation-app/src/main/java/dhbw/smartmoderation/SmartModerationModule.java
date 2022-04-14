package dhbw.smartmoderation;

import static android.content.Context.MODE_PRIVATE;
import static org.briarproject.bramble.api.plugin.TorConstants.DEFAULT_CONTROL_PORT;
import static org.briarproject.bramble.api.plugin.TorConstants.DEFAULT_SOCKS_PORT;
import static org.briarproject.bramble.api.reporting.ReportingConstants.DEV_ONION_ADDRESS;
import static org.briarproject.bramble.api.reporting.ReportingConstants.DEV_PUBLIC_KEY_HEX;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.StrictMode;

import com.vanniktech.emoji.RecentEmoji;

import org.briarproject.bramble.BuildConfig;
import org.briarproject.bramble.api.FeatureFlags;
import org.briarproject.bramble.api.crypto.CryptoComponent;
import org.briarproject.bramble.api.crypto.PublicKey;
import org.briarproject.bramble.api.db.DatabaseConfig;
import org.briarproject.bramble.api.nullsafety.NotNullByDefault;
import org.briarproject.bramble.api.plugin.BluetoothConstants;
import org.briarproject.bramble.api.plugin.LanTcpConstants;
import org.briarproject.bramble.api.plugin.PluginConfig;
import org.briarproject.bramble.api.plugin.TorControlPort;
import org.briarproject.bramble.api.plugin.TorDirectory;
import org.briarproject.bramble.api.plugin.TorSocksPort;
import org.briarproject.bramble.api.plugin.TransportId;
import org.briarproject.bramble.api.plugin.duplex.DuplexPluginFactory;
import org.briarproject.bramble.api.plugin.simplex.SimplexPluginFactory;
import org.briarproject.bramble.api.reporting.DevConfig;
import org.briarproject.bramble.battery.DefaultBatteryManagerModule;
import org.briarproject.bramble.mailbox.MailboxModule;
import org.briarproject.bramble.network.AndroidNetworkModule;
import org.briarproject.bramble.plugin.bluetooth.AndroidBluetoothPluginFactory;
import org.briarproject.bramble.plugin.file.AndroidRemovableDrivePluginFactory;
import org.briarproject.bramble.plugin.tcp.AndroidLanTcpPluginFactory;
import org.briarproject.bramble.plugin.tor.AndroidTorPluginFactory;
import org.briarproject.bramble.plugin.tor.CircumventionModule;
import org.briarproject.bramble.settings.SettingsModule;
import org.briarproject.bramble.socks.SocksModule;
import org.briarproject.bramble.system.DefaultTaskSchedulerModule;
import org.briarproject.bramble.system.DefaultWakefulIoExecutorModule;
import org.briarproject.bramble.util.AndroidUtils;
import org.briarproject.bramble.util.StringUtils;
import org.briarproject.briar.blog.BlogModule;
import org.briarproject.briar.forum.ForumModule;
import org.briarproject.briar.introduction.IntroductionModule;
import org.briarproject.briar.sharing.SharingModule;

import java.io.File;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import api.android.AndroidNotificationManager;
import api.android.DozeWatchdog;
import api.android.LockManager;
import api.android.NetworkUsageMetrics;
import api.android.ScreenFilterMonitor;
import dagger.Module;
import dagger.Provides;
import dhbw.smartmoderation.account.contactexchange.ContactExchangeModule;
import dhbw.smartmoderation.account.contactexchange.ViewModelModule;

@Module(
        includes = {
                AndroidNetworkModule.class,
                CircumventionModule.class,
                DefaultBatteryManagerModule.class,
                SocksModule.class,
                DefaultWakefulIoExecutorModule.class,
                DefaultTaskSchedulerModule.class,
                ViewModelModule.class,
                ContactExchangeModule.class,
                ViewModelModule.class,
                SettingsModule.class,
                IntroductionModule.class,
                // below need to be within same scope as ViewModelProvider.Factory
                BlogModule.class,
                ForumModule.class,
                SharingModule.class,
                MailboxModule.class,
        }
)

public class SmartModerationModule {

    static class EagerSingletons {
        @Inject
        AndroidNotificationManager androidNotificationManager;
        @Inject
        ScreenFilterMonitor screenFilterMonitor;
        @Inject
        NetworkUsageMetrics networkUsageMetrics;
        @Inject
        DozeWatchdog dozeWatchdog;
        @Inject
        LockManager lockManager;
        @Inject
        RecentEmoji recentEmoji;
    }

    private final Application application;

    public SmartModerationModule(Application application) {
        this.application = application;
    }

    public SmartModerationModule() {
        this.application = SmartModerationApplicationImpl.getApp();
    }

    @Provides
    @Singleton
    Application provideApplication() {
        return application;
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
    @Singleton
    @TorDirectory
    File provideTorDirectory(Application app) {
        return app.getDir("tor", MODE_PRIVATE);
    }

    @Provides
    @Singleton
    @TorSocksPort
    int provideTorSocksPort() {
        if (!BuildConfig.DEBUG) {
            return DEFAULT_SOCKS_PORT;
        } else {
            return DEFAULT_SOCKS_PORT + 2;
        }
    }

    @Provides
    @Singleton
    @TorControlPort
    int provideTorControlPort() {
        if (!BuildConfig.DEBUG) {
            return DEFAULT_CONTROL_PORT;
        } else {
            return DEFAULT_CONTROL_PORT + 2;
        }
    }

    @Provides
    @Singleton
    PluginConfig providePluginConfig(AndroidBluetoothPluginFactory bluetooth,
                                     AndroidTorPluginFactory tor, AndroidLanTcpPluginFactory lan,
                                     AndroidRemovableDrivePluginFactory drive,
                                     FeatureFlags featureFlags) {
        @NotNullByDefault
        PluginConfig pluginConfig = new PluginConfig() {

            @Override
            public Collection<DuplexPluginFactory> getDuplexFactories() {
                return asList(bluetooth, tor, lan);
            }

            @Override
            public Collection<SimplexPluginFactory> getSimplexFactories() {
                return singletonList(drive);
            }

            @Override
            public boolean shouldPoll() {
                return true;
            }

            @Override
            public Map<TransportId, List<TransportId>> getTransportPreferences() {
                // Prefer LAN to Bluetooth
                return singletonMap(BluetoothConstants.ID,
                        singletonList(LanTcpConstants.ID));
            }
        };
        return pluginConfig;
    }

    @Provides
    @Singleton
    DevConfig provideDevConfig(Application app, CryptoComponent crypto) {
        @NotNullByDefault
        DevConfig devConfig = new DevConfig() {

            @Override
            public PublicKey getDevPublicKey() {
                try {
                    return crypto.getMessageKeyParser().parsePublicKey(
                            StringUtils.fromHexString(DEV_PUBLIC_KEY_HEX));
                } catch (GeneralSecurityException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public String getDevOnionAddress() {
                return DEV_ONION_ADDRESS;
            }

            @Override
            public File getReportDir() {
                return AndroidUtils.getReportDir(app.getApplicationContext());
            }

            @Override
            public File getLogcatFile() {
                return AndroidUtils.getLogcatFile(app.getApplicationContext());
            }
        };
        return devConfig;
    }

    @Provides
    SharedPreferences provideSharedPreferences(Application app) {
        // FIXME unify this with getDefaultSharedPreferences()
        return app.getSharedPreferences("db", MODE_PRIVATE);
    }

    @Provides
    FeatureFlags provideFeatureFlags() {
        return new FeatureFlags() {

            @Override
            public boolean shouldEnableImageAttachments() {
                return true;
            }

            @Override
            public boolean shouldEnableProfilePictures() {
                return true;
            }

            @Override
            public boolean shouldEnableDisappearingMessages() {
                return true;
            }

            @Override
            public boolean shouldEnableMailbox() {
                return BuildConfig.DEBUG;
            }

            @Override
            public boolean shouldEnablePrivateGroupsInCore() {
                return true;
            }

            @Override
            public boolean shouldEnableForumsInCore() {
                return true;
            }

            @Override
            public boolean shouldEnableBlogsInCore() {
                return true;
            }
        };
    }
}
