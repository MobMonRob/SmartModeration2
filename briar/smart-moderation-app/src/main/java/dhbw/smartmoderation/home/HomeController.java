package dhbw.smartmoderation.home;

import dhbw.smartmoderation.controller.SmartModerationController;

public class HomeController extends SmartModerationController {


    public boolean atLeastOneGroupExists() {

        if(dataService.getGroups().size() > 0) {

            return true;
        }

        return false;
    }
}
