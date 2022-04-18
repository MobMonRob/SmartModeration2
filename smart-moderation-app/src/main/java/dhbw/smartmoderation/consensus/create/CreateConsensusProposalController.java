package dhbw.smartmoderation.consensus.create;

import org.briarproject.briar.api.privategroup.PrivateGroup;

import java.util.ArrayList;
import java.util.Collection;

import dhbw.smartmoderation.controller.SmartModerationController;
import dhbw.smartmoderation.data.model.Meeting;
import dhbw.smartmoderation.data.model.ModelClass;
import dhbw.smartmoderation.data.model.Poll;
import dhbw.smartmoderation.exceptions.CantSendConsensusProposal;
import dhbw.smartmoderation.exceptions.GroupNotFoundException;
import dhbw.smartmoderation.exceptions.MeetingNotFoundException;
import dhbw.smartmoderation.util.Util;

public class CreateConsensusProposalController extends SmartModerationController {

    private Long meetingId;

    public CreateConsensusProposalController(Long meetingId) {
        this.meetingId = meetingId;
    }

    private Meeting getMeeting() throws MeetingNotFoundException {
        return dataService.getMeeting(meetingId);
    }


    public void createConsensusProposal(String title, String consensusProposal, String note) throws CantSendConsensusProposal {
        Poll poll = new Poll();
        try {
            Meeting meeting = getMeeting();
            poll.setTitle(title);
            poll.setConsensusProposal(consensusProposal);
            poll.setNote(note);
            poll.setIsOpen(false);
            poll.setClosedByModerator(false);
            poll.setMeeting(meeting);
            dataService.mergePoll(poll);
            Collection<ModelClass> data = new ArrayList<>();
            data.add(poll);
            synchronizationService.push(getPrivateGroup(meeting.getGroupId()), data);
        } catch (GroupNotFoundException | MeetingNotFoundException exception) {
            dataService.deletePoll(poll);
            throw new CantSendConsensusProposal();
        }
    }
}
