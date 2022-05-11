package dhbw.smartmoderation.connection.synchronization;

import android.util.Log;

import org.briarproject.bramble.api.identity.LocalAuthor;
import org.briarproject.briar.api.privategroup.GroupMessageHeader;
import org.briarproject.briar.api.privategroup.PrivateGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import dhbw.smartmoderation.SmartModerationApplicationImpl;
import dhbw.smartmoderation.connection.ConnectionService;
import dhbw.smartmoderation.data.DataService;
import dhbw.smartmoderation.data.events.GroupUpdateEvent;
import dhbw.smartmoderation.data.model.GroupUpdateObserver;
import dhbw.smartmoderation.data.model.ModelClass;
import dhbw.smartmoderation.data.model.Synchronization;
import dhbw.smartmoderation.data.model.SynchronizationDao;
import dhbw.smartmoderation.exceptions.NoGroupHeadersFoundException;
import dhbw.smartmoderation.exceptions.NoGroupMessageTextFoundException;
import dhbw.smartmoderation.util.Util;

public class SynchronizationServiceImpl implements SynchronizationService {

	private final String TAG = SynchronizationServiceImpl.class.getSimpleName();
	private final ConnectionService connectionService = ((SmartModerationApplicationImpl)SmartModerationApplicationImpl.getApp()).getConnectionService();
	private final DataService dataService = ((SmartModerationApplicationImpl) SmartModerationApplicationImpl.getApp()).getDataService();
	private final SerializationService serializationService;
	private final List<GroupUpdateObserver> groupUpdateObservers = new ArrayList<GroupUpdateObserver>();

	public void setLocalAuthor(LocalAuthor localAuthor) {
		serializationService.setLocalAuthor(localAuthor);
	}

	public SynchronizationServiceImpl() {
		serializationService = new SerializationServiceImpl();
	}

	public void push(PrivateGroup group, Collection<ModelClass> data) {
		connectionService.sendToGroup(serializationService.bulkSerialize(data), group);
	}

	public boolean pull(PrivateGroup group) {

		boolean success = false;
		long lastSynchronized = 0;

		SynchronizationDao synchronizationDao = ((SmartModerationApplicationImpl)SmartModerationApplicationImpl.getApp()).getDaoSession().getSynchronizationDao();
		List<Synchronization> synchronizations= synchronizationDao.loadAll();
		Synchronization sync = null;

		for (Synchronization synchronization : synchronizations) {
			if(synchronization.getGroupId() == Util.bytesToLong(group.getId().getBytes())) {
				sync = synchronization;
			}
		}

		if(sync != null) {
			lastSynchronized = sync.getLastSynchronizedGroupMessageTimestamp();
		}

		Collection<GroupMessageHeader> headers;
		ArrayList<GroupMessageHeader> headerList;

		try {
			 headers = connectionService.getMessageHeaders(group);
			 headerList = new ArrayList<>(headers);

			 Collections.sort(headerList, ((o1, o2) -> {
				if(o1.getTimestamp() < o2.getTimestamp()) {
					return -1;
				}
				else if(o1.getTimestamp() > o2.getTimestamp()) {
					return 1;
				}
				return 0;
			 }));

		} catch (NoGroupHeadersFoundException exception) {
			headerList = new ArrayList<>();
			Log.d(TAG,exception.getMessage());
		}

		long newTimestamp = lastSynchronized;

		for (GroupMessageHeader header : headerList) {
			if (!header.getAuthor().getId().equals(connectionService.getLocalAuthor().getId()) && header.getTimestamp() > lastSynchronized) {
				if (header.getTimestamp() > newTimestamp) {
					newTimestamp = header.getTimestamp();
				}

				try{
					String data = connectionService.getGroupMessageText(header.getId(), group.getId().toString());
					if (data != null) {
						ModelClassData modelClassData = serializationService.bulkDeserializeAndMerge(data);
						fireGroupUpdateEvent(new GroupUpdateEvent(Util.bytesToLong(group.getId().getBytes())));
						if (sync == null) {
							sync = new Synchronization();
							sync.setGroupId(Util.bytesToLong(group.getId().getBytes()));
						}

						sync.setLastSynchronizedGroupMessageTimestamp(newTimestamp);
						synchronizationDao.insertOrReplaceInTx(sync);
						success = true;
					}
				} catch (NoGroupMessageTextFoundException exception){
					Log.d(TAG,exception.getMessage());
				}
			}
		}
		return success;
	}

	@Override
	public void addGroupUpdateObserver(GroupUpdateObserver observer) {

		if (groupUpdateObservers.contains(observer)){
			return;
		}

		this.groupUpdateObservers.add(observer);
	}

	@Override
	public void deleteGroupUpdateObserver(GroupUpdateObserver observer) {

		groupUpdateObservers.remove(observer);
	}

	@Override
	public void fireGroupUpdateEvent(GroupUpdateEvent event) {

		for (GroupUpdateObserver observer : groupUpdateObservers){

			 observer.update(event);
		}
	}

}
