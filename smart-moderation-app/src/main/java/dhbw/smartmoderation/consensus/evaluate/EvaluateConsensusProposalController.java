package dhbw.smartmoderation.consensus.evaluate;

import org.briarproject.bramble.api.identity.LocalAuthor;
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
import dhbw.smartmoderation.exceptions.MemberNotFoundException;
import dhbw.smartmoderation.exceptions.PollNotFoundException;
import dhbw.smartmoderation.util.Util;

public class EvaluateConsensusProposalController extends SmartModerationController {

    private Long pollId;

    public EvaluateConsensusProposalController(Long pollId) {
        this.pollId = pollId;
    }

    public void update() throws PollNotFoundException, GroupNotFoundException {
        this.synchronizationService.pull(getPrivateGroup());
    }

    public Poll getPoll() throws PollNotFoundException {
        return dataService.getPoll(this.pollId);
    }

    public Member getMember() throws MemberNotFoundException {
        LocalAuthor author = connectionService.getLocalAuthor();
        return dataService.getMember(author);
    }

    public Collection<ConsensusLevel> getConsensusLevels() throws PollNotFoundException {
        Group group = getPoll().getMeeting().getGroup();
        return group.getGroupSettings().getConsensusLevels();
    }

    public int getVoteMembersCount() throws PollNotFoundException {
        return getPoll().getMeeting().getPresentVoteMembers().size();
    }

    public int getVoiceCount() throws PollNotFoundException {
        return getPoll().getVoices().size();
    }

    public PrivateGroup getPrivateGroup() throws GroupNotFoundException, PollNotFoundException {
        Collection<PrivateGroup> privateGroups = connectionService.getGroups();
        for (PrivateGroup group : privateGroups) {
            if (getPoll().getMeeting().getGroup().getGroupId().equals(Util.bytesToLong(group.getId().getBytes()))) {
                return group;
            }
        }
        throw new GroupNotFoundException();
    }

    public void createVoice(Voice previousVoice, ConsensusLevel consensusLevel, String description) throws CantSendVoiceException, PollNotFoundException {

        Voice voice = null;

        try {
            voice = new Voice();
            if (previousVoice != null) {
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

        } catch (GroupNotFoundException | MemberNotFoundException exception) {
            dataService.deleteVoice(voice);
            throw new CantSendVoiceException();
        }
    }
}
