package dhbw.smartmoderation.group.chat;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

	private final Context context;
	private final ArrayList<String> Messages = new ArrayList<>();

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
		return new MessageViewHolder(constraintLayout, context);
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

		private final TextView MessageText;

		public MessageViewHolder(ConstraintLayout layout, Context context) {
			super(layout);
			MessageText = new TextView(context);
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
