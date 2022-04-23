package dhbw.smartmoderation.home;

import org.briarproject.bramble.api.plugin.LanTcpConstants;
import org.briarproject.bramble.api.plugin.Plugin;
import org.briarproject.bramble.api.plugin.TorConstants;
import org.briarproject.bramble.api.plugin.TransportId;
import org.briarproject.bramble.api.plugin.duplex.DuplexPlugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import dhbw.smartmoderation.SmartModerationApplicationImpl;
import dhbw.smartmoderation.controller.SmartModerationController;
import dhbw.smartmoderation.moderationCard.DesktopLoginQRScanner;
import dhbw.smartmoderation.util.Client;

public class HomeController extends SmartModerationController {
    Client client;

    public HomeController() {
        this.client = ((SmartModerationApplicationImpl) SmartModerationApplicationImpl.getApp()).getClient();
    }

    public boolean atLeastOneGroupExists() {
        if(dataService.getGroups().size() > 0)
            return true;
        return false;
    }

    public Map<TransportId, Plugin.State> getPluginsStates() {
        Map<TransportId, Plugin.State> pluginsMap = new HashMap<>();
        Collection<DuplexPlugin> plugins = ((SmartModerationApplicationImpl) SmartModerationApplicationImpl.getApp()).getConnectionService().getPluginManager().getDuplexPlugins();
        for (DuplexPlugin plugin : plugins) {
            pluginsMap.put(plugin.getId(), plugin.getState());
        }
        if (client.isHostAlive()) {
            pluginsMap.put(DesktopLoginQRScanner.DESKTOPID, Plugin.State.ACTIVE);
        } else {
            pluginsMap.put(DesktopLoginQRScanner.DESKTOPID, Plugin.State.INACTIVE);
        }
        return pluginsMap;
    }
}
