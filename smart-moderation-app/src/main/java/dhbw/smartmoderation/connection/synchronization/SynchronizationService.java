package dhbw.smartmoderation.connection.synchronization;

import org.briarproject.bramble.api.identity.LocalAuthor;
import org.briarproject.briar.api.privategroup.PrivateGroup;

import java.util.Collection;

import dhbw.smartmoderation.data.events.GroupUpdateEvent;
import dhbw.smartmoderation.data.model.GroupUpdateObserver;
import dhbw.smartmoderation.data.model.ModelClass;

/**
 * A service for synchronising data in private groups.
 */
public interface SynchronizationService {

	/**
	 * Push data to a private group.
	 *
	 * @param group The private group to push the data to
	 * @param data The payload to push
	 */
	void push(PrivateGroup group, Collection<ModelClass> data);

	/**
	 * Get new data form a private group and save it to the local database.
	 *
	 * @param group The private group to get the data from
	 */
	boolean pull(PrivateGroup group);

	void addGroupUpdateObserver(GroupUpdateObserver observer);

	void fireGroupUpdateEvent(GroupUpdateEvent event);

	void setLocalAuthor(LocalAuthor localAuthor);
}
