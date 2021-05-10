package dhbw.smartmoderation.account.contactexchange;

import androidx.annotation.UiThread;
import androidx.fragment.app.Fragment;

public interface BaseFragmentListener {

    @UiThread
    void onBackPressed();

    @UiThread
    void showNextFragment(Fragment fragment, String tag);

    @UiThread
    void handleException(Exception e);
}
