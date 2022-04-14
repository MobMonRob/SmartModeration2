package dhbw.smartmoderation.consensus.result;

import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import dhbw.smartmoderation.R;
import dhbw.smartmoderation.data.model.ConsensusLevel;
import dhbw.smartmoderation.data.model.Voice;

public class LegendTableFragment extends Fragment {

    private LinearLayout tableContainer;
    private TableLayout legendTable;
    private Collection<Voice> voices;
    private View view;
    private ConsensusProposalResultController controller;
    private HashMap<String, TableRow> tableRows;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        this.view = inflater.inflate(R.layout.fragment_legend_table, container, false);
        this.voices = ((ConsensusProposalResult)getActivity()).getController().getVoices();
        this.tableContainer = this.view.findViewById(R.id.tableContainer);
        this.controller = ((ConsensusProposalResult) getActivity()).getController();
        this.tableRows = new HashMap<>();

        this.legendTable = new TableLayout(getActivity());
        TableLayout.LayoutParams layoutParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(20, 20, 20, 20);
        this.legendTable.setLayoutParams(layoutParams);
        this.tableContainer.addView(this.legendTable);
        return this.view;
    }

    public HashMap<String, TableRow> getTableRows() {
        return this.tableRows;
    }

    public void update(){
        this.legendTable.removeAllViews();
        createTableHeader();
        createLegendTable();
    }

    private void createTableHeader() {

        TableRow header = new TableRow(getActivity());
        header.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

        TextView legend = new TextView(getActivity());
        TableRow.LayoutParams legendLayoutParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.7f);
        legend.setLayoutParams(legendLayoutParams);
        legend.setTypeface(legend.getTypeface(), Typeface.BOLD);
        legend.setGravity(Gravity.CENTER);
        legend.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.rectangle_bottom_border_thick));
        legend.setText(getString(R.string.legend));
        header.addView(legend);

        TextView absoluteNumber = new TextView(getActivity());
        TableRow.LayoutParams absoluteNumberLayoutParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.65f);
        absoluteNumber.setLayoutParams(absoluteNumberLayoutParams);
        absoluteNumber.setTypeface(absoluteNumber.getTypeface(), Typeface.BOLD);
        absoluteNumber.setGravity(Gravity.CENTER);
        absoluteNumber.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.rectangle_bottom_border_thick));
        absoluteNumber.setText(getString(R.string.count));
        header.addView(absoluteNumber);

        TextView percentage = new TextView(getActivity());
        TableRow.LayoutParams percentageLayoutParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.65f);
        percentage.setLayoutParams(percentageLayoutParams);
        percentage.setTypeface(percentage.getTypeface(), Typeface.BOLD);
        percentage.setGravity(Gravity.CENTER);
        percentage.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.rectangle_bottom_border_thick));
        percentage.setText(getString(R.string.participationInPercentage));
        header.addView(percentage);

        this.legendTable.addView(header);
    }

    private void createLegendTable() {

        for (Map.Entry<Long, Integer> entry : this.controller.getCountPerConsensusLevel().entrySet()) {

            ConsensusLevel consensusLevel = this.controller.getConsensusLevel(entry.getKey());
            TableRow row = new TableRow(getActivity());
            row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

            row.addView(createLegendLayout(consensusLevel.getName(), consensusLevel.getColor()));
            row.addView(createAmountTextView(entry.getValue()));
            row.addView(createProportionTextView(entry.getValue()));

            row.setOnClickListener(((ConsensusProposalResult)getActivity()).tableRowClickListener);
            this.legendTable.addView(row);
            this.tableRows.put(consensusLevel.getName(), row);
        }

        TableRow row = new TableRow(getActivity());
        row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        row.addView(createLegendLayout(getString(R.string.novoteTaken), ContextCompat.getColor(getActivity(), R.color.default_grey)));
        int amount = this.controller.getVoteMembersCount() - this.controller.getVoiceCount();
        row.addView(createAmountTextView(amount));
        row.addView(createProportionTextView(amount));
        row.setOnClickListener(((ConsensusProposalResult)getActivity()).tableRowClickListener);
        this.legendTable.addView(row);
        this.tableRows.put(getString(R.string.novoteTaken), row);
    }

    public ConstraintLayout createLegendLayout(String name, int color) {

        ConstraintLayout constraintLayout = new ConstraintLayout(getActivity());
        constraintLayout.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 1.7f));

        ImageView colorView = new ImageView(getActivity());
        colorView.setId(View.generateViewId());
        colorView.setLayoutParams(new TableRow.LayoutParams(50,50));
        ShapeDrawable rectangle = new ShapeDrawable(new RectShape());
        rectangle.setIntrinsicHeight(50);
        rectangle.setIntrinsicWidth(50);
        rectangle.getPaint().setColor(color);
        colorView.setImageDrawable(rectangle);
        constraintLayout.addView(colorView);

        ConstraintSet colorConstraintSet = new ConstraintSet();
        colorConstraintSet.clone(constraintLayout);
        colorConstraintSet.connect(colorView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 10);
        colorConstraintSet.connect(colorView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 10);
        colorConstraintSet.connect(colorView.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 10);
        colorConstraintSet.applyTo(constraintLayout);

        TextView nameView = new TextView(getActivity());
        nameView.setId(View.generateViewId());
        TableRow.LayoutParams nameLayoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT);
        nameView.setLayoutParams(nameLayoutParams);
        nameView.setGravity(Gravity.CENTER);
        nameView.setText(name);
        nameView.setSingleLine(true);
        constraintLayout.addView(nameView);

        ConstraintSet nameConstraintSet = new ConstraintSet();
        nameConstraintSet.clone(constraintLayout);
        nameConstraintSet.connect(nameView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 10);
        nameConstraintSet.connect(nameView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 10);
        nameConstraintSet.connect(nameView.getId(), ConstraintSet.LEFT, colorView.getId(), ConstraintSet.RIGHT, 20);
        nameConstraintSet.applyTo(constraintLayout);

        constraintLayout.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.rectangle_bottom_border));

        return constraintLayout;
    }

    public TextView createAmountTextView(int value) {
        TextView amount = new TextView(getActivity());
        TableRow.LayoutParams amountLayoutParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 0.65f);
        amount.setLayoutParams(amountLayoutParams);
        amount.setGravity(Gravity.CENTER);
        amount.setText(String.valueOf(value));
        amount.setSingleLine(true);
        amount.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.rectangle_bottom_border));
        return amount;
    }

    public TextView createProportionTextView(int value) {
        float percentage = ((float)value/(float)this.controller.getVoteMembersCount())*100;
        DecimalFormat decimalFormat = new DecimalFormat("#.##");

        TextView proportion = new TextView (getActivity());
        TableRow.LayoutParams proportionLayoutParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 0.65f);
        proportion.setLayoutParams(proportionLayoutParams);
        proportion.setGravity(Gravity.CENTER);
        proportion.setText(decimalFormat.format(percentage));
        proportion.setSingleLine(true);
        proportion.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.rectangle_bottom_border));
        return proportion;
    }

    @Override
    public void onResume() {
        super.onResume();
        createTableHeader();
        createLegendTable();
    }
}
