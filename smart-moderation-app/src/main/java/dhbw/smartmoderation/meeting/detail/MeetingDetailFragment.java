package dhbw.smartmoderation.meeting.detail;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.beardedhen.androidbootstrap.BootstrapProgressBar;
import com.beardedhen.androidbootstrap.api.defaults.DefaultBootstrapBrand;

import java.util.Collection;
import java.util.List;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.SmartModerationApplicationImpl;
import dhbw.smartmoderation.data.model.Attendance;
import dhbw.smartmoderation.data.model.Meeting;
import dhbw.smartmoderation.data.model.Member;
import dhbw.smartmoderation.data.model.Topic;
import dhbw.smartmoderation.data.model.TopicStatus;
import dhbw.smartmoderation.exceptions.MemberCantBeChangedException;
import dhbw.smartmoderation.exceptions.MemberCantBeDeleteException;
import dhbw.smartmoderation.exceptions.TopicCantBeChangedException;
import dhbw.smartmoderation.exceptions.TopicCantBeDeletedException;
import dhbw.smartmoderation.meeting.create.CreateMeetingActivity;
import dhbw.smartmoderation.uiUtils.SwipeHelper;
import dhbw.smartmoderation.uiUtils.UnderLayButton;
import dhbw.smartmoderation.uiUtils.UnderLayButtonClickListener;
import dhbw.smartmoderation.util.ExceptionHandlingActivity;
import dhbw.smartmoderation.util.Util;
import params.com.stepview.StatusViewScroller;

public class MeetingDetailFragment extends Fragment {

    private View view;
    private RecyclerView memberList;
    private MemberAdapter memberAdapter;

    private RecyclerView topicList;
    private TopicAdapter topicAdapter;
    private BootstrapProgressBar progressBar;
    private StatusViewScroller stateProgressBar;

    private TextView causeInfo;
    private TextView beginInfo;
    private TextView endTag;
    private TextView endInfo;
    private TextView locationInfo;
    private TextView memberTag;

    private ImageButton switchButton;

    private MeetingDetailController meetingDetailController;
    private Long meetingId;
    private Long groupId;
    private Meeting meeting;

    long startTimeInMillis;
    long endTimeInMillis;
    Handler handler = new Handler();

    public String getTitle(String cause) {
        return getString(R.string.meetingDetail_title, cause);
    }

    private Thread progessBarThread;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        if (meetingDetailController.isLocalAuthorModerator()) {
            inflater.inflate(R.menu.settings_menu, menu);
            super.onCreateOptionsMenu(menu, inflater);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.settingsButton) {
            Intent intent = new Intent(getActivity(), CreateMeetingActivity.class);
            intent.putExtra("activity", "MeetingDetailFragment");
            intent.putExtra("meetingId", this.meetingId);
            intent.putExtra("groupId", this.groupId);
            this.endProgressBarThread();
            startActivity(intent);
            requireActivity().finish();
        }

        return super.onOptionsItemSelected(item);
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        this.meetingDetailController = ((BaseActivity) requireActivity()).getMeetingDetailController();
        this.meeting = this.meetingDetailController.getMeeting();
        requireActivity().setTitle(getTitle(this.meeting.getCause()));
        this.meetingId = this.meeting.getMeetingId();
        this.groupId = this.meeting.getGroup().getGroupId();
        this.memberAdapter = new MemberAdapter(getActivity(), meetingDetailController);
        this.topicAdapter = new TopicAdapter(getActivity(), meetingDetailController);

        this.view = inflater.inflate(R.layout.fragment_meeting_detail, container, false);

        this.memberList = view.findViewById(R.id.memberList);
        this.topicList = view.findViewById(R.id.topicList);

        this.causeInfo = view.findViewById(R.id.causeInfo);
        this.causeInfo.setText(this.meeting.getCause());

        this.beginInfo = view.findViewById(R.id.beginInfo);
        String beginTimeString = this.meeting.getDateAsString() + ", " + Util.convertMilliSecondsToTimeString(this.meeting.getStartTime());
        this.beginInfo.setText(beginTimeString);

        this.endTag = view.findViewById(R.id.endTag);
        this.endInfo = view.findViewById(R.id.endInfo);

        this.locationInfo = view.findViewById(R.id.locationInfo);
        this.locationInfo.setText(this.meeting.getLocation());

        this.progressBar = view.findViewById(R.id.progressBar);
        this.progressBar.setVisibility(View.GONE);
        this.stateProgressBar = view.findViewById(R.id.stepView);
        this.stateProgressBar.setVisibility(View.GONE);

        this.memberTag = view.findViewById(R.id.memberTag);

        LinearLayoutManager memberLayoutManager = new LinearLayoutManager(getActivity());
        this.memberList.setLayoutManager(memberLayoutManager);
        this.memberList.setAdapter(this.memberAdapter);
        DividerItemDecoration memberDividerItemDecoration = new DividerItemDecoration(memberList.getContext(), memberLayoutManager.getOrientation());
        this.memberList.addItemDecoration(memberDividerItemDecoration);

        LinearLayoutManager topicLayoutManager = new LinearLayoutManager(getActivity());
        this.topicList.setLayoutManager(topicLayoutManager);
        this.topicList.setAdapter(this.topicAdapter);
        DividerItemDecoration topicDividerItemDecoration = new DividerItemDecoration(topicList.getContext(), topicLayoutManager.getOrientation());
        this.topicList.addItemDecoration(topicDividerItemDecoration);

        this.switchButton = view.findViewById(R.id.switchViewButton);
        this.switchButton.setOnClickListener(v -> {

            if (this.stateProgressBar.getVisibility() == View.VISIBLE) {
                this.progressBar.setVisibility(View.VISIBLE);
                this.stateProgressBar.setVisibility(View.GONE);
                changeConstraints();
                this.switchButton.setImageResource(R.drawable.ic_baseline_linear_scale_24);
            } else {
                this.progressBar.setVisibility(View.GONE);
                this.stateProgressBar.setVisibility(View.VISIBLE);
                changeConstraints();
                this.switchButton.setImageResource(R.drawable.ic_baseline_remove_24);
            }
        });

        if (this.meetingDetailController.isLocalAuthorModerator()) {
            instantiateMemberSwipeHelper();
            instantiateTopicSwipeHelper();
        }
        return this.view;
    }

    @Override
    public void onResume() {
        super.onResume();
        update();
    }

    @Override
    public void onStop() {
        super.onStop();
        endProgressBarThread();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            endProgressBarThread();
        }
    }

    public void update() {
        MeetingDetailAsyncTask meetingDetailAsyncTask = new MeetingDetailAsyncTask("update");
        meetingDetailAsyncTask.execute();
    }

    private void changeConstraints() {
        if (this.stateProgressBar.getVisibility() == View.VISIBLE) {
            ConstraintLayout constraintLayout = view.findViewById(R.id.constraintLayout);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            constraintSet.clear(this.memberTag.getId(), ConstraintSet.TOP);
            constraintSet.clear(this.memberTag.getId(), ConstraintSet.START);
            constraintSet.connect(this.memberTag.getId(), ConstraintSet.TOP, this.stateProgressBar.getId(), ConstraintSet.BOTTOM, 15);
            constraintSet.connect(this.memberTag.getId(), ConstraintSet.START, this.stateProgressBar.getId(), ConstraintSet.START);
            constraintSet.applyTo(constraintLayout);
            return;
        }

        ConstraintLayout constraintLayout = view.findViewById(R.id.constraintLayout);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        constraintSet.clear(this.memberTag.getId(), ConstraintSet.TOP);
        constraintSet.clear(this.memberTag.getId(), ConstraintSet.START);
        constraintSet.connect(this.memberTag.getId(), ConstraintSet.TOP, this.progressBar.getId(), ConstraintSet.BOTTOM, 15);
        constraintSet.connect(this.memberTag.getId(), ConstraintSet.START, this.progressBar.getId(), ConstraintSet.START);
        constraintSet.applyTo(constraintLayout);
    }


    private void updateStateProgressBar() {
        Collection<Topic> topics = this.meetingDetailController.getTopics();
        this.stateProgressBar.getStatusView().setCircleRadius(30.0f);
        this.stateProgressBar.getStatusView().setCurrentStatusZoom(0.2f);
        this.stateProgressBar.getStatusView().setStepCount(topics.size());
        int finishedTopics = 0;
        for (Topic topic : topics) {
            if (topic.getTopicStatus() == TopicStatus.FINISHED) {
                finishedTopics++;
            }
        }
        this.stateProgressBar.getStatusView().setCurrentCount(finishedTopics + 1);
    }

    private boolean allTopicsFinished() {
        for (Topic topic : meetingDetailController.getTopics())
            if (!topic.getStatus().equals(TopicStatus.FINISHED.name())) return false;
        return true;
    }

    private void runProgressBarThread() {
        endProgressBarThread();

        this.startTimeInMillis = this.meeting.getDate() + this.meeting.getStartTime();
        this.endTimeInMillis = this.meeting.getDate() + this.meeting.getEndTime();

        this.progessBarThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                progressBar = view.findViewById(R.id.progressBar);
                long current = System.currentTimeMillis();

                if (current >= startTimeInMillis && current <= endTimeInMillis) {
                    int startToCurrentDifference = (int) (current) - (int) (startTimeInMillis);
                    int startToEndDifference = (int) (endTimeInMillis) - (int) (startTimeInMillis);
                    int progress = (int) (((double) startToCurrentDifference / (double) startToEndDifference) * 100);

                    if ((endTimeInMillis - current) >= meetingDetailController.getTotalTimeOfUpcomingTopics()) {
                        progressBar.setBootstrapBrand(DefaultBootstrapBrand.SUCCESS);
                    } else {
                        progressBar.setBootstrapBrand(DefaultBootstrapBrand.DANGER);
                    }
                    handler.post(() -> progressBar.setProgress(progress));
                } else if (System.currentTimeMillis() < startTimeInMillis) {
                    handler.post(() -> progressBar.setProgress(0));
                } else {
                    if (allTopicsFinished()) {
                        progressBar.setBootstrapBrand(DefaultBootstrapBrand.SUCCESS);
                    } else {
                        progressBar.setBootstrapBrand(DefaultBootstrapBrand.DANGER);
                    }
                    handler.post(() -> progressBar.setProgress(100));
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        this.progessBarThread.start();
    }

    public void endProgressBarThread() {
        if (this.progessBarThread != null) {
            if (!this.progessBarThread.isInterrupted()) {
                this.progessBarThread.interrupt();
            }
        }
    }


    public void createAlertDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setNeutralButton(getString(R.string.ok), ((dialog, which) -> dialog.cancel()));
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void instantiateMemberSwipeHelper() {

        new SwipeHelper(getActivity(), memberList) {
            @Override
            public void instantiateUnderLayButton(RecyclerView.ViewHolder viewHolder, List<UnderLayButton> underLayButtons) {

                MemberAdapter.MemberViewHolder memberViewHolder = (MemberAdapter.MemberViewHolder) viewHolder;
                Member currentMember = memberViewHolder.getMember();

                underLayButtons.add(new UnderLayButton(getString(R.string.delete),
                        ResourcesCompat.getColor(SmartModerationApplicationImpl.getApp().getApplicationContext().getResources(), R.color.default_red, null),
                        (UnderLayButtonClickListener) position -> {
                            MeetingDetailAsyncTask meetingDetailAsyncTask = new MeetingDetailAsyncTask("deleteMember");
                            meetingDetailAsyncTask.execute(currentMember);
                        }));

                if (currentMember.getAttendance(meeting) == Attendance.PRESENT) {

                    underLayButtons.add(new UnderLayButton(getString(R.string.absent),
                            ResourcesCompat.getColor(SmartModerationApplicationImpl.getApp().getApplicationContext().getResources(), R.color.default_blue, null),
                            (UnderLayButtonClickListener) position -> {
                                MeetingDetailAsyncTask meetingDetailAsyncTask = new MeetingDetailAsyncTask("changeMemberStatus");
                                meetingDetailAsyncTask.execute(currentMember, Attendance.ABSENT);
                            }));

                    underLayButtons.add(new UnderLayButton(getString(R.string.excused),
                            ResourcesCompat.getColor(SmartModerationApplicationImpl.getApp().getApplicationContext().getResources(), R.color.colorPrimaryDark, null),
                            (UnderLayButtonClickListener) position -> {
                                MeetingDetailAsyncTask meetingDetailAsyncTask = new MeetingDetailAsyncTask("changeMemberStatus");
                                meetingDetailAsyncTask.execute(currentMember, Attendance.EXCUSED);
                            }));
                } else if (currentMember.getAttendance(meeting) == Attendance.EXCUSED) {

                    underLayButtons.add(new UnderLayButton(getString(R.string.absent),
                            ResourcesCompat.getColor(SmartModerationApplicationImpl.getApp().getApplicationContext().getResources(), R.color.default_blue, null),
                            (UnderLayButtonClickListener) position -> {
                                MeetingDetailAsyncTask meetingDetailAsyncTask = new MeetingDetailAsyncTask("changeMemberStatus");
                                meetingDetailAsyncTask.execute(currentMember, Attendance.ABSENT);
                            }));

                    underLayButtons.add(new UnderLayButton(getString(R.string.present),
                            ResourcesCompat.getColor(SmartModerationApplicationImpl.getApp().getApplicationContext().getResources(), R.color.default_green, null),
                            (UnderLayButtonClickListener) position -> {
                                MeetingDetailAsyncTask meetingDetailAsyncTask = new MeetingDetailAsyncTask("changeMemberStatus");
                                meetingDetailAsyncTask.execute(currentMember, Attendance.PRESENT);
                            }));
                } else {
                    underLayButtons.add(new UnderLayButton(getString(R.string.excused),
                            ResourcesCompat.getColor(SmartModerationApplicationImpl.getApp().getApplicationContext().getResources(), R.color.colorPrimaryDark, null),
                            (UnderLayButtonClickListener) position -> {
                                MeetingDetailAsyncTask meetingDetailAsyncTask = new MeetingDetailAsyncTask("changeMemberStatus");
                                meetingDetailAsyncTask.execute(currentMember, Attendance.EXCUSED);
                            }));

                    underLayButtons.add(new UnderLayButton(getString(R.string.present),
                            ResourcesCompat.getColor(SmartModerationApplicationImpl.getApp().getApplicationContext().getResources(), R.color.default_green, null),
                            (UnderLayButtonClickListener) position -> {
                                MeetingDetailAsyncTask meetingDetailAsyncTask = new MeetingDetailAsyncTask("changeMemberStatus");
                                meetingDetailAsyncTask.execute(currentMember, Attendance.PRESENT);
                            }));
                }
            }
        };
    }


    public void instantiateTopicSwipeHelper() {

        new SwipeHelper(getActivity(), topicList) {

            @Override
            public void instantiateUnderLayButton(RecyclerView.ViewHolder viewHolder, List<UnderLayButton> underLayButtons) {

                TopicAdapter.TopicViewHolder topicViewHolder = (TopicAdapter.TopicViewHolder) viewHolder;
                Topic currentTopic = topicViewHolder.getTopic();

                underLayButtons.add(new UnderLayButton(getString(R.string.delete),
                        ResourcesCompat.getColor(SmartModerationApplicationImpl.getApp().getApplicationContext().getResources(), R.color.default_red, null),
                        (UnderLayButtonClickListener) position -> {
                            MeetingDetailAsyncTask meetingDetailAsyncTask = new MeetingDetailAsyncTask("deleteTopic");
                            meetingDetailAsyncTask.execute(currentTopic);

                        }));

                if (currentTopic.getTopicStatus() == TopicStatus.UPCOMING) {
                    underLayButtons.add(new UnderLayButton(getString(R.string.running),
                            ResourcesCompat.getColor(SmartModerationApplicationImpl.getApp().getApplicationContext().getResources(), R.color.colorPrimaryDark, null),
                            (UnderLayButtonClickListener) position -> {
                                MeetingDetailAsyncTask meetingDetailAsyncTask = new MeetingDetailAsyncTask("changeTopicStatus");
                                meetingDetailAsyncTask.execute(currentTopic, TopicStatus.RUNNING);
                            }));
                } else if (currentTopic.getTopicStatus() == TopicStatus.RUNNING) {
                    underLayButtons.add(new UnderLayButton(getString(R.string.finished),
                            ResourcesCompat.getColor(SmartModerationApplicationImpl.getApp().getApplicationContext().getResources(), R.color.default_green, null),
                            (UnderLayButtonClickListener) position -> {
                                MeetingDetailAsyncTask meetingDetailAsyncTask = new MeetingDetailAsyncTask("changeTopicStatus");
                                meetingDetailAsyncTask.execute(currentTopic, TopicStatus.FINISHED);

                            }));
                }
            }
        };
    }

    @SuppressLint("StaticFieldLeak")
    public class MeetingDetailAsyncTask extends AsyncTask<Object, Object, String> {

        String flag;

        public MeetingDetailAsyncTask(String flag) {
            this.flag = flag;
        }

        @Override
        protected void onProgressUpdate(Object... values) {
            super.onProgressUpdate(values);

            String action = values[0].toString();

            if (action.equals("exception"))
                ((ExceptionHandlingActivity) requireActivity()).handleException((Exception) values[1]);
            else
                createAlertDialog(values[1].toString());
        }

        @Override
        protected String doInBackground(Object... objects) {

            String returnString = "";

            switch (flag) {
                case "update":
                    meetingDetailController.update();
                    meeting = meetingDetailController.getMeeting();
                    returnString = "update";
                    break;
                case "deleteMember":
                    Member currentMember = (Member) objects[0];
                    if (!meetingDetailController.getLocalAuthorId().equals(currentMember.getMemberId())) {
                        try {
                            if (meetingDetailController.hasMemberAlreadyVoted(currentMember)) {
                                meetingDetailController.deleteMember(currentMember);
                            } else {
                                publishProgress("alert", getString(R.string.participantAlreadyVoted));
                            }
                        } catch (MemberCantBeDeleteException exception) {
                            publishProgress("exception", exception);
                        }
                    } else {
                        publishProgress("alert", getString(R.string.removeYourselfFromMeeting));
                    }

                    returnString = "updateMember";
                    break;

                case "changeMemberStatus":
                    Member member = (Member) objects[0];
                    Attendance attendance = (Attendance) objects[1];
                    try {
                        meetingDetailController.changeMemberStatus(member, attendance);
                    } catch (MemberCantBeChangedException exception) {
                        publishProgress("exception", exception);
                    }
                    returnString = "updateMember";
                    break;
                case "deleteTopic":
                    Topic currentTopic = (Topic) objects[0];
                    try {
                        meetingDetailController.deleteTopic(currentTopic);
                    } catch (TopicCantBeDeletedException exception) {
                        publishProgress("exception", exception);
                    }
                    returnString = "updateTopic";
                    break;
                case "changeTopicStatus":
                    Topic topic = (Topic) objects[0];
                    TopicStatus topicStatus = (TopicStatus) objects[1];
                    try {
                        meetingDetailController.changeTopicStatus(topic, topicStatus);
                    } catch (TopicCantBeChangedException exception) {
                        publishProgress("exception", exception);
                    }
                    returnString = "updateTopic";
                    break;
            }
            return returnString;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            switch (s) {
                case "update":
                    causeInfo.setText(meeting.getCause());
                    String beginTimeString = meeting.getDateAsString() + ", " + Util.convertMilliSecondsToTimeString(meeting.getStartTime());
                    beginInfo.setText(beginTimeString);
                    locationInfo.setText(meeting.getLocation());
                    if (isAdded()) {
                        if (meeting.getOpen()) {
                            endTag.setText(getString(R.string.estimatedEnd));
                            String expectedEndTimeString = Util.convertMilliSecondsToTimeString(meeting.getExpectedEndTime());
                            endInfo.setText(expectedEndTimeString);
                            progressBar.setVisibility(View.GONE);
                            stateProgressBar.setVisibility(View.VISIBLE);
                            switchButton.setVisibility(View.GONE);
                            changeConstraints();
                        } else {
                            endTag.setText(getString(R.string.plannedEnd));
                            String plannedEndTimeString = Util.convertMilliSecondsToTimeString(meeting.getEndTime());
                            endInfo.setText(plannedEndTimeString);
                            progressBar.setVisibility(View.VISIBLE);
                            stateProgressBar.setVisibility(View.GONE);
                            changeConstraints();
                            switchButton.setVisibility(View.VISIBLE);
                            switchButton.setImageResource(R.drawable.ic_baseline_linear_scale_24);
                        }
                        memberAdapter.updateMemberList(meetingDetailController.getMembers());
                        topicAdapter.updateTopics(meetingDetailController.getTopics());
                        updateStateProgressBar();
                    }
                    runProgressBarThread();
                    ((BaseActivity) requireActivity()).getPullToRefresh().setRefreshing(false);
                    break;
                case "updateMember":
                    memberAdapter.updateMemberList(meetingDetailController.getMembers());
                    break;
                case "updateTopic":
                    topicAdapter.updateTopics(meetingDetailController.getTopics());
                    updateStateProgressBar();
                    break;
            }
        }
    }
}
