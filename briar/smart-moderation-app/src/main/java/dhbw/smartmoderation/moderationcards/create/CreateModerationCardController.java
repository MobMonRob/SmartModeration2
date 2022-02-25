package dhbw.smartmoderation.moderationcards.create;

import android.view.Display;

import org.briarproject.briar.api.privategroup.PrivateGroup;

import java.util.ArrayList;
import java.util.Collection;

import dhbw.smartmoderation.controller.SmartModerationController;
import dhbw.smartmoderation.data.model.Meeting;
import dhbw.smartmoderation.data.model.ModelClass;
import dhbw.smartmoderation.data.model.ModerationCard;
import dhbw.smartmoderation.data.model.Poll;
import dhbw.smartmoderation.exceptions.CantCreateModerationCardException;
import dhbw.smartmoderation.exceptions.CantSendConsensusProposal;
import dhbw.smartmoderation.exceptions.GroupNotFoundException;
import dhbw.smartmoderation.exceptions.ModerationCardNotFoundException;
import dhbw.smartmoderation.util.Util;

public class CreateModerationCardController extends SmartModerationController {
    public long meetingId;

    public CreateModerationCardController(long meetingId) {
        this.meetingId = meetingId;
    }
    private Meeting getMeeting() {

        for(Meeting meeting : dataService.getMeetings()) {

            if(meeting.getMeetingId().equals(this.meetingId)) {
                return meeting;
            }
        }

        return null;
    }

    public PrivateGroup getPrivateGroup() throws GroupNotFoundException {

        Collection<PrivateGroup> privateGroups = connectionService.getGroups();

        for(PrivateGroup group : privateGroups) {

            if(getMeeting().getGroup().getGroupId().equals(Util.bytesToLong(group.getId().getBytes()))) {
                return group;
            }
        }

        throw new GroupNotFoundException();
    }


    public void createModerationCard(String content, int color) throws CantCreateModerationCardException, ModerationCardNotFoundException {
        Meeting meeting = this.getMeeting();

        ModerationCard moderationCard = new ModerationCard();

        try {

            moderationCard.setContent(content);
            moderationCard.setColor(color);
            moderationCard.setMeeting(meeting);
            dataService.mergeModerationCard(moderationCard);

            Collection<ModelClass> data = new ArrayList<>();
            data.add(moderationCard);
            synchronizationService.push(getPrivateGroup(), data);

        } catch (GroupNotFoundException exception){

            dataService.deleteModerationCard(moderationCard);
            throw new CantCreateModerationCardException();

        }
    }
}
