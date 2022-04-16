package dhbw.smartmoderation.consensus.evaluate;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
import dhbw.smartmoderation.data.model.ConsensusLevel;
import dhbw.smartmoderation.uiUtils.ItemTouchHelperAdapter;
import dhbw.smartmoderation.util.Util;

public class ConsensusLevelAdapter extends RecyclerView.Adapter<ConsensusLevelAdapter.ConsensusLevelViewHolder> implements ItemTouchHelperAdapter {

    private Context context;
    private EvaluateConsensusProposalController controller;
    private ArrayList<ConsensusLevel> consensusLevelList;
    private int selectedPosition = -1;
    private boolean selectionDisabled;

    public ConsensusLevelAdapter(Context context, EvaluateConsensusProposalController controller) {
        this.context = context;
        this.controller = controller;
        this.consensusLevelList = new ArrayList<>();
        Collection<ConsensusLevel> consensusLevels = this.controller.getConsensusLevels();
        this.updateConsensusLevelList(consensusLevels);
    }

    public void updateConsensusLevelList(Collection<ConsensusLevel> consensusLevels) {
        this.consensusLevelList.clear();
        this.consensusLevelList.addAll(consensusLevels);

        Collections.sort(this.consensusLevelList, ((o1, o2) -> {
            if (o1.getNumber() < o2.getNumber()) {
                return -1;
            } else if (o1.getNumber() > o2.getNumber()) {
                return 1;
            }
            return 0;
        }));

        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ConsensusLevelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ConstraintLayout constraintLayout = new ConstraintLayout(context);
        constraintLayout.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, 130));
        ConsensusLevelViewHolder consensusLevelViewHolder = new ConsensusLevelViewHolder(constraintLayout, context, this);
        return consensusLevelViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ConsensusLevelViewHolder holder, int position) {
        ConsensusLevel consensusLevel = this.consensusLevelList.get(position);
        holder.setConsensusLevel(consensusLevel);
        holder.getNumber().setText(Util.toRoman(consensusLevel.getNumber()));
        holder.getTitle().setText(consensusLevel.getName());
        holder.getConstraintLayout().setBackgroundColor(consensusLevel.getColor());
        holder.onBind = true;
        holder.getSelectCheckBox().setChecked(selectedPosition == position);
        holder.onBind = false;
        if (selectionDisabled) holder.getSelectCheckBox().setEnabled(false);
    }

    @Override
    public int getItemCount() {
        return this.consensusLevelList.size();
    }

    public Context getContext() {
        return this.context;
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    public ConsensusLevel getSelectedConsensusLevel() {
        return this.consensusLevelList.get(this.selectedPosition);
    }

    public int getSelectedPosition() {
        return this.selectedPosition;
    }

    public void setSelectionDisabled(boolean selectionDisabled) {
        this.selectionDisabled = selectionDisabled;
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
    }

    @Override
    public void onItemDismiss(int position) {
        ConsensusLevel consensusLevel = this.consensusLevelList.get(position);
        Intent intent = new Intent(this.context, CardActivity.class);
        intent.putExtra("name", consensusLevel.getName());
        intent.putExtra("description", consensusLevel.getDescription());
        intent.putExtra("color", consensusLevel.getColor());
        this.context.startActivity(intent);
    }

    static class ConsensusLevelViewHolder extends RecyclerView.ViewHolder implements CheckBox.OnCheckedChangeListener {
        private ConsensusLevelAdapter consensusLevelAdapter;
        private ConsensusLevel consensusLevel;
        private ConstraintLayout constraintLayout;
        private TextView number;
        private TextView title;
        private CheckBox selectCheckBox;
        public boolean onBind;

        public ConsensusLevelViewHolder(ConstraintLayout constraintLayout, Context context, ConsensusLevelAdapter consensusLevelAdapter) {
            super(constraintLayout);
            this.consensusLevelAdapter = consensusLevelAdapter;
            this.constraintLayout = constraintLayout;
            number = new TextView(context);
            number.setLayoutParams(new ViewGroup.LayoutParams(80, ViewGroup.LayoutParams.WRAP_CONTENT));
            number.setId(View.generateViewId());
            number.setTextSize(12);
            number.setTypeface(number.getTypeface(), Typeface.BOLD);
            constraintLayout.addView(number);
            title = new TextView(context);
            title.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            title.setId(View.generateViewId());
            title.setTextSize(12);
            title.setTypeface(title.getTypeface(), Typeface.BOLD);
            constraintLayout.addView(title);
            selectCheckBox = new CheckBox(context);
            selectCheckBox.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            selectCheckBox.setId(View.generateViewId());
            selectCheckBox.setButtonTintList(ColorStateList.valueOf(ResourcesCompat.getColor(context.getResources(), R.color.default_black, null)));
            constraintLayout.addView(selectCheckBox);
            selectCheckBox.setOnCheckedChangeListener(this);

            ConstraintSet numberConstraintSet = new ConstraintSet();
            numberConstraintSet.clone(constraintLayout);
            numberConstraintSet.connect(number.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 20);
            numberConstraintSet.connect(number.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 20);
            numberConstraintSet.connect(number.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 20);
            numberConstraintSet.applyTo(constraintLayout);

            ConstraintSet titleConstraintSet = new ConstraintSet();
            titleConstraintSet.clone(constraintLayout);
            titleConstraintSet.connect(title.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 20);
            titleConstraintSet.connect(title.getId(), ConstraintSet.LEFT, number.getId(), ConstraintSet.RIGHT, 30);
            titleConstraintSet.connect(title.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 20);
            titleConstraintSet.applyTo(constraintLayout);

            ConstraintSet checkBoxConstraintSet = new ConstraintSet();
            checkBoxConstraintSet.clone(constraintLayout);
            checkBoxConstraintSet.connect(selectCheckBox.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 20);
            checkBoxConstraintSet.connect(selectCheckBox.getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 20);
            checkBoxConstraintSet.connect(selectCheckBox.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 20);
            checkBoxConstraintSet.applyTo(constraintLayout);
        }

        public void setConsensusLevel(ConsensusLevel consensusLevel) {
            this.consensusLevel = consensusLevel;
        }

        public ConsensusLevel getConsensusLevel() {
            return this.consensusLevel;
        }

        public TextView getNumber() {
            return this.number;
        }

        public TextView getTitle() {
            return this.title;
        }

        public CheckBox getSelectCheckBox() {
            return this.selectCheckBox;
        }

        public ConstraintLayout getConstraintLayout() {
            return this.constraintLayout;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!onBind) {
                this.consensusLevelAdapter.setSelectedPosition(getAdapterPosition());
                this.consensusLevelAdapter.notifyDataSetChanged();
            }
        }
    }

}
