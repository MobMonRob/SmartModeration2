package dhbw.smartmoderation.connection;

/**
 * An interface for handling group invitations.
 */
public interface GroupInvitationHandler {

	/**
	 * To be called, when a group invitation is accepted.
	 *
	 * @param invitation The invitation to accept
	 */
	void acceptGroupInvitation(Invitation invitation);

	/**
	 * To be called, when a group invitation is rejected.
	 *
	 * @param invitation The invitation to reject
	 */
	void rejectGroupInvitation(Invitation invitation);

}
