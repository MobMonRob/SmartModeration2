package dhbw.smartmoderation.meeting.detail;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Collection;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.connection.synchronization.PullEvent;
import dhbw.smartmoderation.connection.synchronization.SynchronizableDataType;
import dhbw.smartmoderation.consensus.overview.ConsensusProposalOverviewFragment;
import dhbw.smartmoderation.listOfSpeakers.CumulativeSpeakingTimesFragment;
import dhbw.smartmoderation.listOfSpeakers.ListOfSpeakersController;
import dhbw.smartmoderation.listOfSpeakers.ListOfSpeakersFragment;
import dhbw.smartmoderation.moderationcards.overview.ModerationCardsFragment;
import dhbw.smartmoderation.util.ExceptionHandlingActivity;
import dhbw.smartmoderation.util.UpdateableExceptionHandlingActivity;

public class BaseActivity extends UpdateableExceptionHandlingActivity {

    private BottomNavigationView bottomNavigationView;
    private ListOfSpeakersController listOfSpeakersController;
    private MeetingDetailController meetingDetailController;
    private SwipeRefreshLayout pullToRefresh;
    private Fragment meetingDetailFragment = new MeetingDetailFragment();
    private final Fragment consensusProposalOverviewFragment = new ConsensusProposalOverviewFragment();
    private final Fragment listOfSpeakersFragment = new ListOfSpeakersFragment();
    private Fragment cumulativeSpeakingTimesFragment = new CumulativeSpeakingTimesFragment();
    private Fragment moderationCardsFragment = new ModerationCardsFragment();
    Fragment selectedFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        pullToRefresh = findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(this::updateUI);

        Intent intent = getIntent();
        Long meetingId = intent.getLongExtra("meetingId", 0);

        this.listOfSpeakersController = new ListOfSpeakersController(meetingId);

        this.meetingDetailController = new MeetingDetailController(meetingId);

        this.bottomNavigationView = findViewById(R.id.bottom_navigation);
        this.bottomNavigationView.setOnNavigationItemSelectedListener(navigationListener);

        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, consensusProposalOverviewFragment, "2").hide(consensusProposalOverviewFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, listOfSpeakersFragment, "3").hide(listOfSpeakersFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, cumulativeSpeakingTimesFragment, "4").hide(cumulativeSpeakingTimesFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, moderationCardsFragment, "5").hide(moderationCardsFragment).commit();

    }

    @Override
    protected void onResume() {
        super.onResume();

        if(selectedFragment == meetingDetailFragment) {

            getSupportFragmentManager().beginTransaction().remove(meetingDetailFragment).commit();
        }

        meetingDetailFragment = new MeetingDetailFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, meetingDetailFragment, "1").hide(meetingDetailFragment).commit();

        if(selectedFragment == null) {

            selectedFragment = meetingDetailFragment;
        }

        if(selectedFragment == meetingDetailFragment) {

            getSupportFragmentManager().beginTransaction().show(meetingDetailFragment).commit();
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            if(selectedFragment == meetingDetailFragment) {

                ((MeetingDetailFragment)meetingDetailFragment).endProgressBarThread();

            }
            switch(item.getItemId()) {

                case R.id.meetingNavigation:
                    MeetingDetailFragment newMeetingDetailFragment = new MeetingDetailFragment();
                    getSupportFragmentManager().beginTransaction().remove(meetingDetailFragment).commit();
                    getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, newMeetingDetailFragment, "10").commit();
                    meetingDetailFragment = newMeetingDetailFragment;
                    getSupportFragmentManager().beginTransaction().hide(selectedFragment).show(meetingDetailFragment).commit();
                    selectedFragment = meetingDetailFragment;
                    return true;
                case R.id.consensNavigation:
                    getSupportFragmentManager().beginTransaction().hide(selectedFragment).show(consensusProposalOverviewFragment).commit();
                    ((ConsensusProposalOverviewFragment)consensusProposalOverviewFragment).update();
                    selectedFragment = consensusProposalOverviewFragment;
                    setTitle(((ConsensusProposalOverviewFragment)consensusProposalOverviewFragment).getTitle());
                    return true;
                case R.id.speechNavigation:
                    getSupportFragmentManager().beginTransaction().hide(selectedFragment).show(listOfSpeakersFragment).commit();
                    ((ListOfSpeakersFragment)listOfSpeakersFragment).update();
                    selectedFragment = listOfSpeakersFragment;
                    setTitle(((ListOfSpeakersFragment)listOfSpeakersFragment).getTitle());
                    return true;
                case R.id.speechTimesNavigation:
                    CumulativeSpeakingTimesFragment newCumulativeSpeakingTimesFragment = new CumulativeSpeakingTimesFragment();
                    getSupportFragmentManager().beginTransaction().remove(cumulativeSpeakingTimesFragment).commit();
                    getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, newCumulativeSpeakingTimesFragment, "11").commit();
                    cumulativeSpeakingTimesFragment = newCumulativeSpeakingTimesFragment;
                    getSupportFragmentManager().beginTransaction().hide(selectedFragment).show(cumulativeSpeakingTimesFragment).commit();
                    selectedFragment = cumulativeSpeakingTimesFragment;
                    return true;
                case R.id.moderationCards:
                    setTitle(getString(R.string.moderationCardTitle));
//                    ModerationCardsFragment newModerationCardsFragment = new ModerationCardsFragment();
//                    getSupportFragmentManager().beginTransaction().remove(moderationCardsFragment).commit();
//                    getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, newModerationCardsFragment).commit();
//                    moderationCardsFragment = newModerationCardsFragment;
//                    getSupportFragmentManager().beginTransaction().hide(selectedFragment).show(moderationCardsFragment).commit();
//                    selectedFragment = moderationCardsFragment;
//                    setTitle(((ModerationCardsFragment)moderationCardsFragment).getTitle());
                    return true;

            }
            return false;
        }
    };

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public ListOfSpeakersController getListOfSpeakersController() {

        return this.listOfSpeakersController;
    }

    public MeetingDetailController getMeetingDetailController() {

        return this.meetingDetailController;
    }

    @Override
    public Collection<SynchronizableDataType> getSynchronizableDataTypes() {

        Collection<SynchronizableDataType> dataTypes = new ArrayList<>();

        if(selectedFragment == consensusProposalOverviewFragment){
            dataTypes.add(SynchronizableDataType.POLL);
            dataTypes.add(SynchronizableDataType.VOICE);
            return dataTypes;

        }

        else if(selectedFragment == meetingDetailFragment){

            dataTypes.add(SynchronizableDataType.MEETING);
            dataTypes.add(SynchronizableDataType.MEMBER);
            dataTypes.add(SynchronizableDataType.TOPIC);

        }

        else if (selectedFragment == listOfSpeakersFragment){

            dataTypes.add(SynchronizableDataType.PARTICIPATION);

        }

        else if (selectedFragment == cumulativeSpeakingTimesFragment){

            dataTypes.add(SynchronizableDataType.PARTICIPATION);
        }

        return dataTypes;
    }

    @Override
    protected void updateUI() {

        if(selectedFragment == consensusProposalOverviewFragment){

            ((ConsensusProposalOverviewFragment)consensusProposalOverviewFragment).update();

        }

        else if(selectedFragment == meetingDetailFragment){

            MeetingDetailFragment newMeetingDetailFragment = new MeetingDetailFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, newMeetingDetailFragment, "10").commit();
            getSupportFragmentManager().beginTransaction().hide(selectedFragment).show(newMeetingDetailFragment).commit();
            getSupportFragmentManager().beginTransaction().remove(meetingDetailFragment).commit();
            meetingDetailFragment = newMeetingDetailFragment;
            selectedFragment = meetingDetailFragment;

        }

        else if (selectedFragment == listOfSpeakersFragment) {

            ((ListOfSpeakersFragment)listOfSpeakersFragment).update();

        }

        else if (selectedFragment == cumulativeSpeakingTimesFragment){

            ((CumulativeSpeakingTimesFragment)cumulativeSpeakingTimesFragment).update();
        }
    }

    public SwipeRefreshLayout getPullToRefresh() {

        return pullToRefresh;
    }

}