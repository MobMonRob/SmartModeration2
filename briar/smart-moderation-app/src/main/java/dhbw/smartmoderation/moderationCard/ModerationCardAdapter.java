package dhbw.smartmoderation.moderationCard;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.data.model.ModerationCard;

public class ModerationCardAdapter extends RecyclerView.Adapter<ModerationCardAdapter.ModerationCardViewHolder>{
    private Context context;
    private ArrayList<ModerationCard> moderationCards = new ArrayList<ModerationCard>();

    public ModerationCardAdapter(Context context, Collection<ModerationCard> moderationCards) {
        this.context = context;
        updateModerationCards(moderationCards);
    }
    public void updateModerationCards(Collection<ModerationCard> moderationCards){
        this.moderationCards.clear();
        this.moderationCards.addAll(moderationCards);
    }

    @NonNull
    @Override
    public ModerationCardAdapter.ModerationCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View cardView = LayoutInflater.from(context).inflate(R.layout.moderationcardlistitem, null);
        return new ModerationCardViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(@NonNull ModerationCardAdapter.ModerationCardViewHolder holder, int position) {
        TextView cardTextView = holder.itemView.findViewById(R.id.cardTextView);
        cardTextView.setTextColor(getProperTextColor(moderationCards.get(position).getColor()));
        cardTextView.setText(moderationCards.get(position).getContent());
        cardTextView.setBackgroundColor(moderationCards.get(position).getColor());
    }

    private int getProperTextColor(int backgroundColor) {
        if (backgroundColor >= Color.BLACK && backgroundColor < Color.GRAY) {
            return Color.WHITE;
        } else {
            return Color.BLACK;
        }
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
