package dhbw.smartmoderation.connection.synchronization;

import org.briarproject.bramble.api.identity.LocalAuthor;

import java.util.Collection;

import dhbw.smartmoderation.data.model.*;

public interface SerializationService {

	void setLocalAuthor(LocalAuthor localAuthor);

	String serialize(Member member);

	String serialize(Meeting meeting);

	String serialize(Group group);

	String serialize (Poll poll);

	String serialize (ModerationCard moderationCard);

	String serialize(Topic topic);

	String serialize(Participation participation);

	String serialize(GroupSettings groupSettings);

	String serialize(ConsensusLevel consensusLevel);

	String serialize(Voice voice);

	String bulkSerialize(Collection<ModelClass> models);

	ModelClass deserialize(String json);

	ModelClassData bulkDeserializeAndMerge(String json);
}
