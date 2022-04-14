package dhbw.smartmoderation.connection;

import org.briarproject.briar.api.blog.BlogInvitationRequest;
import org.briarproject.briar.api.blog.BlogInvitationResponse;
import org.briarproject.briar.api.conversation.ConversationMessageVisitor;
import org.briarproject.briar.api.forum.ForumInvitationRequest;
import org.briarproject.briar.api.forum.ForumInvitationResponse;
import org.briarproject.briar.api.introduction.IntroductionRequest;
import org.briarproject.briar.api.introduction.IntroductionResponse;
import org.briarproject.briar.api.messaging.PrivateMessageHeader;
import org.briarproject.briar.api.privategroup.PrivateGroup;
import org.briarproject.briar.api.privategroup.invitation.GroupInvitationRequest;
import org.briarproject.briar.api.privategroup.invitation.GroupInvitationResponse;

/**
 * An implementation of {@link ConversationMessageVisitor} to visit group invitation requests and return their {@code PrivateGroup}-objects.
 */
public class GroupInvitationVisitor implements ConversationMessageVisitor<PrivateGroup> {

	private static final String TAG = GroupInvitationVisitor.class.getSimpleName();

	@Override
	public PrivateGroup visitPrivateMessageHeader(PrivateMessageHeader h) {
		return null;
	}

	@Override
	public PrivateGroup visitBlogInvitationRequest(BlogInvitationRequest r) {
		return null;
	}

	@Override
	public PrivateGroup visitBlogInvitationResponse(BlogInvitationResponse r) {
		return null;
	}

	@Override
	public PrivateGroup visitForumInvitationRequest(ForumInvitationRequest r) {
		return null;
	}

	@Override
	public PrivateGroup visitForumInvitationResponse(ForumInvitationResponse r) {
		return null;
	}

	@Override
	public PrivateGroup visitGroupInvitationRequest(GroupInvitationRequest r) {
		if (!r.isLocal() && !r.wasAnswered()) {
			return r.getNameable();
		} else return null;
	}

	@Override
	public PrivateGroup visitGroupInvitationResponse(GroupInvitationResponse r) {
		return null;
	}

	@Override
	public PrivateGroup visitIntroductionRequest(IntroductionRequest r) {
		return null;
	}

	@Override
	public PrivateGroup visitIntroductionResponse(IntroductionResponse r) {
		return null;
	}

}
