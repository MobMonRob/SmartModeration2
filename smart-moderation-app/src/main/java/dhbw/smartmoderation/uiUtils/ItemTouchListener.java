package dhbw.smartmoderation.uiUtils;

import java.util.ArrayList;
import java.util.Collection;

public interface ItemTouchListener<T> {

    void onItemDismiss(T t);
    void changeNumberingAfterOrderChange(ArrayList<T> collection);
    Collection<T> getCollections();
}
