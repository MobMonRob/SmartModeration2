package dhbw.smartmoderation.consensus.detail;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.text.LineBreaker;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.Collection;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.connection.synchronization.SynchronizableDataType;
import dhbw.smartmoderation.data.model.Poll;
import dhbw.smartmoderation.util.UpdateableExceptionHandlingActivity;

public class ConsensusProposalDetail extends UpdateableExceptionHandlingActivity {

    private TextView titleText;
    private TextView consensusProposalText;
    private TextView notesText;
    private ConsensusProposalDetailController controller;
    private SwipeRefreshLayout pullToRefresh;
    private Poll poll;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consensus_proposal_detail);
        setTitle(getString(R.string.ConsensusProposalDetail_title));
        pullToRefresh = findViewById(R.id.pullToRefresh);

        pullToRefresh.setOnRefreshListener(this::updateUI);

        Intent intent = getIntent();
        Long pollId = intent.getLongExtra("pollId", 0);
        this.titleText = findViewById(R.id.titleText);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            this.titleText.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
        }
        this.consensusProposalText = findViewById(R.id.consensusProposalText);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            this.consensusProposalText.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
        }
        this.notesText = findViewById(R.id.notesText);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            this.notesText.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
        }
        this.controller = new ConsensusProposalDetailController(pollId);
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
        ConsensusProposalDetailAsyncTask consensusProposalDetailAsyncTask = new ConsensusProposalDetailAsyncTask();
        consensusProposalDetailAsyncTask.execute();
    }

    @Override
    public Collection<SynchronizableDataType> getSynchronizableDataTypes() {
        Collection<SynchronizableDataType> dataTypes = new ArrayList<>();
        dataTypes.add(SynchronizableDataType.POLL);
        return dataTypes;
    }

    @SuppressLint("StaticFieldLeak")
    public class ConsensusProposalDetailAsyncTask extends AsyncTask<Object, Exception, String> {
        @Override
        protected String doInBackground(Object... objects) {
            controller.update();
            poll = controller.getPoll();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            initializeTextViews();
            pullToRefresh.setRefreshing(false);
        }
    }
}