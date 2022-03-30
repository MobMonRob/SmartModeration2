package dhbw.smartmoderation.moderationCard;

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
import android.widget.PopupWindow;

import androidx.fragment.app.FragmentActivity;


import dhbw.smartmoderation.R;
import dhbw.smartmoderation.exceptions.CantCreateModerationCardException;
import dhbw.smartmoderation.exceptions.ModerationCardNotFoundException;
import petrov.kristiyan.colorpicker.ColorPicker;

public class CreateModerationCard extends PopupWindow {
    private int cardColor;
    private String moderationCardContent = "";
    private AlertDialog alertDialog;
    private EditText moderationCardContentHolder;
    private SurfaceView cardColorViewer;
    private ModerationCardsController controller;
    private ModerationCardsFragment moderationCardsFragment;
    ModerationCardColorImporter cardColorImporter = ModerationCardColorImporter.getInstance();


    private final View.OnClickListener pickColorButtonClickListener = v -> {
        ColorPicker colorPicker = new ColorPicker((Activity) v.getContext());
        colorPicker.setColors(cardColorImporter.getBackgroundColors());
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

    private final View.OnClickListener addModerationCardClickListener = v -> {
        try {
            moderationCardContent = moderationCardContentHolder.getText().toString();
            controller.createModerationCard(moderationCardContent, cardColor);
            moderationCardsFragment.onResume();
        } catch (CantCreateModerationCardException | ModerationCardNotFoundException e) {
            e.printStackTrace();
        }
        alertDialog.cancel();
    };


    public CreateModerationCard(ModerationCardsFragment fragment) {
        Intent intent = fragment.getActivity().getIntent();
        Bundle extra = intent.getExtras();
        long meetingId = extra.getLong("meetingId");
        controller = new ModerationCardsController(meetingId);
        initializePopup(fragment.getActivity());
        moderationCardsFragment = fragment;
    }

    private void initializePopup(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View popUp = inflater.inflate(R.layout.popup_create_moderation_card, null);
        moderationCardContentHolder = popUp.findViewById(R.id.moderationCardContent);
        Button pickColorButton = popUp.findViewById(R.id.pickColorButton);
        pickColorButton.setOnClickListener(pickColorButtonClickListener);
        //set default color
        cardColor = cardColorImporter.getBackgroundColors()[0];
        cardColorViewer = popUp.findViewById(R.id.colorViewer);
        cardColorViewer.setBackgroundColor(cardColor);
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
