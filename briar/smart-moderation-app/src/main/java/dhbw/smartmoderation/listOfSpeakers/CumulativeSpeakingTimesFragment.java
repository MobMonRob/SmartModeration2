package dhbw.smartmoderation.listOfSpeakers;

import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import java.text.DecimalFormat;
import java.util.Collection;
import dhbw.smartmoderation.R;
import dhbw.smartmoderation.data.model.Participation;
import dhbw.smartmoderation.meeting.detail.BaseActivity;
import dhbw.smartmoderation.util.Util;

public class  CumulativeSpeakingTimesFragment extends Fragment {

    private LinearLayout tableContainer;
    private TableLayout speakingTimesTable;
    private Collection<Participation> participations;
    private ListOfSpeakersController controller;
    private View view;

    public String getTitle(){
        return getString(R.string.cumulativeSpeakingTimesFragment_title);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.controller = ((BaseActivity)getActivity()).getListOfSpeakersController();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        this.view = inflater.inflate(R.layout.fragment_cumulative_speaking_times, container, false);

        getActivity().setTitle(getTitle());
        this.tableContainer = this.view.findViewById(R.id.tableContainer);
        this.speakingTimesTable = new TableLayout(getActivity());
        TableLayout.LayoutParams layoutParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(20, 40, 20, 20);
        this.speakingTimesTable.setLayoutParams(layoutParams);
        this.tableContainer.addView(this.speakingTimesTable);
        this.participations = this.controller.getParticipations();
        createTableHeader();
        createSpeakingTimesTable();
        return this.view;
    }

    public void createTableHeader() {

        TableRow header = new TableRow(getActivity());
        header.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

        TextView participant = new TextView(getActivity());
        TableRow.LayoutParams participantLayoutParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 1.4f);
        participant.setLayoutParams(participantLayoutParams);
        participant.setTypeface(participant.getTypeface(), Typeface.BOLD);
        participant.setGravity(Gravity.CENTER);

        GradientDrawable gradientDrawable = (GradientDrawable) ContextCompat.getDrawable(getActivity(), R.drawable.rectangle_4_borders);
        gradientDrawable.setColor(ContextCompat.getColor(getActivity(), R.color.light_grey));

        participant.setBackground(gradientDrawable);
        participant.setText(getString(R.string.participant));
        header.addView(participant);

        TextView contribution = new TextView(getActivity());
        TableRow.LayoutParams contributionLayoutParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 1.2f);
        contribution.setLayoutParams(contributionLayoutParams);
        contribution.setTypeface(contribution.getTypeface(), Typeface.BOLD);
        contribution.setGravity(Gravity.CENTER);

        LayerDrawable layerDrawable = (LayerDrawable) ContextCompat.getDrawable(getActivity(), R.drawable.rectangle_trb_borders);
        GradientDrawable shape = (GradientDrawable)layerDrawable.findDrawableByLayerId(R.id.trb_drawable);
        shape.setColor(ContextCompat.getColor(getActivity(), R.color.light_grey));

        contribution.setBackground(layerDrawable);
        contribution.setText(getString(R.string.contributions));
        header.addView(contribution);

        TextView totalDuration = new TextView(getActivity());
        TableRow.LayoutParams totalDurationLayoutParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 1.2f);
        totalDuration.setLayoutParams(totalDurationLayoutParams);
        totalDuration.setTypeface(totalDuration.getTypeface(), Typeface.BOLD);
        totalDuration.setGravity(Gravity.CENTER);

        totalDuration.setBackground(layerDrawable);
        totalDuration.setText(getString(R.string.accumulatedSpeakingTime));
        header.addView(totalDuration);

        TextView proportion = new TextView(getActivity());
        TableRow.LayoutParams proportionLayoutParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 1.2f);
        proportion.setLayoutParams(proportionLayoutParams);
        proportion.setTypeface(proportion.getTypeface(), Typeface.BOLD);
        proportion.setGravity(Gravity.CENTER);

        proportion.setBackground(layerDrawable);
        proportion.setText(getString(R.string.speakingTimeInPercent));
        header.addView(proportion);

        this.speakingTimesTable.addView(header);
    }

    public void createSpeakingTimesTable() {

        for(Participation participation : this.participations) {

            TableRow row = new TableRow(getActivity());
            row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

            LayerDrawable rbl_layerDrawable = (LayerDrawable) ContextCompat.getDrawable(getActivity(), R.drawable.rectangle_rbl_borders);
            LayerDrawable rb_layerDrawable = (LayerDrawable) ContextCompat.getDrawable(getActivity(), R.drawable.rectangle_rb_borders);

            TextView participant = new TextView(getActivity());
            TableRow.LayoutParams participantLayoutParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 1.4f);
            participant.setLayoutParams(participantLayoutParams);
            participant.setTypeface(participant.getTypeface(), Typeface.BOLD);
            participant.setGravity(Gravity.CENTER);
            participant.setBackground(rbl_layerDrawable);
            participant.setText(participation.getMember().getName());
            participant.setSingleLine(false);
            row.addView(participant);

            TextView contributions = new TextView(getActivity());
            TableRow.LayoutParams contributionsLayoutParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 1.2f);
            contributions.setLayoutParams(contributionsLayoutParams);
            contributions.setTypeface(contributions.getTypeface(), Typeface.BOLD);
            contributions.setGravity(Gravity.CENTER);
            contributions.setBackground(rb_layerDrawable);
            contributions.setText(participation.getContributions() + "");
            contributions.setSingleLine(true);
            row.addView(contributions);

            TextView totalDuration = new TextView(getActivity());
            TableRow.LayoutParams totalDurationLayoutParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 1.2f);
            totalDuration.setLayoutParams(totalDurationLayoutParams);
            totalDuration.setTypeface(totalDuration.getTypeface(), Typeface.BOLD);
            totalDuration.setGravity(Gravity.CENTER);
            totalDuration.setBackground(rb_layerDrawable);
            String duration = Util.convertMilliSecondsToMinutesTimeString(participation.getTime()) + " " + getString(R.string.minute);
            totalDuration.setText(duration);
            totalDuration.setSingleLine(true);
            row.addView(totalDuration);

            TextView proportion = new TextView(getActivity());
            TableRow.LayoutParams proportionLayoutParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 1.2f);
            proportion.setLayoutParams(proportionLayoutParams);
            proportion.setTypeface(proportion.getTypeface(), Typeface.BOLD);
            proportion.setGravity(Gravity.CENTER);
            proportion.setBackground(rb_layerDrawable);
            float percentage = ((float)participation.getTime()/(float)this.controller.getTotalTime())*100;
            DecimalFormat decimalFormat = new DecimalFormat("#.##");
            proportion.setText(decimalFormat.format(percentage));
            proportion.setSingleLine(true);
            row.addView(proportion);

            this.speakingTimesTable.addView(row);
        }
    }

    public void createNoDataLabel() {

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        this.tableContainer.setLayoutParams(layoutParams);
        this.tableContainer.setGravity(Gravity.CENTER);

        TextView label = new TextView(getActivity());
        ViewGroup.LayoutParams labelLayoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        label.setLayoutParams(labelLayoutParams);
        label.setText(getString(R.string.NoDataAvailableYet));
        label.setTypeface(label.getTypeface(), Typeface.BOLD);
        label.setTextSize(20.0f);
        this.tableContainer.removeAllViews();
        this.tableContainer.addView(label);

    }

    @Override
    public void onResume() {
        super.onResume();
        update();
    }

    public void update() {

        CumulativeSpeakingTimesAsyncTask cumulativeSpeakingTimesAsyncTask = new CumulativeSpeakingTimesAsyncTask();
        cumulativeSpeakingTimesAsyncTask.execute();

    }

    public class CumulativeSpeakingTimesAsyncTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {

            participations = controller.getParticipations();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if(participations.size() > 0) {

                speakingTimesTable.removeAllViews();
                createTableHeader();
                createSpeakingTimesTable();
            }

            else {

                createNoDataLabel();
            }

            ((BaseActivity) getActivity()).getPullToRefresh().setRefreshing(false);
        }

    }

}
