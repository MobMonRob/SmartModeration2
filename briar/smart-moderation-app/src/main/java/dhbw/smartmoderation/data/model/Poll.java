package dhbw.smartmoderation.data.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.ToMany;
import org.greenrobot.greendao.annotation.ToOne;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import dhbw.smartmoderation.SmartModerationApplication;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.NotNull;

@Entity
public class Poll extends ModelClass {

    @Id
    private Long pollId;
    private String consensusProposal;
    private String title;
    private String note;
    private boolean isOpen;
    private boolean closedByModerator;
    private int voteMembersCountOnClosed;

    private long meetingId;
    @ToOne(joinProperty = "meetingId")
    private Meeting meeting;

    @ToMany(referencedJoinProperty = "pollId")
    private List<Voice> voices;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 744817627)
    private transient PollDao myDao;
    @Generated(hash = 1311574920)
    private transient Long meeting__resolvedKey;

    @Keep
    public Poll() {

        this.pollId = ((SmartModerationApplication)SmartModerationApplication.getApp()).getUniqueId();
    }

    @Generated(hash = 1231661361)
    public Poll(Long pollId, String consensusProposal, String title, String note, boolean isOpen,
            boolean closedByModerator, int voteMembersCountOnClosed, long meetingId) {
        this.pollId = pollId;
        this.consensusProposal = consensusProposal;
        this.title = title;
        this.note = note;
        this.isOpen = isOpen;
        this.closedByModerator = closedByModerator;
        this.voteMembersCountOnClosed = voteMembersCountOnClosed;
        this.meetingId = meetingId;
    }

    public Long getPollId() {
        return this.pollId;
    }

    public void setPollId(Long pollId) {
        this.pollId = pollId;
    }

    public String getConsensusProposal() {
        return this.consensusProposal;
    }

    public void setConsensusProposal(String consensusProposal) {
        this.consensusProposal = consensusProposal;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNote() {
        return this.note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean getIsOpen() {
        return this.isOpen;
    }

    public void setIsOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }

    public boolean getClosedByModerator() {
        return this.closedByModerator;
    }

    public void setClosedByModerator(boolean closedByModerator) {
        this.closedByModerator = closedByModerator;
    }

    public long getMeetingId() {
        return this.meetingId;
    }

    public void setMeetingId(long meetingId) {
        this.meetingId = meetingId;
    }

    public Voice getVoice(Long voiceId) {

        for(Voice voice : getVoices()) {

            if(voice.getVoiceId().equals(voiceId)) {
                return voice;
            }
        }

        return null;
    }

    public Status getStatus(Member member) {

        if(this.getClosedByModerator()) {

            return Status.ABGESCHLOSSEN;
        }

        Meeting meeting = getMeeting();

        Collection<Long> presentVoteMemberIds = new ArrayList<>();
        for(Member presentVoteMember : meeting.getPresentVoteMembers()) {
            presentVoteMemberIds.add(presentVoteMember.getMemberId());
        }

        Collection<Long> voiceMemberIds = new ArrayList<>();
        for(Voice voice : getVoices()) {
            voiceMemberIds.add(voice.getMember().getMemberId());
        }

        if(voiceMemberIds.containsAll(presentVoteMemberIds)) {
            return Status.ABGESCHLOSSEN;
        }

        if(voiceMemberIds.contains(member.getMemberId())) {
            return Status.BEWERTET;
        }

        if(presentVoteMemberIds.contains(member.getMemberId())) {

            if(this.getIsOpen()) {

                return Status.OFFEN;
            }

            return Status.ANGELEGT;
        }

        return Status.DEAKTIVIERT;
    }

    /** To-one relationship, resolved on first access. */
    @Generated(hash = 283313835)
    public Meeting getMeeting() {
        long __key = this.meetingId;
        if (meeting__resolvedKey == null || !meeting__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            MeetingDao targetDao = daoSession.getMeetingDao();
            Meeting meetingNew = targetDao.load(__key);
            synchronized (this) {
                meeting = meetingNew;
                meeting__resolvedKey = __key;
            }
        }
        return meeting;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1727209912)
    public void setMeeting(@NotNull Meeting meeting) {
        if (meeting == null) {
            throw new DaoException(
                    "To-one property 'meetingId' has not-null constraint; cannot set to-one to null");
        }
        synchronized (this) {
            this.meeting = meeting;
            meetingId = meeting.getMeetingId();
            meeting__resolvedKey = meetingId;
        }
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 846335259)
    public List<Voice> getVoices() {
        if (voices == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            VoiceDao targetDao = daoSession.getVoiceDao();
            List<Voice> voicesNew = targetDao._queryPoll_Voices(pollId);
            synchronized (this) {
                if (voices == null) {
                    voices = voicesNew;
                }
            }
        }
        return voices;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 237630216)
    public synchronized void resetVoices() {
        voices = null;
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    public int getVoteMembersCountOnClosed() {
        return this.voteMembersCountOnClosed;
    }

    public void setVoteMembersCountOnClosed(int voteMembersCountOnClosed) {
        this.voteMembersCountOnClosed = voteMembersCountOnClosed;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 640598541)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getPollDao() : null;
    }
}
