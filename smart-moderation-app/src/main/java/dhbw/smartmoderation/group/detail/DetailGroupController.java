package dhbw.smartmoderation.group.detail;

import org.briarproject.bramble.api.contact.Contact;
import org.briarproject.briar.api.privategroup.PrivateGroup;

import java.util.ArrayList;
import java.util.Collection;

import dhbw.smartmoderation.controller.SmartModerationController;
import dhbw.smartmoderation.data.model.Ghost;
import dhbw.smartmoderation.data.model.Group;
import dhbw.smartmoderation.data.model.IContact;
import dhbw.smartmoderation.data.model.Meeting;
import dhbw.smartmoderation.data.model.Member;
import dhbw.smartmoderation.data.model.MemberGroupRelation;
import dhbw.smartmoderation.data.model.ModelClass;
import dhbw.smartmoderation.data.model.Role;
import dhbw.smartmoderation.exceptions.GroupNotFoundException;
import dhbw.smartmoderation.exceptions.NoContactsFoundException;
import dhbw.smartmoderation.util.Util;

public class DetailGroupController extends SmartModerationController {

    protected Long groupId;

    public DetailGroupController(Long groupId) {
        if (groupId == null) {
            throw new NullPointerException("GroupId can't be null or empty");
        }
        this.groupId = groupId;
    }

    public void update() {
        try {
            this.synchronizationService.pull(getPrivateGroup());
        } catch (GroupNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Long getGroupId() {
        return groupId;
    }

    public Collection<Member> getMembers() throws GroupNotFoundException {
        return dataService.getGroup(groupId).getUniqueMembers();
    }

    public Collection<Meeting> getMeetings() throws GroupNotFoundException {
        return dataService.getGroup(groupId).getMeetings();
    }

    public Group getGroup() throws GroupNotFoundException {
        return dataService.getGroup(groupId);
    }

    private PrivateGroup getPrivateGroup() throws GroupNotFoundException {
        for (PrivateGroup group : connectionService.getGroups()) {
            Long privateGroupId = Util.bytesToLong(group.getId().getBytes());

            if (privateGroupId.equals(this.groupId)) return group;
        }

        throw new GroupNotFoundException();
    }

    public boolean isLocalAuthorModerator() {

        Group group;

        try {
            group = getGroup();
            Long authorId = connectionService.getLocalAuthorId();
            Member member = group.getMember(authorId);
            if (member.getRoles(group).contains(Role.MODERATOR)) return true;

        } catch (GroupNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        return false;
    }

    public int getModeratorCount() {

        Group group = null;

        try {
            group = getGroup();
        } catch (GroupNotFoundException e) {
            e.printStackTrace();
        }

        int count = 0;

        assert group != null;
        for (Member member : group.getUniqueMembers())
            if (member.getRoles(group).contains(Role.MODERATOR)) count++;

        return count;
    }

    public Long getLocalAuthorId() {
        return connectionService.getLocalAuthorId();
    }

    public void leaveGroup() throws GroupNotFoundException {

        Group group = getGroup();
        Collection<Meeting> meetings = group.getMeetings();

        Member localAuthor = group.getMember(getLocalAuthorId());
        group.removeMember(localAuthor);

        Collection<ModelClass> data = new ArrayList<>();

        for (Meeting meeting : meetings) {
            meeting.removeMember(localAuthor);
            data.add(meeting);
        }

        data.add(group);

        synchronizationService.push(getPrivateGroup(), data);
        dataService.deleteGroup(group);
    }

    public void deleteGroup() throws GroupNotFoundException {

        Group group = getGroup();
        group.setIsDeleted(true);
        dataService.deleteGroup(group);

        Collection<ModelClass> data = new ArrayList<>();
        data.add(group);
        this.synchronizationService.push(getPrivateGroup(), data);
    }

    public void removeMember(Long memberId) throws GroupNotFoundException {

        Group group = getGroup();

        Member member = group.getMember(memberId);
        group.removeMember(member);

        Collection<ModelClass> data = new ArrayList<>();

        data.add(group);

        for (Meeting meeting : group.getMeetings()) {
            meeting.removeMember(member);
            data.add(meeting);
        }

        synchronizationService.push(getPrivateGroup(), data);
    }

    public void deleteMeeting(Long meetingId) throws GroupNotFoundException {

        Group group = getGroup();

        Meeting meeting = group.getMeeting(meetingId);
        meeting.setIsDeleted(true);

        Collection<ModelClass> data = new ArrayList<>();
        data.add(meeting);

        synchronizationService.push(getPrivateGroup(), data);
        dataService.deleteMeeting(meeting);
    }

    public void addGhost(String firstName, String lastName) throws GroupNotFoundException {

        Ghost ghost = new Ghost();
        ghost.setFirstName(firstName);
        ghost.setLastName(lastName);

        Member member = new Member(ghost);
        dataService.mergeMember(member);

        Group group = this.getGroup();

        MemberGroupRelation memberGroupRelation = group.addMember(member, Role.SPECTATOR);

        dataService.saveMemberGroupRelation(memberGroupRelation);

        Collection<ModelClass> data = new ArrayList<>();

        group = getGroup();
        data.add(group);
        data.add(member);

        synchronizationService.push(getPrivateGroup(), data);
    }

    public Collection<IContact> getContacts() throws NoContactsFoundException {

        Collection<Contact> oldContactList = connectionService.getContacts();
        ArrayList<IContact> newContactList = new ArrayList<>();

        for (Contact contact : oldContactList) {
            dhbw.smartmoderation.data.model.Contact newContact = new dhbw.smartmoderation.data.model.Contact(contact);
            newContactList.add(newContact);
        }

        return newContactList;
    }

    public Contact getContact(Long contactId) {

        Collection<Contact> contacts = new ArrayList<>();

        try {
            contacts = connectionService.getContacts();
        } catch (NoContactsFoundException e) {
            e.printStackTrace();
        }

        for (Contact contact : contacts) {
            if (contactId.equals(Util.bytesToLong(contact.getAuthor().getId().getBytes())))
                return contact;

        }
        return null;
    }

    public boolean isConnectedToContact(Contact contact) {
        if (contact == null)
            return false;

        return connectionService.isConnected(contact.getId());
    }

    public void addContacts(ArrayList<IContact> selectedContacts) throws GroupNotFoundException, NoContactsFoundException {

        Group group = getGroup();
        Collection<Member> oldMembers = group.getUniqueMembers();

        for (IContact contact : selectedContacts) {
            for (Member member : oldMembers)
                if (member.getMemberId().equals(contact.getId())) selectedContacts.remove(contact);
        }

        if (selectedContacts.size() > 0) {
            Collection<Contact> contacts = connectionService.getContacts();
            for (IContact contact : selectedContacts) {
                Contact thisBriarContact = null;
                for (Contact briarContact : contacts) {
                    if (contact.getId().equals(Util.bytesToLong(briarContact.getAuthor().getId().getBytes())))
                        thisBriarContact = briarContact;
                }
                connectionService.addContactToGroup(getPrivateGroup(), thisBriarContact);
            }
        }
    }
}
