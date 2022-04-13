package dhbw.smartmoderation.group.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collection;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.connection.synchronization.SynchronizableDataType;
import dhbw.smartmoderation.data.model.ConsensusLevel;
import dhbw.smartmoderation.exceptions.ConsensusLevelsNotFoundException;
import dhbw.smartmoderation.exceptions.CouldNotAddConsensusLevel;
import dhbw.smartmoderation.exceptions.CouldNotChangeConsensusLevel;
import dhbw.smartmoderation.exceptions.CouldNotDeleteConsensusLevel;
import dhbw.smartmoderation.uiUtils.OnStartDragListener;
import dhbw.smartmoderation.uiUtils.SimpleItemTouchHelperCallback;
import dhbw.smartmoderation.util.UpdateableExceptionHandlingActivity;

public class SettingsActivity extends UpdateableExceptionHandlingActivity implements OnStartDragListener, ConsensusLevelAdapter.OnConsensusLevelListener {

    private static final int REQUEST_CODE = 37;

    private SettingsController controller;
    private ConsensusLevelAdapter consensusLevelAdapter;
    private RecyclerView consensusLevelList;
    private LinearLayoutManager consensusLevelLayoutManager;
    private FloatingActionButton generalFab;
    private ItemTouchHelper itemTouchHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle(getString(R.string.settings_title));

        Intent intent = getIntent();
        Bundle extra = intent.getExtras();
        Long groupId = extra.getLong("groupId");

        controller = new SettingsController(groupId);

        consensusLevelList = findViewById(R.id.consensusLevelList);

        generalFab = findViewById(R.id.topFab);
        generalFab.setVisibility(View.VISIBLE);

        generalFab.setOnClickListener(v -> {

            startCreateConsensusLevelActivity(v, null, -1);

        });

        consensusLevelLayoutManager = new LinearLayoutManager(this);
        consensusLevelList.setLayoutManager(consensusLevelLayoutManager);

        try {
            consensusLevelAdapter = new ConsensusLevelAdapter(this, controller.getConsensusLevels(), this, this);

        } catch (ConsensusLevelsNotFoundException consensusLevelsNotFoundException) {

            handleException(consensusLevelsNotFoundException);
        }
        consensusLevelList.setAdapter(consensusLevelAdapter);
        DividerItemDecoration consensusLevelDividerItemDecoration = new DividerItemDecoration(consensusLevelList.getContext(), consensusLevelLayoutManager.getOrientation());
        consensusLevelList.addItemDecoration(consensusLevelDividerItemDecoration);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(consensusLevelAdapter, R.color.default_red, R.drawable.trash, ItemTouchHelper.START);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(consensusLevelList);
    }

    @Override
    protected  void onResume() {
        super.onResume();
        updateUI();
    }

    public void startCreateConsensusLevelActivity(View view, ConsensusLevel consensusLevel, int position) {

        Intent intent = new Intent(this, CreateConsensusLevelActivity.class);

        if(consensusLevel != null) {

            intent.putExtra("name", consensusLevel.getName());
            intent.putExtra("description", consensusLevel.getDescription());
            intent.putExtra("color", consensusLevel.getColor());
        }

        intent.putExtra("position", position);

        this.startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE) {

            if(resultCode == Activity.RESULT_OK) {

                Bundle extra = data.getExtras();

                if(extra.getInt("position") == -1) {

                    SettingsAsyncTask settingsAsyncTask = new SettingsAsyncTask("add");
                    settingsAsyncTask.execute(extra.getString("name"), extra.getInt("color"), extra.getString("description"));
                }

                else {


                    SettingsAsyncTask settingsAsyncTask = new SettingsAsyncTask("change");
                    settingsAsyncTask.execute(extra.getInt("position"), extra.getInt("color"), extra.getString("description"));
                }
            }
        }
    }

    @Override
    public Collection<SynchronizableDataType> getSynchronizableDataTypes() {
        Collection<SynchronizableDataType> dataTypes = new ArrayList<>();
        dataTypes.add(SynchronizableDataType.GROUPSETTINGS);
        dataTypes.add(SynchronizableDataType.CONSENSUSLEVEL);
        return dataTypes;
    }

    @Override
    protected void updateUI() {

        SettingsAsyncTask settingsAsyncTask = new SettingsAsyncTask("update");
        settingsAsyncTask.execute();
    }

    public void reloadConsensusLevelItemTouchHelper() {

        this.itemTouchHelper.attachToRecyclerView(null);

        try {

            this.consensusLevelAdapter.updateConsensusLevelList(this.controller.getConsensusLevels());

        } catch (ConsensusLevelsNotFoundException e) {

            e.printStackTrace();
        }

        this.itemTouchHelper.attachToRecyclerView(this.consensusLevelList);
    }

    @Override
    public void changeNumberingAfterOrderChange(ArrayList<ConsensusLevel> collection) {

        for (int i = 0; i < collection.size(); i++) {

            collection.get(i).setNumber(i+1);
        }

        for (int i = 0; i < collection.size(); i++) {

            final int j = i;
            Handler handler = new Handler();
            handler.post(() -> consensusLevelAdapter.notifyItemChanged(j));
        }

        SettingsAsyncTask settingsAsyncTask = new SettingsAsyncTask("numbering");
        settingsAsyncTask.execute(collection);
    }

    @Override
    public void onConsensusLevelDismiss(Long consensusLevelId) {

        SettingsAsyncTask settingsAsyncTask = new SettingsAsyncTask("delete");
        settingsAsyncTask.execute(consensusLevelId);
    }

    public class SettingsAsyncTask extends AsyncTask<Object, Exception, String> {

        String flag;

        public SettingsAsyncTask(String flag) {

            this.flag = flag;

        }

        @Override
        protected void onProgressUpdate(Exception... values) {
            super.onProgressUpdate(values);
            handleException(values[0]);
        }

        @Override
        protected String doInBackground(Object... objects) {

            String returnString = "";

            switch(flag) {

                case "add":

                    int number = consensusLevelAdapter.consensusLevelList.size() + 1;
                    String name = (String)objects[0];
                    int color = (int)objects[1];
                    String description = (String)objects[2];
                    ConsensusLevel consensusLevel = controller.createConsensusLevel(name, color, description, number);

                    try {

                        controller.addConsensusLevel(consensusLevel);

                    } catch (CouldNotAddConsensusLevel exception) {

                        publishProgress(exception);
                    }
                    returnString = "update";
                    break;

                case "change":

                    int position = (int)objects[0];
                    ConsensusLevel consensusLevelToChange = consensusLevelAdapter.consensusLevelList.get(position);
                    int newColor = (int)objects[1];
                    consensusLevelToChange.setColor(newColor);
                    String newDescription = (String)objects[2];
                    consensusLevelToChange.setDescription(newDescription);

                    try {

                        controller.changeConsensusLevel(consensusLevelToChange);

                    } catch (CouldNotChangeConsensusLevel exception) {

                        publishProgress(exception);
                    }
                    returnString = "update";
                    break;

                case "update":
                    controller.update();
                    returnString = "update";
                    break;

                case "numbering":

                    ArrayList<?> collection = (ArrayList<?>)objects[0];

                    ArrayList<ConsensusLevel> consensusLevels = new ArrayList<>();

                    for(Object o : collection) {

                        if(o instanceof ConsensusLevel) {

                            consensusLevels.add((ConsensusLevel) o);
                        }
                    }

                    try {

                        controller.changeConsensusLevelNumbering(consensusLevels);

                    } catch(CouldNotChangeConsensusLevel exception){

                        publishProgress(exception);
                    }
                    break;

                case "delete":

                    Long consensusLevelId = Long.valueOf(objects[0].toString());

                    try {

                        controller.deleteConsensusLevel(consensusLevelId);

                    } catch (CouldNotDeleteConsensusLevel exception) {

                        publishProgress(exception);
                    }
                    break;

            }

            return returnString;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            switch(s) {

                case "update":

                    try {

                        consensusLevelAdapter.updateConsensusLevelList(controller.getConsensusLevels());

                    } catch (ConsensusLevelsNotFoundException exception) {

                        handleException(exception);
                    }
                    break;

                case "":
                    break;

            }
        }
    }
}