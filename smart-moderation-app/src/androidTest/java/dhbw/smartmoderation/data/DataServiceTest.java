/*package dhbw.smartmoderation.data;

import android.content.Context;

import org.briarproject.bramble.api.UniqueId;
import org.briarproject.bramble.api.crypto.CryptoConstants;
import org.briarproject.bramble.api.crypto.PublicKey;
import org.briarproject.bramble.api.identity.Author;
import org.briarproject.bramble.api.identity.AuthorId;
import org.briarproject.bramble.api.identity.AuthorInfo;
import org.briarproject.bramble.api.sync.GroupId;
import org.briarproject.briar.api.privategroup.GroupMember;
import org.briarproject.briar.api.privategroup.PrivateGroup;
import org.briarproject.briar.api.privategroup.Visibility;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collection;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import dhbw.smartmoderation.data.entities.GroupEntity;
import dhbw.smartmoderation.data.entities.MeetingEntity;
import dhbw.smartmoderation.data.entities.MemberEntity;
import dhbw.smartmoderation.data.entities.MemberGroupRelation;
import dhbw.smartmoderation.data.model.Group;
import dhbw.smartmoderation.data.model.Meeting;
import dhbw.smartmoderation.data.model.Member;
import dhbw.smartmoderation.data.model.Role;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


/**
 * ATTENTION: Sideeffectful!
 * <p>
 * Execution deletes all existing records! Only execute in test environment!
 *//*
@RunWith(AndroidJUnit4.class)
public class DataServiceTest {

    private static Context appContext;

    private static DataService dataService;

    private static void wipeData() {
        MemberEntity.deleteAll(MemberEntity.class);
        NoteEntity.deleteAll(NoteEntity.class);
        MeetingEntity.deleteAll(MeetingEntity.class);
        GroupEntity.deleteAll(GroupEntity.class);
        MemberGroupRelation.deleteAll(MemberGroupRelation.class);
    }

    @BeforeClass
    public static void setUpClass() {
        dataService = new DataServiceImpl();
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    @AfterClass
    public static void tearDownClass() {
        wipeData();
    }

    @Before
    public void setUp() {
        wipeData();
        assert !MemberEntity.findAll(MemberEntity.class).hasNext();
        assert !NoteEntity.findAll(NoteEntity.class).hasNext();
        assert !MeetingEntity.findAll(NoteEntity.class).hasNext();
        assert !GroupEntity.findAll(NoteEntity.class).hasNext();
    }

    @Test
    public void getNotes() {

        // Arrange

        PublicKey[] mockPublicKeys = generateMockPublicKeys(2);

        Member member1 = new Member(new Author(new AuthorId(generateMockIdBytes(0)), 0, "a", mockPublicKeys[0]));

        Group group = new Group(generateMockPrivateGroups(1)[0]);

        member1.addRole(Role.ADMINISTRATOR, group);
        member1.addRole(Role.MODERATOR, group);
        member1.addRole(Role.TIMEKEEPER, group);

        Member member2 = new Member(new Author(new AuthorId(generateMockIdBytes(1)), 0, "a", mockPublicKeys[1]));
        member2.addRole(Role.OBSERVER, group);

        Note note1 = new Note(member1);
        note1.setText("Test Note 1");

        Note note2 = new Note(member1);
        note2.setText("Test Note 2");

        Note note3 = new Note(member2);
        note3.setText("ABC");

        // Act

//		dataService.saveMember(member1);
        dataService.saveMember(member2);

        dataService.saveNote(note1);
        dataService.saveNote(note2);
        dataService.saveNote(note3);

        // Assert

        Collection<Member> actualMembers = dataService.getMembers();
        Member actualMember1 = null;
        Member actualMember2 = null;
        for (Member member : actualMembers) {
            if (member.getId().equals(member1.getId())) {
                actualMember1 = member;
            } else if (member.getId().equals(member2.getId())) {
                actualMember2 = member;
            } else {
                fail();
            }
        }
        if (actualMember1 == null) fail();
        if (actualMember2 == null) fail();

        assertEquals(2, actualMembers.size());

        Collection<Note> actualNotes = dataService.getNotes();
        Note actualNote1 = null;
        Note actualNote2 = null;
        Note actualNote3 = null;
        for (Note note : actualNotes) {
            if (note.getText().equals(note1.getText())) {
                actualNote1 = note;
            } else if (note.getText().equals(note2.getText())) {
                actualNote2 = note;
            } else if (note.getText().equals(note3.getText())) {
                actualNote3 = note;
            } else {
                fail();
            }
        }
        if (actualNote1 == null) fail();
        if (actualNote2 == null) fail();
        if (actualNote3 == null) fail();

        assertEquals(3, actualNotes.size());
        assertEquals(note1.getCreatedAt(), actualNote1.getCreatedAt());
        assertEquals(note1.getCreatedBy().getId(), actualNote1.getCreatedBy().getId());
        assertArrayEquals(note1.getCreatedBy().getRoles(group).toArray(new Role[note1.getCreatedBy().getRoles(group).size()]),
                actualNote1.getCreatedBy().getRoles(group).toArray(new Role[actualNote1.getCreatedBy().getRoles(group).size()]));
        assertEquals(note1.getText(), actualNote1.getText());
        assertEquals(note2.getCreatedAt(), actualNote2.getCreatedAt());
        assertEquals(note2.getCreatedBy().getId(), actualNote2.getCreatedBy().getId());
        assertArrayEquals(note2.getCreatedBy().getRoles(group).toArray(new Role[note2.getCreatedBy().getRoles(group).size()]),
                actualNote2.getCreatedBy().getRoles(group).toArray(new Role[actualNote2.getCreatedBy().getRoles(group).size()]));
        assertEquals(note2.getText(), actualNote2.getText());
        assertEquals(note3.getCreatedAt(), actualNote3.getCreatedAt());
        assertEquals(note3.getCreatedBy().getId(), actualNote3.getCreatedBy().getId());
        assertArrayEquals(note3.getCreatedBy().getRoles(group).toArray(new Role[note3.getCreatedBy().getRoles(group).size()]),
                actualNote3.getCreatedBy().getRoles(group).toArray(new Role[actualNote3.getCreatedBy().getRoles(group).size()]));
        assertEquals(note3.getText(), actualNote3.getText());

    }

    @Test
    public void getMembers() {

        // Arrange

        PublicKey[] mockPublicKeys = generateMockPublicKeys(2);

        Member member1 = new Member(new Author(new AuthorId(generateMockIdBytes(0)), 0, "a", mockPublicKeys[0]));

        Group group = new Group(generateMockPrivateGroups(1)[0]);

        member1.addRole(Role.ADMINISTRATOR, group);
        member1.addRole(Role.MODERATOR, group);
        member1.addRole(Role.TIMEKEEPER, group);

        Member member2 = new Member(new Author(new AuthorId(generateMockIdBytes(1)), 0, "a", mockPublicKeys[1]));
        member2.addRole(Role.OBSERVER, group);

        // Act

        dataService.saveMember(member1);
        dataService.saveMember(member2);

        // Assert

        Collection<Member> actualMembers = dataService.getMembers();
        Member actualMember1 = null;
        Member actualMember2 = null;
        for (Member member : actualMembers) {
            if (member.getId().equals(member1.getId())) {
                actualMember1 = member;
            } else if (member.getId().equals(member2.getId())) {
                actualMember2 = member;
            } else {
                fail();
            }
        }
        if (actualMember1 == null) fail();
        if (actualMember2 == null) fail();

        assertEquals(2, actualMembers.size());
        assertEquals(member1.getId(), actualMember1.getId());
        assertArrayEquals(member1.getRoles(group).toArray(new Role[member1.getRoles(group).size()]),
                actualMember1.getRoles(group).toArray(new Role[actualMember1.getRoles(group).size()]));
        assertEquals(member2.getId(), actualMember2.getId());
        assertArrayEquals(member2.getRoles(group).toArray(new Role[member2.getRoles(group).size()]),
                actualMember2.getRoles(group).toArray(new Role[actualMember2.getRoles(group).size()]));

    }

    @Test
    public void getMeetings() {

        // Arrange

        PublicKey[] mockPublicKeys = generateMockPublicKeys(2);

        Member member1 = new Member(new Author(new AuthorId(generateMockIdBytes(0)), 0, "a", mockPublicKeys[0]));

        Group group = new Group(generateMockPrivateGroups(1)[0]);

        member1.addRole(Role.ADMINISTRATOR, group);
        member1.addRole(Role.MODERATOR, group);
        member1.addRole(Role.TIMEKEEPER, group);

        Member member2 = new Member(new Author(new AuthorId(generateMockIdBytes(1)), 0, "a", mockPublicKeys[1]));
        member2.addRole(Role.OBSERVER, group);

        Note note1 = new Note(member1);
        note1.setText("Test Note 1");

        Note note2 = new Note(member1);
        note2.setText("Test Note 2");

        Note note3 = new Note(member2);
        note3.setText("ABC");

        Meeting meeting1 = new Meeting();
        meeting1.setStartTime(0);
        meeting1.setEndTime(0);
        meeting1.addNote(note1);
        meeting1.addNote(note2);

        Meeting meeting2 = new Meeting();
        meeting2.setStartTime(1);
        meeting2.setEndTime(1);
        meeting2.addNote(note3);

        // Act

        dataService.saveMeeting(meeting1);
        dataService.saveMeeting(meeting2);

        // Assert

        Collection<Meeting> actualMeetings = dataService.getMeetings();
        Meeting actualMeeting1 = null;
        Meeting actualMeeting2 = null;
        for (Meeting meeting : actualMeetings) {
            if (meeting.getStartTime() == meeting1.getStartTime()) {
                actualMeeting1 = meeting;
            } else if (meeting.getStartTime() == meeting2.getStartTime()) {
                actualMeeting2 = meeting;
            } else {
                fail();
            }
        }
        if (actualMeeting1 == null) fail();
        if (actualMeeting2 == null) fail();

        Collection<Note> actualMeeting1Notes = actualMeeting1.getNotes();
        Note actualNote1 = null;
        for (Note note : actualMeeting1Notes) {
            if (note.getText().equals(note1.getText())) {
                actualNote1 = note;
            } else if (note.getText().equals(note2.getText())) {
            } else {
                fail();
            }
        }
        if (actualNote1 == null) fail();
        assertEquals(2, actualMeeting1Notes.size());

        Collection<Note> actualMeeting2Notes = actualMeeting2.getNotes();
        Note actualNote3 = null;
        for (Note note : actualMeeting2Notes) {
            if (note.getText().equals(note3.getText())) {
                actualNote3 = note;
            } else {
                fail();
            }
        }
        if (actualNote3 == null) fail();
        assertEquals(1, actualMeeting2Notes.size());

        assertEquals(2, actualMeetings.size());
        assertEquals(meeting1.getStartTime(), actualMeeting1.getStartTime());
        assertEquals(meeting1.getEndTime(), actualMeeting1.getEndTime());
        assertEquals(note1.getCreatedAt(), actualNote1.getCreatedAt());
        assertEquals(note1.getCreatedBy().getId(), actualNote1.getCreatedBy().getId());
        assertEquals(meeting2.getStartTime(), actualMeeting2.getStartTime());
        assertEquals(meeting2.getEndTime(), actualMeeting2.getEndTime());
        assertEquals(note3.getCreatedAt(), actualNote3.getCreatedAt());
        assertEquals(note3.getCreatedBy().getId(), actualNote3.getCreatedBy().getId());

    }

    private byte[] generateMockIdBytes(int n) {
        byte[] b = new byte[UniqueId.LENGTH];
        for (int i = 0; i < UniqueId.LENGTH; i++) {
            b[i] = (byte) n;
        }
        return b;
    }

    @Test
    public void getGroups() {

        // Arrange

        PublicKey[] mockPublicKeys = generateMockPublicKeys(1);

        Group group = new Group(generateMockPrivateGroups(1)[0]);

        Member member = new Member(new Author(new AuthorId(generateMockIdBytes(0)), 0, "a", mockPublicKeys[0]));
        member.addRole(Role.ADMINISTRATOR, group);
        member.addRole(Role.MOUTH, group);

        Note note = new Note(member);
        note.setText("Test Group Note");

        Meeting meeting1 = new Meeting();
        meeting1.setStartTime(1);
        meeting1.setEndTime(1);
        meeting1.addNote(note);

        Meeting meeting2 = new Meeting();
        meeting2.setStartTime(2);
        meeting2.setEndTime(2);
        group.addMeeting(meeting1);
        group.addMeeting(meeting2);

        // Act

        dataService.saveGroup(group);

        // Assert

        Collection<Meeting> actualMeetings = dataService.getMeetings();
        assertEquals(2, actualMeetings.size());

        Collection<Note> actualNotes = dataService.getNotes();
        assertEquals(1, actualNotes.size());
        assertEquals("Test Group Note", actualNotes.iterator().next().getText());

        Collection<Group> actualGroups = dataService.getGroups();
        assertEquals(1, actualGroups.size());
        assertEquals("GroupId(0000000000000000000000000000000000000000000000000000000000000000)", actualGroups.iterator().next().getId());

    }

    private static PublicKey[] generateMockPublicKeys(int count) {
        PublicKey[] publicKeys = new PublicKey[count];
        for (int i = 0; i < count; i++) {
            final byte b = (byte) i;
            publicKeys[i] = new PublicKey() {
                @Override
                public String getKeyType() {
                    return CryptoConstants.KEY_TYPE_SIGNATURE;
                }

                @Override
                public byte[] getEncoded() {
                    return new byte[] {b};
                }
            };
        }
        return publicKeys;
    }

    private static PrivateGroup[] generateMockPrivateGroups(int count) {
        PrivateGroup[] privateGroups = new PrivateGroup[count];
        for (int i = 0; i < count; i++) {
            byte[] groupIdBytes = new byte[UniqueId.LENGTH];
            for (int j = 0; j < UniqueId.LENGTH; j++) {
                groupIdBytes[j] = (byte) i;
            }
            privateGroups[i] =
                    new PrivateGroup(new org.briarproject.bramble.api.sync.Group(new GroupId(groupIdBytes), null, 0, new byte[] {0}), "" + i, null,
                            null);
        }
        return privateGroups;
    }

    @Test
    public void memberToGroup() {

        // Arrange

        PublicKey[] mockPublicKeys = generateMockPublicKeys(5);
        PrivateGroup[] mockGroups = generateMockPrivateGroups(3);

        Member member1 = new Member(new Author(new AuthorId(generateMockIdBytes(0)), 0, "a", mockPublicKeys[0]));
        Member member2 = new Member(new Author(new AuthorId(generateMockIdBytes(1)), 0, "a", mockPublicKeys[1]));
        Member member3 = new Member(new Author(new AuthorId(generateMockIdBytes(2)), 0, "a", mockPublicKeys[2]));
        Member member4 = new Member(new Author(new AuthorId(generateMockIdBytes(3)), 0, "a", mockPublicKeys[3]));
        Member member5 = new Member(new Author(new AuthorId(generateMockIdBytes(4)), 0, "a", mockPublicKeys[4]));

        Group group1 = new Group(mockGroups[0]);
        Group group2 = new Group(mockGroups[1]);
        Group group3 = new Group(mockGroups[2]);

        // Act #1

        group1.addMember(member1);
        group1.addMember(member2);

        group2.addMember(member3);

        group3.addMember(member4);
        group3.addMember(member5);

        dataService.saveGroup(group1);
        dataService.saveGroup(group2);
        dataService.saveGroup(group3);

        // Assert #1

        Collection<Member> actualMembers = dataService.getMembers();
        assertEquals(5, actualMembers.size());

        Collection<Group> actualGroups = dataService.getGroups();
        assertEquals(3, actualGroups.size());

        Collection<Member> actualGroup1Members = group1.getMembers();
        assertEquals(2, actualGroup1Members.size());
        Member actualMember1 = null;
        Member actualMember2 = null;
        for (Member member : actualGroup1Members) {
            if (member.getId().equals(member1.getId())) {
                actualMember1 = member;
            } else if (member.getId().equals(member2.getId())) {
                actualMember2 = member;
            } else {
                fail();
            }
        }
        if (actualMember1 == null) fail();
        if (actualMember2 == null) fail();

        Collection<Member> actualGroup2Members = group2.getMembers();
        assertEquals(1, actualGroup2Members.size());
        Member actualMember3 = null;
        for (Member member : actualGroup2Members) {
            if (member.getId().equals(member3.getId())) {
                actualMember3 = member;
            } else {
                fail();
            }
        }
        if (actualMember3 == null) fail();

        Collection<Member> actualGroup3Members = group3.getMembers();
        assertEquals(2, actualGroup3Members.size());
        Member actualMember4 = null;
        Member actualMember5 = null;
        for (Member member : actualGroup3Members) {
            if (member.getId().equals(member4.getId())) {
                actualMember4 = member;
            } else if (member.getId().equals(member5.getId())) {
                actualMember5 = member;
            } else {
                fail();
            }
        }
        if (actualMember4 == null) fail();
        if (actualMember5 == null) fail();

        // Act #2

        group1.addMember(member4);
        group2.addMember(member4);
        group3.removeMember(member5);

        dataService.saveGroup(group1);
        dataService.saveGroup(group2);
        dataService.saveGroup(group3);

        // Assert #2

        actualGroup1Members = group1.getMembers();
        assertEquals(3, actualGroup1Members.size());
        Member actualGroup1Member1 = null;
        Member actualGroup1Member2 = null;
        Member actualGroup1Member4 = null;
        for (Member member : actualGroup1Members) {
            if (member.getId().equals(member1.getId())) {
                actualGroup1Member1 = member;
            } else if (member.getId().equals(member2.getId())) {
                actualGroup1Member2 = member;
            } else if (member.getId().equals(member4.getId())) {
                actualGroup1Member4 = member;
            } else {
                fail();
            }
        }
        if (actualGroup1Member1 == null) fail();
        if (actualGroup1Member2 == null) fail();
        if (actualGroup1Member4 == null) fail();

        actualGroup2Members = group2.getMembers();
        assertEquals(2, actualGroup2Members.size());
        Member actualGroup2Member3 = null;
        Member actualGroup2Member4 = null;
        for (Member member : actualGroup2Members) {
            if (member.getId().equals(member3.getId())) {
                actualGroup2Member3 = member;
            } else if (member.getId().equals(member4.getId())) {
                actualGroup2Member4 = member;
            } else {
                fail();
            }
        }
        if (actualGroup2Member3 == null) fail();
        if (actualGroup2Member4 == null) fail();

        actualGroup3Members = group3.getMembers();
        assertEquals(1, actualGroup3Members.size());
        Member actualGroup3Member4 = null;
        for (Member member : actualGroup3Members) {
            if (member.getId().equals(member4.getId())) {
                actualGroup3Member4 = member;
            } else {
                fail();
            }
        }
        if (actualGroup3Member4 == null) fail();

        // Act #3

        group1.addMember(member5);
        group2.addMember(member5);

        dataService.saveGroup(group1);
        dataService.saveGroup(group2);
        dataService.saveGroup(group3);

        // Assert #3

        actualGroup1Members = group1.getMembers();
        assertEquals(4, actualGroup1Members.size());
        Member actualGroup1Member1a = null;
        Member actualGroup1Member2a = null;
        Member actualGroup1Member4a = null;
        Member actualGroup1Member5 = null;
        for (Member member : actualGroup1Members) {
            if (member.getId().equals(member1.getId())) {
                actualGroup1Member1a = member;
            } else if (member.getId().equals(member2.getId())) {
                actualGroup1Member2a = member;
            } else if (member.getId().equals(member4.getId())) {
                actualGroup1Member4a = member;
            } else if (member.getId().equals(member5.getId())) {
                actualGroup1Member5 = member;
            } else {
                fail();
            }
        }
        if (actualGroup1Member1a == null) fail();
        if (actualGroup1Member2a == null) fail();
        if (actualGroup1Member4a == null) fail();
        if (actualGroup1Member5 == null) fail();

        actualGroup2Members = group2.getMembers();
        assertEquals(3, actualGroup2Members.size());
        Member actualGroup2Member3a = null;
        Member actualGroup2Member4a = null;
        Member actualGroup2Member5 = null;
        for (Member member : actualGroup2Members) {
            if (member.getId().equals(member3.getId())) {
                actualGroup2Member3a = member;
            } else if (member.getId().equals(member4.getId())) {
                actualGroup2Member4a = member;
            } else if (member.getId().equals(member5.getId())) {
                actualGroup2Member5 = member;
            } else {
                fail();
            }
        }
        if (actualGroup2Member3a == null) fail();
        if (actualGroup2Member4a == null) fail();
        if (actualGroup2Member5 == null) fail();

        actualGroup3Members = group3.getMembers();
        assertEquals(1, actualGroup3Members.size());
        Member actualGroup3Member4a = null;
        for (Member member : actualGroup3Members) {
            if (member.getId().equals(member4.getId())) {
                actualGroup3Member4a = member;
            } else {
                fail();
            }
        }
        if (actualGroup3Member4a == null) fail();

    }

    @Test
    public void getGroup() {
        PrivateGroup mockPrivateGroup = generateMockPrivateGroups(1)[0];
        Group group = new Group(mockPrivateGroup);
        dataService.saveGroup(group);

        Group actualGroup = dataService.getGroup(mockPrivateGroup);

        assertEquals(group.getId(), actualGroup.getId());
    }

    @Test
    public void getMember() {
        GroupMember mockGroupMember =
                new GroupMember(new Author(new AuthorId(generateMockIdBytes(1)), 0, "a", generateMockPublicKeys(1)[0]),
                        new AuthorInfo(AuthorInfo.Status.OURSELVES), true, null, Visibility.VISIBLE);
        Member member = new Member(mockGroupMember.getAuthor());
        dataService.saveMember(member);

        Member actualMember = dataService.getMember(mockGroupMember);

        assertEquals(member.getId(), actualMember.getId());
    }

    @Test
    public void mergeMember() {
        Group group1 = new Group("g1");
        Group group2 = new Group("g2");
        Group group3 = new Group("g3");

        Member member1 = new Member("m1");
        member1.addRole(Role.ADMINISTRATOR, group1);
        member1.addRole(Role.VISUALIZER, group2);

        dataService.saveGroup(group1);
        dataService.saveGroup(group2);
        dataService.saveGroup(group3);
        dataService.saveMember(member1);

        Member member2 = new Member("m1");
        member2.addRole(Role.TIMEKEEPER, group1);
        member2.addRole(Role.MOUTH, group3);

        dataService.mergeMember(member2);

        Collection<Member> actualMembers = dataService.getMembers();
        assertEquals(1, actualMembers.size());

        Member actualMember = actualMembers.iterator().next();

        assertEquals(2, actualMember.getRoles(group1).size());
        boolean wasAdmin = false;
        boolean wasTimekeeper = false;
        for (Role role : actualMember.getRoles(group1)) {
            if (role == Role.ADMINISTRATOR) {
                if (!wasAdmin) {
                    wasAdmin = true;
                } else fail();
            } else if (role == Role.TIMEKEEPER) {
                if (!wasTimekeeper) {
                    wasTimekeeper = true;
                } else fail();
            } else fail();
        }

        assertEquals(1, actualMember.getRoles(group2).size());
        assertEquals(Role.VISUALIZER, actualMember.getRoles(group2).iterator().next());

        assertEquals(1, actualMember.getRoles(group3).size());
        assertEquals(Role.MOUTH, actualMember.getRoles(group3).iterator().next());
    }

    @Test
    public void mergeNote() {
        Member member = new Member("m1");
        Note note1 = new Note(member, 123);
        note1.setId("n1");
        note1.setText("abc");

        dataService.saveMember(member);
        dataService.saveNote(note1);

        Note note2 = new Note(member, 456);
        note2.setId("n1");
//		note2.setText("def");

        dataService.mergeNote(note2);

        Collection<Note> actualNotes = dataService.getNotes();
        assertEquals(1, actualNotes.size());

        Note actualNote = actualNotes.iterator().next();
        assertEquals(note1.getId(), actualNote.getId());
        assertEquals(note1.getCreatedBy().getId(), actualNote.getCreatedBy().getId());
        assertEquals(note2.getCreatedAt(), actualNote.getCreatedAt());
        assertEquals(note1.getText(), actualNote.getText());
    }

    @Test
    public void mergeMeeting() {
        Member member = new Member("m1");
        Note note1 = new Note(member);
        note1.setId("n1");
        Meeting meeting1 = new Meeting();
        meeting1.setId("me1");
        meeting1.setStartTime(123);
        meeting1.setEndTime(456);
        meeting1.addNote(note1);

        dataService.saveMember(member);
        dataService.saveNote(note1);
        dataService.saveMeeting(meeting1);

        Note note2 = new Note(member);
        note2.setId("n2");
        Meeting meeting2 = new Meeting();
        meeting2.setId("me1");
        meeting2.addNote(note2);

        dataService.mergeMeeting(meeting2);

        Collection<Meeting> actualMeetings = dataService.getMeetings();
        assertEquals(1, actualMeetings.size());

        Meeting actualMeeting = actualMeetings.iterator().next();
        assertEquals(meeting1.getId(), actualMeeting.getId());
        assertEquals(meeting1.getStartTime(), actualMeeting.getStartTime());
        assertEquals(meeting1.getEndTime(), actualMeeting.getEndTime());
        assertEquals(2, actualMeeting.getNotes().size());

        Collection<Note> actualNotes = actualMeeting.getNotes();
        Note actualNote1 = null;
        Note actualNote2 = null;
        for (Note note : actualNotes) {
            if (note.getId().equals(note1.getId())) {
                actualNote1 = note;
            } else if (note.getId().equals(note2.getId())) {
                actualNote2 = note;
            } else fail();
        }
        if (actualNote1 == null) fail();
        if (actualNote2 == null) fail();
    }

    @Test
    public void mergeGroup() {
        Member member1 = new Member("m1");
        Meeting meeting1 = new Meeting();
        Group group1 = new Group("g1");
        group1.addMember(member1);
        group1.addMeeting(meeting1);

        dataService.saveMember(member1);
        dataService.saveMeeting(meeting1);
        dataService.saveGroup(group1);

        Member member2 = new Member("m2");
        Meeting meeting2 = new Meeting();
        Group group2 = new Group("g1");
        group2.addMember(member2);
        group2.addMeeting(meeting2);

        dataService.mergeGroup(group2);

        Collection<Group> actualGroups = dataService.getGroups();
        assertEquals(1, actualGroups.size());

        Group actualGroup = actualGroups.iterator().next();
        assertEquals(group1.getId(), actualGroup.getId());
        assertEquals(2, actualGroup.getMembers().size());

        Collection<Member> actualMembers = actualGroup.getMembers();
        Member actualMember1 = null;
        Member actualMember2 = null;
        for (Member member : actualMembers) {
            if (member.getId().equals(member1.getId())) {
                actualMember1 = member;
            } else if (member.getId().equals(member2.getId())) {
                actualMember2 = member;
            } else fail();
        }
        if (actualMember1 == null) fail();
        if (actualMember2 == null) fail();

        assertEquals(2, actualGroup.getMeetings().size());

        Collection<Meeting> actualMeetings = actualGroup.getMeetings();
        Meeting actualMeeting1 = null;
        Meeting actualMeeting2 = null;
        for (Meeting meeting : actualMeetings) {
            if (meeting.getId().equals(meeting1.getId())) {
                actualMeeting1 = meeting;
            } else if (meeting.getId().equals(meeting2.getId())) {
                actualMeeting2 = meeting;
            } else fail();
        }
        if (actualMeeting1 == null) fail();
        if (actualMeeting2 == null) fail();
    }

} */