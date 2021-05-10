package dhbw.smartmoderation.group.invitations;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import dhbw.smartmoderation.R;
import dhbw.smartmoderation.connection.GroupInvitationHandler;
import dhbw.smartmoderation.connection.Invitation;
import dhbw.smartmoderation.exceptions.GroupNotFoundException;
import dhbw.smartmoderation.exceptions.MemberNotFoundException;
import dhbw.smartmoderation.exceptions.NoContactsFoundException;
import dhbw.smartmoderation.exceptions.SmartModerationException;
import dhbw.smartmoderation.util.ExceptionHandlingActivity;

/**
 * Activity for listing all open group invitations.
 */
public class ListInvitationsActivity extends ExceptionHandlingActivity implements GroupInvitationHandler {

	private static final String TAG = ListInvitationsActivity.class.getSimpleName();

	private ListInvitationsController controller;

	private RecyclerView recInvitations;
	private InvitationsAdapter invitationsAdapter;
	private LinearLayoutManager invitationsLayoutManager;
	private Thread SynchronizeThread = new Thread();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_invitations);
		setTitle(R.string.title_invitations);

		controller = new ListInvitationsController();

		recInvitations = findViewById(R.id.recInvitations);

		invitationsLayoutManager = new LinearLayoutManager(this);
		recInvitations.setLayoutManager(invitationsLayoutManager);
		try {
			invitationsAdapter = new InvitationsAdapter(this, controller.getGroupInvitations(), this);
			recInvitations.setAdapter(invitationsAdapter);
		}catch (SmartModerationException exception){
			handleException(exception);
		}

		DividerItemDecoration invitationsDividerItemDecoration = new DividerItemDecoration(recInvitations.getContext(), invitationsLayoutManager.getOrientation());
		recInvitations.addItemDecoration(invitationsDividerItemDecoration);
	}

	@Override
	protected void onResume() {
		super.onResume();
		try {
			invitationsAdapter.updateInvitations(controller.getGroupInvitations());
			Log.d(TAG, "Invitations: " + controller.getGroupInvitations());
		}catch (SmartModerationException exception){
			handleException(exception);
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
			SynchronizeThread = new Thread(() -> {
					try {
						controller.synchronizeData();
					} catch (SmartModerationException exception) {
						exception.printStackTrace();
						//TODO exception handling
					}
			});
			SynchronizeThread.start();
	}

	@Override
	public void acceptGroupInvitation(Invitation invitation) {
		controller.acceptInvitation(invitation);
		try {
			invitationsAdapter.updateInvitations(controller.getGroupInvitations());
			invitationsAdapter.notifyDataSetChanged();
		} catch (NoContactsFoundException exception) {
			handleException(exception);
		}
	}

	@Override
	public void rejectGroupInvitation(Invitation invitation) {
		controller.rejectInvitation(invitation);
		try {
			invitationsAdapter.updateInvitations(controller.getGroupInvitations());
		}catch(SmartModerationException exception){
			handleException(exception);
		}

		invitationsAdapter.notifyDataSetChanged();
	}

}
