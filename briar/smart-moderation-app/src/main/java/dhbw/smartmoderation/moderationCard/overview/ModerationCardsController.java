package dhbw.smartmoderation.moderationCard.overview;

import org.briarproject.briar.api.privategroup.PrivateGroup;

import java.util.Collection;

import dhbw.smartmoderation.controller.SmartModerationController;
import dhbw.smartmoderation.data.model.Meeting;
import dhbw.smartmoderation.data.model.ModerationCard;
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

    public PrivateGroup getPrivateGroup() throws GroupNotFoundException {
        Collection<PrivateGroup> privateGroups = connectionService.getGroups();

        for (PrivateGroup group : privateGroups) {
            try {
                if (getMeeting().getGroup().getGroupId().equals(Util.bytesToLong(group.getId().getBytes()))) {
                    return group;
                }
            } catch (MeetingNotFoundException e) {
                e.printStackTrace();
            }
        }
        throw new GroupNotFoundException();
    }

    public Collection<ModerationCard> getAllModerationCards() {
        try {
            this.synchronizationService.pull(getPrivateGroup());
        } catch (GroupNotFoundException e) {
            e.printStackTrace();
        }
        try {
            return getMeeting().getModerationCards();
        } catch (MeetingNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
