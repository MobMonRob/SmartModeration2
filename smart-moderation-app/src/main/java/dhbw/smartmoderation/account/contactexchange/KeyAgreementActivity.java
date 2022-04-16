package dhbw.smartmoderation.account.contactexchange;


import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import org.briarproject.bramble.api.event.Event;
import org.briarproject.bramble.api.event.EventBus;
import org.briarproject.bramble.api.event.EventListener;
import org.briarproject.bramble.api.plugin.BluetoothConstants;
import org.briarproject.bramble.api.plugin.LanTcpConstants;
import org.briarproject.bramble.api.plugin.Plugin;
import org.briarproject.bramble.api.plugin.PluginManager;
import org.briarproject.bramble.api.plugin.event.TransportStateEvent;

import java.util.logging.Logger;

import javax.inject.Inject;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.SmartModerationApplication;
import dhbw.smartmoderation.SmartModerationApplicationImpl;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.content.Intent.CATEGORY_DEFAULT;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static dhbw.smartmoderation.BuildConfig.APPLICATION_ID;
import static java.util.logging.Level.INFO;


public abstract class KeyAgreementActivity extends AppCompatActivity implements BaseFragmentListener, EventListener, IntroFragment.IntroScreenSeenListener, KeyAgreementFragment.KeyAgreementEventListener {

    int REQUEST_BLUETOOTH_DISCOVERABLE = 10;
    int REQUEST_PERMISSION_CAMERA_LOCATION = 8;

    private enum BluetoothDecision {
        UNKNOWN, NO_ADAPTER, WAITING, ACCEPTED, REFUSED
    }

    private enum Permission {
        UNKNOWN, GRANTED, SHOW_RATIONALE, PERMANENTLY_DENIED
    }

    private static final Logger LOG = Logger.getLogger(KeyAgreementActivity.class.getName());

    @Inject
    EventBus eventBus;

    @Inject
    PluginManager pluginManager;

    private boolean isResumed = false;

    private boolean continueClicked = false;

    private boolean hasEnabledWifi = false;

    private boolean hasEnabledBluetooth = false;

    private Permission cameraPermission = Permission.UNKNOWN;
    private Permission locationPermission = Permission.UNKNOWN;
    private BluetoothDecision bluetoothDecision = BluetoothDecision.UNKNOWN;
    private BroadcastReceiver bluetoothReceiver = null;
    private Plugin wifiPlugin = null;
    private Plugin bluetoothPlugin = null;
    private BluetoothAdapter bluetoothAdapter = null;
    private KeyAgreementController controller;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ((SmartModerationApplicationImpl) getApplicationContext()).smartModerationComponent.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_container);
        if (savedInstanceState == null) {
            showInitialFragment(IntroFragment.newInstance());
        }

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        bluetoothReceiver = new BluetoothStateReceiver();
        registerReceiver(bluetoothReceiver, filter);
        controller = new KeyAgreementController();
        pluginManager = controller.getPluginManager();
        eventBus = controller.getEventBus();
        wifiPlugin = pluginManager.getPlugin(LanTcpConstants.ID);
        bluetoothPlugin = pluginManager.getPlugin(BluetoothConstants.ID);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    private void showInitialFragment(IntroFragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment, fragment.getUniqueTag())
                .commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (bluetoothReceiver != null) unregisterReceiver(bluetoothReceiver);
    }

    @Override
    protected void onStart() {
        super.onStart();
        eventBus.addListener(this);
        cameraPermission = Permission.UNKNOWN;
        locationPermission = Permission.UNKNOWN;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        isResumed = true;
        showQrCodeFragmentIfAllowed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isResumed = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        eventBus.removeListener(this);
    }

    @Override
    public void onActivityResult(int request, int result, @Nullable Intent data) {

        if (request == REQUEST_BLUETOOTH_DISCOVERABLE) {
            if (result == RESULT_CANCELED) {
                LOG.info("Bluetooth discoverability was refused");
                bluetoothDecision = BluetoothDecision.REFUSED;
            } else {
                LOG.info("Bluetooth discoverability was accepted");
                bluetoothDecision = BluetoothDecision.ACCEPTED;
            }
            showQrCodeFragmentIfAllowed();
        } else super.onActivityResult(request, result, data);
    }

    public void showNextScreen() {

        continueClicked = true;

        if (bluetoothDecision == BluetoothDecision.REFUSED)
            bluetoothDecision = BluetoothDecision.UNKNOWN;

        if (checkPermissions()) showQrCodeFragmentIfAllowed();
    }

    public boolean checkPermissions() {
        if (areEssentialPermissionsGranted()) return true;

        if (cameraPermission == Permission.PERMANENTLY_DENIED) {
            showDenialDialog(getString(R.string.camera_permission_title), getString(R.string.camera_permissions_message));
            return false;
        }

        if (isBluetoothSupported() && locationPermission == Permission.PERMANENTLY_DENIED) {
            showDenialDialog(getString(R.string.camera_permission_title), getString(R.string.camera_permissions_message));
            return false;
        }

        if (cameraPermission == Permission.SHOW_RATIONALE && locationPermission == Permission.SHOW_RATIONALE) {
            showRationale(getString(R.string.camera_permission_title) + " " + getString(R.string.location_permission_title), getString(R.string.camera_permissions_message) + "\n\n" + getString(R.string.location_permissions_message));
        } else if (cameraPermission == Permission.SHOW_RATIONALE) {
            showRationale(getString(R.string.camera_permission_title), getString(R.string.camera_permissions_message));
        } else if (locationPermission == Permission.SHOW_RATIONALE) {
            showRationale(getString(R.string.camera_permission_title), getString(R.string.camera_permissions_message));
        } else {
            requestPermissions();
        }

        return false;
    }

    private void requestPermissions() {
        String[] permissions;

        if (isBluetoothSupported()) {
            permissions = new String[]{CAMERA, ACCESS_FINE_LOCATION};
        } else {
            permissions = new String[]{CAMERA};
        }

        ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION_CAMERA_LOCATION);
    }

    private void showQrCodeFragmentIfAllowed() {

        if (isResumed && continueClicked && areEssentialPermissionsGranted()) {
            if (isWifiReady() && isBluetoothReady()) {
                LOG.info("Wifi and Bluetooth are ready");
                showQrCodeFragment();
            } else {
                if (shouldEnableWifi()) {
                    LOG.info("Enabling wifi plugin");
                    hasEnabledWifi = true;
                    pluginManager.setPluginEnabled(LanTcpConstants.ID, true);
                }
                if (bluetoothDecision == BluetoothDecision.UNKNOWN) {
                    requestBluetoothDiscoverable();
                } else if (bluetoothDecision == BluetoothDecision.REFUSED) {

                } else if (shouldEnableBluetooth()) {
                    LOG.info("Enabling Bluetooth plugin");
                    hasEnabledBluetooth = true;
                    pluginManager.setPluginEnabled(BluetoothConstants.ID, true);
                }
            }
        }
    }

    private boolean isWifiReady() {

        if (wifiPlugin == null) {
            return true;
        }

        Plugin.State state = wifiPlugin.getState();

        return state == Plugin.State.ACTIVE || state == Plugin.State.INACTIVE;
    }

    private boolean isBluetoothReady() {

        if (!isBluetoothSupported()) return true;

        if (bluetoothDecision == BluetoothDecision.UNKNOWN || bluetoothDecision == BluetoothDecision.WAITING || bluetoothDecision == BluetoothDecision.REFUSED)
            return false;

        if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)
            return false;

        return bluetoothPlugin.getState() == Plugin.State.ACTIVE;
    }

    private boolean areEssentialPermissionsGranted() {
        return cameraPermission == Permission.GRANTED && (Build.VERSION.SDK_INT < 23 || locationPermission == Permission.GRANTED || !isBluetoothSupported());
    }

    private void showQrCodeFragment() {
        continueClicked = false;
        bluetoothDecision = BluetoothDecision.UNKNOWN;
        hasEnabledWifi = false;
        hasEnabledBluetooth = false;

        FragmentManager fragmentManager = getSupportFragmentManager();

        if (fragmentManager.findFragmentByTag(KeyAgreementFragment.TAG) == null) {
            KeyAgreementFragment fragment = KeyAgreementFragment.newInstance();
            fragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment, fragment.getUniqueTag()).addToBackStack(fragment.getUniqueTag()).commit();
        }
    }

    private boolean shouldEnableWifi() {

        if (hasEnabledWifi) {
            return false;
        }

        if (wifiPlugin == null) {
            return false;
        }

        Plugin.State state = wifiPlugin.getState();
        return state == Plugin.State.STARTING_STOPPING || state == Plugin.State.DISABLED;
    }

    private boolean shouldEnableBluetooth() {

        if (bluetoothDecision != BluetoothDecision.ACCEPTED) return false;

        if (hasEnabledBluetooth) return false;

        if (!isBluetoothSupported()) return false;

        Plugin.State state = bluetoothPlugin.getState();
        return state == Plugin.State.STARTING_STOPPING || state == Plugin.State.DISABLED;
    }

    private void requestBluetoothDiscoverable() {

        if (!isBluetoothSupported()) {
            bluetoothDecision = BluetoothDecision.NO_ADAPTER;
            showQrCodeFragmentIfAllowed();
        } else {
            Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);

            if (i.resolveActivity(getPackageManager()) != null) {
                LOG.info("Asking for Bluetooth discoverability");
                bluetoothDecision = BluetoothDecision.WAITING;
                startActivityForResult(i, REQUEST_BLUETOOTH_DISCOVERABLE);
            } else {
                bluetoothDecision = BluetoothDecision.NO_ADAPTER;
                showQrCodeFragmentIfAllowed();
            }
        }
    }

    private boolean isBluetoothSupported() {
        return bluetoothAdapter != null && bluetoothPlugin != null;
    }

    private void showDenialDialog(String title, String body) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(body);
        builder.setPositiveButton(getString(R.string.ok), ((dialog, which) -> goToSettings()));
        builder.setNegativeButton(getString(R.string.cancel), ((dialog, which) -> supportFinishAfterTransition()));
        builder.show();
    }

    private void goToSettings() {
        Intent i = new Intent();
        i.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
        i.addCategory(CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + APPLICATION_ID));
        i.addFlags(FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    private void showRationale(String title, String body) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(body);
        builder.setNeutralButton(getString(R.string.continue_), ((dialog, which) -> requestPermissions()));
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != REQUEST_PERMISSION_CAMERA_LOCATION) throw new AssertionError();

        if (gotPermission(CAMERA, permissions, grantResults)) {
            cameraPermission = Permission.GRANTED;
        } else if (shouldShowRationale(CAMERA)) {
            cameraPermission = Permission.SHOW_RATIONALE;
        } else {
            cameraPermission = Permission.PERMANENTLY_DENIED;
        }

        if (isBluetoothSupported()) {
            if (gotPermission(ACCESS_FINE_LOCATION, permissions, grantResults)) {
                locationPermission = Permission.GRANTED;
            } else if (shouldShowRationale(ACCESS_FINE_LOCATION)) {
                locationPermission = Permission.SHOW_RATIONALE;
            } else {
                locationPermission = Permission.PERMANENTLY_DENIED;
            }
        }

        if (checkPermissions()) {
            showQrCodeFragmentIfAllowed();
        }
    }

    @Override
    public void eventOccurred(Event e) {

        if (e instanceof TransportStateEvent) {
            TransportStateEvent t = (TransportStateEvent) e;

            if (t.getTransportId().equals(BluetoothConstants.ID)) {
                if (LOG.isLoggable(INFO)) {
                    LOG.info("Bluetooth state changed to " + t.getState());
                }
                showQrCodeFragmentIfAllowed();
            }
        }
    }

    private boolean gotPermission(String permission, String[] permissions, int[] grantResults) {

        for (int i = 0; i < permissions.length; i++) {
            if (permission.equals(permissions[i])) {
                return grantResults[i] == PERMISSION_GRANTED;
            }
        }
        return false;
    }

    private boolean shouldShowRationale(String permission) {
        return ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
    }


    private class BluetoothStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            LOG.info("Bluetooth scan mode changed");
            showQrCodeFragmentIfAllowed();
        }
    }
}
