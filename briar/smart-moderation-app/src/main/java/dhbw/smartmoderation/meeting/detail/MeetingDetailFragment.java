package dhbw.smartmoderation.meeting.detail;

import android.content.Intent;
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
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.beardedhen.androidbootstrap.BootstrapProgressBar;
import com.beardedhen.androidbootstrap.api.attributes.BootstrapBrand;
import com.beardedhen.androidbootstrap.api.defaults.DefaultBootstrapBrand;
import java.util.Collection;
import java.util.List;
import dhbw.smartmoderation.R;
import dhbw.smartmoderation.SmartModerationApplication;
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
    private LinearLayoutManager memberLayoutManager;

    private RecyclerView topicList;
    private TopicAdapter topicAdapter;
    private LinearLayoutManager topicLayoutManager;
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

    public String getTitle(String cause){
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
        if(meetingDetailController.isLocalAuthorModerator()) {

            inflater.inflate(R.menu.settings_menu, menu);
            super.onCreateOptionsMenu(menu, inflater);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if(itemId == R.id.settingsButton) {

            Intent intent = new Intent(getActivity(), CreateMeetingActivity.class);
            intent.putExtra("activity", "MeetingDetailFragment");
            intent.putExtra("meetingId", this.meetingId);
            intent.putExtra("groupId", this.groupId);
            this.endProgressBarThread();
            startActivityForResult(intent, 1);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        update();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        this.meetingDetailController =  ((BaseActivity)getActivity()).getMeetingDetailController();
        this.meeting = this.meetingDetailController.getMeeting();
        getActivity().setTitle(getTitle(this.meeting.getCause()));
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

        this.memberLayoutManager = new LinearLayoutManager(getActivity());
        this.memberList.setLayoutManager(this.memberLayoutManager);
        this.memberList.setAdapter(this.memberAdapter);
        DividerItemDecoration memberDividerItemDecoration = new DividerItemDecoration(memberList.getContext(), memberLayoutManager.getOrientation());
        this.memberList.addItemDecoration(memberDividerItemDecoration);

        this.topicLayoutManager = new LinearLayoutManager(getActivity());
        this.topicList.setLayoutManager(this.topicLayoutManager);
        this.topicList.setAdapter(this.topicAdapter);
        DividerItemDecoration topicDividerItemDecoration = new DividerItemDecoration(topicList.getContext(), topicLayoutManager.getOrientation());
        this.topicList.addItemDecoration(topicDividerItemDecoration);

        this.switchButton = view.findViewById(R.id.switchViewButton);
        this.switchButton.setOnClickListener(v -> {

            if(this.stateProgressBar.getVisibility() == View.VISIBLE) {
                this.progressBar.setVisibility(View.VISIBLE);
                this.stateProgressBar.setVisibility(View.GONE);
                changeConstraints();
                this.switchButton.setImageResource(R.drawable.ic_baseline_linear_scale_24);
            }

            else {
                this.progressBar.setVisibility(View.GONE);
                this.stateProgressBar.setVisibility(View.VISIBLE);
                changeConstraints();
                this.switchButton.setImageResource(R.drawable.ic_baseline_remove_24);
            }
        });

        if(this.meetingDetailController.isLocalAuthorModerator()) {
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

        if(hidden) {

            endProgressBarThread();

        }
    }

    public void update() {

        Handler handler = new Handler();

        handler.post(() -> {

            meetingDetailController.update();

            this.meeting = meetingDetailController.getMeeting();

            this.causeInfo.setText(meeting.getCause());
            String beginTimeString = this.meeting.getDateAsString() + ", " + Util.convertMilliSecondsToTimeString(this.meeting.getStartTime());
            this.beginInfo.setText(beginTimeString);
            this.locationInfo.setText(this.meeting.getLocation());

            if(isAdded()) {

                if (this.meeting.getOpen()) {

                    this.endTag.setText(getString(R.string.estimatedEnd));
                    String expectedEndTimeString = Util.convertMilliSecondsToTimeString(this.meeting.getExpectedEndTime());
                    this.endInfo.setText(expectedEndTimeString);
                    this.progressBar.setVisibility(View.GONE);
                    this.stateProgressBar.setVisibility(View.VISIBLE);
                    this.switchButton.setVisibility(View.GONE);
                    changeConstraints();

                } else {

                    this.endTag.setText(getString(R.string.plannedEnd));
                    String plannedEndTimeString = Util.convertMilliSecondsToTimeString(this.meeting.getEndTime());
                    this.endInfo.setText(plannedEndTimeString);
                    this.progressBar.setVisibility(View.VISIBLE);
                    this.stateProgressBar.setVisibility(View.GONE);
                    changeConstraints();
                    this.switchButton.setVisibility(View.VISIBLE);
                    this.switchButton.setImageResource(R.drawable.ic_baseline_linear_scale_24);

                }

                this.memberAdapter.updateMemberList(this.meetingDetailController.getMembers());
                this.topicAdapter.updateTopics(this.meetingDetailController.getTopics());
                this.updateStateProgressBar();
            }
        });

        this.runProgressBarThread();
    }

    private void changeConstraints() {

        if(this.stateProgressBar.getVisibility() == View.VISIBLE) {

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

        for(Topic topic : meetingDetailController.getTopics()) {

            if(!topic.getStatus().equals(TopicStatus.FINISHED.name())) {
                return false;
            }
        }

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

                if(current >= startTimeInMillis && current <= endTimeInMillis) {

                    int startToCurrentDifference = (int)(current) -(int)(startTimeInMillis);
                    int startToEndDifference = (int)(endTimeInMillis) - (int)(startTimeInMillis);
                    int progress = (int)(((double)startToCurrentDifference/(double)startToEndDifference)*100);

                    if((endTimeInMillis - current) >= meetingDetailController.getTotalTimeOfUpcomingTopics()) {

                        progressBar.setBootstrapBrand(DefaultBootstrapBrand.SUCCESS);
                    }

                    else {

                        progressBar.setBootstrapBrand(DefaultBootstrapBrand.DANGER);
                    }

                    handler.post(() -> progressBar.setProgress(progress));
                }

                else if (System.currentTimeMillis() < startTimeInMillis) {

                    handler.post(() -> progressBar.setProgress(0));
                }

                else {

                    if(allTopicsFinished()) {

                        progressBar.setBootstrapBrand(DefaultBootstrapBrand.SUCCESS);
                    }

                    else {

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

        if(this.progessBarThread != null) {

            if(!this.progessBarThread.isInterrupted()) {

                this.progessBarThread.interrupt();
            }
        }
    }

    public void instantiateMemberSwipeHelper() {

        new SwipeHelper(getActivity(), memberList) {

            @Override
            public void instantiateUnderLayButton(RecyclerView.ViewHolder viewHolder, List<UnderLayButton> underLayButtons) {

                MemberAdapter.MemberViewHolder memberViewHolder = (MemberAdapter.MemberViewHolder)viewHolder;
                Member currentMember = memberViewHolder.getMember();

                underLayButtons.add(new UnderLayButton(getString(R.string.delete), 0,
                        ResourcesCompat.getColor(SmartModerationApplication.getApp().getApplicationContext().getResources(), R.color.default_red, null),
                        (UnderLayButtonClickListener) position -> {

                            if(!meetingDetailController.getLocalAuthorId().equals(currentMember.getMemberId())) {

                                try {

                                    if(meetingDetailController.hasMemberAlreadyVoted(currentMember)) {

                                        meetingDetailController.deleteMember(currentMember);
                                        memberAdapter.updateMemberList(meetingDetailController.getMembers());
                                    }

                                    else {

                                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                        builder.setMessage(getString(R.string.participantAlreadyVoted));
                                        builder.setCancelable(false);
                                        builder.setNeutralButton(getString(R.string.ok), ((dialog, which) -> {

                                            dialog.cancel();

                                        }));

                                        AlertDialog alertDialog = builder.create();
                                        alertDialog.show();

                                    }


                                } catch (MemberCantBeDeleteException e) {

                                    ((ExceptionHandlingActivity)getActivity()).handleException(e);
                                }
                            }

                            else {

                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setMessage(getString(R.string.removeYourselfFromMeeting));
                                builder.setCancelable(false);
                                builder.setNeutralButton(getString(R.string.ok), ((dialog, which) -> {

                                    dialog.cancel();

                                }));

                                AlertDialog alertDialog = builder.create();
                                alertDialog.show();
                            }

                        }));

                if(currentMember.getAttendance(meeting) == Attendance.PRESENT) {

                    underLayButtons.add(new UnderLayButton(getString(R.string.absent), 0,
                            ResourcesCompat.getColor(SmartModerationApplication.getApp().getApplicationContext().getResources(), R.color.default_blue, null),
                            (UnderLayButtonClickListener) position -> {
                                try {
                                    meetingDetailController.changeMemberStatus(currentMember, Attendance.ABSENT);
                                    memberAdapter.updateMemberList(meetingDetailController.getMembers());
                                } catch (MemberCantBeChangedException e) {
                                    ((ExceptionHandlingActivity)getActivity()).handleException(e);
                                }
                            }));

                    underLayButtons.add(new UnderLayButton(getString(R.string.excused), 0,
                            ResourcesCompat.getColor(SmartModerationApplication.getApp().getApplicationContext().getResources(), R.color.colorPrimaryDark, null),
                            (UnderLayButtonClickListener) position -> {
                                try {
                                    meetingDetailController.changeMemberStatus(currentMember, Attendance.EXCUSED);
                                    memberAdapter.updateMemberList(meetingDetailController.getMembers());
                                } catch (MemberCantBeChangedException e) {
                                    ((ExceptionHandlingActivity)getActivity()).handleException(e);
                                }
                            }));
                }

                else if (currentMember.getAttendance(meeting) == Attendance.EXCUSED) {

                    underLayButtons.add(new UnderLayButton(getString(R.string.absent), 0,
                            ResourcesCompat.getColor(SmartModerationApplication.getApp().getApplicationContext().getResources(), R.color.default_blue, null),
                            (UnderLayButtonClickListener) position -> {
                                try {
                                    meetingDetailController.changeMemberStatus(currentMember, Attendance.ABSENT);
                                    memberAdapter.updateMemberList(meetingDetailController.getMembers());
                                } catch (MemberCantBeChangedException e) {
                                    ((ExceptionHandlingActivity)getActivity()).handleException(e);
                                }
                            }));

                    underLayButtons.add(new UnderLayButton(getString(R.string.present), 0,
                            ResourcesCompat.getColor(SmartModerationApplication.getApp().getApplicationContext().getResources(), R.color.default_green, null),
                            (UnderLayButtonClickListener) position -> {
                                try {
                                    meetingDetailController.changeMemberStatus(currentMember, Attendance.PRESENT);
                                    memberAdapter.updateMemberList(meetingDetailController.getMembers());
                                } catch (MemberCantBeChangedException e) {
                                    ((ExceptionHandlingActivity)getActivity()).handleException(e);
                                }
                            }));
                }

                else {

                    underLayButtons.add(new UnderLayButton(getString(R.string.excused), 0,
                            ResourcesCompat.getColor(SmartModerationApplication.getApp().getApplicationContext().getResources(), R.color.colorPrimaryDark, null),
                            (UnderLayButtonClickListener) position -> {
                                try {
                                    meetingDetailController.changeMemberStatus(currentMember, Attendance.EXCUSED);
                                    memberAdapter.updateMemberList(meetingDetailController.getMembers());
                                } catch (MemberCantBeChangedException e) {
                                    ((ExceptionHandlingActivity)getActivity()).handleException(e);
                                }
                            }));

                    underLayButtons.add(new UnderLayButton(getString(R.string.present), 0,
                            ResourcesCompat.getColor(SmartModerationApplication.getApp().getApplicationContext().getResources(), R.color.default_green, null),
                            (UnderLayButtonClickListener) position -> {
                                try {
                                    meetingDetailController.changeMemberStatus(currentMember, Attendance.PRESENT);
                                    memberAdapter.updateMemberList(meetingDetailController.getMembers());
                                } catch (MemberCantBeChangedException e) {
                                    ((ExceptionHandlingActivity)getActivity()).handleException(e);
                                }
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

                underLayButtons.add(new UnderLayButton(getString(R.string.delete), 0,
                        ResourcesCompat.getColor(SmartModerationApplication.getApp().getApplicationContext().getResources(), R.color.default_red, null),
                        (UnderLayButtonClickListener) position -> {
                            try {
                                meetingDetailController.deleteTopic(currentTopic);
                                topicAdapter.updateTopics(meetingDetailController.getTopics());
                                updateStateProgressBar();
                            } catch (TopicCantBeDeletedException e) {
                                ((ExceptionHandlingActivity)getActivity()).handleException(e);
                            }
                        }));

                if(currentTopic.getTopicStatus() == TopicStatus.UPCOMING) {

                    underLayButtons.add(new UnderLayButton(getString(R.string.running), 0,
                            ResourcesCompat.getColor(SmartModerationApplication.getApp().getApplicationContext().getResources(), R.color.colorPrimaryDark, null),
                            (UnderLayButtonClickListener) position -> {
                                try {
                                    meetingDetailController.changeTopicStatus(currentTopic, TopicStatus.RUNNING);
                                    topicAdapter.updateTopics(meetingDetailController.getTopics());
                                    updateStateProgressBar();
                                } catch (TopicCantBeChangedException e) {
                                    ((ExceptionHandlingActivity)getActivity()).handleException(e);
                                }
                            }));
                }

                else if(currentTopic.getTopicStatus() == TopicStatus.RUNNING) {

                    underLayButtons.add(new UnderLayButton(getString(R.string.finished), 0,
                            ResourcesCompat.getColor(SmartModerationApplication.getApp().getApplicationContext().getResources(), R.color.default_green, null),
                            (UnderLayButtonClickListener) position -> {
                                try {
                                    meetingDetailController.changeTopicStatus(currentTopic, TopicStatus.FINISHED);
                                    topicAdapter.updateTopics(meetingDetailController.getTopics());
                                    updateStateProgressBar();
                                } catch (TopicCantBeChangedException e) {
                                    ((ExceptionHandlingActivity)getActivity()).handleException(e);
                                }
                            }));
                }
            }
        };
    }

}
