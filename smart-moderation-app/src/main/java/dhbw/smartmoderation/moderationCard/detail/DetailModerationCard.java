package dhbw.smartmoderation.moderationCard.detail;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.SmartModerationApplicationImpl;
import dhbw.smartmoderation.data.model.ModerationCard;
import dhbw.smartmoderation.exceptions.CantEditModerationCardException;
import dhbw.smartmoderation.exceptions.CouldNotDeleteModerationCard;
import dhbw.smartmoderation.exceptions.MeetingNotFoundException;
import dhbw.smartmoderation.exceptions.ModerationCardNotFoundException;
import dhbw.smartmoderation.moderationCard.ModerationCardColorImporter;
import dhbw.smartmoderation.moderationCard.overview.ModerationCardsFragment;
import dhbw.smartmoderation.util.Client;
import dhbw.smartmoderation.util.ExceptionHandlingActivity;
import petrov.kristiyan.colorpicker.ColorPicker;

public class DetailModerationCard {
    private long cardId;
    private String cardAuthor;
    private String moderationCardContent;
    private int backgroundColor;
    private int fontColor;
    private AlertDialog alertDialog;
    private EditText moderationCardContentHolder;
    private SurfaceView cardColorViewer;
    private DetailModerationCardController controller;
    private ModerationCardsFragment moderationCardsFragment;
    private final View.OnClickListener pickColorButtonClickListener = v -> {
        ColorPicker colorPicker = new ColorPicker((Activity) v.getContext());
        ModerationCardColorImporter cardColorImporter = ModerationCardColorImporter.getInstance();
        colorPicker.setColors(cardColorImporter.getBackgroundColors());
        colorPicker.setDefaultColorButton(cardColorImporter.getBackgroundColors()[0]);
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

    private final View.OnClickListener saveModerationCardClickListener = v -> {
        try {
            moderationCardContent = moderationCardContentHolder.getText().toString();
            if (!moderationCardContent.isEmpty()) {
                controller.editModerationCard(moderationCardContent, cardAuthor, backgroundColor, fontColor, cardId);
                moderationCardsFragment.onResume();
            } else {
                createErrorDialog();
            }
        } catch (ModerationCardNotFoundException | CantEditModerationCardException | MeetingNotFoundException e) {
            ((ExceptionHandlingActivity) moderationCardsFragment.requireActivity()).handleException(e);
        }
        alertDialog.cancel();
    };

    private final View.OnClickListener deleteModerationCardClickListener = v -> {
        moderationCardContent = moderationCardContentHolder.getText().toString();
        try {
            controller.deleteModerationCard(cardId);
            moderationCardsFragment.onResume();
        } catch (CouldNotDeleteModerationCard | ModerationCardNotFoundException | MeetingNotFoundException e) {
            ((ExceptionHandlingActivity) moderationCardsFragment.requireActivity()).handleException(e);
        }
        alertDialog.cancel();
    };

    public DetailModerationCard(ModerationCard moderationCard, ModerationCardsFragment moderationCardsFragment) {
        backgroundColor = moderationCard.getBackgroundColor();
        fontColor = moderationCard.getFontColor();
        moderationCardContent = moderationCard.getContent();
        cardId = moderationCard.getCardId();
        cardAuthor = moderationCard.getAuthor();
        long meetingId = moderationCard.getMeetingId();
        controller = new DetailModerationCardController(meetingId);
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

    private void createErrorDialog() {
        Context context = moderationCardsFragment.getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        assert context != null;
        builder.setMessage(context.getString(R.string.allFieldsMustBeFilled));
        builder.setCancelable(false);
        builder.setNeutralButton(context.getString(R.string.ok), (dialog, which) -> {
            dialog.cancel();
            this.alertDialog.show();
        });
        AlertDialog errorDialog = builder.create();
        errorDialog.show();
    }

    public void show() {
        alertDialog.show();
    }
}
