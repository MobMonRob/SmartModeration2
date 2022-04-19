package dhbw.smartmoderation.moderationCard.detail;

import org.briarproject.briar.api.privategroup.PrivateGroup;

import java.util.ArrayList;
import java.util.Collection;

import dhbw.smartmoderation.controller.SmartModerationController;
import dhbw.smartmoderation.data.model.Meeting;
import dhbw.smartmoderation.data.model.ModelClass;
import dhbw.smartmoderation.data.model.ModerationCard;
import dhbw.smartmoderation.exceptions.CantEditModerationCardException;
import dhbw.smartmoderation.exceptions.CouldNotDeleteModerationCard;
import dhbw.smartmoderation.exceptions.GroupNotFoundException;
import dhbw.smartmoderation.exceptions.MeetingNotFoundException;
import dhbw.smartmoderation.exceptions.ModerationCardNotFoundException;
import dhbw.smartmoderation.util.Util;

public class DetailModerationCardController extends SmartModerationController {
    public long meetingId;

    public DetailModerationCardController(long meetingId) {
        this.meetingId = meetingId;
    }

    private Meeting getMeeting() throws MeetingNotFoundException {
        return dataService.getMeeting(meetingId);
    }


    public ModerationCard editModerationCard(String content, String author, int backgroundColor, int fontColor, long cardId) throws ModerationCardNotFoundException, CantEditModerationCardException, MeetingNotFoundException {
        Meeting meeting = null;
        meeting = this.getMeeting();
        ModerationCard moderationCard = new ModerationCard();
        try {
            moderationCard.setCardId(cardId);
            moderationCard.setContent(content);
            moderationCard.setAuthor(author);
            moderationCard.setBackgroundColor(backgroundColor);
            moderationCard.setFontColor(fontColor);
            moderationCard.setMeeting(meeting);
            dataService.mergeModerationCard(moderationCard);
            Collection<ModelClass> data = new ArrayList<>();
            data.add(moderationCard);
            synchronizationService.push(getPrivateGroup(meeting.getGroupId()), data);
        } catch (GroupNotFoundException exception) {
            throw new CantEditModerationCardException();
        }
        return moderationCard;
    }

    public void deleteModerationCard(long cardId) throws CouldNotDeleteModerationCard, ModerationCardNotFoundException, MeetingNotFoundException {
        PrivateGroup group;
        try {
            group = getPrivateGroup(getMeeting().getGroupId());

        } catch (GroupNotFoundException | MeetingNotFoundException groupNotFoundException) {
            throw new CouldNotDeleteModerationCard();
        }
        ModerationCard moderationCard = dataService.getModerationCard(cardId);
        dataService.deleteModerationCard(cardId);
        Collection<ModelClass> data = new ArrayList<>();
        moderationCard.setIsDeleted(true);
        data.add(moderationCard);
        synchronizationService.push(group, data);
    }
}
