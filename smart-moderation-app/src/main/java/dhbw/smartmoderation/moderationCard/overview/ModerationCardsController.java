package dhbw.smartmoderation.moderationCard.overview;

import org.briarproject.briar.api.privategroup.PrivateGroup;

import java.util.Collection;

import dhbw.smartmoderation.controller.SmartModerationController;
import dhbw.smartmoderation.data.model.Group;
import dhbw.smartmoderation.data.model.Meeting;
import dhbw.smartmoderation.data.model.Member;
import dhbw.smartmoderation.data.model.ModerationCard;
import dhbw.smartmoderation.data.model.Role;
import dhbw.smartmoderation.exceptions.GroupNotFoundException;
import dhbw.smartmoderation.exceptions.MeetingNotFoundException;
import dhbw.smartmoderation.util.Util;

public class ModerationCardsController extends SmartModerationController {
    public long meetingId;

    public ModerationCardsController(long meetingId) {
        this.meetingId = meetingId;
    }

    private Meeting getMeeting() throws MeetingNotFoundException {
        return dataService.getMeeting(meetingId);
    }

    public Collection<ModerationCard> getAllModerationCards() throws MeetingNotFoundException, GroupNotFoundException {
       this.synchronizationService.pull(getPrivateGroup(getMeeting().getGroupId()));
        return getMeeting().getModerationCards();
    }
    public boolean isLocalAuthorModerator() throws MeetingNotFoundException {
        Group group = getMeeting().getGroup();
        Long authorId = connectionService.getLocalAuthorId();
        Member member = group.getMember(authorId);
        return member != null && member.getRoles(group).contains(Role.MODERATOR);
    }
}
