package dhbw.smartmoderation.data.model;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.JoinEntity;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.ToMany;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.annotation.Transient;

import java.util.ArrayList;
import java.util.List;

import dhbw.smartmoderation.SmartModerationApplication;
import dhbw.smartmoderation.SmartModerationApplicationImpl;
import dhbw.smartmoderation.data.events.GroupUpdateEvent;
import dhbw.smartmoderation.util.Util;

@Entity
public class Group extends ModelClass implements  GroupUpdateObserver{

	@Id
	private Long groupId;
	private String name;

	@Transient
	private boolean hasBeenUpdated = false;

	private long settingsId;
	@ToOne(joinProperty = "settingsId")
	private GroupSettings groupSettings;

	@ToMany(referencedJoinProperty = "groupId")
	private List<Meeting> meetings;

	@ToMany
	@JoinEntity(entity = MemberGroupRelation.class, sourceProperty = "groupId", targetProperty = "memberId")
	private List<Member> members;
	/** Used to resolve relations */
	@Generated(hash = 2040040024)
	private transient DaoSession daoSession;
	/** Used for active entity operations. */
	@Generated(hash = 1591306109)
	private transient GroupDao myDao;
	@Generated(hash = 1158618483)
	private transient Long groupSettings__resolvedKey;

	public Group(PrivateGroup group) {

		this.groupId = group.getId();
	}

	public Group(org.briarproject.briar.api.privategroup.PrivateGroup privateGroup) {

		this.groupId = Util.bytesToLong(privateGroup.getId().getBytes());
	}

	public Group(Long groupId) {

		this.groupId = groupId;
	}

	@Keep
	public Group() {

		this.groupId = ((SmartModerationApplicationImpl) SmartModerationApplicationImpl.getApp()).getUniqueId();
	}

	@Generated(hash = 509012919)
	public Group(Long groupId, String name, long settingsId) {
		this.groupId = groupId;
		this.name = name;
		this.settingsId = settingsId;
	}

	public Long getGroupId() {
		return this.groupId;
	}

	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getSettingsId() {
		return this.settingsId;
	}

	public void setSettingsId(long settingsId) {
		this.settingsId = settingsId;
	}

	public MemberGroupRelation addMember(Member member, Role role) {

		DaoSession daoSession = ((SmartModerationApplicationImpl)SmartModerationApplicationImpl.getApp()).getDaoSession();
		MemberGroupRelationDao memberGroupRelationDao = daoSession.getMemberGroupRelationDao();

		for(MemberGroupRelation memberGroupRelation : memberGroupRelationDao.loadAll()) {

			if(this.groupId.equals(memberGroupRelation.getGroupId()) && member.getMemberId().equals(memberGroupRelation.getMemberId()) && role.name().equals(memberGroupRelation.getRole())) {

				return null;
			}

		}

		MemberGroupRelation relation = new MemberGroupRelation();
		relation.setMemberId(member.getMemberId());
		relation.setGroupId(groupId);
		relation.setRole(role.name());
		return relation;
	}

	public void removeMember(Member member, Role role) {

		DaoSession daoSession = ((SmartModerationApplicationImpl)SmartModerationApplicationImpl.getApp()).getDaoSession();
		MemberGroupRelationDao memberGroupRelationDao = daoSession.getMemberGroupRelationDao();

		for(MemberGroupRelation memberGroupRelation : memberGroupRelationDao.loadAll()) {

			if(this.groupId.equals(memberGroupRelation.getGroupId()) && member.getMemberId().equals(memberGroupRelation.getMemberId()) && role.name().equals(memberGroupRelation.getRole())) {

				memberGroupRelationDao.delete(memberGroupRelation);
			}
		}
	}

	public void removeMember(Member member) {

		DaoSession daoSession = ((SmartModerationApplicationImpl)SmartModerationApplicationImpl.getApp()).getDaoSession();
		MemberGroupRelationDao memberGroupRelationDao = daoSession.getMemberGroupRelationDao();

		for(MemberGroupRelation memberGroupRelation : memberGroupRelationDao.loadAll()) {

			if(this.groupId.equals(memberGroupRelation.getGroupId()) && member.getMemberId().equals(memberGroupRelation.getMemberId())) {

				memberGroupRelationDao.delete(memberGroupRelation);
			}
		}
	}

	public Member getMember(Long memberId) {

		for(Member member : getMembers()) {

			if(member.getMemberId().equals(memberId)) {

				return member;
			}
		}

		return null;
	}

	public List<Member> getUniqueMembers() {
		List<Member> members = new ArrayList<>();

		for (Member member : getMembers()) {

			if (!members.contains(member)) {
				members.add(member);
			}
		}

		return members;
	}

	public List<Member> getVoteMembers() {

		List<Member> voteMembers = new ArrayList<>();

		for(Member member : getUniqueMembers()) {

			if(member.hasPermissionToVote(this)) {

				voteMembers.add(member);
			}
		}

		return voteMembers;
	}

	public Meeting getMeeting(Long meetingId) {

		for(Meeting meeting : getMeetings()) {

			if(meeting.getMeetingId().equals(meetingId)) {

				return meeting;
			}
		}

		return null;
	}

	public boolean hasBeenUpdated(){
		return hasBeenUpdated;
	}

	public void updateChecked(){

		this.hasBeenUpdated = false;
	}

	@Override
	public void update(GroupUpdateEvent event) {

		if (getGroupId().equals(event.getId())){

			this.hasBeenUpdated = true;
		}

	}

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
	@Generated(hash = 778928232)
	public List<Meeting> getMeetings() {
		if (meetings == null) {
			final DaoSession daoSession = this.daoSession;
			if (daoSession == null) {
				throw new DaoException("Entity is detached from DAO context");
			}
			MeetingDao targetDao = daoSession.getMeetingDao();
			List<Meeting> meetingsNew = targetDao._queryGroup_Meetings(groupId);
			synchronized (this) {
				if (meetings == null) {
					meetings = meetingsNew;
				}
			}
		}
		return meetings;
	}

	/** Resets a to-many relationship, making the next get call to query for a fresh result. */
	@Generated(hash = 495885775)
	public synchronized void resetMeetings() {
		meetings = null;
	}

	/**
	 * To-many relationship, resolved on first access (and after reset).
	 * Changes to to-many relations are not persisted, make changes to the target entity.
	 */
	@Generated(hash = 401768219)
	public List<Member> getMembers() {
		if (members == null) {
			final DaoSession daoSession = this.daoSession;
			if (daoSession == null) {
				throw new DaoException("Entity is detached from DAO context");
			}
			MemberDao targetDao = daoSession.getMemberDao();
			List<Member> membersNew = targetDao._queryGroup_Members(groupId);
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
	@Generated(hash = 1333602095)
	public void __setDaoSession(DaoSession daoSession) {
		this.daoSession = daoSession;
		myDao = daoSession != null ? daoSession.getGroupDao() : null;
	}

}
