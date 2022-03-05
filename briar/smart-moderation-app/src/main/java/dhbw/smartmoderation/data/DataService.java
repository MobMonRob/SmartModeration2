package dhbw.smartmoderation.data;

import org.briarproject.bramble.api.identity.Author;
import org.briarproject.briar.api.privategroup.GroupMember;

import java.util.Collection;

import dhbw.smartmoderation.data.model.ConsensusLevel;
import dhbw.smartmoderation.data.model.Contact;
import dhbw.smartmoderation.data.model.Group;
import dhbw.smartmoderation.data.model.GroupSettings;
import dhbw.smartmoderation.data.model.Meeting;
import dhbw.smartmoderation.data.model.Member;
import dhbw.smartmoderation.data.model.MemberGroupRelation;
import dhbw.smartmoderation.data.model.MemberMeetingRelation;
import dhbw.smartmoderation.data.model.ModerationCard;
import dhbw.smartmoderation.data.model.Participation;
import dhbw.smartmoderation.data.model.Poll;
import dhbw.smartmoderation.data.model.PrivateGroup;
import dhbw.smartmoderation.data.model.Topic;
import dhbw.smartmoderation.data.model.Voice;
import dhbw.smartmoderation.exceptions.GroupNotFoundException;
import dhbw.smartmoderation.exceptions.GroupSettingsNotFoundException;
import dhbw.smartmoderation.exceptions.MeetingNotFoundException;
import dhbw.smartmoderation.exceptions.MemberNotFoundException;
import dhbw.smartmoderation.exceptions.ModerationCardNotFoundException;
import dhbw.smartmoderation.exceptions.PollNotFoundException;
import dhbw.smartmoderation.exceptions.VoiceNotFoundException;

/**
 * A service for accessing the database.
 */
public interface DataService {

	void saveMember(Member member);

	void mergeMember(Member member);

	void deleteMember(Member member);

	Collection<Member> getMembers();

	Member getMember(Long memberId) throws MemberNotFoundException;

	Member getMember(Contact contact) throws MemberNotFoundException;

	Member getMember(Author author) throws MemberNotFoundException;

	Member getMember(GroupMember connectionMember) throws MemberNotFoundException;

	void saveMeeting(Meeting meeting);

	void mergeMeeting(Meeting meeting);

	void deleteMeeting(Meeting meeting);

	Collection<Meeting> getMeetings();

	Meeting getMeeting(Long meetingId) throws MeetingNotFoundException;

	void saveGroup(Group group);

	void mergeGroup(Group group);

	void deleteGroup(Group group);

	Collection<Group> getGroups();

	Group getGroup (Long groupId)  throws GroupNotFoundException;

	Group getGroup(PrivateGroup privateGroup) throws GroupNotFoundException;

	Group getGroup(org.briarproject.briar.api.privategroup.PrivateGroup connectionGroup ) throws GroupNotFoundException;

	void savePoll (Poll poll);

	void mergePoll (Poll poll);

	void deletePoll (Poll poll);

	Collection<Poll> getPolls();

	Poll getPoll(Long pollId) throws PollNotFoundException;

	void saveVoice (Voice voice);

	void mergeVoice (Voice voice);

	void deleteVoice (Voice voice);

	Collection<Voice> getVoices();

	Voice getVoice(Long voiceId) throws VoiceNotFoundException;

	void saveConsensusLevel (ConsensusLevel level);

	void mergeConsensusLevel (ConsensusLevel level);

	void deleteConsensusLevel (ConsensusLevel level);

	Collection<ConsensusLevel> getConsensusLevels();

	ConsensusLevel getConsensusLevel(Long consensusLevelId);

	void saveTopic (Topic topic);

	void mergeTopic (Topic topic);

	void deleteTopic (Topic topic);

	Collection<Topic> getTopics();

	void saveGroupSettings (GroupSettings settings);

	void mergeGroupSettings (GroupSettings settings);

	void deleteGroupSettings (GroupSettings settings);

	Collection<GroupSettings> getGroupSettings();

	GroupSettings getGroupSetting (Long settingsId) throws GroupSettingsNotFoundException;

	void saveParticipation (Participation participation);

	void mergeParticipation (Participation participation);

	void deleteParticipation (Participation participation);

	Collection<Participation> getParticipations();

	void saveMemberGroupRelation(MemberGroupRelation memberGroupRelation);

	void saveMemberMeetingRelation(MemberMeetingRelation memberMeetingRelation);

	Collection<MemberGroupRelation> getMemberGroupRelations();

	Collection<MemberMeetingRelation> getMemberMeetingRelations();

	void deleteMemberGroupRelation(MemberGroupRelation memberGroupRelation);

	void deleteMemberMeetingRelation(MemberMeetingRelation memberMeetingRelation);

    void mergeModerationCard(ModerationCard moderationCard);

	void deleteModerationCard(ModerationCard moderationCard);

	ModerationCard getModerationCard(Long cardId) throws ModerationCardNotFoundException;

	Collection<ModerationCard> getModerationCards() ;
}
