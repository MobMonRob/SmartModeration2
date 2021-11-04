package dhbw.smartmoderation;

import android.app.Application;

import com.orm.SugarApp;

import org.briarproject.bramble.BrambleCoreEagerSingletons;
import org.briarproject.briar.BriarCoreEagerSingletons;
import org.greenrobot.greendao.database.Database;

import java.io.IOException;
import java.util.UUID;

import dhbw.smartmoderation.connection.ConnectionService;
import dhbw.smartmoderation.connection.DaggerSmartModerationComponent;
import dhbw.smartmoderation.connection.SmartModerationComponent;
import dhbw.smartmoderation.connection.synchronization.SerializationService;
import dhbw.smartmoderation.connection.synchronization.SynchronizationService;
import dhbw.smartmoderation.connection.synchronization.SynchronizationServiceImpl;
import dhbw.smartmoderation.data.DataService;
import dhbw.smartmoderation.data.DataServiceImpl;
import dhbw.smartmoderation.data.model.DaoMaster;
import dhbw.smartmoderation.data.model.DaoSession;
import dhbw.smartmoderation.data.model.GroupUpdateObserver;
import dhbw.smartmoderation.connection.synchronization.UpdateChecker;
import dhbw.smartmoderation.util.ExceptionHandlingActivity;
import dhbw.smartmoderation.util.WebServer;

/**
 * The application class for the Smart Moderation App.
 */
// Extends SugarApp for using Sugar ORM
public class SmartModerationApplication extends SugarApp {

	private static Application app;
	private ConnectionService connectionService;
	private DataService dataService;
	private SynchronizationService synchronizationService;
	private UpdateChecker updateChecker;
	private Thread updateThread;
	private ExceptionHandlingActivity exceptionHandlingActivity;
	private WebServer webServer;
	public SmartModerationComponent comp;

	@Override
	public void onCreate() {
		super.onCreate();

		app = this;

		// DaggerSmartModerationComponent is a generated class, run app at least once to resolve errors
		comp = DaggerSmartModerationComponent.create();
		BrambleCoreEagerSingletons.Helper.injectEagerSingletons(comp);
		BriarCoreEagerSingletons.Helper.injectEagerSingletons(comp);

		connectionService = comp.getSmartModerationBriarService();
		dataService = new DataServiceImpl();
		synchronizationService = new SynchronizationServiceImpl();

		for(GroupUpdateObserver group : dataService.getGroups()){
			synchronizationService.addGroupUpdateObserver(group);
		}

		updateChecker = new UpdateChecker(synchronizationService,connectionService);
		updateThread = new Thread(updateChecker);
		//updateThread.start();

		webServer = new WebServer(this);
	}

	/**
	 * Statically get the instance of the running Application.
	 *
	 * @return The Application instance
	 */
	public static Application getApp() {

		return app;
	}

	/**
	 * Get the current instance of the ConnectionService
	 *
	 * @return The ConnectionService instance
	 */
	public ConnectionService getConnectionService() {

		return connectionService;
	}


	/**
	 * Get the current instance of the DataService
	 *
	 * @return The DataService instance
	 */
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


	/**
	 * Get the current instance of the SynchronizationService
	 *
	 * @return The SynchronizationService instance
	 */
	public SynchronizationService getSynchronizationService() {

		return synchronizationService;
	}

	public void setCurrentActivity(ExceptionHandlingActivity exceptionHandlingActivity){

		this.exceptionHandlingActivity = exceptionHandlingActivity;
	}

	public ExceptionHandlingActivity getCurrentActivity(){


		return exceptionHandlingActivity;

	}

	public void startWebServer() {

		if(!webServer.isAlive()) {

			try {

				webServer = new WebServer(this);
				webServer.start();

			} catch (IOException e) {

				e.printStackTrace();
			}
		}

	}

	public void stopWebServer() {

		if(webServer.isAlive()) {

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
}
