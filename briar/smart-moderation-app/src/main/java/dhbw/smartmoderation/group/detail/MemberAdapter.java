package dhbw.smartmoderation.group.detail;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.briarproject.bramble.api.contact.Contact;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import dhbw.smartmoderation.R;

import dhbw.smartmoderation.data.model.Group;
import dhbw.smartmoderation.data.model.Member;
import dhbw.smartmoderation.data.model.Role;
import dhbw.smartmoderation.exceptions.GroupNotFoundException;
import dhbw.smartmoderation.uiUtils.ItemTouchHelperAdapter;
import dhbw.smartmoderation.uiUtils.ItemTouchHelperViewHolder;
import dhbw.smartmoderation.uiUtils.OnStartDragListener;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> implements ItemTouchHelperAdapter {

    private static final String TAG = MemberAdapter.class.getSimpleName();

    private final List<Member> members = new ArrayList<>();
    private Group group;
    private Context context;
    private OnMemberListener onMemberListener;
    private OnStartDragListener onStartDragListener;
    private DetailGroupController controller;

    public MemberAdapter(Context context, Collection<Member> members, OnMemberListener onMemberListener, Group group, OnStartDragListener onStartDragListener, DetailGroupController controller){
        super();
        this.context = context;
        this.group = group;
        this.onMemberListener = onMemberListener;
        this.onStartDragListener = onStartDragListener;
        this.controller = controller;
        updateMembers(members);
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ConstraintLayout layout = new ConstraintLayout(context);
        layout.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, 130));
        MemberAdapter.MemberViewHolder memberViewHolder = new MemberAdapter.MemberViewHolder(layout, context, onMemberListener, onStartDragListener, controller);
        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, value, true);
        layout.setBackgroundResource(value.resourceId);
        return memberViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        Member member = members.get(position);
        Log.d(TAG, "RecyclerView: Members [" + position + "]: Member name: " + member.getName());
        holder.setMember(member, group);
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    public void updateMembers(Collection<Member>  members){
        this.members.clear();
        this.members.addAll(members);
        notifyDataSetChanged();
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Collections.swap(members,fromPosition,toPosition);
        notifyItemMoved(fromPosition,toPosition);
    }

    @Override
    public void onItemDismiss(int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getString(R.string.member_reaffirmation,members.get(position).getName()));
        builder.setCancelable(false);
        builder.setNegativeButton(context.getString(R.string.yes), ((dialog, which) -> {

            Long memberId = members.get(position).getMemberId();
            boolean success = onMemberListener.onMemberDismiss(memberId);

            if(success) {

                members.remove(position);
                notifyItemRemoved(position);

            }

            else {

                ((DetailGroupActivity)context).reloadMemberItemTouchHelper();
            }

        }));

        builder.setPositiveButton(context.getString(R.string.no), ((dialog, which) -> {
            dialog.cancel();
            ((DetailGroupActivity)context).reloadMemberItemTouchHelper();
        }));

        android.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    static class MemberViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, ItemTouchHelperViewHolder {

        private Context context;
        private ImageView isOnline;
        private TextView name;
        private TextView ghost;
        private TextView role;
        private OnMemberListener onMemberListener;
        private DetailGroupController controller;
        private Member member;

        public MemberViewHolder(ConstraintLayout layout, Context context, MemberAdapter.OnMemberListener onMemberListener,OnStartDragListener onStartDragListener, DetailGroupController controller) {
            super(layout);
            layout.setOnClickListener(this);
            this.context = context;
            this.onMemberListener = onMemberListener;
            this.controller = controller;

            isOnline = new ImageView(this.context);
            isOnline.setId(View.generateViewId());
            isOnline.setImageResource(R.drawable.circle_green_background);
            isOnline.setLayoutParams(new ViewGroup.LayoutParams(50, 50));
            layout.addView(isOnline);

            name = new TextView(this.context);
            name.setId(View.generateViewId());
            name.setTypeface(name.getTypeface(), Typeface.BOLD);
            name.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            layout.addView(name);

            role = new TextView(this.context);
            role.setId(View.generateViewId());
            role.setText(R.string.role_moderator);
            role.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            layout.addView(role);


            ghost = new TextView(context);
            ghost.setId(View.generateViewId());
            ghost.setTypeface(ghost.getTypeface(), Typeface.BOLD);
            ghost.setText(this.context.getString(R.string.online));
            ghost.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            ghost.setGravity(Gravity.CENTER);
            ghost.setEms(4);
            ghost.setText(R.string.ghost);
            ghost.setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.default_green, null));
            layout.addView(ghost);
            ghost.setVisibility(View.GONE);

            //Init IsOnline
            ConstraintSet memberIsOnlineConstraintSet = new ConstraintSet();
            memberIsOnlineConstraintSet.clone(layout);
            memberIsOnlineConstraintSet.connect(isOnline.getId(),ConstraintSet.TOP,ConstraintSet.PARENT_ID,ConstraintSet.TOP, 20);
            memberIsOnlineConstraintSet.connect(isOnline.getId(),ConstraintSet.BOTTOM,ConstraintSet.PARENT_ID,ConstraintSet.BOTTOM, 20);
            memberIsOnlineConstraintSet.connect(isOnline.getId(),ConstraintSet.LEFT,ConstraintSet.PARENT_ID,ConstraintSet.LEFT, 20);
            memberIsOnlineConstraintSet.applyTo(layout);

            //Init Name
            ConstraintSet memberNameConstraintSet = new ConstraintSet();
            memberNameConstraintSet.clone(layout);
            memberNameConstraintSet.connect(name.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 20);
            memberNameConstraintSet.connect(name.getId(), ConstraintSet.LEFT, isOnline.getId(), ConstraintSet.RIGHT, 40);
            memberNameConstraintSet.connect(name.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 20);
            memberNameConstraintSet.applyTo(layout);

            //Init Role
            ConstraintSet memberRolleConstraintSet = new ConstraintSet();
            memberRolleConstraintSet.clone(layout);
            memberRolleConstraintSet.connect(role.getId(),ConstraintSet.TOP,ConstraintSet.PARENT_ID,ConstraintSet.TOP,20);
            memberRolleConstraintSet.connect(role.getId(),ConstraintSet.BOTTOM,ConstraintSet.PARENT_ID,ConstraintSet.BOTTOM,20);
            memberRolleConstraintSet.connect(role.getId(),ConstraintSet.LEFT, name.getId(),ConstraintSet.RIGHT,40);
            memberRolleConstraintSet.applyTo(layout);

            //Init Ghost
            ConstraintSet memberGhostConstraintSet = new ConstraintSet();
            memberGhostConstraintSet.clone(layout);
            memberGhostConstraintSet.connect(ghost.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 20);
            memberGhostConstraintSet.connect(ghost.getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 20);
            memberGhostConstraintSet.connect(ghost.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 20);
            memberGhostConstraintSet.applyTo(layout);
        }

        public void setMember(Member member,Group group) {
            this.member = member;
            name.setText(this.member.getName());
            updateVisibilityIsOnline();
            updateVisibilityRole(group);
            updateVisibilityGhost();
        }

        private void updateVisibilityGhost(){

            if (member.getIsGhost()){

                ghost.setVisibility(View.VISIBLE);

            }

            else{

                ghost.setVisibility(View.GONE);
            }
        }

        private void updateVisibilityIsOnline(){

            Contact contact = this.controller.getContact(member.getMemberId());

            if (this.controller.isConnectedToContact(contact)) {

                isOnline.setVisibility(View.VISIBLE);
            }

            else if (this.controller.getLocalAuthorId().equals(member.getMemberId())) {

                isOnline.setVisibility(View.VISIBLE);

            }

            else {

                isOnline.setVisibility(View.GONE);
            }
        }

        private void updateVisibilityRole(Group group){

            boolean setModeratorVisible = false;

            for (Role role : member.getRoles(group)){

                if (role.equals(dhbw.smartmoderation.data.model.Role.MODERATOR)){

                    setModeratorVisible = true;
                    break;
                }
            }

            if (setModeratorVisible){

                role.setVisibility(View.VISIBLE);

            }

            else{

                role.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View v) {

            this.onMemberListener.onMemberClick(this.member.getMemberId());
        }


        @Override
        public void onItemSelected() {

        }

        @Override
        public void onItemClear() {

        }
    }

    public interface OnMemberListener{

        void onMemberClick(Long memberId);

        boolean onMemberDismiss(Long memberId);
    }
}
