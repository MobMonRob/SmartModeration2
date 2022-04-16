package dhbw.smartmoderation.home;

import dhbw.smartmoderation.controller.SmartModerationController;

public class HomeController extends SmartModerationController {

    public boolean atLeastOneGroupExists() {
        return dataService.getGroups().size() > 0;
    }
}
