package dhbw.smartmoderation.data.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.ToOne;
import dhbw.smartmoderation.SmartModerationApplication;
import dhbw.smartmoderation.SmartModerationApplicationImpl;

import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.NotNull;

@Entity
public class Topic extends ModelClass{

    @Id
    private Long topicId;
    private String title;
    private long duration;
    private String status;

    private long meetingId;
    @ToOne(joinProperty = "meetingId")
    private Meeting meeting;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 694021448)
    private transient TopicDao myDao;
    @Generated(hash = 1311574920)
    private transient Long meeting__resolvedKey;

    @Keep
    public Topic() {

        this.topicId = ((SmartModerationApplicationImpl)SmartModerationApplicationImpl.getApp()).getUniqueId();
    }

    @Generated(hash = 1480016878)
    public Topic(Long topicId, String title, long duration, String status, long meetingId) {
        this.topicId = topicId;
        this.title = title;
        this.duration = duration;
        this.status = status;
        this.meetingId = meetingId;
    }

    public Long getTopicId() {
        return this.topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getDuration() {
        return this.duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getMeetingId() {
        return this.meetingId;
    }

    public void setMeetingId(long meetingId) {
        this.meetingId = meetingId;
    }

    public TopicStatus getTopicStatus() {

        try {

            return TopicStatus.valueOf(status);

        } catch (IllegalArgumentException ex) {

            return null;
        }
    }

    public void setTopicStatus(TopicStatus status) {

        this.status = status.name();
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
    @Generated(hash = 1373867845)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getTopicDao() : null;
    }

}

