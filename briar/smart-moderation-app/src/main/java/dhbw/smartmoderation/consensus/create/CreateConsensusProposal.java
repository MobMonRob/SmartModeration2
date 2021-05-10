package dhbw.smartmoderation.consensus.create;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.connection.synchronization.PullEvent;
import dhbw.smartmoderation.connection.synchronization.SynchronizableDataType;
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


    private Button createButton;
    private Long meetingId;
    private CreateConsensusProposalController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_consensus_proposal);
        setTitle(getString(R.string.CreateConsensusProposal_title));

        Intent intent = getIntent();
        Bundle extra = intent.getExtras();
        this.meetingId = extra.getLong("meetingId");

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

        this.createButton = findViewById(R.id.createButton);
        this.createButton.setOnClickListener(createListener);

        this.controller = new CreateConsensusProposalController(this.meetingId);
    }

    private final View.OnClickListener createListener = v -> {
        if (this.titleInput.getText().toString().length() > 0 && this.consensusProposalInput.getText().toString().length() > 0)
                try {
                    this.controller.createConsensusProposal(this.titleInput.getText().toString(), this.consensusProposalInput.getText().toString(), this.notesInput.getText().toString());
                    setResult(Activity.RESULT_OK);
                    finish();
                } catch (CantSendConsensusProposal exception) {
                    handleException(exception);
                }

        else {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.titleAndConsensusproposalCantBeEmpty));
            builder.setCancelable(false);
            builder.setNeutralButton(getString(R.string.ok), ((dialog, which) -> {
                dialog.cancel();
            }));

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
}