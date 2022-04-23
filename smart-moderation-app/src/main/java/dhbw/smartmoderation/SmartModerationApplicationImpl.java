package dhbw.smartmoderation;

import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
import static java.util.logging.Logger.getLogger;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Application;
import android.content.res.Configuration;

import androidx.annotation.NonNull;

import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.google.GoogleEmojiProvider;

import org.briarproject.bramble.BrambleAppComponent;
import org.briarproject.bramble.BrambleCoreEagerSingletons;
import org.briarproject.briar.BriarCoreEagerSingletons;
import org.greenrobot.greendao.database.Database;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Handler;
import java.util.logging.Logger;

import dhbw.smartmoderation.connection.ConnectionService;
import dhbw.smartmoderation.connection.synchronization.SynchronizationService;
import dhbw.smartmoderation.connection.synchronization.SynchronizationServiceImpl;
import dhbw.smartmoderation.connection.synchronization.UpdateChecker;
import dhbw.smartmoderation.data.DataService;
import dhbw.smartmoderation.data.DataServiceImpl;
import dhbw.smartmoderation.data.model.DaoMaster;
import dhbw.smartmoderation.data.model.DaoSession;
import dhbw.smartmoderation.data.model.GroupUpdateObserver;
import dhbw.smartmoderation.util.Client;
import dhbw.smartmoderation.util.ExceptionHandlingActivity;
import dhbw.smartmoderation.util.WebServer;

public class SmartModerationApplicationImpl extends Application
        implements SmartModerationApplication {

    private static final Logger LOG =
            getLogger(SmartModerationApplicationImpl.class.getName());

    public SmartModerationComponent smartModerationComponent;

    private static Application app;
    private ConnectionService connectionService;
    private DataService dataService;
    private SynchronizationService synchronizationService;
    private UpdateChecker updateChecker;
    private Thread updateThread;
    private ExceptionHandlingActivity exceptionHandlingActivity;
    private WebServer webServer;
    private Client client;

    @Override
    public void onCreate() {
        super.onCreate();

        app = this;

        smartModerationComponent = createApplicationComponent();
        Logger rootLogger = getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        // Disable the Android logger for release builds
        for (Handler handler : handlers) rootLogger.removeHandler(handler);

        connectionService = smartModerationComponent.getSmartModerationBriarService();
        dataService = new DataServiceImpl();
        synchronizationService = new SynchronizationServiceImpl();

        for (GroupUpdateObserver group : dataService.getGroups()) {
            synchronizationService.addGroupUpdateObserver(group);
        }

        updateChecker = new UpdateChecker(synchronizationService, connectionService);
        updateThread = new Thread(updateChecker);
        //updateThread.start();

        webServer = new WebServer(this);
        client = new Client();

        LOG.info("Created");

        EmojiManager.install(new GoogleEmojiProvider());

    }

    protected SmartModerationComponent createApplicationComponent() {
        SmartModerationComponent smartModerationComponent = DaggerSmartModerationComponent.builder()
                .smartModerationModule(new SmartModerationModule(this))
                .build();

        // We need to load the eager singletons directly after making the
        // dependency graphs
        BrambleCoreEagerSingletons.Helper
                .injectEagerSingletons(smartModerationComponent);
		/*BrambleAndroidEagerSingletons.Helper
				.injectEagerSingletons(smartModerationComponent);*/
        BriarCoreEagerSingletons.Helper.injectEagerSingletons(smartModerationComponent);
        //AndroidEagerSingletons.Helper.injectEagerSingletons(smartModerationComponent);
        return smartModerationComponent;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public static Application getApp() {
        return app;
    }

    public ConnectionService getConnectionService() {
        return connectionService;
    }

    public DataService getDataService() {
        return dataService;
    }

    public Long getUniqueId() {
        return UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
    }

    public DaoSession getDaoSession() {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "notes-db");
        Database db = helper.getWritableDb();
        DaoSession daoSession = new DaoMaster(db).newSession();
        return daoSession;
    }

    public SynchronizationService getSynchronizationService() {
        return synchronizationService;
    }

    public void setCurrentActivity(ExceptionHandlingActivity exceptionHandlingActivity) {
        this.exceptionHandlingActivity = exceptionHandlingActivity;
    }

    public ExceptionHandlingActivity getCurrentActivity() {
        return exceptionHandlingActivity;
    }

    public void startWebServer() {
        if (webServer == null)  webServer = new WebServer(this);
        if (!webServer.isAlive()) {
            try {
                webServer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void stopWebServer() {

        if (webServer.isAlive()) {

            webServer.stop();
        }

    }

    public int getServerPort() {

        return webServer.getListeningPort();
    }

    public Long getMeetingId() {

        return this.webServer.getMeetingId();
    }

    public void setMeetingId(Long currentMeetingId) {

        this.webServer.setMeetingId(currentMeetingId);
    }

    public WebServer getWebServer() {

        return this.webServer;
    }

    public Client getClient() {
        return client;
    }


    @Override
    public SmartModerationComponent getSmartModerationComponent() {
        return smartModerationComponent;
    }

    @Override
    public boolean isRunningInBackground() {
        RunningAppProcessInfo info = new RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(info);
        return (info.importance != IMPORTANCE_FOREGROUND);
    }

    @Override
    public boolean isInstrumentationTest() {
        return false;
    }

    @Override
    public BrambleAppComponent getBrambleAppComponent() {
        return null;
    }
}
