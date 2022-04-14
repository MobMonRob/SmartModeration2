package dhbw.smartmoderation.meeting.detail;

import org.briarproject.briar.api.privategroup.PrivateGroup;

import java.util.ArrayList;
import java.util.Collection;

import dhbw.smartmoderation.controller.SmartModerationController;
import dhbw.smartmoderation.data.model.Attendance;
import dhbw.smartmoderation.data.model.Group;
import dhbw.smartmoderation.data.model.Meeting;
import dhbw.smartmoderation.data.model.Member;
import dhbw.smartmoderation.data.model.MemberMeetingRelation;
import dhbw.smartmoderation.data.model.ModelClass;
import dhbw.smartmoderation.data.model.Poll;
import dhbw.smartmoderation.data.model.Role;
import dhbw.smartmoderation.data.model.Topic;
import dhbw.smartmoderation.data.model.TopicStatus;
import dhbw.smartmoderation.data.model.Voice;
import dhbw.smartmoderation.exceptions.GroupNotFoundException;
import dhbw.smartmoderation.exceptions.MeetingNotFoundException;
import dhbw.smartmoderation.exceptions.MemberCantBeChangedException;
import dhbw.smartmoderation.exceptions.MemberCantBeDeleteException;
import dhbw.smartmoderation.exceptions.TopicCantBeChangedException;
import dhbw.smartmoderation.exceptions.TopicCantBeDeletedException;
import dhbw.smartmoderation.util.Util;

public class MeetingDetailController extends SmartModerationController {

    private Long meetingId;

    public MeetingDetailController(Long meetingId) {
        this.meetingId = meetingId;
    }

    public void update() {

        try {

            this.synchronizationService.pull(getPrivateGroup());

        } catch (GroupNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Meeting getMeeting() {

       for(Meeting meeting : dataService.getMeetings()) {

            if(meeting.getMeetingId().equals(this.meetingId)) {
                return meeting;
            }
        }

        return null;
    }

    public Collection<Member> getMembers() {

        return getMeeting().getMembers();
    }

    public Collection<Topic> getTopics() {

        Meeting meeting = null;

        try {

            meeting = dataService.getMeeting(getMeeting().getMeetingId());

        } catch (MeetingNotFoundException e) {

            e.printStackTrace();
        }

        return meeting.getTopics();
    }

    public long getTotalTimeOfUpcomingTopics() {

        long totalTime = 0;

        for (Topic topic : getTopics()) {

            if(topic.getTopicStatus() == TopicStatus.UPCOMING) {
                totalTime += topic.getDuration();
            }
        }

        return totalTime;
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

            if(getGroup().getGroupId().equals(Util.bytesToLong(group.getId().getBytes()))) {

                return group;
            }
        }

        return null;
    }

    public Long getLocalAuthorId() {

        return connectionService.getLocalAuthorId();
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

    public void changeMemberStatus(Member member, Attendance attendance) throws MemberCantBeChangedException {

        PrivateGroup group;

        try {

            group = getPrivateGroup();

        } catch (GroupNotFoundException exception) {

            throw new MemberCantBeChangedException();
        }

        Meeting meeting = getMeeting();
        MemberMeetingRelation memberMeetingRelation = member.setAttendance(meeting, attendance);
        dataService.saveMemberMeetingRelation(memberMeetingRelation);
        meeting = getMeeting();
        Collection<ModelClass> data = new ArrayList<>();
        data.add(meeting);
        synchronizationService.push(group, data);
    }

    public boolean hasMemberAlreadyVoted(Member member) {

        for(Poll poll : getMeeting().getPolls()) {

            for(Voice voice : poll.getVoices()) {

                if(voice.getMemberId() == member.getMemberId()) {

                    return true;
                }
            }
        }

        return false;

    }

    public void deleteMember(Member member) throws MemberCantBeDeleteException {

        PrivateGroup group;

        try {

            group = getPrivateGroup();

        } catch (GroupNotFoundException exception) {

            throw new MemberCantBeDeleteException();
        }

        Meeting meeting = getMeeting();
        meeting.removeMember(member);

        Collection<ModelClass> data = new ArrayList<>();
        data.add(meeting);

        synchronizationService.push(group, data);
    }

    public void changeTopicStatus(Topic topic, TopicStatus status) throws TopicCantBeChangedException {

        PrivateGroup group;

        try {

            group = getPrivateGroup();

        } catch (GroupNotFoundException exception) {

            throw new TopicCantBeChangedException();
        }

        Collection<ModelClass> data = new ArrayList<>();

        if(status == TopicStatus.RUNNING) {

            for(Topic t : getMeeting().getTopics()) {

                if(t.getTopicStatus() == TopicStatus.RUNNING) {

                    t.setStatus(TopicStatus.FINISHED.name());
                    dataService.saveTopic(t);
                }
            }
        }

        topic.setStatus(status.name());
        dataService.saveTopic(topic);

        Meeting meeting = null;

        try {

            meeting = dataService.getMeeting(meetingId);

        } catch (MeetingNotFoundException e) {

            e.printStackTrace();
        }

        for(Topic t : meeting.getTopics()) {

            data.add(t);
        }

        synchronizationService.push(group, data);
    }

    public void deleteTopic(Topic topic) throws TopicCantBeDeletedException {

        PrivateGroup group;

        try {

            group = getPrivateGroup();

        } catch (GroupNotFoundException exception) {

            throw new TopicCantBeDeletedException();
        }

        Collection<ModelClass> data = new ArrayList<>();
        topic.setIsDeleted(true);
        data.add(topic);
        synchronizationService.push(group, data);
        dataService.deleteTopic(topic);
    }
}
