package dhbw.smartmoderation.group.detail;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.connection.synchronization.SynchronizableDataType;
import dhbw.smartmoderation.data.model.IContact;
import dhbw.smartmoderation.data.model.Meeting;
import dhbw.smartmoderation.data.model.Member;
import dhbw.smartmoderation.exceptions.GroupNotFoundException;
import dhbw.smartmoderation.exceptions.NoContactsFoundException;
import dhbw.smartmoderation.exceptions.SmartModerationException;
import dhbw.smartmoderation.group.create.ContactAdapter;
import dhbw.smartmoderation.group.personInfo.PersonInfoActivity;
import dhbw.smartmoderation.group.settings.SettingsActivity;
import dhbw.smartmoderation.meeting.create.CreateMeetingActivity;
import dhbw.smartmoderation.meeting.detail.BaseActivity;
import dhbw.smartmoderation.uiUtils.OnStartDragListener;
import dhbw.smartmoderation.uiUtils.SimpleItemTouchHelperCallback;
import dhbw.smartmoderation.util.UpdateableExceptionHandlingActivity;

public class DetailGroupActivity extends UpdateableExceptionHandlingActivity implements MemberAdapter.OnMemberListener, MeetingAdapter.OnMeetingListener, OnStartDragListener {

    private static final String TAG = DetailGroupActivity.class.getSimpleName();
    private DetailGroupController controller;
    private boolean allFabVisible;
    private View popup;
    private AlertDialog alertDialog;

    enum Answer {YES, NO}

    private ItemTouchHelper memberItemTouchHelper;
    private ItemTouchHelper meetingsItemTouchHelper;
    private RecyclerView recMembers;
    private RecyclerView recMeetings;
    private MemberAdapter memberAdapter;
    private MeetingAdapter meetingAdapter;
    private ArrayList<FloatingActionButton> fabList;
    private ArrayList<TextView> textList;
    private SwipeRefreshLayout pullToRefresh;
    private final ContactAdapter adapter = getContactAdapter();

    private ContactAdapter getContactAdapter() {

        return new ContactAdapter(this, new ArrayList<>());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_group);

        pullToRefresh = findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(this::updateUI);

        Intent intent = getIntent();
        Long groupId = intent.getLongExtra("groupId", 0);

        try {
            controller = new DetailGroupController(groupId);

        } catch (NullPointerException exception) {
            handleException(exception);
        }

        String groupName = "";

        try {
            groupName = controller.getGroup().getName();
        } catch (GroupNotFoundException e) {
            e.printStackTrace();
        }

        setTitle(getString(R.string.title_activity_group_detail, groupName));

        fabList = new ArrayList<>();
        textList = new ArrayList<>();

        recMembers = findViewById(R.id.recMembers);
        recMeetings = findViewById(R.id.recMeetings);

        FloatingActionButton generalFab = findViewById(R.id.generalFab);
        generalFab.setVisibility(View.GONE);
        FloatingActionButton createMemberFab = findViewById(R.id.createMemberFab);
        FloatingActionButton createMeetingFab = findViewById(R.id.createMeetingFab);
        FloatingActionButton createGhostFab = findViewById(R.id.createGhostFab);

        TextView createMemberText = findViewById(R.id.createMemberText);
        TextView createMeetingText = findViewById(R.id.createMeetingText);
        TextView createGhostText = findViewById(R.id.createGhostText);

        textList.add(createMemberText);

        if (this.controller.isLocalAuthorModerator()) {
            generalFab.setVisibility(View.VISIBLE);
            fabList.add(createMemberFab);
            fabList.add(createMeetingFab);
            fabList.add(createGhostFab);
            textList.add(createMeetingText);
            textList.add(createGhostText);
        }


        generalFab.setOnClickListener(v -> {
            if (!allFabVisible) {
                for (FloatingActionButton fab : fabList)
                    fab.show();

                for (TextView textView : textList)
                    textView.setVisibility(View.VISIBLE);

                allFabVisible = true;
            } else {
                for (FloatingActionButton fab : fabList)
                    fab.hide();

                for (TextView textView : textList)
                    textView.setVisibility(View.GONE);

                allFabVisible = false;
            }
        });

        createMemberFab.setVisibility(View.GONE);
        createMeetingFab.setVisibility(View.GONE);
        createGhostFab.setVisibility(View.GONE);
        createMemberFab.setOnClickListener(this::onAddMember);
        createMeetingFab.setOnClickListener(this::onCreateMeeting);
        createGhostFab.setOnClickListener(this::onCreateGhost);
        allFabVisible = false;

        createMemberText.setVisibility(View.GONE);
        createMeetingText.setVisibility(View.GONE);
        createGhostText.setVisibility(View.GONE);

        LinearLayoutManager meetingsLayoutManager = new LinearLayoutManager(this);
        DividerItemDecoration meetingsDividerItemDecoration = new DividerItemDecoration(recMeetings.getContext(), meetingsLayoutManager.getOrientation());
        recMeetings.setLayoutManager(meetingsLayoutManager);
        recMeetings.addItemDecoration(meetingsDividerItemDecoration);

        LinearLayoutManager memberLayoutManager = new LinearLayoutManager(this);
        DividerItemDecoration memberDividerItemDecoration = new DividerItemDecoration(recMembers.getContext(), memberLayoutManager.getOrientation());
        recMembers.setLayoutManager(memberLayoutManager);
        recMembers.addItemDecoration(memberDividerItemDecoration);

        try {
            memberAdapter = new MemberAdapter(this, controller.getMembers(), this, controller.getGroup(), this, controller);
            meetingAdapter = new MeetingAdapter(this, controller.getMeetings(), this);
        } catch (GroupNotFoundException exception) {
            handleException(exception);
            Log.d(TAG, exception.getMessage(), exception);
        }

        recMembers.setAdapter(memberAdapter);
        recMeetings.setAdapter(meetingAdapter);

        if (this.controller.isLocalAuthorModerator()) {
            ItemTouchHelper.Callback memberCallback = new SimpleItemTouchHelperCallback(memberAdapter, R.color.default_red, R.drawable.trash, ItemTouchHelper.START);
            memberItemTouchHelper = new ItemTouchHelper(memberCallback);
            memberItemTouchHelper.attachToRecyclerView(recMembers);

            ItemTouchHelper.Callback meetingCallback = new SimpleItemTouchHelperCallback(meetingAdapter, R.color.default_red, R.drawable.trash, ItemTouchHelper.START);
            meetingsItemTouchHelper = new ItemTouchHelper(meetingCallback);
            meetingsItemTouchHelper.attachToRecyclerView(recMeetings);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.settingsButton) {
            showPopUp();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public void reloadMemberItemTouchHelper() {
        this.memberItemTouchHelper.attachToRecyclerView(null);

        try {
            this.memberAdapter.updateMembers(this.controller.getMembers());
        } catch (GroupNotFoundException e) {
            e.printStackTrace();
        }

        this.memberItemTouchHelper.attachToRecyclerView(this.recMembers);
    }

    public void reloadMeetingItemTouchHelper() {

        this.meetingsItemTouchHelper.attachToRecyclerView(null);

        try {
            this.meetingAdapter.updateMeetings(this.controller.getMeetings());
        } catch (GroupNotFoundException e) {
            e.printStackTrace();
        }

        this.meetingsItemTouchHelper.attachToRecyclerView(this.recMeetings);
    }

    public void showPopUp() {

        View view = findViewById(R.id.settingsButton);

        Context wrapper = new ContextThemeWrapper(this, R.style.popUpMenuStyle);

        PopupMenu popupMenu = new PopupMenu(wrapper, view);
        MenuInflater inflater = popupMenu.getMenuInflater();

        if (this.controller.isLocalAuthorModerator()) {
            inflater.inflate(R.menu.options_menu_moderator, popupMenu.getMenu());
        } else {
            inflater.inflate(R.menu.options_menu, popupMenu.getMenu());
        }

        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.settingsNavigation) {
                Intent groupSettingsIntent = new Intent(this, SettingsActivity.class);
                groupSettingsIntent.putExtra("groupId", controller.getGroupId());
                startActivity(groupSettingsIntent);
                return true;
            } else if (item.getItemId() == R.id.leaveNavigation) {
                onLeaveGroup();
                return true;
            } else if (item.getItemId() == R.id.deleteNavigation) {
                onDeleteGroup();
                return true;
            } else {
                return false;
            }

        });
    }

    @Override
    public Collection<SynchronizableDataType> getSynchronizableDataTypes() {
        Collection<SynchronizableDataType> dataTypes = new ArrayList<>();
        dataTypes.add(SynchronizableDataType.MEETING);
        dataTypes.add(SynchronizableDataType.MEMBER);
        dataTypes.add(SynchronizableDataType.GROUP);
        return dataTypes;
    }

    @SuppressLint("InflateParams")
    private void onCreateGhost(View view) {

        LayoutInflater inflater = LayoutInflater.from(this);
        popup = inflater.inflate(R.layout.popup_add_ghost, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(popup);

        alertDialogBuilder.setCancelable(false);

        alertDialog = alertDialogBuilder.create();

        Button addBtn = popup.findViewById(R.id.addButton);

        addBtn.setOnClickListener(v -> {
            userChoice(DetailGroupActivity.Answer.YES);
            alertDialog.cancel();
        });

        Button cancelBtn = popup.findViewById(R.id.cancelButton);

        cancelBtn.setOnClickListener(v -> {
            userChoice(DetailGroupActivity.Answer.NO);
            alertDialog.cancel();
        });

        alertDialog.show();
    }

    @SuppressLint("InflateParams")
    private void onAddMember(View view) {

        LayoutInflater inflater = LayoutInflater.from(this);
        popup = inflater.inflate(R.layout.popup_select_contacts, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(popup);

        alertDialogBuilder.setCancelable(false);

        alertDialog = alertDialogBuilder.create();

        RecyclerView recContact = popup.findViewById(R.id.recPopUpContacts);
        Button confirmButton = popup.findViewById(R.id.btnConfirmContacts);
        Button cancelButton = popup.findViewById(R.id.btnCancelContacts);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recContact.setLayoutManager(linearLayoutManager);

        Collection<IContact> contacts = new ArrayList<>();
        Collection<Member> members = new ArrayList<>();

        try {
            contacts = controller.getContacts();
        } catch (NoContactsFoundException e) {
            handleException(e);
        }

        try {
            members = controller.getGroup().getUniqueMembers();
        } catch (GroupNotFoundException e) {
            handleException(e);
        }

        for (Member member : members) {
            Iterator<IContact> iterator = contacts.iterator();
            while (iterator.hasNext()) {
                IContact contact = iterator.next();
                if (contact.getId().equals(member.getMemberId())) {
                    iterator.remove();
                }
            }
        }

        adapter.updateContacts(contacts);
        recContact.setAdapter(adapter);

        DividerItemDecoration contactsDividerItemDecoration = new DividerItemDecoration(recContact.getContext(), linearLayoutManager.getOrientation());
        recContact.addItemDecoration(contactsDividerItemDecoration);

        confirmButton.setOnClickListener(v -> {
            alertDialog.cancel();
            DetailGroupAsyncTask detailGroupAsyncTask = new DetailGroupAsyncTask("addMember");
            detailGroupAsyncTask.execute();
        });

        cancelButton.setOnClickListener(v -> alertDialog.cancel());

        alertDialog.show();
    }

    public void userChoice(DetailGroupActivity.Answer choice) {

        if (choice == DetailGroupActivity.Answer.YES) {
            EditText firstNameInput = popup.findViewById(R.id.firstNameInput);
            EditText lastNameInput = popup.findViewById(R.id.lastNameInput);

            String firstName = String.valueOf(firstNameInput.getText());
            String lastName = String.valueOf(lastNameInput.getText());

            DetailGroupAsyncTask detailGroupAsyncTask = new DetailGroupAsyncTask("addGhost");
            detailGroupAsyncTask.execute(firstName, lastName);
        }
    }

    private void onCreateMeeting(View view) {
        Intent createMeetingIntent = new Intent(this, CreateMeetingActivity.class);
        createMeetingIntent.putExtra("groupId", controller.getGroupId());
        startActivity(createMeetingIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void updateUI() {
        DetailGroupAsyncTask detailGroupAsyncTask = new DetailGroupAsyncTask("update");
        detailGroupAsyncTask.execute();
    }

    private void onDeleteGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.group_reaffirmation));
        builder.setCancelable(false);
        builder.setNegativeButton(getString(R.string.yes), ((dialog, which) -> {
            dialog.cancel();
            DetailGroupAsyncTask detailGroupAsyncTask = new DetailGroupAsyncTask("deleteGroup");
            detailGroupAsyncTask.execute();
        }));

        builder.setPositiveButton(getString(R.string.no), ((dialog, which) -> dialog.cancel()));

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void onLeaveGroup() {
        if (controller.isLocalAuthorModerator() && controller.getModeratorCount() < 2) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.SingleModerator));
            builder.setCancelable(false);
            builder.setNeutralButton(getString(R.string.ok), ((dialog, which) -> dialog.cancel()));
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.group_leave_reaffirmation));
        builder.setCancelable(false);
        builder.setNegativeButton(getString(R.string.yes), ((dialog, which) -> {
            dialog.cancel();
            DetailGroupAsyncTask detailGroupAsyncTask = new DetailGroupAsyncTask("leaveGroup");
            detailGroupAsyncTask.execute();
        }));

        builder.setPositiveButton(getString(R.string.no), ((dialog, which) -> dialog.cancel()));

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onMeetingClick(Long meetingId) {
        Intent meetingDetailIntent = new Intent(this, BaseActivity.class);
        meetingDetailIntent.putExtra("meetingId", meetingId);
        startActivity(meetingDetailIntent);
    }

    @Override
    public void onMeetingDismiss(Long meetingId) {
        DetailGroupAsyncTask detailGroupAsyncTask = new DetailGroupAsyncTask("deleteMeeting");
        detailGroupAsyncTask.execute(meetingId.toString());
    }

    @Override
    public void onMemberClick(Long memberId) {
        Intent memberDetailIntent = new Intent(this, PersonInfoActivity.class);
        memberDetailIntent.putExtra("groupId", controller.getGroupId());
        memberDetailIntent.putExtra("memberId", memberId);
        startActivity(memberDetailIntent);
    }

    @Override
    public boolean onMemberDismiss(Long memberId) {

        if (!controller.getLocalAuthorId().equals(memberId)) {
            DetailGroupAsyncTask detailGroupAsyncTask = new DetailGroupAsyncTask("deleteMember");
            detailGroupAsyncTask.execute(memberId.toString());
            return true;
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.canNotLeaveGroupThisWay));
            builder.setCancelable(false);
            builder.setNeutralButton(getString(R.string.ok), ((dialog, which) -> dialog.cancel()));
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            return false;
        }
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
    }

    @SuppressLint("StaticFieldLeak")
    public class DetailGroupAsyncTask extends AsyncTask<String, Exception, String> {

        String flag;

        public DetailGroupAsyncTask(String flag) {
            this.flag = flag;
        }

        @Override
        protected void onProgressUpdate(Exception... values) {
            super.onProgressUpdate(values);
            handleException(values[0]);
        }

        @Override
        protected String doInBackground(String... strings) {

            String returnString = "";

            switch (flag) {
                case "update":
                    controller.update();
                    returnString = "update";
                    break;
                case "deleteGroup":
                    try {
                        controller.deleteGroup();
                    } catch (GroupNotFoundException exception) {
                        publishProgress(exception);
                    }
                    returnString = "deleteGroup";
                    break;
                case "leaveGroup":
                    try {
                        controller.leaveGroup();
                    } catch (GroupNotFoundException exception) {
                        publishProgress(exception);
                    }
                    returnString = "deleteGroup";
                    break;
                case "addMember":
                    try {
                        controller.addContacts(adapter.getSelectedContacts());
                    } catch (SmartModerationException exception) {
                        publishProgress(exception);
                    }
                    returnString = "addMember";
                    break;
                case "addGhost":
                    try {
                        controller.addGhost(strings[0], strings[1]);
                    } catch (GroupNotFoundException exception) {
                        publishProgress(exception);
                    }
                    returnString = "addGhost";
                    break;
                case "deleteMeeting":
                    try {
                        controller.deleteMeeting(Long.valueOf(strings[0]));
                    } catch (GroupNotFoundException exception) {
                        publishProgress(exception);
                    }
                    returnString = "deleteMeeting";
                    break;
                case "deleteMember":
                    try {
                        controller.removeMember(Long.valueOf(strings[0]));
                    } catch (GroupNotFoundException exception) {
                        publishProgress(exception);
                    }
                    returnString = "deleteMember";
                    break;
            }
            return returnString;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            switch (s) {
                case "update":
                    try {
                        Collection<Member> members = controller.getMembers();
                        memberAdapter.updateMembers(members);
                        Log.d(TAG, "Available Members: " + members);
                        Collection<Meeting> meetings = controller.getMeetings();
                        meetingAdapter.updateMeetings(meetings);
                        Log.d(TAG, "Available Meetings: " + meetings);
                    } catch (GroupNotFoundException exception) {
                        handleException(exception);
                    }

                    pullToRefresh.setRefreshing(false);
                    break;
                case "deleteGroup":
                    finish();
                    break;
                case "addGhost":
                    try {
                        memberAdapter.updateMembers(controller.getMembers());
                    } catch (GroupNotFoundException exception) {
                        handleException(exception);
                    }
                    break;
            }
        }
    }
}
