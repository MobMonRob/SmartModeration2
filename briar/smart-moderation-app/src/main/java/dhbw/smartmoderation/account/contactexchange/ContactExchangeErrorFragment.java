package dhbw.smartmoderation.account.contactexchange;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.SmartModerationApplicationImpl;

public class ContactExchangeErrorFragment extends Fragment {

    public static final String TAG = ContactExchangeErrorFragment.class.getName();
    private static final String ERROR_MSG = "errorMessage";

    public static ContactExchangeErrorFragment newInstance(String errorMsg) {

        ContactExchangeErrorFragment fragment = new ContactExchangeErrorFragment();
        Bundle args = new Bundle();
        args.putString(ERROR_MSG, errorMsg);
        fragment.setArguments(args);
        return fragment;
    }

    public String getUniqueTag() {

        return TAG;
    }

    @Override
    public void onAttach(Context context) {
        ((SmartModerationApplicationImpl)getActivity().getApplicationContext()).smartModerationComponent.inject(this);
        super.onAttach(context);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_error_contact_exchange, container, false);

        Button tryAgain = v.findViewById(R.id.tryAgainButton);
        tryAgain.setOnClickListener(view -> {

            FragmentActivity activity = requireActivity();
            Intent i = new Intent(activity, ContactExchangeActivity.class);
            i.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
            activity.startActivity(i);
        });

        Button cancel = v.findViewById(R.id.cancelButton);
        cancel.setOnClickListener(view -> finish());
        return v;
    }

    @UiThread
    protected void finish() {
        FragmentActivity activity = getActivity();

        if (activity != null) {

            activity.supportFinishAfterTransition();
        }
    }
}
