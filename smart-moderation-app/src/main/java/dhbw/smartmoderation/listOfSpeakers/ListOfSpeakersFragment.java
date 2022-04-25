package dhbw.smartmoderation.listOfSpeakers;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.AsyncTask;
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
import dhbw.smartmoderation.exceptions.ParitcipationCantBeChangedException;
import dhbw.smartmoderation.exceptions.ParticipationCantBeAddedException;
import dhbw.smartmoderation.exceptions.ParticipationCantBeCreatedException;
import dhbw.smartmoderation.exceptions.ParticipationListCouldNotBeCleared;
import dhbw.smartmoderation.exceptions.SmartModerationException;
import dhbw.smartmoderation.exceptions.SpeechCantBeStartedException;
import dhbw.smartmoderation.meeting.detail.BaseActivity;
import dhbw.smartmoderation.uiUtils.OnStartDragListener;
import dhbw.smartmoderation.uiUtils.SimpleItemTouchHelperCallback;
import dhbw.smartmoderation.util.ExceptionHandlingActivity;
import dhbw.smartmoderation.util.Util;

public class ListOfSpeakersFragment extends Fragment implements OnStartDragListener, ParticipationAdapter.OnParticipationListener {

    private ListOfSpeakersController controller;

    private RecyclerView listOfSpeakers;
    private ParticipationAdapter participationAdapter;
    private ItemTouchHelper itemTouchHelper;

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

    private MemberAdapter memberAdapter;

    private Thread durationThread;
    private Handler handler;


    public String getTitle() {
        return getString(R.string.speechList_title);
    }

    private final View.OnClickListener playButtonClickListener = v -> {
        ListOfSpeakersAsyncTask listOfSpeakersAsyncTask = new ListOfSpeakersAsyncTask("play");
        listOfSpeakersAsyncTask.execute();
    };

    private final View.OnClickListener pauseButtonClickListener = v -> {
        ListOfSpeakersAsyncTask listOfSpeakersAsyncTask = new ListOfSpeakersAsyncTask("pause");
        listOfSpeakersAsyncTask.execute();
    };

    private final View.OnClickListener addSpeakerButtonClickListener = v -> {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        @SuppressLint("InflateParams") View popUp = inflater.inflate(R.layout.popup_speaker, null);
        RecyclerView memberList = popUp.findViewById(R.id.speakerList);
        LinearLayoutManager memberListLayoutManager = new LinearLayoutManager(getActivity());
        memberList.setLayoutManager(memberListLayoutManager);

        Collection<Member> presentMembers = this.controller.getPresentMembersNotInSpeechList();
        this.memberAdapter = new MemberAdapter(getActivity(), presentMembers);
        memberList.setAdapter(this.memberAdapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(popUp);
        AlertDialog alertDialog = builder.create();

        Button addBtn = popUp.findViewById(R.id.addButton);
        if (presentMembers.size() == 0)
            addBtn.setEnabled(false);

        addBtn.setOnClickListener(view -> {
            ListOfSpeakersAsyncTask listOfSpeakersAsyncTask = new ListOfSpeakersAsyncTask("addSpeaker");
            listOfSpeakersAsyncTask.execute(alertDialog);
        });

        Button cancelBtn = popUp.findViewById(R.id.cancelButton);
        cancelBtn.setOnClickListener(view -> alertDialog.cancel());
        alertDialog.show();
    };

    private final View.OnClickListener clearSpeechListButtonClickListener = v -> {
        ListOfSpeakersAsyncTask listOfSpeakersAsyncTask = new ListOfSpeakersAsyncTask("clearList");
        listOfSpeakersAsyncTask.execute();
    };

    private final View.OnClickListener addMeButtonClickListener = v -> {
        ListOfSpeakersAsyncTask listOfSpeakersAsyncTask = new ListOfSpeakersAsyncTask("addMe");
        listOfSpeakersAsyncTask.execute();
    };

    private final View.OnClickListener removeMeButtonClickListener = v -> {
        ListOfSpeakersAsyncTask listOfSpeakersAsyncTask = new ListOfSpeakersAsyncTask("removeMe");
        listOfSpeakersAsyncTask.execute();
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.controller = ((BaseActivity) requireActivity()).getListOfSpeakersController();
    }

    @Override
    public void onStop() {
        super.onStop();
        endDurationThread();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            endDurationThread();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_list_of_speakers, container, false);

        this.currentSpeakerTag = view.findViewById(R.id.currentSpeakerTag);
        this.currentSpeaker = view.findViewById(R.id.currentSpeakerInfo);
        this.duration = view.findViewById(R.id.durationInfo);
        this.totalDuration = view.findViewById(R.id.totalDurationInfo);

        this.startStopPanel = view.findViewById(R.id.runStopPanel);
        this.playButton = view.findViewById(R.id.playButton);
        this.playButton.setOnClickListener(this.playButtonClickListener);
        this.pauseButton = view.findViewById(R.id.pauseButton);
        this.pauseButton.setOnClickListener(this.pauseButtonClickListener);

        this.subscribePanelModerator = view.findViewById(R.id.subscribePanelModerator);
        this.addSpeakerButton = view.findViewById(R.id.addSpeakerButton);
        this.addSpeakerButton.setOnClickListener(this.addSpeakerButtonClickListener);
        this.clearSpeechListButton = view.findViewById(R.id.clearSpeechListButton);
        this.clearSpeechListButton.setOnClickListener(this.clearSpeechListButtonClickListener);

        this.subscribePanelParticipant = view.findViewById(R.id.subscribePanelParticipant);
        this.addMeButton = view.findViewById(R.id.addMeButton);
        this.addMeButton.setOnClickListener(this.addMeButtonClickListener);
        this.removeMeButton = view.findViewById(R.id.removeMeButton);
        this.removeMeButton.setOnClickListener(this.removeMeButtonClickListener);

        this.participationAdapter = new ParticipationAdapter(this, getActivity(), this, controller.isLocalAuthorModerator(), this);
        this.listOfSpeakers = view.findViewById(R.id.participationList);
        LinearLayoutManager listOfSpeakersLayoutManager = new WrapContentLinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        this.listOfSpeakers.setLayoutManager(listOfSpeakersLayoutManager);
        this.listOfSpeakers.setAdapter(this.participationAdapter);

        DividerItemDecoration speakersDividerItemDecoration = new DividerItemDecoration(listOfSpeakers.getContext(), listOfSpeakersLayoutManager.getOrientation());
        this.listOfSpeakers.addItemDecoration(speakersDividerItemDecoration);

        if (this.controller.isLocalAuthorModerator()) {
            ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(this.participationAdapter, R.color.default_red, R.drawable.trash, ItemTouchHelper.START);
            this.itemTouchHelper = new ItemTouchHelper(callback);
            this.itemTouchHelper.attachToRecyclerView(this.listOfSpeakers);
        }

        this.handler = new Handler();

        return view;
    }

    public void reloadItemTouchHelper() {
        this.itemTouchHelper.attachToRecyclerView(null);
        this.participationAdapter.updateParticipations(this.controller.getParticipationsInList());
        this.itemTouchHelper.attachToRecyclerView(this.listOfSpeakers);
    }

    private void changeUiOnEmptySpeechList() {
        this.startStopPanel.setVisibility(View.GONE);
        this.clearSpeechListButton.setVisibility(View.GONE);

        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) this.addSpeakerButton.getLayoutParams();
        layoutParams.horizontalBias = 0.5f;
        this.addSpeakerButton.setLayoutParams(layoutParams);
    }

    private void changeUiOnAddSpeaker() {
        this.startStopPanel.setVisibility(View.VISIBLE);
        this.playButton.setVisibility(View.VISIBLE);
        this.clearSpeechListButton.setVisibility(View.VISIBLE);

        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) this.addSpeakerButton.getLayoutParams();
        layoutParams.horizontalBias = 0.2f;
        this.addSpeakerButton.setLayoutParams(layoutParams);
    }

    public void initializeCurrentSpeakerPanel() {
        if (this.controller.getParticipationsInList().size() > 0) {
            Participation nextParticipation = this.controller.getNextParticipation();
            this.currentSpeakerTag.setText(getString(R.string.nextSpeaker));
            long duration = 0;
            if (nextParticipation != null) {
                if (nextParticipation.getIsSpeaking()) {
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
        } else {
            this.currentSpeakerTag.setText(getString(R.string.nextSpeaker));
            this.currentSpeaker.setText("-");
            this.duration.setText("-");
            this.totalDuration.setText("-");
        }
    }

    public void initializeStartStopPanel() {

        if (!this.controller.isLocalAuthorModerator()) {
            this.startStopPanel.setVisibility(View.GONE);
            return;
        }

        if (this.controller.getParticipationsInList().size() > 0) {
            Participation nextParticipation = this.controller.getNextParticipation();

            if (nextParticipation != null) {
                if (nextParticipation.getIsSpeaking()) {
                    this.playButton.setVisibility(View.GONE);
                    this.pauseButton.setVisibility(View.VISIBLE);
                } else {
                    this.playButton.setVisibility(View.VISIBLE);
                    this.pauseButton.setVisibility(View.GONE);
                }
                this.playButton.setEnabled(true);
            }
        } else {
            this.playButton.setVisibility(View.VISIBLE);
            this.playButton.setEnabled(false);
            this.pauseButton.setVisibility(View.GONE);
        }
    }

    private void initializeSubscribePanel() {
        if (this.controller.isLocalAuthorModerator()) {
            this.subscribePanelModerator.setVisibility(View.VISIBLE);
            this.subscribePanelParticipant.setVisibility(View.GONE);

            if (this.controller.getParticipationsInList().size() == 0) {
                changeUiOnEmptySpeechList();
            } else {
                changeUiOnAddSpeaker();
            }
        } else {
            this.subscribePanelModerator.setVisibility(View.GONE);
            this.subscribePanelParticipant.setVisibility(View.VISIBLE);

            if (this.controller.isLocalAuthorInSpeechList()) {
                this.addMeButton.setVisibility(View.GONE);
                this.removeMeButton.setVisibility(View.VISIBLE);
            } else {
                this.addMeButton.setVisibility(View.VISIBLE);
                this.removeMeButton.setVisibility(View.GONE);
            }
        }
    }

    private void runTimerThread() {

        Participation nextParticipation = this.controller.getNextParticipation();

        if (nextParticipation.getIsSpeaking()) {
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
        if (this.durationThread != null) {
            if (!this.durationThread.isInterrupted()) {
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
        ListOfSpeakersAsyncTask listOfSpeakersAsyncTask = new ListOfSpeakersAsyncTask("update");
        listOfSpeakersAsyncTask.execute();
    }

    @Override
    public void onResume() {
        super.onResume();
        update();
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        if (this.controller.isLocalAuthorModerator())
            this.itemTouchHelper.startDrag(viewHolder);
    }


    @Override
    public void onParticipationDismiss(Participation participation) {
        ListOfSpeakersAsyncTask listOfSpeakersAsyncTask = new ListOfSpeakersAsyncTask("delete");
        listOfSpeakersAsyncTask.execute(participation);
    }

    @Override
    public void changeNumberingAfterOrderChange(ArrayList<Participation> collection) {
        for (int i = 0; i < collection.size(); i++)
            collection.get(i).setNumber(i + 1);

        for (int i = 0; i < collection.size(); i++)
            participationAdapter.notifyItemChanged(i);

        ListOfSpeakersAsyncTask listOfSpeakersAsyncTask = new ListOfSpeakersAsyncTask("numbering");
        listOfSpeakersAsyncTask.execute(collection);
    }

    @Override
    public Collection<Participation> getCollections() {
        return controller.getParticipations();
    }


    @SuppressLint("StaticFieldLeak")
    public class ListOfSpeakersAsyncTask extends AsyncTask<Object, Object, String> {

        String flag;

        public ListOfSpeakersAsyncTask(String flag) {
            this.flag = flag;
        }

        @Override
        protected void onProgressUpdate(Object... values) {
            super.onProgressUpdate(values);

            if (values[0] instanceof Exception)
                ((ExceptionHandlingActivity) requireActivity()).handleException((Exception) values[0]);

            if (values[0] instanceof Thread)
                ((Thread) values[0]).interrupt();

            if (values[0] instanceof AlertDialog)
                ((AlertDialog) values[0]).cancel();
        }

        @Override
        protected String doInBackground(Object... objects) {
            String returnString = "";

            switch (flag) {
                case "play":
                    try {
                        controller.startNextSpeech();
                    } catch (SpeechCantBeStartedException exception) {
                        publishProgress(exception);
                    }
                    break;
                case "pause":
                    try {
                        controller.stopSpeech();
                        publishProgress(durationThread);
                    } catch (SmartModerationException exception) {
                        publishProgress(exception);
                    }
                    break;

                case "addSpeaker":
                    AlertDialog alertDialog = (AlertDialog) objects[0];
                    Member selectedMember = memberAdapter.getSelectedMember();
                    if (selectedMember != null) {
                        if (controller.participationAlreadyExistsFor(selectedMember.getMemberId())) {
                            Participation participation = controller.getParticipation(selectedMember.getMemberId());
                            try {
                                controller.addParticipationToSpeechList(participation);
                            } catch (ParticipationCantBeAddedException exception) {
                                publishProgress(exception);
                            }
                        } else {
                            try {
                                controller.createParticipation(selectedMember);
                            } catch (ParticipationCantBeCreatedException exception) {
                                publishProgress(exception);
                            }
                        }
                    }
                    publishProgress(alertDialog);
                    break;
                case "clearList":
                    Participation nextParticipation = controller.getNextParticipation();
                    if (nextParticipation != null && !nextParticipation.getIsSpeaking()) {
                        try {
                            controller.clearParticipationList();
                        } catch (ParticipationListCouldNotBeCleared exception) {
                            publishProgress(exception);
                        }
                    } else {
                        returnString = "alert";
                    }
                    break;
                case "addMe":
                    if (controller.isLocalAuthorPresent()) {
                        if (controller.participationAlreadyExistsFor(controller.getLocalAuthorId())) {
                            Participation participation = controller.getParticipation(controller.getLocalAuthorId());
                            try {
                                controller.addParticipationToSpeechList(participation);
                            } catch (ParticipationCantBeAddedException exception) {
                                publishProgress(exception);
                            }
                        } else {
                            try {
                                controller.createParticipationForLocalAuthor();
                            } catch (ParticipationCantBeCreatedException exception) {
                                publishProgress(exception);
                            }
                        }
                    } else {
                        returnString = "alert";
                    }
                    break;
                case "removeMe":
                    Participation participation = controller.getParticipation(controller.getLocalAuthorId());
                    if (!participation.getIsSpeaking()) {
                        try {
                            controller.removeParticipationFromSpeechList(participation);
                        } catch (SmartModerationException exception) {
                            publishProgress(exception);
                        }
                    } else {
                        returnString = "alert";
                    }
                    break;
                case "update":
                    controller.update();
                    break;
                case "delete":
                    Participation participationToDelete = (Participation) objects[0];
                    try {
                        controller.removeParticipationFromSpeechList(participationToDelete);
                    } catch (SmartModerationException exception) {
                        publishProgress(exception);
                    }
                    break;
                case "numbering":
                    ArrayList<?> collection = (ArrayList<?>) objects[0];
                    Collection<Participation> participations = new ArrayList<>();
                    for (Object o : collection)
                        if (o instanceof Participation) participations.add((Participation) o);
                    try {
                        controller.changeParticipationNumbering(participations);
                    } catch (ParitcipationCantBeChangedException exception) {
                        publishProgress(exception);
                    }
                    break;
            }
            return returnString;
        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            switch (flag) {
                case "play":
                    runTimerThread();
                    participationAdapter.notifyDataSetChanged();
                    currentSpeaker.setText(controller.getNextParticipation().getMember().getName());
                    initializeCurrentSpeakerPanel();
                    initializeStartStopPanel();
                    break;
                case "pause":
                    participationAdapter.updateParticipations(controller.getParticipationsInList());
                    participationAdapter.changeNumberingAfterOrderChange();
                    initializeStartStopPanel();
                    initializeCurrentSpeakerPanel();
                    initializeSubscribePanel();
                    break;
                case "addSpeaker":
                    participationAdapter.updateParticipations(controller.getParticipationsInList());
                    initializeCurrentSpeakerPanel();
                    changeUiOnAddSpeaker();
                    break;
                case "clearList":
                    if (s.equals("alert")) {
                        createAlertDialog(getString(R.string.speechListCanNotBeEmptiedWhileRunning));
                    } else {
                        participationAdapter.updateParticipations(controller.getParticipationsInList());
                        initializeCurrentSpeakerPanel();
                        changeUiOnEmptySpeechList();
                    }
                    break;
                case "addMe":
                    if (s.equals("alert")) {
                        createAlertDialog(getString(R.string.statusMustBePresentToAddToSpeechList));
                    } else {
                        participationAdapter.updateParticipations(controller.getParticipationsInList());
                        initializeSubscribePanel();
                        initializeCurrentSpeakerPanel();
                        changeUiOnAddSpeaker();
                    }
                    break;
                case "removeMe":
                    if (s.equals("alert")) {
                        createAlertDialog(getString(R.string.cantRemoveYourselfFromTheListWhileRunningSpeech));
                    } else {
                        participationAdapter.updateParticipations(controller.getParticipationsInList());
                        participationAdapter.changeNumberingAfterOrderChange();
                        initializeSubscribePanel();
                    }
                    break;
                case "update":
                    Participation nextParticipation = controller.getNextParticipation();
                    if (nextParticipation != null) {
                        if (!nextParticipation.getIsSpeaking()) endDurationThread();
                    } else endDurationThread();

                    initializeCurrentSpeakerPanel();
                    initializeStartStopPanel();
                    initializeSubscribePanel();
                    participationAdapter.updateParticipations(controller.getParticipationsInList());

                    if (nextParticipation != null) {
                        if (nextParticipation.getIsSpeaking()) {
                            endDurationThread();
                            runTimerThread();
                        }
                    }
                    ((BaseActivity) requireActivity()).getPullToRefresh().setRefreshing(false);
                    break;
                case "delete":
                    if (participationAdapter.getParticipationList().size() == 0) {
                        reloadItemTouchHelper();
                        changeUiOnEmptySpeechList();
                    }
                    break;
                case "numbering":
                    initializeCurrentSpeakerPanel();
                    break;
            }
        }
    }
}
