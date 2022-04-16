package dhbw.smartmoderation.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.Collection;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.account.contactexchange.ContactExchangeActivity;
import dhbw.smartmoderation.connection.synchronization.SynchronizableDataType;
import dhbw.smartmoderation.group.create.CreateGroup;
import dhbw.smartmoderation.group.invitations.ListInvitationsActivity;
import dhbw.smartmoderation.group.overview.OverviewGroupActivity;
import dhbw.smartmoderation.util.UpdateableExceptionHandlingActivity;

/**
 * Activity for navigating to all other activities.
 */
public class HomeActivity extends UpdateableExceptionHandlingActivity {

    private Button btnShowGroups;
    private Button btnAddContact;
    private Button btnNewGroup;
    private Button btnGroupInvitations;
    private HomeController homeController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setTitle(getString(R.string.home_title));

        SwipeRefreshLayout pullToRefresh = findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(() -> {
            updateUI();
            pullToRefresh.setRefreshing(false);
        });


        btnShowGroups = findViewById(R.id.btnShowGroups);
        btnAddContact = findViewById(R.id.btnAddContact);
        btnNewGroup = findViewById(R.id.btnNewGroup);
        btnGroupInvitations = findViewById(R.id.btnGroupInvitations);

        btnShowGroups.setOnClickListener(this::onShowGroups);
        btnAddContact.setOnClickListener(this::onAddContact);
        btnNewGroup.setOnClickListener(this::onNewGroup);
        btnGroupInvitations.setOnClickListener(this::onGroupInvitations);

        this.homeController = new HomeController();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    public void onShowGroups(View v) {
        Intent showGroupsIntent = new Intent(HomeActivity.this, OverviewGroupActivity.class);
        startActivity(showGroupsIntent);
    }

    public void onAddContact(View v) {
        Intent addContactIntent = new Intent(HomeActivity.this, ContactExchangeActivity.class);
        startActivity(addContactIntent);
    }

    public void onNewGroup(View v) {
        Intent newGroupIntent = new Intent(HomeActivity.this, CreateGroup.class);
        startActivity(newGroupIntent);
    }

    public void onGroupInvitations(View v) {
        Intent groupInvitationsIntent = new Intent(HomeActivity.this, ListInvitationsActivity.class);
        startActivity(groupInvitationsIntent);
    }

    @Override
    public Collection<SynchronizableDataType> getSynchronizableDataTypes() {
        return null;
    }

    @Override
    protected void updateUI() {
        if (this.homeController.atLeastOneGroupExists()) {
            btnShowGroups.setVisibility(View.VISIBLE);
            btnNewGroup.setVisibility(View.GONE);
        } else {
            btnShowGroups.setVisibility(View.GONE);
            btnNewGroup.setVisibility(View.VISIBLE);
        }
    }
}
