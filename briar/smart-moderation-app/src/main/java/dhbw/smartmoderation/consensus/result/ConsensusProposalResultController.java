package dhbw.smartmoderation.consensus.result;

import org.briarproject.briar.api.privategroup.PrivateGroup;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.TreeMap;
import dhbw.smartmoderation.controller.SmartModerationController;
import dhbw.smartmoderation.data.model.ConsensusLevel;
import dhbw.smartmoderation.data.model.Group;
import dhbw.smartmoderation.data.model.Member;
import dhbw.smartmoderation.data.model.ModelClass;
import dhbw.smartmoderation.data.model.Poll;
import dhbw.smartmoderation.data.model.Role;
import dhbw.smartmoderation.data.model.Voice;
import dhbw.smartmoderation.exceptions.GroupNotFoundException;
import dhbw.smartmoderation.exceptions.PollCantBeClosedException;
import dhbw.smartmoderation.exceptions.PollNotFoundException;
import dhbw.smartmoderation.util.Util;

public class ConsensusProposalResultController extends SmartModerationController {

    private Long pollId;

    public ConsensusProposalResultController(Long pollId) {

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

        try {

            return dataService.getPoll(pollId);

        } catch (PollNotFoundException e) {

            e.printStackTrace();
        }

        return null;
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

    public int getVoteMembersCount() {

        Poll poll = getPoll();

        if(poll.getVoteMembersCountOnClosed() != 0) {

            return getPoll().getVoteMembersCountOnClosed();
        }

        return getPoll().getMeeting().getPresentVoteMembers().size();
    }

    public int getVoiceCount() {

        return getPoll().getVoices().size();
    }

    public Collection<Voice> getVoices() {

       return getPoll().getVoices();
    }

    public ConsensusLevel getConsensusLevel(Long consensusLevelId) {

        for(ConsensusLevel consensusLevel : dataService.getConsensusLevels()) {

            if(consensusLevel.getConsensusLevelId().equals(consensusLevelId)) {

                return consensusLevel;
            }
        }

        return null;
    }

    public TreeMap<Long, Integer> getCountPerConsensusLevel() {

        TreeMap<Long, Integer> consensusLevelCount = new TreeMap<>((o1, o2) -> getConsensusLevel(o1).getNumber() - getConsensusLevel(o2).getNumber());

        for(ConsensusLevel consensusLevel : getPoll().getMeeting().getGroup().getGroupSettings().getConsensusLevels()) {

            consensusLevelCount.put(consensusLevel.getConsensusLevelId(), 0);
        }

        for (Voice voice : getVoices()) {

            if(consensusLevelCount.containsKey(voice.getConsensusLevel().getConsensusLevelId())) {

                consensusLevelCount.put(voice.getConsensusLevel().getConsensusLevelId(), consensusLevelCount.get(voice.getConsensusLevel().getConsensusLevelId()) + 1);
            }
        }

        return consensusLevelCount;

    }

    public boolean isLocalAuthorModerator() {

        Group group = getPoll().getMeeting().getGroup();
        Long authorId = Util.bytesToLong(connectionService.getLocalAuthor().getId().getBytes());
        Member member = group.getMember(authorId);

        if(member.getRoles(group).contains(Role.MODERATOR)) {

            return true;
        }

        return false;
    }

    public Member getMemberFromLocalAuthor() {

        Long authorId = Util.bytesToLong(connectionService.getLocalAuthor().getId().getBytes());

        for (Member member : getPoll().getMeeting().getMembers()) {

            if(member.getMemberId().equals(authorId)) {
                return member;

            }
        }

        return null;
    }

    public Voice getVoiceFromLocalAuthor() {

        for(Voice voice : getPoll().getVoices()) {

            if(voice.getMember().getMemberId().equals(getMemberFromLocalAuthor().getMemberId())) {

                return voice;
            }
        }

        return null;
    }

    public void closePoll() throws PollCantBeClosedException {

        try {

            Poll poll = getPoll();
            poll.setIsOpen(false);
            poll.setClosedByModerator(true);
            poll.setVoteMembersCountOnClosed(poll.getMeeting().getPresentVoteMembers().size());

            dataService.mergePoll(poll);

            Collection<ModelClass> data = new ArrayList<>();
            data.add(poll);
            synchronizationService.push(getPrivateGroup(), data);

        } catch(GroupNotFoundException exception){
            //TODO rollback
            throw new PollCantBeClosedException();
        }
    }

}
