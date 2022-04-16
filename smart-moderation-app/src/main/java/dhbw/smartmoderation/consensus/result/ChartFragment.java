package dhbw.smartmoderation.consensus.result;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.data.model.ConsensusLevel;

public class ChartFragment extends Fragment {
    private PieChart donutChart;
    private ConsensusProposalResultController controller;
    private ConstraintLayout constraintLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chart, container, false);
        this.controller = ((ConsensusProposalResult) requireActivity()).getController();
        constraintLayout = view.findViewById(R.id.constraintLayout);
        return view;
    }

    public PieChart getDonutChart() {
        return  this.donutChart;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void createDonutChart(){
        constraintLayout.removeAllViews();
        donutChart = new PieChart(getActivity());
        donutChart.setOnChartValueSelectedListener(((ConsensusProposalResult) requireActivity()).chartValueSelectedListener);
        donutChart.setCenterTextSize(14f);
        donutChart.setCenterTextTypeface(Typeface.create((Typeface)null, Typeface.BOLD));
        donutChart.setCenterTextColor(requireActivity().getColor(R.color.default_black));
        String centerText = this.controller.getVoiceCount() + "/" + this.controller.getVoteMembersCount();
        donutChart.setCenterText(centerText);
        constraintLayout.addView(donutChart);
        donutChart.getLayoutParams().height = 600;
        donutChart.getLayoutParams().width = android.view.ViewGroup.LayoutParams.MATCH_PARENT;

        List<PieEntry> chartData = new ArrayList<>();
        List<Integer> colors = new ArrayList();

        for (Map.Entry<Long, Integer> entry : this.controller.getCountPerConsensusLevel().entrySet()) {
            ConsensusLevel consensusLevel = this.controller.getConsensusLevel(entry.getKey());
            float percentage = ((float)entry.getValue()/(float)this.controller.getVoteMembersCount())*100;
            chartData.add(new PieEntry(percentage, consensusLevel.getName()));
            colors.add(consensusLevel.getColor());
        }

        float notVotedPercentage = ((float)(this.controller.getVoteMembersCount()- this.controller.getVoiceCount())/(float)this.controller.getVoteMembersCount())*100;
        chartData.add(new PieEntry(notVotedPercentage, getString(R.string.novoteTaken)));
        colors.add(ContextCompat.getColor(requireActivity(), R.color.default_grey));

        PieDataSet chartDataSet = new PieDataSet(chartData, "");
        chartDataSet.setColors(colors);
        chartDataSet.setDrawValues(false);

        PieData donutData = new PieData(chartDataSet);
        donutChart.setData(donutData);
        donutChart.animateY(1400, Easing.EaseInOutQuad);
        donutChart.setCenterTextColor(Color.BLACK);
        donutChart.setExtraOffsets(5, 0, 5, 5);
        donutChart.getLegend().setEnabled(false);
        donutChart.getDescription().setEnabled(false);
        donutChart.setDrawEntryLabels(false);
        donutChart.setUsePercentValues(false);
        donutChart.invalidate();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onResume() {
        super.onResume();
        createDonutChart();
    }
}
