package dhbw.smartmoderation.data.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.JoinEntity;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.ToMany;
import org.greenrobot.greendao.annotation.ToOne;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import dhbw.smartmoderation.SmartModerationApplication;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.NotNull;

@Entity
public class Meeting extends ModelClass {
	@Id
	private Long meetingId;
	private long startTime;
	private long endTime;
	private long date;
	private String cause;
	private String location;
	private boolean online;
	private boolean open;

	private long groupId;
	@ToOne(joinProperty = "groupId")
	private Group group;

	@ToMany(referencedJoinProperty = "meetingId")
	private List<Poll> polls;

	@ToMany(referencedJoinProperty = "meetingId")
	private List<Topic> topics;

	@ToMany(referencedJoinProperty = "meetingId")
	private List<Participation> participations;

	@ToMany
	@JoinEntity(entity = MemberMeetingRelation.class, sourceProperty = "meetingId", targetProperty = "memberId")
	private List<Member> members;
	/** Used to resolve relations */
	@Generated(hash = 2040040024)
	private transient DaoSession daoSession;
	/** Used for active entity operations. */
	@Generated(hash = 1797444500)
	private transient MeetingDao myDao;
	@Generated(hash = 201187923)
	private transient Long group__resolvedKey;

	@Keep
	public Meeting() {

		this.meetingId = ((SmartModerationApplication)SmartModerationApplication.getApp()).getUniqueId();
	}

	@Generated(hash = 1628552253)
	public Meeting(Long meetingId, long startTime, long endTime, long date, String cause, String location,
			boolean online, boolean open, long groupId) {
		this.meetingId = meetingId;
		this.startTime = startTime;
		this.endTime = endTime;
		this.date = date;
		this.cause = cause;
		this.location = location;
		this.online = online;
		this.open = open;
		this.groupId = groupId;
	}

	public Long getMeetingId() {
		return this.meetingId;
	}

	public void setMeetingId(Long meetingId) {
		this.meetingId = meetingId;
	}

	public long getStartTime() {
		return this.startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return this.endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public long getDate() {
		return this.date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public String getDateAsString() {

		Date date = new Date(this.date);
		Format format = new SimpleDateFormat("dd.MM.yyyy");
		return format.format(date);
	}

	public String getCause() {
		return this.cause;
	}

	public void setCause(String cause) {
		this.cause = cause;
	}

	public String getLocation() {
		return this.location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public boolean getOnline() {
		return this.online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}

	public boolean getOpen() {
		return this.open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}

	public long getGroupId() {
		return this.groupId;
	}

	public void setGroupId(long groupId) {
		this.groupId = groupId;
	}

	public long getExpectedEndTime() {

		long time = getStartTime();

		for(Topic topic : getTopics()) {
			time += topic.getDuration();
		}
		return time;

	}

	public Collection<Member> getPresentVoteMembers() {

		Collection<Long> voteMemberIds = new ArrayList<>();

		for(Member voteMember : getGroup().getVoteMembers()) {
			voteMemberIds.add(voteMember.getMemberId());
		}

		Collection<Member> presentMembers = new ArrayList<>();

		for(Member member : getMembers()) {

			if(voteMemberIds.contains(member.getMemberId()) && member.getAttendance(this) == Attendance.PRESENT) {
				presentMembers.add(member);
			}
		}

		return presentMembers;
	}

	public MemberMeetingRelation addMember(Member member) {

		DaoSession daoSession = ((SmartModerationApplication)SmartModerationApplication.getApp()).getDaoSession();
		MemberMeetingRelationDao memberMeetingRelationDao = daoSession.getMemberMeetingRelationDao();

		for(MemberMeetingRelation memberMeetingRelation : memberMeetingRelationDao.loadAll()) {

			if(this.meetingId.equals(memberMeetingRelation.getMeetingId()) && member.getMemberId().equals(memberMeetingRelation.getMemberId())) {

				return null;
			}

		}

		MemberMeetingRelation relation = new MemberMeetingRelation();
		relation.setMemberId(member.getMemberId());
		relation.setMeetingId(meetingId);
		relation.setAttendance(Attendance.ABSENT.name());
		return relation;
	}

	public MemberMeetingRelation addMember(Member member, Attendance attendance) {

		DaoSession daoSession = ((SmartModerationApplication)SmartModerationApplication.getApp()).getDaoSession();
		MemberMeetingRelationDao memberMeetingRelationDao = daoSession.getMemberMeetingRelationDao();

		for(MemberMeetingRelation memberMeetingRelation : memberMeetingRelationDao.loadAll()) {

			if(this.meetingId.equals(memberMeetingRelation.getMeetingId()) && member.getMemberId().equals(memberMeetingRelation.getMemberId())) {

				return null;
			}

		}

		MemberMeetingRelation relation = new MemberMeetingRelation();
		relation.setMemberId(member.getMemberId());
		relation.setMeetingId(meetingId);
		relation.setAttendance(attendance.name());
		return relation;
	}

	public void removeMember(Member member) {

		DaoSession daoSession = ((SmartModerationApplication)SmartModerationApplication.getApp()).getDaoSession();
		MemberMeetingRelationDao memberMeetingRelationDao = daoSession.getMemberMeetingRelationDao();

		for(MemberMeetingRelation memberMeetingRelation : memberMeetingRelationDao.loadAll()) {

			if(this.meetingId.equals(memberMeetingRelation.getMeetingId()) && member.getMemberId().equals(memberMeetingRelation.getMemberId())) {

				memberMeetingRelationDao.delete(memberMeetingRelation);
			}
		}
	}

	public Poll getPoll(Long pollId) {

		for(Poll poll : getPolls()) {

			if(poll.getPollId().equals(pollId)) {

				return poll;
			}
		}

		return null;
	}

	/** To-one relationship, resolved on first access. */
	@Generated(hash = 1458728405)
	public Group getGroup() {
		long __key = this.groupId;
		if (group__resolvedKey == null || !group__resolvedKey.equals(__key)) {
			final DaoSession daoSession = this.daoSession;
			if (daoSession == null) {
				throw new DaoException("Entity is detached from DAO context");
			}
			GroupDao targetDao = daoSession.getGroupDao();
			Group groupNew = targetDao.load(__key);
			synchronized (this) {
				group = groupNew;
				group__resolvedKey = __key;
			}
		}
		return group;
	}

	/** called by internal mechanisms, do not call yourself. */
	@Generated(hash = 2061332765)
	public void setGroup(@NotNull Group group) {
		if (group == null) {
			throw new DaoException("To-one property 'groupId' has not-null constraint; cannot set to-one to null");
		}
		synchronized (this) {
			this.group = group;
			groupId = group.getGroupId();
			group__resolvedKey = groupId;
		}
	}

	/**
	 * To-many relationship, resolved on first access (and after reset).
	 * Changes to to-many relations are not persisted, make changes to the target entity.
	 */
	@Generated(hash = 1098782872)
	public List<Poll> getPolls() {
		if (polls == null) {
			final DaoSession daoSession = this.daoSession;
			if (daoSession == null) {
				throw new DaoException("Entity is detached from DAO context");
			}
			PollDao targetDao = daoSession.getPollDao();
			List<Poll> pollsNew = targetDao._queryMeeting_Polls(meetingId);
			synchronized (this) {
				if (polls == null) {
					polls = pollsNew;
				}
			}
		}
		return polls;
	}

	/** Resets a to-many relationship, making the next get call to query for a fresh result. */
	@Generated(hash = 928798354)
	public synchronized void resetPolls() {
		polls = null;
	}

	/**
	 * To-many relationship, resolved on first access (and after reset).
	 * Changes to to-many relations are not persisted, make changes to the target entity.
	 */
	@Generated(hash = 1622652586)
	public List<Topic> getTopics() {
		if (topics == null) {
			final DaoSession daoSession = this.daoSession;
			if (daoSession == null) {
				throw new DaoException("Entity is detached from DAO context");
			}
			TopicDao targetDao = daoSession.getTopicDao();
			List<Topic> topicsNew = targetDao._queryMeeting_Topics(meetingId);
			synchronized (this) {
				if (topics == null) {
					topics = topicsNew;
				}
			}
		}
		return topics;
	}

	/** Resets a to-many relationship, making the next get call to query for a fresh result. */
	@Generated(hash = 1067351932)
	public synchronized void resetTopics() {
		topics = null;
	}

	/**
	 * To-many relationship, resolved on first access (and after reset).
	 * Changes to to-many relations are not persisted, make changes to the target entity.
	 */
	@Generated(hash = 1005684202)
	public List<Participation> getParticipations() {
		if (participations == null) {
			final DaoSession daoSession = this.daoSession;
			if (daoSession == null) {
				throw new DaoException("Entity is detached from DAO context");
			}
			ParticipationDao targetDao = daoSession.getParticipationDao();
			List<Participation> participationsNew = targetDao._queryMeeting_Participations(meetingId);
			synchronized (this) {
				if (participations == null) {
					participations = participationsNew;
				}
			}
		}
		return participations;
	}

	/** Resets a to-many relationship, making the next get call to query for a fresh result. */
	@Generated(hash = 2135771202)
	public synchronized void resetParticipations() {
		participations = null;
	}

	/**
	 * To-many relationship, resolved on first access (and after reset).
	 * Changes to to-many relations are not persisted, make changes to the target entity.
	 */
	@Generated(hash = 1747104983)
	public List<Member> getMembers() {
		if (members == null) {
			final DaoSession daoSession = this.daoSession;
			if (daoSession == null) {
				throw new DaoException("Entity is detached from DAO context");
			}
			MemberDao targetDao = daoSession.getMemberDao();
			List<Member> membersNew = targetDao._queryMeeting_Members(meetingId);
			synchronized (this) {
				if (members == null) {
					members = membersNew;
				}
			}
		}
		return members;
	}

	/** Resets a to-many relationship, making the next get call to query for a fresh result. */
	@Generated(hash = 1358688666)
	public synchronized void resetMembers() {
		members = null;
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
	@Generated(hash = 1584316095)
	public void __setDaoSession(DaoSession daoSession) {
		this.daoSession = daoSession;
		myDao = daoSession != null ? daoSession.getMeetingDao() : null;
	}



}