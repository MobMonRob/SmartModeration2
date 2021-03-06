package dhbw.smartmoderation.moderationCard.create;

import java.util.ArrayList;
import java.util.Collection;

import dhbw.smartmoderation.data.model.Meeting;
import dhbw.smartmoderation.data.model.ModelClass;
import dhbw.smartmoderation.data.model.ModerationCard;
import dhbw.smartmoderation.exceptions.CantCreateModerationCardException;
import dhbw.smartmoderation.exceptions.GroupNotFoundException;
import dhbw.smartmoderation.exceptions.MeetingNotFoundException;
import dhbw.smartmoderation.exceptions.ModerationCardNotFoundException;
import dhbw.smartmoderation.moderationCard.ModerationCardServiceController;

public class CreateModerationCardController {
    private final ModerationCardServiceController moderationCardServiceController;
    public long meetingId;

    public CreateModerationCardController(long meetingId, ModerationCardServiceController moderationCardServiceController) {
        this.meetingId = meetingId;
        this.moderationCardServiceController = moderationCardServiceController;
    }

    public CreateModerationCardController(long meetingId) {
        this.meetingId = meetingId;
        moderationCardServiceController = new ModerationCardServiceController();
    }

    private Meeting getMeeting() throws MeetingNotFoundException {
        return moderationCardServiceController.getDataService().getMeeting(meetingId);
    }

    public ModerationCard createModerationCard(String content, String author, int backgroundColor, int fontColor) throws CantCreateModerationCardException, ModerationCardNotFoundException, MeetingNotFoundException {
        Meeting meeting;
        meeting = this.getMeeting();

        ModerationCard moderationCard = new ModerationCard();

        try {
            moderationCard.setContent(content);
            moderationCard.setBackgroundColor(backgroundColor);
            moderationCard.setFontColor(fontColor);
            moderationCard.setMeeting(meeting);
            moderationCard.setAuthor(author);
            moderationCardServiceController.getDataService().mergeModerationCard(moderationCard);

            Collection<ModelClass> data = new ArrayList<>();
            data.add(moderationCard);
            moderationCardServiceController.getSynchronizationService().push(moderationCardServiceController.getPrivateGroup(meeting.getGroupId()), data);

        } catch (GroupNotFoundException exception) {
            moderationCardServiceController.getDataService().deleteModerationCard(moderationCard.getCardId());
            throw new CantCreateModerationCardException();
        }
        return moderationCard;
    }

    public String getLocalAuthorName() {
        return moderationCardServiceController.getConnectionService().getLocalAuthor().getName();
    }
}
