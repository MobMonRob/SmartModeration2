package dhbw.smartmoderation.group.personInfo;

import org.briarproject.bramble.api.contact.Contact;
import org.briarproject.briar.api.privategroup.PrivateGroup;

import java.util.ArrayList;
import java.util.Collection;

import dhbw.smartmoderation.controller.SmartModerationController;
import dhbw.smartmoderation.data.model.Attendance;
import dhbw.smartmoderation.data.model.Group;
import dhbw.smartmoderation.data.model.Meeting;
import dhbw.smartmoderation.data.model.Member;
import dhbw.smartmoderation.data.model.MemberGroupRelation;
import dhbw.smartmoderation.data.model.MemberMeetingRelation;
import dhbw.smartmoderation.data.model.ModelClass;
import dhbw.smartmoderation.data.model.Poll;
import dhbw.smartmoderation.data.model.Role;
import dhbw.smartmoderation.exceptions.CantLinkContactToMember;
import dhbw.smartmoderation.exceptions.GroupNotFoundException;
import dhbw.smartmoderation.exceptions.MemberNotFoundException;
import dhbw.smartmoderation.exceptions.NoContactsFoundException;
import dhbw.smartmoderation.util.Util;

public class PersonInfoController extends SmartModerationController {

    private Long groupId;

    public PersonInfoController(Long groupId) {

        this.groupId = groupId;

    }

    public void addRole(Member member, Role role) {

        Group group = getGroup();

        MemberGroupRelation memberGroupRelation = group.addMember(member, role);

        if(memberGroupRelation != null) {
            dataService.saveMemberGroupRelation(memberGroupRelation);
        }


    }

    public void removeRole(Member member, Role role) {

        Group group = getGroup();

        group.removeMember(member, role);

    }

    public boolean isLocalAuthor(Long memberId) {

        if(memberId.equals(connectionService.getLocalAuthorId())) {

            return true;
        }

        return false;
    }

    public PrivateGroup getPrivateGroup() throws GroupNotFoundException {

        Collection<PrivateGroup> privateGroups = connectionService.getGroups();

        for(PrivateGroup group : privateGroups) {

            if(this.groupId.equals(Util.bytesToLong(group.getId().getBytes()))) {

                return group;
            }
        }

        throw new GroupNotFoundException();
    }

    public Group getGroup() {

        Collection<Group> groups = dataService.getGroups();

        for(Group group : groups) {

            if(group.getGroupId().equals(this.groupId)) {

                return group;
            }
        }

        return null;
    }

    public boolean isLocalAuthorModerator() {

        Group group = getGroup();
        Long authorId = connectionService.getLocalAuthorId();
        Member member = group.getMember(authorId);

        if(member.getRoles(group).contains(Role.MODERATOR)) {

            return true;
        }

        return false;
    }

    public int countParticipantsInGroup() {

        int count = 0;
        Group group = getGroup();
        Collection<Member> members = group.getUniqueMembers();

        for(Member member : members) {

            if(member.getRoles(group).contains(Role.PARTICIPANT)) {
                count++;
            }
        }

        return count;
    }

    public int countModeratorsInGroup() {

        int count = 0;
        Group group = getGroup();
        Collection<Member> members = group.getUniqueMembers();

        for(Member member : members) {

            if(member.getRoles(group).contains(Role.MODERATOR)) {
                count++;
            }
        }

        return count;
    }

    public boolean isPollOpen() {

        for(Poll poll : dataService.getPolls()) {

            if(poll.getIsOpen()) {

                return true;
            }
        }

        return false;
    }


    public void submitChanges() throws GroupNotFoundException {

        PrivateGroup privateGroup = getPrivateGroup();
        Group group = getGroup();
        Collection<ModelClass> pushData = new ArrayList<>();
        pushData.add(group);
        synchronizationService.push(privateGroup, pushData);
    }

    public Collection<Contact> getContacts() throws NoContactsFoundException {

        return this.connectionService.getContacts();
    }

    public Long linkContactToMember(Contact contact, Long memberId) throws CantLinkContactToMember {

        PrivateGroup privateGroup;

        try {

            privateGroup = getPrivateGroup();

        } catch(GroupNotFoundException exception){

            throw new CantLinkContactToMember();
        }

       connectionService.addContactToGroup(privateGroup, contact);

        Group group = getGroup();

        Member member = null;

        try {

            member = dataService.getMember(memberId);

        } catch (MemberNotFoundException e) {

            e.printStackTrace();
        }

        if(member.getIsGhost()) {

            group.removeMember(member);

            for(Meeting meeting : group.getMeetings()) {

                meeting.removeMember(member);
            }

            dataService.deleteMember(member);
            member.setMemberId(Util.bytesToLong(contact.getAuthor().getId().getBytes()));
            member.setIsGhost(false);
            dataService.saveMember(member);

            MemberGroupRelation memberGroupRelation = group.addMember(member, Role.PARTICIPANT);

            if(memberGroupRelation != null) {

                dataService.saveMemberGroupRelation(memberGroupRelation);
            }

            for(Meeting meeting : group.getMeetings()) {

                MemberMeetingRelation memberMeetingRelation = meeting.addMember(member, Attendance.ABSENT);

                if(memberMeetingRelation != null) {

                    dataService.saveMemberMeetingRelation(memberMeetingRelation);
                }
            }
        }

        Collection<ModelClass> pushData = new ArrayList<>();
        pushData.add(getGroup());
        pushData.addAll(getGroup().getMeetings());
        synchronizationService.push(privateGroup, pushData);


        /*    Collection<ModelClass> pushData = new ArrayList<>();
            pushData.add(member);
            synchronizationService.push(privateGroup, pushData);

            /*try {

                group = dataService.getGroup(group.getGroupId());

            } catch (GroupNotFoundException e) {
                e.printStackTrace();
            }

            pushData.add(group);
            pushData.add(group.getGroupSettings());
            pushData.addAll(group.getMembers());
            pushData.addAll(group.getMeetings());
            pushData.addAll(group.getGroupSettings().getConsensusLevels());

            synchronizationService.push(privateGroup, pushData);
        }*/

        return member.getMemberId();
    }

}
