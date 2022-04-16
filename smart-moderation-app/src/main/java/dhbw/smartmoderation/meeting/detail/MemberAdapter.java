package dhbw.smartmoderation.meeting.detail;

import android.annotation.SuppressLint;
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

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.data.model.Attendance;
import dhbw.smartmoderation.data.model.Member;
import dhbw.smartmoderation.data.model.Role;


public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    private final Context context;
    private final MeetingDetailController controller;
    private final ArrayList<Member> memberList;

    public MemberAdapter(Context context, MeetingDetailController controller) {
        this.context = context;
        this.controller = controller;
        this.memberList = new ArrayList<>();
        Collection<Member> members = this.controller.getMembers();
        updateMemberList(members);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateMemberList(Collection<Member> members) {
        this.memberList.clear();
        this.memberList.addAll(members);

        Collections.sort(this.memberList, (o1, o2) -> {
            if (o1.getAttendance(controller.getMeeting()).getNumber() < o2.getAttendance(controller.getMeeting()).getNumber()) {
                return -1;
            } else if (o1.getAttendance(controller.getMeeting()).getNumber() > o2.getAttendance(controller.getMeeting()).getNumber()) {
                return 1;
            }
            return 0;
        });
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ConstraintLayout constraintLayout = new ConstraintLayout(context);
        constraintLayout.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, 150));
        return new MemberViewHolder(constraintLayout, context);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        Member member = this.memberList.get(position);
        holder.setMember(member);
        holder.getName().setText(member.getName());

        holder.getRole().setVisibility(View.GONE);

        if (member.getRoles(this.controller.getGroup()).contains(Role.MODERATOR)) {
            holder.getRole().setText(context.getString(R.string.moderator));
            holder.getRole().setVisibility(View.VISIBLE);
        }

        Attendance status = member.getAttendance(this.controller.getMeeting());

        if (status == Attendance.PRESENT) {
            holder.getStatus().setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.default_green, null));
            holder.getStatus().setText(context.getString(R.string.present));
        } else if (status == Attendance.EXCUSED) {
            holder.getStatus().setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.colorPrimaryDark, null));
            holder.getStatus().setText(context.getString(R.string.excused));
        } else {
            holder.getStatus().setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.default_blue, null));
            holder.getStatus().setText(context.getString(R.string.absent));
        }
    }

    @Override
    public int getItemCount() {
        return this.memberList.size();
    }

    public Context getContext() {
        return this.context;
    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {

        private Member member;
        private final TextView name;
        private final TextView role;
        private final TextView status;

        public MemberViewHolder(ConstraintLayout constraintLayout, Context context) {
            super(constraintLayout);

            name = new TextView(context);
            name.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            name.setId(View.generateViewId());
            name.setTextSize(12);
            constraintLayout.addView(name);

            role = new TextView(context);
            role.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            role.setId(View.generateViewId());
            role.setTextSize(10);
            role.setVisibility(View.GONE);
            constraintLayout.addView(role);

            status = new TextView(context);
            status.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            status.setId(View.generateViewId());
            status.setTextSize(12);
            status.setTypeface(status.getTypeface(), Typeface.BOLD);
            status.setGravity(Gravity.CENTER);
            status.setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.default_red, null));
            status.setText(context.getString(R.string.absent));
            status.setEms(5);
            constraintLayout.addView(status);

            ConstraintSet nameConstraintSet = new ConstraintSet();
            nameConstraintSet.clone(constraintLayout);
            nameConstraintSet.connect(name.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 20);
            nameConstraintSet.connect(name.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 20);
            nameConstraintSet.connect(name.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 20);
            nameConstraintSet.applyTo(constraintLayout);

            ConstraintSet roleConstraintSet = new ConstraintSet();
            roleConstraintSet.clone(constraintLayout);
            roleConstraintSet.connect(role.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 20);
            roleConstraintSet.connect(role.getId(), ConstraintSet.LEFT, name.getId(), ConstraintSet.RIGHT, 50);
            roleConstraintSet.connect(role.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 20);
            roleConstraintSet.applyTo(constraintLayout);

            ConstraintSet statusConstraintSet = new ConstraintSet();
            statusConstraintSet.clone(constraintLayout);
            statusConstraintSet.connect(status.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 20);
            statusConstraintSet.connect(status.getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 20);
            statusConstraintSet.connect(status.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 20);
            statusConstraintSet.applyTo(constraintLayout);


        }

        public void setMember(Member member) {
            this.member = member;
        }

        public Member getMember() {
            return this.member;
        }

        public TextView getName() {
            return this.name;
        }

        public TextView getRole() {
            return this.role;
        }

        public TextView getStatus() {
            return this.status;
        }
    }
}
