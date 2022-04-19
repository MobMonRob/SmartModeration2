/*package dhbw.smartmoderation.connection.synchronization;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import dhbw.smartmoderation.data.entities.GroupEntity;
import dhbw.smartmoderation.data.entities.MeetingEntity;
import dhbw.smartmoderation.data.entities.MemberEntity;
import dhbw.smartmoderation.data.entities.MemberGroupRelation;
import dhbw.smartmoderation.data.model.Group;
import dhbw.smartmoderation.data.model.Meeting;
import dhbw.smartmoderation.data.model.Member;
import dhbw.smartmoderation.data.model.ModelClass;
import dhbw.smartmoderation.data.model.Role;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * ATTENTION: Sideeffectful!
 * <p>
 * Execution deletes all existing records! Only execute in test environment!
 *//*
public class SerializationServiceTest {

    private static SerializationService serializationService;

    @BeforeClass
    public static void setUpClass() {
        serializationService = new SerializationServiceImpl();
    }

    @Before
    public void setUp() {
        wipeData();
    }

    @AfterClass
    public static void tearDownClass() {
        wipeData();
    }

    private static void wipeData() {
        MemberEntity.deleteAll(MemberEntity.class);
        NoteEntity.deleteAll(NoteEntity.class);
        MeetingEntity.deleteAll(MeetingEntity.class);
        GroupEntity.deleteAll(GroupEntity.class);
        MemberGroupRelation.deleteAll(MemberGroupRelation.class);
    }

    @Test
    public void serializeMember() {
        Member member = new Member("mock");
        assertEquals("{\"type\":\"member\",\"authorId\":\"mock\"}", serializationService.serialize(member));
    }

    @Test
    public void serializeNote() {
        Member member = new Member("mock");
        Note note = new Note(member, 123);
        note.setId("456");
        note.setText("ABC");
        assertEquals("{\"type\":\"note\",\"noteId\":\"456\",\"createdBy\":\"mock\",\"createdAt\":123,\"text\":\"ABC\"}",
                serializationService.serialize(note));
    }

    @Test
    public void serializeMeeting() {
        Member member = new Member("mock");

        Note note1 = new Note(member, 123);
        note1.setId("456");
        note1.setText("ABC");

        Note note2 = new Note(member, 789);
        note2.setId("101");
        note2.setText("GHI");

        Meeting meeting = new Meeting();
        meeting.setId("112");
        meeting.setStartTime(131);
        meeting.setEndTime(141);
        meeting.addNote(note1);
        meeting.addNote(note2);

        assertEquals("{\"type\":\"meeting\",\"meetingId\":\"112\",\"startTime\":131,\"endTime\":141,\"notes\":[\"456\",\"101\"]}",
                serializationService.serialize(meeting));
    }

    @Test
    public void serializeGroup() {
        Group group = new Group("11");

        Member member1 = new Member("1");
        Member member2 = new Member("2");
        Member member3 = new Member("3");
        Member member4 = new Member("4");

        group.addMember(member1);
        group.addMember(member2);
        group.addMember(member3);
        group.addMember(member4);

        member1.addRole(Role.ADMINISTRATOR, group);
        member2.addRole(Role.ADMINISTRATOR, group);
        member2.addRole(Role.MODERATOR, group);
        member3.addRole(Role.MOUTH, group);
        member4.addRole(Role.OBSERVER, group);
        member4.addRole(Role.TIMEKEEPER, group);
        member4.addRole(Role.VISUALIZER, group);

        Meeting meeting1 = new Meeting();
        meeting1.setId("5");
        Meeting meeting2 = new Meeting();
        meeting2.setId("6");
        Meeting meeting3 = new Meeting();
        meeting3.setId("7");
        Meeting meeting4 = new Meeting();
        meeting4.setId("8");
        Meeting meeting5 = new Meeting();
        meeting5.setId("9");
        Meeting meeting6 = new Meeting();
        meeting6.setId("10");

        group.addMeeting(meeting1);
        group.addMeeting(meeting2);
        group.addMeeting(meeting3);
        group.addMeeting(meeting4);
        group.addMeeting(meeting5);
        group.addMeeting(meeting6);

        assertTrue(serializationService.serialize(group).matches("^\\{\"type\":\"group\",\"groupId\":\"11\",\"meetings\":\\[\"5\"," +
                "\"6\",\"7\",\"8\",\"9\",\"10\"\\],\"members\":\\[(?:\\{\"authorId\":\"4\",\"roles\":\\[(?:\"TIMEKEEPER\",?()|\"VISUALIZER\",?()|" +
                "\"OBSERVER\",?()){3}\\1\\2\\3\\]\\},?()|\\{\"authorId\":\"1\",\"roles\":\\[\"ADMINISTRATOR\"\\]\\},?()|\\{\"authorId\":\"2\",\"roles\":" +
                "\\[(?:\"MODERATOR\",?()|\"ADMINISTRATOR\",?()){2}\\1\\2\\]\\},?()|\\{\"authorId\":\"3\",\"roles\":\\[\"MOUTH\"\\]\\},?()){4}\\1\\2\\3\\4" +
                "\\]\\}$"));
    }

    @Test
    public void bulkSerialize() {
        Member member1 = new Member("1");
        Member member2 = new Member("2");
        Note note = new Note(member1, 3);
        note.setId("4");
        note.setText("a");

        Collection<ModelClass> data = new ArrayList<>();
        data.add(member1);
        data.add(member2);
        data.add(note);

        assertEquals("{\"payload\":[{\"type\":\"member\",\"authorId\":\"1\"},{\"type\":\"member\",\"authorId\":\"2\"},{\"type\":\"note\",\"noteId\":" +
                "\"4\",\"createdBy\":\"1\",\"createdAt\":3,\"text\":\"a\"}]}", serializationService.bulkSerialize(data));
    }

    @Test
    public void deserializeMember() {
        Member member = new Member("1");

        ModelClass deserialized = serializationService.deserialize("{\"type\":\"member\",\"authorId\":\"1\"}");

        assertTrue(deserialized instanceof Member);

        Member desMember = (Member) deserialized;
        assertEquals(member.getId(), desMember.getId());
    }

    @Test
    public void deserializeNote() {
        Member member = new Member("1");
        member.entity().save();
        Note note = new Note(member, 2);
        note.setId("3");
        note.setText("a");

        ModelClass deserialized = serializationService.deserialize("{\"type\":\"note\",\"noteId\":\"3\",\"createdBy\":\"1\",\"createdAt\":2,\"text\":" +
                "\"a\"}");

        assertTrue(deserialized instanceof Note);

        Note desNote = (Note) deserialized;
        assertEquals(note.getId(), desNote.getId());
        assertEquals(note.getCreatedBy().getId(), desNote.getCreatedBy().getId());
        assertEquals(note.getCreatedAt(), desNote.getCreatedAt());
        assertEquals(note.getText(), desNote.getText());
    }

    @Test
    public void deserializeMeeting() {
        Member member = new Member("1");
        Note note1 = new Note(member, 2);
        note1.setId("3");
        note1.setText("a");
        Note note2 = new Note(member, 4);
        note2.setId("5");
        note2.setText("b");
        Meeting meeting = new Meeting();
        meeting.setId("6");
        meeting.setStartTime(7);
        meeting.setEndTime(8);
        meeting.addNote(note1);
        meeting.addNote(note2);
        member.entity().save();
        note1.entity().save();
        note2.entity().save();
        note1.entity().meeting = null;
        note2.entity().meeting = null;

        ModelClass deserialized = serializationService.deserialize("{\"type\":\"meeting\",\"meetingId\":\"6\",\"startTime\":7,\"endTime\":8,\"notes\":" +
                "[\"3\",\"5\"]}");

        assertTrue(deserialized instanceof Meeting);

        Meeting desMeeting = (Meeting) deserialized;
        assertEquals(meeting.getId(), desMeeting.getId());
        assertEquals(meeting.getStartTime(), desMeeting.getStartTime());
        assertEquals(meeting.getEndTime(), desMeeting.getEndTime());
        assertEquals(2, desMeeting.getNotes().size());

        Iterator<Note> actualNotes = desMeeting.getNotes().iterator();
        Note actualNote1 = actualNotes.next();
        assertEquals(note1.getId(), actualNote1.getId());
        assertEquals(note1.getCreatedBy().getId(), actualNote1.getCreatedBy().getId());
        assertEquals(note1.getCreatedAt(), actualNote1.getCreatedAt());
        assertEquals(note1.getText(), actualNote1.getText());

        Note actualNote2 = actualNotes.next();
        assertEquals(note2.getId(), actualNote2.getId());
        assertEquals(note2.getCreatedBy().getId(), actualNote2.getCreatedBy().getId());
        assertEquals(note2.getCreatedAt(), actualNote2.getCreatedAt());
        assertEquals(note2.getText(), actualNote2.getText());
    }

    @Test
    public void deserializeGroup() {
        Member member1 = new Member("1");
        Member member2 = new Member("2");

        Meeting meeting1 = new Meeting();
        meeting1.setId("3");
        meeting1.setStartTime(4);
        Meeting meeting2 = new Meeting();
        meeting2.setId("5");
        meeting2.setStartTime(6);

        Group group = new Group("7");
        group.addMember(member1);
        group.addMember(member2);
        group.addMeeting(meeting1);
        group.addMeeting(meeting2);

        member1.entity().save();
        member2.entity().save();

        member1.addRole(Role.ADMINISTRATOR, group);
        member1.addRole(Role.VISUALIZER, group);
        member2.addRole(Role.ADMINISTRATOR, group);

        meeting1.entity().save();
        meeting2.entity().save();

        MemberGroupRelation.deleteAll(MemberGroupRelation.class);

        ModelClass deserialized = serializationService.deserialize("{\"type\":\"group\",\"groupId\":\"7\",\"meetings\":[\"3\",\"5\"],\"members\":" +
                "[{\"authorId\":\"1\",\"roles\":[\"ADMINISTRATOR\",\"VISUALIZER\"]},{\"authorId\":\"2\",\"roles\":[\"ADMINISTRATOR\"]}]}");

        assertTrue(deserialized instanceof Group);

        Group desGroup = (Group) deserialized;
        assertEquals(group.getId(), desGroup.getId());

        Iterator<Member> actualMembers = desGroup.getMembers().iterator();
        assertEquals(2, desGroup.getMembers().size());

        Member actualMember1 = actualMembers.next();
        assertEquals(member1.getId(), actualMember1.getId());

        Member actualMember2 = actualMembers.next();
        assertEquals(member2.getId(), actualMember2.getId());

        Iterator<Meeting> actualMeetings = desGroup.getMeetings().iterator();
        assertEquals(2, desGroup.getMeetings().size());

        Meeting actualMeeting1 = actualMeetings.next();
        assertEquals(meeting1.getId(), actualMeeting1.getId());
        assertEquals(meeting1.getStartTime(), actualMeeting1.getStartTime());

        Meeting actualMeeting2 = actualMeetings.next();
        assertEquals(meeting2.getId(), actualMeeting2.getId());
        assertEquals(meeting2.getStartTime(), actualMeeting2.getStartTime());
    }

    @Test
    public void bulkDeserialize() {
        Member member1 = new Member("1");
        Member member2 = new Member("2");
        Note note = new Note(member1, 3);
        note.setId("4");
        note.setText("a");

        member1.entity().save();
        member2.entity().save();

        Iterator<ModelClass> deserialized = serializationService.bulkDeserializeAndMerge("{\"payload\":[{\"type\":\"member\",\"authorId\":\"1\"},{\"type\":" +
                "\"member\",\"authorId\":\"2\"},{\"type\":\"note\",\"noteId\":\"4\",\"createdBy\":\"1\",\"createdAt\":3,\"text\":\"a\"}]}").iterator();
        ModelClass first = deserialized.next();
        ModelClass second = deserialized.next();
        ModelClass third = deserialized.next();

        assertTrue(first instanceof Member);
        assertTrue(second instanceof Member);
        assertTrue(third instanceof Note);

        Member actMember1 = (Member) first;
        Member actMember2 = (Member) second;
        Note actNote = (Note) third;

        assertEquals(member1.getId(), actMember1.getId());
        assertEquals(member2.getId(), actMember2.getId());
        assertEquals(note.getId(), actNote.getId());
        assertEquals(note.getCreatedBy().getId(), member1.getId());
        assertEquals(note.getCreatedAt(), actNote.getCreatedAt());
        assertEquals(note.getText(), actNote.getText());
    }

} */