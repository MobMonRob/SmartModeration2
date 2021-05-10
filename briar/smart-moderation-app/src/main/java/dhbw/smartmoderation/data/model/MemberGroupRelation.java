package dhbw.smartmoderation.data.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;

import dhbw.smartmoderation.SmartModerationApplication;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class MemberGroupRelation {

    @Id
    private Long relationId;
    private Long groupId;
    private Long memberId;
    private String role;

    @Keep
    public MemberGroupRelation() {

        this.relationId = ((SmartModerationApplication)SmartModerationApplication.getApp()).getUniqueId();
    }

    @Generated(hash = 785262835)
    public MemberGroupRelation(Long relationId, Long groupId, Long memberId, String role) {
        this.relationId = relationId;
        this.groupId = groupId;
        this.memberId = memberId;
        this.role = role;
    }

    public Long getRelationId() {
        return this.relationId;
    }

    public void setRelationId(Long relationId) {
        this.relationId = relationId;
    }

    public Long getGroupId() {
        return this.groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getMemberId() {
        return this.memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getRole() {
        return this.role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
