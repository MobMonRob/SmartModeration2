package dhbw.smartmoderation.group.invitations;

import org.briarproject.bramble.api.identity.LocalAuthor;
import org.briarproject.briar.api.privategroup.PrivateGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import dhbw.smartmoderation.connection.GroupInvitationVisitor;
import dhbw.smartmoderation.connection.Invitation;
import dhbw.smartmoderation.controller.SmartModerationController;
import dhbw.smartmoderation.data.model.Attendance;
import dhbw.smartmoderation.data.model.Group;
import dhbw.smartmoderation.data.model.Meeting;
import dhbw.smartmoderation.data.model.Member;
import dhbw.smartmoderation.data.model.MemberGroupRelation;
import dhbw.smartmoderation.data.model.MemberMeetingRelation;
import dhbw.smartmoderation.data.model.ModelClass;
import dhbw.smartmoderation.data.model.Role;
import dhbw.smartmoderation.exceptions.GroupNotFoundException;
import dhbw.smartmoderation.exceptions.MemberNotFoundException;
import dhbw.smartmoderation.exceptions.NoContactsFoundException;

/**
 * Controller for {@link ListInvitationsActivity}.
 */
class ListInvitationsController extends SmartModerationController {

	private static final String TAG = ListInvitationsController.class.getSimpleName();

	private GroupInvitationVisitor groupInvitationVisitor;

	private List<PrivateGroup> joinedGroups;

	public ListInvitationsController() {

		groupInvitationVisitor = new GroupInvitationVisitor();
		joinedGroups = new ArrayList<>();
	}

	public List<Invitation> getGroupInvitations() throws NoContactsFoundException {
		return connectionService.getGroupInvitations();
	}

	public void acceptInvitation(Invitation invitation) {
		connectionService.acceptGroupInvitation(invitation);
		joinedGroups.add(invitation.getPrivateGroup());
	}

	public void rejectInvitation(Invitation invitation) {
		connectionService.rejectGroupInvitation(invitation);
	}


	void synchronizeData() throws MemberNotFoundException, GroupNotFoundException {

		for (PrivateGroup joinedGroup : joinedGroups) {
			boolean success = false;
			while(!success) {
				success = synchronizationService.pull(joinedGroup);
			}

			LocalAuthor localAuthor = connectionService.getLocalAuthor();
			Member local = new Member(localAuthor);
			local.setName(localAuthor.getName());
			dataService.saveMember(local);

			Group group = dataService.getGroup(joinedGroup);

			MemberGroupRelation memberGroupRelation = group.addMember(local, Role.PARTICIPANT);

			if(memberGroupRelation != null) {
				dataService.saveMemberGroupRelation(memberGroupRelation);
			}

			ArrayList<Meeting> meetings = new ArrayList<>();

			for(Meeting meeting : group.getMeetings()) {
				MemberMeetingRelation memberMeetingRelation = meeting.addMember(local, Attendance.ABSENT);
				dataService.saveMemberMeetingRelation(memberMeetingRelation);
				meetings.add(meeting);
			}

			Collection<ModelClass> data = new ArrayList<>();
			data.add(group);
			data.add(local);
			data.addAll(meetings);

			synchronizationService.push(joinedGroup, data);
		}

		joinedGroups.clear();
	}
}
