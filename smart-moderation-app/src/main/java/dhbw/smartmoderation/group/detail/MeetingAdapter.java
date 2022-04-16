package dhbw.smartmoderation.group.detail;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.data.model.Meeting;
import dhbw.smartmoderation.uiUtils.ItemTouchHelperAdapter;
import dhbw.smartmoderation.uiUtils.ItemTouchHelperViewHolder;
import dhbw.smartmoderation.util.Util;

public class MeetingAdapter extends RecyclerView.Adapter<MeetingAdapter.MeetingViewHolder> implements ItemTouchHelperAdapter {

    private static final String TAG = MeetingAdapter.class.getSimpleName();

    private final List<Meeting> meetings = new ArrayList<>();

    private final Context context;
    private final OnMeetingListener onMeetingListener;

    public MeetingAdapter(Context context, Collection<Meeting> meetings, OnMeetingListener onMeetingListener) {
        super();
        this.context = context;
        updateMeetings(meetings);
        this.onMeetingListener = onMeetingListener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateMeetings(Collection<Meeting> meetings) {
        this.meetings.clear();
        this.meetings.addAll(meetings);

        Collections.sort(this.meetings, ((o1, o2) -> {
            if (o1.getDate() < o2.getDate())
                return -1;
            else if (o1.getDate() > o2.getDate())
                return 1;
            else if (o1.getDate() == o2.getDate() && o1.getStartTime() < o2.getStartTime())
                return -1;
            else if (o1.getDate() == o2.getDate() && o1.getStartTime() > o2.getStartTime())
                return 1;
            return 0;
        }));
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MeetingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ConstraintLayout layout = new ConstraintLayout(context);
        layout.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, 130));
        MeetingAdapter.MeetingViewHolder meetingViewHolder = new MeetingViewHolder(layout, context, onMeetingListener);
        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, value, true);
        layout.setBackgroundResource(value.resourceId);
        return meetingViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MeetingViewHolder holder, int position) {
        Meeting meeting = meetings.get(position);
        Log.d(TAG, "RecyclerView: Meetings [" + position + "]: Meeting id: " + meeting.getMeetingId());
        holder.setMeeting(meeting);
    }

    @Override
    public int getItemCount() {
        return meetings.size();
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Collections.swap(meetings, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemDismiss(int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getString(R.string.meeting_reaffirmation, meetings.get(position).getCause()));
        builder.setCancelable(false);
        builder.setNegativeButton(context.getString(R.string.yes), ((dialog, which) -> {
            Long meetingId = meetings.get(position).getMeetingId();
            meetings.remove(position);
            notifyItemRemoved(position);
            onMeetingListener.onMeetingDismiss(meetingId);
        }));

        builder.setPositiveButton(context.getString(R.string.no), ((dialog, which) -> {
            dialog.cancel();
            ((DetailGroupActivity) context).reloadMeetingItemTouchHelper();
        }));

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    static class MeetingViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, ItemTouchHelperViewHolder {

        private Meeting meeting;

        private final TextView name;
        private final TextView startDateTime;
        private final TextView isOnline;

        private final OnMeetingListener listener;

        public MeetingViewHolder(ConstraintLayout layout, Context context, OnMeetingListener listener) {
            super(layout);
            layout.setOnClickListener(this);
            this.listener = listener;

            name = new TextView(context);
            name.setId(View.generateViewId());
            name.setTypeface(name.getTypeface(), Typeface.BOLD);
            name.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            layout.addView(name);

            startDateTime = new TextView(context);
            startDateTime.setId(View.generateViewId());
            startDateTime.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            layout.addView(startDateTime);

            isOnline = new TextView(context);
            isOnline.setId(View.generateViewId());
            isOnline.setTypeface(isOnline.getTypeface(), Typeface.BOLD);
            isOnline.setText(context.getString(R.string.online));
            isOnline.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            isOnline.setGravity(Gravity.CENTER);
            isOnline.setEms(4);
            isOnline.setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.default_green, null));
            layout.addView(isOnline);
            isOnline.setVisibility(View.GONE);

            ConstraintSet meetingNameConstraintSet = new ConstraintSet();
            meetingNameConstraintSet.clone(layout);
            meetingNameConstraintSet.connect(name.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 20);
            meetingNameConstraintSet.connect(name.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 20);
            meetingNameConstraintSet.connect(name.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 20);
            meetingNameConstraintSet.applyTo(layout);

            ConstraintSet meetingDateTimeConstraintSet = new ConstraintSet();
            meetingDateTimeConstraintSet.clone(layout);
            meetingDateTimeConstraintSet.connect(startDateTime.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 20);
            meetingDateTimeConstraintSet.connect(startDateTime.getId(), ConstraintSet.LEFT, name.getId(), ConstraintSet.RIGHT, 40);
            meetingDateTimeConstraintSet.connect(startDateTime.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 20);
            meetingDateTimeConstraintSet.applyTo(layout);

            ConstraintSet meetingIsOnlineConstraintSet = new ConstraintSet();
            meetingIsOnlineConstraintSet.clone(layout);
            meetingIsOnlineConstraintSet.connect(isOnline.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 20);
            meetingIsOnlineConstraintSet.connect(isOnline.getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 20);
            meetingIsOnlineConstraintSet.connect(isOnline.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 20);
            meetingIsOnlineConstraintSet.applyTo(layout);
        }


        @Override
        public void onClick(View v) {
            listener.onMeetingClick(this.meeting.getMeetingId());
        }

        public void setMeeting(Meeting meeting) {
            this.meeting = meeting;
            updateName();
            updateStartDateTime();
            updateIsOnline();
        }

        private void updateName() {
            name.setText(meeting.getCause());
        }

        private void updateStartDateTime() {
            String timeString = this.meeting.getDateAsString() + " " + Util.convertMilliSecondsToTimeString(this.meeting.getStartTime()) + " Uhr";
            startDateTime.setText(timeString);
        }

        private void updateIsOnline() {
            if (meeting.getOnline()) {
                isOnline.setVisibility(View.VISIBLE);
            } else {
                isOnline.setVisibility(View.GONE);
            }
        }

        @Override
        public void onItemSelected() {

        }

        @Override
        public void onItemClear() {

        }
    }

    public interface OnMeetingListener {

        void onMeetingClick(Long meetingId);

        void onMeetingDismiss(Long meetingId);
    }
}
