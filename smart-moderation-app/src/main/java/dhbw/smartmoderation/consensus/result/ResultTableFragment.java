package dhbw.smartmoderation.consensus.result;

import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.text.LineBreaker;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import java.util.Collection;
import dhbw.smartmoderation.R;
import dhbw.smartmoderation.data.model.Voice;

public class ResultTableFragment extends Fragment {

    private LinearLayout tableContainer;
    private TableLayout resultTable;
    private Collection<Voice> voices;
    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        this.view = inflater.inflate(R.layout.fragment_result_table, container, false);
        this.voices = ((ConsensusProposalResult)getActivity()).getController().getVoices();
        this.tableContainer = this.view.findViewById(R.id.tableContainer);

        this.resultTable = new TableLayout(getActivity());
        TableLayout.LayoutParams layoutParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(20, 20, 20, 20);
        this.resultTable.setLayoutParams(layoutParams);
        this.tableContainer.addView(this.resultTable);
        return this.view;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void update(){
        this.voices = ((ConsensusProposalResult)getActivity()).getController().getVoices();
        this.resultTable.removeAllViews();
        createTableHeader();
        createResultTable();
    }

    private void createTableHeader() {

        TableRow header = new TableRow(getActivity());
        header.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

        TextView name = new TextView(getActivity());
        TableRow.LayoutParams nameLayoutParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.7f);
        name.setLayoutParams(nameLayoutParams);
        name.setTypeface(name.getTypeface(), Typeface.BOLD);
        name.setGravity(Gravity.CENTER);

        GradientDrawable gradientDrawable = (GradientDrawable) ContextCompat.getDrawable(getActivity(), R.drawable.rectangle_4_borders);
        gradientDrawable.setColor(ContextCompat.getColor(getActivity(), R.color.light_grey));

        name.setBackground(gradientDrawable);
        name.setText(getString(R.string.name));
        header.addView(name);

        TextView description = new TextView(getActivity());
        TableRow.LayoutParams descriptionLayoutParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.3f);
        description.setLayoutParams(descriptionLayoutParams);
        description.setTypeface(description.getTypeface(), Typeface.BOLD);
        description.setGravity(Gravity.CENTER);

        LayerDrawable layerDrawable = (LayerDrawable) ContextCompat.getDrawable(getActivity(), R.drawable.rectangle_trb_borders);
        GradientDrawable shape = (GradientDrawable)layerDrawable.findDrawableByLayerId(R.id.trb_drawable);
        shape.setColor(ContextCompat.getColor(getActivity(), R.color.light_grey));

        description.setBackground(layerDrawable);
        description.setText(getString(R.string.explanation));
        header.addView(description);

        this.resultTable.addView(header);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createResultTable() {

        for(Voice voice : this.voices) {
            TableRow row = new TableRow(getActivity());
            row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

            TextView name = new TextView(getActivity());
            TableRow.LayoutParams nameLayoutParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 0.7f);
            name.setLayoutParams(nameLayoutParams);
            name.setTypeface(name.getTypeface(), Typeface.BOLD);
            name.setGravity(Gravity.CENTER);

            LayerDrawable layerDrawable = (LayerDrawable) ContextCompat.getDrawable(getActivity(), R.drawable.rectangle_rbl_borders);
            GradientDrawable shape = (GradientDrawable)layerDrawable.findDrawableByLayerId(R.id.rbl_drawable);
            shape.setColor(voice.getConsensusLevel().getColor());

            name.setBackground(layerDrawable);
            name.setText(voice.getMember().getName());
            name.setSingleLine(false);
            row.addView(name);

            TextView description = new TextView(getActivity());
            description.setPadding(10, 10, 10, 10);
            TableRow.LayoutParams descriptionLayoutParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 1.3f);
            description.setLayoutParams(descriptionLayoutParams);
            description.setGravity(Gravity.CENTER);

            layerDrawable = (LayerDrawable) ContextCompat.getDrawable(getActivity(), R.drawable.rectangle_rb_borders);
            shape = (GradientDrawable)layerDrawable.findDrawableByLayerId(R.id.rb_drawable);
            shape.setColor(ContextCompat.getColor(getActivity(), R.color.default_color));

            description.setBackground(layerDrawable);
            description.offsetLeftAndRight(2);
            description.offsetTopAndBottom(2);

            if(voice.getExplanation() == null || voice.getExplanation().isEmpty()) {
                description.setText("-");
                description.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            }

            else {
                description.setText(voice.getExplanation());
                description.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                description.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
            }

            description.setMaxLines(4);
            description.setSingleLine(false);
            row.addView(description);

            this.resultTable.addView(row);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onResume() {
        super.onResume();

        if(this.voices.size() > 0) {
            createTableHeader();
            createResultTable();
        }
    }
}
