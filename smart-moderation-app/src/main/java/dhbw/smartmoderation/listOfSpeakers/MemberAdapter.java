package dhbw.smartmoderation.listOfSpeakers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.data.model.Member;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    private final Context context;
    private final ArrayList<Member> memberList;
    private Member selectedMember;
    private int index = -1;

    public MemberAdapter(Context context, Collection<Member> members) {
        this.context = context;
        this.memberList = new ArrayList<>();
        updateMemberList(members);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateMemberList(Collection<Member> memberList) {
        this.memberList.clear();
        this.memberList.addAll(memberList);
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ConstraintLayout constraintLayout = new ConstraintLayout(context);
        constraintLayout.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, 110));
        return new MemberViewHolder(constraintLayout, context);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Member member = this.memberList.get(position);
        holder.getMemberName().setText(member.getName());

        holder.itemView.setOnClickListener(v -> {
            this.selectedMember = member;
            index = position;
            notifyDataSetChanged();
        });

        if (index == position)
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.light_grey, null));
        else
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.default_color, null));
    }

    @Override
    public int getItemCount() {
        return this.memberList.size();
    }

    public Member getSelectedMember() {
        return this.selectedMember;
    }

    public Context getContext() {
        return this.context;
    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {

        private final TextView memberName;

        public MemberViewHolder(ConstraintLayout constraintLayout, Context context) {

            super(constraintLayout);
            memberName = new TextView(context);
            memberName.setId(View.generateViewId());
            memberName.setTextSize(12);
            constraintLayout.addView(memberName);

            ConstraintSet memberConstraintSet = new ConstraintSet();
            memberConstraintSet.clone(constraintLayout);
            memberConstraintSet.connect(memberName.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 20);
            memberConstraintSet.connect(memberName.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 20);
            memberConstraintSet.connect(memberName.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 20);
            memberConstraintSet.applyTo(constraintLayout);
        }


        public TextView getMemberName() {
            return this.memberName;
        }
    }
}
