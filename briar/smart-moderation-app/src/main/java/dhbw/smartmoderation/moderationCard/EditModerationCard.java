package dhbw.smartmoderation.moderationCard;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


import androidx.fragment.app.FragmentManager;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.data.model.ModerationCard;
import dhbw.smartmoderation.exceptions.CantEditModerationCardException;
import dhbw.smartmoderation.exceptions.CouldNotDeleteModerationCard;
import dhbw.smartmoderation.exceptions.ModerationCardNotFoundException;
import dhbw.smartmoderation.util.Client;
import petrov.kristiyan.colorpicker.ColorPicker;

public class EditModerationCard{
    private long cardId;
    private int cardColor;
    private int fontColor;
    private String moderationCardContent;
    private AlertDialog alertDialog;
    private EditText moderationCardContentHolder;
    private SurfaceView cardColorViewer;
    private ModerationCardsController controller;
    private Client client;
    private ModerationCardsFragment moderationCardsFragment;

    private final View.OnClickListener pickColorButtonClickListener = v -> {
        ColorPicker colorPicker = new ColorPicker((Activity) v.getContext());
        ModerationCardColorImporter cardColorImporter = ModerationCardColorImporter.getInstance();
        colorPicker.setColors(cardColorImporter.getBackgroundColors());
        colorPicker.setDefaultColorButton(cardColorImporter.getBackgroundColors()[0]);
        colorPicker.setOnFastChooseColorListener(new ColorPicker.OnFastChooseColorListener() {
            @Override
            public void setOnFastChooseColorListener(int position, int color) {
                cardColor = color;
                cardColorViewer.setBackgroundColor(color);
                colorPicker.dismissDialog();
            }

            @Override
            public void onCancel() {
                colorPicker.dismissDialog();
            }
        });
        colorPicker.setColumns(5)
                .show();

    };

    private final View.OnClickListener saveModerationCardClickListener = v -> {
        try {
            moderationCardContent = moderationCardContentHolder.getText().toString();
            ModerationCard moderationCard = controller.editModerationCard(moderationCardContent, cardColor, fontColor, cardId);
            moderationCardsFragment.onResume();
            if(client != null && client.isRunning()) client.updateModerationCard(moderationCard);
        } catch (ModerationCardNotFoundException | CantEditModerationCardException e) {
            e.printStackTrace();
        }
        alertDialog.cancel();
    };

    private final View.OnClickListener deleteModerationCardClickListener = v -> {
        moderationCardContent = moderationCardContentHolder.getText().toString();
        try {
            controller.deleteModerationCard(cardId);
            moderationCardsFragment.onResume();
            if(client != null && client.isRunning()) client.deleteModerationCard(cardId);
        } catch (CouldNotDeleteModerationCard | ModerationCardNotFoundException couldNotDeleteModerationCard) {
            couldNotDeleteModerationCard.printStackTrace();
        }
        alertDialog.cancel();
    };

    public EditModerationCard(ModerationCard moderationCard, ModerationCardsFragment moderationCardsFragment) {
        cardColor = moderationCard.getBackgroundColor();
        fontColor = moderationCard.getFontColor();
        moderationCardContent = moderationCard.getContent();
        cardId = moderationCard.getCardId();
        long meetingId = moderationCard.getMeetingId();
        controller = new ModerationCardsController(meetingId);
        //todo: hand over ipadress and apikey to client
        client = new Client();
        this.moderationCardsFragment = moderationCardsFragment;
        initializePopup(moderationCardsFragment.getContext());
        fillInModerationCardData(moderationCard);
    }


    private void fillInModerationCardData(ModerationCard moderationCard) {
        moderationCardContentHolder.setText(moderationCard.getContent());
        cardColorViewer.setBackgroundColor(moderationCard.getBackgroundColor());
    }


    private void initializePopup(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View popUp = inflater.inflate(R.layout.popup_edit_moderation_card, null);
        moderationCardContentHolder = popUp.findViewById(R.id.moderationCardContent);
        Button pickColorButton = popUp.findViewById(R.id.pickColorButton);
        pickColorButton.setOnClickListener(pickColorButtonClickListener);
        cardColorViewer = popUp.findViewById(R.id.colorViewer);
        Button saveButton = popUp.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(saveModerationCardClickListener);
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
