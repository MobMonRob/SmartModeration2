package dhbw.smartmoderation.listOfSpeakers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.data.model.Participation;
import dhbw.smartmoderation.uiUtils.ItemTouchHelperAdapter;
import dhbw.smartmoderation.uiUtils.ItemTouchHelperViewHolder;
import dhbw.smartmoderation.uiUtils.OnStartDragListener;

public class ParticipationAdapter extends RecyclerView.Adapter<ParticipationAdapter.ParticipationViewHolder> implements ItemTouchHelperAdapter {

    private Context context;
    private ArrayList<Participation> participationList;
    private OnStartDragListener onStartDragListener;
    private OnParticipationListener onParticipationListener;
    private boolean isLocalAuthorModerator;
    private ListOfSpeakersFragment fragment;

    public ParticipationAdapter(ListOfSpeakersFragment fragment, Context context, OnParticipationListener onParticipationListener, boolean isLocalAuthorModerator,OnStartDragListener onStartDragListener) {
        this.fragment = fragment;
        this.context = context;
        this.participationList = new ArrayList<>();
        this.onParticipationListener = onParticipationListener;
        this.onStartDragListener = onStartDragListener;
        this.isLocalAuthorModerator = isLocalAuthorModerator;
        Collection<Participation> participations = this.onParticipationListener.getCollections();
        this.updateParticipations(participations);
    }

    public void updateParticipations(Collection<Participation> participationList) {

        this.participationList.clear();
        this.participationList.addAll(participationList);

        Collections.sort(this.participationList, ((o1, o2) -> {

            if(o1.getNumber() < o2.getNumber()) {
                return -1;
            }

            else if(o1.getNumber() > o2.getNumber()) {
                return 1;
            }

            return 0;
        }));

        this.notifyDataSetChanged();

        fragment.initializeStartStopPanel();
    }

    @NonNull
    @Override
    public ParticipationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ConstraintLayout constraintLayout = new ConstraintLayout(context);
        constraintLayout.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, 130));
        ParticipationViewHolder participationViewHolder = new ParticipationViewHolder(constraintLayout, context, this);
        return participationViewHolder;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull ParticipationViewHolder holder, int position) {
        Participation participation = this.participationList.get(position);
        holder.setParticipation(participation);
        holder.getNumber().setText(participation.getNumber() + "");
        holder.getName().setText(participation.getMember().getName());
        if(participation.getIsSpeaking()) {
            holder.getSpeakingHint().setText(context.getString(R.string.isTalking));
            holder.getSpeakingHint().setVisibility(View.VISIBLE);
        }

        else {
            holder.getSpeakingHint().setVisibility(View.GONE);
        }

        if(this.isLocalAuthorModerator) {

            holder.getReorder().setVisibility(View.VISIBLE);
        }

        else {

            holder.getReorder().setVisibility(View.GONE);
        }

        holder.getReorder().setOnTouchListener((v, event) -> {

            if(event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                onStartDragListener.onStartDrag(holder);
            }
            return false;

        });
    }

    public ArrayList<Participation> getParticipationList() {
        return participationList;
    }

    @Override
    public int getItemCount() {
        return this.participationList.size();
    }

    public Context getContext() {
        return this.context;
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {

        if(fromPosition < toPosition) {

            for(int i = fromPosition; i < toPosition; i++) {
                Collections.swap(participationList, i, i + 1);
            }
        }

        else {

            for(int i = fromPosition; i > toPosition; i--) {

                Collections.swap(participationList, i, i - 1);
            }
        }

        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemDismiss(int position) {

        Participation participation = this.participationList.remove(position);

        onParticipationListener.onParticipationDismiss(participation);
        Handler handler = new Handler();
        handler.post(() -> notifyItemRemoved(position));

        changeNumberingAfterOrderChange();

    }

    public void changeNumberingAfterOrderChange() {
        onParticipationListener.changeNumberingAfterOrderChange(participationList);

    }

    static class ParticipationViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

        private ParticipationAdapter participationAdapter;
        private Participation participation;
        private ConstraintLayout constraintLayout;
        private TextView number;
        private TextView name;
        private TextView speakingHint;
        private ImageView reorder;

        public ParticipationViewHolder(ConstraintLayout constraintLayout, Context context, ParticipationAdapter participationAdapter)  {
            super(constraintLayout);
            this.participationAdapter = participationAdapter;
            this.constraintLayout = constraintLayout;

            number = new TextView(context);
            number.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            number.setId(View.generateViewId());
            number.setTypeface(number.getTypeface(), Typeface.BOLD);
            number.setTextSize(13);
            constraintLayout.addView(number);

            name = new TextView(context);
            name.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            name.setId(View.generateViewId());
            name.setTypeface(name.getTypeface(), Typeface.BOLD);
            name.setTextSize(13);
            constraintLayout.addView(name);

            speakingHint = new TextView(context);
            speakingHint.setId(View.generateViewId());
            speakingHint.setTypeface(speakingHint.getTypeface(), Typeface.BOLD);
            speakingHint.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            speakingHint.setGravity(Gravity.CENTER);
            speakingHint.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
            speakingHint.setTextColor(ContextCompat.getColor(context, R.color.default_black));
            speakingHint.setVisibility(View.GONE);
            speakingHint.setPadding(10, 5, 10, 5);
            constraintLayout.addView(speakingHint);

            reorder = new ImageView(context);
            reorder.setId(View.generateViewId());
            reorder.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            reorder.setBackgroundResource(R.drawable.reorder);
            constraintLayout.addView(reorder);


            ConstraintSet numberConstraintSet = new ConstraintSet();
            numberConstraintSet.clone(constraintLayout);
            numberConstraintSet.connect(number.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 20);
            numberConstraintSet.connect(number.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 20);
            numberConstraintSet.connect(number.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 20);
            numberConstraintSet.applyTo(constraintLayout);

            ConstraintSet nameConstraintSet = new ConstraintSet();
            nameConstraintSet.clone(constraintLayout);
            nameConstraintSet.connect(name.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 20);
            nameConstraintSet.connect(name.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 20);
            nameConstraintSet.connect(name.getId(), ConstraintSet.LEFT, number.getId(), ConstraintSet.RIGHT, 50);
            nameConstraintSet.applyTo(constraintLayout);

            ConstraintSet speakingHintConstraintSet = new ConstraintSet();
            speakingHintConstraintSet.clone(constraintLayout);
            speakingHintConstraintSet.connect(speakingHint.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 20);
            speakingHintConstraintSet.connect(speakingHint.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 20);
            speakingHintConstraintSet.connect(speakingHint.getId(), ConstraintSet.RIGHT, reorder.getId(), ConstraintSet.LEFT, 50);
            speakingHintConstraintSet.applyTo(constraintLayout);

            ConstraintSet reorderConstraintSet = new ConstraintSet();
            reorderConstraintSet.clone(constraintLayout);
            reorderConstraintSet.connect(reorder.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 20);
            reorderConstraintSet.connect(reorder.getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 20);
            reorderConstraintSet.connect(reorder.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 20);
            reorderConstraintSet.applyTo(constraintLayout);
        }

        public void setParticipation(Participation participation) {
            this.participation = participation;
        }

        public TextView getNumber() {
            return this.number;
        }

        public TextView getName() {
            return this.name;
        }

        public TextView getSpeakingHint() {
            return this.speakingHint;
        }

        public ImageView getReorder() {
            return this.reorder;
        }

        @Override
        public void onItemSelected() {

        }

        @Override
        public void onItemClear() {

            this.participationAdapter.changeNumberingAfterOrderChange();

        }

    }

    public interface OnParticipationListener{

        void onParticipationDismiss(Participation participation);
        void changeNumberingAfterOrderChange(ArrayList<Participation> collection);
        Collection<Participation> getCollections();
    }
}
