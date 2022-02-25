package dhbw.smartmoderation.moderationcards.overview;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
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
import dhbw.smartmoderation.consensus.create.CreateConsensusProposal;
import dhbw.smartmoderation.exceptions.CantCreateModerationCardException;
import dhbw.smartmoderation.exceptions.ModerationCardNotFoundException;
import dhbw.smartmoderation.group.create.CreateGroup;
import dhbw.smartmoderation.moderationcards.create.CreateModerationCard;
import dhbw.smartmoderation.moderationcards.create.CreateModerationCardController;

public class ModerationCardsFragment extends Fragment {
    private View view;
    private ModerationCardsController controller;
    private FloatingActionButton addButton;

    private final View.OnClickListener addButtonClickListener = v -> onAddModerationCard();

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

    private void onAddModerationCard(){
        Intent createModerationCardIntent = new Intent(getActivity(), CreateModerationCard.class);
        Bundle extra = createModerationCardIntent.getExtras();
        long meetingId = extra.getLong("meetingId");
        createModerationCardIntent.putExtra("meetingId", meetingId);
        startActivity(createModerationCardIntent);
    }
}
