package dhbw.smartmoderation.data.model;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.ToOne;

import dhbw.smartmoderation.SmartModerationApplicationImpl;

@Entity
public class Participation extends ModelClass {

    @Id
    private Long participationId;
    private int contributions;
    private long time;
    private boolean isInListOfSpeakers;
    private int number;
    private boolean isSpeaking;
    private long startTime;

    private long meetingId;
    @ToOne(joinProperty = "meetingId")
    private Meeting meeting;

    private long memberId;
    @ToOne(joinProperty = "memberId")
    private Member member;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 1424660522)
    private transient ParticipationDao myDao;
    @Generated(hash = 1311574920)
    private transient Long meeting__resolvedKey;
    @Generated(hash = 336831450)
    private transient Long member__resolvedKey;

    @Keep
    public Participation() {
        this.participationId = ((SmartModerationApplicationImpl)SmartModerationApplicationImpl.getApp()).getUniqueId();
    }

    @Generated(hash = 1143709538)
    public Participation(Long participationId, int contributions, long time, boolean isInListOfSpeakers,
            int number, boolean isSpeaking, long startTime, long meetingId, long memberId) {
        this.participationId = participationId;
        this.contributions = contributions;
        this.time = time;
        this.isInListOfSpeakers = isInListOfSpeakers;
        this.number = number;
        this.isSpeaking = isSpeaking;
        this.startTime = startTime;
        this.meetingId = meetingId;
        this.memberId = memberId;
    }

    public Long getParticipationId() {
        return this.participationId;
    }

    public void setParticipationId(Long participationId) {
        this.participationId = participationId;
    }

    public int getContributions() {
        return this.contributions;
    }

    public void setContributions(int contributions) {
        this.contributions = contributions;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean getIsInListOfSpeakers() {
        return this.isInListOfSpeakers;
    }

    public void setIsInListOfSpeakers(boolean isInListOfSpeakers) {
        this.isInListOfSpeakers = isInListOfSpeakers;
    }

    public int getNumber() {
        return this.number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public boolean getIsSpeaking() {
        return this.isSpeaking;
    }

    public void setIsSpeaking(boolean isSpeaking) {
        this.isSpeaking = isSpeaking;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getMeetingId() {
        return this.meetingId;
    }

    public void setMeetingId(long meetingId) {
        this.meetingId = meetingId;
    }

    public long getMemberId() {
        return this.memberId;
    }

    public void setMemberId(long memberId) {
        this.memberId = memberId;
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

    /** To-one relationship, resolved on first access. */
    @Generated(hash = 993793424)
    public Member getMember() {
        long __key = this.memberId;
        if (member__resolvedKey == null || !member__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            MemberDao targetDao = daoSession.getMemberDao();
            Member memberNew = targetDao.load(__key);
            synchronized (this) {
                member = memberNew;
                member__resolvedKey = __key;
            }
        }
        return member;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 307846971)
    public void setMember(@NotNull Member member) {
        if (member == null) {
            throw new DaoException(
                    "To-one property 'memberId' has not-null constraint; cannot set to-one to null");
        }
        synchronized (this) {
            this.member = member;
            memberId = member.getMemberId();
            member__resolvedKey = memberId;
        }
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

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 796080012)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getParticipationDao() : null;
    }
}
