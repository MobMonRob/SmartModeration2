package dhbw.smartmoderation.group.chat;

import org.briarproject.briar.api.privategroup.GroupMessageHeader;
import org.briarproject.briar.api.privategroup.PrivateGroup;

import java.util.ArrayList;
import java.util.Collection;

import dhbw.smartmoderation.controller.SmartModerationController;
import dhbw.smartmoderation.exceptions.GroupNotFoundException;
import dhbw.smartmoderation.exceptions.NoGroupMessageTextFoundException;
import dhbw.smartmoderation.util.Util;

import static org.briarproject.bramble.util.LogUtils.now;

public class GroupChatController extends SmartModerationController {

	public Collection<String> getMessages() {
		Collection<String> list = new ArrayList<String>();
		list.add("Name : Hallo");
		list.add("Name2 : Hallo ");
		list.add("Name2 : Hallo ");
		list.add("Name2 : Hallo ");
		list.add("Name2 : Hallo ");
		list.add("Name2 : Hallo ");
		list.add("Name2 : Hallo ");
		return list;
	}

	public void createAndStoreMessage(String text, Long groupId) throws GroupNotFoundException {

		PrivateGroup group = getPrivateGroup(groupId);
		connectionService.createAndStoreMessage(text,group);
	}

	public Collection<String> loadItems(Long groupId) throws NoGroupMessageTextFoundException, GroupNotFoundException {

		PrivateGroup group = getPrivateGroup(groupId);
		return connectionService.getMessages(group);
	}

	private PrivateGroup getPrivateGroup(Long groupId) throws GroupNotFoundException {

		for (PrivateGroup group : connectionService.getGroups()){

			if (groupId.equals(Util.bytesToLong(group.getId().getBytes()))) {

				return group;
			}
		};

		throw new GroupNotFoundException();
	}
}
