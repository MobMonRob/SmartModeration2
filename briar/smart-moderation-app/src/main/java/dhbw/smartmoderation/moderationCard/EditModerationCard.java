package dhbw.smartmoderation.moderationCard;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.pes.androidmaterialcolorpickerdialog.ColorPicker;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.data.model.ModerationCard;
import dhbw.smartmoderation.exceptions.CantCreateModerationCardException;
import dhbw.smartmoderation.exceptions.ModerationCardNotFoundException;

public class EditModerationCard {
    AlertDialog alertDialog;
    View popUp;
    LayoutInflater inflater;
    Button addButton;
    Button cancelButton;
    Button pickColorButton;
    EditText moderationCardContentHolder;
    SurfaceView cardColorViewer;
    ModerationCardsController controller;
    int cardColor = Color.BLACK;
    String moderationCardContent = "";

    private final View.OnClickListener pickColorButtonClickListener = v -> {
        ColorPicker colorPicker = new ColorPicker((Activity) v.getContext());
        colorPicker.show();
        colorPicker.enableAutoClose();
        colorPicker.setCallback(color -> {
            cardColor = color;
            cardColorViewer.setBackgroundColor(cardColor);
        });
    };
    //todo: edit not add
    private final View.OnClickListener addModerationCardClickListener = v -> {
        try {
            moderationCardContent = moderationCardContentHolder.getText().toString();
            controller.createModerationCard(moderationCardContent, cardColor);
        } catch (CantCreateModerationCardException | ModerationCardNotFoundException e) {
            e.printStackTrace();
        }
        alertDialog.cancel();
    };
    //todo: delete clicklistener
    public EditModerationCard(ModerationCard moderationCard, Context context) {
        initializePopup(context);
        fillInModerationCardData(moderationCard);
        cardColor = moderationCard.getColor();
        moderationCardContent = moderationCard.getContent();
    }


    public EditModerationCard(Context context) {
        initializePopup(context);
    }

    private void fillInModerationCardData(ModerationCard moderationCard) {
        moderationCardContentHolder.setText(moderationCard.getContent());
        cardColorViewer.setBackgroundColor(moderationCard.getColor());
    }


    private void initializePopup(Context context) {
        inflater = LayoutInflater.from(context);
        popUp = inflater.inflate(R.layout.popup_create_moderation_card, null);
        moderationCardContentHolder = popUp.findViewById(R.id.moderationCardContent);
        pickColorButton = popUp.findViewById(R.id.pickColorButton);
        pickColorButton.setOnClickListener(pickColorButtonClickListener);
        cardColorViewer = popUp.findViewById(R.id.colorViewer);
        addButton = popUp.findViewById(R.id.addButton);
        addButton.setOnClickListener(addModerationCardClickListener);
        cancelButton = popUp.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(view -> alertDialog.cancel());

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(popUp);
        alertDialog = builder.create();

    }

    public void show() {
        alertDialog.show();
    }
}
