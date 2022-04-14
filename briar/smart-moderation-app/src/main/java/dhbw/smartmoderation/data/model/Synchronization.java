package dhbw.smartmoderation.data.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;

import dhbw.smartmoderation.SmartModerationApplicationImpl;

@Entity
public class Synchronization {

    @Id
    private long id;
    private long lastSynchronizedGroupMessageTimestamp;
    private long groupId;

    @Keep
    public Synchronization() {
        this.id = ((SmartModerationApplicationImpl)SmartModerationApplicationImpl.getApp()).getUniqueId();
    }

    @Generated(hash = 295776310)
    public Synchronization(long id, long lastSynchronizedGroupMessageTimestamp, long groupId) {
        this.id = id;
        this.lastSynchronizedGroupMessageTimestamp = lastSynchronizedGroupMessageTimestamp;
        this.groupId = groupId;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getLastSynchronizedGroupMessageTimestamp() {
        return this.lastSynchronizedGroupMessageTimestamp;
    }

    public void setLastSynchronizedGroupMessageTimestamp(long lastSynchronizedGroupMessageTimestamp) {
        this.lastSynchronizedGroupMessageTimestamp = lastSynchronizedGroupMessageTimestamp;
    }

    public long getGroupId() {
        return this.groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }
}
