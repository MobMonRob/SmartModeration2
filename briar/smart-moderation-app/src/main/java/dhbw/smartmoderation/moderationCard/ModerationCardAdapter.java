package dhbw.smartmoderation.moderationCard;

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

public class ModerationCardAdapter extends RecyclerView.Adapter<ModerationCardAdapter.ModerationCardViewHolder>{
    private Context context;
    private ArrayList<String> moderationCards = new ArrayList<String>();

    public ModerationCardAdapter(Context context, Collection<String> moderationCards) {
        this.context = context;
        updateModerationCards(moderationCards);
    }
    public void updateModerationCards(Collection<String> moderationCards){
        this.moderationCards.clear();
        this.moderationCards.addAll(moderationCards);
    }

    @NonNull
    @Override
    public ModerationCardAdapter.ModerationCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ConstraintLayout constraintLayout = new ConstraintLayout(context);
        constraintLayout.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ModerationCardAdapter.ModerationCardViewHolder
                moderationCardViewHolder = new ModerationCardAdapter.ModerationCardViewHolder(constraintLayout, context);
        return moderationCardViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ModerationCardAdapter.ModerationCardViewHolder holder, int position) {
        holder.setModerationCardText(moderationCards.get(position));
    }

    @Override
    public int getItemCount() {
        return moderationCards.size();
    }

    protected static class ModerationCardViewHolder extends RecyclerView.ViewHolder{

        private TextView ModerationCardTextView;
        private Context context;

        public ModerationCardViewHolder(ConstraintLayout layout, Context context) {
            super(layout);
            this.context = context;
            ModerationCardTextView = new TextView(this.context);
            ModerationCardTextView.setId(View.generateViewId());
            ModerationCardTextView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            ModerationCardTextView.setGravity(Gravity.CENTER);
            ModerationCardTextView.setTextSize(18);
            layout.addView(ModerationCardTextView);

            ConstraintSet messageConstraintSet = new ConstraintSet();
            messageConstraintSet.clone(layout);
            messageConstraintSet.connect(ModerationCardTextView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
            messageConstraintSet.connect(ModerationCardTextView.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT);
            messageConstraintSet.connect(ModerationCardTextView.getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT);
            messageConstraintSet.applyTo(layout);

        }

        public void setModerationCardText(String moderationCardText){
            ModerationCardTextView.setText(moderationCardText);
        }
    }
}
