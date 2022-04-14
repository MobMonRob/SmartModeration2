package dhbw.smartmoderation.meeting.create;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.data.model.Meeting;
import dhbw.smartmoderation.data.model.Topic;
import dhbw.smartmoderation.exceptions.CantSubMitMeetingException;
import dhbw.smartmoderation.meeting.detail.BaseActivity;
import dhbw.smartmoderation.uiUtils.SimpleItemTouchHelperCallback;
import dhbw.smartmoderation.util.ExceptionHandlingActivity;
import dhbw.smartmoderation.util.Util;

public class CreateMeetingActivity extends ExceptionHandlingActivity implements TopicAdapter.OnTopicListener {

    private CreateMeetingController createMeetingController;
    private TopicAdapter topicAdapter;
    private ArrayList<Topic> dataSet = new ArrayList<>();

    private RadioGroup onlineSwitch;
    private RadioGroup openSwitch;
    private EditText beginDateInput;
    private EditText beginTimeInput;
    private EditText causeInput;
    private EditText locationInput;
    private RecyclerView topicList;
    private ItemTouchHelper itemTouchHelper;
    private LinearLayoutManager topicLayoutManager;
    private TextView endTag;
    private TextView expectedEndInput;
    private EditText plannedEndInput;

    private FloatingActionButton generalFab;
    private FloatingActionButton addTopicFab;
    private TextView addTopicFabText;
    private FloatingActionButton createMeetingFab;
    private TextView createMeetingFabText;

    private ArrayList<FloatingActionButton> fabList;
    private ArrayList<TextView> textList;
    private boolean allFabVisible;
    private ProgressDialog progressDialog;

    @Override
    public void onTopicClick(View view, Topic topic) {

        onChangeTopic(view, topic);

    }

    enum Answer {YES, NO};

    private View popUp;
    private AlertDialog alertDialog;
    String callback;

    private boolean isPrefilled;
    private Long meetingId;

    final Calendar calendar = Calendar.getInstance();

    private DatePickerDialog.OnDateSetListener date = (view, year, month, dayOfMonth) -> {
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        updateDateLabel();
    };

    private TimePickerDialog.OnTimeSetListener time = (view, hourOfDay, minute) -> {

        if(callback.equals("for_planned_end_time")) {

            updateTimeLabel(plannedEndInput, hourOfDay, minute);
        }

        else if(callback.equals("for_start_time")) {

            updateTimeLabel(beginTimeInput, hourOfDay, minute);
        }

        if(openSwitch.getCheckedRadioButtonId() == R.id.openOn) {
            calculateExceptedEnd();
        }

        else {
            checkTopicsLength();
        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_meeting);
        setTitle(getString(R.string.createMeeting_title));

        Intent intent = getIntent();

        Long groupId = intent.getLongExtra("groupId", 0);

        if(intent.hasExtra("activity")) {

            if(intent.getStringExtra("activity").equals("MeetingDetailFragment")) {

                this.isPrefilled = true;
                this.meetingId = intent.getLongExtra("meetingId", 0);
            }

        }

        createMeetingController = new CreateMeetingController(groupId);

        fabList = new ArrayList<>();
        textList = new ArrayList<>();

        onlineSwitch = findViewById(R.id.onlineMeetingSwitch);
        openSwitch = findViewById(R.id.openEndSwitch);
        causeInput = findViewById(R.id.causeInput);
        beginDateInput = findViewById(R.id.beginDateInput);
        beginTimeInput = findViewById(R.id.beginTimeInput);
        locationInput = findViewById(R.id.locationInput);

        topicList = findViewById(R.id.topicList);

        endTag= findViewById(R.id.plannedEndTag);
        plannedEndInput = findViewById(R.id.plannedEndInput);
        expectedEndInput = findViewById(R.id.expectedEndInput);
        expectedEndInput.setVisibility(View.VISIBLE);

        generalFab = findViewById(R.id.generalFab);

        addTopicFab = findViewById(R.id.addTopicFab);
        fabList.add(addTopicFab);

        addTopicFabText = findViewById(R.id.addTopicText);
        textList.add(addTopicFabText);

        createMeetingFab = findViewById(R.id.createMeetingFab);
        fabList.add(createMeetingFab);

        createMeetingFabText = findViewById(R.id.createMeetingText);
        textList.add(createMeetingFabText);

        addTopicFab.setVisibility(View.GONE);
        addTopicFabText.setVisibility(View.GONE);
        createMeetingFab.setVisibility(View.GONE);
        createMeetingFabText.setVisibility(View.GONE);

        allFabVisible = false;

        generalFab.setOnClickListener(v -> {

            if(!allFabVisible) {

                for(FloatingActionButton fab : fabList) {
                    fab.show();
                }

                for(TextView text : textList) {
                    text.setVisibility(View.VISIBLE);
                }

                allFabVisible = true;
            }

            else {

                for(FloatingActionButton fab : fabList) {
                    fab.hide();
                }

                for(TextView text : textList) {
                    text.setVisibility(View.GONE);
                }

                allFabVisible = false;
            }
        });

        addTopicFab.setOnClickListener(this::onAddTopic);
        createMeetingFab.setOnClickListener(this::onCreateFabClicked);

        beginDateInput.setOnClickListener(this::onSetStartDate);

        beginTimeInput.setOnClickListener(v -> {
            callback = "for_start_time";
            this.onSetStartTime(v);
        });

        plannedEndInput.setOnClickListener(v -> {
            callback = "for_planned_end_time";
            this.onSetStartTime(v);
        });

        topicLayoutManager = new LinearLayoutManager(this);
        topicList.setLayoutManager(topicLayoutManager);

        topicAdapter = new TopicAdapter(this, dataSet, this);
        topicList.setAdapter(topicAdapter);

        DividerItemDecoration topicsDividerItemDecoration = new DividerItemDecoration(topicList.getContext(), topicLayoutManager.getOrientation());
        topicList.addItemDecoration(topicsDividerItemDecoration);

        openSwitch.setOnCheckedChangeListener(((group, checkedId) -> {
            int id = openSwitch.getCheckedRadioButtonId();

            if(id == R.id.openOn) {

                endTag.setText(getString(R.string.estimatedEnd));
                plannedEndInput.setVisibility(View.GONE);
                expectedEndInput.setVisibility(View.VISIBLE);
                calculateExceptedEnd();
            }

            else {

                endTag.setText(getString(R.string.plannedEnd));
                plannedEndInput.setVisibility(View.VISIBLE);
                expectedEndInput.setVisibility(View.GONE);
                checkTopicsLength();
            }

        }));

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(topicAdapter, R.color.default_red, R.drawable.trash, ItemTouchHelper.START);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(topicList);
    }

    @Override
    protected void onResume() {

        if(this.isPrefilled) {
            prefill();
        }

        super.onResume();
    }

    private void prefill() {

        Meeting meeting = this.createMeetingController.getMeeting(this.meetingId);

        this.causeInput.setText(meeting.getCause());
        this.locationInput.setText(meeting.getLocation());
        this.beginDateInput.setText(meeting.getDateAsString());
        this.beginTimeInput.setText(Util.convertMilliSecondsToTimeString(meeting.getStartTime()));
        this.topicAdapter.updateTopics(meeting.getTopics());

        if(meeting.getOnline()) {

            this.onlineSwitch.check(R.id.onlineOn);
        }

        else {

            this.onlineSwitch.check(R.id.onlineOff);
        }

        if(meeting.getOpen()) {

            this.openSwitch.check(R.id.openOn);
            calculateExceptedEnd();
        }

        else {

            this.openSwitch.check(R.id.openOff);
            this.plannedEndInput.setText(Util.convertMilliSecondsToTimeString(meeting.getEndTime()));
            checkTopicsLength();
        }
    }

    private void onCreateFabClicked(View view) {

        if (openSwitch.getCheckedRadioButtonId() == R.id.openOn) {

            if (!causeInput.getText().toString().isEmpty() && !beginDateInput.getText().toString().isEmpty() && !beginTimeInput.getText().toString().isEmpty()
                    && !locationInput.getText().toString().isEmpty()) {

                createMeeting(true);
                return;
            }

        } else {

            if (!causeInput.getText().toString().isEmpty() && !beginDateInput.getText().toString().isEmpty() && !beginTimeInput.getText().toString().isEmpty()
                    && !locationInput.getText().toString().isEmpty() && !plannedEndInput.getText().toString().isEmpty()) {

                createMeeting(false);
                return;
            }

        }

        createMessageDialog();
    }

    private void createMessageDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.allFieldsMustBeFilled));
        builder.setCancelable(false);
        builder.setNeutralButton(getString(R.string.ok), (dialog, which) -> {
            dialog.cancel();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void createMeeting(boolean open) {

        long date = Util.convertDateStringToMilliSeconds(beginDateInput.getText().toString());

        long begin = Util.convertTimeStringToMilliSeconds(beginTimeInput.getText().toString());

        boolean online = false;

        if (onlineSwitch.getCheckedRadioButtonId() == R.id.onlineOn) {

            online = true;
        }

        long end = 0;

        if(!open) {

            end = Util.convertTimeStringToMilliSeconds(plannedEndInput.getText().toString());
        }

        CreateMeetingAsyncTask createMeetingAsyncTask = new CreateMeetingAsyncTask();
        createMeetingAsyncTask.execute(open, online, causeInput.getText().toString(), date, begin, locationInput.getText().toString(), end);
    }

    private void onSetStartTime(View view) {

        TimePickerDialog timePicker = new TimePickerDialog(this, R.style.TimePickerTheme, time, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
        timePicker.show();
    }

    private void onSetStartDate(View view) {

        new DatePickerDialog(this, R.style.TimePickerTheme, date, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }


    private void onChangeTopic(View view, Topic topic) {

        LayoutInflater inflater = LayoutInflater.from(this);
        popUp = inflater.inflate(R.layout.popup_topic, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(popUp);

        alertDialog = alertDialogBuilder.create();

        Button addBtn = popUp.findViewById(R.id.addButton);

        addBtn.setOnClickListener(v -> {
            userChoice(Answer.YES, topic);
            alertDialog.cancel();
        });

        Button cancelBtn = popUp.findViewById(R.id.cancelButton);

        cancelBtn.setOnClickListener(v -> {
            userChoice(Answer.NO, null);
            alertDialog.cancel();
        });

        EditText titleInput = popUp.findViewById(R.id.title_input);
        titleInput.setText(topic.getTitle());

        EditText durationInput = popUp.findViewById(R.id.duration_input);
        durationInput.setText(String.valueOf(Util.milliSecondsToMinutes(topic.getDuration())));

        alertDialog.show();

    }

    private void onAddTopic(View view) {

        LayoutInflater inflater = LayoutInflater.from(this);
        popUp = inflater.inflate(R.layout.popup_topic, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(popUp);

        alertDialog = alertDialogBuilder.create();

        Button addBtn = popUp.findViewById(R.id.addButton);

        addBtn.setOnClickListener(v -> {
            userChoice(Answer.YES, null);
            alertDialog.cancel();
        });

        Button cancelBtn = popUp.findViewById(R.id.cancelButton);

        cancelBtn.setOnClickListener(v -> {
            userChoice(Answer.NO, null);
            alertDialog.cancel();
        });

        alertDialog.show();
    }

    public void userChoice(Answer choice, Topic topic) {

        if(choice == Answer.YES) {

            EditText titleInput = popUp.findViewById(R.id.title_input);
            EditText durationInput = popUp.findViewById(R.id.duration_input);

            boolean onlyNumbers = durationInput.getText().toString().matches("[0-9]+") && durationInput.getText().toString().length() > 0;

            if(!(titleInput.getText().toString().isEmpty()) && !(durationInput.getText().toString().isEmpty()) && onlyNumbers) {

                String title = String.valueOf(titleInput.getText());
                String duration = String.valueOf(durationInput.getText());

                if(topic != null) {

                    topicAdapter.updateTopic(topic, title, duration);
                }

                else {

                    topicAdapter.addTopic(title, duration);

                }

                if(openSwitch.getCheckedRadioButtonId() == R.id.openOn) {
                    calculateExceptedEnd();
                }

                else {
                    checkTopicsLength();
                }
            }
            else {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.bothFieldNeedToBeFilled));
                builder.setCancelable(false);
                builder.setNeutralButton(getString(R.string.ok), ((dialog, which) -> {dialog.cancel();}));
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }

        }
    }

    public void checkTopicsLength() {

        String beginTime = String.valueOf(beginTimeInput.getText());
        String plannedEnd = String.valueOf(plannedEndInput.getText());

        if(openSwitch.getCheckedRadioButtonId() == R.id.openOff && !beginTime.isEmpty() && !plannedEnd.isEmpty()) {

            long milliseconds = Util.convertTimeStringToMilliSeconds(beginTime);

            for(Topic topic : topicAdapter.getTopicList()) {

                milliseconds += topic.getDuration();
            }

            long endMilliseconds = Util.convertTimeStringToMilliSeconds(plannedEnd);

            if(milliseconds > endMilliseconds) {

                topicList.setBackground(this.getResources().getDrawable(R.drawable.red_background_border, null));
                return;
            }
        }

        topicList.setBackground(this.getResources().getDrawable(R.drawable.border, null));
    }

    public void updateDateLabel() {

        String format = "dd.MM.yyyy";
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.GERMANY);
        this.beginDateInput.setText(dateFormat.format(calendar.getTime()));
    }

    public void updateTimeLabel(EditText input, int hour, int minute) {

        String minuteStr = minute + "";

        if(minute < 10) {

            minuteStr = 0 +  minuteStr;
        }

        input.setText(hour + ":" + minuteStr);

    }

    public void calculateExceptedEnd() {

        endTag.setText(getString(R.string.estimatedEnd));

        String beginTime = String.valueOf(beginTimeInput.getText());

        if (!beginTime.isEmpty()) {

           long milliseconds = Util.convertTimeStringToMilliSeconds(beginTime);

            for (Topic topic : topicAdapter.getTopicList()) {

                milliseconds += topic.getDuration();
            }

            String expectedEnd = Util.convertMilliSecondsToTimeString(milliseconds);

            expectedEndInput.setText(expectedEnd);
        }

        else {

            expectedEndInput.setText("");
        }

    }

    public class CreateMeetingAsyncTask extends AsyncTask<Object, Exception, String> {

        @Override
        protected void onPreExecute() {

            super.onPreExecute();

            progressDialog = new ProgressDialog(CreateMeetingActivity.this, R.style.MyAlertDialogStyle);

            if(isPrefilled) {

                progressDialog.setMessage(getString(R.string.updating_meeting));
            }

            else {

                progressDialog.setMessage(getString(R.string.creating_meeting));
            }

            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Object... objects) {

            boolean open = (Boolean)objects[0];
            boolean online = (Boolean)objects[1];
            String cause = objects[2].toString();
            long date = (long)objects[3];
            long begin = (long)objects[4];
            String location = objects[5].toString();
            long end = (long)objects[6];

            try {

                if(isPrefilled) {

                    createMeetingController.submitMeeting(meetingId, open, online, cause, date, begin, location, end, topicAdapter.getTopicList());
                }

                else {

                    createMeetingController.submitMeeting(null, open,online, cause, date, begin, location, end, topicAdapter.getTopicList());
                }


            } catch (CantSubMitMeetingException exception) {

                publishProgress(exception);
            }

            return null;

        }

        @Override
        protected void onProgressUpdate(Exception... values) {
            super.onProgressUpdate(values);
            progressDialog.dismiss();
            handleException(values[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            progressDialog.dismiss();
            Toast toast;

            if(isPrefilled) {

                Intent meetingDetailIntent = new Intent(CreateMeetingActivity.this, BaseActivity.class);
                meetingDetailIntent.putExtra("meetingId", meetingId);
                startActivity(meetingDetailIntent);
                toast = Toast.makeText(getApplicationContext(), getString(R.string.meeting_updated), Toast.LENGTH_SHORT);
            }

            else {

                toast = Toast.makeText(getApplicationContext(), getString(R.string.meeting_created), Toast.LENGTH_SHORT);
            }

            toast.show();
            finish();
        }
    }


}