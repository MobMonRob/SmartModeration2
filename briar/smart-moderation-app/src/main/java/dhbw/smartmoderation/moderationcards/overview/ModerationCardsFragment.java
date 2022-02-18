package dhbw.smartmoderation.moderationcards.overview;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pes.androidmaterialcolorpickerdialog.ColorPicker;

import dhbw.smartmoderation.R;

public class ModerationCardsFragment extends Fragment {
    private View view;
    private ModerationCardsController controller;
    private FloatingActionButton addButton;
    private Button pickColorButton;
    private SurfaceView cardColorViewer;
    private int cardColor;
    private View popUp;


    private final View.OnClickListener pickColorButtonClickListener = v -> {
        ColorPicker colorPicker = new ColorPicker(getActivity());
        colorPicker.show();
        colorPicker.enableAutoClose();
        colorPicker.setCallback(color->{
            cardColor = color;
            cardColorViewer.setBackgroundColor(cardColor);
        });
    };

    private final View.OnClickListener addButtonClickListener = v -> {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        popUp = inflater.inflate(R.layout.popup_create_moderation_card, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(this.popUp);
        AlertDialog alertDialog = builder.create();

        pickColorButton = this.popUp.findViewById(R.id.pickColorButton);
        pickColorButton.setOnClickListener(pickColorButtonClickListener);
        cardColorViewer = this.popUp.findViewById(R.id.colorViewer);
        Button addButton = this.popUp.findViewById(R.id.addButton);
        addButton.setOnClickListener(view ->{
            //todo: get parameters for method addModerationCard()
            controller.addModerationCard();
            alertDialog.cancel();
        });
        Button cancelButton = this.popUp.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener( view -> alertDialog.cancel());
        alertDialog.show();
    };


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        controller = new ModerationCardsController();
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        this.view = inflater.inflate(R.layout.fragment_moderation_cards, container, false);
        getActivity().setTitle(getString(R.string.moderationCardTitle));
        addButton = this.view.findViewById(R.id.floatingActionButton);
        this.addButton.setOnClickListener(addButtonClickListener);
        return this.view;
    }
}
