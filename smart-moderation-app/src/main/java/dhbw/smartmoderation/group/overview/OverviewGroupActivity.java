package dhbw.smartmoderation.group.overview;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collection;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.connection.synchronization.SynchronizableDataType;
import dhbw.smartmoderation.data.model.Group;
import dhbw.smartmoderation.group.create.CreateGroup;
import dhbw.smartmoderation.group.detail.DetailGroupActivity;
import dhbw.smartmoderation.util.UpdateableExceptionHandlingActivity;


public class OverviewGroupActivity extends UpdateableExceptionHandlingActivity implements GroupAdapter.OnGroupListener {

    private static final String TAG = OverviewGroupActivity.class.getSimpleName();

    private OverviewGroupController controller;
    private GroupAdapter groupAdapter;
    private SwipeRefreshLayout pullToRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview_group);
        setTitle(R.string.title_activity_group_overview);

        pullToRefresh = findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(this::updateUI);

        controller = new OverviewGroupController();

        RecyclerView recGroup = findViewById(R.id.recGroupsOverView);

        FloatingActionButton createGroupButton = findViewById(R.id.btnAddGroup);
        createGroupButton.setOnClickListener(this::onAddGroup);

        LinearLayoutManager groupLayoutManager = new LinearLayoutManager(this);
        recGroup.setLayoutManager(groupLayoutManager);
        groupAdapter = new GroupAdapter(this, controller.getGroups(),this);
        recGroup.setAdapter(groupAdapter);
        DividerItemDecoration groupDividerItemDecoration = new DividerItemDecoration(recGroup.getContext(), groupLayoutManager.getOrientation());
        recGroup.addItemDecoration(groupDividerItemDecoration);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }


    @Override
    public void onGroupClick(Long groupId) {
        Intent detailGroupIntent = new Intent(this, DetailGroupActivity.class);
        detailGroupIntent.putExtra("groupId", groupId);
        startActivity(detailGroupIntent);
    }

    private void onAddGroup(View view){
        Intent createGroupIntent = new Intent(this, CreateGroup.class);
        startActivity(createGroupIntent);
    }

    @Override
    public Collection<SynchronizableDataType> getSynchronizableDataTypes() {
        Collection<SynchronizableDataType> dataTypes = new ArrayList<>();
        dataTypes.add(SynchronizableDataType.GROUP);
        return dataTypes;
    }

    @Override
    protected void updateUI() {
        OverviewGroupAsyncTask overviewGroupAsyncTask = new OverviewGroupAsyncTask();
        overviewGroupAsyncTask.execute();
    }


    @SuppressLint("StaticFieldLeak")
    public class OverviewGroupAsyncTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            controller.update();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Collection<Group> groups = controller.getGroups();
            groupAdapter.updateGroups(groups);
            Log.d(TAG, "Available groups: " + groups);
            pullToRefresh.setRefreshing(false);
        }
    }
}
