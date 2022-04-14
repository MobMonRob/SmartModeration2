package dhbw.smartmoderation.group.settings;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.data.model.ConsensusLevel;
import dhbw.smartmoderation.uiUtils.ItemTouchHelperAdapter;
import dhbw.smartmoderation.uiUtils.ItemTouchHelperViewHolder;
import dhbw.smartmoderation.uiUtils.OnStartDragListener;
import dhbw.smartmoderation.util.Util;

public class ConsensusLevelAdapter extends RecyclerView.Adapter<ConsensusLevelAdapter.ConsensusLevelViewHolder> implements ItemTouchHelperAdapter {

    private Context context;
    public ArrayList<ConsensusLevel> consensusLevelList;
    private final OnStartDragListener onStartDragListener;
    private OnConsensusLevelListener onConsensusLevelListener;

    public ConsensusLevelAdapter(Context context, Collection<ConsensusLevel> consensusLevels, OnStartDragListener onStartDragListener, OnConsensusLevelListener onConsensusLevelListener) {
        this.context = context;
        this.consensusLevelList = new ArrayList<>();
        this.onStartDragListener = onStartDragListener;
        this.onConsensusLevelListener = onConsensusLevelListener;

        updateConsensusLevelList(consensusLevels);
    }

    public void updateConsensusLevelList(Collection<ConsensusLevel> consensusLevels) {
        int previousSize = consensusLevelList.size();
        this.consensusLevelList.clear();
        this.consensusLevelList.addAll(consensusLevels);

        Collections.sort(this.consensusLevelList, ((o1, o2) -> {
            if (o1.getNumber() < o2.getNumber())
                return -1;
            else if (o1.getNumber() > o2.getNumber())
                return 1;
            return 0;
        }));

        this.notifyDataSetChanged();

        if (consensusLevels.size() > previousSize) {
            notifyItemRangeInserted(0, consensusLevelList.size());
        }
    }

    @NonNull
    @Override
    public ConsensusLevelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ConstraintLayout constraintLayout = new ConstraintLayout(context);
        constraintLayout.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, 200));
        ConsensusLevelViewHolder consensusLevelViewHolder = new ConsensusLevelViewHolder(constraintLayout, context, this);
        return consensusLevelViewHolder;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull ConsensusLevelViewHolder holder, int position) {

        ConsensusLevel consensusLevel = consensusLevelList.get(position);
        holder.setConsensusLevel(consensusLevel);

        String numbering = Util.toRoman(consensusLevel.getNumber());
        holder.getTextViewNumbering().setText(numbering);

        String name = consensusLevel.getName();
        holder.getTextViewName().setText(name);

        int color = consensusLevel.getColor();
        holder.getColorDrawable().getPaint().setColor(color);

        holder.getReorder().setOnTouchListener((v, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                onStartDragListener.onStartDrag(holder);
            }
            return false;
        });


        holder.getLaunch().setOnClickListener(v -> {
            if (context instanceof SettingsActivity) {
                ((SettingsActivity) context).startCreateConsensusLevelActivity(v, consensusLevelList.get(position), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return consensusLevelList.size();
    }

    public Context getContext() {
        return this.context;
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {

        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++)
                Collections.swap(consensusLevelList, i, i + 1);
        } else {
            for (int i = fromPosition; i > toPosition; i--)
                Collections.swap(consensusLevelList, i, i - 1);
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemDismiss(int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getString(R.string.consensusproposal_reaffirmation_dynamic, consensusLevelList.get(position).getName()));
        builder.setNegativeButton(context.getString(R.string.yes), ((dialog, which) -> {
            Long consensusLevelId = consensusLevelList.get(position).getConsensusLevelId();
            consensusLevelList.remove(position);
            notifyItemRemoved(position);
            onConsensusLevelListener.onConsensusLevelDismiss(consensusLevelId);
            changeNumberingAfterOrderChange();
        }));

        builder.setPositiveButton(context.getString(R.string.no), ((dialog, which) -> {
            dialog.cancel();
            ((SettingsActivity) context).reloadConsensusLevelItemTouchHelper();
        }));

        android.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void changeNumberingAfterOrderChange() {
        onConsensusLevelListener.changeNumberingAfterOrderChange(consensusLevelList);
    }

    public Collection<ConsensusLevel> getConsensusLevelList() {
        return consensusLevelList;
    }

    static class ConsensusLevelViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

        private TextView numbering;
        private TextView name;
        private ImageView color;
        private ShapeDrawable rectangle;
        private ImageView launch;
        private ImageView reorder;
        private ConsensusLevel consensusLevel;
        private ConsensusLevelAdapter consensusLevelAdapter;
        private Context Context;

        ConsensusLevelViewHolder(ConstraintLayout constraintLayout, Context context, ConsensusLevelAdapter consensusLevelAdapter) {
            super(constraintLayout);
            this.consensusLevelAdapter = consensusLevelAdapter;
            this.Context = context;

            numbering = new TextView(context);
            numbering.setLayoutParams(new ViewGroup.LayoutParams(80, ViewGroup.LayoutParams.WRAP_CONTENT));
            numbering.setId(View.generateViewId());
            numbering.setTextSize(15);
            numbering.setTypeface(numbering.getTypeface(), Typeface.BOLD);
            constraintLayout.addView(numbering);

            name = new TextView(context);
            name.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            name.setId(View.generateViewId());
            name.setTextSize(15);
            name.setTypeface(name.getTypeface(), Typeface.BOLD);
            constraintLayout.addView(name);

            color = new ImageView(context);
            color.setId(View.generateViewId());
            color.setLayoutParams(new ViewGroup.LayoutParams(75, 75));
            rectangle = new ShapeDrawable(new RectShape());
            rectangle.setIntrinsicHeight(25);
            rectangle.setIntrinsicWidth(25);
            color.setImageDrawable(rectangle);
            constraintLayout.addView(color);

            launch = new ImageView(context);
            launch.setId(View.generateViewId());
            launch.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            launch.setBackgroundResource(R.drawable.launch);
            constraintLayout.addView(launch);

            reorder = new ImageView(context);
            reorder.setId(View.generateViewId());
            reorder.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            reorder.setBackgroundResource(R.drawable.reorder);
            constraintLayout.addView(reorder);

            ConstraintSet numberingConstraintSet = new ConstraintSet();
            numberingConstraintSet.clone(constraintLayout);
            numberingConstraintSet.connect(numbering.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 20);
            numberingConstraintSet.connect(numbering.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 40);
            numberingConstraintSet.connect(numbering.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 20);
            numberingConstraintSet.applyTo(constraintLayout);

            ConstraintSet nameConstraintSet = new ConstraintSet();
            nameConstraintSet.clone(constraintLayout);
            nameConstraintSet.connect(name.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 20);
            nameConstraintSet.connect(name.getId(), ConstraintSet.LEFT, numbering.getId(), ConstraintSet.RIGHT, 30);
            nameConstraintSet.connect(name.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 20);
            nameConstraintSet.applyTo(constraintLayout);

            ConstraintSet colorConstraintSet = new ConstraintSet();
            colorConstraintSet.clone(constraintLayout);
            colorConstraintSet.connect(color.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 20);
            colorConstraintSet.connect(color.getId(), ConstraintSet.RIGHT, reorder.getId(), ConstraintSet.LEFT, 20);
            colorConstraintSet.connect(color.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 20);
            colorConstraintSet.applyTo(constraintLayout);

            ConstraintSet launchConstraintSet = new ConstraintSet();
            launchConstraintSet.clone(constraintLayout);
            launchConstraintSet.connect(launch.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 20);
            launchConstraintSet.connect(launch.getId(), ConstraintSet.RIGHT, reorder.getId(), ConstraintSet.LEFT, 20);
            launchConstraintSet.connect(launch.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 20);
            launchConstraintSet.applyTo(constraintLayout);

            ConstraintSet reorderConstraintSet = new ConstraintSet();
            reorderConstraintSet.clone(constraintLayout);
            reorderConstraintSet.connect(reorder.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 20);
            reorderConstraintSet.connect(reorder.getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 40);
            reorderConstraintSet.connect(reorder.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 20);
            reorderConstraintSet.applyTo(constraintLayout);
        }

        public void setConsensusLevel(ConsensusLevel consensusLevel) {
            this.consensusLevel = consensusLevel;
        }

        public TextView getTextViewNumbering() {
            return this.numbering;
        }

        public TextView getTextViewName() {
            return this.name;
        }

        public ShapeDrawable getColorDrawable() {
            return this.rectangle;
        }

        public ImageView getLaunch() {
            return this.launch;
        }

        public ImageView getReorder() {
            return this.reorder;
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Context.getColor(R.color.default_drag_color));
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
            consensusLevelAdapter.changeNumberingAfterOrderChange();
        }
    }

    public interface OnConsensusLevelListener {

        void onConsensusLevelDismiss(Long consensusLevelId);

        void changeNumberingAfterOrderChange(ArrayList<ConsensusLevel> collection);
    }
}
