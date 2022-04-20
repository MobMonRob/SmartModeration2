package dhbw.smartmoderation.home;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.briarproject.bramble.api.plugin.BluetoothConstants;
import org.briarproject.bramble.api.plugin.LanTcpConstants;
import org.briarproject.bramble.api.plugin.Plugin;
import org.briarproject.bramble.api.plugin.TorConstants;
import org.briarproject.bramble.api.plugin.TransportId;
import org.briarproject.bramble.api.plugin.duplex.DuplexPlugin;

import java.util.Collection;
import java.util.Map;

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
    private LinearLayout pluginIconsHolder;
    private ImageView bluetoothIcon;
    private ImageView lanIcon;
    private ImageView torIcon;

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
        pluginIconsHolder = findViewById(R.id.plugins);
        bluetoothIcon = findViewById(R.id.bluetoothIcon);
        lanIcon = findViewById(R.id.lanIcon);
        torIcon = findViewById(R.id.torIcon);

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
        this.updateNetworkPluginsState();
        if (this.homeController.atLeastOneGroupExists()) {
            btnShowGroups.setVisibility(View.VISIBLE);
            btnNewGroup.setVisibility(View.GONE);
        } else {
            btnShowGroups.setVisibility(View.GONE);
            btnNewGroup.setVisibility(View.VISIBLE);
        }
    }

    private void updateNetworkPluginsState() {
        Map<TransportId, Plugin.State> pluginsStates = homeController.getPluginsStates();
        Plugin.State torPluginState = pluginsStates.get(TorConstants.ID);
        Plugin.State lanPluginState = pluginsStates.get(LanTcpConstants.ID);
        Plugin.State bluetoothPluginState = pluginsStates.get(BluetoothConstants.ID);
        switch (torPluginState) {
            case ACTIVE:
                torIcon.setColorFilter(Color.GREEN);
                break;
            case INACTIVE:
                torIcon.setColorFilter(Color.GRAY);
                break;
            case DISABLED:
                torIcon.setColorFilter(Color.RED);
                break;
        }
        switch (lanPluginState) {
            case ACTIVE:
                lanIcon.setColorFilter(Color.GREEN);
                break;
            case INACTIVE:
                lanIcon.setColorFilter(Color.GRAY);
                break;
            case DISABLED:
                lanIcon.setColorFilter(Color.RED);
                break;
        }
        switch (bluetoothPluginState) {
            case ACTIVE:
                bluetoothIcon.setColorFilter(Color.GREEN);
                break;
            case INACTIVE:
                bluetoothIcon.setColorFilter(Color.GRAY);
                break;
            case DISABLED:
                bluetoothIcon.setColorFilter(Color.RED);
                break;
        }

    }
}
