package dhbw.smartmoderation.moderationCard;

import org.briarproject.briar.api.privategroup.PrivateGroup;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;

import dhbw.smartmoderation.controller.SmartModerationController;
import dhbw.smartmoderation.data.model.Meeting;
import dhbw.smartmoderation.data.model.ModelClass;
import dhbw.smartmoderation.data.model.ModerationCard;
import dhbw.smartmoderation.data.model.Voice;
import dhbw.smartmoderation.exceptions.CantCreateModerationCardException;
import dhbw.smartmoderation.exceptions.CantEditModerationCardException;
import dhbw.smartmoderation.exceptions.CouldNotDeleteModerationCard;
import dhbw.smartmoderation.exceptions.GroupNotFoundException;
import dhbw.smartmoderation.exceptions.ModerationCardNotFoundException;
import dhbw.smartmoderation.util.Util;

public class ModerationCardsController extends SmartModerationController {
    public long meetingId;

    public ModerationCardsController(long meetingId) {
        this.meetingId = meetingId;
    }

    private Meeting getMeeting() {

        for (Meeting meeting : dataService.getMeetings()) {

            if (meeting.getMeetingId().equals(this.meetingId)) {
                return meeting;
            }
        }

        return null;
    }

    public PrivateGroup getPrivateGroup() throws GroupNotFoundException {

        Collection<PrivateGroup> privateGroups = connectionService.getGroups();

        for (PrivateGroup group : privateGroups) {

            if (getMeeting().getGroup().getGroupId().equals(Util.bytesToLong(group.getId().getBytes()))) {
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

        } catch (GroupNotFoundException exception) {

            dataService.deleteModerationCard(moderationCard);
            throw new CantCreateModerationCardException();

        }
    }

    public void editModerationCard(String content, int color, long cardId) throws ModerationCardNotFoundException, CantEditModerationCardException {
        Meeting meeting = this.getMeeting();

        ModerationCard moderationCard = new ModerationCard();

        try {

            moderationCard.setContent(content);
            moderationCard.setColor(color);
            moderationCard.setCardId(cardId);
            moderationCard.setMeeting(meeting);
            dataService.mergeModerationCard(moderationCard);

            Collection<ModelClass> data = new ArrayList<>();
            data.add(moderationCard);
            synchronizationService.push(getPrivateGroup(), data);

        } catch (GroupNotFoundException exception) {
            throw new CantEditModerationCardException();
        }
    }

    public void deleteModerationCard(long cardId) throws CouldNotDeleteModerationCard, ModerationCardNotFoundException {

        PrivateGroup group;

        try {

            group = getPrivateGroup();

        } catch (GroupNotFoundException groupNotFoundException) {

            throw new CouldNotDeleteModerationCard();
        }

        ModerationCard moderationCard = dataService.getModerationCard(cardId);
        dataService.deleteModerationCard(moderationCard);

        Collection<ModelClass> data = new ArrayList<>();
        moderationCard.setIsDeleted(true);
        data.add(moderationCard);
        synchronizationService.push(group, data);

    }

    public Collection<ModerationCard> getAllModerationCards() throws JSONException {
        Collection<ModerationCard> cards = dataService.getModerationCards();
        JSONArray cardsArray = new JSONArray();
        for (ModerationCard card: cards) {
            JSONObject cardJSON = new JSONObject();
            cardJSON.put("cardId", card.getCardId());
            cardJSON.put("content", card.getContent());
            String  hexColor = String.format("#%06X", (0xFFFFFF & card.getColor()));
            cardJSON.put("color", hexColor);
            cardJSON.put("meetingId", card.getMeetingId());
            cardsArray.put(cardJSON);
            cardsArray.put(cardJSON);
        }
        String s = cardsArray.toString();
        System.out.println(s);
        return dataService.getModerationCards();
    }
}
