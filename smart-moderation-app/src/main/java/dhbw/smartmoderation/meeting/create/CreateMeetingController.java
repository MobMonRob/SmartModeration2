package dhbw.smartmoderation.meeting.create;

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
import dhbw.smartmoderation.data.model.Topic;
import dhbw.smartmoderation.exceptions.CantSubMitMeetingException;
import dhbw.smartmoderation.exceptions.GroupNotFoundException;
import dhbw.smartmoderation.exceptions.MeetingNotFoundException;
import dhbw.smartmoderation.util.Util;

public class CreateMeetingController extends SmartModerationController {

    Long groupId;

    public CreateMeetingController(Long groupId) {
        this.groupId = groupId;
    }

    public Group getGroup() {
        for (Group group : dataService.getGroups())
            if (group.getGroupId().equals(groupId)) return group;
        return null;
    }

    public Meeting getMeeting(Long meetingId) {
        try {
            return dataService.getMeeting(meetingId);
        } catch (MeetingNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Collection<ModelClass> createMeetingWithOpenEnd(Long id, boolean online, String cause, long date, long begin, String location, Collection<Topic> topics) {
        Collection<ModelClass> data = new ArrayList<>();
        Meeting meeting = null;

        if (id != null) {
            try {
                meeting = dataService.getMeeting(id);
            } catch (MeetingNotFoundException e) {
                e.printStackTrace();
            }

            for (Topic topic : meeting.getTopics()) {
                topic.setIsDeleted(true);
                data.add(topic);
                dataService.deleteTopic(topic);
            }
        } else {
            meeting = new Meeting();
        }

        meeting.setOnline(online);
        meeting.setOpen(true);
        meeting.setCause(cause);
        meeting.setLocation(location);
        meeting.setDate(date);
        meeting.setStartTime(begin);
        meeting.setGroup(getGroup());

        dataService.mergeMeeting(meeting);

        for (Member member : getGroup().getUniqueMembers()) {
            MemberMeetingRelation memberMeetingRelation = meeting.addMember(member, Attendance.ABSENT);

            if (memberMeetingRelation != null)
                dataService.saveMemberMeetingRelation(memberMeetingRelation);
        }

        for (Topic topic : topics) {
            topic.setMeeting(meeting);
            data.add(topic);
            dataService.mergeTopic(topic);
        }

        data.add(meeting);

        return data;
    }

    public Collection<ModelClass> createMeetingWithPlannedEnd(Long id, boolean online, String cause, long date, long begin, String location, long end, Collection<Topic> topics) {

        Collection<ModelClass> data = new ArrayList<>();

        Meeting meeting = null;

        if (id != null) {
            try {
                meeting = dataService.getMeeting(id);
            } catch (MeetingNotFoundException e) {
                e.printStackTrace();
            }

            for (Topic topic : meeting.getTopics()) {
                boolean delete = true;
                for (Topic t : topics)
                    if (t.getTopicId().equals(topic.getTopicId())) delete = false;

                topic.setIsDeleted(delete);
                data.add(topic);
                dataService.deleteTopic(topic);
            }
        } else {
            meeting = new Meeting();
        }

        meeting.setOnline(online);
        meeting.setOpen(false);
        meeting.setCause(cause);
        meeting.setLocation(location);
        meeting.setDate(date);
        meeting.setStartTime(begin);
        meeting.setEndTime(end);
        meeting.setGroup(getGroup());

        dataService.mergeMeeting(meeting);

        for (Member member : getGroup().getUniqueMembers()) {
            MemberMeetingRelation memberMeetingRelation = meeting.addMember(member, Attendance.ABSENT);

            if (memberMeetingRelation != null)
                dataService.saveMemberMeetingRelation(memberMeetingRelation);
        }

        for (Topic topic : topics) {
            topic.setMeeting(meeting);
            data.add(topic);
            dataService.mergeTopic(topic);
        }

        data.add(meeting);
        return data;
    }

    public void submitMeeting(Long id, boolean isOpenEnd, boolean online, String cause, long date, long begin, String location, long end, Collection<Topic> topics) throws CantSubMitMeetingException {

        PrivateGroup group;

        try {
            group = getPrivateGroup(groupId);
        } catch (GroupNotFoundException exception) {
            throw new CantSubMitMeetingException();
        }

        Collection<ModelClass> data;

        if (isOpenEnd)
            data = createMeetingWithOpenEnd(id, online, cause, date, begin, location, topics);
        else
            data = createMeetingWithPlannedEnd(id, online, cause, date, begin, location, end, topics);

        synchronizationService.push(group, data);
    }
}
