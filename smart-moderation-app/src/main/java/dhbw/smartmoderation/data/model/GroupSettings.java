package dhbw.smartmoderation.data.model;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.ToMany;

import java.util.List;

import dhbw.smartmoderation.SmartModerationApplicationImpl;


@Entity
public class GroupSettings extends ModelClass {

    @Id
    private Long settingsId;

    @ToMany(referencedJoinProperty = "settingsId")
    private List<ConsensusLevel> consensusLevels;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 1425492271)
    private transient GroupSettingsDao myDao;

    @Keep
    public GroupSettings() {

        this.settingsId = ((SmartModerationApplicationImpl)SmartModerationApplicationImpl.getApp()).getUniqueId();
    }

    @Generated(hash = 2142486419)
    public GroupSettings(Long settingsId) {
        this.settingsId = settingsId;
    }

    public Long getSettingsId() {
        return this.settingsId;
    }

    public void setSettingsId(Long settingsId) {
        this.settingsId = settingsId;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 1482736513)
    public List<ConsensusLevel> getConsensusLevels() {
        if (consensusLevels == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            ConsensusLevelDao targetDao = daoSession.getConsensusLevelDao();
            List<ConsensusLevel> consensusLevelsNew = targetDao._queryGroupSettings_ConsensusLevels(settingsId);
            synchronized (this) {
                if (consensusLevels == null) {
                    consensusLevels = consensusLevelsNew;
                }
            }
        }
        return consensusLevels;
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

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 848739283)
    public synchronized void resetConsensusLevels() {
        consensusLevels = null;
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

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 631944638)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getGroupSettingsDao() : null;
    }


}

