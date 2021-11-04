package dhbw.smartmoderation.consensus.evaluate;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.connection.synchronization.SynchronizableDataType;
import dhbw.smartmoderation.consensus.create.CreateConsensusProposal;
import dhbw.smartmoderation.consensus.result.ConsensusProposalResult;
import dhbw.smartmoderation.data.model.ConsensusLevel;
import dhbw.smartmoderation.data.model.Poll;
import dhbw.smartmoderation.data.model.Status;
import dhbw.smartmoderation.data.model.Voice;
import dhbw.smartmoderation.exceptions.CantSendVoiceException;
import dhbw.smartmoderation.uiUtils.SimpleItemTouchHelperCallback;
import dhbw.smartmoderation.util.UpdateableExceptionHandlingActivity;

public class EvaluateConsensusProposal extends UpdateableExceptionHandlingActivity {

    private TextView title;
    private TextView consensusProposal;
    private RecyclerView consensusLevelList;
    private ConsensusLevelAdapter consensusLevelAdapter;
    private LinearLayoutManager consensusLevelLayoutManager;
    private EditText description;
    private Button sendButton;
    private EvaluateConsensusProposalController controller;
    private Poll poll;
    private ItemTouchHelper itemTouchHelper;
    private boolean isPrefilled;
    private Long voiceId;
    private ProgressDialog progressDialog;
    private SwipeRefreshLayout pullToRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evaluate_consensus_proposal);

        pullToRefresh = findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(() -> {

            updateUI();

        });

        Intent intent = getIntent();

        Long pollId = intent.getLongExtra("pollId", 0);

        if(intent.hasExtra("activity")) {

            if(intent.getStringExtra("activity").equals("ConsensusProposalResult")) {
                this.isPrefilled = true;
                this.voiceId = intent.getLongExtra("voiceId", 0);
            }
        }

        this.controller = new EvaluateConsensusProposalController(pollId);
        this.poll = this.controller.getPoll();

        this.title = findViewById(R.id.consensusProposalTitle);
        this.consensusProposal = findViewById(R.id.consensusProposal);
        this.consensusLevelList = findViewById(R.id.consensusLevelList);
        this.description = findViewById(R.id.descriptionInput);
        this.sendButton = findViewById(R.id.sendButton);

        this.consensusLevelLayoutManager = new LinearLayoutManager(this);
        this.consensusLevelList.setLayoutManager(this.consensusLevelLayoutManager);
        this.consensusLevelAdapter = new ConsensusLevelAdapter(this, this.controller);
        this.consensusLevelList.setAdapter(this.consensusLevelAdapter);
        DividerItemDecoration consensusLevelDividerItemDecoration = new DividerItemDecoration(consensusLevelList.getContext(), consensusLevelLayoutManager .getOrientation());
        this.consensusLevelList.addItemDecoration(consensusLevelDividerItemDecoration);

        this.sendButton.setOnClickListener(this::onSendVoice);

        String title = getString(R.string.EvaluateConsensusProposal_title) + " (" + this.controller.getVoiceCount() + "/" + this.controller.getVoteMembersCount() + ")";
        setTitle(title);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(consensusLevelAdapter, R.color.default_black, R.drawable.hand, ItemTouchHelper.END);
        this.itemTouchHelper = new ItemTouchHelper(callback);
        this.itemTouchHelper.attachToRecyclerView(consensusLevelList);
        if(isPrefilled) {
            prefillRecyclerViewAndEditText();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();

    }

    public void prefillRecyclerViewAndEditText() {

        Voice voice = this.poll.getVoice(this.voiceId);
        int position = voice.getConsensusLevel().getNumber() - 1;
        this.consensusLevelAdapter.setSelectedPosition(position);
        this.consensusLevelAdapter.notifyDataSetChanged();
        this.description.setText(voice.getExplanation());

        if(this.poll.getStatus(this.controller.getMember()) == Status.ABGESCHLOSSEN) {

            this.consensusLevelAdapter.setSelectionDisabled(true);
            this.consensusLevelAdapter.notifyDataSetChanged();
            this.sendButton.setVisibility(View.GONE);
            this.description.setEnabled(false);
        }

        else {

            this.sendButton.setText(getString(R.string.ChangeEvaluation));

        }


    }

    public void onSendVoice(View view) {

        if(this.consensusLevelAdapter.getSelectedPosition() == -1) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.necessaryToChoseConsensusLevel));
            builder.setCancelable(false);
            builder.setNeutralButton(getString(R.string.ok), ((dialog, which) -> dialog.cancel()));
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            return;
        }

        if(this.sendButton.getText() == getText(R.string.ChangeEvaluation)) {

            EvaluateConsensusProposalAsyncTask evaluateConsensusProposalAsyncTask = new EvaluateConsensusProposalAsyncTask("sendVoice");
            evaluateConsensusProposalAsyncTask.execute("change", this.consensusLevelAdapter.getSelectedConsensusLevel(),  this.description.getText().toString(), this.poll.getVoice(voiceId));

        }

        else{

            EvaluateConsensusProposalAsyncTask evaluateConsensusProposalAsyncTask = new EvaluateConsensusProposalAsyncTask("sendVoice");
            evaluateConsensusProposalAsyncTask.execute("new", this.consensusLevelAdapter.getSelectedConsensusLevel(),  this.description.getText().toString());

        }
    }

    @Override
    public Collection<SynchronizableDataType> getSynchronizableDataTypes() {
        Collection<SynchronizableDataType> dataTypes = new ArrayList<>();
        dataTypes.add(SynchronizableDataType.POLL);
        dataTypes.add(SynchronizableDataType.CONSENSUSLEVEL);
        return dataTypes;
    }

    @Override
    protected void updateUI() {

        EvaluateConsensusProposalAsyncTask evaluateConsensusProposalAsyncTask = new EvaluateConsensusProposalAsyncTask("update");
        evaluateConsensusProposalAsyncTask.execute();

    }

    public class EvaluateConsensusProposalAsyncTask extends AsyncTask<Object, Exception, String> {

        String flag;

        public EvaluateConsensusProposalAsyncTask(String flag) {

            this.flag = flag;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if(flag.equals("sendVoice")) {

                progressDialog = new ProgressDialog(EvaluateConsensusProposal.this, R.style.MyAlertDialogStyle);
                progressDialog.setMessage(getString(R.string.creating_consensusProposal));
                progressDialog.setCancelable(false);
                progressDialog.show();

            }
        }

        @Override
        protected void onProgressUpdate(Exception... values) {
            super.onProgressUpdate(values);
            progressDialog.dismiss();
            handleException(values[0]);
        }

        @Override
        protected String doInBackground(Object... objects) {

            switch(flag) {

                case "update":
                    controller.update();
                    poll = controller.getPoll();
                    break;

                case "sendVoice":

                    String mode = objects[0].toString();
                    ConsensusLevel consensusLevel = (ConsensusLevel)objects[1];
                    String description = objects[2].toString();

                    try {

                        if(mode == "change") {

                            Voice voice = (Voice)objects[3];
                            controller.createVoice(voice, consensusLevel, description);
                        }

                        else {

                            controller.createVoice(null, consensusLevel, description);
                        }


                    } catch(CantSendVoiceException exception){

                        publishProgress(exception);
                    }
                    break;
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            switch(flag) {

                case "update":
                    consensusLevelAdapter.updateConsensusLevelList(controller.getConsensusLevels());
                    title.setText(poll.getTitle());
                    consensusProposal.setText(poll.getConsensusProposal());
                    pullToRefresh.setRefreshing(false);
                    break;

                case "sendVoice":
                    progressDialog.dismiss();
                    Intent intent = new Intent(EvaluateConsensusProposal.this, ConsensusProposalResult.class);
                    intent.putExtra("pollId", poll.getPollId());
                    finish();
                    startActivity(intent);
                    break;
            }
        }
    }
}