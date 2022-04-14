package dhbw.smartmoderation.consensus.overview;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import dhbw.smartmoderation.R;
import dhbw.smartmoderation.consensus.evaluate.EvaluateConsensusProposal;
import dhbw.smartmoderation.consensus.result.ConsensusProposalResult;
import dhbw.smartmoderation.data.model.Poll;
import dhbw.smartmoderation.data.model.Status;

public class PollAdapter extends RecyclerView.Adapter<PollAdapter.PollViewHolder> {

    private Context context;
    private ConsensusProposalOverviewController controller;
    private ArrayList<Poll> pollList;
    private RecyclerView recyclerView;
    private PollViewHolder pollViewHolder;

    public PollAdapter(Context context, ConsensusProposalOverviewController controller) {
        this.context = context;
        this.controller = controller;
        this.pollList = new ArrayList<>();
        Collection<Poll> polls = this.controller.getPolls();
        updatePollList(polls);
    }

    public ArrayList<Poll> getPollList() {

        return this.pollList;
    }

    public void updatePollList(Collection<Poll> polls) {
        this.pollList.clear();
        this.pollList.addAll(polls);

        Collections.sort(this.pollList, (o1, o2) -> {

            if (o1.getStatus(this.controller.getMember()).getNumber() < o2.getStatus(this.controller.getMember()).getNumber()) {
                return -1;
            }

            else if (o1.getStatus(this.controller.getMember()).getNumber() > o2.getStatus(this.controller.getMember()).getNumber()) {
                return 1;
            }

            return 0;
        });

        this.notifyDataSetChanged();
    }

    private final View.OnClickListener onClickListener = v -> {

        int position = this.recyclerView.getChildLayoutPosition(v);
        Poll poll = this.pollList.get(position);

        Class activity = null;

        if(poll.getStatus(this.controller.getMember()) == Status.BEWERTET || poll.getStatus(this.controller.getMember()) == Status.ABGESCHLOSSEN || poll.getStatus(this.controller.getMember()) == Status.DEAKTIVIERT) {

            activity = ConsensusProposalResult.class;
        }

        else if(poll.getStatus(this.controller.getMember()) == Status.OFFEN ) {

            activity = EvaluateConsensusProposal.class;

        }

        else if (poll.getStatus(this.controller.getMember()) == Status.ANGELEGT ) {

            this.createAlertDialog(context.getString(R.string.ConsensusProposalIsNotUnlocked));
        }

        if(activity != null) {

            Intent intent = new Intent(context, activity);
            intent.putExtra("pollId", poll.getPollId());
            intent.putExtra("activity", "ConsensusProposalOverview");
            context.startActivity(intent);
        }
    };

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {

        this.recyclerView = recyclerView;
    }

    @NonNull
    @Override
    public PollViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ConstraintLayout constraintLayout = new ConstraintLayout(context);
        constraintLayout.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, 160));

        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, value, true);
        constraintLayout.setBackgroundResource(value.resourceId);

        pollViewHolder = new PollViewHolder(constraintLayout, context, this);
        pollViewHolder.itemView.setOnClickListener(onClickListener);
        return pollViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PollViewHolder holder, int position) {
        Poll poll = this.pollList.get(position);
        holder.setPoll(poll);
        holder.getTitle().setText(poll.getTitle());

        Status status = poll.getStatus(this.controller.getMember());
        if(status == Status.OFFEN) {
            holder.getStatus().setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.colorPrimaryDark, null));
            holder.getStatus().setText(context.getString(R.string.offen));
        }

        else if(status == Status.DEAKTIVIERT) {
            holder.getStatus().setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.transparent, null));
            holder.getStatus().setText(context.getString(R.string.deaktiviert));
        }
        else if(status == Status.ANGELEGT ) {
            holder.getStatus().setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.transparent, null));
            holder.getStatus().setText(context.getString(R.string.angelegt));
        }

        else if(status == Status.BEWERTET) {
            holder.getStatus().setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.default_green, null));
            holder.getStatus().setText(context.getString(R.string.bewertet));
        }

        else  {
            holder.getStatus().setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.default_red, null));
            holder.getStatus().setText(context.getString(R.string.abgeschlossen));
        }

    }

    @Override
    public int getItemCount() {
        return this.pollList.size();
    }

    public Context getContext() {
        return this.context;
    }

    public void createAlertDialog(String message) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setNeutralButton(context.getString(R.string.ok), ((dialog, which) -> dialog.cancel()));
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    static class PollViewHolder extends RecyclerView.ViewHolder {

        private PollAdapter pollAdapter;
        private Poll poll;
        private TextView title;
        private TextView status;

        public PollViewHolder(ConstraintLayout constraintLayout, Context context, PollAdapter pollAdapter) {
            super(constraintLayout);
            this.pollAdapter = pollAdapter;

            title = new TextView(context);
            title.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            title.setId(View.generateViewId());
            title.setTextSize(12);
            title.setTypeface(title.getTypeface(), Typeface.BOLD);
            constraintLayout.addView(title);

            status = new TextView(context);
            status.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            status.setId(View.generateViewId());
            status.setTextSize(12);
            status.setTypeface(status.getTypeface(), Typeface.BOLD);
            status.setGravity(Gravity.CENTER);
            status.setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.colorPrimaryDark, null));
            status.setText(context.getString(R.string.open));
            status.setEms(7);
            constraintLayout.addView(status);

            ConstraintSet titleConstraintSet = new ConstraintSet();
            titleConstraintSet.clone(constraintLayout);
            titleConstraintSet.connect(title.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 20);
            titleConstraintSet.connect(title.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 20);
            titleConstraintSet.connect(title.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 20);
            titleConstraintSet.applyTo(constraintLayout);

            ConstraintSet statusConstraintSet = new ConstraintSet();
            statusConstraintSet.clone(constraintLayout);
            statusConstraintSet.connect(status.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 20);
            statusConstraintSet.connect(status.getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 20);
            statusConstraintSet.connect(status.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 20);
            statusConstraintSet.applyTo(constraintLayout);
        }

        public void setPoll(Poll poll) {
            this.poll = poll;
        }

        public Poll getPoll() {
            return this.poll;
        }

        public TextView getTitle() {
            return this.title;
        }

        public TextView getStatus() {
            return this.status;
        }

    }
}
