package dhbw.smartmoderation.connection;

import org.briarproject.bramble.api.contact.ContactId;
import org.briarproject.briar.api.privategroup.PrivateGroup;

/**
 * A class encapsulating the relevant information for a group invitation.
 */
public class Invitation {

	private ContactId contactId;
	private PrivateGroup privateGroup;

	public Invitation(ContactId contactId, PrivateGroup privateGroup) {
		this.contactId = contactId;
		this.privateGroup = privateGroup;
	}

	/**
	 * Get the contact id of the group owner.
	 *
	 * @return The group owners contact id
	 */
	public ContactId getContactId() {
		return contactId;
	}

	/**
	 * Get the private group for which this invitation is.
	 *
	 * @return The private group
	 */
	public PrivateGroup getPrivateGroup() {
		return privateGroup;
	}

}
