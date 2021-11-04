package dhbw.smartmoderation.group.invitations;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import dhbw.smartmoderation.R;
import dhbw.smartmoderation.connection.GroupInvitationHandler;
import dhbw.smartmoderation.connection.Invitation;

class InvitationsAdapter extends RecyclerView.Adapter<InvitationsAdapter.InvitationViewHolder> {

	private static final String TAG = InvitationsAdapter.class.getSimpleName();

	private Context context;
	private List<Invitation> invitations;
	private GroupInvitationHandler handler;

	InvitationsAdapter(Context context, List<Invitation> invitations, GroupInvitationHandler handler) {
		this.context = context;
		this.invitations = invitations;
		this.handler = handler;

		updateInvitations(invitations);
	}

	@NonNull
	@Override
	public InvitationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ConstraintLayout constraintLayout = new ConstraintLayout(context);
		constraintLayout.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, 150));
		InvitationViewHolder invitationViewHolder = new InvitationViewHolder(constraintLayout, context, handler);
		return invitationViewHolder;
	}

	@Override
	public void onBindViewHolder(@NonNull InvitationViewHolder holder, int position) {
		Invitation invitation = invitations.get(position);
		holder.setInvitation(invitation);
		holder.txtGroupName.setText(invitations.get(position).getPrivateGroup().getName());
	}

	@Override
	public int getItemCount() {
		return invitations.size();
	}

	void updateInvitations(List<Invitation> invitations) {
		this.invitations.clear();
		this.invitations.addAll(invitations);
		Log.d(TAG, "Updated invitations: " + invitations);
	}

	static class InvitationViewHolder extends RecyclerView.ViewHolder {

		private Invitation invitation;
		private GroupInvitationHandler handler;
		private Context context;

		TextView txtGroupName;

		InvitationViewHolder(ConstraintLayout constraintLayout, Context context, GroupInvitationHandler handler) {
			super(constraintLayout);

			this.handler = handler;
			this.context = context;

			txtGroupName = new TextView(this.context);
			txtGroupName.setId(View.generateViewId());
			txtGroupName.setTypeface(txtGroupName.getTypeface(), Typeface.BOLD);
			constraintLayout.addView(txtGroupName);
			ConstraintSet txtGroupNameCS = new ConstraintSet();
			txtGroupNameCS.clone(constraintLayout);
			txtGroupNameCS.connect(txtGroupName.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 20);
			txtGroupNameCS.connect(txtGroupName.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 20);
			txtGroupNameCS.connect(txtGroupName.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 20);
			txtGroupNameCS.applyTo(constraintLayout);

			ImageButton btnReject = new ImageButton(this.context);
			btnReject.setId(View.generateViewId());
			btnReject.setImageResource(R.drawable.ic_reject);
			btnReject.setBackgroundResource(0);
			btnReject.setColorFilter(ContextCompat.getColor(this.context, R.color.default_red));
			btnReject.setOnClickListener(this::onReject);
			constraintLayout.addView(btnReject);
			ConstraintSet buttonRejectCS = new ConstraintSet();
			buttonRejectCS.clone(constraintLayout);
			buttonRejectCS.connect(btnReject.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 20);
			buttonRejectCS.connect(btnReject.getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 20);
			buttonRejectCS.connect(btnReject.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 20);
			buttonRejectCS.applyTo(constraintLayout);

			ImageButton btnAccept = new ImageButton(this.context);
			btnAccept.setId(View.generateViewId());
			btnAccept.setImageResource(R.drawable.ic_accept);
			btnAccept.setBackgroundResource(0);
			btnAccept.setColorFilter(ContextCompat.getColor(this.context, R.color.default_green));
			btnAccept.setOnClickListener(this::onAccept);
			constraintLayout.addView(btnAccept);
			ConstraintSet btnAcceptCS = new ConstraintSet();
			btnAcceptCS.clone(constraintLayout);
			btnAcceptCS.connect(btnAccept.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 20);
			btnAcceptCS.connect(btnAccept.getId(), ConstraintSet.RIGHT, btnReject.getId(), ConstraintSet.LEFT, 20);
			btnAcceptCS.connect(btnAccept.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 20);
			btnAcceptCS.applyTo(constraintLayout);
		}

		void setInvitation(Invitation invitation) {
			this.invitation = invitation;
		}

		private void onAccept(View v) {
			handler.acceptGroupInvitation(invitation);
		}

		private void onReject(View v) {
			handler.rejectGroupInvitation(invitation);
		}

	}

}
