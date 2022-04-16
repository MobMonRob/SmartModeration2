package dhbw.smartmoderation.account.contactexchange;

import org.briarproject.bramble.api.event.EventBus;
import org.briarproject.bramble.api.plugin.PluginManager;

import dhbw.smartmoderation.controller.SmartModerationController;

public class KeyAgreementController extends SmartModerationController {

    public KeyAgreementController() {
    }

    public PluginManager getPluginManager() {
        return connectionService.getPluginManager();
    }

    public EventBus getEventBus() {
        return connectionService.getEventBus();
    }
}
