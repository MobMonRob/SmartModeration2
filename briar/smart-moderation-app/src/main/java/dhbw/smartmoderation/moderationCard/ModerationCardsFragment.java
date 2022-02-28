package dhbw.smartmoderation.moderationCard;

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
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pes.androidmaterialcolorpickerdialog.ColorPicker;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.exceptions.CantCreateModerationCardException;
import dhbw.smartmoderation.exceptions.ModerationCardNotFoundException;

public class ModerationCardsFragment extends Fragment {
    private View view;
    private View popUp;
    private ModerationCardsController controller;
    private FloatingActionButton addButton;
    private long meetingId;
    private Button pickColorButton;
    private SurfaceView cardColorViewer;
    private int cardColor;
    private EditText moderationCardContentHolder;
    private String moderationCardContent;
    private AlertDialog alertDialog;
    private ModerationCardAdapter moderationCardAdapter;
    private RecyclerView moderationCardsRecyclerView;
    private final View.OnClickListener pickColorButtonClickListener = v -> {
        ColorPicker colorPicker = new ColorPicker(this.getActivity());
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
            controller.createModerationCard(moderationCardContent,cardColor);
        } catch (CantCreateModerationCardException | ModerationCardNotFoundException e) {
            e.printStackTrace();
        }
        alertDialog.cancel();
    };

    private final View.OnClickListener addButtonClickListener = v -> {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        popUp = inflater.inflate(R.layout.popup_create_moderation_card, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(this.popUp);
        alertDialog = builder.create();
        pickColorButton = popUp.findViewById(R.id.pickColorButton);
        pickColorButton.setOnClickListener(pickColorButtonClickListener);
        cardColorViewer = popUp.findViewById(R.id.colorViewer);
        moderationCardContentHolder = popUp.findViewById(R.id.moderationCardContent);
        Button addButton = popUp.findViewById(R.id.addButton);
        addButton.setOnClickListener(addModerationCardClickListener);
        Button cancelButton = popUp.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(view -> alertDialog.cancel());
        alertDialog.show();
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        this.view = inflater.inflate(R.layout.fragment_moderation_cards, container, false);
        Intent intent = getActivity().getIntent();
        Bundle extra = intent.getExtras();
        this.meetingId = extra.getLong("meetingId");
        controller = new ModerationCardsController(meetingId);
        getActivity().setTitle(getString(R.string.moderationCardTitle));
        addButton = this.view.findViewById(R.id.floatingActionButton);
        this.addButton.setOnClickListener(addButtonClickListener);
        this.moderationCardsRecyclerView = this.view.findViewById(R.id.moderationCardList);
        this.moderationCardAdapter = new ModerationCardAdapter(getActivity(), controller.getAllModerationCards());
        this.moderationCardsRecyclerView.setAdapter(moderationCardAdapter);

        return this.view;
    }


}
