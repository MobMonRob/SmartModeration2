package dhbw.smartmoderation.group.create;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.briarproject.bramble.api.plugin.duplex.DuplexPlugin;

import java.util.ArrayList;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.SmartModerationApplicationImpl;
import dhbw.smartmoderation.exceptions.CantCreateGroupException;
import dhbw.smartmoderation.exceptions.NoContactsFoundException;
import dhbw.smartmoderation.util.ExceptionHandlingActivity;
import dhbw.smartmoderation.util.Util;

public class CreateGroup extends ExceptionHandlingActivity {

    private RecyclerView recyclerView;
    private ContactAdapter contactAdapter;
    private CreateGroupController createGroupController;
    private LinearLayoutManager contactLayoutManager;
    private EditText groupName;
    private FloatingActionButton generalFab;
    private FloatingActionButton createGroupFab;
    private FloatingActionButton addGhostFab;
    private TextView addGroupText;
    private TextView addGhostText;
    private ArrayList<FloatingActionButton> fabList;
    private ArrayList<TextView> textList;
    private boolean allFabVisible;
    private View popup;
    private AlertDialog alertDialog;

    enum Answer {YES, NO}

    ;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        for (DuplexPlugin duplexPlugin : ((SmartModerationApplicationImpl) SmartModerationApplicationImpl.getApp()).getConnectionService().getPluginManager().getDuplexPlugins()) {
            System.out.println("Duplex " + duplexPlugin.getId() + " | " + duplexPlugin.getState());
        }
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.CreateGroup_title));
        setContentView(R.layout.activity_create_group);
        fabList = new ArrayList<>();
        textList = new ArrayList<>();

        recyclerView = findViewById(R.id.contactList);
        groupName = findViewById(R.id.groupNameInput);

        generalFab = findViewById(R.id.generalFab);

        createGroupFab = findViewById(R.id.createGroupFab);
        fabList.add(createGroupFab);

        addGhostFab = findViewById(R.id.addGhostFab);
        fabList.add(addGhostFab);

        addGroupText = findViewById(R.id.createGroupText);
        textList.add(addGroupText);

        addGhostText = findViewById(R.id.addGhostText);
        textList.add(addGhostText);

        generalFab.setVisibility(View.VISIBLE);
        createGroupFab.setVisibility(View.GONE);
        addGhostFab.setVisibility(View.GONE);
        addGroupText.setVisibility(View.GONE);
        addGhostText.setVisibility(View.GONE);

        allFabVisible = false;

        generalFab.setOnClickListener(v -> {

            if (!allFabVisible) {
                for (FloatingActionButton fab : fabList) {
                    fab.show();
                }

                for (TextView text : textList) {
                    text.setVisibility(View.VISIBLE);
                }

                allFabVisible = true;
                onInputChange();
            } else {
                for (FloatingActionButton fab : fabList)
                    fab.hide();

                for (TextView text : textList)
                    text.setVisibility(View.GONE);

                allFabVisible = false;
            }
        });

        createGroupFab.setOnClickListener(this::onCreateGroup);

        addGhostFab.setOnClickListener(this::onAddGhost);

        createGroupController = new CreateGroupController(this);
        contactLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(contactLayoutManager);

        try {
            contactAdapter = new ContactAdapter(this, createGroupController.getContacts());
            recyclerView.setAdapter(contactAdapter);
        } catch (NoContactsFoundException exception) {
            handleException(exception);
        }

        DividerItemDecoration contactsDividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), contactLayoutManager.getOrientation());
        recyclerView.addItemDecoration(contactsDividerItemDecoration);
    }

    public void onInputChange() {
        if (!Util.isEmpty(groupName) && contactAdapter.atLeastOneContactSelected()) {
            createGroupFab.show();
            addGroupText.setVisibility(View.VISIBLE);
        } else {
            createGroupFab.hide();
            addGroupText.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            contactAdapter.updateContacts(createGroupController.getContacts());
        } catch (NoContactsFoundException exception) {
            handleException(exception);
        }
    }


    private void onCreateGroup(View view) {
        String groupName = Util.getText(this.groupName);
        CreateGroupAsyncTask createGroupAsyncTask = new CreateGroupAsyncTask();
        createGroupAsyncTask.execute(groupName);
    }

    private void onAddGhost(View view) {
        LayoutInflater inflater = LayoutInflater.from(this);
        popup = inflater.inflate(R.layout.popup_add_ghost, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(popup);

        alertDialogBuilder.setCancelable(false);

        alertDialog = alertDialogBuilder.create();

        Button addBtn = popup.findViewById(R.id.addButton);

        addBtn.setOnClickListener(v -> {
            userChoice(Answer.YES);
            alertDialog.cancel();
        });

        Button cancelBtn = popup.findViewById(R.id.cancelButton);

        cancelBtn.setOnClickListener(v -> {
            userChoice(Answer.NO);
            alertDialog.cancel();
        });

        alertDialog.show();
    }

    public void userChoice(Answer choice) {
        if (choice == Answer.YES) {
            EditText firstNameInput = popup.findViewById(R.id.firstNameInput);
            EditText lastNameInput = popup.findViewById(R.id.lastNameInput);
            String firstName = String.valueOf(firstNameInput.getText());
            String lastName = String.valueOf(lastNameInput.getText());
            contactAdapter.addGhost(firstName, lastName);
        } else {
            alertDialog.cancel();
        }
    }


    public class CreateGroupAsyncTask extends AsyncTask<String, Exception, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(CreateGroup.this, R.style.MyAlertDialogStyle);
            progressDialog.setMessage(getString(R.string.creating_group));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {

            String groupName = strings[0];

            try {
                createGroupController.createGroup(groupName, CreateGroup.this.contactAdapter.getSelectedContacts());
            } catch (CantCreateGroupException exception) {
                publishProgress(exception);
            }

            return groupName;
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
            Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.group_created), Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }
    }
}