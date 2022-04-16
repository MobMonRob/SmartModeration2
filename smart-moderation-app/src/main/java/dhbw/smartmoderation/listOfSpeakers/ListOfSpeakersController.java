package dhbw.smartmoderation.listOfSpeakers;

import org.briarproject.briar.api.privategroup.PrivateGroup;

import java.util.ArrayList;
import java.util.Collection;

import dhbw.smartmoderation.controller.SmartModerationController;
import dhbw.smartmoderation.data.model.Attendance;
import dhbw.smartmoderation.data.model.Group;
import dhbw.smartmoderation.data.model.Meeting;
import dhbw.smartmoderation.data.model.Member;
import dhbw.smartmoderation.data.model.ModelClass;
import dhbw.smartmoderation.data.model.Participation;
import dhbw.smartmoderation.data.model.Role;
import dhbw.smartmoderation.exceptions.CantDeleteCurrentSpeakerException;
import dhbw.smartmoderation.exceptions.GroupNotFoundException;
import dhbw.smartmoderation.exceptions.MeetingNotFoundException;
import dhbw.smartmoderation.exceptions.MemberNotFoundException;
import dhbw.smartmoderation.exceptions.ParitcipationCantBeChangedException;
import dhbw.smartmoderation.exceptions.ParticipationCantBeAddedException;
import dhbw.smartmoderation.exceptions.ParticipationCantBeCreatedException;
import dhbw.smartmoderation.exceptions.ParticipationCantBeDeletedException;
import dhbw.smartmoderation.exceptions.ParticipationListCouldNotBeCleared;
import dhbw.smartmoderation.exceptions.SpeechCantBeStartedException;
import dhbw.smartmoderation.util.Util;

public class ListOfSpeakersController extends SmartModerationController {

    private Long meetingId;

    public ListOfSpeakersController(Long meetingId) {
        this.meetingId = meetingId;
    }

    public void update() {
        try {
            this.synchronizationService.pull(getPrivateGroup());
        } catch (GroupNotFoundException e) {
            e.printStackTrace();
        }
    }

    public PrivateGroup getPrivateGroup() throws GroupNotFoundException {
        Collection<PrivateGroup> privateGroups = connectionService.getGroups();

        for (PrivateGroup group : privateGroups) {
            if (getMeeting().getGroup().getGroupId().equals(Util.bytesToLong(group.getId().getBytes())))
                return group;
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

    public Collection<Member> getPresentMembersNotInSpeechList() {
        Collection<Member> presentMembers = new ArrayList<>();

        for (Member member : getMeeting().getMembers()) {
            if (member.getAttendance(getMeeting()) == Attendance.PRESENT) {
                boolean alreadyExisting = participationAlreadyExistsFor(member.getMemberId());
                if (alreadyExisting) {
                    Participation participation = getParticipation(member.getMemberId());
                    if (!participation.getIsInListOfSpeakers())
                        presentMembers.add(member);
                } else
                    presentMembers.add(member);
            }
        }
        return presentMembers;
    }

    public Long getLocalAuthorId() {
        return connectionService.getLocalAuthorId();
    }

    public boolean isLocalAuthorModerator() {
        Group group = getMeeting().getGroup();
        Long authorId = connectionService.getLocalAuthorId();
        Member member = group.getMember(authorId);
        return member != null && member.getRoles(group).contains(Role.MODERATOR);
    }

    public boolean isLocalAuthorPresent() {
        Collection<Member> members = getMeeting().getMembers();
        for (Member member : members) {
            if (member.getMemberId().equals(getLocalAuthorId()) && member.getAttendance(getMeeting()) == Attendance.PRESENT)
                return true;
        }
        return false;
    }

    public Collection<Participation> getParticipations() {
        Meeting meeting = getMeeting();
        return meeting.getParticipations();
    }

    public boolean participationAlreadyExistsFor(Long memberId) {
        for (Participation participation : getParticipations())
            if (participation.getMember().getMemberId().equals(memberId)) return true;
        return false;
    }

    public Participation getParticipation(Long memberId) {
        for (Participation participation : getParticipations())
            if (participation.getMember().getMemberId().equals(memberId)) return participation;
        return null;
    }


    public Collection<Participation> getParticipationsInList() {
        Meeting meeting = getMeeting();
        Collection<Participation> participations = new ArrayList<>();
        for (Participation participation : meeting.getParticipations())
            if (participation.getIsInListOfSpeakers()) participations.add(participation);
        return participations;
    }

    public Participation getNextParticipation() {
        for (Participation participation : getParticipationsInList())
            if (participation.getNumber() == 1) return participation;
        return null;
    }

    public boolean isLocalAuthorInSpeechList() {
        Long authorId = connectionService.getLocalAuthorId();
        for (Participation participation : getParticipationsInList()) {
            if (participation.getMember().getMemberId().equals(authorId) && participation.getIsInListOfSpeakers())
                return true;
        }
        return false;
    }

    public void changeParticipationNumbering(Collection<Participation> participations) throws ParitcipationCantBeChangedException {
        PrivateGroup group;
        try {
            group = getPrivateGroup();
        } catch (GroupNotFoundException exception) {
            throw new ParitcipationCantBeChangedException();
        }

        Collection<ModelClass> data = new ArrayList<>();

        for (Participation participation : participations) {
            dataService.mergeParticipation(participation);
            data.add(participation);
        }

        synchronizationService.push(group, data);
    }

    public void addParticipationToSpeechList(Participation participation) throws ParticipationCantBeAddedException {
        PrivateGroup group;

        try {
            group = getPrivateGroup();
        } catch (GroupNotFoundException exception) {
            throw new ParticipationCantBeAddedException();
        }

        Collection<ModelClass> data = new ArrayList<>();
        participation.setNumber(getParticipationsInList().size() + 1);
        participation.setIsInListOfSpeakers(true);
        dataService.mergeParticipation(participation);
        data.add(participation);
        synchronizationService.push(group, data);
    }

    public void createParticipationForLocalAuthor() throws ParticipationCantBeCreatedException {
        PrivateGroup group;
        Member member;

        try {
            group = getPrivateGroup();
            member = dataService.getMember(connectionService.getLocalAuthorId());
        } catch (GroupNotFoundException | MemberNotFoundException exception) {
            throw new ParticipationCantBeCreatedException();
        }

        Collection<ModelClass> data = new ArrayList<>();

        Participation participation = new Participation();
        participation.setMeeting(getMeeting());
        participation.setMember(member);
        participation.setIsInListOfSpeakers(true);
        participation.setNumber(getParticipationsInList().size() + 1);
        dataService.mergeParticipation(participation);
        data.add(participation);
        synchronizationService.push(group, data);
    }

    public void createParticipation(Member member) throws ParticipationCantBeCreatedException {
        PrivateGroup group;
        try {
            group = getPrivateGroup();
        } catch (GroupNotFoundException exception) {
            throw new ParticipationCantBeCreatedException();
        }

        Collection<ModelClass> data = new ArrayList<>();

        Participation participation = new Participation();
        participation.setMeeting(getMeeting());
        participation.setMember(member);
        participation.setIsInListOfSpeakers(true);
        participation.setNumber(getParticipationsInList().size() + 1);
        dataService.mergeParticipation(participation);
        data.add(participation);
        synchronizationService.push(group, data);
    }

    public void removeParticipationFromSpeechList(Participation participation) throws ParticipationCantBeDeletedException,
            CantDeleteCurrentSpeakerException {
        PrivateGroup group;
        try {
            group = getPrivateGroup();
        } catch (GroupNotFoundException exception) {
            throw new ParticipationCantBeDeletedException();
        }

        Collection<ModelClass> data = new ArrayList<>();

        if (participation.getIsSpeaking()) throw new CantDeleteCurrentSpeakerException();

        participation.setIsInListOfSpeakers(false);
        dataService.mergeParticipation(participation);
        data.add(participation);
        synchronizationService.push(group, data);
    }

    public long getTotalTime() {
        long totalTime = 0;
        for (Participation participation : getParticipations())
            totalTime += participation.getTime();

        return totalTime;
    }

    public void startNextSpeech() throws SpeechCantBeStartedException {
        PrivateGroup group;

        try {
            group = getPrivateGroup();
        } catch (GroupNotFoundException exception) {
            throw new SpeechCantBeStartedException();
        }

        Collection<ModelClass> data = new ArrayList<>();

        for (Participation participation : getParticipationsInList()) {
            participation.setIsSpeaking(false);
            if (participation.getNumber() == 1) {
                participation.setIsSpeaking(true);
                participation.setStartTime(System.currentTimeMillis());
            }

            dataService.mergeParticipation(participation);
            data.add(participation);
        }
        synchronizationService.push(group, data);
    }

    public void stopSpeech() throws ParticipationCantBeDeletedException, CantDeleteCurrentSpeakerException {

        Participation current = getNextParticipation();

        if (current.getIsSpeaking()) {
            current.setIsSpeaking(false);
            current.setContributions(current.getContributions() + 1);
            long time = current.getTime() + (System.currentTimeMillis() - current.getStartTime());
            current.setTime(time);
            current.setStartTime(0);
            removeParticipationFromSpeechList(current);
        }
    }

    public void clearParticipationList() throws ParticipationListCouldNotBeCleared {

        PrivateGroup group;
        try {
            group = getPrivateGroup();
        } catch (GroupNotFoundException exception) {
            throw new ParticipationListCouldNotBeCleared();
        }

        Collection<ModelClass> data = new ArrayList<>();

        for (Participation participation : getParticipationsInList()) {
            participation.setIsInListOfSpeakers(false);
            dataService.mergeParticipation(participation);
            data.add(participation);
        }

        synchronizationService.push(group, data);
    }
}
