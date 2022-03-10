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
import dhbw.smartmoderation.exceptions.CantEditModerationCardException;
import dhbw.smartmoderation.exceptions.CouldNotDeleteModerationCard;
import dhbw.smartmoderation.exceptions.ModerationCardNotFoundException;

public class EditModerationCard {
    private long cardId;
    private int cardColor;
    private String moderationCardContent;
    private AlertDialog alertDialog;
    private EditText moderationCardContentHolder;
    private SurfaceView cardColorViewer;
    private ModerationCardsController controller;

    private final View.OnClickListener pickColorButtonClickListener = v -> {
        ColorPicker colorPicker = new ColorPicker((Activity) v.getContext());
        colorPicker.show();
        colorPicker.enableAutoClose();
        colorPicker.setCallback(color -> {
            cardColor = color;
            cardColorViewer.setBackgroundColor(cardColor);
        });
    };

    private final View.OnClickListener editModerationCardClickListener = v -> {
        try {
            moderationCardContent = moderationCardContentHolder.getText().toString();
            controller.editModerationCard(moderationCardContent, cardColor, cardId);
        } catch (ModerationCardNotFoundException | CantEditModerationCardException e) {
            e.printStackTrace();
        }
        alertDialog.cancel();
    };

    private final View.OnClickListener deleteModerationCardClickListener = v -> {
        moderationCardContent = moderationCardContentHolder.getText().toString();
        try {
            controller.deleteModerationCard(cardId);
        } catch (CouldNotDeleteModerationCard | ModerationCardNotFoundException couldNotDeleteModerationCard) {
            couldNotDeleteModerationCard.printStackTrace();
        }
        alertDialog.cancel();
    };

    public EditModerationCard(ModerationCard moderationCard, Context context) {
        cardColor = moderationCard.getColor();
        moderationCardContent = moderationCard.getContent();
        cardId = moderationCard.getCardId();
        long meetingId = moderationCard.getMeetingId();
        controller = new ModerationCardsController(meetingId);
        initializePopup(context);
        fillInModerationCardData(moderationCard);
    }


    private void fillInModerationCardData(ModerationCard moderationCard) {
        moderationCardContentHolder.setText(moderationCard.getContent());
        cardColorViewer.setBackgroundColor(moderationCard.getColor());
    }


    private void initializePopup(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View popUp = inflater.inflate(R.layout.popup_edit_moderation_card, null);
        moderationCardContentHolder = popUp.findViewById(R.id.moderationCardContent);
        Button pickColorButton = popUp.findViewById(R.id.pickColorButton);
        pickColorButton.setOnClickListener(pickColorButtonClickListener);
        cardColorViewer = popUp.findViewById(R.id.colorViewer);
        Button saveButton = popUp.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(editModerationCardClickListener);
        Button cancelButton = popUp.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(view -> alertDialog.cancel());
        Button deleteButton = popUp.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(deleteModerationCardClickListener);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(popUp);
        alertDialog = builder.create();
    }

    public void show() {
        alertDialog.show();
    }
}
