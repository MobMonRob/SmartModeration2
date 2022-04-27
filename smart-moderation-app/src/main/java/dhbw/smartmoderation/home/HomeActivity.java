package dhbw.smartmoderation.home;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.briarproject.bramble.api.plugin.BluetoothConstants;
import org.briarproject.bramble.api.plugin.LanTcpConstants;
import org.briarproject.bramble.api.plugin.Plugin;
import org.briarproject.bramble.api.plugin.TorConstants;
import org.briarproject.bramble.api.plugin.TransportId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.account.contactexchange.ContactExchangeActivity;
import dhbw.smartmoderation.connection.synchronization.SynchronizableDataType;
import dhbw.smartmoderation.group.create.CreateGroup;
import dhbw.smartmoderation.group.invitations.ListInvitationsActivity;
import dhbw.smartmoderation.group.overview.OverviewGroupActivity;
import dhbw.smartmoderation.moderationCard.DesktopLoginQRScanner;
import dhbw.smartmoderation.util.UpdateableExceptionHandlingActivity;

public class HomeActivity extends UpdateableExceptionHandlingActivity {

    private Button btnShowGroups;
    private Button btnNewGroup;
    private HomeController homeController;
    ArrayList<ImageView> statusIcons;
    private Thread updateThread;

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
        Button btnAddContact = findViewById(R.id.btnAddContact);
        btnNewGroup = findViewById(R.id.btnNewGroup);
        Button btnGroupInvitations = findViewById(R.id.btnGroupInvitations);
        initializeStatusIcons();


        btnShowGroups.setOnClickListener(this::onShowGroups);
        btnAddContact.setOnClickListener(this::onAddContact);
        btnNewGroup.setOnClickListener(this::onNewGroup);
        btnGroupInvitations.setOnClickListener(this::onGroupInvitations);

        this.homeController = new HomeController();
    }

    private void initializeStatusIcons() {
        statusIcons = new ArrayList<>();
        ImageView lanIcon = findViewById(R.id.lanIcon);
        ImageView torIcon = findViewById(R.id.torIcon);
        ImageView bluetoothIcon = findViewById(R.id.bluetoothIcon);
        ImageView desktopAppIcon = findViewById(R.id.desktopIcon);
        torIcon.setTag(TorConstants.ID);
        lanIcon.setTag(LanTcpConstants.ID);
        bluetoothIcon.setTag(BluetoothConstants.ID);
        desktopAppIcon.setTag(DesktopLoginQRScanner.DESKTOPID);
        statusIcons.add(torIcon);
        statusIcons.add(lanIcon);
        statusIcons.add(bluetoothIcon);
        statusIcons.add(desktopAppIcon);
    }

    @Override
    protected void onResume() {
        super.onResume();
        createUpdateUIThread();
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateThread.interrupt();
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
        System.out.println("updatingPluginStates");
        updateNetworkPluginsState();
        if (homeController.atLeastOneGroupExists()) {
            btnShowGroups.setVisibility(View.VISIBLE);
            btnNewGroup.setVisibility(View.GONE);
        } else {
            btnShowGroups.setVisibility(View.GONE);
            btnNewGroup.setVisibility(View.VISIBLE);
        }
    }

    private void createUpdateUIThread(){
        updateThread = new Thread(() -> {
            Handler handler = new Handler(Looper.getMainLooper());
            while (!Thread.currentThread().isInterrupted()) {
                handler.post(this::updateUI);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
         });
        updateThread.start();
    }

    private void updateNetworkPluginsState() {
        Map<TransportId, Plugin.State> pluginsStates = homeController.getPluginsStates();
        for (ImageView icon : statusIcons) {
            switch (pluginsStates.get(icon.getTag())) {
                case ACTIVE:
                    icon.setColorFilter(Color.GREEN);
                    break;
                case INACTIVE:
                    icon.setColorFilter(Color.GRAY);
                    break;
                case DISABLED:
                    icon.setColorFilter(Color.RED);
                    break;
            }
        }
    }
}
