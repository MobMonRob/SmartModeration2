package dhbw.smartmoderation.moderationcards.overview;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import dhbw.smartmoderation.R;

public class ModerationCardsFragment extends Fragment {
    private View view;
    private ModerationCardsController controller;
    private FloatingActionButton addButton;
    private View popUp;

    private View.OnClickListener addButtonClickListener = v -> {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        popUp = inflater.inflate(R.layout.popup_create_moderation_card, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(this.popUp);
        AlertDialog alertDialog = builder.create();
        Button addButton = this.popUp.findViewById(R.id.addButton);
        addButton.setOnClickListener(view ->{
            //todo: get parameters for method addModerationCard()
            controller.addModerationCard();
        });
        Button cancelButton = this.popUp.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener( view -> alertDialog.cancel());
        alertDialog.show();
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
