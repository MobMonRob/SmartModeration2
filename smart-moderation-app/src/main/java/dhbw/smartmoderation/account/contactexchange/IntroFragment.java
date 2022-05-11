package dhbw.smartmoderation.account.contactexchange;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import javax.annotation.Nullable;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.SmartModerationApplicationImpl;

public class IntroFragment extends Fragment {

    interface IntroScreenSeenListener {
        void showNextScreen();
    }

    static final String TAG = IntroFragment.class.getName();

    private IntroScreenSeenListener screenSeenListener;

    public String getUniqueTag() {
        return TAG;
    }

    public static IntroFragment newInstance() {
        Bundle args = new Bundle();
        IntroFragment fragment = new IntroFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        ((SmartModerationApplicationImpl)getActivity().getApplicationContext()).smartModerationComponent.inject(this);
        super.onAttach(context);
        screenSeenListener = (IntroScreenSeenListener) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_keyagreement_intro, container, false);
        View button = v.findViewById(R.id.scanButton);
        button.setOnClickListener(view -> screenSeenListener.showNextScreen());
        return v;
    }
}
