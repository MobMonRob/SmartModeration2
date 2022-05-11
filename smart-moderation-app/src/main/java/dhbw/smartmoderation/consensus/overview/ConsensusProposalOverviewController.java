package dhbw.smartmoderation.consensus.overview;

import org.briarproject.briar.api.privategroup.PrivateGroup;

import java.util.ArrayList;
import java.util.Collection;

import dhbw.smartmoderation.controller.SmartModerationController;
import dhbw.smartmoderation.data.model.Group;
import dhbw.smartmoderation.data.model.Meeting;
import dhbw.smartmoderation.data.model.Member;
import dhbw.smartmoderation.data.model.ModelClass;
import dhbw.smartmoderation.data.model.Poll;
import dhbw.smartmoderation.data.model.Role;
import dhbw.smartmoderation.exceptions.GroupNotFoundException;
import dhbw.smartmoderation.exceptions.MeetingNotFoundException;
import dhbw.smartmoderation.exceptions.PollCantBeDeletedException;
import dhbw.smartmoderation.exceptions.PollCantBeOpenedException;
import dhbw.smartmoderation.util.Util;

public class ConsensusProposalOverviewController extends SmartModerationController {

    private Long meetingId;

    public ConsensusProposalOverviewController(Long meetingId) {
        this.meetingId = meetingId;
    }

    public void update() {
        try {
            this.synchronizationService.pull(getPrivateGroup(getMeeting().getGroupId()));
        } catch (GroupNotFoundException e) {
            e.printStackTrace();
        }
    }


    public Group getGroup() {
        Collection<Group> groups = dataService.getGroups();
        Long groupId = getMeeting().getGroup().getGroupId();

        for(Group group : groups) {
            if(group.getGroupId().equals(groupId)) {
                return group;
            }
        }
        return null;
    }

    public Meeting getMeeting() {
        try {
            return dataService.getMeeting(meetingId);
        } catch (MeetingNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Collection<Poll> getPolls() {
        return getMeeting().getPolls();
    }

    public Member getMember() {
        for(Member member : getMeeting().getMembers()) {
            if(member.getMemberId().equals(connectionService.getLocalAuthorId())) {
                return member;
            }
        }
        return null;
    }

    public boolean isLocalAuthorModerator() {
        Group group = getGroup();
        Long authorId = connectionService.getLocalAuthorId();
        Member member = group.getMember(authorId);

        if(member != null && member.getRoles(group).contains(Role.MODERATOR)) {
            return true;
        }
        return false;
    }

    public void deletePoll(Long pollId) throws PollCantBeDeletedException {

        try{
            Collection<ModelClass> data = new ArrayList<>();
            Poll poll = getMeeting().getPoll(pollId);
            poll.setIsDeleted(true);
            data.add(poll);
            synchronizationService.push(getPrivateGroup(getMeeting().getGroupId()), data);
            dataService.deletePoll(poll);
        } catch(GroupNotFoundException exception){
            throw new PollCantBeDeletedException();
        }
    }

    public void openPoll(Long pollId) throws PollCantBeOpenedException {
        try{
            Collection<ModelClass> data = new ArrayList<>();
            Poll poll = getMeeting().getPoll(pollId);
            poll.setIsOpen(true);
            data.add(poll);
            dataService.mergePoll(poll);
            synchronizationService.push(getPrivateGroup(getMeeting().getGroupId()), data);
        } catch(GroupNotFoundException exception){
            //TODO rollback
            throw new PollCantBeOpenedException();
        }
    }
}
