package dhbw.smartmoderation.moderationCard.overview;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.meeting.detail.BaseActivity;
import dhbw.smartmoderation.moderationCard.DesktopLoginQRScanner;
import dhbw.smartmoderation.moderationCard.create.CreateModerationCard;

public class ModerationCardsFragment extends Fragment {
    private ModerationCardsController controller;
    private long meetingId;
    private ModerationCardAdapter moderationCardAdapter;

    public final View.OnClickListener addButtonClickListener = v -> {
        CreateModerationCard createModerationCard = new CreateModerationCard(this);
        createModerationCard.show();
    };

    public final View.OnClickListener loginButtonClickListener = v -> {
        Intent createQrScanner = new Intent(this.getContext(), DesktopLoginQRScanner.class);
        createQrScanner.putExtra("meetingId", meetingId);
        startActivity(createQrScanner);
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_moderation_cards, container, false);
        Intent intent = requireActivity().getIntent();
        Bundle extra = intent.getExtras();
        this.meetingId = extra.getLong("meetingId");
        controller = new ModerationCardsController(meetingId);
        requireActivity().setTitle(getString(R.string.moderationCardTitle));
        FloatingActionButton addButton = view.findViewById(R.id.floatingActionButton);
        FloatingActionButton loginButton = view.findViewById(R.id.floatingActionButtonQRCode);
        addButton.setOnClickListener(addButtonClickListener);
        loginButton.setOnClickListener(loginButtonClickListener);
        RecyclerView moderationCardsRecyclerView = view.findViewById(R.id.moderationCardList);
        this.moderationCardAdapter = new ModerationCardAdapter(getActivity(), controller.getAllModerationCards());
        moderationCardsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        moderationCardsRecyclerView.setAdapter(moderationCardAdapter);
        ((BaseActivity) requireActivity()).getPullToRefresh().setOnRefreshListener(this::onResume);

        return view;
    }
    @Override
    public void onResume() {
        super.onResume();
        moderationCardAdapter.updateModerationCards(controller.getAllModerationCards());
        ((BaseActivity) requireActivity()).getPullToRefresh().setRefreshing(false);
    }

}
