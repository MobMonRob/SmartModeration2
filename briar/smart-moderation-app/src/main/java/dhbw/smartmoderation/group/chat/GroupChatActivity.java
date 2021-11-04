package dhbw.smartmoderation.group.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.android.material.button.MaterialButton;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import dhbw.smartmoderation.R;
import dhbw.smartmoderation.exceptions.GroupNotFoundException;
import dhbw.smartmoderation.exceptions.NoGroupMessageTextFoundException;
import dhbw.smartmoderation.exceptions.SmartModerationException;
import dhbw.smartmoderation.util.ExceptionHandlingActivity;

public class GroupChatActivity extends ExceptionHandlingActivity {

	MaterialButton SendButton;
	MaterialButton AddContactButton;
	EditText MessageText;
	RecyclerView recMessages;
	GroupChatController controller;
	MessageAdapter Adapter;
	Long groupId;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_chat);

		Intent intent = getIntent();
		groupId = intent.getLongExtra("groupId", 0);


		SendButton = findViewById(R.id.SendMessage);
		AddContactButton = findViewById(R.id.AddContactButton);
		MessageText = findViewById(R.id.MessageText);
		recMessages = findViewById(R.id.recMessages);

		SendButton.setOnClickListener(this::sendMessage);
		AddContactButton.setOnClickListener(this::addContact);

		controller = new GroupChatController();

		Adapter = new MessageAdapter(this,controller.getMessages());

		recMessages.setLayoutManager(new LinearLayoutManager(this));
		recMessages.setAdapter(Adapter);

		loadItems();
	}

	private void loadItems(){

		try {
			Adapter.updateMessages(controller.loadItems(groupId));
		} catch (SmartModerationException | NoGroupMessageTextFoundException exception) {
			handleException(exception);
		}
	}

	public void addContact(View v){

	}

	public void sendMessage(View v){
		try {
			controller.createAndStoreMessage(MessageText.getText().toString(), groupId);
		} catch (GroupNotFoundException exception) {
			handleException(exception);
		}
	}


}
