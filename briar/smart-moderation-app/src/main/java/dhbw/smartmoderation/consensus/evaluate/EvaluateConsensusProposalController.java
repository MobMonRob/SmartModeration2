package dhbw.smartmoderation.consensus.evaluate;

import org.briarproject.briar.api.privategroup.PrivateGroup;
import java.util.ArrayList;
import java.util.Collection;
import dhbw.smartmoderation.controller.SmartModerationController;
import dhbw.smartmoderation.data.model.ConsensusLevel;
import dhbw.smartmoderation.data.model.Group;
import dhbw.smartmoderation.data.model.Member;
import dhbw.smartmoderation.data.model.ModelClass;
import dhbw.smartmoderation.data.model.Poll;
import dhbw.smartmoderation.data.model.Voice;
import dhbw.smartmoderation.exceptions.CantSendVoiceException;
import dhbw.smartmoderation.exceptions.GroupNotFoundException;
import dhbw.smartmoderation.util.Util;

public class EvaluateConsensusProposalController extends SmartModerationController {

    private Long pollId;

    public EvaluateConsensusProposalController(Long pollId) {

        this.pollId = pollId;
    }

    public void update() {

        try {

            this.synchronizationService.pull(getPrivateGroup());

        } catch (GroupNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Poll getPoll() {

        for(Poll poll : dataService.getPolls()) {
            if(poll.getPollId().equals(this.pollId)) {
                return poll;
            }
        }
        return null;
    }

    public Member getMember() {

        Long authorId = connectionService.getLocalAuthorId();

        for(Member member : dataService.getMembers()) {

            if(member.getMemberId().equals(authorId)) {
                return member;
            }
        }
        return null;
    }

    public Collection<ConsensusLevel> getConsensusLevels() {

        Group group = getPoll().getMeeting().getGroup();
        return group.getGroupSettings().getConsensusLevels();
    }

   public int getVoteMembersCount() {

        return getPoll().getMeeting().getPresentVoteMembers().size();
    }

    public int getVoiceCount() {

        return getPoll().getVoices().size();
    }

    public PrivateGroup getPrivateGroup() throws GroupNotFoundException {

        Collection<PrivateGroup> privateGroups = connectionService.getGroups();

        for(PrivateGroup group : privateGroups) {

            if(getPoll().getMeeting().getGroup().getGroupId().equals(Util.bytesToLong(group.getId().getBytes()))) {

                return group;
            }
        }

        throw new GroupNotFoundException();
    }

    public void createVoice(Voice previousVoice, ConsensusLevel consensusLevel, String description) throws CantSendVoiceException {

        Voice voice = null;

        try{

            voice = new Voice();

            if(previousVoice != null) {

                voice = previousVoice;
            }

            voice.setConsensusLevel(consensusLevel);
            voice.setExplanation(description);
            voice.setPoll(getPoll());
            voice.setMember(getMember());
            dataService.mergeVoice(voice);

            Collection<ModelClass> data = new ArrayList<>();
            data.add(voice);
            synchronizationService.push(getPrivateGroup(), data);

        } catch (GroupNotFoundException exception){

            dataService.deleteVoice(voice);
            throw new CantSendVoiceException();
        }


    }

}
