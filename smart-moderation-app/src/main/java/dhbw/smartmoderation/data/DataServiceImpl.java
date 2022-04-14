package dhbw.smartmoderation.data;


import org.briarproject.bramble.api.identity.Author;
import org.briarproject.briar.api.privategroup.GroupMember;
import org.greenrobot.greendao.database.Database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import dhbw.smartmoderation.SmartModerationApplication;
import dhbw.smartmoderation.SmartModerationApplicationImpl;
import dhbw.smartmoderation.data.model.ConsensusLevel;
import dhbw.smartmoderation.data.model.ConsensusLevelDao;
import dhbw.smartmoderation.data.model.Contact;
import dhbw.smartmoderation.data.model.DaoMaster;
import dhbw.smartmoderation.data.model.DaoSession;
import dhbw.smartmoderation.data.model.Group;
import dhbw.smartmoderation.data.model.GroupDao;
import dhbw.smartmoderation.data.model.GroupSettings;
import dhbw.smartmoderation.data.model.GroupSettingsDao;
import dhbw.smartmoderation.data.model.Meeting;
import dhbw.smartmoderation.data.model.MeetingDao;
import dhbw.smartmoderation.data.model.Member;
import dhbw.smartmoderation.data.model.MemberDao;
import dhbw.smartmoderation.data.model.MemberGroupRelation;
import dhbw.smartmoderation.data.model.MemberGroupRelationDao;
import dhbw.smartmoderation.data.model.MemberMeetingRelation;
import dhbw.smartmoderation.data.model.MemberMeetingRelationDao;
import dhbw.smartmoderation.data.model.ModerationCard;
import dhbw.smartmoderation.data.model.ModerationCardDao;
import dhbw.smartmoderation.data.model.Participation;
import dhbw.smartmoderation.data.model.ParticipationDao;
import dhbw.smartmoderation.data.model.Poll;
import dhbw.smartmoderation.data.model.PollDao;
import dhbw.smartmoderation.data.model.PrivateGroup;
import dhbw.smartmoderation.data.model.Topic;
import dhbw.smartmoderation.data.model.TopicDao;
import dhbw.smartmoderation.data.model.Voice;
import dhbw.smartmoderation.data.model.VoiceDao;
import dhbw.smartmoderation.exceptions.GroupNotFoundException;
import dhbw.smartmoderation.exceptions.GroupSettingsNotFoundException;
import dhbw.smartmoderation.exceptions.MeetingNotFoundException;
import dhbw.smartmoderation.exceptions.MemberNotFoundException;
import dhbw.smartmoderation.exceptions.ModerationCardNotFoundException;
import dhbw.smartmoderation.exceptions.PollNotFoundException;
import dhbw.smartmoderation.exceptions.VoiceNotFoundException;
import dhbw.smartmoderation.util.Util;

public class DataServiceImpl implements DataService {

    private DaoSession daoSession;
    private MemberDao memberDao;
    private MeetingDao meetingDao;
    private GroupDao groupDao;
    private GroupSettingsDao groupSettingsDao;
    private PollDao pollDao;
    private TopicDao topicDao;
    private VoiceDao voiceDao;
    private ParticipationDao participationDao;
    private ConsensusLevelDao consensusLevelDao;
    private MemberGroupRelationDao memberGroupRelationDao;
    private MemberMeetingRelationDao memberMeetingRelationDao;
    private ModerationCardDao moderationCardDao;

    public DataServiceImpl() {

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(((SmartModerationApplicationImpl) SmartModerationApplicationImpl.getApp()).getApplicationContext(), "notes-db");
        Database db = helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();
        memberDao = daoSession.getMemberDao();
        meetingDao = daoSession.getMeetingDao();
        groupDao = daoSession.getGroupDao();
        groupSettingsDao = daoSession.getGroupSettingsDao();
        pollDao = daoSession.getPollDao();
        topicDao = daoSession.getTopicDao();
        voiceDao = daoSession.getVoiceDao();
        participationDao = daoSession.getParticipationDao();
        consensusLevelDao = daoSession.getConsensusLevelDao();
        memberGroupRelationDao = daoSession.getMemberGroupRelationDao();
        memberMeetingRelationDao = daoSession.getMemberMeetingRelationDao();
        moderationCardDao = daoSession.getModerationCardDao();
    }

    @Override
    public synchronized void saveMember(Member member) {

        Collection<Member> allMembers = getMembers();
        for (Member currentMember : allMembers) {
            if (currentMember.getMemberId().equals(member.getMemberId())) {
                member.setMemberId(currentMember.getMemberId());
                break;
            }
        }

        memberDao.insertOrReplaceInTx(member);
    }

    @Override
    public synchronized void mergeMember(Member member) {

        Collection<Member> allMembers = getMembers();
        Member previousMember = null;

        for (Member currentMember : allMembers) {
            if (currentMember.getMemberId().equals(member.getMemberId())) {
                previousMember = currentMember;
                member.setMemberId(currentMember.getMemberId());
                saveMember(previousMember);
            }
        }

        if (previousMember != null) {
            if (member.getName() == null || member.getName().isEmpty())
                member.setName(previousMember.getName());
        }

        memberDao.insertOrReplaceInTx(member);
    }

    @Override
    public synchronized void deleteMember(Member member) {
        memberDao.deleteInTx(member);
    }

    @Override
    public synchronized Collection<Member> getMembers() {
        return memberDao.loadAll();
    }

    @Override
    public synchronized Member getMember(Long memberId) throws MemberNotFoundException {

        Member member = memberDao.load(memberId);

        if (member != null) {
            return member;
        }

        throw new MemberNotFoundException();
    }

    @Override
    public Member getMember(Contact contact) throws MemberNotFoundException {

        Member member = memberDao.load(contact.getId());

        if (member != null) {
            return member;
        }

        throw new MemberNotFoundException();
    }

    @Override
    public Member getMember(Author author) throws MemberNotFoundException {

        Member member = memberDao.load(Util.bytesToLong(author.getId().getBytes()));

        if (member != null) {
            return member;
        }

        throw new MemberNotFoundException();
    }

    @Override
    public Member getMember(GroupMember connectionMember) throws MemberNotFoundException {
        return null;
    }

    @Override
    public void saveMeeting(Meeting meeting) {

        Collection<Meeting> allMeetings = getMeetings();

        for (Meeting currentMeeting : allMeetings) {
            if (currentMeeting.getMeetingId().equals(meeting.getMeetingId())) {
                meeting.setMeetingId(currentMeeting.getMeetingId());
                break;
            }
        }

        meetingDao.insertOrReplaceInTx(meeting);
    }

    @Override
    public synchronized void mergeMeeting(Meeting meeting) {

        if (meeting.isDeleted()) {
            try {
                Meeting meetingToDelete = getMeeting(meeting.getMeetingId());
                deleteMeeting(meetingToDelete);
            } catch (MeetingNotFoundException e) {
                e.printStackTrace();
            }
            return;
        }

        Group group = null;

        try {
            group = getGroup(meeting.getGroupId());
        } catch (GroupNotFoundException e) {
            e.printStackTrace();
        }

        if (group == null) return;


        Collection<Meeting> allMeetings = getMeetings();

        Meeting previousMeeting = null;

        for (Meeting currentMeeting : allMeetings) {
            if (currentMeeting.getMeetingId().equals(meeting.getMeetingId())) {
                previousMeeting = currentMeeting;
                meeting.setMeetingId(currentMeeting.getMeetingId());
                saveMeeting(previousMeeting);
            }
        }

        if (previousMeeting != null) {
            if (meeting.getStartTime() == 0) {
                meeting.setStartTime(previousMeeting.getStartTime());
            }
            if (meeting.getEndTime() == 0) {
                meeting.setEndTime(previousMeeting.getEndTime());
            }
            if (meeting.getCause() == null || meeting.getCause().isEmpty()) {
                meeting.setCause(previousMeeting.getCause());
            }
            if (meeting.getLocation() == null || meeting.getLocation().isEmpty()) {
                meeting.setLocation(previousMeeting.getLocation());
            }
            if (!meeting.getOnline()) {
                meeting.setOnline(previousMeeting.getOnline());
            }
            if (!meeting.getOpen()) {
                meeting.setOpen(previousMeeting.getOpen());
            }
            if (meeting.getDate() == 0) {
                meeting.setDate(previousMeeting.getDate());
            }
        }
        meetingDao.insertOrReplaceInTx(meeting);
    }

    @Override
    public synchronized void deleteMeeting(Meeting meeting) {

        meeting = meetingDao.load(meeting.getMeetingId());

        for (Member member : meeting.getMembers()) {
            meeting.removeMember(member);
        }

        for (Topic topic : meeting.getTopics()) {
            deleteTopic(topic);
        }

        for (Poll poll : meeting.getPolls()) {
            deletePoll(poll);
        }

        for (Participation participation : meeting.getParticipations()) {
            deleteParticipation(participation);
        }

        meetingDao.deleteInTx(meeting);
    }

    @Override
    public synchronized Collection<Meeting> getMeetings() {
        return meetingDao.loadAll();
    }

    @Override
    public synchronized Meeting getMeeting(Long meetingId) throws MeetingNotFoundException {

        if (meetingDao.load(meetingId) != null)
            meetingDao.detach(meetingDao.load(meetingId));

        Meeting meeting = meetingDao.load(meetingId);

        if (meeting != null) return meeting;

        throw new MeetingNotFoundException();
    }

    @Override
    public synchronized void saveGroup(Group group) {

        Collection<Group> allGroups = getGroups();

        for (Group currentGroup : allGroups) {
            if (currentGroup.getGroupId().equals(group.getGroupId())) {
                group.setGroupId(currentGroup.getGroupId());
                break;
            }
        }

        groupDao.insertOrReplaceInTx(group);
    }

    @Override
    public synchronized void mergeGroup(Group group) {

        if (group.isDeleted()) {
            try {
                Group groupToDelete = getGroup(group.getGroupId());
                deleteGroup(groupToDelete);
            } catch (GroupNotFoundException e) {
                e.printStackTrace();
            }
            return;
        }

        Collection<Group> allGroups = getGroups();
        Group previousGroup = null;

        for (Group currentGroup : allGroups) {
            if (currentGroup.getGroupId().equals(group.getGroupId())) {
                previousGroup = currentGroup;
                group.setGroupId(currentGroup.getGroupId());
                saveGroup(previousGroup);
            }
        }

        if (previousGroup != null) {
            if (group.getName() == null || group.getName().isEmpty()) {
                group.setName(previousGroup.getName());
            }
        }

        groupDao.insertOrReplaceInTx(group);
    }

    @Override
    public synchronized void deleteGroup(Group group) {

        Collection<Member> members = group.getUniqueMembers();

        for (Member member : group.getMembers()) {
            group.removeMember(member);
        }

        for (Meeting meeting : group.getMeetings()) {
            deleteMeeting(meeting);
        }

        deleteGroupSettings(group.getGroupSettings());

        groupDao.deleteInTx(group);

        for (Member member : members) {
            if (member.getGroups().size() == 0) {
                deleteMember(member);
            }
        }
    }

    @Override
    public synchronized Collection<Group> getGroups() {
        return groupDao.loadAll();
    }

    @Override
    public synchronized Group getGroup(Long groupId) throws GroupNotFoundException {

        if (groupDao.load(groupId) != null) {
            groupDao.detach(groupDao.load(groupId));
        }

        Group group = groupDao.load(groupId);

        if (group != null) {
            return group;
        }

        throw new GroupNotFoundException();
    }

    @Override
    public Group getGroup(PrivateGroup privateGroup) throws GroupNotFoundException {

        Group group = groupDao.load(privateGroup.getId());

        if (group != null) {
            return group;
        }

        throw new GroupNotFoundException();
    }

    @Override
    public Group getGroup(org.briarproject.briar.api.privategroup.PrivateGroup connectionGroup) throws GroupNotFoundException {

        Group group = groupDao.load(Util.bytesToLong(connectionGroup.getId().getBytes()));

        if (group != null) {
            return group;
        }

        throw new GroupNotFoundException();
    }

    @Override
    public synchronized void savePoll(Poll poll) {

        Collection<Poll> allPolls = getPolls();

        for (Poll currentPoll : allPolls) {
            if (currentPoll.getPollId().equals(poll.getPollId())) {
                poll.setPollId(currentPoll.getPollId());
                break;
            }
        }

        pollDao.insertOrReplaceInTx(poll);
    }

    @Override
    public synchronized void mergePoll(Poll poll) {

        if (poll.isDeleted()) {
            deletePoll(poll);
            return;
        }

        Meeting meeting = null;

        try {
            meeting = getMeeting(poll.getMeetingId());
        } catch (MeetingNotFoundException e) {
            e.printStackTrace();
        }

        if (meeting == null) return;

        Collection<Poll> allPolls = getPolls();

        Poll previousPoll = null;

        for (Poll currentPoll : allPolls) {
            if (currentPoll.getPollId().equals(poll.getPollId())) {
                previousPoll = currentPoll;
                poll.setPollId(currentPoll.getPollId());
                savePoll(previousPoll);
            }
        }

        if (previousPoll != null) {
            if (poll.getTitle() == null || poll.getTitle().isEmpty()) {
                poll.setTitle(previousPoll.getTitle());
            }
            if (poll.getNote() == null || poll.getNote().isEmpty()) {
                poll.setNote(previousPoll.getNote());
            }
            if (!poll.getIsOpen()) {
                poll.setIsOpen(previousPoll.getIsOpen());
            }
            if (!poll.getClosedByModerator()) {
                poll.setClosedByModerator(previousPoll.getClosedByModerator());
            }
            if (poll.getConsensusProposal() == null || poll.getConsensusProposal().isEmpty()) {
                poll.setConsensusProposal(previousPoll.getConsensusProposal());
            }
            if (poll.getVoteMembersCountOnClosed() == 0) {
                poll.setVoteMembersCountOnClosed(previousPoll.getVoteMembersCountOnClosed());
            }
        }
        pollDao.insertOrReplaceInTx(poll);
    }

    @Override
    public synchronized void deletePoll(Poll poll) {

        poll = pollDao.load(poll.getPollId());

        for (Voice voice : poll.getVoices()) deleteVoice(voice);

        pollDao.deleteInTx(poll);
    }

    @Override
    public synchronized Collection<Poll> getPolls() {
        return pollDao.loadAll();
    }

    @Override
    public synchronized Poll getPoll(Long pollId) throws PollNotFoundException {

        if (pollDao.load(pollId) != null) {
            pollDao.detach(pollDao.load(pollId));
        }

        Poll poll = pollDao.load(pollId);

        if (poll != null) return poll;

        throw new PollNotFoundException();
    }

    @Override
    public synchronized void saveVoice(Voice voice) {

        Collection<Voice> allVoices = getVoices();

        for (Voice currentVoice : allVoices) {
            if (currentVoice.getVoiceId().equals(voice.getVoiceId())) {
                voice.setVoiceId(currentVoice.getVoiceId());
                break;
            }
        }

        voiceDao.insertOrReplaceInTx(voice);
    }

    @Override
    public synchronized void mergeVoice(Voice voice) {

        if (voice.isDeleted()) {
            deleteVoice(voice);
            return;
        }


        Poll poll = null;

        try {
            poll = getPoll(voice.getPollId());
        } catch (PollNotFoundException e) {
            e.printStackTrace();
        }

        if (poll == null) {
            return;
        }

        Collection<Voice> allVoices = getVoices();

        Voice previousVoice = null;

        for (Voice currentVoice : allVoices) {
            if (currentVoice.getVoiceId().equals(voice.getVoiceId())) {
                previousVoice = currentVoice;
                voice.setVoiceId(currentVoice.getVoiceId());
                saveVoice(previousVoice);
            }
        }

        if (previousVoice != null) {
            if (voice.getExplanation() == null || voice.getExplanation().isEmpty()) {
                voice.setExplanation(previousVoice.getExplanation());
            }
        }

        voiceDao.insertOrReplaceInTx(voice);

        Voice savedVoice = null;

        try {
            savedVoice = getVoice(voice.getVoiceId());
        } catch (VoiceNotFoundException e) {
            e.printStackTrace();
        }

        if (savedVoice != null) {
            Collection<Long> presentVoteMemberIds = new ArrayList<>();
            for (Member presentVoteMember : savedVoice.getPoll().getMeeting().getPresentVoteMembers()) {
                presentVoteMemberIds.add(presentVoteMember.getMemberId());
            }

            Collection<Long> voiceMemberIds = new ArrayList<>();
            for (Voice v : savedVoice.getPoll().getVoices()) {
                voiceMemberIds.add(v.getMember().getMemberId());
            }

            if (voiceMemberIds.containsAll(presentVoteMemberIds)) {
                Poll p = savedVoice.getPoll();
                p.setVoteMembersCountOnClosed(presentVoteMemberIds.size());
                p.setIsOpen(false);
                mergePoll(p);
            }
        }
    }

    @Override
    public synchronized void deleteVoice(Voice voice) {
        voiceDao.deleteInTx(voice);
    }

    @Override
    public synchronized Collection<Voice> getVoices() {
        return voiceDao.loadAll();
    }

    @Override
    public synchronized Voice getVoice(Long voiceId) throws VoiceNotFoundException {

        if (voiceDao.load(voiceId) != null)
            voiceDao.detach(voiceDao.load(voiceId));


        Voice voice = voiceDao.load(voiceId);

        if (voice != null) return voice;

        throw new VoiceNotFoundException();

    }

    @Override
    public synchronized void saveConsensusLevel(ConsensusLevel level) {

        Collection<ConsensusLevel> allLevels = getConsensusLevels();

        for (ConsensusLevel currentLevel : allLevels) {
            if (currentLevel.getConsensusLevelId().equals(level.getConsensusLevelId())) {
                level.setConsensusLevelId(currentLevel.getConsensusLevelId());
                break;
            }
        }

        consensusLevelDao.insertOrReplaceInTx(level);
    }

    @Override
    public synchronized void mergeConsensusLevel(ConsensusLevel level) {

        if (level.isDeleted()) {
            ConsensusLevel consensusLevelToDelete = getConsensusLevel(level.getConsensusLevelId());
            deleteConsensusLevel(consensusLevelToDelete);
            return;
        }

        Collection<ConsensusLevel> allLevels = getConsensusLevels();
        ConsensusLevel previousLevel = null;

        for (ConsensusLevel currentLevel : allLevels) {
            if (currentLevel.getConsensusLevelId().equals(level.getConsensusLevelId())) {
                previousLevel = currentLevel;
                level.setConsensusLevelId(currentLevel.getConsensusLevelId());
                saveConsensusLevel(previousLevel);
            }
        }

        if (previousLevel != null) {
            if (level.getName() == null || level.getName().isEmpty()) {
                level.setName(previousLevel.getName());
            }
            if (level.getDescription() == null || level.getDescription().isEmpty()) {
                level.setDescription(previousLevel.getDescription());
            }
            if (level.getColor() == 0) {
                level.setColor(previousLevel.getColor());
            }
            if (level.getNumber() == 0) {
                level.setNumber(previousLevel.getNumber());
            }
        }

        consensusLevelDao.insertOrReplaceInTx(level);
    }

    @Override
    public synchronized void deleteConsensusLevel(ConsensusLevel level) {
        consensusLevelDao.deleteInTx(level);
    }

    @Override
    public synchronized Collection<ConsensusLevel> getConsensusLevels() {
        return consensusLevelDao.loadAll();
    }

    @Override
    public synchronized ConsensusLevel getConsensusLevel(Long consensusLevelId) {
        return consensusLevelDao.load(consensusLevelId);
    }

    @Override
    public synchronized void saveTopic(Topic topic) {

        Collection<Topic> allTopics = getTopics();

        for (Topic currentTopic : allTopics) {
            if (currentTopic.getTopicId().equals(topic.getTopicId())) {
                topic.setTopicId(currentTopic.getTopicId());
                break;
            }
        }

        topicDao.insertOrReplaceInTx(topic);
    }

    @Override
    public synchronized void mergeTopic(Topic topic) {

        if (topic.isDeleted()) {
            deleteTopic(topic);
            return;
        }

        Meeting meeting = null;

        try {
            meeting = getMeeting(topic.getMeetingId());
        } catch (MeetingNotFoundException e) {
            e.printStackTrace();
        }

        if (meeting == null) return;


        Collection<Topic> allTopics = getTopics();
        Topic previousTopic = null;

        for (Topic currentTopic : allTopics) {
            if (currentTopic.getTopicId().equals(topic.getTopicId())) {
                previousTopic = currentTopic;
                topic.setTopicId(currentTopic.getTopicId());
                saveTopic(previousTopic);
            }
        }
        if (previousTopic != null) {
            if (topic.getTitle() == null || topic.getTitle().isEmpty()) {
                topic.setTitle(previousTopic.getTitle());
            }
            if (topic.getStatus() == null) {
                topic.setStatus(previousTopic.getStatus());
            }
            if (topic.getDuration() == 0) {
                topic.setDuration(previousTopic.getDuration());
            }
        }

        topicDao.insertOrReplaceInTx(topic);
    }

    @Override
    public synchronized void deleteTopic(Topic topic) {
        topicDao.deleteInTx(topic);
    }

    @Override
    public synchronized Collection<Topic> getTopics() {
        return topicDao.loadAll();
    }

    @Override
    public synchronized void saveGroupSettings(GroupSettings settings) {

        Collection<GroupSettings> allSettings = getGroupSettings();

        for (GroupSettings currentSettings : allSettings) {
            if (currentSettings.getSettingsId().equals(settings.getSettingsId())) {
                settings.setSettingsId(currentSettings.getSettingsId());
                break;
            }
        }

        groupSettingsDao.insertOrReplaceInTx(settings);

    }

    @Override
    public void mergeGroupSettings(GroupSettings settings) {

        if (settings.isDeleted()) {
            deleteGroupSettings(settings);
            return;
        }

        Collection<GroupSettings> allSettings = getGroupSettings();
        GroupSettings previousSettings = null;

        for (GroupSettings currentSettings : allSettings) {
            if (currentSettings.getSettingsId().equals(settings.getSettingsId())) {
                previousSettings = currentSettings;
                settings.setSettingsId(currentSettings.getSettingsId());
                saveGroupSettings(previousSettings);
            }
        }

        if (previousSettings != null) {
            Set<Long> deletedConsensusLevels = new HashSet<>();

            saveGroupSettings(settings);
            settings = groupSettingsDao.load(settings.getSettingsId());

            for (ConsensusLevel consensusLevel : settings.getConsensusLevels()) {
                if (consensusLevel.isDeleted()) {
                    deletedConsensusLevels.add(consensusLevel.getConsensusLevelId());
                }

            }

            for (ConsensusLevel consensusLevel : previousSettings.getConsensusLevels()) {
                if (deletedConsensusLevels.contains(consensusLevel.getConsensusLevelId())) {
                    deleteConsensusLevel(consensusLevel);
                }
            }
        }

        groupSettingsDao.insertOrReplaceInTx(settings);
    }

    @Override
    public void deleteGroupSettings(GroupSettings settings) {

        for (ConsensusLevel consensusLevel : settings.getConsensusLevels()) {
            deleteConsensusLevel(consensusLevel);
        }

        groupSettingsDao.deleteInTx(settings);
    }

    @Override
    public Collection<GroupSettings> getGroupSettings() {
        return groupSettingsDao.loadAll();
    }

    @Override
    public synchronized GroupSettings getGroupSetting(Long settingsId) throws GroupSettingsNotFoundException {

        if (groupSettingsDao.load(settingsId) != null)
            groupSettingsDao.detach(groupSettingsDao.load(settingsId));

        GroupSettings groupSettings = groupSettingsDao.load(settingsId);

        if (groupSettings != null) return groupSettings;

        throw new GroupSettingsNotFoundException();
    }

    @Override
    public void saveParticipation(Participation participation) {

        Collection<Participation> allParticipations = getParticipations();

        for (Participation currentParticipation : allParticipations) {
            if (currentParticipation.getParticipationId().equals(participation.getParticipationId())) {
                participation.setParticipationId(currentParticipation.getParticipationId());
                break;
            }
        }
        participationDao.insertOrReplaceInTx(participation);
    }

    @Override
    public void mergeParticipation(Participation participation) {

        if (participation.isDeleted()) {
            deleteParticipation(participation);
            return;
        }

        Meeting meeting = null;

        try {
            meeting = getMeeting(participation.getMeetingId());
        } catch (MeetingNotFoundException e) {
            e.printStackTrace();
        }

        if (meeting == null) return;

        Collection<Participation> allParticipation = getParticipations();
        Participation previousParticipation = null;

        for (Participation currentParticipation : allParticipation) {
            if (currentParticipation.getParticipationId().equals(participation.getParticipationId())) {
                previousParticipation = currentParticipation;
                participation.setParticipationId(currentParticipation.getParticipationId());
                saveParticipation(previousParticipation);
            }
        }

        if (previousParticipation != null) {
            if (participation.getContributions() == 0) {
                participation.setContributions(previousParticipation.getContributions());
            }
            if (participation.getTime() == 0) {
                participation.setTime(previousParticipation.getTime());
            }

        }

        participationDao.insertOrReplaceInTx(participation);
    }

    @Override
    public void deleteParticipation(Participation participation) {
        participationDao.deleteInTx(participation);
    }

    @Override
    public Collection<Participation> getParticipations() {
        return participationDao.loadAll();
    }

    public void saveMemberGroupRelation(MemberGroupRelation memberGroupRelation) {
        memberGroupRelationDao.insertOrReplaceInTx(memberGroupRelation);
    }

    public void saveMemberMeetingRelation(MemberMeetingRelation memberMeetingRelation) {
        memberMeetingRelationDao.insertOrReplaceInTx(memberMeetingRelation);
    }

    @Override
    public Collection<MemberGroupRelation> getMemberGroupRelations() {
        return memberGroupRelationDao.loadAll();
    }

    @Override
    public Collection<MemberMeetingRelation> getMemberMeetingRelations() {
        return memberMeetingRelationDao.loadAll();
    }

    @Override
    public void deleteMemberGroupRelation(MemberGroupRelation memberGroupRelation) {
        memberGroupRelationDao.delete(memberGroupRelation);
    }

    @Override
    public void deleteMemberMeetingRelation(MemberMeetingRelation memberMeetingRelation) {
        memberMeetingRelationDao.delete(memberMeetingRelation);
    }

    @Override
    public void mergeModerationCard(ModerationCard moderationCard) {
        if (moderationCard.isDeleted()) {
            deleteModerationCard(moderationCard);
            return;
        }

        Meeting meeting = null;

        try {
            meeting = getMeeting(moderationCard.getMeetingId());
        } catch (MeetingNotFoundException e) {
            e.printStackTrace();
        }

        if (meeting == null) return;

        moderationCardDao.insertOrReplaceInTx(moderationCard);
    }

    @Override
    public void deleteModerationCard(ModerationCard moderationCard) {
        moderationCardDao.deleteInTx(moderationCard);
    }

    @Override
    public ModerationCard getModerationCard(Long cardId) throws ModerationCardNotFoundException {
        if (moderationCardDao.load(cardId) != null) {
            moderationCardDao.detach(moderationCardDao.load(cardId));
        }

        ModerationCard moderationCard = moderationCardDao.load(cardId);

        if (moderationCard != null) return moderationCard;

        throw new ModerationCardNotFoundException();
    }
}
