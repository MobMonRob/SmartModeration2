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
import dhbw.smartmoderation.util.Util;

public class CreateConsensusProposalController extends SmartModerationController {

    private Long meetingId;

    public CreateConsensusProposalController(Long meetingId) {
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


    public void createConsensusProposal(String title, String consensusProposal, String note) throws CantSendConsensusProposal{

        Meeting meeting = getMeeting();

        Poll poll = new Poll();

        try {

            poll.setTitle(title);
            poll.setConsensusProposal(consensusProposal);
            poll.setNote(note);
            poll.setIsOpen(false);
            poll.setClosedByModerator(false);
            poll.setMeeting(meeting);

            dataService.mergePoll(poll);

            Collection<ModelClass> data = new ArrayList<>();
            data.add(poll);
            synchronizationService.push(getPrivateGroup(), data);

        } catch (GroupNotFoundException exception){

            dataService.deletePoll(poll);
            throw new CantSendConsensusProposal();

        }

    }
}
