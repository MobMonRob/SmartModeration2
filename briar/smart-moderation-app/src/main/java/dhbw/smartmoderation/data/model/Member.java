package dhbw.smartmoderation.data.model;

import org.briarproject.bramble.api.identity.Author;
import org.briarproject.bramble.api.identity.AuthorId;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.JoinEntity;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.ToMany;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import dhbw.smartmoderation.SmartModerationApplication;
import dhbw.smartmoderation.util.Util;

import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

@Entity
public class Member extends ModelClass {

	@Id
	private Long memberId;
	private String name;
	private boolean isGhost;

	@ToMany(referencedJoinProperty = "memberId")
	private List<Participation> participations;

	@ToMany(referencedJoinProperty = "memberId")
	private List<Voice> voices;

	@ToMany
	@JoinEntity(entity = MemberGroupRelation.class, sourceProperty = "memberId", targetProperty = "groupId")
	private List<Group> groups;

	@ToMany
	@JoinEntity(entity = MemberMeetingRelation.class, sourceProperty = "memberId", targetProperty = "meetingId")
	private List<Meeting> meetings;

	/** Used to resolve relations */
	@Generated(hash = 2040040024)
	private transient DaoSession daoSession;
	/** Used for active entity operations. */
	@Generated(hash = 1200613910)
	private transient MemberDao myDao;

	@Keep
	public Member() {

		this.memberId = ((SmartModerationApplication)SmartModerationApplication.getApp()).getUniqueId();
	}

	public Member(IContact contact){

		this.memberId  = contact.getId();

		if(contact instanceof Ghost) {

			isGhost = true;
			name  =((Ghost)contact).getName();
		}
	}

   public Member(Author author) {

		this.memberId = Util.bytesToLong(author.getId().getBytes());

    }

    public Member(AuthorId authorId) {

		this.memberId = Util.bytesToLong(authorId.getBytes());

    }

	public Member(Long memberId) {

		this.memberId = memberId;
	}

	@Generated(hash = 114445602)
	public Member(Long memberId, String name, boolean isGhost) {
		this.memberId = memberId;
		this.name = name;
		this.isGhost = isGhost;
	}

	public Long getMemberId() {
		return this.memberId;
	}

	public void setMemberId(Long memberId) {
		this.memberId = memberId;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean getIsGhost() {
		return this.isGhost;
	}

	public void setIsGhost(boolean isGhost) {
		this.isGhost = isGhost;
	}

	public Attendance getAttendance(Meeting meeting) {

		DaoSession daoSession = ((SmartModerationApplication)SmartModerationApplication.getApp()).getDaoSession();
		MemberMeetingRelationDao memberMeetingRelationDao = daoSession.getMemberMeetingRelationDao();

		for(MemberMeetingRelation memberMeetingRelation : memberMeetingRelationDao.loadAll()) {

			if(this.memberId.equals(memberMeetingRelation.getMemberId()) && meeting.getMeetingId().equals(memberMeetingRelation.getMeetingId())) {

				return Attendance.valueOf(memberMeetingRelation.getAttendance());
			}
		}

		return null;
	}

	public MemberMeetingRelation setAttendance(Meeting meeting, Attendance attendance) {

		DaoSession daoSession = ((SmartModerationApplication)SmartModerationApplication.getApp()).getDaoSession();
		MemberMeetingRelationDao memberMeetingRelationDao = daoSession.getMemberMeetingRelationDao();

		for(MemberMeetingRelation memberMeetingRelation : memberMeetingRelationDao.loadAll()) {

			if(this.memberId.equals(memberMeetingRelation.getMemberId()) && meeting.getMeetingId().equals(memberMeetingRelation.getMeetingId())) {

				memberMeetingRelation.setAttendance(attendance.name());
				return memberMeetingRelation;
			}
		}

		return null;

	}

	public Collection<Role> getRoles(Group group) {

		Collection<Role> roles = new ArrayList<>();


		DaoSession daoSession = ((SmartModerationApplication)SmartModerationApplication.getApp()).getDaoSession();
		MemberGroupRelationDao memberGroupRelationDao = daoSession.getMemberGroupRelationDao();

		for (MemberGroupRelation memberGroupRelation : memberGroupRelationDao.loadAll()) {

			if(this.memberId.equals(memberGroupRelation.getMemberId()) && group.getGroupId().equals(memberGroupRelation.getGroupId())) {
				roles.add(Role.valueOf(memberGroupRelation.getRole()));

			}
		}

		return roles;

	}

	public boolean hasPermissionToVote(Group group) {

		Collection<Role> roles = getRoles(group);

		if (roles.contains(Role.SPECTATOR)) {
			return false;
		}

		return true;
	}

	public boolean isOnline() {
		return true;
		//TODO Implementieren
	}

	/**
	 * To-many relationship, resolved on first access (and after reset).
	 * Changes to to-many relations are not persisted, make changes to the target entity.
	 */
	@Generated(hash = 1203162996)
	public List<Participation> getParticipations() {
		if (participations == null) {
			final DaoSession daoSession = this.daoSession;
			if (daoSession == null) {
				throw new DaoException("Entity is detached from DAO context");
			}
			ParticipationDao targetDao = daoSession.getParticipationDao();
			List<Participation> participationsNew = targetDao._queryMember_Participations(memberId);
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
	@Generated(hash = 1764548603)
	public List<Voice> getVoices() {
		if (voices == null) {
			final DaoSession daoSession = this.daoSession;
			if (daoSession == null) {
				throw new DaoException("Entity is detached from DAO context");
			}
			VoiceDao targetDao = daoSession.getVoiceDao();
			List<Voice> voicesNew = targetDao._queryMember_Voices(memberId);
			synchronized (this) {
				if (voices == null) {
					voices = voicesNew;
				}
			}
		}
		return voices;
	}

	/** Resets a to-many relationship, making the next get call to query for a fresh result. */
	@Generated(hash = 237630216)
	public synchronized void resetVoices() {
		voices = null;
	}

	/**
	 * To-many relationship, resolved on first access (and after reset).
	 * Changes to to-many relations are not persisted, make changes to the target entity.
	 */
	@Generated(hash = 1422972704)
	public List<Group> getGroups() {
		if (groups == null) {
			final DaoSession daoSession = this.daoSession;
			if (daoSession == null) {
				throw new DaoException("Entity is detached from DAO context");
			}
			GroupDao targetDao = daoSession.getGroupDao();
			List<Group> groupsNew = targetDao._queryMember_Groups(memberId);
			synchronized (this) {
				if (groups == null) {
					groups = groupsNew;
				}
			}
		}
		return groups;
	}

	/** Resets a to-many relationship, making the next get call to query for a fresh result. */
	@Generated(hash = 464128061)
	public synchronized void resetGroups() {
		groups = null;
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

	/**
	 * To-many relationship, resolved on first access (and after reset).
	 * Changes to to-many relations are not persisted, make changes to the target entity.
	 */
	@Generated(hash = 876496915)
	public List<Meeting> getMeetings() {
		if (meetings == null) {
			final DaoSession daoSession = this.daoSession;
			if (daoSession == null) {
				throw new DaoException("Entity is detached from DAO context");
			}
			MeetingDao targetDao = daoSession.getMeetingDao();
			List<Meeting> meetingsNew = targetDao._queryMember_Meetings(memberId);
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

	/** called by internal mechanisms, do not call yourself. */
	@Generated(hash = 1742104579)
	public void __setDaoSession(DaoSession daoSession) {
		this.daoSession = daoSession;
		myDao = daoSession != null ? daoSession.getMemberDao() : null;
	}

}
