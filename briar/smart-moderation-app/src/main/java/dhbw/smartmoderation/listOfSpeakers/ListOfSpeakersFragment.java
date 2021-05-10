package dhbw.smartmoderation.listOfSpeakers;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collection;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.data.model.Member;
import dhbw.smartmoderation.data.model.Participation;
import dhbw.smartmoderation.exceptions.MemberNotFoundException;
import dhbw.smartmoderation.exceptions.ParitcipationCantBeChangedException;
import dhbw.smartmoderation.exceptions.ParticipationCantBeAddedException;
import dhbw.smartmoderation.exceptions.ParticipationCantBeCreatedException;
import dhbw.smartmoderation.exceptions.ParticipationCantBeDeletedException;
import dhbw.smartmoderation.exceptions.ParticipationListCouldNotBeCleared;
import dhbw.smartmoderation.exceptions.SmartModerationException;
import dhbw.smartmoderation.exceptions.SpeechCantBeStartedException;
import dhbw.smartmoderation.meeting.detail.BaseActivity;
import dhbw.smartmoderation.uiUtils.ItemTouchListener;
import dhbw.smartmoderation.uiUtils.OnStartDragListener;
import dhbw.smartmoderation.uiUtils.SimpleItemTouchHelperCallback;
import dhbw.smartmoderation.util.ExceptionHandlingActivity;
import dhbw.smartmoderation.util.Util;

public class ListOfSpeakersFragment extends Fragment implements OnStartDragListener, ParticipationAdapter.OnParticipationListener {

    private View view;
    private ListOfSpeakersController controller;

    private RecyclerView listOfSpeakers;
    private LinearLayoutManager listOfSpeakersLayoutManager;
    private ParticipationAdapter participationAdapter;
    private ItemTouchHelper itemTouchHelper;

    private ConstraintLayout currentSpeakerPanel;
    private TextView currentSpeakerTag;
    private TextView currentSpeaker;
    private TextView duration;
    private TextView totalDuration;

    private ConstraintLayout startStopPanel;
    private MaterialButton playButton;
    private MaterialButton pauseButton;

    private ConstraintLayout subscribePanelModerator;
    private MaterialButton addSpeakerButton;
    private MaterialButton clearSpeechListButton;

    private ConstraintLayout subscribePanelParticipant;
    private MaterialButton addMeButton;
    private MaterialButton removeMeButton;

    private View popUp;
    private RecyclerView memberList;
    private LinearLayoutManager memberListLayoutManager;
    private MemberAdapter memberAdapter;

    private Thread durationThread;
    private Handler handler;

    public String getTitle(){
        return getString(R.string.speechList_title);
    }

    private View.OnClickListener playButtonClickListener = v -> {

        try {

            this.controller.startNextSpeech();
            runTimerThread();
            this.participationAdapter.notifyDataSetChanged();
            this.currentSpeaker.setText(this.controller.getNextParticipation().getMember().getName());
            initializeCurrentSpeakerPanel();
            initializeStartStopPanel();

        } catch (SpeechCantBeStartedException e) {
            ((ExceptionHandlingActivity)getActivity()).handleException(e);
        }
    };

    private View.OnClickListener pauseButtonClickListener = v -> {

        try {
            this.controller.stopSpeech();
            this.durationThread.interrupt();
            this.participationAdapter.updateParticipations(this.controller.getParticipationsInList());
            this.participationAdapter.changeNumberingAfterOrderChange();
            initializeStartStopPanel();
            initializeCurrentSpeakerPanel();
        } catch (SmartModerationException e) {
            ((ExceptionHandlingActivity)getActivity()).handleException(e);
        }
    };

    private View.OnClickListener addSpeakerButtonClickListener = v -> {

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        this.popUp = inflater.inflate(R.layout.popup_speaker, null);
        this.memberList = this.popUp.findViewById(R.id.speakerList);
        this.memberListLayoutManager = new LinearLayoutManager(getActivity());
        this.memberList.setLayoutManager(this.memberListLayoutManager);
        Collection<Member> presentMembers = this.controller.getPresentMembersNotInSpeechList();
        this.memberAdapter = new MemberAdapter(getActivity(), presentMembers);
        this.memberList.setAdapter(this.memberAdapter);


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(this.popUp);
        AlertDialog alertDialog = builder.create();

        Button addBtn = this.popUp.findViewById(R.id.addButton);
        addBtn.setOnClickListener( view -> {

            Member selectedMember = this.memberAdapter.getSelectedMember();

            if(selectedMember != null) {

                if(this.controller.participationAlreadyExistsFor(selectedMember.getMemberId())) {

                    Participation participation = this.controller.getParticipation(selectedMember.getMemberId());
                    try {
                        this.controller.addParticipationToSpeechList(participation);
                    }catch (ParticipationCantBeAddedException e) {
                        ((ExceptionHandlingActivity)getActivity()).handleException(e);
                    }

                }

                else {

                    try {
                        this.controller.createParticipation(selectedMember);
                    } catch (ParticipationCantBeCreatedException e) {
                        ((ExceptionHandlingActivity)getActivity()).handleException(e);
                    }
                }

                this.participationAdapter.updateParticipations(this.controller.getParticipationsInList());
                this.initializeCurrentSpeakerPanel();
                this.changeUiOnAddSpeaker();
                alertDialog.cancel();
            }
        });

        Button cancelBtn = this.popUp.findViewById(R.id.cancelButton);
        cancelBtn.setOnClickListener( view -> alertDialog.cancel());


        alertDialog.show();

    };

    private View.OnClickListener clearSpeechListButtonClickListener = v -> {

        if(!this.controller.getNextParticipation().getIsSpeaking()) {

            try {
                this.controller.clearParticipationList();
                this.participationAdapter.updateParticipations(this.controller.getParticipationsInList());
                initializeCurrentSpeakerPanel();
                changeUiOnEmptySpeechList();

            } catch (ParticipationListCouldNotBeCleared participationListCouldNotBeCleared) {

                ((ExceptionHandlingActivity)getActivity()).handleException(participationListCouldNotBeCleared);
            }

        }

        else {

            createAlertDialog(getString(R.string.speechListCanNotBeEmptiedWhileRunning));
        }
    };

    private View.OnClickListener addMeButtonClickListener = v -> {

        if(this.controller.isLocalAuthorPresent()) {

            if(this.controller.participationAlreadyExistsFor(this.controller.getLocalAuthorId())) {

                Participation participation = this.controller.getParticipation(this.controller.getLocalAuthorId());
                try {

                    this.controller.addParticipationToSpeechList(participation);

                }catch(ParticipationCantBeAddedException e){

                    ((ExceptionHandlingActivity)getActivity()).handleException(e);
                }

            }

            else {

                try {

                    this.controller.createParticipationForLocalAuthor();

                } catch(ParticipationCantBeCreatedException exception){
                    ((ExceptionHandlingActivity)getActivity()).handleException(exception);
                }

            }

            this.participationAdapter.updateParticipations(this.controller.getParticipationsInList());
            this.initializeSubscribePanel();
            initializeCurrentSpeakerPanel();
        }

        else {

            createAlertDialog(getString(R.string.statusMustBePresentToAddToSpeechList));
        }

    };

    private View.OnClickListener removeMeButtonClickListener = v -> {

        Participation participation = this.controller.getParticipation(this.controller.getLocalAuthorId());

        if(!participation.getIsSpeaking()) {

            try {
                this.controller.removeParticipationFromSpeechList(participation);
                this.participationAdapter.updateParticipations(this.controller.getParticipationsInList());
                this.participationAdapter.changeNumberingAfterOrderChange();
                initializeSubscribePanel();
            } catch (SmartModerationException e) {
                ((ExceptionHandlingActivity)getActivity()).handleException(e);
            }
        }

        else {

            createAlertDialog(getString(R.string.cantRemoveYourselfFromTheListWhileRunningSpeech));
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.controller = ((BaseActivity)getActivity()).getListOfSpeakersController();
        //this.participationAdapter = new ParticipationAdapter(this, getActivity(),this , controller.isLocalAuthorModerator(), this);
    }

    @Override
    public void onStop() {
        super.onStop();
        endDurationThread();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if(hidden) {
            endDurationThread();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        this.view = inflater.inflate(R.layout.fragment_list_of_speakers, container, false);

        this.currentSpeakerPanel = this.view.findViewById(R.id.currentSpeakerPanel);
        this.currentSpeakerTag = this.view.findViewById(R.id.currentSpeakerTag);
        this.currentSpeaker = this.view.findViewById(R.id.currentSpeakerInfo);
        this.duration = this.view.findViewById(R.id.durationInfo);
        this.totalDuration = this.view.findViewById(R.id.totalDurationInfo);

        this.startStopPanel = this.view.findViewById(R.id.runStopPanel);
        this.playButton = this.view.findViewById(R.id.playButton);
        this.playButton.setOnClickListener(this.playButtonClickListener);
        this.pauseButton = this.view.findViewById(R.id.pauseButton);
        this.pauseButton.setOnClickListener(this.pauseButtonClickListener);

        this.subscribePanelModerator = this.view.findViewById(R.id.subscribePanelModerator);
        this.addSpeakerButton = this.view.findViewById(R.id.addSpeakerButton);
        this.addSpeakerButton.setOnClickListener(this.addSpeakerButtonClickListener);
        this.clearSpeechListButton = this.view.findViewById(R.id.clearSpeechListButton);
        this.clearSpeechListButton.setOnClickListener(this.clearSpeechListButtonClickListener);

        this.subscribePanelParticipant = this.view.findViewById(R.id.subscribePanelParticipant);
        this.addMeButton = this.view.findViewById(R.id.addMeButton);
        this.addMeButton.setOnClickListener(this.addMeButtonClickListener);
        this.removeMeButton = this.view.findViewById(R.id.removeMeButton);
        this.removeMeButton.setOnClickListener(this.removeMeButtonClickListener);

        this.participationAdapter = new ParticipationAdapter(this, getActivity(),this , controller.isLocalAuthorModerator(), this);
        this.listOfSpeakers = this.view.findViewById(R.id.participationList);
        this.listOfSpeakersLayoutManager = new WrapContentLinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        this.listOfSpeakers.setLayoutManager(this.listOfSpeakersLayoutManager);
        this.listOfSpeakers.setAdapter(this.participationAdapter);
        DividerItemDecoration speakersDividerItemDecoration = new DividerItemDecoration(listOfSpeakers.getContext(), listOfSpeakersLayoutManager.getOrientation());
        this.listOfSpeakers.addItemDecoration(speakersDividerItemDecoration);

        if(this.controller.isLocalAuthorModerator()) {

            ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(this.participationAdapter, R.color.default_red, R.drawable.trash, ItemTouchHelper.START);
            this.itemTouchHelper = new ItemTouchHelper(callback);
            this.itemTouchHelper.attachToRecyclerView(this.listOfSpeakers);
        }

        this.handler = new Handler();

        return this.view;
    }

    public void reloadItemTouchHelper() {

        this.itemTouchHelper.attachToRecyclerView(null);
        this.participationAdapter.updateParticipations(this.controller.getParticipationsInList());
        this.itemTouchHelper.attachToRecyclerView(this.listOfSpeakers);
    }

    private void changeUiOnEmptySpeechList() {

        this.playButton.setVisibility(View.GONE);
        this.clearSpeechListButton.setVisibility(View.GONE);

        ConstraintLayout.LayoutParams layoutParams =  (ConstraintLayout.LayoutParams)this.addSpeakerButton.getLayoutParams();
        layoutParams.horizontalBias = 0.5f;
        this.addSpeakerButton.setLayoutParams(layoutParams);
    }

    private void changeUiOnAddSpeaker() {

        this.playButton.setVisibility(View.VISIBLE);
        this.clearSpeechListButton.setVisibility(View.VISIBLE);

        ConstraintLayout.LayoutParams layoutParams =  (ConstraintLayout.LayoutParams)this.addSpeakerButton.getLayoutParams();
        layoutParams.horizontalBias = 0.2f;
        this.addSpeakerButton.setLayoutParams(layoutParams);
    }

    public void initializeCurrentSpeakerPanel() {

        if(this.controller.getParticipationsInList().size() > 0) {

            Participation nextParticipation = this.controller.getNextParticipation();

            this.currentSpeakerTag.setText(getString(R.string.nextSpeaker));

            long duration = 0;

            if(nextParticipation != null) {

                if(nextParticipation.getIsSpeaking()) {

                    this.currentSpeakerTag.setText(getString(R.string.currentSpeaker));
                    duration = System.currentTimeMillis() - nextParticipation.getStartTime();
                }

                this.currentSpeaker.setText(nextParticipation.getMember().getName());

                String durationString = Util.convertMilliSecondsToMinutesTimeString(duration) + " " + getString(R.string.minute);
                this.duration.setText(durationString);

                long total = nextParticipation.getTime() + duration;
                String totalDurationString = Util.convertMilliSecondsToMinutesTimeString(total) + " " + getString(R.string.minute);
                this.totalDuration.setText(totalDurationString);

            }
        }

        else {

            this.currentSpeakerTag.setText(getString(R.string.nextSpeaker));
            this.currentSpeaker.setText("-");
            this.duration.setText("-");
            this.totalDuration.setText("-");
        }
    }

    public void initializeStartStopPanel() {

        if(!this.controller.isLocalAuthorModerator()) {

            this.startStopPanel.setVisibility(View.GONE);
            return;
        }

        if(this.controller.getParticipationsInList().size() > 0) {

            Participation nextParticipation = this.controller.getNextParticipation();

            if(nextParticipation != null) {

                if(nextParticipation.getIsSpeaking()) {

                    this.playButton.setVisibility(View.GONE);
                    this.pauseButton.setVisibility(View.VISIBLE);
                }

                else {

                    this.playButton.setVisibility(View.VISIBLE);
                    this.pauseButton.setVisibility(View.GONE);
                }

                this.playButton.setEnabled(true);

            }

        }

        else {

            this.playButton.setVisibility(View.VISIBLE);
            this.playButton.setEnabled(false);
            this.pauseButton.setVisibility(View.GONE);
        }
    }

    private void initializeSubscribePanel() {

        if(this.controller.isLocalAuthorModerator()) {

            this.subscribePanelModerator.setVisibility(View.VISIBLE);
            this.subscribePanelParticipant.setVisibility(View.GONE);
        }

        else {

            this.subscribePanelModerator.setVisibility(View.GONE);
            this.subscribePanelParticipant.setVisibility(View.VISIBLE);

            if(this.controller.isLocalAuthorInSpeechList()) {

                this.addMeButton.setVisibility(View.GONE);
                this.removeMeButton.setVisibility(View.VISIBLE);
            }

            else {

                this.addMeButton.setVisibility(View.VISIBLE);
                this.removeMeButton.setVisibility(View.GONE);
            }
        }
    }

    private void runTimerThread() {

        Participation nextParticipation = this.controller.getNextParticipation();

        if(nextParticipation.getIsSpeaking()) {

            long startTime = nextParticipation.getStartTime();

            this.durationThread = new Thread(() -> {

                while (!Thread.currentThread().isInterrupted()) {

                    long difference = System.currentTimeMillis() - startTime;

                    String durationString = Util.convertMilliSecondsToMinutesTimeString(difference) + " " + getString(R.string.minute);
                    handler.post(() -> duration.setText(durationString));

                    long total = nextParticipation.getTime() + difference;
                    String totalDurationString = Util.convertMilliSecondsToMinutesTimeString(total) + " " + getString(R.string.minute);
                    handler.post(() -> totalDuration.setText(totalDurationString));

                    try {

                        Thread.sleep(1000);

                    } catch (InterruptedException e) {

                        e.printStackTrace();
                        Thread.currentThread().interrupt();
                    }

                }
            });

            this.durationThread.start();
        }

    }

    public void endDurationThread() {

        if(this.durationThread != null) {

            if(!this.durationThread.isInterrupted()) {

                this.durationThread.interrupt();
            }
        }
    }
    private void createAlertDialog(String message) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setNeutralButton(getString(R.string.ok), ((dialog, which) -> dialog.cancel()));
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void update() {

        Handler handler = new Handler();
        handler.post(() -> {

            controller.update();

            Participation nextParticipation = controller.getNextParticipation();

            if(nextParticipation != null) {

                if(!nextParticipation.getIsSpeaking()) {

                    endDurationThread();
                }

            }

            else {

                endDurationThread();
            }

            initializeCurrentSpeakerPanel();
            initializeStartStopPanel();
            initializeSubscribePanel();
            this.participationAdapter.updateParticipations(this.controller.getParticipationsInList());

            if(this.controller.getNextParticipation() != null) {

                if(this.controller.getNextParticipation().getIsSpeaking()) {

                    endDurationThread();
                    this.runTimerThread();
                }
            }

        });
    }

    @Override
    public void onResume() {
        super.onResume();
        update();
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {

        if(this.controller.isLocalAuthorModerator()) {

            this.itemTouchHelper.startDrag(viewHolder);
        }
    }


    @Override
    public void onParticipationDismiss(Participation participation) {

        try {

            this.controller.removeParticipationFromSpeechList(participation);

        } catch (SmartModerationException e) {

            e.printStackTrace();
        }

        if(this.participationAdapter.getParticipationList().size() == 0) {

            this.reloadItemTouchHelper();
        }

    }

    @Override
    public void changeNumberingAfterOrderChange(ArrayList<Participation> collection) {
        try {
            for (int i = 0; i < collection.size(); i++) {

                collection.get(i).setNumber(i+1);
            }

            for (int i = 0; i < collection.size(); i++) {

                participationAdapter.notifyItemChanged(i);
            }

            this.controller.changeParticipationNumbering(collection);
            initializeCurrentSpeakerPanel();

        } catch (ParitcipationCantBeChangedException e) {

            ((ExceptionHandlingActivity)getActivity()).handleException(e);
        }
    }

    @Override
    public Collection<Participation> getCollections() { return controller.getParticipations(); }
}
