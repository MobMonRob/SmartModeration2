package dhbw.smartmoderation.consensus.overview;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import dhbw.smartmoderation.R;
import dhbw.smartmoderation.SmartModerationApplication;
import dhbw.smartmoderation.SmartModerationApplicationImpl;
import dhbw.smartmoderation.consensus.create.CreateConsensusProposal;
import dhbw.smartmoderation.consensus.detail.ConsensusProposalDetail;
import dhbw.smartmoderation.data.model.Poll;
import dhbw.smartmoderation.exceptions.PollCantBeCreatedException;
import dhbw.smartmoderation.exceptions.PollCantBeDeletedException;
import dhbw.smartmoderation.exceptions.PollCantBeOpenedException;
import dhbw.smartmoderation.meeting.detail.BaseActivity;
import dhbw.smartmoderation.uiUtils.SwipeHelper;
import dhbw.smartmoderation.uiUtils.UnderLayButton;
import dhbw.smartmoderation.uiUtils.UnderLayButtonClickListener;
import dhbw.smartmoderation.util.ExceptionHandlingActivity;

import static android.content.Context.WIFI_SERVICE;

public class ConsensusProposalOverviewFragment extends Fragment {

    private final int REQUEST_CODE = 0;

    private View view;
    private RadioGroup serverSwitch;
    private TextView serverLabel;
    private RecyclerView pollList;
    private PollAdapter pollAdapter;
    private LinearLayoutManager pollLayoutManager;
    private FloatingActionButton generalFab;
    private TextView serverInfo;
    private ConsensusProposalOverviewController controller;
    private Long meetingId;

    SmartModerationApplicationImpl app =  (SmartModerationApplicationImpl) SmartModerationApplicationImpl.getApp();

    public String getTitle(){
        return getString(R.string.consensusProposalOverviewFragment_title);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        this.view = inflater.inflate(R.layout.fragment_poll_overview, container, false);

        Intent intent = getActivity().getIntent();
        Bundle extra = intent.getExtras();
        this.meetingId = extra.getLong("meetingId");

        this.controller = new ConsensusProposalOverviewController(meetingId);
        this.serverInfo = this.view.findViewById(R.id.serverInfo);
        this.pollList = this.view.findViewById(R.id.pollList);
        this.pollAdapter = new PollAdapter(getActivity(), this.controller);
        this.pollList.setAdapter(this.pollAdapter);
        this.pollLayoutManager = new LinearLayoutManager(getActivity());
        this.pollList.setLayoutManager(this.pollLayoutManager);
        DividerItemDecoration pollDividerItemDecoration = new DividerItemDecoration(pollList.getContext(), pollLayoutManager.getOrientation());
        this.pollList.addItemDecoration(pollDividerItemDecoration);

        this.generalFab = this.view.findViewById(R.id.generalFab);

        if(!this.controller.isLocalAuthorModerator()) {
            this.generalFab.setVisibility(View.GONE);
        }

        this.generalFab.setOnClickListener(this::onAddConsensusProposal);

        instantiatePollSwipeHelper();

        return this.view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(controller.isLocalAuthorModerator()) {

            setHasOptionsMenu(true);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        if(controller.isLocalAuthorModerator()) {

            inflater.inflate(R.menu.switch_menu, menu);

            MenuItem item = menu.findItem(R.id.serverSwitch);
            item.setActionView(R.layout.server_switch_layout);

            this.serverSwitch = item.getActionView().findViewById(R.id.radioGroup);
            this.serverLabel = item.getActionView().findViewById(R.id.serverStartedLabel);

            if(app.getWebServer().wasStarted() && app.getMeetingId().equals(this.meetingId)) {

                this.serverSwitch.check(R.id.serverOn);
                this.serverLabel.setText(getString(R.string.stop_server));
                initIPAddress();
            }

            else {

                this.serverSwitch.check(R.id.serverOff);
                this.serverLabel.setText(getString(R.string.start_server));
                this.serverInfo.setText("");
            }

            this.serverSwitch.setOnCheckedChangeListener(((group, checkedId) -> {

                int id = serverSwitch.getCheckedRadioButtonId();

                if(id == R.id.serverOn) {

                    this.serverLabel.setText(getString(R.string.stop_server));
                    app.startWebServer();
                    app.setMeetingId(this.meetingId);
                    initIPAddress();

                }

                else {

                    this.serverLabel.setText(getString(R.string.start_server));
                    app.setMeetingId(0L);
                    app.stopWebServer();
                    this.serverInfo.setText("");

                }

            }));

            super.onCreateOptionsMenu(menu, inflater);
        }
    }

    public void initIPAddress() {

        WifiManager wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }

        byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

        String ipAddressString = "";

        try {

            ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();

        } catch (UnknownHostException ex) {

            Log.e("WIFIIP", "Unable to get host address.");

        }

        String  port = String.valueOf(app.getServerPort());
        String ipAddressWithPort = getString(R.string.running_server, ipAddressString, port);

        this.serverInfo.setText(ipAddressWithPort);

    }



    private void onAddConsensusProposal(View v) {
        Intent intent = new Intent(getActivity(), CreateConsensusProposal.class);
        intent.putExtra("meetingId", this.meetingId);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    public void onResume() {
        super.onResume();
        update();
    }

    public void update() {

        ConsensusProposalOverviewAsyncTask consensusProposalOverviewAsyncTask = new ConsensusProposalOverviewAsyncTask("update");
        consensusProposalOverviewAsyncTask.execute();

    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            this.pollAdapter.updatePollList(this.controller.getPolls());
        }

    }

    public void deletePoll(Long pollId) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.consensusproposal_reaffirmation));
        builder.setCancelable(false);
        builder.setNegativeButton(getString(R.string.yes), ((dialog, which) -> {
            try {

                this.controller.deletePoll(pollId);
                this.pollAdapter.updatePollList(this.controller.getPolls());

            } catch(PollCantBeDeletedException exception){

                ((ExceptionHandlingActivity)getActivity()).handleException(exception);
            }
        }));
        builder.setPositiveButton(getString(R.string.no), ((dialog, which) -> {
            dialog.cancel();
        }));

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void instantiatePollSwipeHelper() {

        new SwipeHelper(getActivity(), this.pollList ) {

            @Override
            public void instantiateUnderLayButton(RecyclerView.ViewHolder viewHolder, List<UnderLayButton> underLayButtons) {

                PollAdapter.PollViewHolder pollViewHolder = (PollAdapter.PollViewHolder)viewHolder;
                Poll currentPoll = pollViewHolder.getPoll();

                underLayButtons.add(new UnderLayButton(getString(R.string.delete), 0,
                        ResourcesCompat.getColor(getActivity().getResources(), R.color.default_red, null),
                        (UnderLayButtonClickListener) position -> {

                            pollAdapter.getPollList().remove(position);
                            pollAdapter.notifyItemRemoved(position);

                            ConsensusProposalOverviewAsyncTask consensusProposalOverviewAsyncTask = new ConsensusProposalOverviewAsyncTask("deletePoll");
                            consensusProposalOverviewAsyncTask.execute(currentPoll);
                        }));

                underLayButtons.add(new UnderLayButton(getString(R.string.detail), 0,
                        ResourcesCompat.getColor(getActivity().getResources(), R.color.default_blue, null),
                        (UnderLayButtonClickListener) position -> {
                            Intent intent = new Intent(getActivity(), ConsensusProposalDetail.class);
                            intent.putExtra("pollId", currentPoll.getPollId());
                            startActivity(intent);
                        }));

                if(!currentPoll.getIsOpen() && controller.isLocalAuthorModerator()) {

                    underLayButtons.add(new UnderLayButton(getString(R.string.open), 0,
                            ResourcesCompat.getColor(getActivity().getResources(), R.color.colorPrimary, null),
                            (UnderLayButtonClickListener) position -> {

                                ConsensusProposalOverviewAsyncTask consensusProposalOverviewAsyncTask = new ConsensusProposalOverviewAsyncTask("openPoll");
                                consensusProposalOverviewAsyncTask.execute(currentPoll);

                            }));

                }
            }
        };
    }

    public class ConsensusProposalOverviewAsyncTask extends AsyncTask<Object, Exception, String> {

        String flag;

        public ConsensusProposalOverviewAsyncTask(String flag) {

            this.flag = flag;
        }

        @Override
        protected void onProgressUpdate(Exception... values) {
            super.onProgressUpdate(values);
            ((ExceptionHandlingActivity)getActivity()).handleException(values[0]);
        }

        @Override
        protected String doInBackground(Object... objects) {

            String returnString = "";

            switch(flag) {

                case "update":
                    controller.update();
                    returnString = "update";
                    break;

                case "deletePoll":
                    Poll currentPoll = (Poll)objects[0];
                    deletePoll(currentPoll.getPollId());
                    returnString = "updatePoll";
                    break;

                case "openPoll":

                    Poll pollToOpen = (Poll)objects[0];

                    try {

                        controller.openPoll(pollToOpen.getPollId());

                    } catch(PollCantBeOpenedException exception){

                        publishProgress(exception);
                    }
                    returnString = "updatePoll";
                    break;


            }

            return returnString;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            switch(s) {

                case "update":
                    pollAdapter.updatePollList(controller.getPolls());
                    ((BaseActivity)getActivity()).getPullToRefresh().setRefreshing(false);
                    break;

                case "updatePoll":
                    pollAdapter.updatePollList(controller.getPolls());
                    break;
            }
        }
    }
}