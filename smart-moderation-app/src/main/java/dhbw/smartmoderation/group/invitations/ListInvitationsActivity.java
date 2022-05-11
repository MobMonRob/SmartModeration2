package dhbw.smartmoderation.group.invitations;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.connection.GroupInvitationHandler;
import dhbw.smartmoderation.connection.Invitation;
import dhbw.smartmoderation.exceptions.NoContactsFoundException;
import dhbw.smartmoderation.exceptions.SmartModerationException;
import dhbw.smartmoderation.util.ExceptionHandlingActivity;

/**
 * Activity for listing all open group invitations.
 */
public class ListInvitationsActivity extends ExceptionHandlingActivity implements GroupInvitationHandler {

	private static final String TAG = ListInvitationsActivity.class.getSimpleName();
	private ListInvitationsController controller;
	private InvitationsAdapter invitationsAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_invitations);
		setTitle(R.string.title_invitations);

		controller = new ListInvitationsController();

		RecyclerView recInvitations = findViewById(R.id.recInvitations);

		LinearLayoutManager invitationsLayoutManager = new LinearLayoutManager(this);
		recInvitations.setLayoutManager(invitationsLayoutManager);

		try {
			invitationsAdapter = new InvitationsAdapter(this, controller.getGroupInvitations(), this);
			recInvitations.setAdapter(invitationsAdapter);
		} catch (SmartModerationException exception){
			handleException(exception);
		}

		DividerItemDecoration invitationsDividerItemDecoration = new DividerItemDecoration(recInvitations.getContext(), invitationsLayoutManager.getOrientation());
		recInvitations.addItemDecoration(invitationsDividerItemDecoration);

		SwipeRefreshLayout pullToRefresh = findViewById(R.id.pullToRefresh);
		pullToRefresh.setOnRefreshListener(() -> {
			onResume();
			pullToRefresh.setRefreshing(false);
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		try {
			invitationsAdapter.updateInvitations(controller.getGroupInvitations());
			Log.d(TAG, "Invitations: " + controller.getGroupInvitations());
		} catch (SmartModerationException exception){
			handleException(exception);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		InvitationsAsyncTask invitationsAsyncTask = new InvitationsAsyncTask();
		invitationsAsyncTask.setFlag("synchronize");
		invitationsAsyncTask.execute();
	}

	@Override
	public void acceptGroupInvitation(Invitation invitation) {
		InvitationsAsyncTask invitationsAsyncTask = new InvitationsAsyncTask();
		invitationsAsyncTask.setFlag("accept");
		invitationsAsyncTask.execute(invitation);
	}

	@Override
	public void rejectGroupInvitation(Invitation invitation) {
		InvitationsAsyncTask invitationsAsyncTask = new InvitationsAsyncTask();
		invitationsAsyncTask.setFlag("reject");
		invitationsAsyncTask.execute(invitation);

	}

	@SuppressLint("StaticFieldLeak")
	public class InvitationsAsyncTask extends AsyncTask<Object, Exception, String> {
		String flag;

		public void setFlag(String flag) {
			this.flag = flag;
		}

		@Override
		protected void onProgressUpdate(Exception... values) {
			super.onProgressUpdate(values);
			handleException(values[0]);
		}

		@Override
		protected String doInBackground(Object... objects) {

			String returnString = "";

			switch(flag) {
				case "accept":
					Invitation invitationToAccept = (Invitation)objects[0];
					controller.acceptInvitation(invitationToAccept);
					returnString = "acceptOrReject";
					break;
				case "reject":
					Invitation invitationToReject = (Invitation)objects[0];
					controller.rejectInvitation(invitationToReject);
					returnString = "acceptOrReject";
					break;
				case "synchronize":
					try {
						controller.synchronizeData();
					} catch (SmartModerationException exception) {
						publishProgress(exception);
					}
					returnString = "synchronize";
					break;
			}

			return returnString;
		}

		@SuppressLint("NotifyDataSetChanged")
		@Override
		protected void onPostExecute(String s) {
			super.onPostExecute(s);

			if ("acceptOrReject".equals(s)) {
				try {
					invitationsAdapter.updateInvitations(controller.getGroupInvitations());
					invitationsAdapter.notifyDataSetChanged();
				} catch (NoContactsFoundException exception) {
					handleException(exception);
				}
			}
		}
	}
}
