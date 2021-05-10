package dhbw.smartmoderation.consensus.detail;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.graphics.text.LineBreaker;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.connection.synchronization.PullEvent;
import dhbw.smartmoderation.connection.synchronization.SynchronizableDataType;
import dhbw.smartmoderation.data.model.Poll;
import dhbw.smartmoderation.util.UpdateableExceptionHandlingActivity;

public class ConsensusProposalDetail extends UpdateableExceptionHandlingActivity {

    private TextView titleText;
    private TextView consensusProposalText;
    private TextView notesText;
    private ConsensusProposalDetailController controller;

    private Poll poll;

    private Long pollId;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consensus_proposal_detail);
        setTitle(getString(R.string.ConsensusProposalDetail_title));


        SwipeRefreshLayout pullToRefresh = findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(() -> {

            updateUI();
            pullToRefresh.setRefreshing(false);

        });

        Intent intent = getIntent();
        pollId = intent.getLongExtra("pollId", 0);

        this.titleText = findViewById(R.id.titleText);
        this.titleText.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
        this.consensusProposalText = findViewById(R.id.consensusProposalText);
        this.consensusProposalText.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
        this.notesText = findViewById(R.id.notesText);
        this.notesText.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);

        this.controller = new ConsensusProposalDetailController(this.pollId);
        this.poll = this.controller.getPoll();

        initializeTextViews();
    }

    public void initializeTextViews() {

        this.titleText.setText(this.poll.getTitle());
        this.consensusProposalText.setText(this.poll.getConsensusProposal());
        this.notesText.setText(this.poll.getNote());
    }

    @Override
    protected void updateUI() {

        Handler handler = new Handler();
        handler.post(() ->  controller.update());

        this.poll = controller.getPoll();
        initializeTextViews();
    }

    @Override
    public Collection<SynchronizableDataType> getSynchronizableDataTypes() {
        Collection<SynchronizableDataType> dataTypes = new ArrayList<>();
        dataTypes.add(SynchronizableDataType.POLL);
        return dataTypes;
    }
}