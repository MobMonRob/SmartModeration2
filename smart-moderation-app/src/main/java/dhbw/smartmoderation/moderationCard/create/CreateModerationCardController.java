package dhbw.smartmoderation.moderationCard.create;

import org.briarproject.briar.api.privategroup.PrivateGroup;

import java.util.ArrayList;
import java.util.Collection;

import dhbw.smartmoderation.controller.SmartModerationController;
import dhbw.smartmoderation.data.model.Meeting;
import dhbw.smartmoderation.data.model.ModelClass;
import dhbw.smartmoderation.data.model.ModerationCard;
import dhbw.smartmoderation.exceptions.CantCreateModerationCardException;
import dhbw.smartmoderation.exceptions.GroupNotFoundException;
import dhbw.smartmoderation.exceptions.MeetingNotFoundException;
import dhbw.smartmoderation.exceptions.ModerationCardNotFoundException;
import dhbw.smartmoderation.util.Util;

public class CreateModerationCardController extends SmartModerationController {
    public long meetingId;

    public CreateModerationCardController(long meetingId) {
        this.meetingId = meetingId;
    }

    private Meeting getMeeting() throws MeetingNotFoundException {
        return dataService.getMeeting(meetingId);
    }


    public ModerationCard createModerationCard(String content, String author, int backgroundColor, int fontColor) throws CantCreateModerationCardException, ModerationCardNotFoundException, MeetingNotFoundException {
        Meeting meeting = this.getMeeting();

        ModerationCard moderationCard = new ModerationCard();

        try {
            moderationCard.setContent(content);
            moderationCard.setBackgroundColor(backgroundColor);
            moderationCard.setFontColor(fontColor);
            moderationCard.setMeeting(meeting);
            moderationCard.setAuthor(author);
            dataService.mergeModerationCard(moderationCard);

            Collection<ModelClass> data = new ArrayList<>();
            data.add(moderationCard);
            synchronizationService.push(getPrivateGroup(meeting.getGroupId()), data);

        } catch (GroupNotFoundException exception) {
            dataService.deleteModerationCard(moderationCard.getCardId());
            throw new CantCreateModerationCardException();
        }
        return moderationCard;
    }

    public String getLocalAuthorName() {
        return connectionService.getLocalAuthor().getName();
    }
}
