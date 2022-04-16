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

import java.util.Objects;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.util.ExceptionHandlingActivity;

public class CardActivity extends ExceptionHandlingActivity {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Objects.requireNonNull(this.getSupportActionBar()).hide();
        setContentView(R.layout.activity_card);
        Intent intent = getIntent();
        Bundle extra = intent.getExtras();
        String name = extra.getString("name");
        String description = extra.getString("description");
        int color = extra.getInt("color");
        TextView name1 = findViewById(R.id.consensusLevelName);
        name1.setText(name);
        TextView description1 = findViewById(R.id.consensusLevelDescription);
        description1.setText(description);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            description1.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
        }
        ConstraintLayout cardConstraintLayout = findViewById(R.id.cardConstraintLayout);
        cardConstraintLayout.setBackgroundColor(color);

    }
}