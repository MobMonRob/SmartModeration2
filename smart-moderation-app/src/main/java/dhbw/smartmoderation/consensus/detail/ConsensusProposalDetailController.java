package dhbw.smartmoderation.consensus.detail;

import org.briarproject.briar.api.privategroup.PrivateGroup;

import dhbw.smartmoderation.controller.SmartModerationController;
import dhbw.smartmoderation.data.model.Poll;
import dhbw.smartmoderation.exceptions.GroupNotFoundException;
import dhbw.smartmoderation.util.Util;

public class ConsensusProposalDetailController extends SmartModerationController {

    private final Long pollId;

    public ConsensusProposalDetailController(Long pollId) { this.pollId = pollId; }

    public void update() {
        try {
            this.synchronizationService.pull(getPrivateGroup());
        } catch (GroupNotFoundException e) {
            e.printStackTrace();
        }
    }

    private PrivateGroup getPrivateGroup() throws GroupNotFoundException {
        Long groupId = getPoll().getMeeting().getGroup().getGroupId();

        for (PrivateGroup group : connectionService.getGroups()){
            Long privateGroupId = Util.bytesToLong(group.getId().getBytes());
            if (privateGroupId.equals(groupId)) {
                return group;
            }
        }
        throw new GroupNotFoundException();
    }

    public Poll getPoll() {
        for(Poll poll : dataService.getPolls()) {
            if(poll.getPollId().equals(pollId)) {
                return poll;
            }
        }
        return null;
    }
}
