package dhbw.smartmoderation.consensus.result;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableRow;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.connection.synchronization.SynchronizableDataType;
import dhbw.smartmoderation.consensus.evaluate.EvaluateConsensusProposal;
import dhbw.smartmoderation.data.model.Member;
import dhbw.smartmoderation.data.model.Status;
import dhbw.smartmoderation.exceptions.PollCantBeClosedException;
import dhbw.smartmoderation.util.UpdateableExceptionHandlingActivity;

public class ConsensusProposalResult extends UpdateableExceptionHandlingActivity {

    private ConsensusProposalResultController controller;
    private LegendTableFragment legendTableFragment;
    private ResultTableFragment resultTableFragment;
    private ChartFragment chartFragment;
    private TableRow selectedTableRow;
    private Long pollId;
    private SwipeRefreshLayout pullToRefresh;
    private final View.OnClickListener endButtonClickListener = v -> {
        ConsensusProposalResultAsyncTask consensusProposalResultAsyncTask = new ConsensusProposalResultAsyncTask("closePoll");
        consensusProposalResultAsyncTask.execute();
    };

    private final View.OnClickListener showButtonClickListener = v -> {
        Intent intent = new Intent(this, EvaluateConsensusProposal.class);
        intent.putExtra("pollId", this.pollId);
        intent.putExtra("activity", "ConsensusProposalResult");
        intent.putExtra("voiceId", this.controller.getVoiceFromLocalAuthor().getVoiceId());
        startActivity(intent);
    };

    public OnChartValueSelectedListener chartValueSelectedListener = new OnChartValueSelectedListener() {

        @Override
        public void onValueSelected(Entry e, Highlight h) {

            HashMap<String, TableRow> tableRows = legendTableFragment.getTableRows();

            String name = ((PieEntry) e).getLabel();

            if (tableRows.containsKey(name)) {
                for (TableRow tableRow : tableRows.values()) {
                    tableRow.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.default_color));
                }
                Objects.requireNonNull(tableRows.get(name)).setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.light_grey));
                selectedTableRow = tableRows.get(name);
            }
        }

        @Override
        public void onNothingSelected() {
            HashMap<String, TableRow> tableRows = legendTableFragment.getTableRows();

            for (TableRow tableRow : tableRows.values()) {
                tableRow.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.default_color));
            }
            selectedTableRow = null;
        }
    };

    public View.OnClickListener tableRowClickListener = v -> {
        PieChart donutChart = this.chartFragment.getDonutChart();
        if (v.equals(selectedTableRow)) {
            donutChart.highlightValue(0, -1, false);
            selectedTableRow.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.default_color));
            selectedTableRow = null;
            return;
        }

        for (TableRow tableRow : legendTableFragment.getTableRows().values()) {
            tableRow.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.default_color));
        }

        List<PieEntry> pieEntries = donutChart.getData().getDataSetByIndex(0).getEntriesForXValue(0);
        selectedTableRow = (TableRow) v;
        selectedTableRow.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.light_grey));

        for (Map.Entry<String, TableRow> entry : legendTableFragment.getTableRows().entrySet()) {
            if (entry.getValue().equals(selectedTableRow)) {
                for (PieEntry pieEntry : pieEntries) {
                    if (pieEntry.getLabel().equals(entry.getKey())) {
                        donutChart.highlightValue(pieEntries.indexOf(pieEntry), 0, false);
                    }
                }
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consensus_proposal_result);
        setTitle(getString(R.string.consensusproposalResultFragement_title));

        pullToRefresh = findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(this::updateUI);

        Intent intent = getIntent();
        this.pollId = intent.getLongExtra("pollId", 0);
        this.controller = new ConsensusProposalResultController(pollId);

        this.resultTableFragment = new ResultTableFragment();
        this.legendTableFragment = new LegendTableFragment();
        this.chartFragment = new ChartFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.resultTableFragment, this.resultTableFragment);
        transaction.replace(R.id.legendTableFragment, this.legendTableFragment);
        transaction.replace(R.id.chartFragment, this.chartFragment);
        transaction.commit();
        createButtonPanel();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    public ConsensusProposalResultController getController() {
        return this.controller;
    }

    public void createEndButton(ConstraintLayout constraintLayout, float bias) {

        Button endButton = new Button(this);
        endButton.setId(View.generateViewId());
        endButton.setTextSize(10);
        endButton.setTextColor(ContextCompat.getColor(this, R.color.default_color));
        endButton.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        endButton.setTypeface(endButton.getTypeface(), Typeface.BOLD);
        endButton.setText(getString(R.string.end_evaluation));
        GradientDrawable gradientDrawable = (GradientDrawable) ContextCompat.getDrawable(this, R.drawable.rounded);
        assert gradientDrawable != null;
        gradientDrawable.setColor(ContextCompat.getColor(this, R.color.default_red));
        endButton.setBackground(gradientDrawable);
        endButton.setOnClickListener(endButtonClickListener);
        constraintLayout.addView(endButton);

        ConstraintSet endButtonConstraintSet = new ConstraintSet();
        endButtonConstraintSet.clone(constraintLayout);
        endButtonConstraintSet.connect(endButton.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        endButtonConstraintSet.connect(endButton.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        endButtonConstraintSet.connect(endButton.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        endButtonConstraintSet.connect(endButton.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        endButtonConstraintSet.setHorizontalBias(endButton.getId(), bias);
        endButtonConstraintSet.applyTo(constraintLayout);
    }

    public void createShowButton(ConstraintLayout constraintLayout, float bias) {

        Button showButton = new Button(this);
        showButton.setId(View.generateViewId());
        showButton.setTextSize(10);
        showButton.setTextColor(ContextCompat.getColor(this, R.color.default_color));
        showButton.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        showButton.setTypeface(showButton.getTypeface(), Typeface.BOLD);
        showButton.setText(getString(R.string.show_evaluation));
        GradientDrawable gradientDrawable = (GradientDrawable) ContextCompat.getDrawable(this, R.drawable.rounded);
        assert gradientDrawable != null;
        gradientDrawable.setColor(ContextCompat.getColor(this, R.color.colorPrimary));
        showButton.setBackground(gradientDrawable);
        showButton.setOnClickListener(showButtonClickListener);
        constraintLayout.addView(showButton);

        ConstraintSet showButtonConstraintSet = new ConstraintSet();
        showButtonConstraintSet.clone(constraintLayout);
        showButtonConstraintSet.connect(showButton.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        showButtonConstraintSet.connect(showButton.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        showButtonConstraintSet.connect(showButton.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        showButtonConstraintSet.connect(showButton.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        showButtonConstraintSet.setHorizontalBias(showButton.getId(), bias);

        showButtonConstraintSet.applyTo(constraintLayout);
        showButton.getId();
    }

    public void createButtonPanel() {

        ConstraintLayout buttonPanel = findViewById(R.id.buttons);
        buttonPanel.removeAllViews();
        Member currentMember = this.controller.getMemberFromLocalAuthor();

        if (this.controller.isLocalAuthorModerator()) {
            if (currentMember.hasPermissionToVote(this.controller.getPoll().getMeeting().getGroup())) {
                if (this.controller.getPoll().getStatus(currentMember) == Status.ABGESCHLOSSEN && controller.getVoiceFromLocalAuthor() != null) {
                    createShowButton(buttonPanel, 0.5f);
                } else {
                    createShowButton(buttonPanel, 0.25f);
                    createEndButton(buttonPanel, 0.75f);
                }
            } else {
                if (this.controller.getPoll().getStatus(currentMember) != Status.ABGESCHLOSSEN) {
                    createEndButton(buttonPanel, 0.5f);
                }
            }
        } else {
            if (currentMember.hasPermissionToVote(this.controller.getPoll().getMeeting().getGroup()) && controller.getVoiceFromLocalAuthor() != null) {
                createShowButton(buttonPanel, 0.5f);
            }
        }
    }

    @Override
    public Collection<SynchronizableDataType> getSynchronizableDataTypes() {
        Collection<SynchronizableDataType> dataTypes = new ArrayList<>();
        dataTypes.add(SynchronizableDataType.VOICE);
        dataTypes.add(SynchronizableDataType.CONSENSUSLEVEL);
        dataTypes.add(SynchronizableDataType.POLL);
        return dataTypes;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void updateUI() {
        ConsensusProposalResultAsyncTask consensusProposalResultAsyncTask = new ConsensusProposalResultAsyncTask("update");
        consensusProposalResultAsyncTask.execute();
    }

    @SuppressLint("StaticFieldLeak")
    public class ConsensusProposalResultAsyncTask extends AsyncTask<Object, Exception, String> {

        String flag;

        public ConsensusProposalResultAsyncTask(String flag) {
            this.flag = flag;
        }

        @Override
        protected void onProgressUpdate(Exception... values) {
            super.onProgressUpdate(values);
            handleException(values[0]);
        }

        @Override
        protected String doInBackground(Object... objects) {
            switch (flag) {
                case "update":
                    controller.update();
                    break;
                case "closePoll":
                    try {
                        controller.closePoll();
                    } catch (PollCantBeClosedException exception) {
                        publishProgress(exception);
                    }
                    break;
            }
            return null;
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            switch (flag) {
                case "update":
                    createButtonPanel();
                    chartFragment.createDonutChart();
                    legendTableFragment.update();
                    resultTableFragment.update();
                    pullToRefresh.setRefreshing(false);
                    break;
                case "closePoll":
                    createButtonPanel();
                    break;
            }
        }
    }
}
