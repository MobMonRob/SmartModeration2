package dhbw.smartmoderation.moderationCard.create;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.SmartModerationApplicationImpl;
import dhbw.smartmoderation.data.model.ModerationCard;
import dhbw.smartmoderation.exceptions.CantCreateModerationCardException;
import dhbw.smartmoderation.exceptions.ModerationCardNotFoundException;
import dhbw.smartmoderation.moderationCard.ModerationCardColorImporter;
import dhbw.smartmoderation.moderationCard.overview.ModerationCardsFragment;
import dhbw.smartmoderation.util.Client;
import petrov.kristiyan.colorpicker.ColorPicker;

public class CreateModerationCard {
    private int backgroundColor;
    private int fontColor;
    private AlertDialog alertDialog;
    private EditText moderationCardContentHolder;
    private SurfaceView cardColorViewer;
    private CreateModerationCardController controller;
    private ModerationCardsFragment moderationCardsFragment;
    ModerationCardColorImporter cardColorImporter = ModerationCardColorImporter.getInstance();

    private final View.OnClickListener pickColorButtonClickListener = v -> {
        ColorPicker colorPicker = new ColorPicker((Activity) v.getContext());
        colorPicker.setColors(cardColorImporter.getBackgroundColors());
        colorPicker.setOnFastChooseColorListener(new ColorPicker.OnFastChooseColorListener() {
            @Override
            public void setOnFastChooseColorListener(int position, int color) {
                backgroundColor = color;
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

    private final View.OnClickListener addModerationCardClickListener = v -> {
        try {
            String moderationCardContent = moderationCardContentHolder.getText().toString();
            String cardAuthor = controller.getLocalAuthorName();
            ModerationCard moderationCard = controller.createModerationCard(moderationCardContent, cardAuthor, backgroundColor, fontColor);
            moderationCardsFragment.onResume();
            Client client = ((SmartModerationApplicationImpl) SmartModerationApplicationImpl.getApp()).getClient();
            if(client != null && client.isRunning()) client.addModerationCard(moderationCard);
        } catch (CantCreateModerationCardException | ModerationCardNotFoundException e) {
            e.printStackTrace();
        }
        alertDialog.cancel();
    };


    public CreateModerationCard(ModerationCardsFragment fragment) {
        Intent intent = fragment.requireActivity().getIntent();
        Bundle extra = intent.getExtras();
        long meetingId = extra.getLong("meetingId");
        controller = new CreateModerationCardController(meetingId);
        initializePopup(fragment.getActivity());
        moderationCardsFragment = fragment;
    }

    private void initializePopup(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View popUp = inflater.inflate(R.layout.popup_create_moderation_card, null);
        moderationCardContentHolder = popUp.findViewById(R.id.moderationCardContent);
        Button pickColorButton = popUp.findViewById(R.id.pickColorButton);
        pickColorButton.setOnClickListener(pickColorButtonClickListener);
        //set default colors
        backgroundColor = cardColorImporter.getBackgroundColors()[0];
        fontColor = cardColorImporter.getFontColor(backgroundColor);
        cardColorViewer = popUp.findViewById(R.id.colorViewer);
        cardColorViewer.setBackgroundColor(backgroundColor);
        Button addButton = popUp.findViewById(R.id.addButton);
        addButton.setOnClickListener(addModerationCardClickListener);
        Button cancelButton = popUp.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(view -> alertDialog.cancel());
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(popUp);
        alertDialog = builder.create();
    }

    public void show() {
        alertDialog.show();
    }
}
