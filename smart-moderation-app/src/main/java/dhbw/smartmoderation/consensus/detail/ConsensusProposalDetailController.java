package dhbw.smartmoderation.consensus.detail;

import org.briarproject.briar.api.privategroup.PrivateGroup;

import dhbw.smartmoderation.controller.SmartModerationController;
import dhbw.smartmoderation.data.model.Poll;
import dhbw.smartmoderation.exceptions.GroupNotFoundException;
import dhbw.smartmoderation.exceptions.PollNotFoundException;
import dhbw.smartmoderation.util.Util;

public class ConsensusProposalDetailController extends SmartModerationController {

    private Long pollId;

    public ConsensusProposalDetailController(Long pollId) { this.pollId = pollId; }

    public void update() throws PollNotFoundException {
        try {
            this.synchronizationService.pull(getPrivateGroup());
        } catch (GroupNotFoundException e) {
            e.printStackTrace();
        }
    }

    private PrivateGroup getPrivateGroup() throws GroupNotFoundException, PollNotFoundException {
        Long groupId = getPoll().getMeeting().getGroup().getGroupId();

        for (PrivateGroup group : connectionService.getGroups()){
            Long privateGroupId = Util.bytesToLong(group.getId().getBytes());
            if (privateGroupId.equals(groupId)) {
                return group;
            }
        }
        throw new GroupNotFoundException();
    }

    public Poll getPoll() throws PollNotFoundException {
        return dataService.getPoll(pollId);
    }
}
