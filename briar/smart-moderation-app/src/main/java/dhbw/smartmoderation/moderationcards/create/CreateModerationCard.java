package dhbw.smartmoderation.moderationcards.create;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pes.androidmaterialcolorpickerdialog.ColorPicker;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.exceptions.CantCreateModerationCardException;
import dhbw.smartmoderation.exceptions.ModerationCardNotFoundException;
import dhbw.smartmoderation.moderationcards.overview.ModerationCardsController;

public class CreateModerationCard extends Fragment {
    private Button pickColorButton;
    private SurfaceView cardColorViewer;
    private int cardColor;
    private View popUp;
    private CreateModerationCardController createModerationCardController;
    private EditText moderationCardContentHolder;
    private String moderationCardContent;
    private AlertDialog addModerationCardDialog;

    private final View.OnClickListener pickColorButtonClickListener = v -> {
        ColorPicker colorPicker = new ColorPicker(getActivity());
        colorPicker.show();
        colorPicker.enableAutoClose();
        colorPicker.setCallback(color->{
            cardColor = color;
            cardColorViewer.setBackgroundColor(cardColor);
        });
    };
    private final View.OnClickListener addModerationCardClickListener = v ->{
        try {
            moderationCardContent = moderationCardContentHolder.getText().toString();
            createModerationCardController.createModerationCard(moderationCardContent,cardColor);
        } catch (CantCreateModerationCardException | ModerationCardNotFoundException e) {
            e.printStackTrace();
        }
        addModerationCardDialog.cancel();
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getActivity().getIntent();
        Bundle extra = intent.getExtras();
        long meetingId = extra.getLong("meetingId");
        createModerationCardController = new CreateModerationCardController(meetingId);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        popUp = inflater.inflate(R.layout.popup_create_moderation_card, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(this.popUp);
        addModerationCardDialog = builder.create();
        pickColorButton = this.popUp.findViewById(R.id.pickColorButton);
        pickColorButton.setOnClickListener(pickColorButtonClickListener);
        cardColorViewer = this.popUp.findViewById(R.id.colorViewer);
        moderationCardContentHolder = this.popUp.findViewById(R.id.moderationCardContent);
        Button addButton = this.popUp.findViewById(R.id.addButton);
        addButton.setOnClickListener(addModerationCardClickListener);
        Button cancelButton = this.popUp.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener( view -> addModerationCardDialog.cancel());
        addModerationCardDialog.show();
        return popUp;
    }
}
