package dhbw.smartmoderation.connection.synchronization;

import java.util.Collection;

import dhbw.smartmoderation.data.model.ModelClass;

public class ModelClassData {

    private PullEvent PullEvent;

    private Collection<ModelClass> data;


    public dhbw.smartmoderation.connection.synchronization.PullEvent getPullEvent() {
        return PullEvent;
    }

    public Collection<ModelClass> getData() {
        return data;
    }

    public void setData(Collection<ModelClass> data) {
        this.data = data;
    }

    public void setPullEvent(PullEvent event){
        this.PullEvent = event;
    }
}
