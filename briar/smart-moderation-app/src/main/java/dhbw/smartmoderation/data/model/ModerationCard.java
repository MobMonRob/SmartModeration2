package dhbw.smartmoderation.data.model;

import android.graphics.Color;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.ToOne;

import dhbw.smartmoderation.SmartModerationApplication;
import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.NotNull;

@Entity
public class ModerationCard extends ModelClass{

    @Id
    private Long cardId;
    private String content;
    private int color;

    private long meetingId;
    @ToOne(joinProperty = "meetingId")
    private Meeting meeting;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 199570951)
    private transient ModerationCardDao myDao;
    @Generated(hash = 1311574920)
    private transient Long meeting__resolvedKey;

    @Generated(hash = 116460095)
    public ModerationCard(Long cardId, String content, int color, long meetingId) {
        this.cardId = cardId;
        this.content = content;
        this.color = color;
        this.meetingId = meetingId;
    }
    @Keep
    public ModerationCard() {
        cardId = ((SmartModerationApplication) SmartModerationApplication.getApp()).getUniqueId();
    }
    public Long getCardId() {
        return this.cardId;
    }
    public void setCardId(Long cardId) {
        this.cardId = cardId;
    }
    public String getContent() {
        return this.content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public int getColor() {
        return this.color;
    }
    public void setColor(int color) {
        this.color = color;
    }
    public long getMeetingId() {
        return this.meetingId;
    }
    public void setMeetingId(long meetingId) {
        this.meetingId = meetingId;
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
    @Generated(hash = 1988012154)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getModerationCardDao() : null;
    }
}
