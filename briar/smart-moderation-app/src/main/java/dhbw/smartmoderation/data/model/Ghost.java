package dhbw.smartmoderation.data.model;

import java.util.UUID;

import dhbw.smartmoderation.SmartModerationApplication;

public class Ghost implements IContact {

    private String firstName;
    private String lastName;
    private Long id;

    public Ghost() {

        this.id = ((SmartModerationApplication)SmartModerationApplication.getApp()).getUniqueId();
    }


    @Override
    public String getName() {

        return this.firstName + " " + this.lastName;
    }

    @Override
    public Long getId() {

        return this.id;
    }

    public void setFirstName(String firstName) {

        this.firstName = firstName;
    }

    public void setLastName(String lastName) {

        this.lastName = lastName;
    }

    public String getFirstName() {

        return firstName;
    }

    public String getLastName() {

        return lastName;
    }
}
