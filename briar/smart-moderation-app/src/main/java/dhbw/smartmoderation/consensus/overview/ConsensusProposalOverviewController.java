package dhbw.smartmoderation.consensus.overview;

import org.briarproject.bramble.api.identity.LocalAuthor;
import org.briarproject.briar.api.privategroup.PrivateGroup;
import dhbw.smartmoderation.controller.SmartModerationController;
import dhbw.smartmoderation.data.model.Group;
import dhbw.smartmoderation.data.model.Meeting;
import dhbw.smartmoderation.data.model.Member;
import dhbw.smartmoderation.data.model.ModelClass;
import dhbw.smartmoderation.data.model.Poll;
import dhbw.smartmoderation.data.model.Role;
import dhbw.smartmoderation.data.model.Voice;
import dhbw.smartmoderation.exceptions.GroupNotFoundException;
import dhbw.smartmoderation.exceptions.MeetingNotFoundException;
import dhbw.smartmoderation.exceptions.PollCantBeCreatedException;
import dhbw.smartmoderation.exceptions.PollCantBeDeletedException;
import dhbw.smartmoderation.exceptions.PollCantBeOpenedException;
import dhbw.smartmoderation.util.Util;

import java.util.ArrayList;
import java.util.Collection;

public class ConsensusProposalOverviewController extends SmartModerationController {

    private Long meetingId;

    public ConsensusProposalOverviewController(Long meetingId) {
        this.meetingId = meetingId;
    }

    public void update() {

        try {

            this.synchronizationService.pull(getPrivateGroup());

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

    public PrivateGroup getPrivateGroup() throws GroupNotFoundException {

        Collection<PrivateGroup> privateGroups = connectionService.getGroups();

        for(PrivateGroup group : privateGroups) {

            if(getMeeting().getGroup().getGroupId().equals(Util.bytesToLong(group.getId().getBytes()))) {

                return group;
            }
        }

        throw new GroupNotFoundException();
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

            if(member.getMemberId().equals(Util.bytesToLong(connectionService.getLocalAuthor().getId().getBytes()))) {

                return member;
            }
        }

        return null;
    }

    public boolean isLocalAuthorModerator() {

        Group group = getGroup();

        LocalAuthor localAuthor = connectionService.getLocalAuthor();

        if(localAuthor != null) {

            Long authorId = Util.bytesToLong(localAuthor.getId().getBytes());
            Member member = group.getMember(authorId);

            if(member.getRoles(group).contains(Role.MODERATOR)) {

                return true;
            }

            return false;
        }

        return false;

    }

    public void deletePoll(Long pollId) throws PollCantBeDeletedException {

        try{

            Collection<ModelClass> data = new ArrayList<>();

            Poll poll = getMeeting().getPoll(pollId);
            poll.setIsDeleted(true);
            data.add(poll);
            synchronizationService.push(getPrivateGroup(), data);
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
            synchronizationService.push(getPrivateGroup(), data);

        } catch(GroupNotFoundException exception){
            //TODO rollback
            throw new PollCantBeOpenedException();
        }

    }
}
