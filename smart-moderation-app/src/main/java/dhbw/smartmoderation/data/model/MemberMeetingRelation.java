package dhbw.smartmoderation.data.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;

import dhbw.smartmoderation.SmartModerationApplicationImpl;

@Entity
public class MemberMeetingRelation {

    @Id
    private Long relationId;
    private Long meetingId;
    private Long memberId;
    private String attendance;

    @Keep
    public MemberMeetingRelation() {

        this.relationId = ((SmartModerationApplicationImpl)SmartModerationApplicationImpl.getApp()).getUniqueId();
    }

    @Generated(hash = 1348046334)
    public MemberMeetingRelation(Long relationId, Long meetingId, Long memberId, String attendance) {
        this.relationId = relationId;
        this.meetingId = meetingId;
        this.memberId = memberId;
        this.attendance = attendance;
    }

    public Long getRelationId() {
        return this.relationId;
    }

    public void setRelationId(Long relationId) {
        this.relationId = relationId;
    }

    public Long getMeetingId() {
        return this.meetingId;
    }

    public void setMeetingId(Long meetingId) {
        this.meetingId = meetingId;
    }

    public Long getMemberId() {
        return this.memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getAttendance() {
        return this.attendance;
    }

    public void setAttendance(String attendance) {
        this.attendance = attendance;
    }

}
