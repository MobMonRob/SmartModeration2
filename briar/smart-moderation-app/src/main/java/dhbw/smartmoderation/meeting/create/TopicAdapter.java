package dhbw.smartmoderation.meeting.create;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import dhbw.smartmoderation.data.model.Topic;
import dhbw.smartmoderation.data.model.TopicStatus;
import dhbw.smartmoderation.group.detail.MemberAdapter;
import dhbw.smartmoderation.uiUtils.ItemTouchHelperAdapter;

public class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.TopicViewHolder> implements ItemTouchHelperAdapter {

    private Context context;
    private ArrayList<Topic> topicList;
    private OnTopicListener onTopicListener;

    public TopicAdapter(Context context, Collection<Topic> topicList, OnTopicListener onTopicListener) {
        this.context = context;
        this.onTopicListener = onTopicListener;
        this.topicList = new ArrayList<>();
        updateTopics(topicList);
    }

    public void updateTopics(Collection<Topic> topics) {
        this.topicList.clear();
        this.topicList.addAll(topics);
        this.notifyDataSetChanged();
    }

    public void updateTopic(Topic previousTopic, String title, String duration) {
        topicList.remove(previousTopic);
        Topic topic = new Topic();
        topic.setTitle(title);
        long minutes = Long.parseLong(duration);
        topic.setDuration(TimeUnit.MINUTES.toMillis(minutes));
        topic.setStatus(TopicStatus.UPCOMING.name());
        topicList.add(topic);
        notifyDataSetChanged();
    }

    public void addTopic (String title, String duration) {
        Topic topic = new Topic();
        topic.setTitle(title);
        long minutes = Long.parseLong(duration);
        topic.setDuration(TimeUnit.MINUTES.toMillis(minutes));
        topic.setStatus(TopicStatus.UPCOMING.name());
        topicList.add(topic);
        notifyDataSetChanged();
    }

    public ArrayList<Topic> getTopicList() {
        return this.topicList;
    }

    @NonNull
    @Override
    public TopicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ConstraintLayout constraintLayout = new ConstraintLayout(context);
        constraintLayout.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, 100));
        TopicViewHolder topicViewHolder = new TopicViewHolder(constraintLayout, context, this, onTopicListener);
        return topicViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull TopicViewHolder holder, int position) {
        Topic topic = this.topicList.get(position);
        String title = topic.getTitle();
        String duration = TimeUnit.MILLISECONDS.toMinutes(topic.getDuration()) + " min.";
        holder.setTopic(topic);
        holder.getTextView().setText(title + " - " + duration);
    }

    @Override
    public int getItemCount() {
        return this.topicList.size();
    }

    public Context getContext() {
        return this.context;
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {

    }

    @Override
    public void onItemDismiss(int position) {
        topicList.remove(position);
        notifyItemRemoved(position);
    }

    static class TopicViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView title;
        private Topic topic;
        private TopicAdapter.OnTopicListener onTopicListener;


        public TopicViewHolder(ConstraintLayout constraintLayout, Context context, TopicAdapter topicAdapter, OnTopicListener onTopicListener) {
            super(constraintLayout);
            constraintLayout.setOnClickListener(this);
            title = new TextView(context);
            title.setId(View.generateViewId());
            constraintLayout.addView(title);
            this.onTopicListener = onTopicListener;

            TypedValue value = new TypedValue();
            context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, value, true);
            constraintLayout.setBackgroundResource(value.resourceId);

            ConstraintSet titleConstraintSet = new ConstraintSet();
            titleConstraintSet.clone(constraintLayout);
            titleConstraintSet.connect(title.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 12);
            titleConstraintSet.connect(title.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 20);
            titleConstraintSet.connect(title.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 12);
            titleConstraintSet.applyTo(constraintLayout);
        }

        public void setTopic(Topic topic) {
            this.topic = topic;
        }

        public TextView getTextView() {
            return this.title;
        }

        @Override
        public void onClick(View v) {

            onTopicListener.onTopicClick(v, topic);

        }
    }

    public interface OnTopicListener {

        void onTopicClick(View view, Topic topic);

    }
}
