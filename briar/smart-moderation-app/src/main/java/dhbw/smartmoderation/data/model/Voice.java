package dhbw.smartmoderation.data.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.NotNull;

import dhbw.smartmoderation.SmartModerationApplication;

@Entity
public class Voice extends ModelClass {

    @Id
    private Long voiceId;
    private String explanation;

    private long consensusLevelId;
    @ToOne(joinProperty = "consensusLevelId")
    private ConsensusLevel consensusLevel;

    private long pollId;
    @ToOne(joinProperty = "pollId")
    private Poll poll;

    private long memberId;
    @ToOne(joinProperty = "memberId")
    private Member member;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 207264020)
    private transient VoiceDao myDao;
    @Generated(hash = 948260480)
    public Voice(Long voiceId, String explanation, long consensusLevelId,
            long pollId, long memberId) {
        this.voiceId = voiceId;
        this.explanation = explanation;
        this.consensusLevelId = consensusLevelId;
        this.pollId = pollId;
        this.memberId = memberId;
    }
    @Keep
    public Voice() {

        this.voiceId = ((SmartModerationApplication)SmartModerationApplication.getApp()).getUniqueId();
    }

    public Long getVoiceId() {
        return this.voiceId;
    }
    public void setVoiceId(Long voiceId) {
        this.voiceId = voiceId;
    }
    public String getExplanation() {
        return this.explanation;
    }
    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
    public long getConsensusLevelId() {
        return this.consensusLevelId;
    }
    public void setConsensusLevelId(long consensusLevelId) {
        this.consensusLevelId = consensusLevelId;
    }
    public long getPollId() {
        return this.pollId;
    }
    public void setPollId(long pollId) {
        this.pollId = pollId;
    }
    public long getMemberId() {
        return this.memberId;
    }
    public void setMemberId(long memberId) {
        this.memberId = memberId;
    }
    @Generated(hash = 1129072320)
    private transient Long consensusLevel__resolvedKey;
    /** To-one relationship, resolved on first access. */
    @Generated(hash = 1352950148)
    public ConsensusLevel getConsensusLevel() {
        long __key = this.consensusLevelId;
        if (consensusLevel__resolvedKey == null
                || !consensusLevel__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            ConsensusLevelDao targetDao = daoSession.getConsensusLevelDao();
            ConsensusLevel consensusLevelNew = targetDao.load(__key);
            synchronized (this) {
                consensusLevel = consensusLevelNew;
                consensusLevel__resolvedKey = __key;
            }
        }
        return consensusLevel;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1589880721)
    public void setConsensusLevel(@NotNull ConsensusLevel consensusLevel) {
        if (consensusLevel == null) {
            throw new DaoException(
                    "To-one property 'consensusLevelId' has not-null constraint; cannot set to-one to null");
        }
        synchronized (this) {
            this.consensusLevel = consensusLevel;
            consensusLevelId = consensusLevel.getConsensusLevelId();
            consensusLevel__resolvedKey = consensusLevelId;
        }
    }
    @Generated(hash = 856499023)
    private transient Long poll__resolvedKey;
    /** To-one relationship, resolved on first access. */
    @Generated(hash = 452623753)
    public Poll getPoll() {
        long __key = this.pollId;
        if (poll__resolvedKey == null || !poll__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            PollDao targetDao = daoSession.getPollDao();
            Poll pollNew = targetDao.load(__key);
            synchronized (this) {
                poll = pollNew;
                poll__resolvedKey = __key;
            }
        }
        return poll;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1448390100)
    public void setPoll(@NotNull Poll poll) {
        if (poll == null) {
            throw new DaoException(
                    "To-one property 'pollId' has not-null constraint; cannot set to-one to null");
        }
        synchronized (this) {
            this.poll = poll;
            pollId = poll.getPollId();
            poll__resolvedKey = pollId;
        }
    }
    @Generated(hash = 336831450)
    private transient Long member__resolvedKey;
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
    @Generated(hash = 987314460)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getVoiceDao() : null;
    }


}