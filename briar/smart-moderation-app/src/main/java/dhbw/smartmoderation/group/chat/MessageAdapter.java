package dhbw.smartmoderation.group.chat;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;
import dhbw.smartmoderation.group.create.ContactAdapter;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

	private Context context;
	private ArrayList<String> Messages = new ArrayList<String>();

	public MessageAdapter(Context context,Collection<String> messages){
		this.context = context;
		updateMessages(messages);
	}

	public void updateMessages(Collection<String> messages){
		Messages.clear();
		Messages.addAll(messages);
	}

	@NonNull
	@Override
	public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
			int viewType) {
		ConstraintLayout constraintLayout = new ConstraintLayout(context);
		constraintLayout.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		MessageAdapter.MessageViewHolder
				messageViewHolder = new MessageAdapter.MessageViewHolder(constraintLayout, context);
		return messageViewHolder;
	}

	@Override
	public void onBindViewHolder(@NonNull MessageViewHolder holder,
			int position) {
		holder.setMessage(Messages.get(position));
	}

	@Override
	public int getItemCount() {
		return Messages.size();
	}

	protected static class MessageViewHolder extends RecyclerView.ViewHolder{

		private TextView MessageText;
		private Context context;

		public MessageViewHolder(ConstraintLayout layout, Context context) {
			super(layout);
			this.context = context;
			MessageText = new TextView(this.context);
			MessageText.setId(View.generateViewId());
			MessageText.setLayoutParams(new ViewGroup.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
			MessageText.setGravity(Gravity.CENTER);
			MessageText.setTextSize(18);
			layout.addView(MessageText);

			ConstraintSet messageConstraintSet = new ConstraintSet();
			messageConstraintSet.clone(layout);
			messageConstraintSet.connect(MessageText.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
			messageConstraintSet.connect(MessageText.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);
			messageConstraintSet.connect(MessageText.getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT);
			messageConstraintSet.applyTo(layout);

		}

		public void setMessage(String message){
			MessageText.setText(message);
		}
	}
}
