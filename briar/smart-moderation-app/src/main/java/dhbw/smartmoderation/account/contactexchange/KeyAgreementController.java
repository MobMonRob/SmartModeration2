package dhbw.smartmoderation.account.contactexchange;

import org.briarproject.bramble.api.event.EventBus;
import org.briarproject.bramble.api.keyagreement.KeyAgreementTask;
import org.briarproject.bramble.api.keyagreement.PayloadEncoder;
import org.briarproject.bramble.api.keyagreement.PayloadParser;
import org.briarproject.bramble.api.plugin.BluetoothConstants;
import org.briarproject.bramble.api.plugin.LanTcpConstants;
import org.briarproject.bramble.api.plugin.Plugin;
import org.briarproject.bramble.api.plugin.PluginManager;

import javax.inject.Provider;

import dhbw.smartmoderation.controller.SmartModerationController;

public class KeyAgreementController extends SmartModerationController  {


    public KeyAgreementController() {

    }

    public PluginManager getPluginManager() {

        return connectionService.getPluginManager();
    }

    public Plugin getWifiPlugin() {

        return connectionService.getPluginManager().getPlugin(LanTcpConstants.ID);
    }

    public Plugin getBluetoothPlugin() {

        return connectionService.getPluginManager().getPlugin(BluetoothConstants.ID);
    }

    public EventBus getEventBus() {

        return connectionService.getEventBus();
    }


    public Provider<KeyAgreementTask> getKeyAgreementTaskProvider() {

        return connectionService.getKeyAgreementTaskProvider();

    }

    public PayloadEncoder getPayloadEncoder() {

        return connectionService.getPayloadEncoder();
    }

    public PayloadParser getPayloadParser() {

        return connectionService.getPayloadParser();
    }

}
