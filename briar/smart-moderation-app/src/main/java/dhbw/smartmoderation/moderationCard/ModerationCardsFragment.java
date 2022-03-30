package dhbw.smartmoderation.moderationCard;

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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.meeting.detail.BaseActivity;

public class ModerationCardsFragment extends Fragment {
    private View view;
    private ModerationCardsController controller;
    private FloatingActionButton addButton;
    private FloatingActionButton loginButton;
    private long meetingId;
    private ModerationCardAdapter moderationCardAdapter;
    private RecyclerView moderationCardsRecyclerView;

    public final View.OnClickListener addButtonClickListener = v -> {
        CreateModerationCard createModerationCard = new CreateModerationCard(this);
        createModerationCard.show();
    };

    public final View.OnClickListener loginButtonClickListener = v -> {
        DesktopLoginView createDesktopLoginView = new DesktopLoginView(getActivity());
        createDesktopLoginView.show();
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        this.view = inflater.inflate(R.layout.fragment_moderation_cards, container, false);
        Intent intent = getActivity().getIntent();
        Bundle extra = intent.getExtras();
        this.meetingId = extra.getLong("meetingId");
        controller = new ModerationCardsController(meetingId);
        getActivity().setTitle(getString(R.string.moderationCardTitle));
        addButton = this.view.findViewById(R.id.floatingActionButton);
        loginButton = this.view.findViewById(R.id.floatingActionButtonQRCode);
        this.addButton.setOnClickListener(addButtonClickListener);
        this.loginButton.setOnClickListener(loginButtonClickListener);
        this.moderationCardsRecyclerView = this.view.findViewById(R.id.moderationCardList);
        this.moderationCardAdapter = new ModerationCardAdapter(getActivity(), controller.getAllModerationCards());
        this.moderationCardsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        this.moderationCardsRecyclerView.setAdapter(moderationCardAdapter);
        ((BaseActivity)getActivity()).getPullToRefresh().setOnRefreshListener(this::onResume);

        return this.view;
    }
    @Override
    public void onResume() {
        super.onResume();
        moderationCardAdapter.updateModerationCards(controller.getAllModerationCards());
        ((BaseActivity)getActivity()).getPullToRefresh().setRefreshing(false);
    }

}
