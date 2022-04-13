package dhbw.smartmoderation.moderationCard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.data.model.ModerationCard;

public class ModerationCardAdapter extends RecyclerView.Adapter<ModerationCardAdapter.ModerationCardViewHolder>{
    private Context context;
    private ArrayList<ModerationCard> moderationCards = new ArrayList<ModerationCard>();
    private RecyclerView recyclerView;

    private View.OnClickListener onClickListener = view -> {
        int position = this.recyclerView.getChildLayoutPosition(view);
        ModerationCard moderationCard = this.moderationCards.get(position);
        EditModerationCard editModerationCardView = new EditModerationCard(moderationCard, FragmentManager.findFragment(view));
        editModerationCardView.show();

    };

    public ModerationCardAdapter(Context context, Collection<ModerationCard> moderationCards) {
        this.context = context;
        updateModerationCards(moderationCards);
    }
    public void updateModerationCards(Collection<ModerationCard> moderationCards){
        this.moderationCards.clear();
        this.moderationCards.addAll(moderationCards);
        this.notifyDataSetChanged();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {

        this.recyclerView = recyclerView;
    }

    @NonNull
    @Override
    public ModerationCardAdapter.ModerationCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View cardView = LayoutInflater.from(context).inflate(R.layout.moderationcardlistitem, null);
        cardView.setOnClickListener(onClickListener);
        return new ModerationCardViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(@NonNull ModerationCardAdapter.ModerationCardViewHolder holder, int position) {
        TextView cardTextView = holder.itemView.findViewById(R.id.cardTextView);
        cardTextView.setText(moderationCards.get(position).getContent());
        cardTextView.setTextColor(moderationCards.get(position).getFontColor());
        cardTextView.setBackgroundColor(moderationCards.get(position).getBackgroundColor());

        TextView authorTextView = holder.itemView.findViewById(R.id.authorTextView);
        String authorName = moderationCards.get(position).getAuthor();
        String authorInitials = authorName.length() < 2 ? authorName : authorName.substring(0, 2);
        authorTextView.setText(authorInitials);
    }

    @Override
    public int getItemCount() {
        return moderationCards.size();
    }

    protected static class ModerationCardViewHolder extends RecyclerView.ViewHolder{
        public ModerationCardViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

}
