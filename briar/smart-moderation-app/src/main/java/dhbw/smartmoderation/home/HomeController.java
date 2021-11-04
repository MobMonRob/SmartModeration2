package dhbw.smartmoderation.home;

import org.briarproject.briar.api.privategroup.PrivateGroup;

import java.util.ArrayList;
import java.util.Collection;

import dhbw.smartmoderation.controller.SmartModerationController;

import dhbw.smartmoderation.exceptions.GroupNotFoundException;

public class HomeController extends SmartModerationController {


    public boolean atLeastOneGroupExists() {

        if(dataService.getGroups().size() > 0) {

            return true;
        }

        return false;
    }
}
