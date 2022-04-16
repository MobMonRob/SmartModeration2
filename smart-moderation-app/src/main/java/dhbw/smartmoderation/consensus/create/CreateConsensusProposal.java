package dhbw.smartmoderation.consensus.create;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.exceptions.CantSendConsensusProposal;
import dhbw.smartmoderation.util.ExceptionHandlingActivity;

public class CreateConsensusProposal extends ExceptionHandlingActivity {

    private final int TITLE_LENGTH = 25;
    private final int CONSENSUS_PROPOSAL_LENGTH = 250;
    private final int NOTES_LENGTH = 250;
    private EditText titleInput;
    private EditText consensusProposalInput;
    private EditText notesInput;
    private TextView titleCount;
    private TextView consensusProposalCount;
    private TextView notesCount;
    private CreateConsensusProposalController controller;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_consensus_proposal);
        setTitle(getString(R.string.CreateConsensusProposal_title));
        Intent intent = getIntent();
        Bundle extra = intent.getExtras();
        Long meetingId = extra.getLong("meetingId");
        this.titleInput = findViewById(R.id.titleInput);

        this.titleInput.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(TITLE_LENGTH)
        });

        this.titleInput.addTextChangedListener(titleTextWatcher);
        this.consensusProposalInput = findViewById(R.id.consensusProposalInput);

        this.consensusProposalInput.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(CONSENSUS_PROPOSAL_LENGTH)
        });

        this.consensusProposalInput.addTextChangedListener(consensusProposalTextWatcher);
        this.notesInput = findViewById(R.id.notesInput);

        this.notesInput.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(NOTES_LENGTH)
        });

        this.notesInput.addTextChangedListener(noteTextWatcher);
        this.titleCount = findViewById(R.id.titleCount);
        this.consensusProposalCount = findViewById(R.id.consensusProposalCount);
        this.notesCount = findViewById(R.id.notesCount);
        Button createButton = findViewById(R.id.createButton);
        createButton.setOnClickListener(createListener);
        this.controller = new CreateConsensusProposalController(meetingId);
    }

    private final View.OnClickListener createListener = v -> {
        if (this.titleInput.getText().toString().length() > 0 && this.consensusProposalInput.getText().toString().length() > 0) {
            CreateConsensusProposalAsyncTask createConsensusProposalAsyncTask = new CreateConsensusProposalAsyncTask();
            createConsensusProposalAsyncTask.execute(titleInput.getText().toString(), consensusProposalInput.getText().toString(), notesInput.getText().toString());
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.titleAndConsensusproposalCantBeEmpty));
            builder.setCancelable(false);
            builder.setNeutralButton(getString(R.string.ok), ((dialog, which) -> dialog.cancel()));
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    };

    private final TextWatcher titleTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String charCount = "(" + titleInput.getText().toString().length() + "/" + TITLE_LENGTH + ")";
            titleCount.setText(charCount);
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    private final TextWatcher consensusProposalTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String charCount = "(" + consensusProposalInput.getText().toString().length() + "/" + CONSENSUS_PROPOSAL_LENGTH + ")";
            consensusProposalCount.setText(charCount);
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    private final TextWatcher noteTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String charCount = "(" + notesInput.getText().toString().length() + "/" + NOTES_LENGTH + ")";
            notesCount.setText(charCount);
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    @SuppressLint("StaticFieldLeak")
    public class CreateConsensusProposalAsyncTask extends AsyncTask<Object, Exception, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(CreateConsensusProposal.this, R.style.MyAlertDialogStyle);
            progressDialog.setMessage(getString(R.string.creating_consensusProposal));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Object... objects) {
            String title = objects[0].toString();
            String consensusProposal = objects[1].toString();
            String notes = objects[2].toString();

            try {
                controller.createConsensusProposal(title, consensusProposal, notes);
            } catch (CantSendConsensusProposal exception) {
                publishProgress(exception);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Exception... values) {
            super.onProgressUpdate(values);
            progressDialog.dismiss();
            handleException(values[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.dismiss();
            Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.consensusProposal_created), Toast.LENGTH_SHORT);
            toast.show();
            setResult(Activity.RESULT_OK);
            finish();
        }
    }
}