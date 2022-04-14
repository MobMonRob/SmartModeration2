package dhbw.smartmoderation.consensus.evaluate;

import android.content.Intent;
import android.graphics.text.LineBreaker;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.util.ExceptionHandlingActivity;

public class CardActivity extends ExceptionHandlingActivity {

    private ConstraintLayout cardConstraintLayout;
    private TextView name;
    private TextView description;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getSupportActionBar().hide();
        setContentView(R.layout.activity_card);

        Intent intent = getIntent();
        Bundle extra = intent.getExtras();
        String name = extra.getString("name");
        String description = extra.getString("description");
        int color = extra.getInt("color");

        this.name = findViewById(R.id.consensusLevelName);
        this.name.setText(name);
        this.description = findViewById(R.id.consensusLevelDescription);
        this.description.setText(description);
        this.description.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
        this.cardConstraintLayout = findViewById(R.id.cardConstraintLayout);
        this.cardConstraintLayout.setBackgroundColor(color);

    }
}