package dhbw.smartmoderation.data.model;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.ToMany;
import org.greenrobot.greendao.annotation.ToOne;

import java.util.List;

import dhbw.smartmoderation.SmartModerationApplicationImpl;

@Entity
public class ConsensusLevel extends ModelClass {

    @Id
    private Long consensusLevelId;
    private String name;
    private String description;
    private int number;
    private int color;

    private long settingsId;
    @ToOne(joinProperty = "settingsId")
    private GroupSettings groupSettings;

    @ToMany(referencedJoinProperty = "consensusLevelId")
    private List<Voice> voices;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 1812013018)
    private transient ConsensusLevelDao myDao;

    @Generated(hash = 675369423)
    public ConsensusLevel(Long consensusLevelId, String name, String description, int number, int color, long settingsId) {
        this.consensusLevelId = consensusLevelId;
        this.name = name;
        this.description = description;
        this.number = number;
        this.color = color;
        this.settingsId = settingsId;
    }

    @Keep
    public ConsensusLevel() {
        this.consensusLevelId = ((SmartModerationApplicationImpl)SmartModerationApplicationImpl.getApp()).getUniqueId();
    }

    public Long getConsensusLevelId() {
        return this.consensusLevelId;
    }

    public void setConsensusLevelId(Long consensusLevelId) {
        this.consensusLevelId = consensusLevelId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getNumber() {
        return this.number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getColor() {
        return this.color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public long getSettingsId() {
        return this.settingsId;
    }

    public void setSettingsId(long settingsId) {
        this.settingsId = settingsId;
    }

    @Generated(hash = 1158618483)
    private transient Long groupSettings__resolvedKey;

    /** To-one relationship, resolved on first access. */
    @Generated(hash = 976034199)
    public GroupSettings getGroupSettings() {
        long __key = this.settingsId;
        if (groupSettings__resolvedKey == null || !groupSettings__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            GroupSettingsDao targetDao = daoSession.getGroupSettingsDao();
            GroupSettings groupSettingsNew = targetDao.load(__key);
            synchronized (this) {
                groupSettings = groupSettingsNew;
                groupSettings__resolvedKey = __key;
            }
        }
        return groupSettings;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1186862619)
    public void setGroupSettings(@NotNull GroupSettings groupSettings) {
        if (groupSettings == null) {
            throw new DaoException("To-one property 'settingsId' has not-null constraint; cannot set to-one to null");
        }
        synchronized (this) {
            this.groupSettings = groupSettings;
            settingsId = groupSettings.getSettingsId();
            groupSettings__resolvedKey = settingsId;
        }
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 1323299465)
    public List<Voice> getVoices() {
        if (voices == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            VoiceDao targetDao = daoSession.getVoiceDao();
            List<Voice> voicesNew = targetDao._queryConsensusLevel_Voices(consensusLevelId);
            synchronized (this) {
                if (voices == null) {
                    voices = voicesNew;
                }
            }
        }
        return voices;
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
    @Generated(hash = 237630216)
    public synchronized void resetVoices() {
        voices = null;
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
    @Generated(hash = 1002345223)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getConsensusLevelDao() : null;
    }
}
