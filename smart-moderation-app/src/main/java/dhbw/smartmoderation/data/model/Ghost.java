package dhbw.smartmoderation.data.model;

import dhbw.smartmoderation.SmartModerationApplicationImpl;

public class Ghost implements IContact {

    private String firstName;
    private String lastName;
    private Long id;

    public Ghost() {

        this.id = ((SmartModerationApplicationImpl)SmartModerationApplicationImpl.getApp()).getUniqueId();
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
