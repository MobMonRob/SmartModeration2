package dhbw.smartmoderation.group.create;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collection;
import dhbw.smartmoderation.R;
import dhbw.smartmoderation.data.model.Ghost;
import dhbw.smartmoderation.data.model.IContact;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

   private Context context;
   private ArrayList<IContact> contactList;
   private ArrayList<IContact> selectedContacts;

    public
    ContactAdapter(Context context, Collection<IContact> contactList) {
        this.context = context;
        this.contactList = new ArrayList<>();
        this.selectedContacts = new ArrayList<>();
        updateContacts(contactList);
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ConstraintLayout constraintLayout = new ConstraintLayout(context);
        constraintLayout.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, 130));
        ContactViewHolder contactViewHolder = new ContactViewHolder(constraintLayout, context, this);
        return contactViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        IContact contact = contactList.get(position);
        String name = contact.getName();
        holder.setContact(contact);
        holder.getTextView().setText(name);

        if(contact instanceof Ghost) {
            holder.getGhostHint().setText(context.getString(R.string.ghost));
            holder.getGhostHint().setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public Context getContext() {
        return this.context;
    }

    public ArrayList<IContact> getSelectedContacts() {
        return this.selectedContacts;
    }

    public void updateContacts(Collection<IContact> contactList) {
        this.contactList.clear();
        this.contactList.addAll(contactList);
    }

    public boolean atLeastOneContactSelected() {
        return this.selectedContacts.size() > 0;
    }

    public void addGhost(String firstName, String lastName) {
        Ghost ghost = new Ghost();
        ghost.setFirstName(firstName);
        ghost.setLastName(lastName);
        contactList.add(ghost);
        notifyDataSetChanged();
    }

   static class ContactViewHolder extends RecyclerView.ViewHolder {
        private TextView contactName;
        private CheckBox selectCheckBox;
        private TextView ghostHint;
        private IContact contact;

        public ContactViewHolder(ConstraintLayout constraintLayout, Context context, ContactAdapter adapter) {
            super(constraintLayout);

            contactName = new TextView(context);
            contactName.setId(View.generateViewId());
            constraintLayout.addView(contactName);

            selectCheckBox = new CheckBox(context);
            selectCheckBox.setId(View.generateViewId());
            selectCheckBox.setButtonTintList(ColorStateList.valueOf(ResourcesCompat.getColor(context.getResources(), R.color.colorPrimary, null)));
            constraintLayout.addView(selectCheckBox);

            if (contact instanceof Ghost) {
                selectCheckBox.setSelected(true);
                adapter.getSelectedContacts().add(contact);
            }

            selectCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    adapter.getSelectedContacts().add(contact);
                } else {
                    adapter.getSelectedContacts().remove(contact);
                }
            });

            ConstraintSet checkBoxConstraintSet = new ConstraintSet();
            checkBoxConstraintSet.clone(constraintLayout);
            checkBoxConstraintSet.connect(selectCheckBox.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 20);
            checkBoxConstraintSet.connect(selectCheckBox.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 20);
            checkBoxConstraintSet.connect(selectCheckBox.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 20);
            checkBoxConstraintSet.applyTo(constraintLayout);

            ConstraintSet textViewConstraintSet = new ConstraintSet();
            textViewConstraintSet.clone(constraintLayout);
            textViewConstraintSet.connect(contactName.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 20);
            textViewConstraintSet.connect(contactName.getId(), ConstraintSet.LEFT, selectCheckBox.getId(), ConstraintSet.RIGHT, 50);
            textViewConstraintSet.connect(contactName.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 20);
            textViewConstraintSet.applyTo(constraintLayout);

            ghostHint = new TextView(context);
            ghostHint.setId(View.generateViewId());
            ghostHint.setTypeface(ghostHint.getTypeface(), Typeface.BOLD);
            ghostHint.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            ghostHint.setGravity(Gravity.CENTER);
            ghostHint.setEms(4);
            ghostHint.setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.default_green, null));
            constraintLayout.addView(ghostHint);
            ghostHint.setVisibility(View.GONE);

            ConstraintSet ghostFieldConstraintSet = new ConstraintSet();
            ghostFieldConstraintSet.clone(constraintLayout);
            ghostFieldConstraintSet.connect(ghostHint.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 20);
            ghostFieldConstraintSet.connect(ghostHint.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 20);
            ghostFieldConstraintSet.connect(ghostHint.getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 20);
            ghostFieldConstraintSet.applyTo(constraintLayout);

        }

        public void setContact(IContact contact) {
            this.contact = contact;
        }

        public TextView getTextView() {
            return this.contactName;
        }

        public TextView getGhostHint() {
            return this.ghostHint;
        }
    }

}
