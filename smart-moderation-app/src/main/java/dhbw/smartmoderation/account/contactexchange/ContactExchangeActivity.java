package dhbw.smartmoderation.account.contactexchange;

import static android.widget.Toast.LENGTH_LONG;
import static androidx.lifecycle.Lifecycle.State.STARTED;
import static java.util.Objects.requireNonNull;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.UiThread;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import org.briarproject.bramble.api.identity.Author;
import org.briarproject.bramble.api.keyagreement.KeyAgreementResult;
import org.briarproject.bramble.api.nullsafety.MethodsNotNullByDefault;
import org.briarproject.bramble.api.nullsafety.ParametersNotNullByDefault;

import javax.annotation.Nullable;
import javax.inject.Inject;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.SmartModerationApplicationImpl;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
public class ContactExchangeActivity extends KeyAgreementActivity {

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    private ContactExchangeViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle state) {
        ((SmartModerationApplicationImpl) getApplicationContext()).smartModerationComponent.inject(this);
        super.onCreate(state);
        requireNonNull(getSupportActionBar()).setTitle(this.getString(R.string.ContactExchangeActivity_Title));
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ContactExchangeViewModel.class);
    }

    private void startContactExchange(KeyAgreementResult result) {

        viewModel.getSucceeded().observe(this, succeeded -> {
            if (succeeded == null) {
                return;
            }

            if (succeeded) {
                Author remote = requireNonNull(viewModel.getRemoteAuthor());
                contactExchangeSucceeded(remote);
            } else {
                Author duplicate = viewModel.getDuplicateAuthor();

                if (duplicate == null) contactExchangeFailed();
                else duplicateContact(duplicate);
            }
        });

        viewModel.startContactExchange(result.getTransportId(),
                result.getConnection(), result.getMasterKey(),
                result.wasAlice());
    }

    @UiThread
    private void contactExchangeSucceeded(Author remoteAuthor) {
        String contactName = remoteAuthor.getName();
        String text = getString(R.string.addContact) + " " + contactName;
        Toast.makeText(this, text, LENGTH_LONG).show();
        supportFinishAfterTransition();
    }

    @UiThread
    private void duplicateContact(Author remoteAuthor) {
        String contactName = remoteAuthor.getName();
        String text = contactName + getString(R.string.already_exists);
        Toast.makeText(this, text, LENGTH_LONG).show();
        finish();
    }

    @UiThread
    private void contactExchangeFailed() {
        showErrorFragment();
    }


    @Override
    public void keyAgreementFailed() {
        showErrorFragment();
    }

    @Nullable
    @Override
    public String keyAgreementWaiting() {
        return getString(R.string.keyagreement_wait);
    }

    @Nullable
    @Override
    public String keyAgreementStarted() {
        return getString(R.string.keyagreement_started);
    }

    @Override
    public void keyAgreementAborted(boolean remoteAborted) {
        showErrorFragment();
    }

    @Nullable
    @Override
    public String keyAgreementFinished(KeyAgreementResult result) {
        startContactExchange(result);
        return getString(R.string.keyagreement_finished);
    }

    private void showErrorFragment() {
        ContactExchangeErrorFragment fragment = new ContactExchangeErrorFragment();
        showNextFragment(fragment, fragment.getUniqueTag());
    }

    public void showNextFragment(Fragment fragment, String tag) {
        if (!getLifecycle().getCurrentState().isAtLeast(STARTED)) return;

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment, tag)
                .addToBackStack(tag)
                .commit();
    }


    @Override
    public void handleException(Exception e) {
        supportFinishAfterTransition();
    }
}
