package dhbw.smartmoderation.meeting.detail;

import android.content.Context;
import android.graphics.Typeface;
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
import java.util.concurrent.TimeUnit;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.data.model.Topic;
import dhbw.smartmoderation.data.model.TopicStatus;

public class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.TopicViewHolder> {

    private Context context;
    private MeetingDetailController controller;
    private ArrayList<Topic> topicList;

    public TopicAdapter(Context context, MeetingDetailController controller) {
        this.context = context;
        this.controller = controller;
        this.topicList = new ArrayList<>();
        Collection<Topic> topics = this.controller.getTopics();
        updateTopics(topics);
    }

    public void updateTopics(Collection<Topic> topics) {
        this.topicList.clear();
        this.topicList.addAll(topics);
        Collections.sort(this.topicList, (o1, o2) -> o1.getStatus().compareTo(o2.getStatus()));
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TopicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ConstraintLayout constraintLayout = new ConstraintLayout(context);
        constraintLayout.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, 150));
        TopicViewHolder topicViewHolder = new TopicViewHolder(constraintLayout, context, this);
        return topicViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull TopicViewHolder holder, int position) {
        Topic topic = this.topicList.get(position);
        String title = topic.getTitle();
        String duration = TimeUnit.MILLISECONDS.toMinutes(topic.getDuration()) + " " + context.getString(R.string.minute) + ".";
        holder.setTopic(topic);
        holder.getTitle().setText(title + " - " + duration);

        TopicStatus status = topic.getTopicStatus();

        if (status == TopicStatus.RUNNING) {
            holder.getStatus().setVisibility(View.VISIBLE);
            holder.getStatus().setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.colorPrimaryDark, null));
            holder.getStatus().setText(context.getString(R.string.running));
        } else if (status == TopicStatus.FINISHED) {
            holder.getStatus().setVisibility(View.VISIBLE);
            holder.getStatus().setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.default_green, null));
            holder.getStatus().setText(context.getString(R.string.finished));
        } else {
            holder.getStatus().setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return this.topicList.size();
    }

    public Context getContext() {
        return this.context;
    }


    static class TopicViewHolder extends RecyclerView.ViewHolder {

        private TextView title;
        private TextView status;
        private Topic topic;

        public TopicViewHolder(ConstraintLayout constraintLayout, Context context, TopicAdapter topicAdapter) {
            super(constraintLayout);

            title = new TextView(context);
            title.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            title.setId(View.generateViewId());
            title.setTextSize(12);
            constraintLayout.addView(title);

            status = new TextView(context);
            status.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            status.setId(View.generateViewId());
            status.setTextSize(12);
            status.setTypeface(status.getTypeface(), Typeface.BOLD);
            status.setGravity(Gravity.CENTER);
            status.setEms(5);
            status.setVisibility(View.GONE);
            constraintLayout.addView(status);

            ConstraintSet titleConstraintSet = new ConstraintSet();
            titleConstraintSet.clone(constraintLayout);
            titleConstraintSet.connect(title.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 12);
            titleConstraintSet.connect(title.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 20);
            titleConstraintSet.connect(title.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 12);
            titleConstraintSet.applyTo(constraintLayout);

            ConstraintSet statusConstraintSet = new ConstraintSet();
            statusConstraintSet.clone(constraintLayout);
            statusConstraintSet.connect(status.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 20);
            statusConstraintSet.connect(status.getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 20);
            statusConstraintSet.connect(status.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 20);
            statusConstraintSet.applyTo(constraintLayout);
        }

        public void setTopic(Topic topic) {
            this.topic = topic;
        }

        public Topic getTopic() {
            return this.topic;
        }

        public TextView getTitle() {
            return this.title;
        }

        public TextView getStatus() {
            return this.status;
        }
    }

}
