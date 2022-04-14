package dhbw.smartmoderation.group.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.util.ExceptionHandlingActivity;


public class CreateConsensusLevelActivity extends ExceptionHandlingActivity {

    private EditText nameInput;
    private Button colorInput;
    private EditText descriptionInput;
    private Button saveButton;
    private Bundle extra;
    private int position;
    private int color;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_consensus_level);
        setTitle(getString(R.string.CreateConsensusLevel_title));

        nameInput = findViewById(R.id.consensusLevelNameInput);
        colorInput = findViewById(R.id.consensusLevelColorInput);
        descriptionInput = findViewById(R.id.consensusLevelDescriptionInput);
        saveButton = findViewById(R.id.saveButton);

        Intent intent = getIntent();
        extra = intent.getExtras();
        position = extra.getInt("position");

        if (position != -1) {
            this.color = extra.getInt("color");
            nameInput.setText(extra.getString("name"));
            nameInput.setEnabled(false);
            colorInput.setBackgroundColor(color);
            descriptionInput.setText(extra.getString("description"));
        } else {
            colorInput.setBackgroundColor(getResources().getColor(R.color.colorPrimary, null));
        }

        colorInput.setOnClickListener(v -> {
            ColorDrawable buttonColor = new ColorDrawable(color);
        });

        saveButton.setOnClickListener(v -> {
            if (nameInput.getText().toString().length() > 0) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("position", position);
                resultIntent.putExtra("name", nameInput.getText().toString());

                resultIntent.putExtra("color", color);
                resultIntent.putExtra("description", descriptionInput.getText().toString());
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.noConsensusLevelName));
                builder.setCancelable(false);
                builder.setNeutralButton(getString(R.string.ok), (dialog, which) -> {
                    dialog.cancel();
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
    }
}
