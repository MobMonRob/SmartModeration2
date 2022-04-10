package dhbw.smartmoderation.connection.synchronization;
import org.briarproject.bramble.api.identity.LocalAuthor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import dhbw.smartmoderation.SmartModerationApplication;
import dhbw.smartmoderation.SmartModerationApplicationImpl;
import dhbw.smartmoderation.data.DataService;
import dhbw.smartmoderation.data.DataServiceImpl;
import dhbw.smartmoderation.data.model.*;
import dhbw.smartmoderation.exceptions.GroupNotFoundException;
import dhbw.smartmoderation.exceptions.GroupSettingsNotFoundException;
import dhbw.smartmoderation.exceptions.MeetingNotFoundException;
import dhbw.smartmoderation.exceptions.MemberNotFoundException;
import dhbw.smartmoderation.exceptions.PollNotFoundException;
import dhbw.smartmoderation.util.Util;

public class SerializationServiceImpl implements SerializationService {

	private LocalAuthor localAuthor;

	private DataService dataService = ((SmartModerationApplicationImpl) SmartModerationApplicationImpl.getApp()).getDataService();

	public void setLocalAuthor(LocalAuthor localAuthor) {
		this.localAuthor = localAuthor;
	}

	@Override
	public String serialize(Member member) {
		return jsonify(member).toString();
	}

	@Override
	public String serialize(Meeting meeting) { return jsonify(meeting).toString(); }

	@Override
	public String serialize(Group group) { return jsonify(group).toString(); }

	@Override
	public String serialize(Poll poll) {
		return jsonify(poll).toString();
	}

	@Override
	public String serialize(ModerationCard moderationCard) {
		return jsonify(moderationCard).toString();
	}

	@Override
	public String serialize(Voice voice) { return jsonify(voice).toString(); }

	@Override
	public String serialize(Participation participation) { return jsonify(participation).toString(); }

	@Override
	public String serialize(Topic topic) {
		return jsonify(topic).toString();
	}

	@Override
	public String serialize(ConsensusLevel consensusLevel) { return jsonify(consensusLevel).toString(); }

	@Override
	public String serialize(GroupSettings groupSettings) {
		return jsonify(groupSettings).toString();
	}

	@Override
	public String bulkSerialize(Collection<ModelClass> models) {

		JSONArray payload = new JSONArray();

		for (ModelClass model : models) {

			if (model.getClass().getName().equals(Member.class.getName())) {

				payload.put(jsonify((Member) model));
			}

			else if (model.getClass().getName().equals(Meeting.class.getName())) {

				payload.put(jsonify((Meeting) model));

			}

			else if (model.getClass().getName().equals(Group.class.getName())) {

				payload.put(jsonify((Group) model));
			}

			else if (model.getClass().getName().equals(Poll.class.getName())) {

				payload.put(jsonify((Poll) model));
			}

			else if (model.getClass().getName().equals(ModerationCard.class.getName())) {

				payload.put(jsonify((ModerationCard) model));
			}

			else if (model.getClass().getName().equals(Voice.class.getName())) {

				payload.put(jsonify((Voice) model));
			}

			else if (model.getClass().getName().equals(ConsensusLevel.class.getName())) {

				payload.put(jsonify((ConsensusLevel) model));
			}

			else if (model.getClass().getName().equals(Topic.class.getName())) {

				payload.put(jsonify((Topic) model));
			}

			else if (model.getClass().getName().equals(Participation.class.getName())) {

				payload.put(jsonify((Participation) model));
			}

			else if (model.getClass().getName().equals(GroupSettings.class.getName())) {

				payload.put(jsonify((GroupSettings) model));
			}
		}

		try {

			return new JSONObject()
					.put("payload", payload)
					.toString();

		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public ModelClass deserialize(String json) {
		try {
			JSONObject root = new JSONObject(json);
			switch (root.getString("type")) {
				case "member":
					return deserializeMember(root);
				case "meeting":
					return deserializeMeeting(root);
				case "group":
					return deserializeGroup(root);
				case "poll":
					return deserializePoll(root);
				case "voice":
					return deserializeVoice(root);
				case "consensusLevel":
					return deserializeConsensusLevel(root);
				case "topic":
					return deserializeTopic(root);
				case "participation":
					return deserializeParticipation(root);
				case "groupSettings":
					return deserializeGroupSettings(root);
				case "moderationCard":
					return deserializeModerationCard(root);
				default:
					throw new IllegalArgumentException("Unsupported type for deserialization: " + root.get("type"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public ModelClassData bulkDeserializeAndMerge(String json) {
		try {

			Collection<ModelClass> deserialized = new ArrayList<>();

			JSONObject root = new JSONObject(json);
			JSONArray payload = root.getJSONArray("payload");

			List<JSONObject> memberJSONs = new ArrayList<>();
			List<JSONObject> meetingJSONs = new ArrayList<>();
			List<JSONObject> groupJSONs = new ArrayList<>();
			List<JSONObject> pollJSONs = new ArrayList<>();
			List<JSONObject> voiceJSONs = new ArrayList<>();
			List<JSONObject> consensusLevelJSONs = new ArrayList<>();
			List<JSONObject> topicJSONs = new ArrayList<>();
			List<JSONObject> participationJSONs = new ArrayList<>();
			List<JSONObject> groupSettingsJSONs = new ArrayList<>();
			List<JSONObject> moderationCardsJSONs = new ArrayList<>();

			for (int i = 0; i < payload.length(); i++) {
				JSONObject current = ((JSONObject) payload.get(i));
				switch (current.getString("type")) {
					case "member":
						memberJSONs.add(current);
						break;
					case "meeting":
						meetingJSONs.add(current);
						break;
					case "group":
						groupJSONs.add(current);
						break;
					case "poll":
						pollJSONs.add(current);
						break;
					case "voice":
						voiceJSONs.add(current);
						break;
					case "consensusLevel":
						consensusLevelJSONs.add(current);
						break;
					case "topic":
						topicJSONs.add(current);
						break;
					case "participation":
						participationJSONs.add(current);
						break;
					case "groupSettings":
						groupSettingsJSONs.add(current);
						break;
					case "moderationCard":
						moderationCardsJSONs.add(current);
						break;
					default:
						throw new IllegalArgumentException("Unsupported type for deserialization: " + root.get("type"));
				}
			}

			ModelClassData modelClassData = new ModelClassData();

			for (JSONObject memberJSON : memberJSONs) {
				Member member = (Member) deserialize(memberJSON.toString());
				dataService.mergeMember(member);
				deserialized.add(member);
			}

			for (JSONObject groupSettingsJSON : groupSettingsJSONs) {
				GroupSettings groupSettings = (GroupSettings) deserialize(groupSettingsJSON.toString());
				dataService.mergeGroupSettings(groupSettings);
				deserialized.add(groupSettings);
			}

			for (JSONObject groupJSON : groupJSONs) {
				Group group = (Group) deserialize(groupJSON.toString());

				if(group != null) {
					dataService.mergeGroup(group);
					deserialized.add(group);
				}
			}

			for (JSONObject meetingJSON : meetingJSONs) {
				Meeting meeting = (Meeting) deserialize(meetingJSON.toString());

				if(meeting != null) {

					dataService.mergeMeeting(meeting);
					deserialized.add(meeting);
				}

			}

			for (JSONObject pollJSON : pollJSONs) {
				Poll poll = (Poll) deserialize(pollJSON.toString());
				dataService.mergePoll(poll);
				deserialized.add(poll);
			}
			for (JSONObject moderationCardsJSON : moderationCardsJSONs) {
				ModerationCard moderationCard = (ModerationCard) deserialize(moderationCardsJSON.toString());
				dataService.mergeModerationCard(moderationCard);
				deserialized.add(moderationCard);
			}

			for (JSONObject consensusLevelJSON : consensusLevelJSONs) {
				ConsensusLevel consensusLevel = (ConsensusLevel) deserialize(consensusLevelJSON.toString());
				dataService.mergeConsensusLevel(consensusLevel);
				deserialized.add(consensusLevel);
			}

			for (JSONObject topicJSON : topicJSONs) {
				Topic topic = (Topic) deserialize(topicJSON.toString());
				dataService.mergeTopic(topic);
				deserialized.add(topic);
			}


			for (JSONObject voiceJSON : voiceJSONs) {
				Voice voice = (Voice) deserialize(voiceJSON.toString());
				dataService.mergeVoice(voice);
				deserialized.add(voice);
			}


			for (JSONObject participationJSON : participationJSONs) {
				Participation participation = (Participation) deserialize(participationJSON.toString());
				dataService.mergeParticipation(participation);
				deserialized.add(participation);
			}

			modelClassData.setData(deserialized);
			return modelClassData;

		} catch (JSONException e) {
			e.printStackTrace();
			return null;
			//TODO handling
		}
	}

	/**
	 * Returned String does not contain information about the roles of the member in its groups. Use SerializationService{@link #serialize(Group)} to serialize
	 * roles.
	 */
	private JSONObject jsonify(Member member) {
		try {

			return new JSONObject()
					.put("type", "member")
					.put("memberId", member.getMemberId())
					.put("name", member.getName())
					.put("isGhost", member.getIsGhost())
					.put("isDeleted",member.isDeleted());

		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	private JSONObject jsonify(Poll poll) {
		try {

			return new JSONObject()
					.put("type", "poll")
					.put("pollId", poll.getPollId())
					.put("title", poll.getTitle())
					.put("consensusProposal", poll.getConsensusProposal())
					.put("note", poll.getNote())
					.put("isOpen", poll.getIsOpen())
					.put("isClosedByModerator", poll.getClosedByModerator())
					.put("meeting", poll.getMeeting().getMeetingId())
					.put("voteMembersCountOnClosed", poll.getVoteMembersCountOnClosed())
					.put("isDeleted", poll.isDeleted());

		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	private JSONObject jsonify(ModerationCard moderationCard) {
		try {

			return new JSONObject()
					.put("type", "moderationCard")
					.put("cardId", moderationCard.getCardId())
					.put("content", moderationCard.getContent())
					.put("backgroundColor", moderationCard.getBackgroundColor())
					.put("fontColor", moderationCard.getFontColor())
					.put("meetingId", moderationCard.getMeetingId())
					.put("isDeleted",moderationCard.isDeleted());

		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	private JSONObject jsonify(Voice voice) {
		try {

			return new JSONObject()
					.put("type", "voice")
					.put("voiceId", voice.getVoiceId())
					.put("explanation", voice.getExplanation())
					.put("member", voice.getMember().getMemberId())
					.put("consensusLevel", voice.getConsensusLevel().getConsensusLevelId())
					.put("poll", voice.getPoll().getPollId())
					.put("isDeleted", voice.isDeleted());

		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	private JSONObject jsonify(Topic topic) {
		try {

			return new JSONObject()
					.put("type", "topic")
					.put("topicId", topic.getTopicId())
					.put("title", topic.getTitle())
					.put("duration", topic.getDuration())
					.put("status", topic.getStatus())
					.put("meeting", topic.getMeeting().getMeetingId())
					.put("isDeleted", topic.isDeleted());

		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	private JSONObject jsonify(Participation participation) {
		try {

			return new JSONObject()
					.put("type", "participation")
					.put("participationId", participation.getParticipationId())
					.put("contributions", participation.getContributions())
					.put("number", participation.getNumber())
					.put("startTime", participation.getStartTime())
					.put("isSpeaking", participation.getIsSpeaking())
					.put("isInListOfSpeakers", participation.getIsInListOfSpeakers())
					.put("time", participation.getTime())
					.put("meeting", participation.getMeeting().getMeetingId())
					.put("member", participation.getMember().getMemberId())
					.put("isDeleted", participation.isDeleted());

		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	private JSONObject jsonify(ConsensusLevel consensusLevel) {
		try {

			return new JSONObject()
					.put("type", "consensusLevel")
					.put("consensusLevelId", consensusLevel.getConsensusLevelId())
					.put("name", consensusLevel.getName())
					.put("description", consensusLevel.getDescription())
					.put("color", consensusLevel.getColor())
					.put("number", consensusLevel.getNumber())
					.put("groupSettings", consensusLevel.getGroupSettings().getSettingsId())
					.put("isDeleted", consensusLevel.isDeleted());

		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	private JSONObject jsonify(GroupSettings groupSettings) {

		try {

			return new JSONObject()
					.put("type", "groupSettings")
					.put("settingsId", groupSettings.getSettingsId())
					.put("isDeleted", groupSettings.isDeleted());

		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	private JSONObject jsonify(Meeting meeting) {

		try {

			Map<Long, Attendance> membersWithAttendance = new HashMap<>();

			for (Member member : meeting.getMembers()) {

				membersWithAttendance.put(member.getMemberId(), member.getAttendance(meeting));
			}

			JSONArray members = new JSONArray();

			for(Long memberId : membersWithAttendance.keySet()) {

				JSONObject memberJSON = new JSONObject()
						.put("memberId", memberId)
						.put("attendance", membersWithAttendance.get(memberId).name());

				members.put(memberJSON);
			}

			return new JSONObject()
					.put("type", "meeting")
					.put("meetingId", meeting.getMeetingId())
					.put("date", meeting.getDate())
					.put("startTime", meeting.getStartTime())
					.put("endTime", meeting.getEndTime())
					.put("cause", meeting.getCause())
					.put("location", meeting.getLocation())
					.put("online", meeting.getOnline())
					.put("open", meeting.getOpen())
					.put("members", members)
					.put("group", meeting.getGroup().getGroupId())
					.put("isDeleted", meeting.isDeleted());

		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	private JSONObject jsonify(Group group) {
		try {

			Map<Long, Set<Role>> membersWithRoles = new HashMap<>();

			for (Member member : group.getUniqueMembers()) {

				Set<Role> roles = membersWithRoles.get(member.getMemberId());

				for (Role role : member.getRoles(group)) {

					if (roles == null) {
						roles = new HashSet<>();
					}

					roles.add(role);
				}

				membersWithRoles.put(member.getMemberId(), roles);
			}

			JSONArray members = new JSONArray();

			for (Long memberId : membersWithRoles.keySet()) {

				JSONArray roles = new JSONArray();

				if (membersWithRoles.get(memberId) != null && !membersWithRoles.get(memberId).isEmpty()) {
					for (Role role : membersWithRoles.get(memberId)) {
						roles.put(role.name());
					}
				}

				JSONObject memberJSON = new JSONObject()
						.put("memberId", memberId)
						.put("roles", roles);
				members.put(memberJSON);
			}

			return new JSONObject()
					.put("type", "group")
					.put("groupId", group.getGroupId())
					.put("members", members)
					.put("name", group.getName())
					.put("groupSettings", group.getGroupSettings().getSettingsId())
					.put("isDeleted", group.isDeleted());

		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	private Member deserializeMember(JSONObject json) {
		try {

			Member member = new Member(json.getLong("memberId"));
			member.setName(json.getString("name"));
			member.setIsGhost(json.getBoolean("isGhost"));
			member.setIsDeleted(json.getBoolean("isDeleted"));
			return member;

		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	private Poll deserializePoll(JSONObject json) {
		try {

			Poll poll = new Poll();
			poll.setPollId(json.getLong("pollId"));
			poll.setTitle(json.getString("title"));
			poll.setConsensusProposal(json.getString("consensusProposal"));
			poll.setNote(json.getString("note"));
			poll.setIsOpen(json.getBoolean("isOpen"));
			poll.setClosedByModerator(json.getBoolean("isClosedByModerator"));
			poll.setVoteMembersCountOnClosed(json.getInt("voteMembersCountOnClosed"));
			poll.setIsDeleted(json.getBoolean("isDeleted"));

			try {

				poll.setMeeting(dataService.getMeeting(json.getLong("meeting")));

			} catch (MeetingNotFoundException e) {

				e.printStackTrace();
			}

			return poll;

		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	private ModerationCard deserializeModerationCard(JSONObject json) {
		try {
			ModerationCard moderationCard = new ModerationCard();
			moderationCard.setCardId(json.getLong("cardId"));
			moderationCard.setContent(json.getString("content"));
			moderationCard.setBackgroundColor(json.getInt("backgroundColor"));
			moderationCard.setFontColor(json.getInt("fontColor"));
			moderationCard.setIsDeleted(json.getBoolean("isDeleted"));

			try {

				moderationCard.setMeeting(dataService.getMeeting(json.getLong("meetingId")));

			} catch (MeetingNotFoundException e) {

				e.printStackTrace();
			}

			return moderationCard;

		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	private Participation deserializeParticipation(JSONObject json) {
		try {

			Participation participation = new Participation();
			participation.setParticipationId(json.getLong("participationId"));
			participation.setContributions(json.getInt("contributions"));
			participation.setNumber(json.getInt("number"));
			participation.setStartTime(json.getLong("startTime"));
			participation.setIsInListOfSpeakers(json.getBoolean("isInListOfSpeakers"));
			participation.setIsSpeaking(json.getBoolean("isSpeaking"));
			participation.setTime(json.getLong("time"));
			participation.setIsDeleted(json.getBoolean("isDeleted"));

			try {

				participation.setMeeting(dataService.getMeeting(json.getLong("meeting")));

			} catch (MeetingNotFoundException e) {

				e.printStackTrace();
			}

			try {

				participation.setMember(dataService.getMember(json.getLong("member")));

			} catch (MemberNotFoundException e) {
				e.printStackTrace();
			}

			return participation;

		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	private Voice deserializeVoice(JSONObject json) {
		try {

			Voice voice = new Voice();
			voice.setVoiceId(json.getLong("voiceId"));
			voice.setExplanation(json.getString("explanation"));
			voice.setIsDeleted(json.getBoolean("isDeleted"));
			try {

				voice.setMember(dataService.getMember(json.getLong("member")));

			} catch (MemberNotFoundException e) {

				e.printStackTrace();
			}


			try {

				voice.setPoll(dataService.getPoll(json.getLong("poll")));

			} catch (PollNotFoundException e) {

				e.printStackTrace();
			}

			voice.setConsensusLevel(dataService.getConsensusLevel(json.getLong("consensusLevel")));

			return voice;

		} catch (JSONException e) {

			e.printStackTrace();
			return null;
		}
	}

	private ConsensusLevel deserializeConsensusLevel(JSONObject json) {
		try {

			ConsensusLevel consensusLevel = new ConsensusLevel();
			consensusLevel.setConsensusLevelId(json.getLong("consensusLevelId"));
			consensusLevel.setName(json.getString("name"));
			consensusLevel.setDescription(json.getString("description"));
			consensusLevel.setColor(json.getInt("color"));
			consensusLevel.setNumber(json.getInt("number"));

			try {

				consensusLevel.setGroupSettings(dataService.getGroupSetting(json.getLong("groupSettings")));

			} catch (GroupSettingsNotFoundException e) {

				e.printStackTrace();
			}

			consensusLevel.setIsDeleted(json.getBoolean("isDeleted"));
			return consensusLevel;

		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	private Topic deserializeTopic(JSONObject json) {
		try {

			Topic topic = new Topic();
			topic.setTopicId(json.getLong("topicId"));
			topic.setTitle(json.getString("title"));
			topic.setDuration(json.getLong("duration"));
			topic.setStatus(json.getString("status"));

			try {

				topic.setMeeting(dataService.getMeeting(json.getLong("meeting")));

			} catch (MeetingNotFoundException e) {

				e.printStackTrace();
			}

			topic.setIsDeleted(json.getBoolean("isDeleted"));
			return topic;

		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	private GroupSettings deserializeGroupSettings (JSONObject json) {
		try {

			GroupSettings groupSettings = new GroupSettings();
			groupSettings.setSettingsId(json.getLong("settingsId"));
			groupSettings.setIsDeleted(json.getBoolean("isDeleted"));
			return groupSettings;

		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	private Meeting deserializeMeeting(JSONObject json) {

		try {

			Long meetingId = json.getLong("meetingId");
			Meeting meeting = new Meeting();
			meeting.setMeetingId(meetingId);

			Meeting existingMeeting = null;

			try {

				existingMeeting = dataService.getMeeting(meetingId);

			} catch (MeetingNotFoundException e) {

				e.printStackTrace();
			}

			if(existingMeeting != null) {

				JSONArray memberWithStatus = json.getJSONArray("members");

				boolean contains = false;

				for(int i = 0; i < memberWithStatus.length(); i++) {

					if(Util.bytesToLong(localAuthor.getId().getBytes()) == memberWithStatus.getJSONObject(i).getLong("memberId")) {

						contains = true;
					}
				}

				if(!contains) {

					dataService.deleteMeeting(existingMeeting);
					return null;
				}
			}

			meeting.setDate(json.getLong("date"));
			meeting.setStartTime(json.getLong("startTime"));
			meeting.setEndTime(json.getLong("endTime"));
			meeting.setCause(json.getString("cause"));
			meeting.setLocation(json.getString("location"));
			meeting.setOnline(json.getBoolean("online"));
			meeting.setOpen(json.getBoolean("open"));

			try {
				meeting.setGroup(dataService.getGroup(json.getLong("group")));
			} catch (GroupNotFoundException e) {
				e.printStackTrace();
			}
			meeting.setIsDeleted(json.getBoolean("isDeleted"));

			for(MemberMeetingRelation memberMeetingRelation : dataService.getMemberMeetingRelations()) {

				if(memberMeetingRelation.getMeetingId().equals(meeting.getMeetingId())) {

					dataService.deleteMemberMeetingRelation(memberMeetingRelation);
				}
			}

			Collection<Member> allMembers = dataService.getMembers();

			for(Member member : allMembers) {

				JSONArray memberWithStatus = json.getJSONArray("members");

				for(int i = 0; i < memberWithStatus.length(); i++) {

					Long memberId = memberWithStatus.getJSONObject(i).getLong("memberId");
					Attendance attendance = Attendance.valueOf(memberWithStatus.getJSONObject(i).getString("attendance"));

					if(member.getMemberId().equals(memberId)) {
						MemberMeetingRelation memberMeetingRelation = meeting.addMember(member, attendance);
						dataService.saveMemberMeetingRelation(memberMeetingRelation);
					}
				}

			}

			return meeting;

		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Asserts, that the complete meeting-objects of the groups meetings and the complete member-objects of the groups members are present in the database
	 */
	private Group deserializeGroup(JSONObject json) {
		try {

			Long groupId = json.getLong("groupId");

			Group group = new Group(groupId);

			Group existingGroup = null;

			try {

				existingGroup = dataService.getGroup(groupId);

			} catch (GroupNotFoundException e) {

				e.printStackTrace();
			}

			if(existingGroup != null)  {

				JSONArray memberWithRoleArray = json.getJSONArray("members");

				for (int i = 0; i < memberWithRoleArray.length(); i++) {

					if(Util.bytesToLong(localAuthor.getId().getBytes()) == (memberWithRoleArray.getJSONObject(i).getLong("memberId"))) {

						if(memberWithRoleArray.getJSONObject(i).getJSONArray("roles").length() == 0) {

							dataService.deleteGroup(existingGroup);
							return null;
						}
					}
				}
			}

			group.setName(json.getString("name"));
			group.setIsDeleted(json.getBoolean("isDeleted"));

			try {

				group.setGroupSettings(dataService.getGroupSetting(json.getLong("groupSettings")));

			} catch (GroupSettingsNotFoundException e) {

				e.printStackTrace();
			}

			for(MemberGroupRelation memberGroupRelation : dataService.getMemberGroupRelations()) {

				if(memberGroupRelation.getGroupId().equals(group.getGroupId())) {

					dataService.deleteMemberGroupRelation(memberGroupRelation);
				}
			}

			Collection<Member> allMembers = dataService.getMembers();

			for (Member member : allMembers) {

				JSONArray memberWithRoleArray = json.getJSONArray("members");

				for (int i = 0; i < memberWithRoleArray.length(); i++) {

					Long memberId = memberWithRoleArray.getJSONObject(i).getLong("memberId");

					JSONArray roles = memberWithRoleArray.getJSONObject(i).getJSONArray("roles");

					if (member.getMemberId().equals(memberId)) {

						for (int j = 0; j < roles.length(); j++) {

							MemberGroupRelation memberGroupRelation = group.addMember(member, Role.valueOf(roles.getString(j)));

							if(memberGroupRelation != null) {
								dataService.saveMemberGroupRelation(memberGroupRelation);
							}
						}
					}
				}
			}

			return group;

		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
}
