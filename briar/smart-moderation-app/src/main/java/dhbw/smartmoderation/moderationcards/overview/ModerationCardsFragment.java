package dhbw.smartmoderation.moderationcards.overview;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import dhbw.smartmoderation.R;

public class ModerationCardsFragment extends Fragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    public String getTitle(){
        return getString(R.string.moderationCardTitle);
    }
}
